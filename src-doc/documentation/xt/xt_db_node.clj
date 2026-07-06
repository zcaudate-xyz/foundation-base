(ns documentation.xt-db-node
  (:use code.test))

[[:hero {:title "xt.db.node"
         :subtitle "Client, kernel, proxy, and runtime node layers."
         :lead "`xt.db.node` packages database behavior for node-like runtimes: browser clients, Supabase clients, kernels, proxies, proxy utilities, and runtime adapters."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Client/server database flows need message boundaries. The node layer separates client calls, kernel execution, proxying, and runtime setup so the same database model can run in workers, browsers, and service contexts."

[[:chapter {:title "Internal usage" :link "internal"}]]

"Tests under `test-lang/xt/db/node` cover browser clients, base clients, Supabase clients, kernels, proxies, and runtime behavior. POC tests in `test-lang/xt/db/poc` show worker and shared-worker usage."

[[:chapter {:title "API" :link "api"}]]

