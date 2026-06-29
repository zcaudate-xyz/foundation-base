(ns xt.substrate.page-core-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event-common]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
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

^{:refer xt.substrate.page-core/wrap-space-args :added "4.1"}
(fact "prepends the model context to handler arguments"

  (!.js
    ((page-core/wrap-space-args
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

^{:refer xt.substrate.page-core/check-event :added "4.1"}
(fact "supports boolean, string, function, and object predicates"

  (!.js
    [(page-core/check-event true "go" {} {})
     (page-core/check-event false "go" {} {})
     (page-core/check-event "go" "go" {} {})
     (page-core/check-event (fn [signal ctx]
                              (return (== signal "fn")))
                            "fn" {} {})
     (page-core/check-event {"go" true} "go" {} {})
     (page-core/check-event {"go" false} "go" {} {})])
  => [true false true true true false])

^{:refer xt.substrate.page-core/runtime-page :added "4.1"}
(fact "creates a blank runtime container"

  (!.js
    (page-core/runtime-page {"meta" {"kind" "page"}
                             "opts" {"debug" true}}))
  => {"::" "substrate.page"
      "groups" {}
      "meta" {"kind" "page"}
      "opts" {"meta" {"kind" "page"}
              "opts" {"debug" true}}})

^{:refer xt.substrate.page-core/get-space-page :added "4.1"}
(fact "reads the nested runtime slot from a space"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var runtime (page-core/runtime-page {"meta" {"kind" "page"}}))
    (page-core/set-space-page node "space/a" runtime)
    (page-core/get-space-page node "space/a"))
  => {"::" "substrate.page"
      "groups" {}
      "meta" {"kind" "page"}
      "opts" {"meta" {"kind" "page"}}})

^{:refer xt.substrate.page-core/ensure-space-page :added "4.1"}
(fact "stores the runtime in a nested per-space slot"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/ensure-space-page node "space/a")
    (node-space/get-space-state node "space/a"))
  => {"count" 1
      "label" "A"
      "page" {"::" "substrate.page"
               "groups" {}
               "meta" {}
               "opts" {}}})

^{:refer xt.substrate.page-core/set-space-page :added "4.1"}
(fact "replaces the nested runtime slot for a space"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var runtime (page-core/runtime-page {"meta" {"kind" "custom"}}))
    (page-core/set-space-page node "space/a" runtime)
    (page-core/get-space-page node "space/a"))
  => {"::" "substrate.page"
      "groups" {}
      "meta" {"kind" "custom"}
      "opts" {"meta" {"kind" "custom"}}})

^{:refer xt.substrate.page-core/group-get :added "4.1"}
(fact "reads a previously attached group"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" [1 2]}}})
    (var group (page-core/group-get node "space/a" "page"))
    (. group ["name"]))
  => "page")

^{:refer xt.substrate.page-core/group-ensure :added "4.1"}
(fact "throws when the group is missing"

  (!.js
    (page-core/group-ensure
     (substrate/node-create (-/create-node))
     "space/a"
     "missing"))
  => (throws))

^{:refer xt.substrate.page-core/model-ensure :added "4.1"}
(fact "returns the registered group and model"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" [1 2]}}})
    (var [group model] (page-core/model-ensure node "space/a" "page" "ping"))
    {"group" (. group ["name"])
     "model" (. model ["::"])
     "input" (. (. model ["input"]) ["current"])} )
  => {"group" "page"
      "model" "event.model"
      "input" {"data" [1 2]}})

^{:refer xt.substrate.page-core/prep-model :added "4.1"}
(fact "prepares the model context and disabled flag"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" [1 2]}}})
    (var [path context disabled]
         (page-core/prep-model node "space/a" "page" "ping" {"args" [3 4]}))
    {"path" path
     "args" (. context ["args"])
     "disabled" disabled
     "space" (. (. context ["space"]) ["id"])
     "group" (. (. context ["group"]) ["name"])})
  => {"path" ["page" "ping"]
      "args" [3 4]
      "disabled" false
      "space" "space/a"
      "group" "page"})

^{:refer xt.substrate.page-core/get-model-dependents :added "4.1"}
(fact "finds groups that depend on a model"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "source"
     {"a" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}}
      "b" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}}})
    (page-core/add-group-attach
     node
     "space/a"
     "consumer"
     {"c" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}
           "deps" [["source" "a"]]}})
    (page-core/get-model-dependents node "space/a" "source" "a"))
  => {"consumer" ["c"]})

