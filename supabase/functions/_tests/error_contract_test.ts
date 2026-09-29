// Error-contract tests for the admin edge functions. Runs every function's real handler against a fake Supabase
// (auth + PostgREST + storage) so no project, database or network is needed:
//
//   deno test --allow-net --allow-env --allow-read supabase/functions/_tests/
//
// The contract (read by the apps' EdgeFunctionErrors.kt): every failure is { "error": "<plain sentence>", "code": "<CODE>" }
// with a meaningful HTTP status, and the sentence never carries database/auth internals.
import { assert, assertEquals } from "jsr:@std/assert@1";

type Handler = (req: Request) => Promise<Response>;
type Route = { status: number; body: unknown };

// --- fake Supabase ---------------------------------------------------------------------------------------------
const routes = new Map<string, Route>();
const calls: string[] = [];

/** key: "GET profiles", "POST auth/admin/users", "PUT auth/admin/users", "POST storage" ... */
function route(key: string, status: number, body: unknown) {
  routes.set(key, { status, body });
}
function dbFails(key: string, code: string, message = "internal detail: relation \"secret_table\" violates something", status = 400) {
  route(key, status, { code, message, details: "Key (x)=(1) already exists.", hint: null });
}

const TOKENS: Record<string, { id: string; email: string }> = {
  "admin-token": { id: "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa", email: "boss@x.pk" },
  "teacher-token": { id: "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb", email: "teach@x.pk" },
};

const fake = Deno.serve({ port: 0, onListen: () => {} }, async (req) => {
  const url = new URL(req.url);
  const path = url.pathname;
  const json = (status: number, body: unknown) => new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });

  if (path === "/auth/v1/user") {
    if ((req.headers.get("Authorization") ?? "").includes("flaky-token")) return json(503, { msg: "auth service unavailable" });
    const user = TOKENS[(req.headers.get("Authorization") ?? "").replace("Bearer ", "")];
    return user ? json(200, { id: user.id, email: user.email, aud: "authenticated", app_metadata: {}, user_metadata: {}, created_at: "" })
      : json(401, { code: 401, msg: "invalid JWT: unable to parse or verify signature" });
  }
  let key: string;
  if (path.startsWith("/auth/v1/admin/users")) key = `${req.method} auth/admin/users`;
  else if (path.startsWith("/storage/v1/")) key = `${req.method === "DELETE" ? "DELETE" : req.method} storage`;
  else if (path.startsWith("/rest/v1/")) {
    key = `${req.method} ${path.replace("/rest/v1/", "")}`;
    // A route can target one lookup of a table by its filter column: "GET profiles?email" vs "GET profiles?id".
    const filterColumn = [...url.searchParams.keys()].find((k) => (url.searchParams.get(k) ?? "").startsWith("eq."));
    if (filterColumn && routes.has(`${key}?${filterColumn}`)) key = `${key}?${filterColumn}`;
  }
  else return json(404, {});
  calls.push(key);
  const r = routes.get(key);
  if (r) return json(r.status, r.body);
  if (key.startsWith("GET ")) return json(200, []);
  return json(200, key.includes("auth/admin") ? { user: { id: "cccccccc-cccc-4ccc-8ccc-cccccccccccc" } } : {});
});
const baseUrl = `http://127.0.0.1:${(fake.addr as Deno.NetAddr).port}`;
Deno.env.set("SUPABASE_URL", baseUrl);
Deno.env.set("SUPABASE_SERVICE_ROLE_KEY", "service-role-test-key");

// --- load each function's handler by intercepting Deno.serve ----------------------------------------------------
const realServe = Deno.serve;
const handlers: Record<string, Handler> = {};
async function load(name: string): Promise<Handler> {
  if (handlers[name]) return handlers[name];
  let captured: Handler | undefined;
  // deno-lint-ignore no-explicit-any
  (Deno as any).serve = (h: Handler) => { captured = h; return { finished: Promise.resolve(), shutdown: () => Promise.resolve() }; };
  try {
    await import(`../${name}/index.ts`);
  } finally {
    // deno-lint-ignore no-explicit-any
    (Deno as any).serve = realServe;
  }
  assert(captured, `${name} did not call Deno.serve`);
  return handlers[name] = captured!;
}

