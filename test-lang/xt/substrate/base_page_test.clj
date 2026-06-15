(ns xt.substrate.base-page-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.base-page :as base-page]
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

^{:refer xt.substrate.base-page/async-fn :added "4.1"}
(fact "runs the success callback for plain values"

  (notify/wait-on :js
    (var out nil)
    (-> (base-page/async-fn
         (fn [ctx]
           (return (. ctx ["value"])))
         {"value" 3}
         {"success" (fn [value]
                      (:= out ["success" value])
                      (return value))
          "error" (fn [err]
                    (:= out ["error" err])
                    (return err))})
        (promise/x:promise-then
         (fn [] (repl/notify out)))))
  => ["success" 3])

^{:refer xt.substrate.base-page/wrap-space-args :added "4.1"}
(fact "prepends the model context to handler arguments"

  (!.js
    ((base-page/wrap-space-args
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

^{:refer xt.substrate.base-page/check-event :added "4.1"}
(fact "supports boolean, string, function, and object predicates"

  (!.js
    [(base-page/check-event true "go" {} {})
     (base-page/check-event false "go" {} {})
     (base-page/check-event "go" "go" {} {})
     (base-page/check-event (fn [signal ctx]
                              (return (== signal "fn")))
                            "fn" {} {})
     (base-page/check-event {"go" true} "go" {} {})
     (base-page/check-event {"go" false} "go" {} {})])
  => [true false true true true false])

^{:refer xt.substrate.base-page/runtime-page :added "4.1"}
(fact "creates a blank runtime container"

  (!.js
    (base-page/runtime-page {"meta" {"kind" "page"}
                             "opts" {"debug" true}}))
  => {"::" "substrate.page"
      "groups" {}
      "meta" {"kind" "page"}
      "opts" {"meta" {"kind" "page"}
              "opts" {"debug" true}}})

^{:refer xt.substrate.base-page/get-space-page :added "4.1"}
(fact "reads the nested runtime slot from a space"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var runtime (base-page/runtime-page {"meta" {"kind" "page"}}))
    (base-page/set-space-page node "space/a" runtime)
    (base-page/get-space-page node "space/a"))
  => {"::" "substrate.page"
      "groups" {}
      "meta" {"kind" "page"}
      "opts" {"meta" {"kind" "page"}}})

^{:refer xt.substrate.base-page/ensure-space-page :added "4.1"}
(fact "stores the runtime in a nested per-space slot"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (base-page/ensure-space-page node "space/a")
    (node-space/get-space-state node "space/a"))
  => {"count" 1
      "label" "A"
      "page" {"::" "substrate.page"
               "groups" {}
               "meta" {}
               "opts" {}}})

^{:refer xt.substrate.base-page/set-space-page :added "4.1"}
(fact "replaces the nested runtime slot for a space"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var runtime (base-page/runtime-page {"meta" {"kind" "custom"}}))
    (base-page/set-space-page node "space/a" runtime)
    (base-page/get-space-page node "space/a"))
  => {"::" "substrate.page"
      "groups" {}
      "meta" {"kind" "custom"}
      "opts" {"meta" {"kind" "custom"}}})

^{:refer xt.substrate.base-page/group-get :added "4.1"}
(fact "reads a previously attached group"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (base-page/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" [1 2]}}})
    (var group (base-page/group-get node "space/a" "page"))
    (. group ["name"]))
  => "page")

^{:refer xt.substrate.base-page/group-ensure :added "4.1"}
(fact "throws when the group is missing"

  (!.js
    (base-page/group-ensure
     (substrate/node-create (-/create-node))
     "space/a"
     "missing"))
  => (throws))

^{:refer xt.substrate.base-page/model-ensure :added "4.1"}
(fact "returns the registered group and model"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (base-page/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" [1 2]}}})
    (var [group model] (base-page/model-ensure node "space/a" "page" "ping"))
    {"group" (. group ["name"])
     "model" (. model ["::"])
     "input" (. (. model ["input"]) ["current"])} )
  => {"group" "page"
      "model" "event.model"
      "input" {"data" [1 2]}})

^{:refer xt.substrate.base-page/prep-model :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/get-model-dependents :added "4.1"}
(fact "finds groups that depend on a model"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (base-page/add-group-attach
     node
     "space/a"
     "source"
     {"a" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}}
      "b" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}}})
    (base-page/add-group-attach
     node
     "space/a"
     "consumer"
     {"c" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}
           "deps" [["source" "a"]]}})
    (base-page/get-model-dependents node "space/a" "source" "a"))
  => {"consumer" ["c"]})

^{:refer xt.substrate.base-page/get-group-dependents :added "4.1"}
(fact "finds groups that depend on another group"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (base-page/add-group-attach
     node
     "space/a"
     "source"
     {"a" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}}})
    (base-page/add-group-attach
     node
     "space/a"
     "consumer"
     {"c" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}
           "deps" [["source" "a"]]}})
    (base-page/get-group-dependents node "space/a" "source"))
  => {"consumer" true})

