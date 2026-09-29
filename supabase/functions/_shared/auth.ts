// Shared helpers for CMS Edge Functions: verify the CALLER (from their JWT)
// before acting with the service-role client. Service role bypasses RLS, so
// every function must gate on requireAdmin() itself.
//
// Error contract (read by the apps' EdgeFunctionErrors.kt): every failure is
//   { "error": "<sentence a user can read>", "code": "<SHORT_CODE>" }
// with a meaningful HTTP status. The message is ALWAYS safe to show to the end user: never put raw
// database/auth error text, table or column names, request-body key names or stack traces in it --
// log those with console.error and translate them into plain words here instead.
import { createClient, SupabaseClient } from "npm:@supabase/supabase-js@2";

export function serviceClient(): SupabaseClient {
  return createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
    { auth: { autoRefreshToken: false, persistSession: false } },
  );
}

const BOOTSTRAP_ADMIN_EMAIL = "admin@example.com"; // mirrors bootstrap_admin_email() in SQL

/** Resolves the caller from the Authorization header and asserts admin rights. */
export async function requireAdmin(
  req: Request,
  svc: SupabaseClient,
): Promise<{ email: string; uid: string }> {
  const jwt = req.headers.get("Authorization")?.replace("Bearer ", "");
  if (!jwt) throw httpError(401, "You're not signed in. Sign in again.", "UNAUTHORIZED");

  const { data: { user }, error } = await svc.auth.getUser(jwt);
  // A rejected token is the caller's problem (401); an auth service that could not answer is ours (500) -- otherwise an
  // outage would tell every admin their session had expired.
  if (error && (error.status === undefined || error.status === 0 || error.status >= 500)) {
    console.error("auth lookup failed:", error);
    throw permissionCheckFailed();
  }
  if (error || !user?.email) throw httpError(401, "Your session has expired. Sign in again.", "UNAUTHORIZED");
  const email = user.email.trim().toLowerCase();

  if (email === BOOTSTRAP_ADMIN_EMAIL) return { email, uid: user.id };

  // A lookup that fails is not "not an admin": report it as a server problem so a database hiccup doesn't read as a permission error.
  const { data: profile, error: profileErr } = await svc.from("profiles")
    .select("role,status").eq("id", user.id).maybeSingle();
  if (profileErr) { console.error("admin check (profile):", profileErr); throw permissionCheckFailed(); }
  if (profile?.role === "ADMIN" && profile?.status === "ACTIVE") {
    return { email, uid: user.id };
  }
  const { data: teacher, error: teacherErr } = await svc.from("teachers")
    .select("is_admin,status,is_active").eq("email", email).maybeSingle();
  if (teacherErr) { console.error("admin check (teacher):", teacherErr); throw permissionCheckFailed(); }
  if (teacher?.is_admin && teacher?.status === "ACTIVE" && teacher?.is_active) {
    return { email, uid: user.id };
  }
  throw httpError(403, "Only an admin can do this.", "FORBIDDEN");
}

function permissionCheckFailed(): Response {
  return httpError(500, "Couldn't check your permissions right now. Try again in a moment.", "PERMISSION_CHECK_FAILED");
}

const DEFAULT_CODES: Record<number, string> = {
  400: "VALIDATION",
  401: "UNAUTHORIZED",
  403: "FORBIDDEN",
  404: "NOT_FOUND",
  409: "CONFLICT",
  429: "RATE_LIMIT",
};

/** A JSON error response. [message] must already be user-safe; [code] defaults from the status. */
export function httpError(status: number, message: string, code?: string): Response {
  return new Response(
    JSON.stringify({ error: message, code: code ?? DEFAULT_CODES[status] ?? "INTERNAL" }),
    { status, headers: { "Content-Type": "application/json" } },
  );
}

export function ok(body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
}

/** Parses the request body, turning a malformed/empty body into a clear 400 instead of a 500. */
export async function readJson(req: Request): Promise<Record<string, unknown>> {
  try {
    const body = await req.json();
    if (body && typeof body === "object") return body as Record<string, unknown>;
  } catch (_) {
    // fall through
  }
  throw httpError(400, "The request wasn't valid. Refresh the app and try again.", "VALIDATION");
}

type DbLikeError = { code?: string | null; message?: string | null };

/**
 * Turns a Postgres/PostgREST error into a user-safe response. Known SQLSTATEs get a plain-words
 * message; anything else becomes [fallback] (a sentence that says WHICH step failed) with a 500.
 * Raw text is only ever logged, never returned.
 */
export function dbError(err: DbLikeError, fallback: string): Response {
  console.error("db error:", err);
  switch (err.code) {
    case "23505":
      return httpError(409, "That record already exists.", "ALREADY_EXISTS");
    case "23503":
      return httpError(409, "A related record is missing or still in use, so this couldn't be completed.", "RELATED_RECORDS");
    case "23502":
    case "23514":
    case "22001":
    case "22P02":
      return httpError(400, "One of the values isn't valid. Check the details and try again.", "VALIDATION");
    case "42501":
      return httpError(403, "The server isn't allowed to change that record.", "FORBIDDEN");
    case "P0001": {
      // A RAISE EXCEPTION from one of our own triggers: written for users, safe to pass on.
      const first = (err.message ?? "").split("\n")[0].trim();
      if (first && first.length <= 220 && !/https?:|supabase|postgrest|exception|\{|\}/i.test(first)) {
        return httpError(409, first, "CONFLICT");
      }
      return httpError(500, fallback, "DB_FAILED");
    }
    default:
      return httpError(500, fallback, "DB_FAILED");
  }
}

type AuthLikeError = { code?: string | null; message?: string | null; status?: number | null };

/**
 * Turns a GoTrue admin-API error into a user-safe response. [fallback] says which step failed and is
 * used for anything unrecognised; the raw message is logged only.
 */
export function authError(err: AuthLikeError, fallback: string): Response {
  console.error("auth error:", err);
  const text = `${err.code ?? ""} ${err.message ?? ""}`.toLowerCase();
  if (text.includes("email_exists") || text.includes("already been registered") || text.includes("already registered")) {
    return httpError(409, "An account with this email already exists.", "EMAIL_EXISTS");
  }
  if (text.includes("weak_password") || text.includes("password should be") || text.includes("password is too")) {
    return httpError(400, "Choose a stronger password (at least 6 characters).", "WEAK_PASSWORD");
  }
  if (text.includes("email_address_invalid") || text.includes("validation_failed") || text.includes("invalid email")) {
    return httpError(400, "That email address isn't valid.", "INVALID_EMAIL");
  }
  if (text.includes("rate_limit") || err.status === 429) {
    return httpError(429, "Too many attempts. Wait a minute and try again.", "RATE_LIMIT");
  }
  if (text.includes("user_not_found")) {
    return httpError(404, "That account no longer exists.", "NOT_FOUND");
  }
  return httpError(500, fallback, "AUTH_FAILED");
}

/** Wraps a handler with uniform error handling (thrown Responses pass through). */
export function handle(
  fn: (req: Request) => Promise<Response>,
): (req: Request) => Promise<Response> {
  return async (req) => {
    try {
      return await fn(req);
    } catch (e) {
      if (e instanceof Response) return e;
      console.error(e);
      // Never echo e.message: it can carry raw database/auth/network text.
      return httpError(500, "The server hit an unexpected problem. Try again in a moment.", "INTERNAL");
    }
  };
}
