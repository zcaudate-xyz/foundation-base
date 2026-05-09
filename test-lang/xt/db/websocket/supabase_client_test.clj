(ns xt.db.websocket.supabase-client-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-websocket :as ws]
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


^{:refer xt.db.websocket.supabase-client/client? :added "4.1"}
(fact "recognizes realtime client objects"

  (!.js
   [(realtime/client? {"::" "supabase.realtime.client"})
    (realtime/client? {"::" "supabase.realtime.channel"})
    (realtime/client? nil)])
  => [true false false])

^{:refer xt.db.websocket.supabase-client/channel? :added "4.1"}
(fact "recognizes realtime channel objects"

  (!.js
   [(realtime/channel? {"::" "supabase.realtime.channel"})
    (realtime/channel? {"::" "supabase.realtime.client"})
    (realtime/channel? nil)])
  => [true false false])

^{:refer xt.db.websocket.supabase-client/normalize-topic :added "4.1"}
(fact "adds the realtime prefix once"

  (!.js
   [(realtime/normalize-topic "public:messages")
    (realtime/normalize-topic "realtime:public:messages")])
  => ["realtime:public:messages"
      "realtime:public:messages"])

^{:refer xt.db.websocket.supabase-client/endpoint-base-url :added "4.1"}
(fact "normalizes http endpoints into websocket urls"

  (!.js
   [(realtime/endpoint-base-url "https://demo.supabase.co/realtime/v1")
    (realtime/endpoint-base-url "http://demo.supabase.co/realtime/v1/")
    (realtime/endpoint-base-url "wss://demo.supabase.co/realtime/v1/websocket")])
  => ["wss://demo.supabase.co/realtime/v1/websocket"
      "ws://demo.supabase.co/realtime/v1/websocket"
      "wss://demo.supabase.co/realtime/v1/websocket"])

^{:refer xt.db.websocket.supabase-client/next-ref :added "4.1"}
(fact "increments and stringifies the outbound ref counter"

  (!.js
   (var client {"ref" 0})
   [(realtime/next-ref client)
    (realtime/next-ref client)
    (. client ["ref"])])
  => ["1"
      "2"
      2])

^{:refer xt.db.websocket.supabase-client/make-message :added "4.1"}
(fact "creates phoenix message envelopes with optional join refs"

  (!.js
   [(realtime/make-message "topic" "event" {"id" 1} "1" nil)
    (realtime/make-message "topic" "event" {} "2" "1")])
  => [{"topic" "topic"
       "event" "event"
       "payload" {"id" 1}
       "ref" "1"}
      {"topic" "topic"
       "event" "event"
       "payload" {}
       "ref" "2"
       "join_ref" "1"}])

^{:refer xt.db.websocket.supabase-client/find-channel :added "4.1"}
(fact "finds channels by normalized topic"

  (!.js
   (var channel {"topic" "realtime:public:messages"})
   [(realtime/find-channel {"channels" [channel]}
                           "realtime:public:messages")
    (realtime/find-channel {"channels" [channel]}
                           "realtime:public:rooms")])
  => [{"topic" "realtime:public:messages"}
      nil])

^{:refer xt.db.websocket.supabase-client/send-now :added "4.1"}
(fact "encodes and sends websocket frames immediately"

  (!.js
   (var sent [])
   (var client
        {"conn"
         {"send" (fn [payload]
                    (xt/x:arr-push sent payload)
                    (return true))}
         "encode"
         (fn [msg]
           (return (xt/x:cat "encoded:" (. msg ["event"]))))})
   (realtime/send-now client {"event" "phx_join"})
   sent)
  => ["encoded:phx_join"])

^{:refer xt.db.websocket.supabase-client/flush-send-buffer :added "4.1"}
(fact "drains buffered messages through send-now"

  (!.js
   (var sent [])
   (var client {"conn" {"send" (fn [payload]
                                 (xt/x:arr-push sent payload)
                                 (return true))}
                "encode" (fn [msg]
                           (return (. msg ["event"])))
                "send-buffer" [{"event" "one"}
                               {"event" "two"}]})
   [(realtime/flush-send-buffer client)
    (. client ["send-buffer"])
    sent])
  => [true
      []
      ["one" "two"]])

