(ns js.lib.osc-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.osc :as osc]
              [js.core :as j]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-spec :as xt]
              [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.osc/newOSC :added "4.0" :unchecked true}
(fact "creates a new OSC instance"

  (!.js
   (xtd/obj-keys (osc/newOSC)))
  => ["options" "eventHandler"])

^{:refer js.lib.osc/newMessage :added "4.0" :unchecked true}
(fact "creates a new OSC Message"

  (!.js
   (xtd/obj-keys (osc/newMessage ["test", "path"], 50, 100.52, "test")))
  => ["offset" "address" "types" "args"])

^{:refer js.lib.osc/newBundle :added "4.0" :unchecked true}
(fact "creates a new OSC Bundle"

  (!.js
   (xtd/obj-keys
     (osc/newBundle
      (osc/newMessage ["test", "path"], 50, 100.52, "test")
      (osc/newMessage ["test", "path"], 50, 100.52, "test"))))
  => ["offset" "timetag" "bundleElements"])

^{:refer js.lib.osc/DatagramPlugin :added "4.0" :unchecked true}
(fact "creates a Datagram plugin"

  (!.js
   (var plugin (osc/DatagramPlugin {:send {:port 41234}}))
   (and (== -1 (. plugin socketStatus))
        (xt/x:is-function? (. plugin notify))
        (== "udp4" (. (. plugin socket) type))
        (== 41234 (. (. (. plugin options) send) port))))
  => true)

^{:refer js.lib.osc/BridgePlugin :added "4.0" :unchecked true}
(fact "creates a Bridge plugin"

  (!.js
   (var plugin (osc/BridgePlugin {}))
   (and (== -1 (. plugin socketStatus))
        (xt/x:is-function? (. plugin notify))
        (== "udp4" (. (. plugin socket) type))
        (== 8080 (. (. (. plugin options) wsServer) port))))
  => true)

^{:refer js.lib.osc/WebsocketClientPlugin :added "4.0" :unchecked true}
(fact "creates a Ws Client Plugin"

  (!.js
   (var plugin (osc/WebsocketClientPlugin {}))
   (and (== -1 (. plugin socketStatus))
        (xt/x:is-function? (. plugin notify))
        (== nil (. plugin socket))
        (== 8080 (. (. plugin options) port))))
  => true)

^{:refer js.lib.osc/WebsocketServerPlugin :added "4.0" :unchecked true}
(fact "creates a Ws Server Plugin"

  (!.js
   (var plugin (osc/WebsocketServerPlugin {:port 8081}))
   (and (== -1 (. plugin socketStatus))
        (xt/x:is-function? (. plugin notify))
        (== nil (. plugin socket))
        (== 8081 (. (. plugin options) port))))
  => true)

^{:refer js.lib.osc/on :added "4.0" :unchecked true
  :setup [(l/rt:restart)
          (l/rt:scaffold :js)]
  :teardown [(l/rt:restart)
             (l/rt:scaffold :js)]}
(fact "adds an event listener to the osc server"

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

  (notify/wait-on :js
    (var osc (osc/newOSC
              {:plugin (osc/DatagramPlugin
                        {:send {:port 41234}})}))
    (var messageFn
         (fn []
           (return
             (osc/newMessage (xtd/arr-random ["/test"
                                            "/foo"
                                            "/bar"
                                            "/baz"])
                            (xt/x:random)))))

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
