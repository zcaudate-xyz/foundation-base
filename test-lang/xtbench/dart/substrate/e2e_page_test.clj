(ns xtbench.dart.substrate.e2e-page-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.base-page :as base-page]
             [xt.substrate.transport-memory :as transport-memory]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.base-page/add-group :name demo-000-page-model-basic}
(fact "a page model computes its output from args on initial refresh"

  (notify/wait-on :dart
    (var node (substrate/node-create {"id" "node"}))
    (var group (base-page/add-group node
                                    nil
                                    "page"
                                    {"greet"
                                     {"handler" (fn [context]
                                                  (var args (. context ["args"]))
                                                  (return (xt/x:cat "hello "
                                                                    (xt/x:get-idx args 0))))
                                      "defaults" {"args" ["world"]}}}))
    (-> (. group ["init"])
        (promise/x:promise-then
         (fn [_]
           (var model-result (base-page/model-ensure node nil "page" "greet"))
           (var model (xt/x:get-idx model-result 1))
           (repl/notify (event-model/get-current model nil))))))
  => "hello world")

^{:refer xt.substrate.base-page/model-update :name demo-001-page-model-update}
(fact "page-model-update refreshes a model with new args"

  (notify/wait-on :dart
    (var node (substrate/node-create {"id" "node"}))
    (base-page/add-group-attach node
                                nil
                                "page"
                                {"greet"
                                 {"handler" (fn [context]
                                              (var args (. context ["args"]))
                                              (return (xt/x:cat "hello "
                                                                (xt/x:get-idx args 0))))
                                  "defaults" {"args" ["world"]}}})
    (-> (substrate/page-model-update node nil "page" "greet" {})
        (promise/x:promise-then
         (fn [_]
           (var model-result (base-page/model-ensure node nil "page" "greet"))
           (var model (xt/x:get-idx model-result 1))
           (repl/notify (event-model/get-current model nil))))))
  => "hello world"

  (notify/wait-on :dart
    (var node (substrate/node-create {"id" "node"}))
    (base-page/add-group-attach node
                                nil
                                "page"
                                {"greet"
                                 {"handler" (fn [context]
                                              (var args (. context ["args"]))
                                              (return (xt/x:cat "hello "
                                                                (xt/x:get-idx args 0))))
                                  "defaults" {"args" ["world"]}}})
    (-> (substrate/page-model-set-input node nil "page" "greet" {"data" ["substrate"]} {})
        (promise/x:promise-then
         (fn [_]
           (var model-result (base-page/model-ensure node nil "page" "greet"))
           (var model (xt/x:get-idx model-result 1))
           (repl/notify (event-model/get-current model nil))))))
  => "hello substrate")

^{:refer xt.substrate.base-page/add-group-attach :name demo-003-page-model-remote}
(fact "a page model handler can issue a request over a memory transport"

  (notify/wait-on :dart
    (var wire (transport-memory/memory-pair {"left_id" "client"
                                             "right_id" "server"}))
    (var server (substrate/node-create
                 {"id" "server"
                  "handlers"
                  {"demo/echo"
                   {"fn" (fn [space args request node]
                           (return {"echo" (xt/x:get-idx args 0)
                                    "server" (. node ["id"])}))
                    "meta" {"kind" "request"}}}}))
    (var client (substrate/node-create {"id" "client"}))
    (-> (promise/x:promise-all
         [(substrate/attach-transport
           client
           "server"
           (transport-memory/text-endpoint (. wire ["left"])))
          (substrate/attach-transport
           server
           "client"
           (transport-memory/text-endpoint (. wire ["right"])))])
        (promise/x:promise-then
         (fn [_]
           (base-page/add-group-attach client
                                       nil
                                       "page"
                                       {"echo"
                                        {"handler" (fn [context]
                                                     (var local-node (. context ["node"]))
                                                     (var space    (. context ["space"]))
                                                     (var args     (. context ["args"]))
                                                     (return
                                                      (substrate/request
                                                       local-node
                                                       (. space ["id"])
                                                       "demo/echo"
                                                       args
                                                       {"transport_id" "server"})))
                                         "defaults" {"args" ["ping"]}}})
           (return (substrate/page-model-update client nil "page" "echo" {}))))
        (promise/x:promise-then
         (fn [_]
           (var model-result (base-page/model-ensure client nil "page" "echo"))
           (var model (xt/x:get-idx model-result 1))
           (repl/notify (event-model/get-current model nil))))))
  => {"echo" "ping" "server" "server"})

