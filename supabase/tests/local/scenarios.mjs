import { pg, ROOT, OUT, DB } from "./harness.mjs";
import path from "node:path";
import fs from "node:fs";
const c = new pg.Client({ ...DB, database: "cms_test" });
await c.connect();
const results = [];
async function seed(sql) { await c.query(sql); }
async function tryIt(name, sql, params) {
  await c.query("savepoint s");
  try {
    await c.query(sql, params);
    results.push({ name, ok: true });
    await c.query("release savepoint s");
  } catch (e) {
    await c.query("rollback to savepoint s");
    results.push({ name, code: e.code, message: e.message, detail: e.detail ?? null, hint: e.hint ?? null, constraint: e.constraint ?? null, table: e.table ?? null, column: e.column ?? null });
  }
}
const names = await c.query("select conname as n from pg_constraint where connamespace='public'::regnamespace union select indexname from pg_indexes where schemaname='public' union select tablename from pg_tables where schemaname='public'");
fs.writeFileSync(path.join(OUT, "db-names.txt"), names.rows.map((r) => r.n).join(String.fromCharCode(10)));
await c.query("begin");
await seed(`
 insert into departments(dept_id,name,code) values ('eng','English','ENG'),('cs','Computer Science','CS');
 insert into buildings(building_id,name,code) values ('b1','Main Block','MB');
 insert into rooms(room_id,building_id,room_no,name) values ('r1','b1','R14','Room 14'),('r2','b1','R15','Room 15');
 insert into teachers(email,name,dept_id) values ('jane@x.pk','Jane Doe','eng'),('omar@x.pk','Omar Ali','eng');
 insert into academic_sessions(session_id,dept_id,start_year,end_year,max_students,current_semester,shift_mode,program_type)
   values ('eng_2023','eng',2023,2027,2,3,'MORNING','BS');
 insert into session_students(session_id,roll_number,name,shift) values ('eng_2023','ENG-23-01','Ali','MORNING');
 insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift)
   values ('eng_2023','MONDAY','09:00','10:00','ENG-301','Poetry','jane@x.pk','R14','MORNING');
 insert into notifications(id,title,body,created_by_email) values ('88888888-8888-4888-8888-888888888888','Exam notice','Bring your card','jane@x.pk');
 insert into datesheets(id,session_id,semester,shift) values ('11111111-1111-1111-1111-111111111111','eng_2023',3,'MORNING');
 insert into datesheet_slots(datesheet_id,exam_date,start_time,end_time,course_code,subject_name,room_id,invigilator_email)
   values ('11111111-1111-1111-1111-111111111111','2026-05-12','09:00','12:00','ENG-301','Poetry','r1','jane@x.pk');
`);
const tt = (extra) => `insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift) values ${extra}`;
const ds = (extra) => `insert into datesheet_slots(datesheet_id,exam_date,start_time,end_time,course_code,subject_name,room_id,invigilator_email) values ${extra}`;
// --- trigger messages ---
await tryIt("timetable teacher clash", tt(`('eng_2023','MONDAY','09:30','10:30','ENG-302','Prose','jane@x.pk','R15','MORNING')`));
const ttCo = (extra) => `insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift,co_teacher_emails) values ${extra}`;
await tryIt("timetable co-teacher clash", ttCo(`('eng_2023','MONDAY','09:30','10:30','ENG-303','Project','omar@x.pk','R15','MORNING',array['jane@x.pk'])`));
await tryIt("timetable co-teacher ok", ttCo(`('eng_2023','TUESDAY','09:00','10:00','ENG-304','Project','omar@x.pk','R15','MORNING',array['jane@x.pk'])`));
await tryIt("timetable co-teacher without main teacher", `insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,shift,co_teacher_emails) values ('eng_2023','WEDNESDAY','09:00','10:00','ENG-305','Project','MORNING',array['jane@x.pk'])`);
await tryIt("timetable co-teacher is the main teacher", ttCo(`('eng_2023','WEDNESDAY','11:00','12:00','ENG-306','Project','omar@x.pk','R15','MORNING',array['omar@x.pk'])`));
await tryIt("timetable co-teacher not a teacher", ttCo(`('eng_2023','THURSDAY','09:00','10:00','ENG-307','Project','omar@x.pk','R15','MORNING',array['ghost@x.pk'])`));
await tryIt("absent student marked late", `insert into session_attendance(session_id,semester,course_code,date,roll_number,status,is_late,teacher_email) values ('eng_2023',3,'ENG-301','2026-10-05','ENG-23-01','ABSENT',true,'jane@x.pk')`);
await tryIt("present student marked late is fine", `insert into session_attendance(session_id,semester,course_code,date,roll_number,status,is_late,teacher_email) values ('eng_2023',3,'ENG-301','2026-10-06','ENG-23-01','PRESENT',true,'jane@x.pk')`);
await tryIt("timetable room clash", tt(`('eng_2023','MONDAY','09:30','10:30','ENG-302','Prose','omar@x.pk','R14','MORNING')`));
await seed("insert into session_students(session_id,roll_number,name,shift) values ('eng_2023','ENG-23-02','Filler','MORNING')");
await tryIt("roster full", `insert into session_students(session_id,roll_number,name,shift) values ('eng_2023','ENG-23-02','Sara','MORNING')`);
await seed("update academic_sessions set max_students=50 where session_id='eng_2023'");
await tryIt("wrong shift for session", `insert into session_students(session_id,roll_number,name,shift) values ('eng_2023','ENG-23-03','Sara','EVENING')`);
await tryIt("roll block violation", `insert into session_students(session_id,roll_number,name,shift) values ('eng_2023','ENG-23-599','Sara','MORNING')`);
await tryIt("datesheet room clash", ds(`('11111111-1111-1111-1111-111111111111','2026-05-12','10:00','11:00','ENG-302','Prose','r1','omar@x.pk')`));
await tryIt("datesheet invigilator clash", ds(`('11111111-1111-1111-1111-111111111111','2026-05-12','10:00','11:00','ENG-302','Prose','r2','jane@x.pk')`));
await tryIt("switch session to evening only with morning students", `update academic_sessions set shift_mode='EVENING' where session_id='eng_2023'`);
await tryIt("timetable row for a shift the session does not run", tt(`('eng_2023','TUESDAY','09:00','10:00','ENG-301','Poetry',null,null,'EVENING')`));
// --- constraint messages ---
const sess = (vals, cols = "session_id,dept_id,start_year,end_year,current_semester,shift_mode,program_type") => `insert into academic_sessions(${cols}) values ${vals}`;
await tryIt("dup department code", `insert into departments(dept_id,name,code) values ('eng2','English 2','ENG')`);
await tryIt("dup department id", `insert into departments(dept_id,name,code) values ('eng','English 3','ENG3')`);
await tryIt("dup session same id", sess(`('eng_2023','eng',2023,2027,1,'MORNING','BS')`));
await tryIt("bad session id format", sess(`('weird','eng',2024,2028,1,'MORNING','BS')`));
await tryIt("bad semester", sess(`('eng_2024','eng',2024,2028,9,'MORNING','BS')`));
await tryIt("bad end year", sess(`('eng_2024','eng',2024,2030,1,'MORNING','BS')`));
await tryIt("bad max students", sess(`('eng_2024','eng',2024,2028,1,'MORNING','BS',0)`, "session_id,dept_id,start_year,end_year,current_semester,shift_mode,program_type,max_students"));
await tryIt("session missing department (fk)", sess(`('zzz_2024','zzz',2024,2028,1,'MORNING','BS')`));
await tryIt("dup student roll", `insert into session_students(session_id,roll_number,name,shift) values ('eng_2023','ENG-23-01','Ali again','MORNING')`);
await tryIt("student missing name", `insert into session_students(session_id,roll_number,name,shift) values ('eng_2023','ENG-23-04',null,'MORNING')`);
await tryIt("student too-long name", `insert into session_students(session_id,roll_number,name,shift) values ('eng_2023','ENG-23-05', repeat('x',5000),'MORNING')`);
await tryIt("dup room number", `insert into rooms(room_id,building_id,room_no) values ('r9','b1','R14')`);
await tryIt("room unknown building (fk)", `insert into rooms(room_id,building_id,room_no) values ('r10','nope','R99')`);
await tryIt("dup teacher email", `insert into teachers(email,name,dept_id) values ('jane@x.pk','Jane Again','eng')`);
await tryIt("teacher unknown dept (fk)", `insert into teachers(email,name,dept_id) values ('new@x.pk','New','nope')`);
await tryIt("dup building id", `insert into buildings(building_id,name,code) values ('b1','Main Block','MB')`);
await tryIt("timetable end before start", tt(`('eng_2023','TUESDAY','10:00','09:00','ENG-301','Poetry',null,null,'MORNING')`));
await tryIt("bad enum value", `insert into session_students(session_id,roll_number,name,shift) values ('eng_2023','ENG-23-06','X','NIGHT')`);