^{:refer xt.substrate.page-core/get-group-dependents :added "4.1"}
(fact "finds groups that depend on another group"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "source"
     {"a" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}}})
    (page-core/add-group-attach
     node
     "space/a"
     "consumer"
     {"c" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}
           "deps" [["source" "a"]]}})
    (page-core/get-group-dependents node "space/a" "source"))
  => {"consumer" true})

^{:refer xt.substrate.page-core/run-tail-call :added "4.1"}
(fact "returns the accumulated run state"

  (!.js
    (var context {"acc" {"value" 1}
                  "path" ["page" "ping"]
                  "node" {}
                  "space" {"id" "space/a"}})
    (page-core/run-tail-call context nil))
  => {"value" 1})

^{:refer xt.substrate.page-core/run-remote :added "4.1"}
(fact "runs the remote pipeline and returns the accumulator"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"main" true}))
              "pipeline" {"remote" {"handler" (fn [ctx] (return {"remote" true}))}}
              "defaults" {"args" []}}})
    (var [path context disabled]
         (page-core/prep-model node "space/a" "page" "ping" {}))
    (-> (page-core/run-remote context true path nil)
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"remote" (. acc ["remote"])
                         "path" (. acc ["path"])})))))
  => {"remote" [true {"remote" true}]
      "path" ["page" "ping"]})

^{:refer xt.substrate.page-core/remote-call :added "4.1"}
(fact "invokes a remote handler through the model"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"main" true}))
              "pipeline" {"remote" {"handler" (fn [ctx] (return {"remote" true}))}}
              "defaults" {"args" []}}})
    (-> (page-core/remote-call node "space/a" "page" "ping" [] true)
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"remote" (. acc ["remote"])
                         "path" (. acc ["path"])})))))
  => {"remote" [true {"remote" true}]
      "path" ["page" "ping"]})

^{:refer xt.substrate.page-core/run-refresh :added "4.1"}
(fact "runs the main pipeline and returns the accumulator"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"refreshed" true}))
              "defaults" {"args" []}}})
    (var [path context disabled]
         (page-core/prep-model node "space/a" "page" "ping" {}))
    (-> (page-core/run-refresh context disabled path nil)
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"main" (. acc ["main"])
                         "path" (. acc ["path"])})))))
  => {"main" [true {"refreshed" true}]
      "path" ["page" "ping"]})

^{:refer xt.substrate.page-core/refresh-model-dependents :added "4.1"}
(fact "returns the map of dependent models"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "source"
     {"a" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}}})
    (page-core/add-group-attach
     node
     "space/a"
     "consumer"
     {"c" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}
           "deps" [["source" "a"]]}})
    (page-core/refresh-model-dependents node "space/a" "source" "a"))
  => {"consumer" ["c"]})

^{:refer xt.substrate.page-core/refresh-model :added "4.1"}
(fact "refreshes a single model and returns the run accumulator"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"refreshed" true}))
              "defaults" {"args" []}}})
    (-> (page-core/refresh-model node "space/a" "page" "ping" {} nil)
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"main" (. acc ["main"])
                         "path" (. acc ["path"])})))))
  => {"main" [true {"refreshed" true}]
      "path" ["page" "ping"]})

^{:refer xt.substrate.page-core/refresh-model-remote :added "4.1"}
(fact "refreshes the remote stage of a model"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"main" true}))
              "pipeline" {"remote" {"handler" (fn [ctx] (return {"remote" true}))}}
              "defaults" {"args" []}}})
    (-> (page-core/refresh-model-remote node "space/a" "page" "ping" nil)
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"remote" (. acc ["remote"])
                         "path" (. acc ["path"])})))))
  => {"remote" [true {"remote" true}]
      "path" ["page" "ping"]})

