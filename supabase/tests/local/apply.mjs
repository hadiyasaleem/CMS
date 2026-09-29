import { pg, ROOT, HERE, DB } from "./harness.mjs";
import path from "node:path";
import fs from "node:fs";
const migDir = path.join(ROOT, "supabase", "migrations");
const conn = (database) => new pg.Client({ ...DB, database });
const admin = conn("postgres");
await admin.connect();
await admin.query("drop database if exists cms_test with (force)");
await admin.query("create database cms_test encoding 'UTF8' template template0 lc_collate 'C' lc_ctype 'C'");
await admin.end();
const c = conn("cms_test");
await c.connect();
await c.query(fs.readFileSync(path.join(HERE, "shim.sql"), "utf8"));
const only = process.argv[2]; // optional: stop after this file name prefix
for (const f of fs.readdirSync(migDir).filter((f) => f.endsWith(".sql")).sort()) {
  let sql = fs.readFileSync(`${migDir}/${f}`, "utf8");
  sql = sql.replace(/create extension if not exists pg_cron[^;]*;/gi, "-- (pg_cron shimmed)");
  try {
    await c.query(sql);
    console.log("OK   ", f);
  } catch (e) {
    console.log("FAIL ", f, "\n   ", e.message, e.position ? `(pos ${e.position})` : "", e.where ? "\n    where: " + e.where.split("\n")[0] : "");
    process.exitCode = 1;
    if (!process.env.CONTINUE) break;
  }
  if (only && f.startsWith(only)) break;
}
await c.end();
