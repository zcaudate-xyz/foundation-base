# code.manage test-repair automation

`automation.edn` maps the existing GitHub Actions test splits to `code.manage`
namespace selectors and targeted verification commands.

The checked-in policy is `:observe`. Pull requests and main-branch runs publish
JSON artifacts and annotations. Main also sends changed reports to the repair
service, which records them without scheduling repairs. After reviewing the
reports, generate and commit the baseline one section at a time:

```bash
lein manage incomplete-report :section code :output target/code.json :write-baseline
```

Repeat for `hara`, `std`, `xt`, `code`, `foundation`, `web3`, `kmi`, and
`postgres`, review `incomplete-baseline.edn`, then change `:policy` to
`:new-only`. Only findings outside that reviewed baseline will fail CI and be
queued for repair.

## Signed webhook and local repair service

GitHub Actions collects the section artifacts after the matrix completes and
POSTs each changed report to `/v1/findings`. It signs the exact JSON body with
HMAC-SHA256. The local service verifies the signature, main-branch repository
and commit metadata, and an idempotency key before persisting jobs in SQLite.

Configure matching GitHub Actions secrets:

- `CODE_MANAGE_WEBHOOK_URL`: an externally reachable HTTPS URL ending in
  `/v1/findings`
- `CODE_MANAGE_WEBHOOK_SECRET`: a long random shared secret

Expose port 8787 through an authenticated HTTPS reverse proxy or tunnel; do not
expose the raw HTTP listener directly. Set the same secret locally and run:

```bash
CODE_MANAGE_GITHUB_REPOSITORY=zcaudate-xyz/foundation-base \
CODE_MANAGE_WEBHOOK_SECRET='...' \
npm run code-manage:serve
```

`GET /health` supports build-server monitoring. The worker handles one queued
repair at a time. A job contains at most ten findings, is attempted twice, and
then remains in SQLite with `failed` status for investigation.

The worker creates an isolated git worktree and invokes the locally installed
Codex CLI. Codex is instructed to make semantic assertions, deliberately prove
that every new fact can fail, and report `needs-human-contract` instead of
inventing an assertion. The worker independently runs the section test command.
Only after it passes does the worker commit, push, and open a draft PR.

Before enabling the service, verify local authentication:

```bash
gh auth status
codex login status
```

### Artifact polling fallback

The service can also use the authenticated `gh` CLI to poll completed main
workflow runs and download the same artifacts:

```bash
CODE_MANAGE_GITHUB_REPOSITORY=zcaudate-xyz/foundation-base \
npm run code-manage:poll -- --once
```

### Run continuously with launchd

The checked-in plist runs the webhook server and worker for this repository's
current path. Replace its example secret before loading it:

```bash
cp config/manage/com.greenways.code-manage-poller.plist.example \
  ~/Library/LaunchAgents/com.greenways.code-manage-poller.plist

launchctl bootstrap gui/$(id -u) \
  ~/Library/LaunchAgents/com.greenways.code-manage-poller.plist
```

Logs are written to `~/Library/Logs/code-manage-poller.log` and
`~/Library/Logs/code-manage-poller.error.log`. Stop it with:

```bash
launchctl bootout gui/$(id -u) \
  ~/Library/LaunchAgents/com.greenways.code-manage-poller.plist
```

State is persisted in `target/code-manage-poller.sqlite`. The computer must be
awake, online, and reachable through the configured HTTPS endpoint.
