// revoke-student-link — unlinks a student account from its college record.
// The student keeps their GoTrue login but returns to the link-request gate
// and must file (and get approved) a new request to see any data again.
//
// POST { sessionId, rollNumber }
import { dbError, handle, httpError, ok, readJson, requireAdmin, serviceClient } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  const svc = serviceClient();
  await requireAdmin(req, svc);

  const { sessionId, rollNumber } = await readJson(req) as { sessionId?: string; rollNumber?: string };
  if (!sessionId || !rollNumber) throw httpError(400, "Choose the student whose link you want to remove.");

  const { data: student, error: studentErr } = await svc.from("session_students")
    .select("linked_email")
    .eq("session_id", sessionId).eq("roll_number", rollNumber).maybeSingle();
  if (studentErr) throw dbError(studentErr, "Couldn't look up the student, so nothing was changed. Try again.");
  if (!student) throw httpError(404, `Roll number ${rollNumber} is no longer on this roster. Refresh the list.`);
  if (!student.linked_email) return ok({ sessionId, rollNumber, alreadyUnlinked: true });

  // Clear both sides of the link.
  const { error: rosterErr } = await svc.from("session_students")
    .update({ linked_email: "" })
    .eq("session_id", sessionId).eq("roll_number", rollNumber);
  if (rosterErr) throw dbError(rosterErr, "Couldn't remove the link from the student's roster record. Try again.");

  const { error: profileErr } = await svc.from("profiles")
    .update({ linked_session_id: null, linked_roll: null })
    .eq("email", student.linked_email);
  if (profileErr) throw dbError(profileErr, "The roster link was removed but the student's account couldn't be updated. Try again.");

  // Invalidate any previously-approved request so history stays truthful.
  const { error: historyErr } = await svc.from("student_link_requests")
    .update({ status: "REJECTED", rejection_reason: "Link revoked by admin" })
    .eq("session_id", sessionId).eq("roll_number_claimed", rollNumber)
    .eq("status", "APPROVED");
  // The link itself is already cleared; a history-tidy failure must not make the whole action look failed.
  if (historyErr) console.error("revoke: couldn't reject the old approved request:", historyErr);

  return ok({ sessionId, rollNumber, unlinkedEmail: student.linked_email });
}));
