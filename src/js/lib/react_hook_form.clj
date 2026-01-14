(ns js.lib.react-hook-form
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.react :as r]]
   :import  [["react-hook-form" :as #{useForm Controller}]
             ["@hookform/resolvers/zod" :as #{zodResolver}]
             ["zod" :as #{z}]
             ["i18next" :as I18n]]})

(def.js useFormBase useForm)

(def.js FormController Controller)

(def.js ZodResolver zodResolver)

(def.js Z z)

(def.js t
  (fn [x] (return x))
  #_(. I18n t))


(defn.js useFormState
  [{:# [defaultValues
        schema]
    :.. props}]
  (var rprops (k/obj-assign
               (:? schema {:resolver (-/ZodResolver schema)}
                   {})
               props))
  (return
   (-/useFormBase
    {:defaultValues defaultValues
     :mode "onChange"
     :reValidateMode "onChange"
     :.. rprops})))

(defn.js useFormStateMap
  [m]
  (return
   (k/obj-map m -/useFormState)))

(defn.js useControls
  [(:= keys [])]
  (var arr      (r/useMemo
                 (fn []
                   (return
                    (. keys (map (fn [x]
                                   (return (:? (k/is-string? x)
                                               [x nil]
                                               x)))))))
                 [keys]))
  (var mgetters (r/useMemo
                 (fn []
                   (return
                    (. arr (reduce (fn [out [x init]]
                                     (:= (. out [x])
                                         (:? (k/is-function? init)
                                             (init)
                                             init))
                                     (return out))
                                   {}))))
                 [arr]))
  
  (var [state setState] (r/useState mgetters))
  
  (var msetters (r/useMemo
                 (fn []
                   (return
                    (. arr (reduce (fn [out [x init]]
                                     (:= (. out [(+ "set" (k/capitalize x))])
                                         (fn [val]
                                           (setState (fn [prev] 
                                                       (return (Object.assign
                                                                {}
                                                                prev
                                                                {x val}))))))
                                     (return out))
                                   {}))))
                 [arr]))
  
  (return (Object.assign {} msetters state)))

