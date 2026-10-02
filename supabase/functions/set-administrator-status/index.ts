// set-administrator-status — administrator account lifecycle: ACTIVE / DISABLED /
// DELETE (soft). Disabling or deleting also bans the GoTrue account so existing
// sessions stop working. Administrators live in `profiles` (role = 'ADMIN'),
// not `teachers`, so this looks up by profiles.id directly.
//
// Refuses to act on the caller's own account, or on the bootstrap admin
// (admin@example.com), so the college can never lock itself out of admin access.
//
// POST { email, status: "ACTIVE" | "DISABLED" | "DELETE" }
import { handle, httpError, ok, requireAdmin, serviceClient } from "../_shared/auth.ts";

const BOOTSTRAP_ADMIN_EMAIL = "admin@example.com";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  const caller = await requireAdmin(req, svc);

  const { email, status } = await req.json();
  if (!email) throw httpError(400, "email is required");
  if (!status) throw httpError(400, "status is required");
  const normalized = String(email).trim().toLowerCase();

  if (normalized === caller.email) {
    throw httpError(400, "You cannot change your own account status.");
  }
  if (normalized === BOOTSTRAP_ADMIN_EMAIL) {
    throw httpError(400, "The bootstrap administrator account cannot be disabled or removed.");
  }

  const { data: admin } = await svc.from("profiles")
    .select("id").eq("email", normalized).eq("role", "ADMIN").maybeSingle();
  if (!admin) throw httpError(404, "Administrator not found");

  const update: Record<string, unknown> = { updated_by: caller.email };

  if (status === "DELETE") {
    update.status = "DISABLED";
    update.is_deleted = true;
    update.deleted_at = new Date().toISOString();
    update.deleted_by = caller.email;
  } else {
    if (!["ACTIVE", "DISABLED"].includes(status)) {
      throw httpError(400, "status must be ACTIVE, DISABLED or DELETE");
    }
    update.status = status;
  }

  const { error: profileErr } = await svc.from("profiles")
    .update(update).eq("id", admin.id);
  if (profileErr) throw httpError(500, profileErr.message);

  // Mirror onto the auth account so sign-in stops/resumes immediately.
  const blocked = status !== "ACTIVE";
  await svc.auth.admin.updateUserById(admin.id, {
    ban_duration: blocked ? "876000h" : "none", // ~100 years vs lift
  });

  return ok({ email: normalized, applied: update });
}));