^{:refer xt.db.websocket.supabase-client/push :added "4.1"}
(fact "sends immediately when open and buffers while disconnected"

  (!.js
   (var sent [])
   (var open-client {"state" "open"
                     "conn" {"send" (fn [payload]
                                       (xt/x:arr-push sent payload)
                                       (return true))}
                     "encode" (fn [msg]
                                (return (. msg ["event"])))
                     "send-buffer" []})
   (var closed-client {"state" "closed"
                       "send-buffer" []})
   [(realtime/push open-client {"event" "open"})
    sent
    (realtime/push closed-client {"event" "closed"})
    (. closed-client ["send-buffer"])])
  => [{"event" "open"}
      ["open"]
      {"event" "closed"}
      [{"event" "closed"}]])

^{:refer xt.db.websocket.supabase-client/stop-heartbeat :added "4.1"}
(fact "clears the active heartbeat timer"

  (!.js
   (var cleared nil)
   (var client {"heartbeat-timer" "timer-1"
                "clear-interval" (fn [id]
                                   (:= cleared id)
                                   (return true))})
   [(realtime/stop-heartbeat client)
    cleared
    (. client ["heartbeat-timer"])])
  => [nil
      "timer-1"
      nil])

^{:refer xt.db.websocket.supabase-client/heartbeat :added "4.1"}
(fact "sends heartbeat frames once per open interval"

  (!.js
   (var sent [])
   (var client {"state" "open"
                "ref" 0
                "pending-heartbeat-ref" nil
                "send-buffer" []
                "conn" {"send" (fn [payload]
                                  (xt/x:arr-push sent payload)
                                  (return true))}
                "encode" (fn [msg] (return msg))})
   (realtime/heartbeat client)
   [(. client ["pending-heartbeat-ref"])
    (. client ["heartbeat-status"])
    (. (xt/x:first sent) ["event"])
    (. (xt/x:first sent) ["ref"])])
  => ["1"
      "sent"
      "heartbeat"
      "1"])

^{:refer xt.db.websocket.supabase-client/start-heartbeat :added "4.1"}
(fact "restarts the timer and schedules periodic heartbeats"

  (!.js
   (var cleared nil)
   (var scheduled-ms nil)
   (var client {"heartbeat-timer" "old-timer"
                "clear-interval" (fn [id]
                                   (:= cleared id)
                                   (return true))
                "schedule-interval" (fn [_handler ms]
                                      (:= scheduled-ms ms)
                                      (return "new-timer"))})
   [(realtime/start-heartbeat client)
    cleared
    scheduled-ms
    (. client ["heartbeat-timer"])])
  => [true
      "old-timer"
      25000
      "new-timer"])

^{:refer xt.db.websocket.supabase-client/enrich-postgres-payload :added "4.1"}
(fact "normalizes raw postgres_changes payloads"

  (!.js
   [(realtime/enrich-postgres-payload
     {"ids" ["bind-1"]
      "data" {"schema" "public"
              "table" "messages"
              "commit_timestamp" "2026-05-06T00:00:00Z"
              "type" "INSERT"
              "record" {"id" 1}
              "old_record" {}}})
    (realtime/enrich-postgres-payload
     {"eventType" "DELETE"
      "table" "messages"})])
  => [{"ids" ["bind-1"]
       "schema" "public"
       "table" "messages"
       "commit_timestamp" "2026-05-06T00:00:00Z"
       "errors" []
       "eventType" "INSERT"
       "new" {"id" 1}
       "old" {}}
      {"eventType" "DELETE"
       "table" "messages"}])

