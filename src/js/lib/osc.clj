(ns js.lib.osc
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [send]))

(l/script :js
  {:bundle  {:default [["osc-js" :as OSC]]}
   :import [["osc-js" :as OSC]]})

(defmacro.js newOSC
  "creates a new OSC instance"
  {:added "4.0"}
  [& [m]]
  (list 'new 'OSC m))

(defmacro.js newMessage
  "creates a new OSC Message"
  {:added "4.0"}
  [path & args]
  (apply list 'new 'OSC.Message path args))

(defmacro.js newBundle
  "creates a new OSC Bundle"
  {:added "4.0"}
  [& args]
  (apply list 'new 'OSC.Bundle args))

(defmacro.js DatagramPlugin
  "creates a Datagram plugin"
  {:added "4.0"}
  [& [m]]
  (list 'new 'OSC.DatagramPlugin m))

(defmacro.js BridgePlugin
  "creates a Bridge plugin"
  {:added "4.0"}
  [& [m]]
  (list 'new 'OSC.BridgePlugin m))

(defmacro.js WebsocketClientPlugin
  "creates a Ws Client Plugin"
  {:added "4.0"}
  [& [m]]
  (list 'new 'OSC.WebsocketClientPlugin m))

(defmacro.js WebsocketServerPlugin
  "creates a Ws Server Plugin"
  {:added "4.0"}
  [& [m]]
  (list 'new 'OSC.WebsocketServerPlugin m))

(defmacro.js on
  "adds an event listener to the osc server"
  {:added "4.0"}
  [osc path f]
  (list '. osc (list 'on path f)))

(defmacro.js off
  "removes an event listener to the osc server"
  {:added "4.0"}
  [osc path f]
  (list '. osc (list 'off path)))

(defmacro.js send
  "sends a message or a bundle"
  {:added "4.0"}
  [osc msg & [opts]]
  (list '. osc (list 'send msg)))

(defmacro.js open
  "binds a server to a port"
  {:added "4.0"}
  [osc & [opts]]
  (list '. osc (list 'open opts)))

(defmacro.js status
  "gets the current osc status"
  {:added "4.0"}
  [osc]
  (list '. osc (list 'status)))

(defmacro.js close
  "closes the osc"
  {:added "4.0"}
  [osc]
  (list '. osc (list 'close)))
