#!/usr/bin/env node

import { spawnSync } from "node:child_process";
import { createHmac, timingSafeEqual } from "node:crypto";
import { createServer } from "node:http";
import { DatabaseSync } from "node:sqlite";
import {
  mkdtempSync,
  mkdirSync,
  readFileSync,
  readdirSync,
  rmSync,
} from "node:fs";
import { tmpdir } from "node:os";
import { basename, dirname, join, resolve } from "node:path";

export const MAX_FINDINGS_PER_PR = 10;

function now() {
  return new Date().toISOString();
}

export function run(command, args, options = {}) {
  const result = spawnSync(command, args, {
    cwd: options.cwd,
    env: { ...process.env, ...options.env },
    input: options.input,
    encoding: "utf8",
    maxBuffer: 32 * 1024 * 1024,
    stdio: options.inherit ? "inherit" : "pipe",
  });
  if (result.error) throw result.error;
  if (result.status !== 0) {
    const output = [result.stdout, result.stderr].filter(Boolean).join("\n").trim();
    throw new Error(`${command} ${args.join(" ")} failed (${result.status})${output ? `\n${output}` : ""}`);
  }
  return (result.stdout ?? "").trim();
}

export function openState(path) {
  mkdirSync(dirname(resolve(path)), { recursive: true });
  const db = new DatabaseSync(path);
  db.exec(`
    PRAGMA journal_mode = WAL;
    CREATE TABLE IF NOT EXISTS workflow_runs (
      run_id INTEGER PRIMARY KEY,
      head_sha TEXT NOT NULL,
      status TEXT NOT NULL,
      jobs INTEGER NOT NULL DEFAULT 0,
      error TEXT,
      updated_at TEXT NOT NULL
    );
    CREATE TABLE IF NOT EXISTS repair_jobs (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      job_key TEXT NOT NULL UNIQUE,
      run_id INTEGER NOT NULL,
      section TEXT NOT NULL,
      fingerprint TEXT NOT NULL,
      status TEXT NOT NULL,
      attempts INTEGER NOT NULL DEFAULT 0,
      payload TEXT NOT NULL,
      result TEXT,
      error TEXT,
      updated_at TEXT NOT NULL
    );
    CREATE TABLE IF NOT EXISTS webhook_deliveries (
      idempotency_key TEXT PRIMARY KEY,
      section TEXT NOT NULL,
      fingerprint TEXT NOT NULL,
      jobs INTEGER NOT NULL DEFAULT 0,
      received_at TEXT NOT NULL
    );
  `);
  db.prepare(`
    UPDATE repair_jobs
    SET status = 'failed', error = 'poller stopped while this job was running', updated_at = ?
    WHERE status = 'running'
  `).run(now());
  return db;
}

export function validateReport(report, workflowRun, repository) {
  if (report?.["schema-version"] !== 1) throw new Error("unsupported report schema");
  if (report.kind !== "code.manage.incomplete") throw new Error("unsupported report kind");
  if (report.ref !== "refs/heads/main") throw new Error(`report is not from main: ${report.ref}`);
  if (report.repository !== repository) throw new Error(`unexpected repository: ${report.repository}`);
  if (report.sha !== workflowRun.headSha) throw new Error("report SHA does not match workflow run");
  if (!report.section || !report.fingerprint || !report["test-command"]) {
    throw new Error("report is missing repair metadata");
  }
  return report;
}

export function webhookSignature(secret, body) {
  return `sha256=${createHmac("sha256", secret).update(body).digest("hex")}`;
}

export function verifyWebhookSignature(secret, body, supplied) {
  if (!secret || !supplied) return false;
  const expected = Buffer.from(webhookSignature(secret, body));
  const actual = Buffer.from(supplied);
  return expected.length === actual.length && timingSafeEqual(expected, actual);
}

