(ns js.cell-v2.control-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [js.cell-v2 :as cell-v2]
             [js.cell-v2.control :as control]
             [js.cell-v2.event :as event]
             [js.cell-v2.transport-legacy :as legacy]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell-v2.control/register-control-routes :added "4.0" :unchecked true}
(fact "registers the v1-compatible control surface on cell-v2"
  (!.js
   (var system (cell-v2/make-system {}))
   (var seen [])
   (cell-v2/register-route system
                           "local/custom"
                           (fn [ctx arg]
                             (return ["custom" arg]))
                           {:args ["arg"]
                            :async false
                            :kind "query"})
   (control/register-control-routes system)
   (cell-v2/add-event-listener
    system
    "seen"
    true
    (fn [input signal bus]
      (x:arr-push seen [signal
                        (k/get-key input "body")])))
   (var ping-async (j/<! (cell-v2/call-action system "@/ping-async" {:input [5]} nil)))
   (var echo-async (j/<! (cell-v2/call-action system "@/echo-async" {:input ["hello" 5]} nil)))
   (var error-async (j/<! (cell-v2/call-action system "@/error-async" {:input [5]} nil)))
   {"route_list" (cell-v2/call-action system "@/route-list" {} nil)
    "route_entry" (cell-v2/call-action system "@/route-entry"
                                       {:input ["@/echo"]}
                                       nil)
    "eval_status_0" (cell-v2/call-action system "@/eval-status" {} nil)
    "final_status_0" (cell-v2/call-action system "@/final-status" {} nil)
    "trigger" (cell-v2/call-action system "@/trigger"
                                   {:input ["stream" "hello" "ok" {"value" 1}]}
                                   nil)
    "eval_disable" (cell-v2/call-action system "@/eval-disable" {} nil)
    "eval_status_1" (cell-v2/call-action system "@/eval-status" {} nil)
    "eval_disabled" (cell-v2/call-action system "@/eval"
                                         {:input ["1 + 1"]}
                                         nil)
    "eval_enable" (cell-v2/call-action system "@/eval-enable"
                                       {:input [true]}
                                       nil)
    "eval_ok" (cell-v2/call-action system "@/eval"
                                   {:input ["1 + 1"]}
                                   nil)
    "final_set" (cell-v2/call-action system "@/final-set"
                                     {:input [true]}
                                     nil)
    "final_status_1" (cell-v2/call-action system "@/final-status" {} nil)
    "final_error" (cell-v2/call-action system "@/eval-enable" {} nil)
    "ping_async" ping-async
    "echo_async" echo-async
    "error_async" error-async
    "seen" seen})
  => {"route_list" ["local/custom"
                    "@/trigger"
                    "@/trigger-async"
                    "@/final-set"
                    "@/final-status"
                    "@/eval-enable"
                    "@/eval-disable"
                    "@/eval-status"
                    "@/route-list"
                    "@/route-entry"
                    "@/ping"
                    "@/ping-async"
                    "@/echo"
                    "@/echo-async"
                    "@/error"
                    "@/error-async"]
      "route_entry" {"args" ["arg"]
                     "async" false}
      "eval_status_0" true
      "final_status_0" nil
      "trigger" {"op" "stream"
                 "topic" "hello"
                 "status" "ok"
                 "body" {"value" 1}}
      "eval_disable" {"eval" false}
      "eval_status_1" false
      "eval_disabled" {"status" "error"
                       "body" "Not enabled - EVAL"}
      "eval_enable" {"eval" true}
      "eval_ok" 2
      "final_set" {"eval" true
                   "final" true}
      "final_status_1" true
      "final_error" {"status" "error"
                     "body" "Worker State is Final."}
      "ping_async" ["pong" integer?]
      "echo_async" ["hello" integer?]
      "error_async" {"status" "error"
                     "body" ["error" integer?]}
      "seen" [["hello" {"value" 1}]
              ["@/::STATE" {"eval" false}]
              ["@/::STATE" {"eval" true}]]})

^{:refer js.cell-v2.control/register-control-routes :added "4.0" :unchecked true}
(fact "bridges control routes through the legacy worker format"
  (!.js
   (var sent [])
   (var listeners [])
   (var worker {})
   (k/set-key worker
              "postMessage"
              (fn [message]
                (x:arr-push sent message)
                (return message)))
   (k/set-key worker
              "addEventListener"
              (fn [name handler capture]
                (x:arr-push listeners handler)
                (return true)))
   (var system (cell-v2/make-system {}))
   (control/register-control-routes system)
   (legacy/make-legacy-worker-transport worker
                                        {:id "legacy"
                                         :system system
                                         :forwardAll false})
   ((. listeners [0]) {:data {:op "eval"
                              :id "e1"
                              :body "1 + 1"}})
   ((. listeners [0]) {:data {:op "route"
                              :id "r1"
                              :route "@/eval-disable"
                              :body []}})
   ((. listeners [0]) {:data {:op "route"
                              :id "r2"
                              :route "@/trigger"
                              :body ["stream" "hello" "ok" "world"]}})
   sent)
  => [{"op" "eval"
       "id" "e1"
       "status" "ok"
       "body" (k/json-encode {:type "data"
                              :value 2})}
      {"op" "stream"
       "id" "emit-1"
       "topic" "@/::STATE"
       "status" "ok"
       "body" {"eval" false}}
      {"op" "route"
       "id" "r1"
       "status" "ok"
       "body" {"eval" false}}
      {"op" "stream"
       "id" "emit-2"
       "topic" "hello"
       "status" "ok"
       "body" "world"}
      {"op" "route"
       "id" "r2"
       "status" "ok"
       "body" {"op" "stream"
               "topic" "hello"
               "status" "ok"
               "body" "world"}}])
