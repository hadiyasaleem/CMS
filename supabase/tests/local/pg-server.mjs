// Starts a throwaway PostgreSQL on port 54329 (data in .testtools/pgdata) and keeps running until killed.
import fs from "node:fs";
import path from "node:path";
import { EmbeddedPostgres, TOOLS, DB } from "./harness.mjs";

const dir = path.join(TOOLS, "pgdata");
const server = new EmbeddedPostgres({ databaseDir: dir, user: DB.user, password: DB.password, port: DB.port, persistent: true });
if (!fs.existsSync(path.join(dir, "PG_VERSION"))) await server.initialise();
await server.start();
console.log("PG READY");
setInterval(() => {}, 1 << 30);
