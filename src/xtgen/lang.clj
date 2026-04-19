(ns xtgen.lang
  (:require [clojure.string :as str]
            [std.block.template :as gen]))

(def ^:private GENERATE_COMMON_LIB_TEMPLATE
  "
(defmacro.xt ^{:standalone ~standalone} 
  ~target
  ~arglists   
  ~form)")

(defn generate-common-lib-input
  [{:keys [op-spec] :as e}]
  (let [target   (first (:symbol e))
        arglists (first (:arglists op-spec))
        form     (list target (first (:arglists op-spec)))]
    {'name     name
     'arglists arglists  
     'target   target
     'form     form
     'standalone true}))

(def ^:private +generate-common-lib+
  (gen/get-template GENERATE_COMMON_LIB_TEMPLATE
                    generate-common-lib-input))

(defn generate-common-lib
  [e]
  (gen/fill-template +generate-common-lib+ e))

(comment
  [std.lang.base.grammar-xtalk :as xtalk]
  (gen/fill-template +generate-common-lib+
                     (first std.lang.base.grammar-xtalk/+xt-common-array+))
  )
