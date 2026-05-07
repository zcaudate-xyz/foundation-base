(ns xt.db.websocket.supabase-client-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.db.websocket.supabase-client :as realtime]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.websocket.supabase-client/endpoint-url :added "4.1.3"}
(fact "builds websocket endpoints with apikey and vsn params"

  (!.js
   (var client
        (realtime/create-client
         "https://demo.supabase.co/realtime/v1"
         {"apikey" "anon-key"
            "params" {"log_level" "debug"}
            "transport" (fn [_]
                         (return {"send" (fn [_msg] (return true))
                                 "close" (fn [] (return true))
                                 "addEventListener" (fn [_event _handler]
                                                      (return true))}))}))
   (var url (. client (endpointURL)))
   [(xt/x:str-starts-with url "wss://demo.supabase.co/realtime/v1/websocket?")
    (< -1 (xt/x:str-index-of url "log_level=debug"))
    (< -1 (xt/x:str-index-of url "apikey=anon-key"))
    (< -1 (xt/x:str-index-of url "vsn=1.0.0"))])
  => [true true true true])

^{:refer xt.db.websocket.supabase-client/create-client :added "4.1.3"}
(fact "buffers join messages until open and dispatches postgres change payloads"

  (!.js
   (var handlers {})
   (var sent [])
   (var current nil)
   (var status-log [])
   (var payload-log [])
   (var Transport
        (fn [url]
          (var ws {"url" url
                   "send" (fn [msg]
                            (xt/x:arr-push sent msg)
                            (return true))
                   "close" (fn [] (return true))
                   "addEventListener" (fn [event handler]
                                        (xt/x:set-key handlers event handler)
                                        (return true))})
          (xt/x:set-key ws "emit"
                        (fn [event payload]
                          ((xt/x:get-key handlers event) payload)))
          (:= current ws)
          (return ws)))
   (var client
        (realtime/create-client
         "wss://demo.supabase.co/realtime/v1"
         {"apikey" "anon-key"
            "transport" Transport
            "schedule-interval" (fn [_handler _ms] (return nil))
            "clear-interval" (fn [_id] (return nil))}))
   (var channel (. client (channel "public:messages" {"config" {"broadcast" {"self" true}}})))
   (. channel (on "postgres_changes"
                  {"event" "*"
                   "schema" "public"
                   "table" "messages"}
                  (fn [payload _ref]
                    (xt/x:arr-push payload-log payload))))
    (. channel (subscribe (fn [status _err]
                            (xt/x:arr-push status-log status))))
    (. current (emit "open" {}))
    (var join-msg (realtime/default-decode (xt/x:first sent)))
    (. current (emit "message"
                     {"data"
                      (realtime/default-encode
                       {"topic" "realtime:public:messages"
                        "event" "phx_reply"
                        "payload" {"status" "ok"
                                  "response" {"postgres_changes"
                                              [{"id" "bind-1"
                                                "event" "*"
                                                "schema" "public"
                                                "table" "messages"}]}}
                        "ref" (. join-msg ["ref"])})}))
    (. current (emit "message"
                     {"data"
                      (realtime/default-encode
                       {"topic" "realtime:public:messages"
                        "event" "postgres_changes"
                        "payload" {"ids" ["bind-1"]
                                  "data" {"schema" "public"
                                          "table" "messages"
                                          "commit_timestamp" "2026-05-06T00:00:00Z"
                                          "type" "INSERT"
                                          "record" {"id" 1
                                                    "body" "hello"}
                                          "old_record" {}}}
                        "ref" nil})}))
   [(. join-msg ["topic"])
    (. join-msg ["event"])
    (. (. (. join-msg ["payload"]) ["config"]) ["postgres_changes"] [0] ["table"])
    status-log
    (. (xt/x:first payload-log) ["eventType"])
    (. (. (xt/x:first payload-log) ["new"]) ["body"])])
  => ["realtime:public:messages"
      "phx_join"
      "messages"
      ["SUBSCRIBED"]
      "INSERT"
      "hello"])

^{:refer xt.db.websocket.supabase-client/set-auth :added "4.1.3"}
(fact "sends auth refresh and leave frames for joined channels"

  (!.js
   (var handlers {})
   (var sent [])
   (var current nil)
   (var Transport
        (fn [_url]
          (var ws {"send" (fn [msg]
                            (xt/x:arr-push sent msg)
                            (return true))
                   "close" (fn [] (return true))
                   "addEventListener" (fn [event handler]
                                        (xt/x:set-key handlers event handler)
                                        (return true))})
          (xt/x:set-key ws "emit"
                        (fn [event payload]
                          ((xt/x:get-key handlers event) payload)))
          (:= current ws)
          (return ws)))
   (var client
        (realtime/create-client
         "wss://demo.supabase.co/realtime/v1"
         {"apikey" "anon-key"
            "transport" Transport
           "schedule-interval" (fn [_handler _ms] (return nil))
           "clear-interval" (fn [_id] (return nil))}))
    (var channel (. client (channel "public:rooms" {})))
    (. channel (subscribe nil))
    (. current (emit "open" {}))
     (var join-msg (realtime/default-decode (xt/x:first sent)))
    (. current (emit "message"
                     {"data"
                      (realtime/default-encode
                        {"topic" "realtime:public:rooms"
                        "event" "phx_reply"
                        "payload" {"status" "ok"
                                   "response" {"postgres_changes" []}}
                        "ref" (. join-msg ["ref"])})}))
    (. client (setAuth "token-2"))
    (. client (removeChannel channel))
     (var auth-msg (realtime/default-decode (xt/x:get-idx sent 1)))
     (var leave-msg (realtime/default-decode (xt/x:get-idx sent 2)))
    [(. auth-msg ["event"])
     (. (. auth-msg ["payload"]) ["access_token"])
     (. leave-msg ["event"])
     (xt/x:len (. client (getChannels)))])
  => ["access_token"
      "token-2"
      "phx_leave"
      0])
