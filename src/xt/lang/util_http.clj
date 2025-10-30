(ns xt.lang.util-http
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k :suppress true]
             [js.core :as j]]
   :export [MODULE]})

(defn.js fetch-call
  "completes a http call with options
   ;;^:hidden
 
   #_#_#_
   (notify/wait-on :js
     (-> (http/fetch-call (+ \"http://localhost:\"
                             (@! (:http-port (l/default-notify))))
                          {:as \"text\"})
         (. (then (repl/>notify)))))
   => \"OK\""
  {:added "4.0"}
  [url options]
  (var as-ret (. options ["as"]))
  (var p (fetch url options))

  (cond (k/nil? as-ret)
        (return p)

        (== as-ret "json")
        (return (. p (then (fn:> [res] (res.json)))))

        (== as-ret "text")
        (return (. p (then (fn:> [res] (res.text)))))))

(defn.js es-connect
  "connects to an event source
   
   (notify/wait-on :js
     (var es (http/es-connect
              (@! (str \"http://localhost:\" (:port (l/annex:get :es))
                       \"/eval/es\"))
              {:on-message (fn [msg]
                             (repl/notify msg.data)
                             (es.close))})))
   => \"TEST-5\""
  {:added "4.0"}
  [url
   opts]
  (var es (new EventSource url opts))
  (var #{on-open
         on-message
         on-close} opts)
  (when on-message
    (es.addEventListener "message" on-message))
  (when on-open
    (es.addEventListener "open" on-open))
  (when on-close
    (es.addEventListener "close" on-close))
  (return es))

(defn.js es-active?
  "checks if event source is active"
  {:added "4.0"}
  [conn]
  (return true))

(defn.js es-close
  "closes the event source"
  {:added "4.0"}
  [conn]
  (conn.close))

(defn.js ws-connect
  "connects to a websocket source"
  {:added "4.0"}
  [url
   opts]
  (var es (new WebSocket url))
  (var #{on-open
         on-message
         on-close
         on-error} opts)
  (var interval nil)
  (when on-message
    (es.addEventListener "message" on-message))
  (when on-open
    (es.addEventListener "open" on-open))
  (when on-close
    (es.addEventListener "close" on-close))
  (when on-error
    (es.addEventListener "error" on-error))
  (return es))

(defn.js ws-active?
  "checks if websocket is active"
  {:added "4.0"}
  [conn]
  (return
   (== (. conn readyState) WebSocket.OPEN)))

(defn.js ws-close
  "closes the websocket"
  {:added "4.0"}
  [conn]
  (return
   (. conn (close))))

(defn.js ws-send
  "sends text through websocket"
  {:added "4.0"}
  [conn text]
  (return
   (. conn (send text))))

;;
;; XTALK 
;;

(l/script :xtalk
  {:export [MODULE]})

(defabstract.xt fetch-call [url options])

(defabstract.xt es-connect [url
                            opts])

(defabstract.xt es-active? [conn])

(defabstract.xt es-close [conn])

(defabstract.xt ws-connect [url
                            opts])

(defabstract.xt ws-active? [conn])

(defabstract.xt ws-close [conn])

(defabstract.xt ws-send [conn text])


(def.xt MODULE (!:module))

