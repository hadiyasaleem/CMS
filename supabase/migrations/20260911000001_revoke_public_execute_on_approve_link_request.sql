-- CREATE FUNCTION grants EXECUTE to PUBLIC by default, unlike record_semester_result (created back
-- when a schema-wide default-privilege revoke was in effect). Close that gap explicitly so this new
-- RPC matches the same anon-blocked posture as every other privileged RPC in this schema.
revoke all on function approve_link_request(uuid, text) from public;
grant execute on function approve_link_request(uuid, text) to authenticated;
