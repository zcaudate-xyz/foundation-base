(ns xt.db.system.impl-common-ws
  (:require [hara.lang :as l]))

(l/script :xtalk)

(defabstract.xt create-ws-client
  [defaults])

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [js.net.ws-native :as js-ws]]})

(defn.js create-ws-client
  [defaults]
  (return
   (js-ws/create defaults)))
