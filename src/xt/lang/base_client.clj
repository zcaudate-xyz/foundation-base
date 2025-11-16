(ns xt.lang.base-client
  (:require [std.lang :as l]
            [xt.lang.base-repl :as repl]))

(l/script :js
  {:require [[xt.lang.base-repl :as repl]
             [js.core :as j]]})

(defn.js client-basic
  "creates a basic client"
  {:added "4.0"}
  [host port opts]
  (let [net (eval "require('net')")
        rl  (eval  "require('readline')")
        conn (new net.Socket)
        _      (conn.connect port host)
        _      (. conn (on "error" (fn:>)))
        _      (. conn (on "end" (fn:>)))
        _      (. conn (on "connect"
                           (fn []
                             (var stream (rl.createInterface conn conn))
                             (stream.on
                              "line"
                              (fn [line]
                                (conn.write (+ (repl/return-eval
                                                (JSON.parse line))
                                               "\n")))))))
        _      (. (eval "require('process')")
                  (on "unhandledRejection" (fn:>)))]
    (return conn)))

(defn.js client-ws
  "creates a basic websocket client"
  {:added "4.0"}
  [host port opts]
  (var #{path secured} opts)
  (var conn   (new WebSocket (+ "ws" (:? secured "s" "")
                                "://" host ":" port "/" (or path  ""))))
  (var interval (j/setInterval (fn []
                                 (. conn (send "ping")))
                               30000))
  (. conn
     (addEventListener
      "message"
      (fn [msg]
        (when (== msg.data "pong")
          (return))
        
        (let [#{id body} (JSON.parse msg.data)
              out (repl/return-eval body)]
          (. conn (send (JSON.stringify {:id id
                                         :status "ok"
                                         :body out})))))))
  (. conn
     (addEventListener
      "close"
      (fn []
        (j/clearInterval interval))))
  (return conn))

(def.js MODULE (!:module))

(l/script :lua
  {:require [[xt.lang.base-repl :as repl]]})

(l/script :python
  {:require [[xt.lang.base-repl :as repl]]})
