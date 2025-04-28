(ns js.react.ext-box
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-box :as event-box]
             [js.react :as r]
             [js.core :as j]]
   :export [MODULE]})

(defn.js makeBox
  "creates a box for react"
  {:added "4.0"}
  [initial]
  (return (r/const (event-box/make-box initial))))

(defn.js listenBox
  "listens to the box out"
  {:added "4.0"}
  [box path meta]
  (var getFn (fn:> (k/clone-shallow
                    (event-box/get-data box path))))
  (var [data changeData] (r/local getFn))
  (r/watch [(k/json-encode path)]
    (var listener-id (j/randomId 4))
    (event-box/add-listener box listener-id path
                            (fn [m]
                              (changeData getFn))
                            meta)
    (changeData getFn)
    (return (fn [] (event-box/remove-listener box listener-id))))
  (return data))

(defn.js useBox
  "getters and setters for the box"
  {:added "4.0"}
  [box path meta]
  (var data (-/listenBox box path meta))
  (var setData
       (r/const (fn [value]
                  (event-box/set-data box path value))))
  (return [data setData]))

(def.js MODULE (!:module))
