// set-teacher-status — teacher account lifecycle: ACTIVE / DISABLED / BANNED /
// DELETE (soft), plus permission-flag updates. Disabling or banning also bans
// the GoTrue account so existing sessions stop working.
//
// POST { email, status?: "ACTIVE" | "DISABLED" | "BANNED" | "DELETE",
//        permissions?: { canApproveLinkRequests?, canSendNotifications? } }
import { authError, dbError, handle, httpError, ok, readJson, requireAdmin, serviceClient } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  const caller = await requireAdmin(req, svc);

  const { email, status, permissions } = await readJson(req) as {
    email?: string;
    status?: string;
    permissions?: { canApproveLinkRequests?: boolean; canSendNotifications?: boolean };
  };
  if (!email) throw httpError(400, "Choose a teacher.");
  const normalized = String(email).trim().toLowerCase();

  const { data: teacher } = await svc.from("teachers")
    .select("auth_uid").eq("email", normalized).maybeSingle();
  if (!teacher) throw httpError(404, "No teacher account exists for that email.");

  const update: Record<string, unknown> = { updated_by: caller.email };

  if (permissions) {
    if (permissions.canApproveLinkRequests !== undefined) update.can_approve_link_requests = permissions.canApproveLinkRequests;
    if (permissions.canSendNotifications !== undefined) update.can_send_notifications = permissions.canSendNotifications;
  }

  if (status === "DELETE") {
    update.is_active = false;
    update.status = "DISABLED";
    update.is_deleted = true;
    update.deleted_at = new Date().toISOString();
    update.deleted_by = caller.email;
  } else if (status) {
    if (!["ACTIVE", "DISABLED", "BANNED"].includes(status)) {
      throw httpError(400, "The status must be Active, Disabled, Banned or Delete.");
    }
    update.status = status;
    if (status === "ACTIVE") update.is_active = true;
  }

  const { error: teacherErr } = await svc.from("teachers")
    .update(update).eq("email", normalized);
  if (teacherErr) throw dbError(teacherErr, "Couldn't update the teacher's status. Try again.");

  // Mirror onto the auth account + profile so sign-in stops/resumes immediately.
  if (teacher.auth_uid && status) {
    const blocked = status !== "ACTIVE";
    const { error: banErr } = await svc.auth.admin.updateUserById(teacher.auth_uid, {
      ban_duration: blocked ? "876000h" : "none", // ~100 years vs lift
    });
    // If this fails the teacher record says one thing and their login another: say so, never report success.
    if (banErr) {
      throw authError(
        banErr,
        blocked
          ? "The teacher's record was updated but their login couldn't be blocked, so they can still sign in. Try again."
          : "The teacher's record was updated but their login couldn't be re-enabled, so they still can't sign in. Try again.",
      );
    }
    const { error: profileErr } = await svc.from("profiles")
      .update({ status: status === "DELETE" ? "DISABLED" : status })
      .eq("id", teacher.auth_uid);
    if (profileErr) console.error("set-teacher-status: profile mirror failed:", profileErr);
  }

  return ok({ email: normalized, applied: update });
}));
