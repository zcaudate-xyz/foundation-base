(ns js.lib.react-hook-form
  (:require [hara.lang :as l]))

(l/script :js
  {:import  [["react-hook-form" :as #{useForm Controller}]
             ["@hookform/resolvers/zod" :as #{zodResolver}]
             ["zod" :as #{z}]
             ["i18next" :as I18n]]
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [js.react :as r]]})

(def.js useFormBase useForm)

(def.js FormController Controller)

(def.js ZodResolver zodResolver)

(def.js Z z)

(def.js t
  (fn [x] (return x))
  #_(. I18n t))

(defn.js useFormState
  [props]
  (var defaultValues (xt/x:get-key props "defaultValues"))
  (var schema (xt/x:get-key props "schema"))
  (var rprops (Object.assign {}
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
  (return m))

(defn.js useControls
  [props]
  (return props))

(defn.js mergeContexts
  [& contexts]
  (return (Object.assign {} contexts)))
