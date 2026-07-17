(ns xt.ui.core-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.ui.core :as ui]]})

^{:refer xt.ui.core/registry-compose :added "4.1"}
(fact "composes contracts and allows platform renderer replacement"
  (!.js
   (var base (ui/base-registry))
   (var platform (ui/registry-create "web"))
   (ui/registry-register-renderer platform "ui/text" "react/text")
   (var registry (ui/registry-compose [base platform]))
   [(xt/x:get-key (ui/registry-contract registry "ui/text") "tier")
    (ui/registry-renderer registry "ui/text")])
  => ["portable" "react/text"])

^{:refer xt.ui.core/validate-node :added "4.1"}
(fact "validates portable trees and rejects unknown components"
  (!.js
   (do:>
    (var registry (ui/base-registry))
    (var thrown false)
    (try
      (ui/validate-node registry (ui/node "web/only" {} []))
      (catch e
        (:= thrown true)))
    (return
     [(ui/validate-node registry
                        (ui/node "ui/column" {"class" "gap-4"}
                                 [(ui/text "Hello" {})]))
      thrown])))
  => [true true])

^{:refer xt.ui.core/effect! :added "4.1"}
(fact "normalizes unavailable and failing native services"
  (notify/wait-on :js
    (var runtime (ui/runtime-create nil nil {} {} {}))
    (promise/x:promise-then
     (ui/effect! runtime "device/camera" {})
     (fn [missing]
       (repl/notify [missing
                     (ui/capability? runtime "device/camera")]))))
  => [{"status" "unavailable" "service" "device/camera"} false])

^{:refer xt.ui.core/resolve-slot :added "4.1"}
(fact "native slots replace portable fallback content"
  (!.js
   (var fallback [(ui/text "Upload" {})])
   (var slot (ui/slot "avatar" fallback {}))
   [(ui/resolve-slot (ui/runtime-create nil nil {} {} {}) slot)
    (ui/resolve-slot
     (ui/runtime-create nil nil {} {} {"avatar" [(ui/text "Camera" {})]})
     slot)])
  => [[{"component" "ui/text"
        "props" {"value" "Upload"}
        "children" []}]
      [{"component" "ui/text"
        "props" {"value" "Camera"}
        "children" []}]] )


^{:refer xt.ui.core/node :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/text :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/slot :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/extension :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/component-contract :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/registry-create :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/registry-register-contract :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/registry-register-renderer :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/registry-contract :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/registry-renderer :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/validate-props :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/runtime-create :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/capability? :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/service :added "4.1"}
(fact "TODO")

^{:refer xt.ui.core/base-registry :added "4.1"}
(fact "TODO")