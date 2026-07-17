(ns xt.net.ws-native
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defprotocol.xt]])
  (:refer-clojure :exclude [send]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.net.http-util :as util]]})

(defprotocol.xt IWebsocket
  (connect [client opts])
  (disconnect [client])
  (send [client input])
  (add-listeners [client m]))

(defprotocol.xt IWebsocketHeartbeat
  (start-heartbeat [client name f interval])
  (stop-heartbeat  [client name]))

(defn.xt prepare-url
  [client input]
  (var #{url path} input)
  (if (not (xt/x:nil? url))
    (return url))
  
  (var #{defaults} client)
  (var #{secured
         host
         port
         basepath} defaults)
  
  (return (xt/x:cat "ws" (:? secured "s" "")
                    "://" host
                    ":"
                    (xt/x:to-string (or port 80))
                    (or basepath "")
                    (or path ""))))