^{:refer xt.db.websocket.supabase-client/binding-matches? :added "4.1"}
(fact "matches postgres bindings by id event schema and table"

  (!.js
   [(realtime/binding-matches?
     {"type" "postgres_changes"
      "id" "bind-1"
      "filter" {"event" "*"
                 "schema" "public"
                 "table" "messages"}}
     "postgres_changes"
     {"ids" ["bind-1"]
      "data" {"schema" "public"
              "table" "messages"
              "type" "INSERT"
              "record" {"id" 1}
              "old_record" {}}})
    (realtime/binding-matches?
     {"type" "broadcast"
      "filter" {"event" "sync"}}
     "broadcast"
     {"event" "sync"})
    (realtime/binding-matches?
     {"type" "postgres_changes"
      "filter" {"event" "DELETE"
                 "schema" "public"
                 "table" "messages"}}
     "postgres_changes"
     {"data" {"schema" "public"
              "table" "messages"
              "type" "INSERT"
              "record" {"id" 1}
              "old_record" {}}})])
  => [true true false])

^{:refer xt.db.websocket.supabase-client/channel-trigger :added "4.1"}
(fact "dispatches matching channel bindings with enriched payloads"

  (!.js
   (var seen [])
   (var channel {"bindings"
                 [{"type" "postgres_changes"
                   "filter" {"event" "*"
                              "schema" "public"
                              "table" "messages"}
                   "callback" (fn [payload ref]
                                (xt/x:arr-push seen [(. payload ["eventType"])
                                                     (. payload ["table"])
                                                     ref]))}]})
   (realtime/channel-trigger
    channel
    "postgres_changes"
    {"data" {"schema" "public"
             "table" "messages"
             "type" "INSERT"
             "record" {"id" 1}
             "old_record" {}}}
    "1")
   seen)
  => [["INSERT" "messages" "1"]])

^{:refer xt.db.websocket.supabase-client/route-message :added "4.1"}
(fact "resolves pending callbacks clears heartbeat refs and routes to channels"

  (!.js
   (var pending-log [])
   (var channel-log [])
   (var client {"pending" {"1" (fn [payload _msg]
                                  (xt/x:arr-push pending-log (. payload ["status"])))}
                "pending-heartbeat-ref" "2"
                "channels"
                [{"topic" "realtime:public:messages"
                  "bindings"
                  [{"type" "phx_reply"
                    "callback" (fn [payload ref]
                                 (xt/x:arr-push channel-log [(. payload ["status"])
                                                              ref]))}]}]})
   (realtime/route-message
    client
    {"topic" "realtime:public:messages"
     "event" "phx_reply"
     "payload" {"status" "ok"}
     "ref" "1"})
   (realtime/route-message
    client
    {"topic" "phoenix"
     "event" "phx_reply"
     "payload" {"status" "ok"}
     "ref" "2"})
   [pending-log
    channel-log
    (. client ["pending-heartbeat-ref"])
    (. client ["heartbeat-status"])
    (xt/x:has-key? (. client ["pending"]) "1")])
  => [["ok"]
      [["ok" "1"]]
      nil
      "ok"
      false])

^{:refer xt.db.websocket.supabase-client/receive-raw :added "4.1"}
(fact "decodes raw frames before routing them"

  (!.js
   (var seen [])
   (var client {"decode" realtime/default-decode
                "pending" {}
                "channels"
                [{"topic" "realtime:public:messages"
                  "bindings"
                  [{"type" "broadcast"
                    "filter" {"event" "sync"}
                    "callback" (fn [payload ref]
                                 (xt/x:arr-push seen [(. payload ["event"])
                                                      ref]))}]}]})
   (var msg
        (realtime/receive-raw
         client
         {"data" (realtime/default-encode
                  {"topic" "realtime:public:messages"
                   "event" "broadcast"
                   "payload" {"event" "sync"}
                   "ref" "3"})}))
   [(. msg ["event"])
    seen])
  => ["broadcast"
      [["sync" "3"]]])