^{:refer xt.substrate.base-page/add-group-attach :name demo-003-page-model-remote}
(fact "a page model handler can issue a request over a memory transport"

  (notify/wait-on :dart
    (var wire (transport-memory/memory-pair {"left_id" "client"
                                             "right_id" "server"}))
    (var server (substrate/node-create
                 {"id" "server"
                  "handlers"
                  {"demo/echo"
                   {"fn" (fn [space args request node]
                           (return {"echo" (xt/x:get-idx args 0)
                                    "server" (. node ["id"])}))
                    "meta" {"kind" "request"}}}}))
    (var client (substrate/node-create {"id" "client"}))
    (-> (promise/x:promise-all
         [(substrate/attach-transport
           client
           "server"
           (transport-memory/text-endpoint (. wire ["left"])))
          (substrate/attach-transport
           server
           "client"
           (transport-memory/text-endpoint (. wire ["right"])))])
        (promise/x:promise-then
         (fn [_]
           (base-page/add-group-attach client
                                       nil
                                       "page"
                                       {"echo"
                                        {"handler" (fn [context]
                                                     (var local-node (. context ["node"]))
                                                     (var space    (. context ["space"]))
                                                     (var args     (. context ["args"]))
                                                     (return
                                                      (substrate/request
                                                       local-node
                                                       (. space ["id"])
                                                       "demo/echo"
                                                       args
                                                       {"transport_id" "server"})))
                                         "defaults" {"args" ["ping"]}}})
           (return (substrate/page-model-update client nil "page" "echo" {}))))
        (promise/x:promise-then
         (fn [_]
           (var model-result (base-page/model-ensure client nil "page" "echo"))
           (var model (xt/x:get-idx model-result 1))
           (repl/notify (event-model/get-current model nil))))))
  => {"echo" "ping" "server" "server"})

^{:refer xt.substrate.base-page/remote-call :name demo-004-page-model-local-and-remote}
(fact "a model can have separate local and remote handlers"

  (notify/wait-on :dart
    (var node (substrate/node-create {"id" "node"}))
    (base-page/add-group-attach node
                                nil
                                "page"
                                {"both"
                                 {"handler" (fn [_]
                                              (return "local-value"))
                                  "pipeline" {"remote" {"handler" (fn [_]
                                                                    (return "remote-value"))}}
                                  "defaults" {"args" []}}})
    (-> (substrate/page-model-update node nil "page" "both" {})
        (promise/x:promise-then
         (fn [_]
           (return (base-page/remote-call node nil "page" "both" [] true))))
        (promise/x:promise-then
         (fn [_]
           (var model-result (base-page/model-ensure node nil "page" "both"))
           (var model (xt/x:get-idx model-result 1))
           (repl/notify (event-model/get-current model nil))))))
  => "remote-value"

  (notify/wait-on :dart
    (var node (substrate/node-create {"id" "node"}))
    (base-page/add-group-attach node
                                nil
                                "page"
                                {"both"
                                 {"handler" (fn [_]
                                              (return "local-value"))
                                  "pipeline" {"remote" {"handler" (fn [_]
                                                                    (return "remote-value"))}}
                                  "defaults" {"args" []}}})
    (-> (substrate/page-model-update node nil "page" "both" {})
        (promise/x:promise-then
         (fn [_]
           (var model-result (base-page/model-ensure node nil "page" "both"))
           (var model (xt/x:get-idx model-result 1))
           (repl/notify (event-model/get-current model nil))))))
  => "local-value")