export function acceptWebhook({ db, repository, secret, body, headers }) {
  if (headers["x-foundation-event"] !== "code.manage.incomplete.v1") {
    throw new Error("unsupported webhook event");
  }
  if (!verifyWebhookSignature(secret, body, headers["x-foundation-signature"])) {
    throw new Error("invalid webhook signature");
  }
  const report = JSON.parse(body);
  validateReport(report, { headSha: report.sha }, repository);
  const idempotency = headers["x-foundation-idempotency-key"];
  if (!idempotency) throw new Error("missing idempotency key");
  const inserted = db.prepare(`
    INSERT OR IGNORE INTO webhook_deliveries
      (idempotency_key, section, fingerprint, received_at)
    VALUES (?, ?, ?, ?)
  `).run(idempotency, report.section, report.fingerprint, now());
  if (!inserted.changes) return { duplicate: true, jobs: 0 };
  try {
    const runId = Number(report["run-id"]);
    if (!Number.isSafeInteger(runId)) throw new Error("report has invalid run-id");
    const jobs = enqueueReport(db, runId, report);
    db.prepare("UPDATE webhook_deliveries SET jobs = ? WHERE idempotency_key = ?")
      .run(jobs, idempotency);
    return { duplicate: false, jobs };
  } catch (error) {
    db.prepare("DELETE FROM webhook_deliveries WHERE idempotency_key = ?").run(idempotency);
    throw error;
  }
}

export function startWebhookServer({ db, repository, secret, port = 8787 }) {
  if (!secret) throw new Error("CODE_MANAGE_WEBHOOK_SECRET is required");
  const server = createServer((request, response) => {
    if (request.method === "GET" && request.url === "/health") {
      response.writeHead(200, { "content-type": "application/json" });
      response.end(JSON.stringify({ ok: true }));
      return;
    }
    if (request.method !== "POST" || request.url !== "/v1/findings") {
      response.writeHead(404).end(); return;
    }
    let body = "";
    request.setEncoding("utf8");
    request.on("data", (chunk) => {
      body += chunk;
      if (Buffer.byteLength(body) > 1024 * 1024) request.destroy();
    });
    request.on("end", () => {
      try {
        const result = acceptWebhook({ db, repository, secret, body, headers: request.headers });
        response.writeHead(result.duplicate ? 200 : 202, { "content-type": "application/json" });
        response.end(JSON.stringify({ accepted: true, ...result }));
      } catch (error) {
        response.writeHead(400, { "content-type": "application/json" });
        response.end(JSON.stringify({ accepted: false, error: error.message }));
      }
    });
  });
  server.listen(port);
  return server;
}

export function repairBatches(report) {
  if (report.policy !== "new-only") return [];
  const ids = new Set(report.diff?.new ?? []);
  const findings = (report.findings ?? []).filter((finding) => ids.has(finding.id));
  const batches = [];
  for (let index = 0; index < findings.length; index += MAX_FINDINGS_PER_PR) {
    const batch = findings.slice(index, index + MAX_FINDINGS_PER_PR);
    batches.push({
      ...report,
      batch: batches.length + 1,
      findings: batch,
      counts: { ...report.counts, new: batch.length },
    });
  }
  return batches;
}

export function buildPrompt(report) {
  return [
    "Repair the incomplete code.test facts described in the JSON report below.",
    "Follow AGENTS.md and the repository's custom code.test conventions.",
    "Tests must prove semantic return values or state, not merely successful delivery.",
    "Use one :refer fact per namespace or give additional facts unique :id metadata.",
    "Do not edit generated test-lang/xtbench files directly.",
    "Only address the listed findings. Do not commit, push, or open a pull request.",
    "For every new fact, temporarily break its expectation, confirm the intended fact fails, then restore it and rerun.",
    "If the semantic contract cannot be established, stop and report needs-human-contract rather than inventing an assertion.",
    "Run the targeted section test before returning.",
    "",
    JSON.stringify(report, null, 2),
  ].join("\n");
}

export function listWorkflowRuns({ repository, workflow, limit = 20 }) {
  return JSON.parse(run("gh", [
    "run", "list", "--repo", repository,
    "--workflow", workflow,
    "--branch", "main",
    "--status", "completed",
    "--limit", String(limit),
    "--json", "databaseId,headSha,conclusion,createdAt,event",
  ]));
}

function jsonFiles(directory) {
  const files = [];
  for (const entry of readdirSync(directory, { withFileTypes: true })) {
    const path = join(directory, entry.name);
    if (entry.isDirectory()) files.push(...jsonFiles(path));
    else if (entry.isFile() && entry.name.endsWith(".json")) files.push(path);
  }
  return files.sort();
}

