(ns std.lang.model.spec-xtalk.typed-check
  (:require [std.lang.model.spec-xtalk.typed-common :as types]
            [std.lang.model.spec-xtalk.typed-infer :as infer]))

(defn function-env
  [fn-def]
  (into {}
        (map (fn [arg]
               [(:name arg) (:type arg)]))
        (:inputs fn-def)))

(defn check-fn-def
  [fn-def]
  (let [ctx {:env (function-env fn-def)
             :ns (symbol (:ns fn-def))
             :aliases (get (:body-meta fn-def) :aliases)
             :function fn-def}
        inferred (infer/infer-body (:raw-body fn-def) ctx)
        return-type (:type inferred)
        errors (cond-> (vec (:errors inferred))
                 (and (:output fn-def)
                      (not= (:output fn-def) types/+unknown-type+)
                      (not (infer/compatible-type? return-type (:output fn-def) ctx)))
                 (conj {:tag :return-type-mismatch
                        :function (types/current-function-symbol fn-def)
                        :expected (types/type->data (:output fn-def))
                        :actual (types/type->data return-type)}))]
    {:function (types/current-function-symbol fn-def)
     :declared {:inputs (mapv (fn [arg]
                                {:name (:name arg)
                                 :type (types/type->data (:type arg))})
                              (:inputs fn-def))
                :output (types/type->data (:output fn-def))}
     :return (types/type->data return-type)
     :errors errors}))

(defn check-function
  [fn-ref]
  (let [fn-def (cond
                 (instance? std.lang.model.spec_xtalk.typed_common.XtFnDef fn-ref)
                 fn-ref

                 (symbol? fn-ref)
                 (types/get-function fn-ref)

                 :else
                 nil)]
    (when fn-def
      (check-fn-def fn-def))))
