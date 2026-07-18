import assert from "node:assert/strict";
import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import test from "node:test";

import {
  acceptWebhook,
  buildPrompt,
  enqueueReport,
  openState,
  repairBatches,
  validateReport,
  webhookSignature,
} from "../poller.mjs";
import { notify, signature } from "../notify.mjs";

function report(overrides = {}) {
  const findings = Array.from({ length: 23 }, (_, index) => ({ id: `missing:${index}` }));
  return {
    "schema-version": 1,
    kind: "code.manage.incomplete",
    repository: "owner/repo",
    ref: "refs/heads/main",
    sha: "abc123",
    "run-id": "42",
    section: "code",
    label: "code",
    policy: "new-only",
    fingerprint: "fingerprint",
    "test-command": "lein test :with [code]",
    diff: { new: findings.map(({ id }) => id) },
    counts: { total: 23, new: 23 },
    findings,
    ...overrides,
  };
}

test("validates report provenance against the completed workflow", () => {
  const value = report();
  assert.equal(validateReport(value, { headSha: "abc123" }, "owner/repo"), value);
  assert.throws(
    () => validateReport({ ...value, ref: "refs/pull/1/merge" }, { headSha: "abc123" }, "owner/repo"),
    /not from main/,
  );
  assert.throws(
    () => validateReport(value, { headSha: "different" }, "owner/repo"),
    /SHA does not match/,
  );
});

test("batches only new-only findings into draft PRs of at most ten", () => {
  const batches = repairBatches(report());
  assert.deepEqual(batches.map(({ findings }) => findings.length), [10, 10, 3]);
  assert.deepEqual(batches.map(({ batch }) => batch), [1, 2, 3]);
  assert.deepEqual(repairBatches(report({ policy: "observe" })), []);
});

test("builds a constrained semantic test-repair prompt", () => {
  const prompt = buildPrompt(report({ findings: [{ id: "missing:semantic" }] }));
  assert.match(prompt, /semantic return values or state/);
  assert.match(prompt, /Do not commit, push, or open a pull request/);
  assert.match(prompt, /missing:semantic/);
  assert.match(prompt, /temporarily break its expectation/);
  assert.match(prompt, /needs-human-contract/);
});

test("persists jobs and deduplicates a workflow report", () => {
  const directory = mkdtempSync(join(tmpdir(), "code-manage-poller-test-"));
  try {
    const db = openState(join(directory, "state.sqlite"));
    assert.equal(enqueueReport(db, 42, report()), 3);
    assert.equal(enqueueReport(db, 42, report()), 0);
    const jobs = db.prepare("SELECT status, section FROM repair_jobs ORDER BY id").all()
      .map((job) => ({ ...job }));
    assert.deepEqual(jobs, [
      { status: "queued", section: "code" },
      { status: "queued", section: "code" },
      { status: "queued", section: "code" },
    ]);
    db.close();
  } finally {
    rmSync(directory, { force: true, recursive: true });
  }
});

test("accepts authentic webhook reports once and rejects tampering", () => {
  const directory = mkdtempSync(join(tmpdir(), "code-manage-webhook-test-"));
  try {
    const db = openState(join(directory, "state.sqlite"));
    const body = JSON.stringify(report());
    const headers = {
      "x-foundation-event": "code.manage.incomplete.v1",
      "x-foundation-idempotency-key": "delivery-42-code",
      "x-foundation-signature": webhookSignature("secret", body),
    };
    assert.deepEqual(acceptWebhook({ db, repository: "owner/repo", secret: "secret", body, headers }),
      { duplicate: false, jobs: 3 });
    assert.deepEqual(acceptWebhook({ db, repository: "owner/repo", secret: "secret", body, headers }),
      { duplicate: true, jobs: 0 });
    assert.throws(() => acceptWebhook({
      db, repository: "owner/repo", secret: "secret", body: `${body} `, headers,
    }), /invalid webhook signature/);
    db.close();
  } finally {
    rmSync(directory, { force: true, recursive: true });
  }
});

test("notifier signs the exact body sent to the service", async () => {
  const directory = mkdtempSync(join(tmpdir(), "code-manage-notify-test-"));
  try {
    const { writeFileSync } = await import("node:fs");
    writeFileSync(join(directory, "code.json"), JSON.stringify(report()));
    const requests = [];
    const sent = await notify({
      directory, url: "https://build.example/v1/findings", secret: "secret",
      fetchFn: async (url, options) => {
        requests.push({ url, options });
        return { ok: true, status: 202 };
      },
    });
    assert.equal(sent, 1);
    assert.equal(requests[0].options.headers["x-foundation-signature"],
      signature("secret", requests[0].options.body));
  } finally {
    rmSync(directory, { force: true, recursive: true });
  }
});