^{:refer xt.db.websocket.supabase-client/bind-channel-ids :added "4.1"}
(fact "binds server postgres change ids onto local bindings"

  (!.js
   (var channel {"bindings"
                 [{"type" "postgres_changes"
                   "filter" {"event" "*"}}
                  {"type" "broadcast"
                   "filter" {"event" "sync"}}
                  {"type" "postgres_changes"
                   "filter" {"event" "*"}}]})
   (realtime/bind-channel-ids
    channel
    {"postgres_changes" [{"id" "bind-1"}
                         {"id" "bind-2"}]})
   [(. (. channel ["bindings"]) [0] ["id"])
    (. (. channel ["bindings"]) [1] ["id"])
    (. (. channel ["bindings"]) [2] ["id"])])
  => ["bind-1"
      nil
      "bind-2"])

^{:refer xt.db.websocket.supabase-client/channel-join-payload :added "4.1"}
(fact "builds join payloads from config bindings tokens and extras"

  (!.js
   (realtime/channel-join-payload
    {"client" {"access-token" "token-1"}
     "params" {"config" {"broadcast" {"self" true}}}
     "bindings" [{"type" "postgres_changes"
                   "filter" {"event" "*"
                              "schema" "public"
                              "table" "messages"}}]
     "join-payload-extra" {"trace" true}}))
  => {"config" {"broadcast" {"self" true}
                "postgres_changes"
                [{"event" "*"
                  "schema" "public"
                  "table" "messages"}]}
      "access_token" "token-1"
      "trace" true})

^{:refer xt.db.websocket.supabase-client/subscribe-channel :added "4.1"}
(fact "queues join frames and resolves subscription callbacks on replies"

  (!.js
   (var statuses [])
   (var client {"state" "closed"
                "ref" 0
                "send-buffer" []
                "pending" {}
                "channels" []
                "connect" (fn [] (return true))})
   (var channel {"::" "supabase.realtime.channel"
                 "client" client
                 "topic" "realtime:public:messages"
                 "params" {"config" {}}
                 "bindings" [{"type" "postgres_changes"
                              "filter" {"event" "*"
                                         "schema" "public"
                                         "table" "messages"}}]
                 "join-payload-extra" {}
                 "join-ref" nil
                 "state" "closed"})
   (realtime/subscribe-channel
    channel
    (fn [status _err]
      (xt/x:arr-push statuses status)))
   ((. (. client ["pending"]) ["1"])
    {"status" "ok"
     "response" {"postgres_changes" [{"id" "bind-1"}]}}
    nil)
   [(. channel ["state"])
    (. channel ["join-ref"])
    (. (. (. client ["send-buffer"]) [0]) ["event"])
    (. (. channel ["bindings"]) [0] ["id"])
    statuses])
  => ["joined"
      "1"
      "phx_join"
      "bind-1"
      ["SUBSCRIBED"]])

^{:refer xt.db.websocket.supabase-client/unsubscribe-channel :added "4.1"}
(fact "pushes leave frames and closes the channel state"

  (!.js
   (var sent [])
   (var client {"state" "open"
                "ref" 0
                "send-buffer" []
                "conn" {"send" (fn [payload]
                                  (xt/x:arr-push sent payload)
                                  (return true))}
                "encode" (fn [msg] (return msg))})
   (var channel {"client" client
                 "topic" "realtime:public:messages"
                 "join-ref" "0"
                 "state" "joined"})
   [(realtime/unsubscribe-channel channel)
    (. channel ["state"])
    (. (xt/x:first sent) ["event"])])
  => [true
      "closed"
      "phx_leave"])

^{:refer xt.db.websocket.supabase-client/remove-channel-local :added "4.1"}
(fact "removes one channel from the local registry"

  (!.js
   (var a {"topic" "a"})
   (var b {"topic" "b"})
   (var client {"channels" [a b]})
   [(realtime/remove-channel-local client a)
    (. client ["channels"])])
  => [{"topic" "a"}
      [{"topic" "b"}]])

