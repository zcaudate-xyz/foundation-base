(ns documentation.xt-db
  (:use code.test))

[[:hero {:title "xt.db"
         :subtitle "Database text, system, node, and runtime layers."
         :lead "`xt.db` is a portable database stack: text builders generate SQL and PGREST shapes, system layers run against memory/postgres/sqlite/supabase, and node layers expose clients, kernels, and proxies."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Applications need a single schema and query model across local memory, SQL databases, Supabase, web workers, and generated clients. `xt.db` splits that problem into text generation, runtime systems, and node-facing adapters."

[[:chapter {:title "How to read the layers" :link "layers"}]]

"Start with `xt.db.text` for schema/query builders, then `xt.db.system` for backend implementations, then `xt.db.node` for client/kernel/proxy integration. POC tests under `test-lang/xt/db/poc` show how those layers combine."

[[:chapter {:title "Pages" :link "pages"}]]

[[:card-grid {:title "Database documentation"
              :items [{:meta "Text" :title "xt.db.text" :text "Schema, graph, tree, SQL, and PGREST builders." :href "xt-db-text.html"}
                      {:meta "Systems" :title "xt.db.system" :text "Memory, postgres, sqlite, supabase, realtime, and websocket systems." :href "xt-db-system.html"}
                      {:meta "Node" :title "xt.db.node" :text "Client, kernel, proxy, and runtime layers." :href "xt-db-node.html"}
                      {:meta "Walkthrough" :title "xt.db walkthrough" :text "Binding framework and substrate walkthrough material." :href "xt-db-walkthrough.html"}]}]]
