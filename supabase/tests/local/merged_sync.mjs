// Database-side checks for merged lectures and the incremental sync that carries them (period_sessions).
// Runs against the scratch cms_test database (see apply.mjs); everything happens in one rolled-back transaction.
import { pg, DB } from "./harness.mjs";

const c = new pg.Client({ ...DB, database: "cms_test" });
await c.connect();
const results = [];
const check = (name, ok, detail = "") => results.push({ name, ok: !!ok, detail });
async function expectError(name, sql, match) {
  await c.query("savepoint e");
  try {
    await c.query(sql);
    await c.query("release savepoint e");
    check(name, false, "expected an error but it succeeded");
  } catch (e) {
    await c.query("rollback to savepoint e");
    check(name, !match || e.message.includes(match), e.message);
  }
}
const one = async (sql, p) => (await c.query(sql, p)).rows[0];
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

await c.query("begin");
await c.query(`
 insert into departments(dept_id,name,code) values ('ch','Chemistry','CH'),('ur','Urdu','UR'),('ph','Physics','PH');
 insert into teachers(email,name,dept_id) values ('t1@x.pk','Teacher One','ch'),('t2@x.pk','Teacher Two','ur');
 insert into academic_sessions(session_id,dept_id,start_year,end_year,max_students,current_semester,shift_mode,program_type) values
   ('ch_2023','ch',2023,2027,50,3,'MORNING','BS'),
   ('ur_2023','ur',2023,2027,50,3,'MORNING','BS'),
   ('ph_2023','ph',2023,2027,50,3,'EVENING','BS');
 insert into session_students(session_id,roll_number,name,shift) values
   ('ch_2023','R-01','Ali','MORNING'),('ur_2023','R-01','Sara','MORNING');
`);

// The owning session's lecture, and a link to a second session.
const p = await one(`insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift)
  values ('ch_2023','MONDAY','09:00','10:00','GE-101','English','t1@x.pk','R1','MORNING') returning id, updated_at`);
await c.query("insert into period_sessions(period_id,session_id) values ($1,'ur_2023')", [p.id]);

// 1. The sync reads: active links for a period, and for a guest session.
check("links readable by period", (await c.query("select 1 from period_sessions where period_id=$1 and is_deleted=false", [p.id])).rowCount === 1);
check("links readable by guest session", (await c.query("select 1 from period_sessions where session_id='ur_2023' and is_deleted=false")).rowCount === 1);

// 2. Same-shift guard.
await expectError("link to a session that does not run the shift is refused", `insert into period_sessions(period_id,session_id) values ('${p.id}','ph_2023')`);

// 3. Idempotent re-link (setPeriodLink upserts on period_id,session_id) revives a soft-deleted link.
await c.query("update period_sessions set is_deleted=true where period_id=$1 and session_id='ur_2023'", [p.id]);
check("unlink hides the link from the sync read", (await c.query("select 1 from period_sessions where period_id=$1 and is_deleted=false", [p.id])).rowCount === 0);
await c.query(`insert into period_sessions(period_id,session_id,is_deleted) values ($1,'ur_2023',false)
  on conflict (period_id,session_id) do update set is_deleted=excluded.is_deleted`, [p.id]);
check("re-link revives the same row", (await c.query("select 1 from period_sessions where period_id=$1 and is_deleted=false", [p.id])).rowCount === 1
  && (await one("select count(*)::int n from period_sessions where period_id=$1", [p.id])).n === 1);

// 4. Incremental sync: changes bump updated_at so a checkpoint-based pull sees them.
const before = await one("select updated_at from period_sessions where period_id=$1", [p.id]);
await sleep(20);
await c.query("update period_sessions set is_deleted=true, updated_at='2000-01-01' where period_id=$1", [p.id]);
const after = await one("select updated_at from period_sessions where period_id=$1", [p.id]);
// now() is fixed inside a transaction, so prove the touch trigger by overwriting updated_at with a stale value.
check("period_sessions.updated_at is refreshed on change", after.updated_at >= before.updated_at && after.updated_at.getFullYear() > 2000);
await c.query("update period_sessions set is_deleted=false where period_id=$1", [p.id]);
const pBefore = await one("select updated_at from timetable_periods where id=$1", [p.id]);
await sleep(20);
await c.query("update timetable_periods set is_deleted=true, updated_at='2000-01-01' where id=$1", [p.id]);
const pAfter = await one("select updated_at, is_deleted from timetable_periods where id=$1", [p.id]);
check("timetable_periods soft delete bumps updated_at", pAfter.updated_at >= pBefore.updated_at && pAfter.updated_at.getFullYear() > 2000 && pAfter.is_deleted === true);
await c.query("update timetable_periods set is_deleted=false where id=$1", [p.id]);

// 5. Conflicts: a merged lecture is one row (no self clash); a second row with the same teacher/time clashes.
await expectError("a second class with the same teacher and time is refused",
  `insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift)
   values ('ur_2023','MONDAY','09:00','10:00','GE-101','English','t1@x.pk','R2','MORNING')`);
const q = await one(`insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift)
   values ('ur_2023','MONDAY','09:00','10:00','GE-101','English','t2@x.pk','R2','MORNING') returning id`);
check("unmerged class with its own teacher and room is accepted", !!q.id);

// 6. Deleting the lecture soft-deletes its links (what removePeriod does), so the sync read drops them.
await c.query("update period_sessions set is_deleted=true where period_id=$1", [p.id]);
check("removed lecture leaves no active links", (await c.query("select 1 from period_sessions where period_id=$1 and is_deleted=false", [p.id])).rowCount === 0);

// 7. RLS: the teacher of a merged lecture teaches the linked session's students (attendance/marks access).
await c.query("update period_sessions set is_deleted=false where period_id=$1", [p.id]);
async function asTeacher(email, sql) {
  await c.query("savepoint u");
  await c.query("select set_config('request.jwt.claims', $1, true), set_config('request.jwt.claim.sub', $2, true)", [JSON.stringify({ email, sub: "00000000-0000-4000-8000-000000000002", role: "authenticated" }), "00000000-0000-4000-8000-000000000002"]);
  await c.query("set local role authenticated");
  let r;
  try { r = (await c.query(sql)).rows[0]; } catch (e) { r = { error: e.message }; }
  await c.query("rollback to savepoint u");
  await c.query("reset role");
  return r;
}
const own = await asTeacher("t1@x.pk", "select teaches_student('ch_2023','R-01') as v");
const linked = await asTeacher("t1@x.pk", "select teaches_student('ur_2023','R-01') as v");
const stranger = await asTeacher("t2@x.pk", "select teaches_student('ch_2023','R-01') as v");
check("teacher teaches own session's student", own?.v === true, JSON.stringify(own));
check("teacher teaches the linked session's student", linked?.v === true, JSON.stringify(linked));
check("unrelated teacher does not teach it", stranger?.v === false, JSON.stringify(stranger));

await c.query("rollback");
await c.end();

let failed = 0;
for (const r of results) {
  if (!r.ok) failed++;
  console.log(`${r.ok ? "PASS" : "FAIL"}  ${r.name}${r.ok ? "" : "  -> " + r.detail}`);
}
console.log(`\n${results.length - failed}/${results.length} passed`);
process.exitCode = failed ? 1 : 0;