function reset() {
  routes.clear();
  calls.length = 0;
  route("GET profiles?id", 200, [{ role: "ADMIN", status: "ACTIVE" }]); // the admin caller is an admin unless a test says otherwise
}

async function call(fn: string, opts: { token?: string | null; body?: unknown; raw?: string } = {}): Promise<{ status: number; error: string; code: string }> {
  const handler = await load(fn);
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  const token = opts.token === undefined ? "admin-token" : opts.token;
  if (token) headers["Authorization"] = `Bearer ${token}`;
  const res = await handler(new Request("http://localhost/fn", { method: "POST", headers, body: opts.raw ?? JSON.stringify(opts.body ?? {}) }));
  const text = await res.text();
  let parsed: { error?: string; code?: string } = {};
  try { parsed = JSON.parse(text); } catch (_) { /* not JSON */ }
  return { status: res.status, error: parsed.error ?? "", code: parsed.code ?? "", ...(res.status === 200 ? { error: "", code: "" } : {}) };
}

const UNSAFE = /https?:|supabase|postgrest|exception|relation|secret_table|violates|sqlstate|\{|\}|\bat\s+\S+\.ts/i;

/** Every failure must be a plain sentence with a code; returns the response for further asserts. */
function expectFailure(r: { status: number; error: string; code: string }, status: number, code: string, contains?: string) {
  assertEquals(r.status, status, `status (error was: ${r.error})`);
  assertEquals(r.code, code, `code (error was: ${r.error})`);
  assert(r.error.length > 0 && r.error.length <= 220, `message length ${r.error.length}: ${r.error}`);
  assert(!UNSAFE.test(r.error), `message leaks internals: ${r.error}`);
  if (contains) assert(r.error.includes(contains), `expected "${contains}" in "${r.error}"`);
  // Optional: keep the real bodies so the Kotlin parser (EdgeFunctionErrors) can be run over them -- see Documentation/local-test-tools.md.
  const capture = Deno.env.get("EDGE_CAPTURE");
  if (capture) Deno.writeTextFileSync(capture, JSON.stringify({ status: r.status, body: JSON.stringify({ error: r.error, code: r.code }) }) + "\n", { append: true });
}

const FUNCTIONS = [
  ["admin-create-user", { email: "a@b.pk", password: "secret1", role: "TEACHER" }],
  ["archive-and-delete-session", { sessionId: "eng_2023" }],
  ["promote-session", { sessionId: "eng_2023" }],
  ["reset-teacher-password", { email: "t@b.pk", newPassword: "secret1" }],
  ["revoke-student-link", { sessionId: "eng_2023", rollNumber: "ENG-23-01" }],
  ["set-teacher-status", { email: "t@b.pk", status: "DISABLED" }],
] as const;

// --- shared gate: every function -------------------------------------------------------------------------------
for (const [fn, body] of FUNCTIONS) {
  Deno.test(`${fn}: no token -> 401 UNAUTHORIZED`, async () => {
    reset();
    expectFailure(await call(fn, { token: null, body }), 401, "UNAUTHORIZED", "not signed in");
  });
  Deno.test(`${fn}: bad token -> 401 with an expired-session sentence`, async () => {
    reset();
    expectFailure(await call(fn, { token: "garbage", body }), 401, "UNAUTHORIZED", "expired");
  });
  Deno.test(`${fn}: signed-in non-admin -> 403 FORBIDDEN`, async () => {
    reset();
    route("GET profiles?id", 200, [{ role: "TEACHER", status: "ACTIVE" }]);
    expectFailure(await call(fn, { token: "teacher-token", body }), 403, "FORBIDDEN", "admin");
  });
  Deno.test(`${fn}: unreadable permission lookup is a server problem, not "not an admin"`, async () => {
    reset();
    dbFails("GET profiles?id", "XX000", "connection to server lost: internal", 500);
    const r = await call(fn, { body });
    assertEquals(r.status, 500, `an outage must not look like a permission problem (got: ${r.error})`);
    assert(!UNSAFE.test(r.error), r.error);
  });
  Deno.test(`${fn}: auth service down is a server problem, not an expired session`, async () => {
    reset();
    const r = await call(fn, { token: "flaky-token", body });
    assertEquals(r.status, 500, `got: ${r.error}`);
    assertEquals(r.code, "PERMISSION_CHECK_FAILED");
    assert(!UNSAFE.test(r.error), r.error);
  });
  Deno.test(`${fn}: malformed body -> 400 VALIDATION`, async () => {
    reset();
    expectFailure(await call(fn, { raw: "not json at all" }), 400, "VALIDATION");
  });
}