// --- RPCs and guards, called as signed-in users ---
async function asUser(name, email, sql) {
  await c.query("savepoint u");
  try {
    await c.query("select set_config('request.jwt.claims', $1, true), set_config('request.jwt.claim.sub', $2, true)", [JSON.stringify({ email, sub: "00000000-0000-4000-8000-000000000001", role: "authenticated" }), "00000000-0000-4000-8000-000000000001"]);
    await c.query("set local role authenticated");
    await c.query(sql);
    results.push({ name, ok: true });
  } catch (e) {
    results.push({ name, code: e.code, message: e.message, detail: e.detail ?? null, hint: e.hint ?? null, constraint: e.constraint ?? null, table: e.table ?? null, column: e.column ?? null });
  }
  await c.query("rollback to savepoint u");
  await c.query("reset role");
}
const RID = "99999999-9999-4999-8999-999999999999";
await asUser("approve link request without permission", "nobody@x.pk", `select approve_link_request('${RID}', 'nobody@x.pk')`);
await asUser("approve link request that no longer exists", "admin@example.com", `select approve_link_request('${RID}', 'admin@example.com')`);
await asUser("approve attendance edit without being admin", "nobody@x.pk", `select approve_attendance_edit_request('${RID}', 'nobody@x.pk')`);
await asUser("approve attendance edit that no longer exists", "admin@example.com", `select approve_attendance_edit_request('${RID}', 'admin@example.com')`);
await asUser("record result without permission", "nobody@x.pk", `select record_semester_result('eng_2023','ENG-23-01',3,3.0,3.0)`);
await asUser("delete a notification someone else sent", "nobody@x.pk", `select delete_notification('88888888-8888-4888-8888-888888888888')`);
await asUser("delete a notification that no longer exists", "jane@x.pk", `select delete_notification('${RID}')`);
await asUser("delete your own notification", "jane@x.pk", `select delete_notification('88888888-8888-4888-8888-888888888888')`);
await asUser("upload app logs (idempotent)", "jane@x.pk", `select ingest_app_logs('[{"log_id":"L1","occurred_at":"2026-09-30T10:00:00Z","severity":"CRITICAL","message":"boom","account_email":"jane@x.pk"}]'::jsonb), ingest_app_logs('[{"log_id":"L1","occurred_at":"2026-09-30T10:00:00Z","severity":"CRITICAL","message":"boom","account_email":"jane@x.pk"}]'::jsonb)`);
await c.query("rollback");
fs.writeFileSync(path.join(OUT, "scenario-results.json"), JSON.stringify(results, null, 2));
for (const r of results) console.log(r.ok ? `OK(no error)  ${r.name}` : `${r.code}  ${r.name}\n      msg: ${r.message}${r.detail ? "\n      detail: " + r.detail : ""}`);
await c.end();
