(ns xt.lang.common-client
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.common-lib :as lib]]})

(defn.js client-ws
  "Creates a WebSocket client for the debug REPL."
  {:added "4.0"}
  [host port opts]
  (var #{path secured} opts)
  (var conn (new WebSocket (+ "ws" (:? secured "s" "")
                              "://" host ":" port "/" (or path ""))))
  (var interval
       (. window (setInterval (fn [] (. conn (send "ping"))) 30000)))
  (. conn
     (addEventListener
      "message"
      (fn [msg]
        (when (== msg.data "pong")
          (return))
        (let [#{id body} (JSON.parse msg.data)
              out (lib/return-eval body)]
          (. conn (send (JSON.stringify {:id id
                                         :status "ok"
                                         :body out})))))))
  (. conn
     (addEventListener
      "close"
      (fn []
        (. window (clearInterval interval)))))
  (return conn))
