# app.manage

`app.manage` is an independent application-management section of Foundation,
published as `xyz.zcaudate/app.manage`. It does not depend on `code.manage`.
It extracts StatsTrade's page-requirement publication into a reusable Clojure API.

The core manages dependency planning, source impact, reproducible artifact
generation, builds, verification, and source watching. Optional adapters reuse
Foundation's PostgreSQL DSL publication and `std.make` module compilers.
Dupasupa demonstrates the full build/watch path. StatsTrade currently adopts the
contract-publication adapter only.

Generation never installs SQL or deploys applications. Explicit integration checks
may install a DSL fixture into their own disposable local database. The APIs do not yet scaffold
complete features or lint UI composition.

## Project declaration

A project is ordinary Clojure data. Targets declare source ownership, dependency
edges, exclusive output paths, and a renderer. Renderers receive the project,
canonical root, target ID, and the artifact maps produced by earlier targets.
They return `{relative-path text}` and should be deterministic and free of writes.
The site-map reader reads page requirements as data with reader evaluation
disabled. Compiler targets subsequently load their configured trusted Clojure
DSL namespaces. Emitted JavaScript runs only through declared verification
commands or when the application is launched.

```clojure
(require '[app.manage :as manage])

(def PROJECT
  {:id 'my-app
   :root "."
   :default-targets [:contracts]
   :targets
   {:contracts
    {:sources ["src/my_app"]
     :outputs ["generated/contracts.edn"]
     :render (fn [_] {"generated/contracts.edn" "{:version 1}\n"})}}})

(manage/plan PROJECT)
(manage/affected PROJECT ["src/my_app/model.clj"])
(manage/analyse PROJECT)
(manage/generate PROJECT)                  ; proposal, no writes
(manage/generate PROJECT {:write true})
(manage/check PROJECT)                     ; render again and compare with disk
(manage/build PROJECT)                     ; write and run relevant declared checks
(manage/test-project PROJECT)              ; build test prerequisites, then run checks
```

`plan`, `render`, and `check` accept selected target IDs as a second argument.
`generate` takes `{:targets [...] :write true}`. Dependencies are included once,
before their dependants. Unknown targets, cycles, overlapping output ownership,
unsafe paths, and undeclared renderer outputs fail before generation writes.

`check` reports `:missing`, `:changed`, and `:unexpected` paths. Its expected
content comes from fresh renderers, not a stored checksum baseline. Output paths
must be exclusively generated: extra files are considered orphans. Generation
reports orphans but never deletes them. Writes are not a filesystem transaction.

`analyse` renders in memory and returns a structured finding if configuration,
page resolution, or publication fails. A successful analysis does not certify
database permissions or application behavior. It currently reports the first
failure; it is not a comprehensive source linter.

## Page publication

`app.manage.site-map` supports the existing StatsTrade form:

```clojure
(def SITE-MAP-REQUIREMENTS
  {:tables [seed/Campaign]
   :rpcs [seed/ping]
   :groups [groups/CAMPAIGNS]})
```

A registry supplies `:seed-namespaces`, `:group-namespaces`, `:resolve`, `:rpc?`,
`:get-rpc`, `:rpc-spec`, `:schema`, and `:table-schema`. References outside the
configured namespaces and wrong-kind declarations fail. Groups contribute RPCs
from their models and actions. Tables are explicitly declared; group dataviews
do not implicitly publish tables. Forward table dependencies are included.

`site-map/target` constructs a management target from a zero-argument registry
supplier, a site config (`:site`, `:source-root`, `:output-root`, optional
`:public-root`, `:depends`, `:sources`), and a JSON encoder. It publishes the same
four files consumed by `xt.db.node.site-map`: manifest, schema, lookup, and RPC.
`site-map/build-sections` constructs deferred JSON sections for existing
`std.make` builds. Project-specific JSON encoders preserve legacy formatting.

## CLI

```sh
lein run -m app.manage my-app.build/PROJECT plan
lein run -m app.manage my-app.build/PROJECT analyse
lein run -m app.manage my-app.build/PROJECT generate
lein run -m app.manage my-app.build/PROJECT generate --write
lein run -m app.manage my-app.build/PROJECT check
lein run -m app.manage my-app.build/PROJECT build
lein run -m app.manage my-app.build/PROJECT test
lein run -m app.manage my-app.build/PROJECT watch
```

