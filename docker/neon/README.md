# Foundation Neon environments

These are persistent cloud branches with local bridge containers. Docker does
not host a replacement for Neon's managed services.

The scaffold owns the **StatsTrade Foundation** project in the Greenways
organization, AWS Singapore (`aws-ap-southeast-1`), Postgres **18**. Realtime
requires Postgres 18. An explicit suspend interval is unavailable on this
account, so compute inherits the account's suspension policy. Branches use
0.25 CU and do not expire.

| Branch | Local bridge | Purpose |
| --- | --- | --- |
| neon-min | http://localhost:18787 | Reusable automated integration environment |
| neon-dev | http://localhost:18788 | Persistent synthetic users, rows and topics |

## Setup and daily use

Prerequisites: Node 24+, authenticated Neon CLI 8.14+, Docker Compose, Java/Lein,
and the Python and Ruby runtimes/dependencies used by Foundation tests. Install
bridge dependencies with `npm ci` in `src-js/neon-bridge`. Ruby needs
`concurrent-ruby`; Python's HTTP Neon path does not require a WebSocket package.

From StatsTrade's root:

```sh
make neon-setup
make neon-up NEON_ENV=min
make neon-up NEON_ENV=dev
make neon-status NEON_ENV=min
make test-neon NEON_ENV=min
make neon-down NEON_ENV=min
```

From Foundation's root, the equivalent commands are:

```sh
node src-js/neon-bridge/environments.mjs setup
node src-js/neon-bridge/environments.mjs up min
node src-js/neon-bridge/environments.mjs test min
```

`setup` reuses recorded project/branch IDs, applies `neon.ts`, configures the
Data API to expose only `neon_scratch`, installs the fixture schema, and seeds
dev accounts. Existing dev row values survive repeated setup. Development
email/password verification is disabled on these synthetic-only branches.

`up` starts a branch's bridge and checks SQL, Auth, Data API ownership rules,
and a real Realtime subscription. `status` checks bridge/database health.
Docker's periodic liveness probe does not query Postgres, so it does not keep
cloud compute awake; database readiness is checked on demand.
`down` stops only the local container and retains cloud data. The root Makefile's
default target remains the existing Docker application workflow.

`test` runs real service checks followed by Foundation's TLS JDBC test and the
canonical JavaScript adapter tests, generates the Python/Ruby benches using
seedgen, and executes them. Missing configuration is an error for this command.
The ordinary unit-test suites remain runnable without cloud credentials.

## Configuration and interfaces

Ignored `.local/project.json` records the cloud project identity. `.local/min.env`
and `.local/dev.env` contain owner connection and managed-service credentials;
the separate `*.bridge.env` files give containers only a read-only database role
and the server-side Auth/Realtime settings. All files are written with mode 0600.
Do not print or commit them. The internal `config` command returns secrets for
the Clojure harness; use `status` for human-readable diagnostics.

`scaffold.neon/jdbc-config` returns a direct, encrypted JDBC configuration with
startup/shutdown hooks. Its optional shutdown hook stops the local bridge only.
`scaffold.neon/adapter-defaults` returns only `data_url` and `bridge_url`.
StatsTrade provides wrappers in `statsbuild.testing.neon.min` and `.dev`.

Select `backend-neon/provider` and install the Neon node handlers explicitly.
No default provider, app login, Supabase environment, or StatsTrade SQL
publication is changed by this scaffold.

## Fixtures, cleanup and recovery

`neon_scratch` contains `NeonSmoke` and the echo/update RPCs. Data API access is
restricted to the current user. The update RPC explicitly checks identity,
ownership, and topic membership before its privileged transactional write.
`xt_neon` stores private retained events; `xt_neon_test` stores private membership
and fixture ownership records. Realtime authorization is enforced by the bridge's
membership queries rather than relying on RLS in the live query engine.

Dev has two persistent users. Their generated passwords are stored only in
`dev.env` as `NEON_DEV_OWNER_PASSWORD` and `NEON_DEV_OTHER_PASSWORD`. Sample row
IDs are `11111111-1111-4111-8111-111111111111` and
`22222222-2222-4222-8222-222222222222`, with topics `foundation-dev:owner` and
`foundation-dev:other`.

Each integration run holds a database advisory lock and creates unique test
identities/topics. It removes its own fixture rows and Auth users afterward.
Concurrent runs against the same branch fail clearly. Different environments
can run simultaneously. Stop an abandoned process to release its session lock.

For leftovers after a crash, explicitly reset the selected fixture environment:

```sh
make neon-reset NEON_ENV=min
```

Reset acquires the same lock, removes registered synthetic Auth users, recreates
only the three Foundation-owned schemas, and reseeds dev when selected. It never
deletes a branch or recreates managed Auth. Restart the bridge after reset to
discard any in-memory sessions/subscriptions. Test failures are recorded in
`.local/last-adapter-failure.log` with injected passwords redacted.

The full StatsTrade schema still depends on Supabase Auth/storage/publication
semantics. This environment verifies Foundation capabilities and is not a
StatsTrade application migration.
