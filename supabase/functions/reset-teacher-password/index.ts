// reset-teacher-password — lets an admin set a new password for a teacher's
// GoTrue account, for when the teacher forgot (or was never told) their
// temporary password. Does not require the teacher's current password.
//
// POST { email, newPassword }
import { handle, httpError, ok, requireAdmin, serviceClient } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  await requireAdmin(req, svc);

  const { email, newPassword } = await req.json();
  if (!email || !newPassword) throw httpError(400, "email and newPassword are required");
  const normalized = String(email).trim().toLowerCase();

  const { data: teacher } = await svc.from("teachers")
    .select("auth_uid").eq("email", normalized).maybeSingle();
  if (!teacher?.auth_uid) throw httpError(404, "Teacher not found");

  const { error } = await svc.auth.admin.updateUserById(teacher.auth_uid, { password: newPassword });
  if (error) throw httpError(500, error.message);

  return ok({ email: normalized });
}));
