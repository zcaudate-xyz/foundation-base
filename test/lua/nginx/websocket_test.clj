(ns lua.nginx.websocket-test
  (:require [net.http :as http]
            [net.http.websocket :as client]
            [rt.nginx]
            [std.json :as json]
            [std.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :nginx.instance
   :require [[lua.nginx :as n]
              [lua.nginx.websocket :as ws]
              [xt.lang.common-lib :as k]
              [xt.lang.common-data :as xtd]
              [xt.sys.cache-common :as cache]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.websocket/STREAM-FLAG-KEY :added "4.0"}
(fact "path to flag key - when set, will close all connections"
  ^:hidden
  
  (ws/STREAM-FLAG-KEY "TICKER")
  => "__stream__:TICKER:__flag__")

^{:refer lua.nginx.websocket/STREAM-ACTIVE-KEY :added "4.0"}
(fact "returns the number of active connections"

  (ws/STREAM-ACTIVE-KEY "TICKER")
  => "__stream__:TICKER:__active__")

^{:refer lua.nginx.websocket/service-register :added "4.0"
  :setup [(!.lua (cache/flush (cache/cache :GLOBAL)))]}
(fact "registers a ws service with nginx"
  ^:hidden
  
  (ws/service-register "TICKER"
                       {:hello "world"})
  => true
  
  (-> (!.lua (cache/get-all (cache/cache :GLOBAL)))
      (update "__meta__:stream" json/read))
  => {"__stream__:TICKER:__active__" 0,
      "__meta__:stream" {"TICKER" {"hello" "world", "key" "TICKER"}}}
  
  (ws/service-register "TICKER"
                       {:hello "world"})
  => false
  
  (ws/service-unregister "TICKER")
  => true
  
  (!.lua (cache/get-all (cache/cache :GLOBAL)))
  => {"__meta__:stream" "{}"}

  (ws/service-unregister "TICKER")
  => false)

^{:refer lua.nginx.websocket/service-unregister :added "4.0"}
(fact "unregisters a ws service with nginx")

^{:refer lua.nginx.websocket/service-signal-flag :added "4.0"
  :setup [(!.lua (cache/flush (cache/cache :GLOBAL)))
          (ws/service-register "TICKER"
                               {:hello "world"})]}
(fact "sets the flag for the service (killing all connections)"
  ^:hidden
  
  (!.lua (cache/get (cache/cache :GLOBAL)
                 (ws/STREAM-FLAG-KEY "TICKER")))
  => nil
  
  (ws/service-signal-flag "TICKER")
  => true
  
  (!.lua (cache/get (cache/cache :GLOBAL)
                 (ws/STREAM-FLAG-KEY "TICKER")))
  => true

  (ws/service-reset-flag "TICKER")
  => true

  (!.lua (cache/get (cache/cache :GLOBAL)
                 (ws/STREAM-FLAG-KEY "TICKER")))
  => nil)

^{:refer lua.nginx.websocket/service-reset-flag :added "4.0"}
(fact "clears the flag for the service allowing connections")

^{:refer lua.nginx.websocket/service-prep :added "4.0"
  :setup [(!.lua (cache/flush (cache/cache :GLOBAL)))
          (ws/service-register "TICKER"
                       {:hello "world"})]}
(fact "helper function to check if registeration is valid"
  ^:hidden
  
  (ws/service-prep "HELLO")
  => (throws)

  (!.lua
   (ws/service-prep "TICKER"))
  => {"TICKER" {"hello" "world", "key" "TICKER"}})

^{:refer lua.nginx.websocket/service-add-connection :added "4.0"
  :setup [(!.lua (cache/flush (cache/cache :GLOBAL)))
          (!.lua (cache/flush (cache/cache :WS_DEBUG)))
          (ws/service-register "WS_DEBUG"
                       {:hello "world"})]}
(fact "helper for ws-loop to add itself to registry"
  ^:hidden
  
  (ws/connection-count "WS_DEBUG")
  => 0
  
  (!.lua
     (ws/service-add-connection "WS_DEBUG" "id-0" {})
     (ws/service-add-connection "WS_DEBUG" "id-1" {}))
  
  (ws/connection-list "WS_DEBUG")
  => ["id-0" "id-1"]
  
  (ws/connection-count "WS_DEBUG")
  => 2

  (ws/connection-info "WS_DEBUG" "id-0")
  => (contains {"started" integer? "group" "WS_DEBUG", "id" "id-0"})

  (!.lua
   (ws/connection-info-update "WS_DEBUG" "id-0"
                              (fn [info]
                                (return (k/obj-assign {:hello "world"}
                                                      info)))))
  => (contains {"started" integer?
                "group" "WS_DEBUG",
                "id" "id-0"
                "hello" "world"})
  
  (ws/service-remove-connection "WS_DEBUG" "id-0")
  => true

  (ws/service-remove-connection "WS_DEBUG" "id-0")
  => false
  
  (ws/connection-count "WS_DEBUG")
  => 1

  (ws/connection-purge "WS_DEBUG" "id-1")
  => true

  ;; connection count remains at one because the thread
  ;; is responsible for it's own cleanup
  (ws/connection-count "WS_DEBUG")
  => 1)

^{:refer lua.nginx.websocket/service-remove-connection :added "4.0"}
(fact "helper for ws-loop to remove itself from registry")

^{:refer lua.nginx.websocket/connection-count :added "4.0"}
(fact "returns number of connections for the given ")

^{:refer lua.nginx.websocket/connection-list :added "4.0"}
(fact "lists all connection keys")

^{:refer lua.nginx.websocket/connection-info :added "4.0"}
(fact "gets the connection info")

^{:refer lua.nginx.websocket/connection-info-update :added "4.0"}
(fact "updates the connection info")

^{:refer lua.nginx.websocket/connection-purge :added "4.0"}
(fact "deletes the record for the connection (meaning the loop will shut down)")

^{:refer lua.nginx.websocket/es-main :added "4.0"}
(fact "creates an event-stream service")

^{:refer lua.nginx.websocket/new-connection :added "4.0"}
(fact "creates a new websocket connection"
  ^:hidden
  
  (!.lua
   (ws/new-connection {})
   true)
  => (throws))

^{:refer lua.nginx.websocket/ws-stream-loop :added "4.0"
  :setup [(l/rt:restart)
          (!.lua
           (ws/service-register "WS_DEBUG" {})
           (local f ws/ws-main)
           (:= (. DEBUG ["ws_handler"])
               (fn []
                 (f "WS_DEBUG"
                    {}
                    {:setup (fn [_ uid]
                              (cache/set (cache/cache :GLOBAL)
                                      (cat "__COUNTER__:" uid)
                                      0)
                              (return [uid]))
                     :main (fn [conn uid vars]
                             (local uid (xtd/first vars))
                             (local num (cache/incr (cache/cache :GLOBAL)
                                                 (cat "__COUNTER__:" uid)
                                                 1))
                             (ws/send-text conn (k/to-string num))
                             (n/sleep 0.1))
                     :teardown (fn []
                                 (cache/del (cache/cache :GLOBAL)
                                            "__COUNTER__"))}
                    {}
                    {})))
           true)]}
(fact "streaming loop for passive clients"
  ^:hidden
  
  (def -out- (atom []))
  
  (def -ws- @(client/websocket (str "ws://localhost:"
                                    (:port (l/rt:inner :lua))
                                    "/eval/ws")
                               {:on-message  (fn [ws data _]
                                               (swap! -out- conj (str data)))}))
  (ws/connection-count "WS_DEBUG")
  => 1
  
  (do 
    (Thread/sleep 1000)
    (client/close! -ws-)
    @-out-)
  => (contains ["1" "2" "3" "4" "5"])

  (ws/connection-count "WS_DEBUG")
  => 0)

^{:refer lua.nginx.websocket/ws-listener-loop :added "4.0"}
(fact "helper function for the main loop")

^{:refer lua.nginx.websocket/ws-main :added "4.0"
  :setup [(l/rt:restart)
          (!.lua
           (ws/service-register "WS_DEBUG" {})
           (local f ws/ws-main)
           (:= (. DEBUG ["ws_handler"])
               (fn []
                 (f "WS_DEBUG"
                    {:setup (fn [conn uid]
                              (return [uid]))
                     :main (fn [conn uid vars data]
                             (ws/send-text conn (cjson.encode
                                                 [(xtd/first vars) data])))}
                    {}
                    {}
                    {})))
           true)]}
(fact "constructs a main service loop"
  ^:hidden
  
  (def -out- (atom []))
  
  (def -ws- @(client/websocket (str "ws://localhost:"
                                    (:port (l/rt:inner :lua))
                                    "/eval/ws")
                               {:on-message  (fn [ws data _]
                                               (swap! -out- conj (str data)))}))
  (ws/connection-count "WS_DEBUG")
  => 1
  
  (do @(client/send! -ws- "hello")
      (Thread/sleep 100)
      (json/read (first @-out-)))
  ;; "7F2BE8B5-BA78-463E-9248-3294A5A8FB74"
  => (contains [string? "hello"])

  (do @(client/close! -ws-)
      (Thread/sleep 100)
      (ws/connection-count "WS_DEBUG"))
  => 0)

^{:refer lua.nginx.websocket/ws-echo :added "4.0"
  :setup [(l/rt:restart)
          (!.lua
           (ws/service-register "WS_DEBUG" {})
           (local f ws/ws-echo)
           (:= (. DEBUG ["ws_handler"])
               (fn []
                 (f "WS_DEBUG")))
           true)]}
(fact "constructs an echo server"
  ^:hidden
  
  (def -out- (atom []))
  
  (ws/connection-count "WS_DEBUG")
  => 0
  
  (def -ws- @(client/websocket (str "ws://localhost:"
                                    (:port (l/rt:inner :lua))
                                    "/eval/ws")
                               {:on-message  (fn [ws data _]
                                               (swap! -out- conj (str data)))}))
  
  (ws/connection-count "WS_DEBUG")
  => 1

  (do @(client/send! -ws- "HELLO WORLD")
      (Thread/sleep 100)
      @-out-)
  => ["HELLO WORLD"])

^{:refer lua.nginx.websocket/es-test-loop :added "4.0"
  :setup [(l/rt:restart)
          (!.lua
           (ws/service-register "ES_DEBUG" {} nil)
           (:= (. DEBUG ["es_handler"])
               (fn []
                 (ws/es-test-loop "ES_DEBUG"
                                  100
                                  5
                                  (fn [n]
                                    (return (cat  "TEST-" n)))))))]}
(fact "runs as es-test-loop"
  ^:hidden
  
  (def +events+ (:events (net.http/event-stream
                          (str "http://localhost:" (:port (l/rt :lua))
                               "/eval/es"))))
  
  ;; EVENT SOURCE
  (do (Thread/sleep 100)
      @+events+)
  => ["TEST-5" "TEST-4" "TEST-3" "TEST-2" "TEST-1"])
