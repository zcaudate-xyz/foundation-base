(ns hara.typed.xtalk-infer
  (:require [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-compat :as compat]
            [hara.typed.xtalk-env :as env]
            [hara.typed.xtalk-form :as form]
            [hara.typed.xtalk-call :as call]
            [hara.typed.xtalk-intrinsic :as intrinsic]
            [hara.typed.xtalk-lower :as lower]
            [hara.typed.xtalk-ops :as ops]
            [hara.typed.xtalk-parse :as parse]))

(declare intrinsic-callbacks)

(defn infer-type
  [form ctx]
  (let [ctx (if (:infer ctx) ctx (assoc ctx :infer infer-type))
        file (or (some-> form meta :file) (get-in ctx [:loc :file]))
        form-loc (types/source-loc form file)
        loc (merge (:loc ctx) form-loc)]
    (compat/with-loc
      (cond
        (or (nil? form)
            (string? form)
            (number? form)
            (boolean? form)
            (keyword? form))
        (compat/result (compat/literal-type form))

        (map? form)
        (form/infer-map form ctx)

        (vector? form)
        (form/infer-vector form ctx)

        (symbol? form)
        (compat/result (env/lookup-symbol-type form ctx))

        (seq? form)
        (let [lowered (lower/lower-form form ctx)]
          (if (not= lowered form)
            (infer-type lowered ctx)
            (let [op (first form)
                  resolved-op (if (symbol? op)
                                (compat/resolve-local-symbol op ctx)
                                op)
                  builtin-entry (when (symbol? resolved-op)
                                  (ops/canonical-entry resolved-op))
                  intrinsic-out (intrinsic/infer-intrinsic form ctx (intrinsic-callbacks))
                  builtin-out (when builtin-entry
                                (call/infer-builtin-form builtin-entry form ctx))]
              (if intrinsic-out
                intrinsic-out
                (if builtin-out
                  builtin-out
                  (case op
                    :- (form/infer-free form ctx)
                    do (form/infer-body (rest form) ctx)
                    return (infer-type (second form) ctx)
                    let (form/infer-let form ctx)
                    var (form/infer-binding-form form ctx)
                    := (form/infer-binding-form form ctx)
                    if (form/infer-if form ctx)
                    cond (form/infer-cond form ctx)
                    when (form/infer-when form ctx)
                    while (form/infer-while form ctx)
                    yield (form/infer-yield form ctx)
                    fn (form/infer-anon-fn form ctx)
                    = (compat/result types/+bool-type+
                                     (mapcat :errors (map #((:infer ctx) % ctx) (rest form))))
                    == (compat/result types/+bool-type+
                                      (mapcat :errors (map #((:infer ctx) % ctx) (rest form))))
                    not= (compat/result types/+bool-type+
                                        (mapcat :errors (map #((:infer ctx) % ctx) (rest form))))
                    and (compat/result types/+bool-type+
                                       (mapcat :errors (map #((:infer ctx) % ctx) (rest form))))
                    or (form/infer-or form ctx)
                    not (compat/result types/+bool-type+
                                       (compat/merge-errors ((:infer ctx) (second form) ctx)))
                    xt.lang.common-lib/not-empty? (compat/result types/+bool-type+
                                                                 (compat/merge-errors ((:infer ctx) (second form) ctx)))
                    xt.lang.common-lib/is-empty? (compat/result types/+bool-type+
                                                                (compat/merge-errors ((:infer ctx) (second form) ctx)))
                    xt.lang.common-lib/arrayify (let [arg-out ((:infer ctx) (second form) ctx)
                                                      arg-type (form/arrayify-type (:type arg-out) ctx)]
                                                  (compat/result arg-type
                                                                 (:errors arg-out)))
                    xt.event.base-listener/blank-container (form/infer-blank-container form ctx)
                    xt.event.base-listener/make-container (form/infer-make-container form ctx)
                    (if (keyword? op)
                      (form/infer-keyword-call form ctx)
                      (call/infer-function-call form ctx))))))))

      :else
      (compat/result types/+unknown-type+
                     [{:tag :unsupported-form
                       :form form}]))
      loc)))

(defn intrinsic-callbacks
  []
  {:result compat/result
   :infer-type infer-type
   :resolve-type compat/resolve-type
   :arrayify-type form/arrayify-type
   :infer-get-key form/infer-get-key
   :infer-get-path form/infer-get-path
   :infer-obj-assign form/infer-obj-assign
   :infer-make-container form/infer-make-container
   :infer-blank-container form/infer-blank-container})
