(ns xt.db.system.main-ws
  (:require [hara.lang :as l]))


(l/script :xtalk)

(defabstract.xt create-ws-client
  [type defaults])

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [js.net.ws-native :as js-ws]]})

(defn.js create-ws-client
  [type defaults]
  (cond (== type "ws")
        (return (js-ws/create defaults))))