// --- admin-create-user -------------------------------------------------------------------------------------------
Deno.test("admin-create-user: missing password -> 400", async () => {
  reset();
  expectFailure(await call("admin-create-user", { body: { email: "a@b.pk", role: "TEACHER" } }), 400, "VALIDATION", "password");
});
Deno.test("admin-create-user: bad role -> 400", async () => {
  reset();
  expectFailure(await call("admin-create-user", { body: { email: "a@b.pk", password: "x", role: "STUDENT" } }), 400, "VALIDATION", "Teacher or Admin");
});
Deno.test("admin-create-user: email already registered and no profile -> 409 EMAIL_EXISTS", async () => {
  reset();
  route("POST auth/admin/users", 422, { code: "email_exists", msg: "A user with this email address has already been registered", error_code: "email_exists" });
  expectFailure(await call("admin-create-user", { body: { email: "a@b.pk", password: "secret1", role: "TEACHER" } }), 409, "EMAIL_EXISTS");
});
Deno.test("admin-create-user: weak password -> 400 WEAK_PASSWORD", async () => {
  reset();
  route("POST auth/admin/users", 422, { code: "weak_password", msg: "Password should be at least 6 characters.", error_code: "weak_password" });
  expectFailure(await call("admin-create-user", { body: { email: "a@b.pk", password: "1", role: "TEACHER" } }), 400, "WEAK_PASSWORD");
});
Deno.test("admin-create-user: teacher row rejected as duplicate -> 409 ALREADY_EXISTS", async () => {
  reset();
  dbFails("POST teachers", "23505", 'duplicate key value violates unique constraint "teachers_pkey"', 409);
  expectFailure(await call("admin-create-user", { body: { email: "a@b.pk", password: "secret1", role: "TEACHER" } }), 409, "ALREADY_EXISTS");
});
Deno.test("admin-create-user: unknown database failure -> 500 with a step-specific sentence", async () => {
  reset();
  dbFails("POST profiles", "XX000", "boom", 500);
  const r = await call("admin-create-user", { body: { email: "a@b.pk", password: "secret1", role: "ADMIN" } });
  expectFailure(r, 500, "DB_FAILED", "safe to retry");
});
Deno.test("admin-create-user: a trigger's RAISE text is passed through as a 409", async () => {
  reset();
  route("POST teachers", 400, { code: "P0001", message: "Teacher Jane Doe already has an overlapping lecture: Poetry.", details: null, hint: null });
  expectFailure(await call("admin-create-user", { body: { email: "a@b.pk", password: "secret1", role: "TEACHER" } }), 409, "CONFLICT", "Jane Doe");
});

