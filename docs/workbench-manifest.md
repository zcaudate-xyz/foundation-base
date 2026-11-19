# Application manifests

`app.manage.manifest/with-workbench` installs the `:application-manifest` target and
the standard Pages & models provider. Both Dupasupa and FinFolk use this entry point.

The configuration supplies `:application` (a symbol naming a zero-argument provider),
optional `:operations` (a provider of operation specifications), `:sources` and
`:depends`. The application provider may return a keyed registry, as FinFolk does,
or a sequence of page declarations. Legacy `:pages` and generated configuration
cannot be installed together.

Generate with the existing app.manage CLI, selecting `application-manifest`, or
include it in a normal project generation. Output is canonical EDN and JSON under
`generated/workbench/manifest.*`, with manifest version 1. Map keys and page order
are stable; execution order vectors are preserved. No timestamps or output-root
paths are added. Existing page extension fields are preserved.

Compilation rejects duplicate IDs/routes, unresolved operation specifications,
mutations declared as read models and unknown ordered models. Generated-file
parity uses the normal project checks. Operation signatures come from each
application's existing operation registry; the compiler does not infer routes or
business expectations from SQL.

## Shared execution

`app.manage.page-runtime` owns declaration lookup, transport request binding,
defaults/input binding, ordered page reads and scenario references. Operation
adapters provide actual execution; absent adapters remain blocked.

Dupasupa's `dupasupa.pages` is now the application declaration source. Production
campaign/profile request bindings, the campaign group identifier and group metadata,
the generated manifest and the verifier all consume it. `dupasupa.bindings` publishes
those definitions with the shared binder. The verifier registers operation adapters
for the actual campaign model and verified-session Next handlers, rather than
switching on page names. Existing scenario result shapes and unsupported profile
mutation behavior are preserved. FinFolk retains its existing application adapter;
the shared runtime is available in both Foundation copies.

Add a page declaration using an existing operation and its workbench entry and
shared execution become available without changing UI sections or verifier page
branches. New backend operations still require an implementation and explicit
adapter registration; business assertions remain authored in saved EDN/code.test.
