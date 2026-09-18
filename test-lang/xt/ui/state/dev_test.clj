(ns xt.ui.state.dev-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.state.dev :as dev]]})

^{:refer xt.ui.state.dev/create :added "4.1"}
(fact "diagnostics default to an idle disabled state without capabilities"
  (!.js (dev/create nil nil))
  => {"enabled" false "capabilities" {} "status" "idle" "summary" {} "panels" {}})

^{:refer xt.ui.state.dev/available? :added "4.1"}
(fact "diagnostics require both the global switch and the requested capability"
  (!.js
   [(dev/available? (dev/create true {"inspect" true}) "inspect")
    (dev/available? (dev/create false {"inspect" true}) "inspect")
    (dev/available? (dev/create true {"inspect" false}) "inspect")
    (dev/available? (dev/create true {}) "missing")])
  => [true false false false])

^{:refer xt.ui.state.dev/set-summary! :added "4.1"}
(fact "summary replacement marks diagnostics ready and normalizes nil"
  (!.js
   (var current (dev/create true {"inspect" true}))
   (dev/set-summary! current {"models" 3})
   (var ready [(. current ["status"]) (. current ["summary"])])
   (dev/set-summary! current nil)
   [ready (. current ["summary"]) (. current ["capabilities"])])
  => [["ready" {"models" 3}] {} {"inspect" true}])
