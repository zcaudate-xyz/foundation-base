(ns xt.ui.core-test
  (:use code.test)
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.ui.core :as ui]]})

^{:refer xt.ui.core/node :added "4.1"}
(fact "defaults node props and children without discarding supplied values"
  (!.js [(ui/node "ui/row" nil nil)
         (ui/node "ui/column" {"hidden" false} ["hello" 0])])
  => [{"component" "ui/row" "props" {} "children" []}
      {"component" "ui/column" "props" {"hidden" false} "children" ["hello" 0]}])

^{:refer xt.ui.core/text :added "4.1"}
(fact "text preserves scalar values and merges explicit props"
  (!.js [(ui/text 0 nil) (ui/text "default" {"value" "override" "class" "small"})])
  => [{"component" "ui/text" "props" {"value" 0} "children" []}
      {"component" "ui/text" "props" {"value" "override" "class" "small"} "children" []}])

^{:refer xt.ui.core/slot :added "4.1"}
(fact "slots carry their identifier, props and fallback children"
  (!.js [(ui/slot "toolbar" nil nil) (ui/slot "body" ["fallback"] {"key" "body"})])
  => [{"component" "ui/slot" "props" {"slot_id" "toolbar"} "children" []}
      {"component" "ui/slot" "props" {"slot_id" "body" "key" "body"} "children" ["fallback"]}])

^{:refer xt.ui.core/extension :added "4.1"}
(fact "extensions retain a portable fallback and mark extension props"
  (!.js [(ui/extension "native/camera" nil nil)
         (ui/extension "native/camera" {"key" "camera"} ["Upload"])])
  => [{"component" "native/camera" "props" {"extension" true} "children" []}
      {"component" "native/camera" "props" {"extension" true "key" "camera"} "children" ["Upload"]}])

^{:refer xt.ui.core/component-contract :added "4.1"}
(fact "contracts have portable defaults and preserve explicit schema fields"
  (!.js [(ui/component-contract "ui/test" nil nil nil nil nil)
         (ui/component-contract "native/test" ui/NATIVE ["value"] ["on_change"] ["body"] "ui/text")])
  => [{"id" "ui/test" "tier" "portable" "props" [] "events" [] "slots" [] "fallback" nil}
      {"id" "native/test" "tier" "native" "props" ["value"] "events" ["on_change"] "slots" ["body"] "fallback" "ui/text"}])

^{:refer xt.ui.core/registry-create :added "4.1"}
(fact "registry layers start empty and do not share mutable storage"
  (!.js
   (var first (ui/registry-create "first"))
   (var second (ui/registry-create "second"))
   (ui/registry-register-renderer first "ui/text" "first/text")
   second)
  => {"id" "second" "contracts" {} "renderers" {}})

^{:refer xt.ui.core/registry-register-contract :added "4.1"}
(fact "identical definitions are idempotent and incompatible contracts are rejected"
  (!.js
   (var registry (ui/registry-create "test"))
   (var contract (ui/component-contract "ui/test" nil ["value"] nil nil nil))
   (var result (ui/registry-register-contract registry contract))
   (ui/registry-register-contract registry
                                  (ui/component-contract "ui/test" nil ["value"] nil nil nil))
   (var message nil)
   (try
     (ui/registry-register-contract registry
                                    (ui/component-contract "ui/test" nil ["other"] nil nil nil))
     (catch e (:= message (xt/x:ex-message e))))
   [(== result registry)
    (xt/x:get-path registry ["contracts" "ui/test" "props"])
    message])
  => [true ["value"] "ERR - incompatible UI contract - ui/test"])

^{:refer xt.ui.core/registry-register-renderer :added "4.1"}
(fact "renderer registration replaces only the requested renderer"
  (!.js
   (var registry (ui/registry-create "test"))
   (ui/registry-register-renderer registry "ui/text" "old")
   (ui/registry-register-renderer registry "ui/row" "row")
   (var result (ui/registry-register-renderer registry "ui/text" "new"))
   [(== registry result) (xt/x:get-key registry "renderers")])
  => [true {"ui/text" "new" "ui/row" "row"}])

^{:refer xt.ui.core/registry-compose :added "4.1"}
(fact "composes contracts and allows platform renderer replacement"
  (!.js
   (var base (ui/base-registry))
   (var platform (ui/registry-create "web"))
   (ui/registry-register-renderer platform "ui/text" "react/text")
   (var registry (ui/registry-compose [base platform]))
   [(xt/x:get-key (ui/registry-contract registry "ui/text") "tier")
    (ui/registry-renderer registry "ui/text")])
  => ["portable" "react/text"]

  (!.js
   (var base (ui/registry-create "base"))
   (var app (ui/registry-create "app"))
   (ui/registry-register-renderer base "ui/text" "base/text")
   (ui/registry-register-renderer app "ui/text" "app/text")
   (var registry (ui/registry-compose [base app]))
   [(ui/registry-renderer registry "ui/text")
    (ui/registry-renderer base "ui/text")
    (ui/registry-compose nil)])
  => ["app/text" "base/text" {"id" "composed" "contracts" {} "renderers" {}}])

^{:refer xt.ui.core/registry-contract :added "4.1"}
(fact "looks up registered contracts and leaves missing contracts absent"
  (!.js
   (var registry (ui/base-registry))
   [(xt/x:get-key (ui/registry-contract registry "ui/text") "id")
    (xt/x:nil? (ui/registry-contract registry "missing"))])
  => ["ui/text" true])

^{:refer xt.ui.core/registry-renderer :added "4.1"}
(fact "looks up renderer values without inventing defaults"
  (!.js
   (var registry (ui/registry-create "test"))
   (ui/registry-register-renderer registry "ui/text" "native/text")
   [(ui/registry-renderer registry "ui/text")
    (xt/x:nil? (ui/registry-renderer registry "missing"))])
  => ["native/text" true])

