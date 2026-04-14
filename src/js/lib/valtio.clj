(ns js.lib.valtio
  (:require [std.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [use val proxy]))

(l/script :js
  {:require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
              [js.core :as j]]
   :import [["valtio/vanilla" :as [* ValtioCore]]
            ["valtio/utils" :as [* ValtioUtils]]
            ["valtio" :as [* Valtio]]
            ["valtio" :as [* Valtio]]
            ["valtio/vanilla" :as [* ValtioCore]]
            ["valtio/utils" :as [* ValtioUtils]]]})

;;
;; valtio
;;

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ValtioCore"
                                   :tag "js"}]

  [getVersion
   proxy
   [proxyRef ref]
   snapshot
   subscribe])

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ValtioUtils"
                                   :tag "js"}]
  [[proxyAddComputed addComputed]
   proxyWithComputed])

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Valtio"
                                   :tag "js"}]
  [useSnapshot])

(defn.js make
  "makes a proxy with reset"
  {:added "4.0"}
  [m]
  (var out (-/proxy))
  (var val-fn (fn []
                (return (:? (xt/x:is-function? m)
                            (m)
                            (xtd/clone-nested m)))))
  (var __reset__ (fn []
                   (xt/for:object [[k _] out]
                      (k/del-key out k))
                    (j/assign out (val-fn))))
  (j/defineProperty out "__reset__"
    {:value __reset__
     :writable false})
  (__reset__ out)
  (return out))

(defn.js reset
  "resets proxy to original"
  {:added "4.0"}
  [pobj m]
  (var #{__reset__} pobj)
  (if (xt/x:is-function? __reset__)
    (__reset__)
    (xt/for:object [[k _] pobj]
      (k/del-key pobj k)))
  (return (j/assign pobj m)))

(defn.js useVal
  "uses only the getter"
  {:added "4.0"}
  ([pobj f := k/identity]
   (let [snap      (-/useSnapshot pobj)]
     (return (:?  (xt/x:is-function? f)
                   (f snap)
                   
                   (xt/x:is-array? f)
                   (. f (reduce (fn [acc k]
                                  (return (. acc [k])))
                                snap))
                  
                  :else snap)))))

(defmacro.js val
  "macro for `useVal`"
  {:added "4.0"}
  [sym & more]
  (let [more (if (or (keyword? (first more))
                     (string? (first more)))
                 [(mapv f/strn more)]
                 more)]
    (apply list `useVal sym more)))

(defmacro.js listen
  "listens for store values"
  {:added "4.0"}
  [syms]
  (mapv (fn [sym]
          (list `useVal sym))
        syms))

;;
;; Accessors
;;

(defn.js getAccessors
  "creates accessors on the proxy"
  {:added "4.0"}
  ([pobj]
   (let [getFn   (fn []
                   (return (k/obj-omit (-/useSnapshot pobj)
                                       ["__reset__"])))
         setFn   (fn [m]
                   (return (j/assign pobj m)))
          resetFn (fn [m]
                    (xt/for:object [[k _] pobj]
                      (k/del-key pobj k))
                    (j/assign pobj m))]
     (return [getFn setFn resetFn pobj]))))

(defn.js getFieldAccessors
  "creates field accessors on the proxy"
  {:added "4.0"}
  ([pobj field]
   (let [getFVal   (fn []
                     (return (. (-/useSnapshot pobj) [field])))
         setFVal   (fn [v]
                     (:= (. pobj [field]) v))
         resetFVal (fn [m]
                     #_#_
                     (k/swap-key pobj field)
                     (k/aset pobj field
                             (k/obj-assign
                              (xt/x:get-key (:? initFn (initFn) {})
                                          field)
                               extra)))]
     (return [getFVal setFVal resetFVal pobj]))))

(defn.js useProxy
  "uses the proxy object directly or via id lookup"
  {:added "4.0"}
  ([pobj]
   (let [[getFn
          setVal
          resetVal]  (-/getAccessors pobj)]
     (return [(getFn) setVal resetVal]))))

(defn.js useProxyField
  "uses the proxy object field directly or via id lookup"
  {:added "4.0"}
  ([pobj field]
     (let [[getFVal
            setFVal
            resetFVal] (-/getFieldAccessors pobj field)]
       (return [(getFVal) setFVal resetFVal]))))

(defn.js wrapProxyField
  "wraps a component with `record` and `field`"
  {:added "4.0"}
  [Component [getter setter]]
  (return
   (fn [#{[record
           field
           (:= callbacks [])
           (:.. rprops)]}]
     (let [[value setValue] (-/useProxyField record field)
           tprops (j/assign {getter value
                             setter (fn [out]
                                      (setValue out)
                                      (. callbacks (map (fn:> [f] (f out)))))}
                            rprops)]
       (return [:% Component #{(:.. tprops)}])))))

(def create-use-fn
  (fn [sym field proxyFn fieldFn]
    (if (not field)
      (list proxyFn  sym)
      (let [field (if (keyword? field)
                      (.replaceAll (name field) "-" "_")
                      field)]
        (list fieldFn sym field)))))

(defmacro.js use
  "uses the proxy"
  {:added "4.0"}
  ([sym & [field]]
   (create-use-fn sym field
                  'js.lib.valtio/useProxy
                  'js.lib.valtio/useProxyField)))

(defn.js useData
  "data function helper"
  {:added "4.0"}
  [pobj initial]
  (:= initial (or initial (fn:> {})))
  (var [getFn setFn setData] (-/getAccessors pobj))
  (var data (getFn))
  (var getKey (fn:> [key] (xt/x:get-key data key)))
  (var setKey (fn:> [key value] (setFn {key value})))
  (var toggleKey (fn [key]
                   (return (fn []
                             (setFn {key (not (getKey key))})))))
  (var fnKey  (fn [key]
                (return (fn [value]
                          (setFn {key value})))))
  (var resetData (fn [] (return (setData (initial)))))
  (var resetKey  (fn [key] (return (setKey (. (initial) [key])))))
  (return #{setKey
            fnKey
            toggleKey
            getKey
            resetKey
            data
            setData
            resetData}))

(comment)
