(ns lua.nginx.websocket
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :lua
  {:require [[xt.lang.base-lib :as k]
             [xt.sys.cache-common :as cache]
             [lua.nginx :as n :include [:uuid]]]
   :import [["resty.websocket.server" :as ngxwsserver]
            ["cjson" :as cjson]
            ["resty.websocket.server" :as ngxwsserver]]})

(h/template-entries [l/tmpl-macro {:base "websocket"
                                   :inst "wb"
                                   :tag "lua"}]
  [[set_timeout []]
   [send_text   [text]]
   [send_binary [data]]
   [send_ping   [] {:optional [msg]}]
   [send_pong   [] {:optional [msg]}]
   [send_close  [] {:optional [code msg]}]
   [send_frame  [fin opcode payload]]
   [recv_frame]
   [fatal      nil {:property true}]])

;;
;; WS Service
;;

(defmacro.lua
  STREAM-FLAG-KEY
  "path to flag key - when set, will close all connections"
  {:added "4.0"}
  [name]
  (list 'cat "__stream__:" name ":__flag__"))

(defmacro.lua
  STREAM-ACTIVE-KEY
  "returns the number of active connections
 
   (ws/STREAM-ACTIVE-KEY \"TICKER\")
   => \"__stream__:TICKER:__active__\""
  {:added "4.0"}
  [name]
  (list 'cat "__stream__:" name ":__active__"))

;;
;;
;;

(defn.lua service-register
  "registers a ws service with nginx"
  {:added "4.0"}
  [name metadata setup]
  (local g     (cache/cache :GLOBAL))
  (local meta  (cache/meta-get "stream"))
  (if (. meta  [name])
    (return false)
    (do 
      (cache/set g (-/STREAM-ACTIVE-KEY name) 0)
      (cache/meta-assoc "stream" name
                        (k/obj-assign {:key name}
                                      metadata))
      (if setup (setup))
      (return true))))

(defn.lua service-unregister
  "unregisters a ws service with nginx"
  {:added "4.0"}
  [name teardown]
  (local g    (cache/cache :GLOBAL))
  (local meta (cache/meta-get "stream"))
  (if (not (. meta [name]))
    (return false))
  
  (local count (cache/get g (-/STREAM-ACTIVE-KEY name)))
  
  (when (== 0 count)
    (cache/meta-dissoc "stream" name)
    (cache/del g (-/STREAM-FLAG-KEY name))
    (cache/del g (-/STREAM-ACTIVE-KEY name))
    (if teardown (teardown))
    (return true))
  
  (error "CONNECTIONS STILL REMAIN"))

(defn.lua service-signal-flag
  "sets the flag for the service (killing all connections)"
  {:added "4.0"}
  [name]
  (local g   (cache/cache :GLOBAL))
  (cache/set g
             (-/STREAM-FLAG-KEY name)
             true)
  (return true))

(defn.lua service-reset-flag
  "clears the flag for the service allowing connections"
  {:added "4.0"}
  [name]
  (local g     (cache/cache :GLOBAL))
  (cache/del g (-/STREAM-FLAG-KEY name))
  (return true))

(defn.lua service-prep
  "helper function to check if registeration is valid"
  {:added "4.0"}
  [name]
  (local g     (cache/cache :GLOBAL))
  (local meta  (cache/meta-get "stream"))
  (if (not (. meta [name]))
    (error "SERVICE NOT REGISTERED"))
  (return meta))

(defn.lua service-add-connection
  "helper for ws-loop to add itself to registry"
  {:added "4.0"}
  [name uid conndata]
  (local g     (cache/cache :GLOBAL))
  (local meta  (-/service-prep name))
  (if (cache/get (cache/cache name) uid)
    (error "INSTANCE ALREADY ADDED"))
  
  (cache/set (cache/cache name)
          uid
          (cjson.encode (k/obj-assign
                         {:id uid
                          :group name
                          :started (os.time)}
                         (or conndata {}))))
  (cache/incr g (-/STREAM-ACTIVE-KEY name) 1)
  (return true))

(defn.lua service-remove-connection
  "helper for ws-loop to remove itself from registry"
  {:added "4.0"}
  [name uid]
  (local g     (cache/cache :GLOBAL))
  (local meta  (-/service-prep name))
  (when (cache/get (cache/cache name) uid)
    (cache/del (cache/cache name) uid)
    (cache/incr g (-/STREAM-ACTIVE-KEY name) -1)
    (return true))
  (return false))

(defn.lua connection-count
  "returns number of connections for the given"
  {:added "4.0"}
  [name]
  (local g     (cache/cache :GLOBAL))
  (local meta  (-/service-prep name))
  (return (cache/get g (-/STREAM-ACTIVE-KEY name))))

(defn.lua connection-list
  "lists all connection keys"
  {:added "4.0"}
  [name]
  (return (cache/list-keys (cache/cache name))))

(defn.lua connection-info
  "gets the connection info"
  {:added "4.0"}
  [name uid]
  (return (cjson.decode (cache/get (cache/cache name) uid))))

(defn.lua connection-info-update
  "updates the connection info"
  {:added "4.0"}
  [name uid f]
  (local info (cjson.decode (cache/get (cache/cache name) uid)))
  (local change (f info))
  (cache/set (cache/cache name) uid (cjson.encode change))
  (return change))

(defn.lua connection-purge
  "deletes the record for the connection (meaning the loop will shut down)"
  {:added "4.0"}
  [name uid]
  (cache/del (cache/cache name) uid)
  (return true))

;;
;; ES Service
;;

(defn.lua es-main
  "creates an event-stream service"
  {:added "4.0"}
  ([name
    stream
    conndata]
   ^LOOP
   (local uid    (n/uuid))
   (local state  {})
   (local g (cache/cache :GLOBAL))
   (when (cache/get g (-/STREAM-FLAG-KEY name))
     (return (n/exit 404)))
   (-/service-add-connection name uid conndata)

   (local #{setup
            main
            teardown}  stream)
   (local vars (:? setup (setup uid state) []))
   (local abort-fn
          (fn []
            (when teardown
              (pcall (fn []
                       (teardown uid vars state))))
            (-/service-remove-connection name uid)
            (:= ngx.status ngx.HTTP-OK)
            (return (n/exit 200))))
   (n/on-abort abort-fn)

   (while (and (not (n/worker-exiting))
               (cache/get (cache/cache name)
                          uid))
     (main uid vars state))

   (abort-fn)))

(defn.lua new-connection
  "creates a new websocket connection"
  {:added "4.0"}
  [m]
  (local '[ws err] (. ngxwsserver
                     (new (k/obj-assign
                           {:timeout 10000
                            :max-payload-len 65535}
                           m))))
  (when (not ws)
    (n/log n/ERR "failed to create stream: " err)
    (return (n/exit 444)))
  (return ws))

(defn.lua ws-stream-loop
  "streaming loop for passive clients"
  {:added "4.0"}
  [name
   conn
   uid
   handlers
   state]
  ^INIT
  (local #{setup
           main
           teardown}  handlers)
  (local vars (:? setup (setup conn uid state) []))
  ^LOOP
  (while (and (not (n/worker-exiting))
              (cache/get (cache/cache name)
                         uid))
    (when (-/fatal conn)
      (n/log n/ERR "Stream not working")
      (break))

    (main conn uid vars state))
  
  (if teardown
    (teardown conn uid vars state))
  (-/send-close conn))

(defn.lua ws-listener-loop
  "helper function for the main loop"
  {:added "4.0"}
  [name
   conn
   uid
   handlers
   state]
  ^INIT
  (local #{setup
           main
           teardown
           check} handlers)
  (local g (cache/cache :GLOBAL))
  (local vars (:? setup (setup conn uid state) []))
  
  ^LOOP
  (while (and (not (n/worker-exiting))
              (cache/get (cache/cache name)
                           uid)
              (not (cache/get g (-/STREAM-FLAG-KEY name))))
    (local '[data tag err] (-/recv-frame conn))
    (when (-/fatal conn)
      (n/log n/ERR "failed to receive frame: " err)
      (break))
    
    (cond (not data)
          (do (local '[bytes err] (-/send-ping conn))
              (when (not bytes)
                (n/log n/ERR "failed to send ping: " err)
                (break)))
          
          (== tag "close")
          (do (-/send-close conn 1000)
              (break))

          (== tag "ping")
          (do (local '[bytes err] (-/send-pong conn))
              (when (not bytes)
                (n/log n/ERR "failed to send ping: " err)
                (break)))

          (== tag "pong")
          (n/log n/INFO "pong recieved")

          :else
          (if main
                (main conn uid vars data state)))
    (when (and check (check conn uid vars state))
      (break)))

  (if teardown
    (teardown conn uid vars state)))

(defn.lua ws-main
  "constructs a main service loop"
  {:added "4.0"}
  ([name
    listener
    stream
    conndata
    opts]
   ^LOOP
   (local g      (cache/cache :GLOBAL))
   (local conn   (-/new-connection opts))
   (local uid    (n/uuid))
   (local state  {})
  
   
   (when (cache/get g (-/STREAM-FLAG-KEY name))
     (return (n/exit 404)))
   
   (-/service-add-connection name uid conndata)

   (if (and stream (. stream ["main"]))
     (n/thread-spawn (fn []
                       (-/ws-stream-loop name
                                         conn
                                         uid
                                         stream
                                         state))))
   
   (-/ws-listener-loop name
                       conn
                       uid
                       listener
                       state)
   (-/service-remove-connection name uid)
   (return (n/exit 200))))

(defn.lua ws-echo
  "constructs an echo server"
  {:added "4.0"}
  [name]
  (-/ws-main name
             {:main (fn [conn uid vars data]
                      (-/send-text conn data))}
             {}
             {}
             {}))

(defn.lua es-test-loop
  "runs as es-test-loop"
  {:added "4.0"}
  [name ms n f]
  (-/es-main name
             {:main (fn [uid state]
                      (while (> n 0)
                        (n/say "data: " (f n) "\n")
                        (:= n (- n 1))
                        (n/sleep (/ ms 1000)))
                      (cache/del (cache/cache name)
                                 uid))}
             {}))

(comment
  (./create-tests)
  )
