# Neon for xt.db and substrate

The `neon` implementation uses the Neon Data API for pulls/RPCs and this Node bridge for email/password Auth and topic events. Clients use ordinary HTTP in JavaScript, Python, Ruby, Dart and Lua/nginx. The bridge uses `@neon/realtime` to watch topic heads and reads retained event rows to serve ordered, replayable batches. It runs alongside the host application; no deployment happens automatically.

## Setup

For persistent `neon-min` and `neon-dev` cloud branches with local Docker bridge
containers and runnable Foundation integration tests, see
[the environment scaffold](../../docker/neon/README.md).

Enable Neon Data API, Managed Better Auth and Realtime on a supported branch. Install `events.sql` in that branch. The private `xt_neon` schema must not be exposed by the Data API. Grant the bridge role SELECT on its tables and the application membership tables. Grant publishers schema USAGE, EXECUTE on `publish`, and the INSERT/UPDATE/SELECT privileges its invoker function needs. Grant the retention operator DELETE/UPDATE and EXECUTE on `prune`; keep these privileges away from clients.

Run `npm ci` in this directory. `example.mjs` shows a service using application-owned `app.topic_members(topic, user_id)` for authorization. Supply `DATABASE_URL`, `NEON_REALTIME_URL`, `NEON_REALTIME_SECRET`, `NEON_AUTH_BASE_URL`, and `APP_ORIGIN`, then run `npm start`. The example listens on loopback port 8787; proxy it at the application's origin over HTTPS. `APP_ORIGIN` must be trusted by Neon Auth. Production cookies require HTTPS. Configure request limits and login rate limits in the host proxy.

For another access model, construct `createBridge({auth, source, authorizeTopic})`. `createNeonSource` requires a `headQuery(user, topic)` callback returning `{sql, params}` that joins current membership tables. Both this query and `authorizeTopic` must enforce access; Realtime does not apply RLS. Use bound parameters. No SQL, database credentials or Realtime secrets are sent to clients.

## Foundation configuration

```clojure
(main/create-impl "neon"
  {"data_url" "https://<branch-data-api-base>"
   "bridge_url" "https://<application-origin>"
   "caching_fn" (fn [] (return caching-impl))
   "resync" (fn [impl topics] (return (reload-cache impl topics)))}
  schema lookup {"neon" (backend-neon/provider)})
```

Require `xt.db.system.backend-neon` as `backend-neon` explicitly. Other backends are not loaded by `main`; see [optional backend setup](../../doc/optional-db-backends.md).

Use the complete Data API base URL from Neon, without a table suffix. Both trailing-slash forms work. Static bearer JWTs can be supplied as `token` when Auth is managed externally. Email/password Auth uses `xt.db.system.impl-neon-session`; successful Auth populates the instance's JWT and opaque bridge credential. Authenticated Data API requests renew the JWT through the bridge. To restore a native session, retain the opaque `bridge_token` securely and call `current-session`. Browser clients can send `cookie: true` in Auth credentials and use the same-origin HttpOnly cookie instead. Never persist a Data API JWT as the bridge credential.

Install `xt.db.node.kernel-neon/init-handlers` on the service node and `proxy-neon/init-handlers` on forwarding nodes. `client-neon` exposes `sign-up`, `sign-in`, `sign-out`, `current-session`, and `refresh-session`. Database pull/RPC and subscribe/unsubscribe use existing `@xt.db/*` handlers. Auth handlers return the bridge's session result; verification-required signup returns no active user. Signing out clears the local session and cache. Hosts must also discard or reload page models on identity changes, so previously published UI outputs do not survive a user switch. `stop-db` stops subscriptions and clears credentials.

## Publishing and replay

Publish within the same database transaction as the application write:

```sql
SELECT xt_neon.publish('organisation:123',
  '{"db/sync":{"UserAccount":[{"id":"123","nickname":"new"}]}}');
SELECT xt_neon.publish('organisation:123',
  '{"db/remove":{"UserAccount":["123"]}}');
```

The publishing function serializes sequence allocation per topic until commit. For transactions publishing multiple topics, acquire/publish them in a consistent topic order to avoid deadlocks. Topic/event names and payload table names are application-defined and must match the cache schema. Events are idempotent cache mutations, not arbitrary executable messages.

A new subscription starts at the current head; initialize the cache after subscribing so writes during the initial pull are replayed. Cursor-based reconnects replay retained rows. `prune(topic, through)` atomically advances the retention floor and deletes older rows. Retention length is application-owned.

`subscribe-db(impl, conn-id, topics)` merges topics; unsubscribe removes requested topics. A connection has one poll loop with 25-second bounded waits and bounded retry backoff. State is under `impl.state.connections[conn-id]`, including `status`, `cursor`, and `task`. Replay sequences remain decimal strings to preserve bigint precision. Terminal authentication/authorization errors stop polling. Late responses from replaced connections are ignored.

On `resync_required`, a configured `resync(impl, topics)` must replace stale cache state, including deletions, and return a promise resolving only after replacement. The client establishes a new baseline before invoking this callback and resumes polling afterward. Without a callback the connection stops with that status. Bridge restarts invalidate in-memory sessions and cursor signatures; reauthenticate and reload the cache. The bridge is a single-process service; use sticky routing when deploying multiple instances.

HTTP endpoints are POST `/auth/sign-up`, `/auth/sign-in`, `/auth/session`, `/auth/refresh`, `/auth/sign-out`, `/events/subscribe`, `/events/poll`, and `/events/unsubscribe`. Native clients receive opaque session credentials; browser sessions use HttpOnly SameSite cookies. Session restoration lasts within the bridge's configured one-hour session lifetime. Upstream cookies remain server-side.

## Tests

Run `npm test` here for bridge and official Auth SDK HTTP tests. `test/live.test.mjs` skips unless its listed `NEON_*` variables are supplied; use a disposable branch and an authorized test topic. It performs reads, an explicitly configured RPC, and a test event publication. No live test is claimed by the fixture tests.

From the foundation directory:

```sh
lein test :only xt.db.system.impl-neon-test
node src-js/neon-bridge/test/fixture.mjs
# In another terminal:
NEON_FIXTURE_PORT=19081 lein test :only xt.db.system.impl-neon-http-test
lein seedgen benchadd '[xt.db.system.impl-neon-test xt.db.system.impl-neon-http-test]' :lang python :write true
NEON_FIXTURE_PORT=19081 lein test :only xtbench.python.db.system.impl-neon-test xtbench.python.db.system.impl-neon-http-test
```

Repeat generation/tests for `ruby`, `dart`, and `lua.nginx` with their runtime dependencies installed. Lua HTTP requires OpenResty/nginx; Ruby promises require `concurrent-ruby`. Generated bench files are build artifacts. `test/events.sql` exercises publication and retention inside a transaction that rolls back; run it using psql from this directory against a test database.

SDK references: https://neon.com/docs/realtime/sdks/typescript and https://neon.com/docs/auth/quick-start/react.
