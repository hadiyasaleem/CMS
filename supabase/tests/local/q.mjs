import { pg, ROOT, OUT, DB } from "./harness.mjs";
import path from "node:path";
const c = new pg.Client({ ...DB, database: "cms_test" });
await c.connect();
const r = await c.query(process.argv[2]);
console.log(r.rows.map((x) => Object.values(x).join(" | ")).join("\n"));
await c.end();
