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
import { authError, dbError, handle, httpError, ok, readJson, requireAdmin, serviceClient } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  const caller = await requireAdmin(req, svc);

  const body = await readJson(req) as Record<string, string | undefined>;
  // Accept both casings -- the Kotlin client's global JSON naming strategy can rewrite
  // camelCase fields to snake_case depending on which call path serializes the request,
  // so don't assume a single casing.
  const email = body.email;
  const newPassword = body.newPassword ?? body.new_password;
  if (!email || !newPassword) {
    throw httpError(400, "Enter the teacher's email and a new password.");
  }
  const normalized = String(email).trim().toLowerCase();

  const { data: teacher, error: lookupErr } = await svc.from("teachers")
    .select("auth_uid").eq("email", normalized).maybeSingle();
  if (lookupErr) throw dbError(lookupErr, "Couldn't look up the teacher, so the password was not changed. Try again.");
  if (!teacher) throw httpError(404, "No teacher account exists for that email.");

  let uid = teacher.auth_uid as string | null;
  if (!uid) {
    const { data: created, error: createErr } = await svc.auth.admin.createUser({
      email: normalized,
      password: newPassword,
      email_confirm: true,
    });
    if (createErr) throw authError(createErr, "Couldn't create a login for this teacher. Try again.");
    uid = created.user!.id;

    const { error: linkErr } = await svc.from("teachers").update({ auth_uid: uid, updated_by: caller.email }).eq("email", normalized);
    if (linkErr) throw dbError(linkErr, "The login was created but couldn't be linked to the teacher record. Try again.");
    const { error: profileErr } = await svc.from("profiles").upsert({ id: uid, email: normalized, role: "TEACHER", teacher_email: normalized, status: "ACTIVE" });
    if (profileErr) throw dbError(profileErr, "The login was created but the teacher's account couldn't be set up. Try again.");
  } else {
    const { error } = await svc.auth.admin.updateUserById(uid, { password: newPassword });
    if (error) throw authError(error, "Couldn't change the password. Try again.");
  }

  return ok({ email: normalized });
}));
