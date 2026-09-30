// A tiny PostgREST look-alike over the scratch cms_test database, so the app's real Supabase client (and its real
// sync code) can be pointed at the local test database. It implements only what the sync code uses:
//   GET   /rest/v1/<table>?select=*&col=eq.v&col=gte.v&order=col.asc&offset=&limit=   (also a Range header)
//   POST  /rest/v1/<table>?on_conflict=a,b     (upsert, returns the rows when asked)
//   PATCH /rest/v1/<table>?col=eq.v            (update)
// plus test-only helpers:  POST /__sql {sql, params}   GET /__log   POST /__log/clear
// Start it with:  node supabase/tests/local/postgrest-shim.mjs   (needs the embedded Postgres from pg-server.mjs)
import http from "node:http";
import { pg, DB } from "./harness.mjs";

const PORT = Number(process.env.SHIM_PORT ?? 54330);
const pool = new pg.Pool({ ...DB, database: "cms_test", max: 4 });
// The shim acts as a trusted service role (no RLS, no profile guard) so tests can seed and change any row.
pool.on("connect", (c) => c.query("set timezone='UTC'; select set_config('request.jwt.claims', '{\"role\":\"service_role\"}', false)"));
let log = [];
// null = trusted service role; otherwise every REST request runs as this signed-in user, with row-level security applied.
let identity = null;
async function q(sql, values = [], who = identity) {
  if (!who) return pool.query(sql, values);
  const c = await pool.connect();
  try {
    await c.query("begin");
    await c.query("set local role authenticated");
    await c.query("select set_config('request.jwt.claims', $1, true), set_config('request.jwt.claim.sub', $2, true)", [JSON.stringify({ email: who.email, sub: who.sub, role: "authenticated" }), who.sub]);
    const r = await c.query(sql, values);
    await c.query("commit");
    return r;
  } catch (e) {
    await c.query("rollback").catch(() => {});
    throw e;
  } finally {
    c.release();
  }
}
let failing = new Set(); // tables that answer 503, to test how a refresh copes with a failing table

const ident = (s) => {
  if (!/^[a-z_][a-z0-9_]*$/i.test(s)) throw new Error(`bad identifier ${s}`);
  return `"${s}"`;
};
const OPS = { eq: "=", neq: "<>", gt: ">", gte: ">=", lt: "<", lte: "<=" };

function splitTop(s) {
  const out = [];
  let depth = 0, cur = "";
  for (const ch of s) {
    if (ch === "(") depth++;
    if (ch === ")") depth--;
    if (ch === "," && depth === 0) { out.push(cur); cur = ""; } else cur += ch;
  }
  if (cur) out.push(cur);
  return out;
}

// col.op.val  |  and(...)  |  or(...)   -> SQL, pushing bind values
function condSql(cond, values) {
  const grp = cond.match(/^(and|or)\((.*)\)$/);
  if (grp) return "(" + splitTop(grp[2]).map((c) => condSql(c, values)).join(grp[1] === "and" ? " and " : " or ") + ")";
  const [col, op, ...rest] = cond.split(".");
  const val = rest.join(".");
  if (OPS[op]) { values.push(val); return `${ident(col)} ${OPS[op]} $${values.length}`; }
  if (op === "is") return `${ident(col)} is ${val === "null" ? "null" : val === "true" ? "true" : "false"}`;
  throw new Error(`unsupported condition ${cond}`);
}

function whereFrom(params, values) {
  const parts = [];
  for (const [key, raw] of params) {
    if (["select", "order", "offset", "limit", "on_conflict", "columns"].includes(key)) continue;
    if (key === "or" || key === "and") { parts.push(condSql(`${key}${raw}`, values)); continue; }
    const dot = raw.indexOf(".");
    const op = raw.slice(0, dot);
    const val = raw.slice(dot + 1);
    if (OPS[op]) {
      values.push(val);
      parts.push(`${ident(key)} ${OPS[op]} $${values.length}`);
    } else if (op === "is") {
      parts.push(`${ident(key)} is ${val === "null" ? "null" : val === "true" ? "true" : "false"}`);
    } else if (op === "in") {
      const list = val.replace(/^\(|\)$/g, "").split(",").map((v) => v.replace(/^"|"$/g, ""));
      values.push(list);
      parts.push(`${ident(key)}::text = any($${values.length})`);
    } else throw new Error(`unsupported filter ${key}=${raw}`);
  }
  return parts.length ? " where " + parts.join(" and ") : "";
}

const readBody = (req) => new Promise((res) => { let b = ""; req.on("data", (d) => (b += d)); req.on("end", () => res(b)); });