^{:refer xt.db.websocket.supabase-client/remove-channel :added "4.1"}
(fact "unsubscribes and removes one tracked channel"

  (!.js
   (var sent [])
   (var client {"state" "open"
                "ref" 0
                "send-buffer" []
                "channels" []
                "conn" {"send" (fn [payload]
                                  (xt/x:arr-push sent payload)
                                  (return true))}
                "encode" (fn [msg] (return msg))})
   (var channel {"::" "supabase.realtime.channel"
                 "client" client
                 "topic" "realtime:public:messages"
                 "join-ref" "0"
                 "state" "joined"})
   (xt/x:arr-push (. client ["channels"]) channel)
   [(realtime/remove-channel client channel)
    (xt/x:len (. client ["channels"]))
    (. (xt/x:first sent) ["event"])])
  => ["ok"
      0
      "phx_leave"])

^{:refer xt.db.websocket.supabase-client/remove-all-channels :added "4.1"}
(fact "removes all tracked channels one by one"

  (!.js
   (var sent [])
   (var client (realtime/client-create-base "wss://demo.supabase.co/realtime/v1" {}))
   (xt/x:set-key client "state" "open")
   (xt/x:set-key client "conn"
                 {"send" (fn [payload]
                           (xt/x:arr-push sent payload)
                           (return true))})
   (xt/x:set-key client "encode" (fn [msg] (return msg)))
   (var a (realtime/create-channel client "public:a" {"config" {}}))
   (var b (realtime/create-channel client "public:b" {"config" {}}))
   (xt/x:set-key a "join-ref" "0")
   (xt/x:set-key a "state" "joined")
   (xt/x:set-key b "join-ref" "0")
   (xt/x:set-key b "state" "joined")
   [(realtime/remove-all-channels client)
    (xt/x:len (. client ["channels"]))
    (xt/x:len sent)])
  => [["ok" "ok"]
      0
      2])

^{:refer xt.db.websocket.supabase-client/connect :added "4.1"}
(fact "delegates connection through the configured hook"

  (!.js
   (var called 0)
   (var client {"state" "closed"
                "manual-disconnect" true
                "connect-fn" (fn [inner]
                               (:= called (+ called 1))
                               (xt/x:set-key inner "state" "connecting")
                               (return {"ok" true}))})
   [(realtime/connect client)
    called
    (. client ["manual-disconnect"])])
  => [{"ok" true}
      1
      false])

^{:refer xt.db.websocket.supabase-client/disconnect :added "4.1"}
(fact "stops heartbeats clears conn state and calls disconnect hooks"

  (!.js
   (var cleared nil)
   (var closed [])
   (var client {"state" "open"
                "conn" {"id" "conn"}
                "heartbeat-timer" "timer-1"
                "clear-interval" (fn [id]
                                   (:= cleared id)
                                   (return true))
                "disconnect-fn" (fn [_client code reason]
                                  (xt/x:arr-push closed [code reason])
                                  (return true))})
   (realtime/disconnect client 1000 "bye")
   [cleared
    closed
    (. client ["manual-disconnect"])
    (. client ["state"])
    (. client ["conn"])])
  => ["timer-1"
      [[1000 "bye"]]
      true
      "closed"
      nil])

^{:refer xt.db.websocket.supabase-client/create-channel :added "4.1"}
(fact "creates channels once per normalized topic"

  (!.js
   (var client {"channels" []})
   (var a (realtime/create-channel client "public:messages" {"config" {}}))
   (var b (realtime/create-channel client "realtime:public:messages" {"config" {}}))
   [(. a ["topic"])
    (== a b)
    (xt/x:len (. client ["channels"]))])
  => ["realtime:public:messages"
      true
      1])

^{:refer xt.db.websocket.supabase-client/client-create-base :added "4.1"}
(fact "creates stateful clients with helper methods and defaults"

  (!.js
   (var client
        (realtime/client-create-base
         "wss://demo.supabase.co/realtime/v1"
         {"apikey" "anon-key"
          "access-token" "token-1"
          "params" {"log_level" "debug"}}))
   [(. client ["::"])
    (. client ["endpoint"])
    (. client ["apikey"])
    (. client ["access-token"])
    (xt/x:is-function? (. client ["channel"]))
    (xt/x:is-function? (. client ["endpointURL"]))])
  => ["supabase.realtime.client"
      "wss://demo.supabase.co/realtime/v1"
      "anon-key"
      "token-1"
      true
      true])

