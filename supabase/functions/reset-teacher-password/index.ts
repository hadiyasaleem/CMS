// reset-teacher-password — lets an admin set a new password for a teacher's
// GoTrue account, for when the teacher forgot (or was never told) their
// temporary password. Does not require the teacher's current password.
//
// Some teacher rows were seeded straight into the `teachers` table (e.g. bulk
// import) and never got a matching GoTrue account, so auth_uid is null --
// there is no password to "reset" because none was ever created. Rather than
// fail, create the missing account with the given password so this button
// always leaves the teacher with working credentials.
//
// POST { email, newPassword }
import { handle, httpError, ok, requireAdmin, serviceClient } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  const caller = await requireAdmin(req, svc);

  const { email, newPassword } = await req.json();
  if (!email || !newPassword) throw httpError(400, "email and newPassword are required");
  const normalized = String(email).trim().toLowerCase();

  const { data: teacher } = await svc.from("teachers")
    .select("auth_uid").eq("email", normalized).maybeSingle();
  if (!teacher) throw httpError(404, "Teacher not found");

  let uid = teacher.auth_uid as string | null;
  if (!uid) {
    const { data: created, error: createErr } = await svc.auth.admin.createUser({
      email: normalized,
      password: newPassword,
      email_confirm: true,
    });
    if (createErr) throw httpError(500, createErr.message);
    uid = created.user!.id;

    await svc.from("teachers").update({ auth_uid: uid, updated_by: caller.email }).eq("email", normalized);
    await svc.from("profiles").upsert({ id: uid, email: normalized, role: "TEACHER", teacher_email: normalized, status: "ACTIVE" });
  } else {
    const { error } = await svc.auth.admin.updateUserById(uid, { password: newPassword });
    if (error) throw httpError(500, error.message);
  }

  return ok({ email: normalized });
}));
