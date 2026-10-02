// reset-administrator-password — lets an admin set a new password for another
// administrator's GoTrue account, for when they forgot their password. Does
// not require the target's current password. Administrators live in
// `profiles` (role = 'ADMIN'), keyed directly by profiles.id = auth uid.
//
// POST { email, newPassword }
import { handle, httpError, ok, requireAdmin, serviceClient } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  await requireAdmin(req, svc);

  const body = await req.json();
  // Accept both casings -- the Kotlin client's global JSON naming strategy can rewrite
  // camelCase fields to snake_case depending on which call path serializes the request,
  // so don't assume a single casing.
  const email = body.email;
  const newPassword = body.newPassword ?? body.new_password;
  if (!email || !newPassword) {
    throw httpError(400, `email and newPassword are required (got keys: ${Object.keys(body).join(", ")})`);
  }
  const normalized = String(email).trim().toLowerCase();

  const { data: admin } = await svc.from("profiles")
    .select("id").eq("email", normalized).eq("role", "ADMIN").maybeSingle();
  if (!admin) throw httpError(404, "Administrator not found");

  const { error } = await svc.auth.admin.updateUserById(admin.id, { password: newPassword });
  if (error) throw httpError(500, error.message);

  return ok({ email: normalized });
}));
