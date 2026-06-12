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

(defn.xt create-base
  [type methods defaults]
  (return
   (xt/x:obj-assign {"::" (or type "xt.net.http-websocket")
                     "defaults" (or defaults {})
                     "::/override" (or methods {})} {})))

