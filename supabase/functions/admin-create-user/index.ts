// admin-create-user — replaces the client-side SecondaryAuthCreator hack.
// Creates a GoTrue account (email pre-confirmed: nothing is sent through the
// rate-limited built-in SMTP), inserts the teachers record, then upgrades the
// auto-created profile row.
//
// POST { email, password, role: "TEACHER" | "ADMIN",
//        name?, deptId?, designation?, phone? }
import { authError, dbError, handle, httpError, ok, readJson, requireAdmin, serviceClient } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  const caller = await requireAdmin(req, svc);

  const { email, password, role, name, deptId, designation, phone } = await readJson(req) as Record<string, string | undefined>;
  if (!email || !password) throw httpError(400, "Enter an email address and a password.");
  if (role !== "TEACHER" && role !== "ADMIN") throw httpError(400, "The account type must be Teacher or Admin.");
  const normalized = String(email).trim().toLowerCase();

  // Create the auth account. If it already exists (e.g. a previous attempt failed midway),
  // recover its uid from the profile the signup trigger created and reset its password so the
  // admin's chosen credentials apply — this makes the whole operation idempotent/retry-safe.
  let uid: string;
  const { data: created, error: createErr } = await svc.auth.admin.createUser({
    email: normalized,
    password,
    email_confirm: true, // no confirmation email → immune to free-tier SMTP limits
  });
  if (createErr) {
    const { data: existing, error: existingErr } = await svc.from("profiles")
      .select("id").eq("email", normalized).maybeSingle();
    if (existingErr) throw dbError(existingErr, "Couldn't check whether this account already exists. Try again.");
    if (!existing) throw authError(createErr, "Couldn't create the login for this account. Try again.");
    uid = existing.id;
    await svc.auth.admin.updateUserById(uid, { password, email_confirm: true });
  } else {
    uid = created.user!.id;
  }

  // Teacher row FIRST — profiles.teacher_email FK-references teachers(email), so the row must
  // exist before we point a profile at it.
  if (role === "TEACHER") {
    const { error: teacherErr } = await svc.from("teachers").upsert({
      email: normalized,
      auth_uid: uid,
      name: name ?? normalized.split("@")[0],
      dept_id: deptId ?? null,
      designation: designation ?? null,
      phone: phone ?? null,
      created_by: caller.email,
      updated_by: caller.email,
    });
    if (teacherErr) throw dbError(teacherErr, "The login was created but the teacher record couldn't be saved. Try again; it is safe to retry.");
  }

  // trg_on_auth_user_created already inserted a STUDENT profile — upgrade it.
  const { error: profileErr } = await svc.from("profiles").upsert({
    id: uid,
    email: normalized,
    role,
    teacher_email: role === "TEACHER" ? normalized : null,
    status: "ACTIVE",
  });
  if (profileErr) throw dbError(profileErr, `The login was created but couldn't be set up as ${role === "ADMIN" ? "an admin" : "a teacher"}. Try again; it is safe to retry.`);

  return ok({ uid, email: normalized, role });
}));
