(ns xt.net.http-websocket
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defprotocol.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.net.http-util :as util]]})

(defprotocol.xt IWebsocket
  (connect [client opts])
  (disconnect [client])
  (send [client input])
  (add-listeners [client m]))

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
                    (or port "80")
                    (or basepath "")
                    (or path ""))))


