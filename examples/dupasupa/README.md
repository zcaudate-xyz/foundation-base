# Dupasupa

Dupasupa is a Next.js/React application authored in Clojure/XTalk and managed by
Foundation's `app.manage`. Its campaign page uses the actual Substrate model;
a thin Next route adapter calls PostgreSQL DSL functions for reads and saves.

## Run

Requirements: JDK 21+, Leiningen, Node 22+, npm dependencies, and a running local
Supabase PostgreSQL server at `127.0.0.1:55122` and its Auth API at
`127.0.0.1:55121` (the Foundation `local-min` scaffold).

```sh
cd examples/dupasupa
npm ci
make serve
```

Open [Dupasupa](http://127.0.0.1:8000/web-main). `make serve` generates the authored
sources, builds Next.js, runs the DSL integration checks, and starts Next with a
fresh owned local database and two seeded accounts and campaigns. Stop it with
Ctrl-C.

Edit the title and click Save. The campaign heading displays the committed title,
and the form shows “Saving…” followed by “Saved.” Validation failures preserve
the draft and display the PostgreSQL error. Saved titles survive page reloads.
The development database lasts for the server session and is dropped on shutdown;
a new server session starts with a fresh campaign.

`APP_TEST_POSTGRES_URL` can select another local Supabase administrator URL ending
in `/postgres`. Only loopback destinations are accepted. No existing application
database is reset. PostgreSQL credentials stay in the Next server environment.

## Management previews

Run `make manage`, open `http://127.0.0.1:8130`, and select **Previews**.
The Campaigns preview embeds the Next application running on port 8000;
start it separately with `make serve` if needed. The Public page preview serves
the generated static page from the management server. Desktop/Mobile controls,
Reload and Open in new tab work for either preview. Definitions live in the
`:previews` vector of `dupasupa.build/PROJECT`.

## Source ownership

| Source | Responsibility |
| --- | --- |
| `src/dupasupa/build.clj` | Dependency graph, generated Next routes/layout, checks |
| `src/dupasupa/pages/main/react_campaigns.clj` | React components, controlled input and save states |
| `src/dupasupa/model.clj` | Actual Substrate model lifecycle and refresh after save |
| `src/dupasupa/server.clj` | Verified-session Next adapter to native functions |
| `src/dupasupa/pages/main/profile.clj` | Profile editor and save feedback |
| `src/dupasupa/auth.clj` | Supabase session provider and shared auth devtools |
| `src/dupasupa/auth_fixture.clj` | Owned local Auth users for development and tests |
| `src/dupasupa/db` | Typed PostgreSQL tables, validation, reads and writes |
| `src/dupasupa/dev.clj` | Owned development database and Next process lifecycle |
| `test/dupasupa/integration_test.clj` | Clojure facts over the database, model, React form and worker fixture |

`generated/next` is the running Next application. Its JavaScript, routes and
layout are generated from the Clojure build declaration; edit authored sources
and regenerate. The earlier static page/worker assets remain integration fixtures;
they are not the running campaign application's persistence layer.

```sh
make plan                       # dependency order
make generate                   # regenerate without connecting to PostgreSQL
make check                      # generated-file parity
make build                      # generate, Next production build and DSL checks
make test                       # same declared checks and prerequisites
lein test :only dupasupa.build-test
make watch                      # regenerate/recheck authored source changes
```

All new assertions live in `code.test` facts, including the React form workflow
against the real PostgreSQL fixture and a remount proving saved data is reloaded.
There is no separate JavaScript assertion suite. The Next production build is
also an `app.manage` check. Tests create real temporary Supabase Auth users,
exercise the generated Next handlers, and mount the actual auth provider/profile
components. Unauthenticated and forged-token requests fail; campaign/profile
writes use the verified user rather than caller-supplied IDs. Production config
excludes all development account credentials. Production deployment remains
outside this example.

See [app.manage](../../docs/app-manage.md) for the reusable fixture lifecycle,
strict test results, publication adapters and management APIs.


## Profile and auth devtools

Open [Profile](http://127.0.0.1:8000/profile), open **Test accounts** at the
bottom right, and choose **Sign in as alice** or **Sign in as bob**. Edit the
display name, handle and bio, then save. Switching accounts replaces the private
page and campaign model; signing out clears private UI. Session refresh and
persistence are handled by Supabase Auth, and the server verifies every token
with `getUser` before using its user ID.

The `app.nextjs.devtools` toolkit is a devtools-only copy of StatsTrade's
`statsui.basic.layout.common.frame-devtool`. Dupasupa mounts its full runtime
panel with the application's existing Supabase client; it also runs inside
`app.manage`'s embedded previews. Inspection receives live campaign model and
profile query state, with secrets redacted from snapshots.

The dev server owns temporary local Auth accounts and deletes only those users
on shutdown. Their credentials are supplied only to the development account
picker. No service-role key is sent to the browser. The profile schema and
validation live in the PostgreSQL DSL; React and Next routes are authored in
Clojure and generated through the project graph.

## Slice build state (initial implementation)

The manager's **Slices** view describes campaigns and profile across the stack.
`GET /api/slices` supplies the same evidence and work notes to agents. Notes are
revision-checked via `POST /api/slice-work` using the manager session action token.
Actions accept `slice` and optional `stage` to scope a build. Evidence is stored
under `.cache/app.manage`; shared work notes live under `.app-manage/work.edn`.

```sh
lein run -m app.manage.slices dupasupa.build/PROJECT status
lein run -m app.manage.slices dupasupa.build/PROJECT generate campaigns backbone
```

Current status is intentionally incomplete: min/dev stack profiles, generated RPC
bindings/types, profile Substrate wiring and typed screens remain to be implemented.
A generated file is not proof of a passing integration test. Changes invalidate
recorded evidence; work notes cannot mark a stage verified.

## App Workbench

Open http://127.0.0.1:8130, select **Previews**, then **Campaigns · App Workbench**
or **Profile · App Workbench**. The copied `app.nextjs.devtools` panel appears at
the bottom right of the application frame (scroll down to see the entire frame).
Use **Test accounts** to select Alice or Bob; the account button signs out.
**Inspect runtime** prints redacted session, live page state and query outputs to
the browser console. Campaigns exposes its actual Substrate handle; Profile exposes
its committed record and save status. SharedWorker reports disconnected until the
host registers one. Theme/toast and account provisioning remain disabled unless
host controllers are supplied.

The manager owns generation, checks and previews; the Next.js app owns its existing
Supabase client and session. Start them in separate terminals with `make manage`
and `make serve`. Devtools use disposable development accounts and are hidden in
production. Assertions remain in the Clojure DB/UI integration scaffold.

## Campaigns and Pages & models

Campaigns now supports creating campaigns, editing their titles, and refreshing
the list. Titles are trimmed and validated (1–120 characters) in PostgreSQL.
Creation uses the verified session owner; callers cannot supply a different owner.
Empty and failed reads are shown explicitly. Model refresh failures propagate to
the UI rather than appearing as an empty successful result.

Open http://127.0.0.1:8130 and select **Pages & models** for the same registry and
verification workbench used by FinFolk. Campaigns exposes its `rows` model plus
`create` and `save` actions; Profile exposes its authenticated read. **Try model**
prepares an editable request. **Check all definitions** checks read/write contracts.
Load **campaign-lifecycle** and choose **Run / verify**, or select **Run saved checks**.
Every run owns a fresh database and real local Supabase Auth users, then removes
those resources. It does not operate on the interactive application's campaign data.

The saved 12-step case exercises create, trim, update, page/model refresh, Alice/Bob
ownership, signed-out access, profile reads, and blank/null/oversized title rejection.
Expectations are versioned in `.app-manage/page-tests.edn`. Expand results for actual
outputs, model state and a credential-free transport trace. Edited runtime sources
or emitted files require regeneration; previous evidence is marked stale.
The page operation inspects the Substrate page model, while mounted React behavior
is covered by the Clojure DB/UI scaffold. Profile mutation remains unavailable in
the interactive workbench and is reported as blocked.

```sh
make generate
lein run -m app.manage.verify dupasupa.build/PROJECT run
make build
```

The `page-model-verification` check also mounts the emitted management UI and clicks
its saved-check controls through the real loopback HTTP action queue. Assertions
remain in `code.test` facts; no standalone JavaScript test suite was added.


The page registry now also drives production request bindings through
`dupasupa.bindings`. Its campaign group metadata and workbench manifest derive from
those same application declarations. Shared `app.manage.page-runtime` handles page
lookup, request/input bindings and ordered verification scenarios; the project
verifier registers supported operation adapters and owns only its local fixture
transport and model lifecycle. New pages reusing supported operations do not need
new workbench sections or verifier page branches.