http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://localhost:${PORT}`);
  const send = (code, body, headers = {}) => { res.writeHead(code, { "content-type": "application/json", ...headers }); res.end(body === undefined ? "" : JSON.stringify(body)); };
  try {
    if (url.pathname === "/__log") return send(200, log);
    if (url.pathname === "/__log/clear") { log = []; return send(200, {}); }
    if (url.pathname === "/__fail") { failing = new Set(JSON.parse((await readBody(req)) || "{}").tables ?? []); return send(200, {}); }
    if (url.pathname === "/__as") { const b = JSON.parse((await readBody(req)) || "{}"); identity = b.email ? { email: b.email, sub: b.sub } : null; return send(200, {}); }
    if (url.pathname === "/__sql") {
      const { sql, params, as } = JSON.parse(await readBody(req));
      const r = await q(sql, params ?? [], as ?? null);
      return send(200, r.rows);
    }
    const m = url.pathname.match(/^\/rest\/v1\/([a-z_0-9]+)$/i);
    if (!m) return send(404, { message: "not found" });
    if (failing.has(m[1])) { log.push({ method: req.method, table: m[1], query: url.search, returned: 0, failed: true }); return send(503, { message: "injected failure" }); }
    const table = ident(m[1]);
    const prefer = req.headers["prefer"] ?? "";
    const wantRows = prefer.includes("return=representation");

    if (req.method === "GET") {
      const values = [];
      const where = whereFrom(url.searchParams, values);
      let order = "";
      const o = url.searchParams.get("order");
      if (o) order = " order by " + o.split(",").map((p) => { const [c, d] = p.split("."); return `${ident(c)} ${d === "desc" ? "desc" : "asc"}`; }).join(", ");
      let offset = Number(url.searchParams.get("offset") ?? 0);
      let limit = url.searchParams.get("limit");
      const range = req.headers["range"];
      if (range && /^\d+-\d+$/.test(range)) { const [a, b] = range.split("-").map(Number); offset = a; limit = b - a + 1; }
      const sql = `select row_to_json(t) as r from (select * from ${table}${where}${order}${limit ? ` limit ${Number(limit)}` : ""} offset ${offset}) t`;
      const rows = (await q(sql, values)).rows.map((x) => x.r);
      log.push({ method: "GET", table: m[1], query: url.search, returned: rows.length, ids: rows.map((r) => r.id ?? r.period_id ?? r.session_id ?? null) });
      return send(200, rows);
    }
    if (req.method === "POST") {
      const body = JSON.parse((await readBody(req)) || "[]");
      const items = Array.isArray(body) ? body : [body];
      const conflict = url.searchParams.get("on_conflict");
      const out = [];
      for (const item of items) {
        const cols = Object.keys(item).filter((k) => item[k] !== null || k !== "id");
        const vals = cols.map((k) => item[k]);
        let sql = `insert into ${table} (${cols.map(ident).join(",")}) values (${cols.map((_, i) => `$${i + 1}`).join(",")})`;
        if (conflict) {
          const keys = conflict.split(",");
          const updates = cols.filter((c) => !keys.includes(c));
          sql += ` on conflict (${keys.map(ident).join(",")}) do ` + (updates.length ? `update set ${updates.map((c) => `${ident(c)} = excluded.${ident(c)}`).join(", ")}` : "nothing");
        }
        sql += " returning row_to_json(" + table + ".*) as r";
        const r = await q(sql, vals);
        out.push(...r.rows.map((x) => x.r));
      }
      log.push({ method: "POST", table: m[1], query: url.search, wrote: items.length });
      return wantRows ? send(201, out) : send(201);
    }
    if (req.method === "PATCH") {
      const body = JSON.parse((await readBody(req)) || "{}");
      const values = [];
      const sets = Object.keys(body).map((k) => { values.push(body[k]); return `${ident(k)} = $${values.length}`; });
      const where = whereFrom(url.searchParams, values);
      const r = await q(`update ${table} set ${sets.join(", ")}${where} returning row_to_json(${table}.*) as r`, values);
      log.push({ method: "PATCH", table: m[1], query: url.search, wrote: r.rowCount });
      return wantRows ? send(200, r.rows.map((x) => x.r)) : send(204);
    }
    return send(405, { message: "method not allowed" });
  } catch (e) {
    log.push({ error: e.message, url: req.url });
    send(400, { message: e.message, code: e.code ?? null });
  }
}).listen(PORT, () => console.log(`PostgREST shim on http://localhost:${PORT}`));
