(ns documentation.xt-db-walkthrough
  (:use code.test))

[[:hero {:title "xt.db walkthrough"
         :subtitle "Binding framework and substrate walkthrough."
         :lead "The database walkthrough ties together schema text builders, backend systems, node adapters, and substrate transports into an application-facing data layer."}]]

[[:chapter {:title "Source material" :link "source"}]]

"The implementation pass should fold in the narrative from `docs/xt_db_binding_framework_readme.md` and `docs/xt_db_substrate_walkthrough.md`. Keep the walkthrough concise here, and link to tests for full executable scenarios."

[[:chapter {:title "Example flow" :link "flow"}]]

"A typical flow starts with schema/query text builders, chooses a system backend, exposes a kernel through node/proxy layers, and connects clients through memory, websocket, worker, or Supabase transports."

[[:chapter {:title "Internal examples" :link "examples"}]]

"Use `test-lang/xt/db/poc/n00_basic_test.clj`, `s01_worker_min_test.clj`, `s04_sharedworker_test.clj`, and `s06_supabase_auth_test.clj` as the first curated walkthrough examples."
