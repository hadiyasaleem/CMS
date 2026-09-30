// archive-and-delete-session — Free tier has no automated backups, so this is
// the backup story: export EVERYTHING about an inactive session to a JSON file
// in the documents/archives/ folder (service-role only), then cascade-delete
// the session (FKs remove roster/attendance/marks/gpa/fees/fines/periods) and
// purge its exam-paper blobs.
//
// POST { sessionId }
import { dbError, handle, httpError, ok, readJson, requireAdmin, serviceClient } from "../_shared/auth.ts";

const TABLES = [
  "session_subjects", "session_students", "session_attendance", "session_marks",
  "student_semester_gpa", "session_fee_heads", "fee_overrides", "fines",
  "exam_paper_submissions", "datesheets",
] as const;

// What each exported table is called to a person, for "couldn't back up X" messages.
const TABLE_LABELS: Record<string, string> = {
  session_subjects: "subject",
  session_students: "student",
  session_attendance: "attendance",
  session_marks: "marks",
  student_semester_gpa: "results",
  session_fee_heads: "fee item",
  fee_overrides: "fee override",
  fines: "fine",
  exam_paper_submissions: "exam paper",
  datesheets: "datesheet",
  session_fees: "fee structure",
  timetable_periods: "timetable",
};

function backupFailed(table: string): Response {
  return httpError(
    500,
    `Couldn't back up the session's ${TABLE_LABELS[table] ?? "data"} records, so nothing was deleted. Try again.`,
    "ARCHIVE_EXPORT_FAILED",
  );
}

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  const caller = await requireAdmin(req, svc);

  const { sessionId } = await readJson(req) as { sessionId?: string };
  if (!sessionId) throw httpError(400, "Choose a session to archive.");

  const { data: session, error: sessionErr } = await svc.from("academic_sessions")
    .select("*").eq("session_id", sessionId).maybeSingle();
  if (sessionErr) throw dbError(sessionErr, "Couldn't look up the session, so nothing was deleted. Try again.");
  if (!session) throw httpError(404, "That session no longer exists. Refresh the list.");
  if (session.is_active) {
    throw httpError(409, "This session is still active. Deactivate it before archiving and deleting it.", "SESSION_ACTIVE");
  }

  // 1) Export every session-scoped table (+ fees parent + timetable) to one JSON doc.
  const archive: Record<string, unknown> = {
    archivedAt: new Date().toISOString(),
    archivedBy: caller.email,
    session,
  };
  // Every export is checked: an ignored failure here would archive an EMPTY table and then delete the real rows.
  const { data: fees, error: feesErr } = await svc.from("session_fees").select("*").eq("session_id", sessionId);
  if (feesErr) { console.error(feesErr); throw backupFailed("session_fees"); }
  archive["session_fees"] = fees ?? [];
  const { data: periods, error: periodsErr } = await svc.from("timetable_periods")
    .select("*").eq("primary_session_id", sessionId);
  if (periodsErr) { console.error(periodsErr); throw backupFailed("timetable_periods"); }
  archive["timetable_periods"] = periods ?? [];
  for (const table of TABLES) {
    const { data, error } = await svc.from(table).select("*").eq("session_id", sessionId);
    if (error) { console.error(`export ${table}:`, error); throw backupFailed(table); }
    archive[table] = data ?? [];
  }

  const path = `archives/${sessionId}_${Date.now()}.json`;
  const { error: uploadErr } = await svc.storage.from("documents").upload(
    path,
    new Blob([JSON.stringify(archive)], { type: "application/json" }),
  );
  if (uploadErr) {
    console.error("archive upload failed:", uploadErr);
    throw httpError(500, "Couldn't save the backup file, so nothing was deleted. Try again.", "ARCHIVE_UPLOAD_FAILED");
  }

  // 2) Purge exam-paper blobs, then delete the session row (cascades handle the rest).
  const paperPaths = (archive["exam_paper_submissions"] as Array<
    { storage_path?: string; key_storage_path?: string }
  >).flatMap((p) => [p.storage_path, p.key_storage_path])
    .filter((p): p is string => !!p);
  if (paperPaths.length > 0) await svc.storage.from("exam-papers").remove(paperPaths);

  const { error: deleteErr } = await svc.from("academic_sessions")
    .delete().eq("session_id", sessionId);
  if (deleteErr) throw dbError(deleteErr, "The backup was saved, but the session couldn't be deleted. Try again.");

  // 3) Leave a tombstone. The apps sync "rows changed since last time", and a physically removed row never shows up in
  // that, so other devices would keep the session (and its roster/timetable) in their caches forever. A soft-deleted
  // stub of the session row is what tells them to drop it; the heavy data stays gone.
  const { error: tombstoneErr } = await svc.from("academic_sessions").insert({
    ...session,
    is_active: false,
    is_deleted: true,
    deleted_at: new Date().toISOString(),
    deleted_by: caller.email,
  });
  if (tombstoneErr) console.error("archive tombstone failed (session is deleted, other devices may keep a stale copy):", tombstoneErr);

  return ok({ sessionId, archivePath: path, papersDeleted: paperPaths.length, tombstone: !tombstoneErr });
}));
