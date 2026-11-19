# app.nextjs devtools

This is a devtools-only copy of
`statsui.basic.layout.common.frame-devtool`, renamed to `app.nextjs.devtools`.
The original StatsTrade sources remain in place. Application layouts, navigation,
profile screens, domain action groups, schemas and database RPCs are not copied.

The copied panel retains the account picker and account actions, theme/toast
controls, store/UI/context/query inspection, injected debug content, SharedWorker
inspection and viewport information. The small popover/button helpers are local
to the tools; they do not install a page layout or a theme provider.

## Source modules

- `app.nextjs.devtools`: copied panel/provider and injection hooks.
- `app.nextjs.devtools.runtime`: bindings to the host's existing Supabase client
  and supplied account actions; bounded, credential-redacted inspection snapshots.
- `app.nextjs.devtools.ui`: only the UI primitives the panel needs.
- `app.nextjs.build`: standalone package publication through `app.manage`.

Provenance is recorded in `resources/app.nextjs/origin.json`. The broad `statsui`
copy was removed; no `basic/layout`, `edge`, `group`, branding or auth screens are
included in this package.

## Use from Clojure/XTalk

Require the new namespace in the host client module:

```clojure
:require [[app.nextjs.devtools :as devtools]]
```

Mount `devtools/FrameDevtoolProvider` around the panel and any components using
its injection hooks. Pass the application's existing Supabase client as `client`.
The host keeps its own Next.js layout, Tamagui configuration, authentication
provider and data access rules.

Provider options:

| Option | Meaning |
| --- | --- |
| `client` | Existing Supabase client with `auth.getSession`, `onAuthStateChange`, `signInWithPassword` and `signOut` |
| `accounts` | Explicit test accounts with `id`/`auth`, `handle`, `email`, and optional development password |
| `createUser`, `deleteUser` | Optional server-backed callbacks; unavailable actions are disabled |
| `store`, `ui`, `context`, `queries` | Actual application state to inspect |
| `theme` | Host theme controller `{current, setCurrent}` |
| `toast` | Host notification controller with `show(title, options)` |
| `sharedWorker` | Existing worker resource; also register it through `useDevtoolSharedWorker` |
| `actions` | Extra wrench actions `{id, label, run}` supplied by the host |

Render `devtools/FrameDevtoolPanel` inside the provider. It is hidden in production.
Use `useDevtoolInjection` to register an app-specific debug view, and
`useDevtoolSharedWorker` to register/clear the current worker resource.

The module does not create an auth client, provision privileged users, embed test
credentials, assume a database schema, or install an authentication/layout stack.
Only the host should supply local development accounts and server callbacks.
Inspection snapshots redact tokens, passwords, keys and cookies and handle cycles.

## Generate and check

Run from the Foundation root:

```sh
lein run -m app.manage app.nextjs.build/PROJECT generate --write
lein run -m app.manage app.nextjs.build/PROJECT check
lein test :only app.nextjs.devtools-test
```

The output is `target/app-nextjs`, a local `@app/nextjs` package with the tool
module and its generated XTalk dependencies. UI is emitted as JavaScript using
`React.createElement`, so `.mjs` files do not require a JSX parsing step. The host
provides React, React DOM, Tamagui and `@tamagui/lucide-icons`; Supabase is supplied
as an existing client, not bundled or configured by the tools.

Dupasupa now mounts this toolkit using its existing Supabase client. Its
`app.manage` UI at http://127.0.0.1:8130 exposes Campaigns and Profile under
**Previews**; both contain the development panel. `dupasupa.auth` supplies client,
accounts and live inspection bindings while `dupasupa.auth-context` keeps the
application's auth hooks independent of tool rendering. Next.js aliases React
Native/SVG imports to their web implementations for the copied Tamagui controls.
FinFolk has not been switched to this panel.

**App Workbench** is the suggested name for the combined workflow: `app.manage`
for generation, checks and previews, plus `app.nextjs.devtools` for the running
application. The namespaces remain unchanged.
