(ns js.cell-v2.transport-legacy-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.cell-v2 :as cell-v2]
             [js.cell-v2.event :as event]
             [js.cell-v2.protocol :as protocol]
             [js.cell-v2.transport :as transport]
             [js.cell-v2.transport-legacy :as legacy]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell-v2.transport-legacy/legacy->frame :added "4.0" :unchecked true}
(fact "translates legacy messages to protocol frames"
  (!.js
   (var tx (transport/make-transport nil {:id "legacy"}))
   {"route_call" (legacy/legacy->frame tx
                                       {:op "route"
                                        :id "c1"
                                        :route "local/echo"
                                        :body ["hello"]})
    "route_result" (legacy/legacy->frame tx
                                         {:op "route"
                                          :id "c1"
                                          :status "ok"
                                          :body ["echo" "hello"]})
    "stream" (legacy/legacy->frame tx
                                   {:op "stream"
                                    :id "s1"
                                    :topic event/EV_REMOTE
                                    :status "ok"
                                    :body {"request_id" "r1"}})
    "eval_call" (legacy/legacy->frame tx
                                      {:op "eval"
                                       :id "e1"
                                       :body "1 + 1"})
    "eval_result" (legacy/legacy->frame tx
                                        {:op "eval"
                                         :id "e1"
                                         :status "ok"
                                         :body (k/json-encode
                                                {:type "data"
                                                 :value 2})})})
  => {"route_call" {"op" "call"
                    "id" "c1"
                    "action" "local/echo"
                    "body" {"input" ["hello"]}
                    "meta" {"legacyOp" "route"}}
      "route_result" {"op" "result"
                      "id" "c1"
                      "status" "ok"
                      "ref" nil
                      "body" ["echo" "hello"]
                      "meta" {}}
      "stream" {"op" "emit"
                "id" "s1"
                "signal" "cell/::REMOTE"
                "status" "ok"
                "ref" nil
                "body" {"request_id" "r1"}
                "meta" {}}
      "eval_call" {"op" "call"
                   "id" "e1"
                   "action" "@/eval"
                   "body" {"input" ["1 + 1"]}
                   "meta" {"legacyOp" "eval"
                           "annex" "debug"}}
      "eval_result" {"op" "result"
                     "id" "e1"
                     "status" "ok"
                     "ref" nil
                     "body" 2
                     "meta" {}}})

^{:refer js.cell-v2.transport-legacy/frame->legacy :added "4.0" :unchecked true}
(fact "translates protocol frames back to legacy messages"
  (!.js
   (var tx (transport/make-transport nil {:id "legacy"}))
   (legacy/remember-kind tx "c1" "route")
   (legacy/remember-kind tx "e1" "eval")
   {"route_call" (legacy/frame->legacy tx
                                       (protocol/call "c2"
                                                      "local/echo"
                                                      {:input ["hello"]}
                                                      nil))
    "route_result" (legacy/frame->legacy tx
                                         (protocol/result "c1"
                                                          "ok"
                                                          ["echo" "hello"]
                                                          nil
                                                          nil))
    "emit" (legacy/frame->legacy tx
                                 (protocol/emit "s1"
                                                event/EV_REMOTE
                                                "ok"
                                                {"request_id" "r1"}
                                                nil
                                                nil))
    "task" (legacy/frame->legacy tx
                                 (protocol/task "u1"
                                                "task-1"
                                                "pending"
                                                {"progress" 0.5}
                                                nil))
    "eval_result" (legacy/frame->legacy tx
                                        (protocol/result "e1"
                                                         "ok"
                                                         2
                                                         nil
                                                         nil))})
  => {"route_call" {"op" "route"
                    "id" "c2"
                    "route" "local/echo"
                    "body" ["hello"]}
      "route_result" {"op" "route"
                      "id" "c1"
                      "status" "ok"
                      "body" ["echo" "hello"]}
      "emit" {"op" "stream"
              "id" "s1"
              "topic" "cell/::REMOTE"
              "status" "ok"
              "body" {"request_id" "r1"}}
      "task" {"op" "stream"
              "id" "u1"
              "topic" "cell/::TASK"
              "status" "pending"
              "body" {"ref" "task-1"
                      "body" {"progress" 0.5}
                      "meta" {}}}
      "eval_result" {"op" "eval"
                     "id" "e1"
                     "status" "ok"
                     "body" (k/json-encode
                             {:type "data"
                              :value 2})}})

^{:refer js.cell-v2.transport-legacy/make-legacy-worker-transport :added "4.0" :unchecked true}
(fact "bridges legacy worker route and stream messages"
  (!.js
   (var sent [])
   (var listeners [])
   (var seen [])
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
   (cell-v2/register-route system
                           "local/echo"
                           (fn [ctx arg]
                             (return ["echo" arg]))
                           {:kind "query"})
   (cell-v2/add-event-listener
    system
    "seen"
    true
    (fn [input signal bus]
      (x:arr-push seen [signal
                        (k/get-key input "body")])))
   (legacy/make-legacy-worker-transport worker
                                        {:id "legacy"
                                         :system system
                                         :forwardAll false})
   ((. listeners [0]) {:data {:op "route"
                              :id "c1"
                              :route "local/echo"
                              :body ["hello"]}})
   ((. listeners [0]) {:data {:op "stream"
                              :id "s1"
                              :topic event/EV_REMOTE
                              :status "ok"
                              :body {"request_id" "r1"}}})
   {"sent" sent
    "seen" seen})
  => {"sent" [{"op" "route"
               "id" "c1"
               "status" "ok"
               "body" ["echo" "hello"]}]
      "seen" [["cell/::REMOTE" {"request_id" "r1"}]]})
