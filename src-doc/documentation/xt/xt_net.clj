(ns documentation.xt-net
  (:use code.test))

[[:hero {:title "xt.net"
         :subtitle "HTTP, websocket, SQL, Redis, and Supabase helpers."
         :lead "`xt.net` contains portable networking adapters used by generated programs and substrate/database systems."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Generated xtalk programs need consistent wrappers around fetch, websocket transports, Phoenix websocket conventions, SQL connections, Redis connections, and Supabase addons."

[[:chapter {:title "Internal usage" :link "internal"}]]

"The networking layer is used by xt.db, xt.substrate transports, and Supabase-related examples. Tests under `test-lang/xt/net` provide runnable behavior examples."

[[:chapter {:title "API" :link "api"}]]

