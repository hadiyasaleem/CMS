// set-teacher-status — teacher account lifecycle: ACTIVE / DISABLED / BANNED /
// DELETE (soft), plus permission-flag updates. Disabling or banning also bans
// the GoTrue account so existing sessions stop working.
//
// POST { email, status?: "ACTIVE" | "DISABLED" | "BANNED" | "DELETE",
//        permissions?: { canApproveLinkRequests?, canEditTimetable?,
//                        canSendNotifications?, canManageDatesheets? } }
import { handle, httpError, ok, requireAdmin, serviceClient } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  const caller = await requireAdmin(req, svc);

  const { email, status, permissions } = await req.json();
  if (!email) throw httpError(400, "email is required");
  const normalized = String(email).trim().toLowerCase();

  const { data: teacher } = await svc.from("teachers")
    .select("auth_uid").eq("email", normalized).maybeSingle();
  if (!teacher) throw httpError(404, "Teacher not found");

  const update: Record<string, unknown> = { updated_by: caller.email };

  if (permissions) {
    if (permissions.canApproveLinkRequests !== undefined) update.can_approve_link_requests = permissions.canApproveLinkRequests;
    if (permissions.canEditTimetable !== undefined) update.can_edit_timetable = permissions.canEditTimetable;
    if (permissions.canSendNotifications !== undefined) update.can_send_notifications = permissions.canSendNotifications;
    if (permissions.canManageDatesheets !== undefined) update.can_manage_datesheets = permissions.canManageDatesheets;
  }

  if (status === "DELETE") {
    update.is_active = false;
    update.status = "DISABLED";
  } else if (status) {
    if (!["ACTIVE", "DISABLED", "BANNED"].includes(status)) {
      throw httpError(400, "status must be ACTIVE, DISABLED, BANNED or DELETE");
    }
    update.status = status;
    if (status === "ACTIVE") update.is_active = true;
  }

  const { error: teacherErr } = await svc.from("teachers")
    .update(update).eq("email", normalized);
  if (teacherErr) throw httpError(500, teacherErr.message);

  // Mirror onto the auth account + profile so sign-in stops/resumes immediately.
  if (teacher.auth_uid && status) {
    const blocked = status !== "ACTIVE";
    await svc.auth.admin.updateUserById(teacher.auth_uid, {
      ban_duration: blocked ? "876000h" : "none", // ~100 years vs lift
    });
    await svc.from("profiles")
      .update({ status: status === "DELETE" ? "DISABLED" : status })
      .eq("id", teacher.auth_uid);
  }

  return ok({ email: normalized, applied: update });
}));
