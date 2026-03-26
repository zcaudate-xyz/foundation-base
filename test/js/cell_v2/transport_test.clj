(ns js.cell-v2.transport-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.cell-v2 :as cell-v2]
             [js.cell-v2.event :as event]
             [js.cell-v2.protocol :as protocol]
             [js.cell-v2.transport :as transport]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell-v2.transport/receive-frame :added "4.0" :unchecked true}
(fact "handles inbound call and result frames"
  (!.js
   (var sent [])
   (var resolved [])
   (var rejected [])
   (var tx (transport/make-transport nil
                                     {:id "peer"
                                      :send (fn [frame]
                                              (x:arr-push sent frame)
                                              (return frame))}))
   (var system (cell-v2/make-system {}))
   (transport/bind-system tx system {:forwardAll false})
   (cell-v2/register-route system
                           "local/echo"
                           (fn [ctx arg]
                             (return ["echo" arg]))
                           {:kind "query"})
   (transport/receive-frame tx
                            (protocol/call "c1"
                                           "local/echo"
                                           {:input ["hello"]}
                                           nil)
                            nil)
   (:= (. tx ["pending"] ["p1"])
       {:resolve (fn [body frame]
                   (x:arr-push resolved body))
        :reject (fn [frame]
                  (x:arr-push rejected frame))})
   (transport/receive-frame tx
                            (protocol/result "p1"
                                             "ok"
                                             {:pong true}
                                             nil
                                             nil)
                            nil)
   {"sent" sent
    "resolved" resolved
    "rejected" rejected
    "pending" (k/obj-keys (. tx ["pending"]))})
  => {"sent" [{"op" "result"
               "id" "c1"
               "status" "ok"
               "ref" nil
               "body" ["echo" "hello"]
               "meta" {}}]
      "resolved" [{"pong" true}]
      "rejected" []
      "pending" []})

^{:refer js.cell-v2.transport/call :added "4.0" :unchecked true}
(fact "tracks async task frames by task ref"
  (!.js
   (var sent [])
   (var progress [])
   (var resolved [])
   (var rejected [])
   (var tx (transport/make-transport nil
                                     {:id "peer"
                                      :send (fn [frame]
                                              (x:arr-push sent frame)
                                              (return frame))}))
   (transport/call tx
                   "remote/http"
                   {:input {:url "/ping"}}
                   nil
                   {:task (fn [frame entry inner]
                            (x:arr-push progress
                                        [(k/get-key frame "status")
                                         (k/get-key frame "ref")
                                         (k/get-key frame "body")]))})
   (var call-id (. sent [0] ["id"]))
   (k/set-key (. tx ["pending"] [call-id])
              "resolve"
              (fn [body frame]
                (x:arr-push resolved body)))
   (k/set-key (. tx ["pending"] [call-id])
              "reject"
              (fn [frame]
                (x:arr-push rejected [(k/get-key frame "status")
                                      (k/get-key frame "ref")])))
   (transport/receive-frame tx
                            (protocol/task call-id
                                           "task-1"
                                           "accepted"
                                           {"progress" 0}
                                           nil)
                            nil)
   (transport/receive-frame tx
                            (protocol/task "u1"
                                           "task-1"
                                           "pending"
                                           {"progress" 0.5}
                                           nil)
                            nil)
   (var active-before (transport/list-tasks tx))
   (transport/receive-frame tx
                            (protocol/task "u2"
                                           "task-1"
                                           "ok"
                                           {"data" true}
                                           nil)
                            nil)
   {"progress" progress
    "resolved" resolved
    "rejected" rejected
    "active_before" active-before
    "active_after" (transport/list-tasks tx)
    "pending" (k/obj-keys (. tx ["pending"]))})
  => {"progress" [["accepted" "task-1" {"progress" 0}]
                  ["pending" "task-1" {"progress" 0.5}]
                  ["ok" "task-1" {"data" true}]]
      "resolved" [{"data" true}]
      "rejected" []
      "active_before" ["task-1"]
      "active_after" []
      "pending" []})

^{:refer js.cell-v2.transport/handle-task :added "4.0" :unchecked true}
(fact "rejects terminal error task frames"
  (!.js
   (var rejected [])
   (var tx (transport/make-transport nil {:id "peer"}))
   (:= (. tx ["pending"] ["c9"])
       {:callId "c9"
        :resolve (fn [body frame] body)
        :reject (fn [frame]
                  (x:arr-push rejected [(k/get-key frame "status")
                                        (k/get-key frame "ref")]))})
   (transport/receive-frame tx
                            (protocol/task "c9"
                                           "task-9"
                                           "error"
                                           {"message" "boom"}
                                           nil)
                            nil)
   {"rejected" rejected
    "tasks" (transport/list-tasks tx)
    "pending" (k/obj-keys (. tx ["pending"]))})
  => {"rejected" [["error" "task-9"]]
      "tasks" []
      "pending" []})

^{:refer js.cell-v2.transport/bind-system :added "4.0" :unchecked true}
(fact "forwards subscribed signals and respects unsubscribe"
  (!.js
   (var sent [])
   (var tx (transport/make-transport nil
                                     {:id "peer"
                                      :send (fn [frame]
                                              (x:arr-push sent frame)
                                              (return frame))}))
   (var system (cell-v2/make-system {}))
   (transport/bind-system tx system {:forwardAll false})
   (transport/receive-frame tx
                            (protocol/subscribe "s1"
                                                event/EV_REMOTE
                                                nil)
                            nil)
   (cell-v2/emit-signal system
                        event/EV_REMOTE
                        {"request_id" "r1"}
                        {"source" "test"}
                        "ok")
   (transport/receive-frame tx
                            (protocol/unsubscribe "s2"
                                                  event/EV_REMOTE
                                                  nil)
                            nil)
   (cell-v2/emit-signal system
                        event/EV_REMOTE
                        {"request_id" "r2"}
                        nil
                        "ok")
   {"sent" sent
    "subscriptions" (transport/list-subscriptions tx)})
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
               "meta" {"source" "test"}}
              {"op" "result"
               "id" "s2"
               "status" "ok"
               "ref" nil
               "body" {"signal" "cell/::REMOTE"
                       "subscribed" false}
               "meta" {}}]
      "subscriptions" []})
