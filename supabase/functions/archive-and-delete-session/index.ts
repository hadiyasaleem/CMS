// archive-and-delete-session — Free tier has no automated backups, so this is
// the backup story: export EVERYTHING about an inactive session to a JSON file
// in the documents/archives/ folder (service-role only), then cascade-delete
// the session (FKs remove roster/attendance/marks/gpa/fees/fines/periods) and
// purge its exam-paper blobs.
//
// POST { sessionId }
import { handle, httpError, ok, requireAdmin, serviceClient } from "../_shared/auth.ts";

const TABLES = [
  "session_subjects", "session_students", "session_attendance", "session_marks",
  "student_semester_gpa", "session_fee_heads", "fee_overrides", "fines",
  "exam_paper_submissions", "datesheets",
] as const;

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  const caller = await requireAdmin(req, svc);

  const { sessionId } = await req.json();
  if (!sessionId) throw httpError(400, "sessionId is required");

  const { data: session } = await svc.from("academic_sessions")
    .select("*").eq("session_id", sessionId).maybeSingle();
  if (!session) throw httpError(404, "Session not found");
  if (session.is_active) throw httpError(409, "Session is still active — deactivate it first");

  // 1) Export every session-scoped table (+ fees parent + timetable) to one JSON doc.
  const archive: Record<string, unknown> = {
    archivedAt: new Date().toISOString(),
    archivedBy: caller.email,
    session,
  };
  const { data: fees } = await svc.from("session_fees").select("*").eq("session_id", sessionId);
  archive["session_fees"] = fees ?? [];
  const { data: periods } = await svc.from("timetable_periods")
    .select("*").eq("primary_session_id", sessionId);
  archive["timetable_periods"] = periods ?? [];
  for (const table of TABLES) {
    const { data, error } = await svc.from(table).select("*").eq("session_id", sessionId);
    if (error) throw httpError(500, `export ${table}: ${error.message}`);
    archive[table] = data ?? [];
  }

  const path = `archives/${sessionId}_${Date.now()}.json`;
  const { error: uploadErr } = await svc.storage.from("documents").upload(
    path,
    new Blob([JSON.stringify(archive)], { type: "application/json" }),
  );
  if (uploadErr) throw httpError(500, `archive upload failed: ${uploadErr.message}`);

  // 2) Purge exam-paper blobs, then delete the session row (cascades handle the rest).
  const paperPaths = (archive["exam_paper_submissions"] as Array<
    { storage_path?: string; key_storage_path?: string }
  >).flatMap((p) => [p.storage_path, p.key_storage_path])
    .filter((p): p is string => !!p);
  if (paperPaths.length > 0) await svc.storage.from("exam-papers").remove(paperPaths);

  const { error: deleteErr } = await svc.from("academic_sessions")
    .delete().eq("session_id", sessionId);
  if (deleteErr) throw httpError(500, deleteErr.message);

  return ok({ sessionId, archivePath: path, papersDeleted: paperPaths.length });
}));