Run from the consumer's project root. `check` and `analyse` exit nonzero on
failure. Loading a trusted project var may load its DSL registry; management
does not sandbox project-supplied Clojure functions.

## Sample and StatsTrade

[Dupasupa](../examples/dupasupa/README.md) declares real PostgreSQL tables/RPCs,
Substrate model libraries, contract maps, a worker fixture, and a generated
Next.js/React application with a thin native RPC adapter. Its campaign page saves
to an owned local Supabase PostgreSQL database. Its Clojure integration facts
exercise the actual model and React form against PostgreSQL.

StatsTrade's `statsbuild.project.application/PROJECT` supplies its real registry
and all three site roots. Its existing `statsbuild.project.site-map` entry points
delegate to this shared engine, retaining existing callers and `std.make` builds.
The StatsTrade adapter reads the existing typed publication and binds the
authored application schema; it does not regenerate the database publication.
Regenerate SQL/EDN with the existing database publisher after changing DB source.

```sh
# From statstrade-v2:
lein run -m statsbuild.project.application analyse
lein run -m statsbuild.project.application generate
lein run -m statsbuild.project.application check
lein test :only statsbuild.project.site-map-test statsbuild.project.application-test

# From Foundation:
lein test :only app.manage-test app.manage.site-map-test
```

## Build and watch adapters

`app.manage.make/target` accepts `:sources`, `:depends`, `:outputs`, `:load`
namespaces, and `:sections` containing ordinary `std.make` compilation entries.
It captures compiler output in mock mode, including module graphs, then lets
`app.manage` validate and write it. Copy/resource sections are not supported by
this text-artifact adapter; binary assets need a separate adapter.

`app.manage.postgres/target` takes `:application`, `:scaffold`, `:sources`,
`:output-root`, and optional `:emit`. It publishes `schema.sql` and portable
`schema.edn` from the authored application. `intern-seed!` exposes database-owned
seed vars and `registry` supplies the site-map resolver. Projects declare their
seed namespace, group namespaces, RPC namespace, and RPC SQL schema explicitly.

Verification is project data:

```clojure
:checks {:integration
         (integration/check {:targets [:integration]
                             :namespaces ['my-app.integration-test]})}
```

`build` writes rendered targets and runs checks whose prerequisites were built.
`test` builds all declared check prerequisites and refuses an empty suite.
Checks use argument vectors rather than shell interpolation; stdout, stderr,
exit code, and timeout are reported. A custom `:run` function may instead return
`{:status :ok}` or `{:status :error ...}`. A test failure is a failed build; the
already generated files remain available for diagnosis.

For watching, declare `:source-paths` (existing project source directories) and
`:project-sources` (files whose changes affect the whole project). Use
`app.manage.watch/start!` with the qualified project-var symbol (or a zero-argument
supplier of the latest project map), and close its returned handle with `stop!`.
Symbols are resolved again after source reloads. Debounced filesystem batches are serialized. Project-local namespace
`:require` and `l/script :require` dependencies determine reload order; XTalk registrations and Clojure
mappings are cleared before reloading. Target dependencies determine which
artifacts rebuild. Syntax/render failures retain the previous outputs, and a
subsequent fixed source change retries normally. Restart watching if changing
which source directories are watched. Stop is idempotent and closes its watcher
and executor.


## Multi-runtime integration checks

