// promote-session — bumps a session's current_semester and deletes the completed
// semester's exam papers (Storage objects + metadata rows). Marks/GPA/attendance
// history is NEVER touched — it is the longitudinal stats dataset.
//
// POST { sessionId }
import { handle, httpError, ok, requireAdmin, serviceClient } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  await requireAdmin(req, svc);

  const { sessionId } = await req.json();
  if (!sessionId) throw httpError(400, "sessionId is required");

  const { data: session, error } = await svc.from("academic_sessions")
    .select("current_semester,is_active").eq("session_id", sessionId).maybeSingle();
  if (error || !session) throw httpError(404, "Session not found");

  const completed = session.current_semester;

  // 1) Delete the completed semester's exam papers: Storage blobs first, then rows.
  const { data: papers } = await svc.from("exam_paper_submissions")
    .select("id,storage_path,key_storage_path")
    .eq("session_id", sessionId).eq("semester", completed);
  const paths = (papers ?? [])
    .flatMap((p) => [p.storage_path, p.key_storage_path])
    .filter((p): p is string => !!p);
  if (paths.length > 0) await svc.storage.from("exam-papers").remove(paths);
  await svc.from("exam_paper_submissions").delete()
    .eq("session_id", sessionId).eq("semester", completed);

  // 2) Advance the pointer — or graduate after semester 8.
  if (completed >= 8) {
    await svc.from("academic_sessions")
      .update({ is_active: false }).eq("session_id", sessionId);
    await svc.from("session_students")
      .update({ enrollment_status: "GRADUATED" })
      .eq("session_id", sessionId).eq("enrollment_status", "ACTIVE");
    return ok({ sessionId, graduated: true, papersDeleted: paths.length });
  }
  await svc.from("academic_sessions")
    .update({ current_semester: completed + 1 }).eq("session_id", sessionId);

  return ok({ sessionId, promotedTo: completed + 1, papersDeleted: paths.length });
}));
