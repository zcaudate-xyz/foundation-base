(ns js.react.helper-data
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.react :as r]
             [js.core :as j]]
   :export [MODULE]})

(def.js __WRAPPED__ ((:- "Symbol") "__WRAPPED__"))

(def.js WrappedContext
  (r/createContext nil))

(def.js WrappedCache
  (new Map))

(defn.js wrapMemoize
  "wraps a component to pass data"
  {:added "4.0"}
  [component wrap-fn]
  (var cached (. -/WrappedCache (get component)))
  (when cached
    (return cached))

  (var wrapped (wrap-fn component))
  (. -/WrappedCache (set component wrapped))
  (return wrapped))

(defn.js useWrappedComponent
  "uses a wrappedComponent"
  {:added "4.0"}
  [component #{[$id
                $data
                (:.. props)]}]
  (var top  (r/useContext -/WrappedContext))

  (when (not $id)
    (return (r/createElement
             (. -/WrappedContext Provider)
             {:value #{[(:.. top) (:.. $data)]}}
             (r/createElement component props))))

  (var ntop (j/assignNew (and top (. top [(+ "$." $id)]))
                         $data))
  
  (var nprops (j/fromEntries
               (. (j/entries ntop)
                  (filter (fn [[k]]
                            (return
                             (not (. k (startsWith "$")))))))))
  
  (var wrapped (r/createElement
                component
                (j/assignNew props nprops)))
  
  (return
   (r/createElement (. -/WrappedContext Provider)
                    {:value ntop}
                    wrapped)))

(defn.js wrapData
  "wraps the data"
  {:added "4.0"}
  [component displayName]
  (return
   (-/wrapMemoize
    component
    (fn [component]
      (var WrappedComponent
           (fn [props]
             (return (-/useWrappedComponent component props))))

      (when displayName
        (:= (. WrappedComponent displayName)
            displayName))
      
      (:= (. WrappedComponent [-/__WRAPPED__])
          true)

      (return WrappedComponent)))))

(defn.js wrapForward
  "allows :ref to be passed"
  {:added "4.0"}
  [component displayName]
  (return
   (-/wrapData
    (r/forwardRef
     (fn ForwardInner [props ref]
       (return
        (r/createElement component (j/assign {:ref ref} props)))))
    displayName)))

(def.js MODULE (!:module))

