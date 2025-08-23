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

(defn.js createWrappedComponent
  [Component #{[$id
                $data
                (:.. props)]}]
  (var top  (r/useContext -/WrappedContext))

  (when (not $id)
    (return (r/createElement
             (. -/WrappedContext Provider)
             {:value #{[(:.. top) (:.. $data)]}}
             (r/createElement Component props))))

  (var ntop (j/assignNew (and top (. top [(+ "$." $id)]))
                         $data))
  
  (var nprops (j/fromEntries
               (. (j/entries ntop)
                  (filter (fn [[k]]
                            (return
                             (not (. k (startsWith "$")))))))))
  
  (var wrapped (r/createElement
                Component
                (j/assignNew props nprops)))
  
  (return
   (r/createElement (. -/WrappedContext Provider)
                    {:value ntop}
                    wrapped)))

(defn.js wrapData
  [Component]
  (var WrappedComponent
       (fn [props]
         (return (-/createWrappedComponent Component props))))

  (:= (. WrappedComponent displayName)
      (. Component displayName))
  
  (:= (. WrappedComponent [-/__WRAPPED__])
      true)

  (return WrappedComponent))

(def.js MODULE (!:module))
