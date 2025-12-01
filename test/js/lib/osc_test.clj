(ns js.lib.osc-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.string :as str]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.osc :as osc]
             [js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.osc/newOSC :added "4.0" :unchecked true}
(fact "creates a new OSC instance"
  ^:hidden
  
  (!.js
   (k/obj-keys (osc/newOSC)))
  => ["options" "eventHandler"])

^{:refer js.lib.osc/newMessage :added "4.0" :unchecked true}
(fact "creates a new OSC Message"
  ^:hidden

  (!.js
   (k/obj-keys (osc/newMessage ["test", "path"], 50, 100.52, "test")))
  => ["offset" "address" "types" "args"])

^{:refer js.lib.osc/newBundle :added "4.0" :unchecked true}
(fact "creates a new OSC Bundle"
  ^:hidden

  (!.js
   (k/obj-keys
    (osc/newBundle
     (osc/newMessage ["test", "path"], 50, 100.52, "test")
     (osc/newMessage ["test", "path"], 50, 100.52, "test"))))
  => ["offset" "timetag" "bundleElements"])

^{:refer js.lib.osc/DatagramPlugin :added "4.0" :unchecked true}
(fact "creates a Datagram plugin"
  ^:hidden

  (!.js
   (k/get-data
    (osc/DatagramPlugin
     {:send {:port 41234}})))
  => {"socketStatus" -1,
      "notify" "<function>",
      "socket"
      {"_eventsCount" 2,
       "_events" {"message" "<function>", "error" "<function>"},
       "type" "udp4"},
      "options"
      {"send" {"host" "localhost", "port" 41234},
       "type" "udp4",
       "open" {"host" "localhost", "exclusive" false, "port" 41234}}})

^{:refer js.lib.osc/BridgePlugin :added "4.0" :unchecked true}
(fact "creates a Bridge plugin"
  ^:hidden

  (!.js
   (k/get-data
    (osc/BridgePlugin
     {})))
  => {"websocket" nil,
      "socketStatus" -1,
      "notify" "<function>",
      "socket"
      {"_eventsCount" 2,
       "_events" {"message" "<function>", "error" "<function>"},
       "type" "udp4"},
      "options"
      {"udpServer"
       {"host" "localhost", "exclusive" false, "port" 41234},
       "udpClient" {"host" "localhost", "port" 41235},
       "wsServer" {"host" "localhost", "port" 8080},
       "receiver" "ws"}})

^{:refer js.lib.osc/WebsocketClientPlugin :added "4.0" :unchecked true}
(fact "creates a Ws Client Plugin"
  ^:hidden
  
  (!.js
   (k/get-data
    (osc/WebsocketClientPlugin
     {})))
  => {"socketStatus" -1,
      "notify" "<function>",
      "socket" nil,
      "options"
      {"protocol" [], "host" "localhost", "secure" false, "port" 8080}})

^{:refer js.lib.osc/WebsocketServerPlugin :added "4.0" :unchecked true}
(fact "creates a Ws Server Plugin"
  ^:hidden
  
  (!.js
   (k/get-data
    (osc/WebsocketServerPlugin
     {:port 8081})))
  => {"socketStatus" -1,
      "notify" "<function>",
      "socket" nil,
      "options" {"host" "localhost", "port" 8081}})

^{:refer js.lib.osc/on :added "4.0" :unchecked true
  :setup [(l/rt:restart)
          (l/rt:scaffold :js)]
  :teardown [(l/rt:restart)
             (l/rt:scaffold :js)]}
(fact "adds an event listener to the osc server"
  ^:hidden
  
  (notify/wait-on :js
    (var osc (osc/newOSC
              {:plugin (osc/DatagramPlugin
                        {:send {:port 41234}})}))
    
    (osc/on osc "*"
            (fn [msg info]
              (repl/notify 
               {:msg  msg
                :info info})))
    
    (osc/on osc "open"
            (fn []
              (osc/send osc (osc/newMessage "/test" 12.221, "hello")
                        {:host "127.0.0.1" :port 41234})))
    
    (osc/open osc {}))
  => (contains-in
      {"msg" {"offset" 24,
              "args" [number? "hello"],
              "types" ",fs",
              "address" "/test"},
       "info" {"address" "127.0.0.1", "port" 41234, "family" "IPv4", "size" 24}}))

^{:refer js.lib.osc/off :added "4.0" :unchecked true}
(fact "removes an event listener to the osc server")

^{:refer js.lib.osc/send :added "4.0" :unchecked true}
(fact "sends a message or a bundle")

^{:refer js.lib.osc/open :added "4.0" :unchecked true
  :setup [(l/rt:restart)
          (l/rt:scaffold :js)]
  :teardown [(l/rt:restart)
             (l/rt:scaffold :js)]}
(fact "binds a server to a port"
  ^:hidden
  
  (notify/wait-on :js
    (var osc (osc/newOSC
              {:plugin (osc/DatagramPlugin
                        {:send {:port 41234}})}))
    
    (osc/on osc "*"
            (fn [msg info]
              (osc/close osc)))
    
    (osc/on osc "open"
            (fn []
              (osc/send osc (osc/newMessage "/test" 12.221, "hello")
                        {:host "127.0.0.1" :port 41234})))


    (osc/on osc "close"
            (fn []
              (repl/notify
               (osc/status osc))))
    
    (osc/open osc {})))

^{:refer js.lib.osc/status :added "4.0" :unchecked true}
(fact "gets the current osc status")

^{:refer js.lib.osc/close :added "4.0" :unchecked true
  :setup [(l/rt:restart)
          (l/rt:scaffold :js)]
  :teardown [(l/rt:restart)
             (l/rt:scaffold :js)]}
(fact "closes the osc"
  ^:hidden
  
  (notify/wait-on :js
    (var osc (osc/newOSC
              {:plugin (osc/DatagramPlugin
                        {:send {:port 41234}})}))
    (var messageFn
         (fn []
           (return
            (osc/newMessage (k/arr-random ["/test"
                                           "/foo"
                                           "/bar"
                                           "/baz"])
                            (k/random)))))
    
    (osc/on osc "*"
            (fn [msg info]
              (repl/notify
               {:msg msg :info info})))
    
    (osc/on osc "open"
            (fn []

              (osc/send osc
                        (osc/newBundle
                         (messageFn)
                         (messageFn)
                         (messageFn)
                         (messageFn)
                         (messageFn))
                        {:host "127.0.0.1" :port 41234})))


    
    
    (osc/open osc {}))
  => (contains-in
      {"msg"
       map?,
       "info"
       map?}))
