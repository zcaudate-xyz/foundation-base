(ns xt.substrate.page-util-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event-common]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-util :as page-util]
             [xt.substrate.base-space :as node-space]]})

(defn.js create-node
  []
  (return
   {"id" "node-a"
    "spaces" {"space/a" {"state" {"count" 1
                                  "label" "A"}}
              "space/b" {"state" {"count" 10
                                  "label" "B"}}}}))

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.page-util/wrap-space-args :added "4.1"}
(fact "prepends the model context to handler arguments"

  (!.js
    ((page-util/wrap-space-args
      (fn [ctx a b]
        (return {"space" (. ctx ["space_id"])
                 "path" (. ctx ["path"])
                 "args" [a b]})))
     {"space_id" "space/a"
      "path" ["page" "ping"]
      "args" [1 2]}))
  => {"space" "space/a"
      "path" ["page" "ping"]
      "args" [1 2]})

^{:refer xt.substrate.page-util/check-event :added "4.1"}
(fact "supports boolean, string, function, and object predicates"

  (!.js
    [(page-util/check-event true "go" {} {})
     (page-util/check-event false "go" {} {})
     (page-util/check-event "go" "go" {} {})
     (page-util/check-event (fn [signal ctx]
                              (return (== signal "fn")))
                            "fn" {} {})
     (page-util/check-event {"go" true} "go" {} {})
     (page-util/check-event {"go" false} "go" {} {})])
  => [true false true true true false])

^{:refer xt.substrate.page-util/run-tail-call :added "4.1"}
(fact "returns the accumulated run state"

  (!.js
    (var context {"acc" {"value" 1}
                  "path" ["page" "ping"]
                  "node" {}
                  "space" {"id" "space/a"}})
    (page-util/run-tail-call context nil))
  => {"value" 1})

^{:refer xt.substrate.page-util/run-remote :added "4.1"}
(fact "runs the remote pipeline and returns the accumulator"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/group-add-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"main" true}))
              "pipeline" {"remote" {"handler" (fn [ctx] (return {"remote" true}))}}
              "defaults" {"args" []}}})
    (var [path context disabled]
         (page-core/model-prep node "space/a" "page" "ping" {}))
    (-> (page-util/run-remote context true path nil)
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"remote" (. acc ["remote"])
                         "path" (. acc ["path"])})))))
  => {"remote" [true {"remote" true}]
      "path" ["page" "ping"]})

^{:refer xt.substrate.page-util/run-refresh :added "4.1"}
(fact "runs the main pipeline and returns the accumulator"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/group-add-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"refreshed" true}))
              "defaults" {"args" []}}})
    (var [path context disabled]
         (page-core/model-prep node "space/a" "page" "ping" {}))
    (-> (page-util/run-refresh context disabled path nil)
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"main" (. acc ["main"])
                         "path" (. acc ["path"])})))))
  => {"main" [true {"refreshed" true}]
      "path" ["page" "ping"]})

^{:refer xt.substrate.page-util/get-group-deps :added "4.1"}
(fact "compiles local and cross-model dependencies"
  (!.js
   (page-util/get-group-deps
    "hello"
    {"source" {}
     "detail" {"deps" ["source" ["other" "remote"]]}}))
  => {"hello" {"source" {"detail" true}}
      "other" {"remote" {"detail" true}}})

^{:refer xt.substrate.page-util/raw-callback-id :added "4.1"}
(fact "builds a stable trigger id for a space"

  (!.js
    [(page-util/raw-callback-id "space/a")
     (page-util/raw-callback-id nil)])
  => ["@/raw/page/space/a"
      "@/raw/page/"])

^{:refer xt.substrate.page-util/register-page-trigger :added "4.1"}
(fact "stores a keyed trigger entry on the node"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var entry (page-util/register-page-trigger
                node
                "signal/a"
                (fn [_space frame local-node]
                  (return frame))
                {"kind" "test"}))
    {"id" (. entry ["id"])
     "meta" (. entry ["meta"])
     "stored" (. (. node ["triggers"]) ["signal/a"] ["id"])} )
  => {"id" "signal/a"
      "meta" {"kind" "test"}
      "stored" "signal/a"})

^{:refer xt.substrate.page-util/unregister-page-trigger :added "4.1"}
(fact "removes a keyed trigger entry from the node"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-util/register-page-trigger
     node
     "signal/a"
     (fn [_space frame local-node]
       (return frame))
     {"kind" "test"})
    (var prev (page-util/unregister-page-trigger node "signal/a"))
    {"id" (. prev ["id"])
     "remaining" (== nil (xt/x:get-key (. node ["triggers"]) "signal/a"))} )
  => {"id" "signal/a"
      "remaining" true})