`app.manage.integration` captures the Statslink testing pattern: one `code.test`
namespace declares PostgreSQL and JavaScript runtimes, calls real typed functions
and actual Substrate models, and asserts both persisted data and client state.
Assertions live in Clojure DSL facts, with unique fact IDs. There is no separate
handwritten JavaScript test suite. This follows the original
[shared scaffold](https://github.com/statstrade-dev/statstrade-core/blob/master/src/statsinfra/test/sample_link/scaffold_pilot.clj)
and [client sync tests](https://github.com/statstrade-dev/statstrade-core/blob/master/test/statslink/full/worker_main_test.clj).

```clojure
:checks {:integration
         (integration/check {:targets [:integration]
                             :namespaces ['my-app.integration-test]})}
```

The `:integration` target can have no outputs; its dependencies declare what must
be generated before the facts run. `app.manage test` generates those dependencies
and runs each declared namespace. Any failed assertion, thrown error, namespace
load/setup error, skipped check, or empty suite fails the check and CLI exit code.
`generate`, `analyse`, and `check` remain free of database connections.

Use `integration/fixture` for ordered resources. Each resource has `:id`,
`:start (fn [prior-context] ...)`, and `:stop (fn [context value] ...)`.
`start!` unwinds partially started resources after failure; `stop!` runs every
cleanup in reverse order even after an error, and is idempotent. Put these calls
in `fact:global` setup/teardown. Runtime resources should stop before the database.
Use `:test-mode true` on test runtime declarations so loading facts does not
connect before their fixture starts. Run these runtime suites sequentially.

`app.manage.integration.postgres/database` accepts a loopback administrator URL
ending in `/postgres`, without URL parameters. It generates a unique
`app_test_<label>_<uuid>` database, creates it from `template0`, and drops only
that owned database. `execute!` supports application-specific extension/schema
bootstrap inside that database. Existing application databases are never reset.
A running local Supabase server and its PostgreSQL JDBC driver are prerequisites;
the fixture does not start or stop the user's Docker services.

`app.manage.integration.client` is test-only Node interop: a parameterized RPC
caller restricted to declared names, argument counts and primitive SQL types.
Pass signatures from the application's native DSL registry. It contains no
domain queries. Do not emit it in browser bundles or expose administrator
credentials there. Node must resolve the `pg` package from the runtime's working
directory; `http-kit` supplies the basic runtime's notification channel.
`integration/await-js` waits for promises and turns unexpected rejections or
notification timeouts into failing facts. Intentional rejection tests must return
an explicit rejection marker and assert the relevant error fields.

Dupasupa demonstrates database reads/writes/rejection, actual model output and
HTML escaping, generated worker messages, React form submission and reload, and disposal. FinFolk's
`finfolkbuild.project.application/PROJECT` uses the same fixture and native
publication with its existing account backbone and logbook functions. These are
runtime/model integration tests, not HTTP authentication/RLS or browser pixel
tests. New workflows should extend their application facts using the same scaffold.

## Local management UI

`app.manage.server` provides a shared UI for a project declaration:

```sh
lein run -m app.manage.server myapp.build/PROJECT --port 8130
# Dupasupa: make manage
```

The server compiles `app.manage.ui` and `app.manage.ui-model` from Clojure/XTalk
into memory. The model uses Substrate and accepts an injected transport so it can
run without DOM or HTTP. CSS ships with the app.manage package.
The graph, target inspector, source/output viewers, checks and run history read the
actual declaration and local files. Build/check/test/watch actions call the same
management functions as the CLI. Startup only indexes files; it does not build or
run integration checks. Operations are serialized with watcher rebuilds, and progress
comes from `app.manage/*on-event*`. History is per server session.

The service binds to `127.0.0.1`, uses a per-session action token, and restricts
file/preview reads to declared target sources/outputs. It is a local development tool.
`session!` exposes its controller without opening a socket for headless testing.

## Page/model verification

`app.manage.verify` shares declarative assertions between the manager UI, CLI and
project `code.test` suites. A project declares `:pages` (definition provider),
`:page-verifier` (request executor), `:verification-targets` (generation targets),
and optionally `:page-test-fixture` (a function that runs a thunk with fresh local
resources and always cleans them up).

Requests use a project-defined page/kind/member/inputs/payload contract and
`{:expect {:equals value}}`, `{:expect {:contains subset}}`, or
`{:expect {:error subset}}`. Results without assertions are `:executed`.
`nil` and a missing result are distinct. Ordered `:steps` require unique string
IDs; projects implement their own output references. No executable assertions
are evaluated. Unsupported execution should return `{:blocked reason}`.

Saved cases are versioned `.app-manage/page-tests.edn`; save operations reject
stale revisions. Cached evidence under `.cache/app.manage` includes request,
expected/actual results and source/artifact/case fingerprints. A source or case
edit marks it stale. Wrap an emitted runtime target with
`(verify/with-receipt target "generated/verification/receipt.edn")` and declare
`:verification-receipt` on the project to block execution of stale or edited
artifacts. Receipt files are generated, disposable build metadata.

The UI's Pages & models view supports loading, editing, executing, saving and
replaying cases, inspecting per-step status and full runtime diagnostics.
`verify-definitions` checks operation presence and forbids write operations as
read models. This is structural coverage, distinct from live fixture coverage.
The shared actions are `verify-page`, `verify-definitions`, `save-page-case`, and
`run-page-cases` through the existing serialized action API.

```sh
lein run -m app.manage.verify myapp.build/PROJECT run
```

The CLI replays saved cases through the same executor and exits nonzero on
failure. FinFolk demonstrates an emitted XTalk worker delegating page/model SDK
adapters to the actual Substrate backbone, with native PostgreSQL fixtures and
an ordered ownership scenario.

## Configurable HTTP API

`app.manage.server` serves an opt-in JSON API at `/api/v1`, using its existing
session token, serialized execution queue, run history and slice evidence. No
project-specific server wrapper is needed. Existing UI endpoints remain available.

Declare the API on the project map:

```clojure
:http-api
{:enabled true
 :operations ["analyse" "build"]
 :actions {:verify-ui {:operation "build" :slice "ui" :stage "browser" :tests true}}
 :artifacts {:report {:path ".cache/ui-report.json" :content-type "application/json"}
             :screenshots {:directory ".cache/ui" :filename-pattern "failure-[0-9]+[.]png"
                           :content-type "image/png"}}}
```

Omit `:http-api` or set `:enabled false` to disable the versioned API. `:operations`
allows generic submissions through `POST /api/v1/runs`; omit it to expose only the
fixed named actions in `:actions`. These action maps use the same operation fields
as `/api/actions`, including target or slice/stage selection. They cannot execute
arbitrary shell commands supplied over HTTP. Project declarations remain trusted.

Read `/api/session` for the per-process token, then include it as the
`X-App-Manage-Token` header on every POST. The server binds to loopback and does not
enable cross-origin browser access. The new configuration applies to `/api/v1`;
it does not restrict the existing UI `/api/actions` endpoint.

- `GET /api/v1`: discovery.
- `GET /api/v1/openapi.json`: OpenAPI 3 document derived from project configuration.
- `GET /api/v1/checks`: check definitions, named actions, in-memory check status and persisted slice evidence.
- `POST /api/v1/actions/{name}/runs`: submit a fixed configured action; request body is unused.
- `POST /api/v1/runs`: submit an allowed operation as a JSON object.
- `GET /api/v1/runs`: recent run history from the shared controller.
- `GET /api/v1/runs/{id}`: poll a run and read its result and check output.
- `GET /api/v1/artifacts/{name}`: read a configured file.
- `GET /api/v1/artifacts/{name}/{filename}`: read a file from a configured directory matching its filename pattern.

POST returns HTTP 202 and `{id, status: "queued"}`. Poll until `status` is `ok` or
`error`; a failed check is a completed run with `status: "error"`, not an HTTP
transport error. Run IDs are process-local and recent history is bounded; clients
should poll promptly and retain completed results. Artifact endpoints serve the
latest files, so clients must inspect report timestamps rather than assuming the
file belongs to a particular run. Artifact paths are confined to the project root,
including symlink resolution. Unknown and disabled endpoints return 404; missing
or invalid POST tokens return 403.

### Preview links

The classic management frontend gives every declared preview its own hash route:
`#/previews/<encoded-preview-id>`. Selecting a preview updates the URL; opening
that link or reloading restores the selected preview. Browser Back and Forward
restore earlier selections, including navigation between previews and other
workbench views. The **Preview link** beside the preview controls provides a
copyable link to the workbench selection. The application URL in the frame is
unchanged, and periodic status polling preserves the live iframe.

For example, FinFolk's native event preview is `#/previews/native-events` and its
account preview is `#/previews/native-account`. Existing `?view=previews` links
remain supported; a hash route takes precedence.
