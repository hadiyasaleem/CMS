// Shared helpers for CMS Edge Functions: verify the CALLER (from their JWT)
// before acting with the service-role client. Service role bypasses RLS, so
// every function must gate on requireAdmin() itself.
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
  if (!jwt) throw httpError(401, "Missing Authorization header");

  const { data: { user }, error } = await svc.auth.getUser(jwt);
  if (error || !user?.email) throw httpError(401, "Invalid token");
  const email = user.email.trim().toLowerCase();

  if (email === BOOTSTRAP_ADMIN_EMAIL) return { email, uid: user.id };

  const { data: profile } = await svc.from("profiles")
    .select("role,status").eq("id", user.id).maybeSingle();
  if (profile?.role === "ADMIN" && profile?.status === "ACTIVE") {
    return { email, uid: user.id };
  }
  const { data: teacher } = await svc.from("teachers")
    .select("is_admin,status,is_active").eq("email", email).maybeSingle();
  if (teacher?.is_admin && teacher?.status === "ACTIVE" && teacher?.is_active) {
    return { email, uid: user.id };
  }
  throw httpError(403, "Admin rights required");
}

export function httpError(status: number, message: string): Response {
  return new Response(JSON.stringify({ error: message }), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

export function ok(body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
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
      return httpError(500, e instanceof Error ? e.message : "Internal error");
    }
  };
}