^{:refer xt.substrate.page-core/refresh-model-dependents-unthrottled :added "4.1"}
(fact "refreshes dependents without using the throttle"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "source"
     {"a" {"handler" (fn [ctx] (return {"source" true}))
           "defaults" {"args" []}}})
    (page-core/add-group-attach
     node
     "space/a"
     "consumer"
     {"c" {"handler" (fn [ctx] (return {"consumer" true}))
           "defaults" {"args" []}
           "deps" [["source" "a"]]}})
    (-> (page-core/refresh-model-dependents-unthrottled
         node "space/a" "source" "a" nil)
        (promise/x:promise-then
         (fn [arr]
           (repl/notify {"count" (xt/x:len arr)
                         "first" (. (. (xt/x:get-idx arr 0) ["main"]) [1])})))))
  => {"count" 1
      "first" {"consumer" true}})

^{:refer xt.substrate.page-core/refresh-group :added "4.1"}
(fact "refreshes every model in the group"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"ping" true}))
              "defaults" {"args" []}}
      "pong" {"handler" (fn [ctx] (return {"pong" true}))
              "defaults" {"args" []}}})
    (-> (page-core/refresh-group node "space/a" "page" {} nil)
        (promise/x:promise-then
         (fn [arr]
           (repl/notify {"count" (xt/x:len arr)})))))
  => {"count" 2})

^{:refer xt.substrate.page-core/get-group-deps :added "4.1"}
(fact "compiles local and cross-model dependencies"
  (!.js
   (page-core/get-group-deps
    "hello"
    {"source" {}
     "detail" {"deps" ["source" ["other" "remote"]]}}))
  => {"hello" {"source" {"detail" true}}
      "other" {"remote" {"detail" true}}})

^{:refer xt.substrate.page-core/get-unknown-deps :added "4.1"}
(fact "reports missing dependent views in the current space runtime"
  (!.js
   (var node (substrate/node-create (-/create-node)))
   (page-core/add-group-attach
    node
    "space/a"
    "other"
    {"remote" {"handler" (fn [space args request node]
                           (return (. space ["state"] ["label"])))
               "defaults" {"args" []}}})
   (page-core/get-unknown-deps
    node
    "space/a"
    "hello"
    {"detail" {"deps" ["missing" ["other" "unknown"]]}}
    (page-core/get-group-deps
     "hello"
     {"detail" {"deps" ["missing" ["other" "unknown"]]}})))
  => [["hello" "missing"]
      ["other" "unknown"]])

^{:refer xt.substrate.page-core/create-throttle :added "4.1"}
(fact "creates a throttle for the group"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var throttle (page-core/create-throttle node "space/a" "page" nil))
    {"has-handler" (xt/x:is-function? (. throttle ["handler"]))
     "active" (. throttle ["active"])
     "queued" (. throttle ["queued"])})
  => {"has-handler" true
      "active" {}
      "queued" {}})

^{:refer xt.substrate.page-core/create-model :added "4.1"}
(fact "creates an initialized view model"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var model
         (page-core/create-model
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

^{:refer xt.substrate.page-core/add-group-attach :added "4.1"}
(fact "registers a group with its models"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var group
         (page-core/add-group-attach
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

^{:refer xt.substrate.page-core/add-group :added "4.1"}
(fact "attaches a group and triggers initial refresh"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (var group (page-core/add-group
                node
                "space/a"
                "page"
                {"ping" {"handler" (fn [ctx] (return {"ping" true}))
                         "defaults" {"args" []}}
                 "pong" {"handler" (fn [ctx] (return {"pong" true}))
                         "defaults" {"args" []}}}))
    (-> (. group ["init"])
        (promise/x:promise-then
         (fn [arr]
           (repl/notify {"name" (. group ["name"])
                         "model-count" (xt/x:len (xt/x:obj-keys (. group ["models"])))
                         "init-count" (xt/x:len arr)})))))
  => {"name" "page"
      "model-count" 2
      "init-count" 2})

^{:refer xt.substrate.page-core/remove-group :added "4.1"}
(fact "removes a group from the runtime"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" []}}})
    (var removed (page-core/remove-group node "space/a" "page"))
    {"removed-name" (. removed ["name"])
     "remaining" (page-core/group-get node "space/a" "page")})
  => {"removed-name" "page"
      "remaining" nil})

^{:refer xt.substrate.page-core/remove-group :added "4.1"}
(fact "throws when dependents exist"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "source"
     {"a" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}}})
    (page-core/add-group-attach
     node
     "space/a"
     "consumer"
     {"c" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}
           "deps" [["source" "a"]]}})
    (page-core/remove-group node "space/a" "source"))
  => (throws))

