(ns js.cell-v2.transport-socket-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.cell-v2 :as cell-v2]
             [js.cell-v2.event :as event]
             [js.cell-v2.protocol :as protocol]
             [js.cell-v2.transport-socket :as socket-transport]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell-v2.transport-socket/make-socket-transport :added "4.0" :unchecked true}
(fact "encodes outbound frames and decodes inbound socket messages"
  (!.js
   (var sent [])
   (var listeners [])
   (var socket {})
   (k/set-key socket
              "send"
              (fn [payload]
                (x:arr-push sent payload)
                (return payload)))
   (k/set-key socket
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
   (socket-transport/make-socket-transport socket
                                           {:id "peer"
                                            :system system
                                            :forwardAll false})
   ((. listeners [0]) {:data (k/json-encode
                              (protocol/call "c1"
                                             "local/echo"
                                             {:input ["hello"]}
                                             nil))})
   {"listener_count" (k/len listeners)
    "sent" [(k/json-decode (. sent [0]))]})
  => {"listener_count" 1
      "sent" [{"op" "result"
               "id" "c1"
               "status" "ok"
               "ref" nil
               "body" ["echo" "hello"]
               "meta" {}}]})

^{:refer js.cell-v2.transport-socket/attach-socket :added "4.0" :unchecked true}
(fact "forwards subscribed signals over the socket"
  (!.js
   (var sent [])
   (var listeners [])
   (var socket {})
   (k/set-key socket
              "send"
              (fn [payload]
                (x:arr-push sent payload)
                (return payload)))
   (k/set-key socket
              "addEventListener"
              (fn [name handler capture]
                (x:arr-push listeners handler)
                (return true)))
   (var system (cell-v2/make-system {}))
   (socket-transport/make-socket-transport socket
                                           {:id "peer"
                                            :system system
                                            :forwardAll false})
   ((. listeners [0]) {:data (k/json-encode
                              (protocol/subscribe "s1"
                                                  event/EV_REMOTE
                                                  nil))})
   (cell-v2/emit-signal system
                        event/EV_REMOTE
                        {"request_id" "r1"}
                        nil
                        "ok")
   {"sent" [(k/json-decode (. sent [0]))
            (k/json-decode (. sent [1]))]})
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
               "meta" {}}]})
