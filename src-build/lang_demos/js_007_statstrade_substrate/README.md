# Topic editor over xt.substrate

A small, runnable slice of the statslink / statsui / [iberia.hook](https://github.com/statstrade-dev/statstrade-ui/blob/main/src/iberia/hook.clj)
pattern, implemented inside foundation-base. It lists two topics, selects a detail
record, and edits its title using the existing Melbourne components.

The `TOPIC` declaration in `model.clj` keeps `:actions`, `:views`, and `:entries`.
An action applies `:input` to its arguments and context, calls the function
transformed by `:wrap`, then applies `:output` to the resolved value. Missing
transformations default to identity. Creating actions does not execute them.
Entry field names are strings because the declaration is emitted as JavaScript.

| Original responsibility | Example implementation |
| --- | --- |
| statslink request boundary | `link/call`: `substrate/request` with node, space, and transport context |
| statslink action transformations | `link/action-fn` and `make-actions` |
| iberia list/detail hooks | `hook/useListView` and `useSingleView`, backed by `js.react.ext-model` |
| raw event subscriptions | `hook/listenRawEvents`, with space/signal keys, optional predicates, and effect cleanup |
| statsui entries/actions/views | `main/TopicEditor`, rendering declared fields and submitting `actions.modify` |

`fixture.clj` connects client and server nodes with the substrate memory transport.
The server owns the records and the `topic/list`, `topic/detail`, and
`topic/modify` routes. A successful edit publishes `topic/changed`; the list
refreshes, and the detail refreshes only when the event matches its current id.
One substrate trigger fans out to independent keyed listeners. Closing the
fixture unsubscribes and detaches its transports.

The example reuses the already ported UI stack rather than adding a UI port.
It requires no database or external service. Reloading resets both records.

## Run

Use the repository's Java 21 / Leiningen setup and Node 24:

```bash
lein run -m lang-demos.js-007-statstrade-substrate.build
cd .build/demo/js-007-statstrade-substrate
npm install
npm run dev
```

Open http://localhost:28012. Select the second topic, edit its title, and save.
The new title appears in both the list and detail, with revision 2. A blank title
shows `Title is required` and leaves the record unchanged.

The build emits a single app with React Native imports mapped to
`react-native-web`. Generated output stays under `.build`.

## Verify

From the repository root:

```bash
lein test :only lang-demos.js-007-statstrade-substrate.example-test
lein test :only js.react.ext-model-test
```

From the emitted app directory, `npm test` builds the production bundle and
mounts it in jsdom with real React, Melbourne, and react-native-web. It checks
initial reads, switching the selected record, submitting an edit, event-driven
list/detail updates, and the rejected blank-title flow.

The accompanying `ext-model` fixes align subscriptions with base-model's
`model.*` events and prevent refresh effects from returning a Promise as React
cleanup. Focused tests cover those regressions.

This slice has no pagination, cached list reconciliation, remote database,
authentication, or generic form generation. The adapters stay in the example
until another concrete use case warrants a shared API.