^{:refer xt.substrate.page-core/remove-model :added "4.1"}
(fact "removes a model from its group"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" []}}
      "pong" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" []}}})
    (var removed (page-core/remove-model node "space/a" "page" "ping"))
    {"removed-type" (. removed ["::"])
     "remaining" (xt/x:obj-keys (. (. (page-core/group-get node "space/a" "page") ["models"])))})
  => {"removed-type" "event.model"
      "remaining" ["pong"]})

^{:refer xt.substrate.page-core/remove-model :added "4.1"}
(fact "throws when model dependents exist"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "source"
     {"a" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}}})
    (page-core/add-group-attach
     node
     "space/a"
     "consumer"
     {"c" {"handler" (fn [space args request node]
                       (return {"args" args}))
           "defaults" {"args" []}
           "deps" [["source" "a"]]}})
    (page-core/remove-model node "space/a" "source" "a"))
  => (throws))

^{:refer xt.substrate.page-core/group-update :added "4.1"}
(fact "updates every model in the group"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"ping" true}))
              "defaults" {"args" []}}
      "pong" {"handler" (fn [ctx] (return {"pong" true}))
              "defaults" {"args" []}}})
    (-> (page-core/group-update node "space/a" "page" {})
        (promise/x:promise-then
         (fn [result]
           (repl/notify {"ping" (. (. (. result ["ping"]) ["main"]) [1])
                         "pong" (. (. (. result ["pong"]) ["main"]) [1])})))))
  => {"ping" {"ping" true}
      "pong" {"pong" true}})

^{:refer xt.substrate.page-core/model-update :added "4.1"}
(fact "updates a single model"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"updated" true}))
              "defaults" {"args" []}}})
    (-> (page-core/model-update node "space/a" "page" "ping" {})
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"main" (. acc ["main"])})))))
  => {"main" [true {"updated" true}]})

^{:refer xt.substrate.page-core/model-set-input :added "4.1"}
(fact "sets input and refreshes the model"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx]
                           (return {"data" (. (. ctx ["input"]) ["data"])}))
              "defaults" {"args" []}}})
    (-> (page-core/model-set-input node "space/a" "page" "ping" {"data" [1 2 3]} {})
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"main" (. acc ["main"])})))))
  => {"main" [true {"data" [1 2 3]}]})

^{:refer xt.substrate.page-core/get-current-output :added "4.1"}
(fact "returns the current output value of a model"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"updated" true}))
              "defaults" {"args" []}}})
    (-> (page-core/refresh-model node "space/a" "page" "ping" {} nil)
        (promise/x:promise-then
         (fn [_]
           (repl/notify (page-core/get-current-output node "space/a" "page" "ping"))))))
  => {"updated" true})

^{:refer xt.substrate.page-core/trigger-group-raw :added "4.1"}
(fact "only triggers matching models"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"always" {"handler" (fn [space args request node]
                            (return {"args" args}))
                "defaults" {"args" []}
                "options" {"trigger" true}}
      "match" {"handler" (fn [space args request node]
                           (return {"args" args}))
               "defaults" {"args" []}
               "options" {"trigger" "go"}}
      "skip" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" []}
              "options" {"trigger" "other"}}})
    (var group (page-core/group-ensure node "space/a" "page"))
    (page-core/trigger-group-raw node "space/a" group "go" {"value" 9}))
  => ["always" "match"])

^{:refer xt.substrate.page-core/trigger-group :added "4.1"}
(fact "resolves the group and triggers matching models"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"always" {"handler" (fn [space args request node]
                            (return {"args" args}))
                "defaults" {"args" []}
                "options" {"trigger" true}}
      "match" {"handler" (fn [space args request node]
                           (return {"args" args}))
               "defaults" {"args" []}
               "options" {"trigger" "go"}}
      "skip" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" []}
              "options" {"trigger" "other"}}})
    (page-core/trigger-group node "space/a" "page" "go" {"value" 9}))
  => ["always" "match"])

^{:refer xt.substrate.page-core/trigger-model :added "4.1"}
(fact "triggers a single model when signal matches"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"triggered" true}))
              "defaults" {"args" []}
              "options" {"trigger" "go"}}})
    (-> (page-core/trigger-model node "space/a" "page" "ping" "go" {"value" 9})
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"main" (. acc ["main"])})))))
  => {"main" [true {"triggered" true}]})