// --- archive-and-delete-session ----------------------------------------------------------------------------------
Deno.test("archive: no sessionId -> 400", async () => {
  reset();
  expectFailure(await call("archive-and-delete-session", { body: {} }), 400, "VALIDATION", "Choose a session");
});
Deno.test("archive: session missing -> 404 NOT_FOUND", async () => {
  reset();
  route("GET academic_sessions", 200, []);
  expectFailure(await call("archive-and-delete-session", { body: { sessionId: "x" } }), 404, "NOT_FOUND", "no longer exists");
});
Deno.test("archive: active session -> 409 SESSION_ACTIVE", async () => {
  reset();
  route("GET academic_sessions", 200, [{ session_id: "eng_2023", is_active: true }]);
  expectFailure(await call("archive-and-delete-session", { body: { sessionId: "eng_2023" } }), 409, "SESSION_ACTIVE", "still active");
});
Deno.test("archive: a failed export names the table and deletes nothing", async () => {
  reset();
  route("GET academic_sessions", 200, [{ session_id: "eng_2023", is_active: false }]);
  dbFails("GET session_marks", "XX000", "boom", 500);
  const r = await call("archive-and-delete-session", { body: { sessionId: "eng_2023" } });
  expectFailure(r, 500, "ARCHIVE_EXPORT_FAILED", "marks");
  assert(!calls.includes("DELETE academic_sessions"), "the session must not be deleted when the backup failed");
});
Deno.test("archive: a failed upload deletes nothing", async () => {
  reset();
  route("GET academic_sessions", 200, [{ session_id: "eng_2023", is_active: false }]);
  route("POST storage", 500, { statusCode: "500", error: "InternalError", message: "storage down" });
  const r = await call("archive-and-delete-session", { body: { sessionId: "eng_2023" } });
  expectFailure(r, 500, "ARCHIVE_UPLOAD_FAILED", "nothing was deleted");
  assert(!calls.includes("DELETE academic_sessions"), "the session must not be deleted when the backup failed");
});
Deno.test("archive: a delete blocked by a foreign key -> 409 RELATED_RECORDS", async () => {
  reset();
  route("GET academic_sessions", 200, [{ session_id: "eng_2023", is_active: false }]);
  route("POST storage", 200, { Key: "documents/archives/x.json" });
  dbFails("DELETE academic_sessions", "23503", "update or delete violates foreign key constraint", 409);
  expectFailure(await call("archive-and-delete-session", { body: { sessionId: "eng_2023" } }), 409, "RELATED_RECORDS");
});

// --- promote-session ---------------------------------------------------------------------------------------------
Deno.test("promote: no sessionId -> 400", async () => {
  reset();
  expectFailure(await call("promote-session", { body: {} }), 400, "VALIDATION", "Choose a session");
});
Deno.test("promote: session missing -> 404", async () => {
  reset();
  route("GET academic_sessions", 200, []);
  expectFailure(await call("promote-session", { body: { sessionId: "x" } }), 404, "NOT_FOUND");
});
Deno.test("promote: lookup failure says nothing was promoted", async () => {
  reset();
  dbFails("GET academic_sessions", "XX000", "boom", 500);
  expectFailure(await call("promote-session", { body: { sessionId: "x" } }), 500, "DB_FAILED", "not promoted");
});
Deno.test("promote: a failed final update says which semester and what was already done", async () => {
  reset();
  route("GET academic_sessions", 200, [{ current_semester: 3, is_active: true }]);
  dbFails("PATCH academic_sessions", "XX000", "boom", 500);
  expectFailure(await call("promote-session", { body: { sessionId: "eng_2023" } }), 500, "DB_FAILED", "Semester 4");
});
Deno.test("promote: a roster-cap trigger message passes through", async () => {
  reset();
  route("GET academic_sessions", 200, [{ current_semester: 8, is_active: true }]);
  route("PATCH academic_sessions", 400, { code: "P0001", message: "ENG 2023–2027 can't be closed yet.", details: null, hint: null });
  expectFailure(await call("promote-session", { body: { sessionId: "eng_2023" } }), 409, "CONFLICT", "can't be closed");
});

