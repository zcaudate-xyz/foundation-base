(ns js.net.http-websocket
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-websocket :as websocket]]})

(defn.js request-http-raw
  [input]
  (var #{url
         method
         headers
         body} input)
  (return (. (websocket url {"method" method
                             "headers" headers
                             "body" body})
             (then (fn [res]
                     (return (. res
                                (text)
                                (then (fn [text]
                                        (return {"status" (. res ["status"])
                                                 "headers" (. res ["headers"])
                                                 "body" text}))))))))))


(defn.js connect-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client opts])

(defn.js disconnect-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client])

(defn.js send-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client input])

(defn.js add-listeners-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client m])

(defn.js create-methods
  []
  (return
   {"connect"          -/connect-ws
    "disconnect"       -/disconnect-ws
    "send"             -/send-ws
    "add_listeners"    -/add-listeners-ws}))

(defn.js create
  [defaults]
  (return
   (websocket/create-base nil
                          (-/create-methods)
                          defaults)))
