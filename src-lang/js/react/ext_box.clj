(ns js.react.ext-box
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt] [xt.lang.common-data :as xtd] [xt.lang.common-tree :as xtt] [xt.event.base-box :as event-box] [js.react :as r] [xt.lang.spec-promise :as promise]]})

(defn.js createBox
  "creates a box for react"
  {:added "4.0"}
  [initial]
  (return (event-box/make-box initial)))

(defn.js useListenBox
  "listens to the box out"
  {:added "4.0"}
  [box path meta]
  (var dataFn (r/useCallback
               (fn []
                 (return (xtd/clone-shallow
                          (event-box/get-data box path))))
               [box path]))
  (var [data setData] (r/local (dataFn)))
  (var path-str (xt/x:json-encode path))
  (r/watch [path-str]
    (var listener-id (. (Math.random)
                        (toString 36)
                        (substr 2 4)))
    (event-box/add-listener box listener-id path
                             (fn [_ _ _ _]
                               (setData (dataFn)))
                              meta)
    (var nData (dataFn))
    (when (not (xtt/eq-nested data nData))
      (setData nData))
    (return (fn [] (event-box/remove-listener box listener-id))))
  (return data))

(defn.js useBox
  "getters and setters for the box"
  {:added "4.0"}
  [box path meta]
  (var data (-/useListenBox box path meta))
  (var setData
       (r/useCallback
        (fn [value]
          (event-box/set-data box path value))
        [box path]))
  (return [data setData]))

(defn.js attachLocalStorage
  "attaches localstorage to the box"
  {:added "4.0"}
  [storage-key box listener-id path]
  (var initial (event-box/get-data box path))
  (when (and (not= (typeof localStorage) "undefined")
             localStorage.getItem)
    (var stored (. localStorage (getItem storage-key)))
    (when stored
      (try
        (:= stored (JSON.parse stored))
        (catch e
            (:= stored initial)))
      (event-box/set-data box path stored))
    
    
    (event-box/add-listener
     box
     listener-id
     path
       (fn [_ payload _ _]
         (promise/x:promise
           (fn []
             (. localStorage (setItem storage-key
                                      (JSON.stringify (. payload ["data"])))))))))
  (return box))

(def.js listenBox -/useListenBox)

(def.js ^{:arglists ([box path])}
  getData event-box/get-data)

(def.js ^{:arglists ([box path value])}
  setData event-box/set-data)

(def.js ^{:arglists ([box path])}
  delData event-box/del-data)

(def.js ^{:arglists ([box])}
  resetData event-box/reset-data)

(def.js ^{:arglists ([box path value])}
  mergeData event-box/merge-data)

(def.js ^{:arglists ([box path value])}
  appendData event-box/append-data)

(def.js ^{:arglists ([box listener-id path callback meta])}
  addListener event-box/add-listener)

(def.js ^{:arglists ([box listener-id])}
  removeListener event-box/remove-listener)