// --- reset-teacher-password --------------------------------------------------------------------------------------
Deno.test("reset-password: missing fields -> 400", async () => {
  reset();
  expectFailure(await call("reset-teacher-password", { body: { email: "t@b.pk" } }), 400, "VALIDATION", "new password");
});
Deno.test("reset-password: unknown teacher -> 404", async () => {
  reset();
  route("GET teachers", 200, []);
  expectFailure(await call("reset-teacher-password", { body: { email: "t@b.pk", newPassword: "secret1" } }), 404, "NOT_FOUND", "No teacher account");
});
Deno.test("reset-password: a failed teacher lookup is a server problem, not 'no such teacher'", async () => {
  reset();
  dbFails("GET teachers", "XX000", "boom", 500);
  const r = await call("reset-teacher-password", { body: { email: "t@b.pk", newPassword: "secret1" } });
  assertEquals(r.status, 500, `got: ${r.error}`);
});
Deno.test("reset-password: weak new password -> 400 WEAK_PASSWORD", async () => {
  reset();
  route("GET teachers", 200, [{ auth_uid: "11111111-1111-4111-8111-111111111111" }]);
  route("PUT auth/admin/users", 422, { code: "weak_password", msg: "Password should be at least 6 characters.", error_code: "weak_password" });
  expectFailure(await call("reset-teacher-password", { body: { email: "t@b.pk", newPassword: "1" } }), 400, "WEAK_PASSWORD");
});

// --- revoke-student-link -----------------------------------------------------------------------------------------
Deno.test("revoke: missing fields -> 400", async () => {
  reset();
  expectFailure(await call("revoke-student-link", { body: { sessionId: "eng_2023" } }), 400, "VALIDATION", "Choose the student");
});
Deno.test("revoke: student missing -> 404 naming the roll number", async () => {
  reset();
  route("GET session_students", 200, []);
  expectFailure(await call("revoke-student-link", { body: { sessionId: "eng_2023", rollNumber: "ENG-23-01" } }), 404, "NOT_FOUND", "ENG-23-01");
});
Deno.test("revoke: already unlinked is a success, not an error", async () => {
  reset();
  route("GET session_students", 200, [{ linked_email: "" }]);
  const r = await call("revoke-student-link", { body: { sessionId: "eng_2023", rollNumber: "ENG-23-01" } });
  assertEquals(r.status, 200);
});
Deno.test("revoke: profile update failure says the roster link was already removed", async () => {
  reset();
  route("GET session_students", 200, [{ linked_email: "s@x.pk" }]);
  dbFails("PATCH profiles", "XX000", "boom", 500);
  expectFailure(await call("revoke-student-link", { body: { sessionId: "eng_2023", rollNumber: "ENG-23-01" } }), 500, "DB_FAILED", "roster link was removed");
});

// --- set-teacher-status ------------------------------------------------------------------------------------------
Deno.test("set-status: no email -> 400", async () => {
  reset();
  expectFailure(await call("set-teacher-status", { body: {} }), 400, "VALIDATION", "Choose a teacher");
});
Deno.test("set-status: unknown teacher -> 404", async () => {
  reset();
  route("GET teachers", 200, []);
  expectFailure(await call("set-teacher-status", { body: { email: "t@b.pk", status: "ACTIVE" } }), 404, "NOT_FOUND");
});
Deno.test("set-status: a failed teacher lookup is a server problem, not 'no such teacher'", async () => {
  reset();
  dbFails("GET teachers", "XX000", "boom", 500);
  const r = await call("set-teacher-status", { body: { email: "t@b.pk", status: "ACTIVE" } });
  assertEquals(r.status, 500, `got: ${r.error}`);
});
Deno.test("set-status: invalid status -> 400", async () => {
  reset();
  route("GET teachers", 200, [{ auth_uid: null }]);
  expectFailure(await call("set-teacher-status", { body: { email: "t@b.pk", status: "SLEEPING" } }), 400, "VALIDATION", "Active, Disabled, Banned or Delete");
});
Deno.test("set-status: a failed ban says the login can still sign in", async () => {
  reset();
  route("GET teachers", 200, [{ auth_uid: "11111111-1111-4111-8111-111111111111" }]);
  route("PUT auth/admin/users", 500, { code: "unexpected_failure", msg: "database error" });
  expectFailure(await call("set-teacher-status", { body: { email: "t@b.pk", status: "DISABLED" } }), 500, "AUTH_FAILED", "still sign in");
});
