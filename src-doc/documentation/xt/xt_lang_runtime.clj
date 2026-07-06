(ns documentation.xt-lang-runtime
  (:use code.test))

[[:hero {:title "xt.lang.runtime"
         :subtitle "Promise, protocol, resource, notify, and repl layers."
         :lead "Runtime-oriented `xt.lang` namespaces define the conventions generated programs use for async, protocols, resources, notifications, and REPL-style interaction."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Cross-target code needs more than syntax parity. It also needs shared expectations around async return values, resource loading, protocol dispatch, and debugging or notification flows."

[[:chapter {:title "Internal usage" :link "internal"}]]

"The runtime common layer is used by network, database, and substrate modules. It is also covered by xtbench parity tests so target implementations stay aligned."

[[:chapter {:title "API" :link "api"}]]