^{:refer xt.substrate.page-core/trigger-all :added "4.1"}
(fact "triggers all groups in the space"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-group-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" []}
              "options" {"trigger" "go"}}})
    (page-core/add-group-attach
     node
     "space/a"
     "other"
     {"pong" {"handler" (fn [space args request node]
                          (return {"args" args}))
              "defaults" {"args" []}
              "options" {"trigger" "go"}}})
    (page-core/trigger-all node "space/a" "go" {}))
  => {"page" ["ping"]
      "other" ["pong"]})

^{:refer xt.substrate.page-core/raw-callback-id :added "4.1"}
(fact "builds a stable trigger id for a space"

  (!.js
    [(page-core/raw-callback-id "space/a")
     (page-core/raw-callback-id nil)])
  => ["@/raw/page/space/a"
      "@/raw/page/"])

^{:refer xt.substrate.page-core/register-page-trigger :added "4.1"}
(fact "stores a keyed trigger entry on the node"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var entry (page-core/register-page-trigger
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

^{:refer xt.substrate.page-core/unregister-page-trigger :added "4.1"}
(fact "removes a keyed trigger entry from the node"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/register-page-trigger
     node
     "signal/a"
     (fn [_space frame local-node]
       (return frame))
     {"kind" "test"})
    (var prev (page-core/unregister-page-trigger node "signal/a"))
    {"id" (. prev ["id"])
     "remaining" (== nil (xt/x:get-key (. node ["triggers"]) "signal/a"))} )
  => {"id" "signal/a"
      "remaining" true})

^{:refer xt.substrate.page-core/add-raw-callback :added "4.1"}
(fact "registers a stable raw callback per space"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var entry (page-core/add-raw-callback node "space/a"))
    {"id" (. entry ["id"])
     "meta" (. entry ["meta"])})
  => {"id" "@/raw/page/space/a"
      "meta" {"space_id" "space/a"}})

^{:refer xt.substrate.page-core/remove-raw-callback :added "4.1"}
(fact "removes the stable raw callback per space"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/add-raw-callback node "space/a")
    (var prev (page-core/remove-raw-callback node "space/a"))
    {"id" (. prev ["id"])
     "meta" (. prev ["meta"])})
  => {"id" "@/raw/page/space/a"
      "meta" {"space_id" "space/a"}})


^{:refer xt.substrate.page-core/trigger-listeners :added "4.1"}
(fact "dispatches events to keyed listeners on the node"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (var captured [])
    (event-common/add-keyed-listener
     node
     (xt/x:json-encode ["space/a" ["page" "ping"]])
     "listener/a"
     "page"
     (fn [_id data _t _meta]
       (xt/x:arr-push captured data)
       (return data))
     {}
     nil)
    (var triggered (page-core/trigger-listeners
                    node "space/a" ["page" "ping"] {"value" 9}))
    {"triggered" triggered
     "captured" captured})
  => {"triggered" ["listener/a"]
      "captured" [{"space_id" "space/a"
                   "path" ["page" "ping"]
                   "value" 9}]})


^{:refer xt.substrate.page-core/set-proxy-dispatcher :added "4.1"}
(fact "sets and returns the proxy dispatcher"

  (!.js
    (var dispatcher (fn [op node space-id group-id args]
                      (return {"dispatched" op})))
    (var result (page-core/set-proxy-dispatcher dispatcher))
    (page-core/set-proxy-dispatcher nil)
    (== result dispatcher))
  => true)

^{:refer xt.substrate.page-core/get-proxy-dispatcher :added "4.1"}
(fact "returns the current proxy dispatcher"

  (!.js
    (var dispatcher (fn [op node space-id group-id args]
                      (return {"dispatched" op})))
    (page-core/set-proxy-dispatcher dispatcher)
    (var current (page-core/get-proxy-dispatcher))
    (page-core/set-proxy-dispatcher nil)
    (== current dispatcher))
  => true)

^{:refer xt.substrate.page-core/proxy-group? :added "4.1"}
(fact "checks whether a group is marked as a remote proxy"

  (!.js
    [(page-core/proxy-group? {"remote" true})
     (page-core/proxy-group? {"name" "local"})
     (page-core/proxy-group? {})])
  => [true nil nil])