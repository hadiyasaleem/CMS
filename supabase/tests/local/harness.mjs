// Local scratch-database harness for the error-message work. Everything it needs lives in <repo>/.testtools
// (gitignored, see Documentation/local-test-tools.md); nothing here touches a real Supabase project.
import fs from "node:fs";
import { createRequire } from "node:module";
import path from "node:path";
import { fileURLToPath } from "node:url";

export const HERE = path.dirname(fileURLToPath(import.meta.url));
export const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
export const TOOLS = path.join(ROOT, ".testtools");
export const OUT = path.join(TOOLS, "run");
fs.mkdirSync(OUT, { recursive: true });
const requireFromTools = createRequire(path.join(TOOLS, "node", "node_modules", "x.js"));
export const pg = requireFromTools("pg");
export const EmbeddedPostgres = requireFromTools("embedded-postgres").default ?? requireFromTools("embedded-postgres");
export const DB = { host: "localhost", port: 54329, user: "postgres", password: "postgres" };
