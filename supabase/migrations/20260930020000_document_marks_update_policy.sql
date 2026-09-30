-- Records why session_marks can only be UPDATED by an admin (no behaviour change; a comment on the policy).
--
-- A teacher enters a score once. It is then locked in the app (MarksEntryController.lockedRolls), and any correction
-- goes through a mark edit request that an admin approves. The database enforces the same rule: the marks screen saves
-- with an upsert, so a batch that includes a roll someone already saved is refused as a whole -- the apps translate that
-- into "Some of these scores were already saved and are locked..." (ConstraintMessages.permissionDenied).
-- Production already has this policy (see 20260930010000_backfill_live_only_drift.sql); only the comment is new.
comment on policy upd_marks on session_marks is
  'Admin only. Saved scores are locked for teachers; corrections go through mark_edit_requests, approved by an admin.';
