// revoke-student-link — unlinks a student account from its college record.
// The student keeps their GoTrue login but returns to the link-request gate
// and must file (and get approved) a new request to see any data again.
//
// POST { sessionId, rollNumber }
import { handle, httpError, ok, requireAdmin, serviceClient } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  await requireAdmin(req, svc);

  const { sessionId, rollNumber } = await req.json();
  if (!sessionId || !rollNumber) throw httpError(400, "sessionId and rollNumber are required");

  const { data: student } = await svc.from("session_students")
    .select("linked_email")
    .eq("session_id", sessionId).eq("roll_number", rollNumber).maybeSingle();
  if (!student) throw httpError(404, "Student record not found");
  if (!student.linked_email) return ok({ sessionId, rollNumber, alreadyUnlinked: true });

  // Clear both sides of the link.
  const { error: rosterErr } = await svc.from("session_students")
    .update({ linked_email: "" })
    .eq("session_id", sessionId).eq("roll_number", rollNumber);
  if (rosterErr) throw httpError(500, rosterErr.message);

  const { error: profileErr } = await svc.from("profiles")
    .update({ linked_session_id: null, linked_roll: null })
    .eq("email", student.linked_email);
  if (profileErr) throw httpError(500, profileErr.message);

  // Invalidate any previously-approved request so history stays truthful.
  await svc.from("student_link_requests")
    .update({ status: "REJECTED", rejection_reason: "Link revoked by admin" })
    .eq("session_id", sessionId).eq("roll_number_claimed", rollNumber)
    .eq("status", "APPROVED");

  return ok({ sessionId, rollNumber, unlinkedEmail: student.linked_email });
}));
