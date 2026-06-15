(ns hara.typed.xtalk-call
  (:require [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-compat :as compat]
            [hara.typed.xtalk-env :as env]
            [hara.typed.xtalk-form :as form]
            [hara.typed.xtalk-ops :as ops]))

(defn wildcard-callable?
  [type ctx]
  (let [type (compat/resolve-type type ctx)]
    (cond
      (= type types/+unknown-type+)
      true

      (compat/any-type? type)
      true

      (= :maybe (:kind type))
      (wildcard-callable? (:item type) ctx)

      (= :union (:kind type))
      (boolean (some #(wildcard-callable? % ctx) (:types type)))

      :else
      false)))

(defn callable-types
  [type ctx]
  (let [type (compat/resolve-type type ctx)]
    (cond
      (= :fn (:kind type))
      [type]

      (= :maybe (:kind type))
      (callable-types (:item type) ctx)

      (= :union (:kind type))
      (vec (mapcat #(callable-types % ctx) (:types type)))

      :else
      [])))

(defn call-arg-errors
  [arg-results expected-inputs args ctx]
  (mapcat (fn [arg-result expected arg-form]
            (when-not (compat/compatible-type? (:type arg-result) expected ctx)
              [{:tag :call-arg-type-mismatch
                :form arg-form
                :expected (types/type->data expected)
                :actual (types/type->data (:type arg-result))}]))
          arg-results
          expected-inputs
          args))

(defn optional-arity?
  [input-types provided-count ctx]
  (and (<= provided-count (count input-types))
       (every? #(compat/compatible-type? types/+nil-type+ % ctx)
               (drop provided-count input-types))))

(defn infer-function-call
  [[callee & args :as form] ctx]
  (let [callee-type (cond
                      (symbol? callee)
                      (env/lookup-symbol-type callee ctx)

                      :else
                      (:type ((:infer ctx) callee ctx)))
        arg-results (mapv #((:infer ctx) % ctx) args)
        errors (vec (mapcat :errors arg-results))
        callee-type (compat/resolve-type callee-type ctx)
        callable-types (callable-types callee-type ctx)
        wildcard? (wildcard-callable? callee-type ctx)]
    (if (seq callable-types)
      (let [arity-types (filterv #(optional-arity? (:inputs %) (count args) ctx)
                                 callable-types)]
        (if (seq arity-types)
          (let [passing-types (filterv (fn [fn-type]
                                         (every? true?
                                                 (map #(compat/compatible-type? (:type %1) %2 ctx)
                                                      arg-results
                                                      (take (count args) (:inputs fn-type)))))
                                       arity-types)
                chosen-types (if (seq passing-types) passing-types arity-types)
                arg-errors (if (seq passing-types)
                             []
                             (call-arg-errors arg-results
                                              (take (count args) (:inputs (first arity-types)))
                                              args
                                              ctx))]
            (compat/result (types/union-type (map :output chosen-types))
                           (concat errors arg-errors)))
          (if wildcard?
            (compat/result types/+unknown-type+ errors)
            (compat/result types/+unknown-type+
                           (concat errors
                                   [{:tag :call-arity-mismatch
                                     :form form
                                     :expected (mapv #(count (:inputs %)) callable-types)
                                     :actual (count args)}])))))
      (if wildcard?
        (compat/result types/+unknown-type+ errors)
        (compat/result types/+unknown-type+
                       (conj errors
                             {:tag :not-callable
                              :form form
                              :actual (types/type->data callee-type)}))))))

(def +builtin-rules+
  {'x:get-key form/infer-get-key
   'x:get-idx form/infer-get-idx
   'x:get-path form/infer-get-path
   'x:nil? (form/infer-fixed-output types/+bool-type+)
   'x:not-nil? (form/infer-fixed-output types/+bool-type+)
   'x:len (form/infer-fixed-output types/+int-type+)
   'x:cat (form/infer-fixed-output types/+str-type+)
   'x:json-encode (form/infer-fixed-output types/+str-type+)
   'x:to-string (form/infer-fixed-output types/+str-type+)
   'x:to-number (form/infer-fixed-output types/+num-type+)
   'x:str-split (form/infer-fixed-output {:kind :array
                                          :item types/+str-type+})
   'x:str-join (form/infer-fixed-output types/+str-type+)
   'x:is-function? (form/infer-fixed-output types/+bool-type+)
   'x:is-string? (form/infer-fixed-output types/+bool-type+)
   'x:is-number? (form/infer-fixed-output types/+bool-type+)
   'x:is-integer? (form/infer-fixed-output types/+bool-type+)
   'x:is-boolean? (form/infer-fixed-output types/+bool-type+)
   'x:is-object? (form/infer-fixed-output types/+bool-type+)
   'x:is-array? (form/infer-fixed-output types/+bool-type+)
   'x:iter-eq (form/infer-fixed-output types/+bool-type+)
   'x:iter-has? (form/infer-fixed-output types/+bool-type+)
   'x:iter-native? (form/infer-fixed-output types/+bool-type+)
   'x:obj-keys (form/infer-fixed-output {:kind :array
                                          :item types/+str-type+})
   'x:obj-vals form/infer-obj-vals
   'x:obj-pairs form/infer-obj-pairs
   'x:obj-clone form/infer-obj-clone
   'x:obj-assign form/infer-obj-assign
   'x:arr-clone form/infer-arr-clone})

(defn infer-op-spec-form
  [builtin-entry form ctx]
  (let [[_ & args] form
        arg-results (mapv #((:infer ctx) % ctx) args)
        self-type (some-> arg-results first :type)
        fn-types (ops/op-types builtin-entry (assoc ctx :self self-type))]
    (when (seq fn-types)
      (let [arg-results arg-results
            errors (vec (mapcat :errors arg-results))
            arity-types (filterv #(optional-arity? (:inputs %) (count args) ctx)
                                 fn-types)]
        (if (seq arity-types)
          (let [passing-types (filterv (fn [fn-type]
                                         (every? true?
                                                 (map #(compat/compatible-type? (:type %1) %2 ctx)
                                                      arg-results
                                                      (take (count args) (:inputs fn-type)))))
                                       arity-types)
                chosen-types (if (seq passing-types) passing-types arity-types)
                arg-errors (if (seq passing-types)
                             []
                             (call-arg-errors arg-results
                                              (take (count args) (:inputs (first arity-types)))
                                              args
                                              ctx))]
            (compat/result (types/union-type (map :output chosen-types))
                           (concat errors arg-errors)))
          (compat/result types/+unknown-type+
                         (concat errors
                                 [{:tag :call-arity-mismatch
                                   :form form
                                   :expected (or (ops/op-arglists builtin-entry)
                                                 (mapv #(count (:inputs %)) fn-types))
                                   :actual (count args)}])))))))

(defn infer-builtin-form
  [builtin-entry form ctx]
  (or (when-let [rule (get +builtin-rules+ (:canonical-symbol builtin-entry))]
        (rule form ctx))
      (infer-op-spec-form builtin-entry form ctx)))
