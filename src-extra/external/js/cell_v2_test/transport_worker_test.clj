(ns js.cell-v2.transport-worker-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.cell-v2 :as cell-v2]
             [js.cell-v2.event :as event]
             [js.cell-v2.protocol :as protocol]
             [js.cell-v2.transport-worker :as worker-transport]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell-v2.transport-worker/make-worker-transport :added "4.0" :unchecked true}
(fact "binds a worker channel for inbound call frames"
  (!.js
   (var sent [])
   (var listeners [])
   (var worker {})
   (k/set-key worker
              "postMessage"
              (fn [frame]
                (x:arr-push sent frame)
                (return frame)))
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
   (worker-transport/make-worker-transport worker
                                           {:id "peer"
                                            :system system
                                            :forwardAll false})
   ((. listeners [0]) {:data (protocol/call "c1"
                                            "local/echo"
                                            {:input ["hello"]}
                                            nil)})
   {"listener_count" (k/len listeners)
    "sent" sent})
  => {"listener_count" 1
      "sent" [{"op" "result"
               "id" "c1"
               "status" "ok"
               "ref" nil
               "body" ["echo" "hello"]
               "meta" {}}]})

^{:refer js.cell-v2.transport-worker/attach-worker :added "4.0" :unchecked true}
(fact "forwards subscribed signals through the worker channel"
  (!.js
   (var sent [])
   (var listeners [])
   (var worker {})
   (k/set-key worker
              "postMessage"
              (fn [frame]
                (x:arr-push sent frame)
                (return frame)))
   (k/set-key worker
              "addEventListener"
              (fn [name handler capture]
                (x:arr-push listeners handler)
                (return true)))
   (var system (cell-v2/make-system {}))
   (worker-transport/make-worker-transport worker
                                           {:id "peer"
                                            :system system
                                            :forwardAll false})
   ((. listeners [0]) {:data (protocol/subscribe "s1"
                                                 event/EV_REMOTE
                                                 nil)})
   (cell-v2/emit-signal system
                        event/EV_REMOTE
                        {"request_id" "r1"}
                        nil
                        "ok")
   ((. listeners [0]) {:data (protocol/unsubscribe "s2"
                                                   event/EV_REMOTE
                                                   nil)})
   (cell-v2/emit-signal system
                        event/EV_REMOTE
                        {"request_id" "r2"}
                        nil
                        "ok")
   {"sent" sent})
  => {"sent" [{"op" "result"
               "id" "s1"
               "status" "ok"
               "ref" nil
               "body" {"signal" "cell/::REMOTE"
                       "subscribed" true}
               "meta" {}}
              {"op" "emit"
               "id" "emit-1"
               "signal" "cell/::REMOTE"
               "status" "ok"
               "ref" nil
               "body" {"request_id" "r1"}
               "meta" {}}
              {"op" "result"
               "id" "s2"
               "status" "ok"
               "ref" nil
               "body" {"signal" "cell/::REMOTE"
                       "subscribed" false}
               "meta" {}}]})
