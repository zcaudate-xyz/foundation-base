(ns js.cell-v2.transport-http-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.cell-v2.protocol :as protocol]
             [js.cell-v2.transport :as transport]
             [js.cell-v2.transport-http :as http-transport]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell-v2.transport-http/make-http-transport :added "4.0" :unchecked true}
(fact "applies request-reply http responses back into pending calls"
  (!.js
   (var seen [])
   (var resolved [])
   (var tx (http-transport/make-http-transport
            (fn [payload inner]
              (x:arr-push seen payload)
              (return (protocol/result "c1"
                                       "ok"
                                       {"pong" true}
                                       nil
                                       nil)))
            {}))
   (:= (. tx ["pending"] ["c1"])
       {:callId "c1"
        :resolve (fn [body frame]
                   (x:arr-push resolved body))
        :reject (fn [frame]
                  frame)})
   (transport/send-frame tx
                         (protocol/call "c1"
                                        "remote/http"
                                        {:input {:url "/ping"}}
                                        nil))
   {"seen" seen
    "resolved" resolved
    "pending" (k/obj-keys (. tx ["pending"]))})
  => {"seen" [{"op" "call"
               "id" "c1"
               "action" "remote/http"
               "body" {"input" {"url" "/ping"}}
               "meta" {}}]
      "resolved" [{"pong" true}]
      "pending" []})

^{:refer js.cell-v2.transport-http/apply-response :added "4.0" :unchecked true}
(fact "decodes string http responses"
  (!.js
   (var resolved [])
   (var tx (http-transport/make-http-transport
            (fn [payload inner]
              (return (k/json-encode
                       (protocol/result "c2"
                                        "ok"
                                        {"pong" true}
                                        nil
                                        nil))))
            {}))
   (:= (. tx ["pending"] ["c2"])
       {:callId "c2"
        :resolve (fn [body frame]
                   (x:arr-push resolved body))
        :reject (fn [frame]
                  frame)})
   (transport/send-frame tx
                         (protocol/call "c2"
                                        "remote/http"
                                        {:input {:url "/pong"}}
                                        nil))
   {"resolved" resolved
    "pending" (k/obj-keys (. tx ["pending"]))})
  => {"resolved" [{"pong" true}]
      "pending" []})
