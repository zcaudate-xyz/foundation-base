#!/usr/bin/env node

import { createHmac } from "node:crypto";
import { readFileSync, readdirSync } from "node:fs";
import { join } from "node:path";

function files(path) {
  return readdirSync(path, { withFileTypes: true }).flatMap((entry) => {
    const child = join(path, entry.name);
    return entry.isDirectory() ? files(child) : entry.name.endsWith(".json") ? [child] : [];
  });
}

export function signature(secret, body) {
  return `sha256=${createHmac("sha256", secret).update(body).digest("hex")}`;
}

export async function notify({ directory, url, secret, fetchFn = fetch }) {
  let sent = 0;
  for (const path of files(directory).sort()) {
    const report = JSON.parse(readFileSync(path, "utf8"));
    if (!(report.counts?.new || report.counts?.resolved)) continue;
    const body = JSON.stringify(report);
    const idempotency = `${report.repository}:${report.sha}:${report.section}:${report.fingerprint}`;
    const response = await fetchFn(url, {
      method: "POST",
      headers: {
        "content-type": "application/json",
        "x-foundation-event": "code.manage.incomplete.v1",
        "x-foundation-idempotency-key": idempotency,
        "x-foundation-signature": signature(secret, body),
      },
      body,
    });
    if (!response.ok) throw new Error(`notification failed for ${report.section}: ${response.status}`);
    sent += 1;
  }
  return sent;
}

if (import.meta.filename === process.argv[1]) {
  const [directory] = process.argv.slice(2);
  const url = process.env.CODE_MANAGE_WEBHOOK_URL;
  const secret = process.env.CODE_MANAGE_WEBHOOK_SECRET;
  if (!directory || !url || !secret) throw new Error("directory, CODE_MANAGE_WEBHOOK_URL, and CODE_MANAGE_WEBHOOK_SECRET are required");
  console.log(`sent ${await notify({ directory, url, secret })} code.manage notification(s)`);
}
