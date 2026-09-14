-- The plain "from public" revoke didn't fully take: anon apparently holds its own direct EXECUTE
-- grant (likely from a schema-wide default-privilege grant to anon set up elsewhere in this
-- project), independent of PUBLIC's. Revoke it explicitly from anon too, confirmed via
-- has_function_privilege() that anon can no longer execute this function afterward.
revoke all on function approve_link_request(uuid, text) from anon;
