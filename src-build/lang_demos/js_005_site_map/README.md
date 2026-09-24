# JS-005: SharedWorker site map

This demo shows the page connecting to a URL-backed SharedWorker and asking it
to initialise the database kernel. The page passes empty schema and lookup
objects; the worker fetches `manifest.json`, then the manifest's schema, lookup,
and RPC JSON files.

The worker exposes `@demo/site-map-summary` as a small diagnostic action. The
page calls it after `@xt.db/kernel-init` completes so the response confirms
which tables and RPC ids the worker loaded. The sample uses in-memory database
adapters, so `ping` is included to demonstrate the RPC registry format but is
not executed against a database.

Build and serve from the Foundation project root:

```sh
lein run -m lang-demos.js-005-site-map.build
cd .build/demo/js-005-site-map
make start
```

Open <http://localhost:8080>, then click **Connect and initialise**. In the
Network panel, the four `/worker/site-map/*.json` requests are made by the
SharedWorker, not by the page.