^{:refer xt.ui.core/validate-props :added "4.1"}
(fact "accepts declared props and events and reports the exact unsupported prop"
  (!.js
   (var contract (ui/component-contract "ui/test" nil ["value"] ["on_change"] nil nil))
   (var message nil)
   (try
     (ui/validate-props contract {"unknown" true})
     (catch e (:= message (xt/x:ex-message e))))
   [(ui/validate-props contract nil)
    (ui/validate-props contract {"value" 0 "on_change" (fn [value] (return value))})
    message])
  => [true true "ERR - unsupported UI prop - ui/test.unknown"])

^{:refer xt.ui.core/validate-node :added "4.1"}
(fact "validates portable trees and rejects unknown components, including nested ones"
  (!.js
   (var registry (ui/base-registry))
   (var message nil)
   (try
     (ui/validate-node registry (ui/node "ui/column" {} [(ui/node "web/only" {} [])]))
     (catch e (:= message (xt/x:ex-message e))))
   [(ui/validate-node registry (ui/node "ui/column" {"class" "gap-4"} [(ui/text "Hello" {})]))
    (ui/validate-node registry [nil "text" 0 [(ui/text "nested" {})]])
    message])
  => [true true "ERR - unregistered UI component - web/only"])

^{:refer xt.ui.core/runtime-create :added "4.1"}
(fact "runtime defaults preserve the supplied store and registry"
  (!.js (ui/runtime-create "store" "registry" nil nil nil))
  => {"store" "store" "registry" "registry" "capabilities" {} "services" {} "slots" {}})

^{:refer xt.ui.core/capability? :added "4.1"}
(fact "capabilities require a true flag and missing capabilities are unavailable"
  (!.js
   (var runtime (ui/runtime-create nil nil {"yes" true "no" false "text" "yes"} nil nil))
   [(ui/capability? runtime "yes") (ui/capability? runtime "no")
    (ui/capability? runtime "text") (ui/capability? runtime "missing")])
  => [true false false false])

^{:refer xt.ui.core/service :added "4.1"}
(fact "returns the installed service handler without invoking it"
  (!.js
   (var handler (fn [args] (return (xt/x:get-key args "value"))))
   (var runtime (ui/runtime-create nil nil nil {"echo" handler} nil))
   [(== handler (ui/service runtime "echo"))
    ((ui/service runtime "echo") {"value" "pong"})
    (xt/x:nil? (ui/service runtime "missing"))])
  => [true "pong" true])

^{:refer xt.ui.core/effect! :added "4.1"}
(fact "normalizes unavailable native services"
  (notify/wait-on :js
    (var runtime (ui/runtime-create nil nil {} {} {}))
    (promise/x:promise-then
     (ui/effect! runtime "device/camera" {})
     (fn [missing]
       (repl/notify [missing (ui/capability? runtime "device/camera")]))))
  => [{"status" "unavailable" "service" "device/camera"} false])

^{:refer xt.ui.core/effect! :id effect-results :added "4.1"}
(fact "preserves synchronous and asynchronous results and normalizes thrown failures"
  (notify/wait-on :js
    (var runtime
         (ui/runtime-create nil nil nil
          {"echo" (fn [args] (return args))
           "async" (fn [args] (return (promise/x:promise-run {"saved" (. args ["id"])})))
           "fail" (fn [_args] (xt/x:err "offline"))
           "invalid" "not a handler"} nil))
    (promise/x:promise-then
     (promise/x:promise-all [(ui/effect! runtime "echo" nil)
                            (ui/effect! runtime "async" {"id" 7})
                            (ui/effect! runtime "fail" {})
                            (ui/effect! runtime "invalid" {})])
     (fn [results]
       (repl/notify [(xt/x:get-key results 0)
                     (xt/x:get-key results 1)
                     (xt/x:get-path results [2 "status"])
                     (xt/x:get-path results [2 "service"])
                     (xt/x:get-path results [2 "message"])
                     (xt/x:get-key results 3)]))))
  => [{} {"saved" 7} "error" "fail" "offline" {"status" "unavailable" "service" "invalid"}])

^{:refer xt.ui.core/resolve-slot :added "4.1"}
(fact "native slots replace portable fallback content, including an empty replacement"
  (!.js
   (var fallback [(ui/text "Upload" {})])
   (var slot (ui/slot "avatar" fallback {}))
   [(ui/resolve-slot (ui/runtime-create nil nil {} {} {}) slot)
    (ui/resolve-slot (ui/runtime-create nil nil {} {} {"avatar" [(ui/text "Camera" {})]}) slot)
    (ui/resolve-slot (ui/runtime-create nil nil {} {} {"avatar" []}) slot)])
  => [[{"component" "ui/text" "props" {"value" "Upload"} "children" []}]
      [{"component" "ui/text" "props" {"value" "Camera"} "children" []}]
      []])

^{:refer xt.ui.core/base-registry :added "4.1"}
(fact "registers all structural contracts without platform renderers"
  (!.js
   (var registry (ui/base-registry))
   [(xt/x:get-key registry "id")
    (xt/x:len (xt/x:obj-keys (. registry ["contracts"])))
    (xt/x:get-path registry ["contracts" "ui/text" "props"])
    (xt/x:get-path registry ["contracts" "ui/slot" "props"])
    (. registry ["renderers"])])
  => ["xt.ui/base" 7 ["value" "class" "style" "hidden" "key" "aria_label"]
      ["slot_id" "class" "style" "hidden" "key"] {}])
