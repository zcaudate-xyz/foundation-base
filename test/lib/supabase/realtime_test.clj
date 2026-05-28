(ns lib.supabase.realtime-test
  (:use code.test)
  (:require [lib.supabase.realtime :refer :all]
            [lib.supabase.common :as common]
            [net.http.websocket :as ws])
  (:import (java.util.concurrent CompletableFuture)))

(defn sample-client
  []
  (common/create-client "http://localhost:54321" "key-123" {}))

(def sample-socket
  (Object.))

^{:refer lib.supabase.realtime/trim-trailing-slash :added "4.1"}
(fact "trims a trailing slash"
  [(trim-trailing-slash "http://a/")
   (trim-trailing-slash "http://a")]
  => ["http://a" "http://a"])

^{:refer lib.supabase.realtime/derive-websocket-url :added "4.1"}
(fact "derives the realtime websocket URL"
  [(derive-websocket-url "https://example.supabase.co")
   (derive-websocket-url "http://localhost:54321/rest/v1")]
  => ["wss://example.supabase.co/realtime/v1/websocket"
      "ws://localhost:54321/realtime/v1/websocket"])

^{:refer lib.supabase.realtime/prepare-connect-url :added "4.1"}
(fact "builds a websocket URL with query params"
  (prepare-connect-url (sample-client) {:params {:log_level "debug"}})
  => #(and (string? %)
           (.contains % "ws://localhost:54321/realtime/v1/websocket?")
           (.contains % "apikey=key-123")
           (.contains % "log_level=debug")))

^{:refer lib.supabase.realtime/connected? :added "4.1"}
(fact "checks the socket open flag"
  (let [client (sample-client)]
    [(connected? client)
     (do (common/swap-state! client assoc :socket_open? true)
         (connected? client))])
  => [false true])

^{:refer lib.supabase.realtime/append-message! :added "4.1"}
(fact "buffers partial websocket messages until completion"
  (let [client (sample-client)]
    [(append-message! client "{\"topic\":\"a\"" false)
     (append-message! client ",\"event\":\"b\"}" true)])
  => [nil {"topic" "a" "event" "b"}])