^{:refer xt.substrate.base-page/run-tail-call :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/run-remote :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/remote-call :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/run-refresh :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/refresh-model-dependents :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/refresh-model :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/refresh-model-remote :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/refresh-model-dependents-unthrottled :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/refresh-group :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/get-group-deps :added "4.1"}
(fact "compiles local and cross-model dependencies"
  (!.js
   (base-page/get-group-deps
    "hello"
    {"source" {}
     "detail" {"deps" ["source" ["other" "remote"]]}}))
  => {"hello" {"source" {"detail" true}}
      "other" {"remote" {"detail" true}}})

^{:refer xt.substrate.base-page/get-unknown-deps :added "4.1"}
(fact "reports missing dependent views in the current space runtime"
  (!.js
   (var node (substrate/node-create (-/create-node)))
   (base-page/add-group-attach
    node
    "space/a"
    "other"
    {"remote" {"handler" (fn [space args request node]
                           (return (. space ["state"] ["label"])))
               "defaults" {"args" []}}})
   (base-page/get-unknown-deps
    node
    "space/a"
    "hello"
    {"detail" {"deps" ["missing" ["other" "unknown"]]}}
    (base-page/get-group-deps
     "hello"
     {"detail" {"deps" ["missing" ["other" "unknown"]]}})))
  => [["hello" "missing"]
      ["other" "unknown"]])

^{:refer xt.substrate.base-page/create-throttle :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/create-model :added "4.1"}
(fact "creates an initialized view model"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var model
         (base-page/create-model
          node
          "space/a"
          "page"
          "ping"
          {"handler" (fn [space args request node]
                       (return {"space" (. space ["id"])
                                "args" args}))
           "defaults" {"args" [1 2]
                     "output" {"value" 0}}}))
    {"type" (. model ["::"])
     "input" (. (. model ["input"]) ["current"])
     "output" (. (. model ["output"]) ["type"])
     "listener" (. (. (. model ["listeners"]) ["@/page"]) ["meta"] ["listener/type"])} )
  => {"type" "event.model"
      "input" {"data" [1 2]}
      "output" "output"
      "listener" "model"})

^{:refer xt.substrate.base-page/add-group-attach :added "4.1"}
(fact "registers a group with its models"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var group
         (base-page/add-group-attach
          node
          "space/a"
          "page"
          {"ping" {"handler" (fn [space args request node]
                               (return {"args" args}))
                   "defaults" {"args" [1 2]}}
           "pong" {"handler" (fn [space args request node]
                               (return {"args" args}))
                   "defaults" {"args" []}}}))
    {"name" (. group ["name"])
     "models" (. (. group ["models"]) ["ping"] ["::"])})
  => {"name" "page"
      "models" "event.model"})

^{:refer xt.substrate.base-page/add-group :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/remove-group :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/remove-model :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/group-update :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/model-update :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/model-set-input :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/trigger-group-raw :added "4.1"}
(fact "only triggers matching models"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (base-page/add-group-attach
     node
     "space/a"
     "page"
     {"always" {"handler" (fn [space args request node]
                            (return {"args" args}))
                "defaults" {"args" []}
                "trigger" true}
      "match" {"handler" (fn [space args request node]
                           (return {"args" args}))
               "defaults" {"args" []}
               "trigger" "go"}
      "skip" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" []}
              "trigger" "other"}})
    (var group (base-page/group-ensure node "space/a" "page"))
    (base-page/trigger-group-raw node "space/a" group "go" {"value" 9}))
  => ["always" "match"])

^{:refer xt.substrate.base-page/trigger-group :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/trigger-model :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/trigger-all :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/raw-callback-id :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/register-page-trigger :added "4.1"}
(fact "stores a keyed trigger entry on the node"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var entry (base-page/register-page-trigger
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

^{:refer xt.substrate.base-page/unregister-page-trigger :added "4.1"}
(fact "removes a keyed trigger entry from the node"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (base-page/register-page-trigger
     node
     "signal/a"
     (fn [_space frame local-node]
       (return frame))
     {"kind" "test"})
    (var prev (base-page/unregister-page-trigger node "signal/a"))
    {"id" (. prev ["id"])
     "remaining" (== nil (xt/x:get-key (. node ["triggers"]) "signal/a"))} )
  => {"id" "signal/a"
      "remaining" true})

^{:refer xt.substrate.base-page/add-raw-callback :added "4.1"}
(fact "registers a stable raw callback per space"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var entry (base-page/add-raw-callback node "space/a"))
    {"id" (. entry ["id"])
     "meta" (. entry ["meta"])})
  => {"id" "@/raw/page/space/a"
      "meta" {"space_id" "space/a"}})

^{:refer xt.substrate.base-page/remove-raw-callback :added "4.1"}
(fact "removes the stable raw callback per space"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (base-page/add-raw-callback node "space/a")
    (var prev (base-page/remove-raw-callback node "space/a"))
    {"id" (. prev ["id"])
     "meta" (. prev ["meta"])})
  => {"id" "@/raw/page/space/a"
      "meta" {"space_id" "space/a"}})


^{:refer xt.substrate.base-page/trigger-listeners :added "4.1"}
(fact "TODO")