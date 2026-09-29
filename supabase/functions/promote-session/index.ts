// promote-session — bumps a session's current_semester and deletes the completed
// semester's exam papers (Storage objects + metadata rows). Marks/GPA/attendance
// history is NEVER touched — it is the longitudinal stats dataset.
//
// POST { sessionId }
import { dbError, handle, httpError, ok, readJson, requireAdmin, serviceClient } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  await requireAdmin(req, svc);

  const { sessionId } = await readJson(req) as { sessionId?: string };
  if (!sessionId) throw httpError(400, "Choose a session to promote.");

  const { data: session, error } = await svc.from("academic_sessions")
    .select("current_semester,is_active").eq("session_id", sessionId).maybeSingle();
  if (error) throw dbError(error, "Couldn't look up the session, so it was not promoted. Try again.");
  if (!session) throw httpError(404, "That session no longer exists. Refresh the list.");

  const completed = session.current_semester;

  // 1) Delete the completed semester's exam papers: Storage blobs first, then rows.
  const { data: papers, error: papersErr } = await svc.from("exam_paper_submissions")
    .select("id,storage_path,key_storage_path")
    .eq("session_id", sessionId).eq("semester", completed);
  if (papersErr) {
    throw dbError(papersErr, `Couldn't check Semester ${completed}'s exam papers, so the session was not promoted. Try again.`);
  }
  const paths = (papers ?? [])
    .flatMap((p) => [p.storage_path, p.key_storage_path])
    .filter((p): p is string => !!p);
  if (paths.length > 0) await svc.storage.from("exam-papers").remove(paths);
  const { error: papersDeleteErr } = await svc.from("exam_paper_submissions").delete()
    .eq("session_id", sessionId).eq("semester", completed);
  if (papersDeleteErr) {
    throw dbError(papersDeleteErr, `Couldn't remove Semester ${completed}'s exam papers, so the session was not promoted. Try again.`);
  }

  // 2) Advance the pointer — or graduate after semester 8.
  if (completed >= 8) {
    const { error: deactivateErr } = await svc.from("academic_sessions")
      .update({ is_active: false }).eq("session_id", sessionId);
    if (deactivateErr) {
      throw dbError(deactivateErr, "Couldn't mark the session as graduated. Its exam papers were already removed; try again.");
    }
    const { error: graduateErr } = await svc.from("session_students")
      .update({ enrollment_status: "GRADUATED" })
      .eq("session_id", sessionId).eq("enrollment_status", "ACTIVE");
    if (graduateErr) {
      throw dbError(graduateErr, "The session was closed but its students couldn't be marked as graduated. Try again.");
    }
    return ok({ sessionId, graduated: true, papersDeleted: paths.length });
  }
  const { error: promoteErr } = await svc.from("academic_sessions")
    .update({ current_semester: completed + 1 }).eq("session_id", sessionId);
  if (promoteErr) {
    throw dbError(promoteErr, `Couldn't move the session to Semester ${completed + 1}. Semester ${completed}'s exam papers were already removed; try again.`);
  }

  return ok({ sessionId, promotedTo: completed + 1, papersDeleted: paths.length });
}));