export function downloadReports({ repository, runId }) {
  const directory = mkdtempSync(join(tmpdir(), `code-manage-run-${runId}-`));
  try {
    try {
      run("gh", [
        "run", "download", String(runId),
        "--repo", repository,
        "--pattern", "code-manage-incomplete-*",
        "--dir", directory,
      ]);
    } catch (error) {
      if (/no (valid )?artifacts? found|no artifacts? match/i.test(error.message)) return [];
      throw error;
    }
    return jsonFiles(directory).map((path) => JSON.parse(readFileSync(path, "utf8")));
  } finally {
    rmSync(directory, { force: true, recursive: true });
  }
}

export function enqueueReport(db, runId, report) {
  const insert = db.prepare(`
    INSERT OR IGNORE INTO repair_jobs
      (job_key, run_id, section, fingerprint, status, payload, updated_at)
    VALUES (?, ?, ?, ?, 'queued', ?, ?)
  `);
  let inserted = 0;
  for (const batch of repairBatches(report)) {
    const key = `${runId}:${batch.section}:${batch.batch}:${batch.fingerprint}`;
    inserted += Number(insert.run(
      key, runId, batch.section, batch.fingerprint, JSON.stringify(batch), now(),
    ).changes);
  }
  return inserted;
}

export function pollOnce({ db, repository, workflow = "run-test.yml", limit = 20 }) {
  const known = db.prepare(`
    SELECT 1 FROM workflow_runs WHERE run_id = ? AND status IN ('processed', 'skipped')
  `);
  const record = db.prepare(`
    INSERT INTO workflow_runs (run_id, head_sha, status, jobs, error, updated_at)
    VALUES (?, ?, ?, ?, ?, ?)
    ON CONFLICT(run_id) DO UPDATE SET
      status = excluded.status, jobs = excluded.jobs,
      error = excluded.error, updated_at = excluded.updated_at
  `);
  const runs = listWorkflowRuns({ repository, workflow, limit })
    .sort((left, right) => left.databaseId - right.databaseId);
  let jobs = 0;
  for (const workflowRun of runs) {
    if (known.get(workflowRun.databaseId)) continue;
    try {
      const reports = downloadReports({ repository, runId: workflowRun.databaseId });
      let runJobs = 0;
      for (const report of reports) {
        validateReport(report, workflowRun, repository);
        runJobs += enqueueReport(db, workflowRun.databaseId, report);
      }
      jobs += runJobs;
      const status = reports.length ? "processed" : "skipped";
      record.run(workflowRun.databaseId, workflowRun.headSha, status, runJobs, null, now());
      console.log(`${status} workflow run ${workflowRun.databaseId}: ${runJobs} repair job(s)`);
    } catch (error) {
      record.run(workflowRun.databaseId, workflowRun.headSha, "failed", 0, error.message, now());
      console.error(`workflow run ${workflowRun.databaseId} failed: ${error.message}`);
    }
  }
  return jobs;
}

function claimJob(db) {
  db.exec("BEGIN IMMEDIATE");
  try {
    const job = db.prepare("SELECT * FROM repair_jobs WHERE status = 'queued' ORDER BY id LIMIT 1").get();
    if (job) {
      db.prepare(`
        UPDATE repair_jobs
        SET status = 'running', attempts = attempts + 1, updated_at = ?
        WHERE id = ? AND status = 'queued'
      `).run(now(), job.id);
    }
    db.exec("COMMIT");
    return job ? { ...job, status: "running", attempts: job.attempts + 1 } : null;
  } catch (error) {
    db.exec("ROLLBACK");
    throw error;
  }
}

function branchName(report, runId) {
  const section = report.section.replace(/[^a-zA-Z0-9._-]/g, "-");
  return `codex/incomplete/${section}/${report.fingerprint.slice(0, 12)}-${report.batch}`;
}

function pullRequestBody(report) {
  const findings = report.findings.map((finding) => `- \`${finding.id}\``).join("\n");
  return [
    `Automated local Codex repair for \`${report.section}\` at \`${report.sha}\`.`,
    "", "Findings repaired:", findings, "", "Verification:",
    `- \`${report["test-command"]}\``,
    "- New facts were required to fail after a deliberate expectation break before restoration.",
  ].join("\n");
}

