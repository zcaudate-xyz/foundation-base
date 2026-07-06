(ns documentation.xt-lang-runtime
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-promise :as promise]
             [xt.lang.common-resource :as rt]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

[[:hero {:title "xt.lang.runtime"
         :subtitle "Promise, protocol, resource, notify, and repl layers."
         :lead "Runtime-oriented `xt.lang` namespaces define the conventions generated programs use for async, protocols, resources, notifications, and REPL-style interaction."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Cross-target code needs more than syntax parity. It also needs shared expectations around async return values, resource loading, protocol dispatch, and debugging or notification flows."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Promise states"}]]

"`xt.lang.common-promise` wraps raw promises in a target-agnostic state map so code can inspect status before the value is ready."

(fact "construct resolved, rejected, and pending promise states"
  ^{:refer xt.lang.common-promise/make-resolve-state :added "4.0"}
  (!.js
    (var p (promise/make-resolve-state 7))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "value")])
  => ["xt.promise" "resolved" 7]

  ^{:refer xt.lang.common-promise/make-rejected-state :added "4.0"}
  (!.js
    (var p (promise/make-rejected-state "boom"))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "error")])
  => ["xt.promise" "rejected" "boom"]

  ^{:refer xt.lang.common-promise/make-pending-state :added "4.0"}
  (!.js
    (var p (promise/make-pending-state false))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "is_async")])
  => ["xt.promise" "pending" false])

[[:section {:title "Resource container"}]]

"`xt.lang.common-resource` gives every target a shared `xt` state container for config and spaces, similar to a global app context."

(fact "manage config and spaces in the xt container"
  ^{:refer xt.lang.common-resource/xt-ensure :added "4.0"}
  (!.js
    (rt/xt-purge)
    (rt/xt-ensure)
    (xt/x:get-key (rt/xt-current) "::"))
  => "xt"

  ^{:refer xt.lang.common-resource/xt-config-set :added "4.0"}
  (!.js
    (rt/xt-purge-config)
    (rt/xt-config-set "app.host" "127.0.0.1")
    (rt/xt-config-list))
  => ["app.host"]

  ^{:refer xt.lang.common-resource/xt-item-set :added "4.0"}
  (!.js
    (rt/xt-purge-spaces)
    (rt/xt-item-set "session" "user-1" {:name "root"})
    (rt/xt-item "session" "user-1"))
  => {"name" "root"})

[[:section {:title "End-to-end: a configured runtime"}]]

"A typical runtime boot loads config, stores session items, and prepares a pending promise for async work."

(fact "boot a minimal runtime"
  (!.js
    (rt/xt-purge)
    (rt/xt-config-set "api.url" "http://localhost:8080")
    (rt/xt-item-set "status" "ready" true)
    [(rt/xt-config "api.url")
     (rt/xt-item "status" "ready")
     (xt/x:get-key (promise/make-pending-state false) "status")])
  => ["http://localhost:8080" true "pending"])

[[:chapter {:title "Internal usage" :link "internal"}]]

"The runtime common layer is used by network, database, and substrate modules. It is also covered by xtbench parity tests so target implementations stay aligned."

[[:chapter {:title "API" :link "api"}]]