^{:refer xt.db.websocket.supabase-client/normalize-endpoint :added "4.1"}
(fact "emits lua.nginx source for endpoint normalization"
  (let [out (l/emit-as :lua.nginx
                       '[(xt.db.websocket.supabase-client/normalize-endpoint endpoint)])]
    [(string? out)
     (< 0 (count out))])
  => [true true])

^{:refer xt.db.websocket.supabase-client/default-encode :added "4.1"}
(fact "encodes realtime frames as JSON"

  (!.js
   (realtime/default-encode {"topic" "phoenix"
                             "event" "heartbeat"}))
  => "{\"topic\":\"phoenix\",\"event\":\"heartbeat\"}")

^{:refer xt.db.websocket.supabase-client/default-decode :added "4.1"}
(fact "decodes JSON strings and message event data payloads"

  (!.js
   [(realtime/default-decode "{\"topic\":\"phoenix\",\"event\":\"heartbeat\"}")
    (realtime/default-decode {"data" "{\"topic\":\"phoenix\",\"event\":\"reply\"}"})])
  => [{"topic" "phoenix"
       "event" "heartbeat"}
      {"topic" "phoenix"
       "event" "reply"}])

^{:refer xt.db.websocket.supabase-client/transport->driver :added "4.1"}
(fact "wraps transport constructors in websocket protocol drivers"

  (!.js
   (var Transport
        (fn [url]
          (return {"url" url
                   "send" (fn [_payload] (return true))
                   "close" (fn [] (return true))
                   "addEventListener" (fn [_event _handler] (return true))})))
   (var driver (realtime/transport->driver Transport))
   (var socket (ws/connect driver "wss://demo.supabase.co/realtime/v1"))
   [(. driver ["::"])
    (. socket ["::"])
    (. (. socket ["_raw"]) ["url"])])
  => ["websocket.client.driver"
      "websocket.client"
      "wss://demo.supabase.co/realtime/v1"])

^{:refer xt.db.websocket.supabase-client/default-connect-fn :added "4.1"}
(fact "connects through the websocket driver and installs lifecycle listeners"

  (!.js
   (var handlers {})
   (var current nil)
   (var Transport
        (fn [url]
          (var raw {"url" url
                    "send" (fn [_payload] (return true))
                    "close" (fn [] (return true))
                    "addEventListener" (fn [event handler]
                                         (xt/x:set-key handlers event handler)
                                         (return true))})
          (xt/x:set-key raw "emit"
                        (fn [event payload]
                          ((xt/x:get-key handlers event) payload)))
          (:= current raw)
          (return raw)))
   (var client
        (realtime/client-create-base
         "wss://demo.supabase.co/realtime/v1"
         {"transport-driver" (realtime/transport->driver Transport)
          "schedule-interval" (fn [_handler _ms] (return "timer-1"))
          "clear-interval" (fn [_id] (return true))
          "encode" (fn [msg] (return msg))
          "decode" (fn [msg] (return msg))}))
   (realtime/default-connect-fn client)
   (. current (emit "open" {}))
   [(. client ["state"])
    (. client ["heartbeat-timer"])
    (. (. client ["conn"]) ["::"])])
  => ["open"
      "timer-1"
      "websocket.client"])

^{:refer xt.db.websocket.supabase-client/default-disconnect-fn :added "4.1"}
(fact "closes raw websocket connections with optional close codes"

  (!.js
   (var closed [])
   (realtime/default-disconnect-fn
    {"conn" {"close" (fn [code reason]
                        (xt/x:arr-push closed [code reason])
                        (return true))}}
    1000
    "bye")
   closed)
  => [[1000 "bye"]])