export function repairJob({ report, runId, repository, checkout, codexBin = "codex" }) {
  const parent = mkdtempSync(join(tmpdir(), "code-manage-repair-"));
  const worktree = join(parent, "worktree");
  const branch = branchName(report, runId);
  try {
    run("git", ["fetch", "origin", "main"], { cwd: checkout });
    run("git", ["worktree", "add", "-b", branch, worktree, report.sha], { cwd: checkout });
    run(codexBin, [
      "exec", "--ephemeral",
      "--ask-for-approval", "never",
      "--sandbox", "workspace-write",
      "--cd", worktree,
      "-",
    ], { cwd: worktree, input: buildPrompt(report) });
    run("bash", ["-lc", report["test-command"]], { cwd: worktree, inherit: true });
    const changes = run("git", ["status", "--porcelain"], { cwd: worktree });
    if (!changes) throw new Error("Codex produced no repository changes");
    run("git", ["add", "--all"], { cwd: worktree });
    run("git", ["commit", "-m", `test: repair incomplete ${report.label} coverage`], { cwd: worktree });
    run("git", ["push", "--set-upstream", "origin", branch], { cwd: worktree });
    const pullRequest = run("gh", [
      "pr", "create", "--repo", repository,
      "--draft", "--base", "main", "--head", branch,
      "--title", `Repair incomplete tests: ${report.label}`,
      "--body", pullRequestBody(report),
    ], { cwd: worktree });
    return { branch, pullRequest };
  } finally {
    try { run("git", ["worktree", "remove", "--force", worktree], { cwd: checkout }); } catch {}
    rmSync(parent, { force: true, recursive: true });
  }
}

export function processNextJob({ db, repository, checkout, codexBin }) {
  const job = claimJob(db);
  if (!job) return false;
  try {
    const result = repairJob({
      report: JSON.parse(job.payload),
      runId: job.run_id,
      repository,
      checkout,
      codexBin,
    });
    db.prepare(`
      UPDATE repair_jobs SET status = 'completed', result = ?, error = NULL, updated_at = ?
      WHERE id = ?
    `).run(JSON.stringify(result), now(), job.id);
    console.log(`created draft PR ${result.pullRequest}`);
  } catch (error) {
    const nextStatus = job.attempts < 2 ? "queued" : "failed";
    db.prepare(`
      UPDATE repair_jobs SET status = ?, error = ?, updated_at = ? WHERE id = ?
    `).run(nextStatus, error.message, now(), job.id);
    console.error(`repair job ${job.id} ${nextStatus}: ${error.message}`);
  }
  return true;
}

function configFromEnvironment() {
  const checkout = resolve(process.env.CODE_MANAGE_REPOSITORY ?? process.cwd());
  const repository = process.env.CODE_MANAGE_GITHUB_REPOSITORY;
  if (!repository) throw new Error("CODE_MANAGE_GITHUB_REPOSITORY is required (for example, owner/repo)");
  return {
    checkout,
    repository,
    workflow: process.env.CODE_MANAGE_WORKFLOW ?? "run-test.yml",
    database: resolve(process.env.CODE_MANAGE_STATE ?? join(checkout, "target/code-manage-poller.sqlite")),
    codexBin: process.env.CODEX_BIN ?? "codex",
    interval: Number(process.env.CODE_MANAGE_POLL_SECONDS ?? 300) * 1000,
    webhookSecret: process.env.CODE_MANAGE_WEBHOOK_SECRET,
    port: Number(process.env.CODE_MANAGE_PORT ?? 8787),
  };
}

export async function main(args = process.argv.slice(2)) {
  const once = args.includes("--once");
  const config = configFromEnvironment();
  const db = openState(config.database);
  if (args.includes("--serve")) {
    startWebhookServer({ db, repository: config.repository,
                         secret: config.webhookSecret, port: config.port });
    console.log(`listening for code.manage findings on port ${config.port}`);
    setInterval(() => { while (processNextJob({ ...config, db })) {} }, 5000);
    return;
  }
  const cycle = () => {
    pollOnce({ db, repository: config.repository, workflow: config.workflow });
    while (processNextJob({ ...config, db })) {}
  };
  cycle();
  if (once) return;
  console.log(`polling ${config.repository} every ${config.interval / 1000}s`);
  setInterval(cycle, config.interval);
}

if (process.argv[1] && basename(process.argv[1]) === basename(import.meta.filename)) {
  main().catch((error) => {
    console.error(error.stack ?? error.message);
    process.exitCode = 1;
  });
}