^{:refer lib.supabase.realtime/dispatch-frame! :added "4.1"}
(fact "dispatches broadcast and postgres change frames to handlers"
  (let [client (sample-client)
        seen (atom [])]
    (common/swap-state! client assoc :subscriptions
                        {1 {:kind :broadcast
                            :topic-match "realtime:room"
                            :event-name "sync"
                            :handler #(swap! seen conj [:broadcast (:event %) (:payload %)])}
                         2 {:kind :postgres_changes
                            :topic-match "realtime:room"
                            :handler #(swap! seen conj [:postgres (:payload %)])}})
    (dispatch-frame! client {"topic" "realtime:room"
                             "event" "broadcast"
                             "payload" {"event" "sync"
                                        "payload" {"id" 1}}})
    (dispatch-frame! client {"topic" "realtime:room"
                             "event" "postgres_changes"
                             "payload" {"new" {"id" 2}}})
    @seen)
  => [[:broadcast "sync" {"id" 1}]
      [:postgres {"new" {"id" 2}}]])

^{:refer lib.supabase.realtime/connect :added "4.1"}
(fact "connects through net.http.websocket and stores the socket"
  (let [client (sample-client)]
    (with-redefs [ws/websocket (fn [_url _opts]
                                 (CompletableFuture/completedFuture sample-socket))]
      [(connect client)
       (:socket (common/raw-state client))
       (connected? client)]))
  => [sample-socket sample-socket true])

^{:refer lib.supabase.realtime/disconnect :added "4.1"}
(fact "closes and clears the tracked socket"
  (let [client (sample-client)]
    (common/swap-state! client assoc :socket sample-socket :socket_open? true)
    (with-redefs [ws/close! (fn [_socket]
                              (CompletableFuture/completedFuture true))]
      [(disconnect client)
       (:socket (common/raw-state client))
       (connected? client)]))
  => #(let [[client socket connected] %]
        (and (map? client)
             (nil? socket)
             (false? connected))))

^{:refer lib.supabase.realtime/resolve-topic :added "4.1"}
(fact "normalizes topics with the realtime: prefix"
  [(resolve-topic "room")
   (resolve-topic "realtime:room")]
  => ["realtime:room" "realtime:room"])

^{:refer lib.supabase.realtime/join-payload :added "4.1"}
(fact "builds a phoenix join payload with filters and auth"
  (let [client (sample-client)]
    (common/swap-state! client assoc :auth_token "token-1")
    (join-payload client {:filters [{"event" "*"}]}))
  => {"config" {"broadcast" {"ack" false
                             "self" false}
                "presence" {"key" ""}
                "postgres_changes" [{"event" "*"}]}
      "access_token" "token-1"})

^{:refer lib.supabase.realtime/join-frame :added "4.1"}
(fact "builds a join frame with topic and refs"
  (let [client (sample-client)
        frame (join-frame client "room")]
    [(get frame "topic")
     (get frame "event")
     (integer? (get frame "ref"))
     (= (get frame "ref") (get frame "join_ref"))])
  => ["realtime:room" "phx_join" true true])

^{:refer lib.supabase.realtime/leave-frame :added "4.1"}
(fact "builds a leave frame with topic and refs"
  (let [client (sample-client)
        frame (leave-frame client "room")]
    [(get frame "topic")
     (get frame "event")
     (= {} (get frame "payload"))])
  => ["realtime:room" "phx_leave" true])

^{:refer lib.supabase.realtime/send-frame! :added "4.1"}
(fact "sends encoded frames over the tracked socket"
  (let [client (sample-client)
        seen (atom nil)]
    (common/swap-state! client assoc :socket sample-socket :socket_open? true)
    (with-redefs [ws/send! (fn [_socket payload]
                             (reset! seen payload)
                             (CompletableFuture/completedFuture true))]
      [(send-frame! client {"topic" "realtime:room"})
       @seen]))
  => [{"topic" "realtime:room"} "{\"topic\":\"realtime:room\"}"])

^{:refer lib.supabase.realtime/join-channel :added "4.1"}
(fact "tracks joined channels and sends a join frame"
  (let [client (sample-client)]
    (with-redefs [send-frame! (fn [_client frame] frame)]
      [(join-channel client "room")
       (contains? (:channels (common/raw-state client)) "realtime:room")]))
  => #(let [[frame tracked] %]
        (and (= "phx_join" (get frame "event"))
             tracked)))

^{:refer lib.supabase.realtime/leave-channel :added "4.1"}
(fact "removes tracked channels and sends a leave frame"
  (let [client (sample-client)]
    (common/swap-state! client assoc :channels {"realtime:room" {"event" "phx_join"}})
    (with-redefs [send-frame! (fn [_client frame] frame)]
      [(leave-channel client "room")
       (contains? (:channels (common/raw-state client)) "realtime:room")]))
  => #(let [[frame tracked] %]
        (and (= "phx_leave" (get frame "event"))
             (false? tracked))))

^{:refer lib.supabase.realtime/subscribe-broadcast :added "4.1"}
(fact "registers a broadcast subscription"
  (let [client (sample-client)]
    (with-redefs [connect (fn [_client] sample-socket)
                  join-channel (fn [_client _topic & [_opts]] true)]
      (let [id (subscribe-broadcast client {:topic "room" :event "sync"} identity)]
        [(integer? id)
         (get-in (common/raw-state client) [:subscriptions id :kind])])))
  => [true :broadcast])

^{:refer lib.supabase.realtime/subscribe-postgres-changes :added "4.1"}
(fact "registers a postgres_changes subscription"
  (let [client (sample-client)]
    (with-redefs [connect (fn [_client] sample-socket)
                  join-channel (fn [_client _topic & [_opts]] true)]
      (let [id (subscribe-postgres-changes client
                                           {:schema_name "scratch"
                                            :table_name "Entry"}
                                           identity)]
        [(integer? id)
         (get-in (common/raw-state client) [:subscriptions id :kind])])))
  => [true :postgres_changes])

^{:refer lib.supabase.realtime/unsubscribe :added "4.1"}
(fact "removes subscriptions from client state"
  (let [client (sample-client)]
    (common/swap-state! client assoc :subscriptions {1 {:kind :broadcast}})
    [(unsubscribe client 1)
     (:subscriptions (common/raw-state client))])
  => [1 {}])

^{:refer lib.supabase.realtime/broadcast! :added "4.1"}
(fact "builds and sends a broadcast frame"
  (let [client (sample-client)]
    (with-redefs [send-frame! (fn [_client frame] frame)]
      (broadcast! client {:topic "room"
                          :event "sync"
                          :payload {"id" 1}})))
  => #(and (= "realtime:room" (get % "topic"))
           (= "broadcast" (get % "event"))
           (= {"event" "sync" "payload" {"id" 1}} (get % "payload"))))
