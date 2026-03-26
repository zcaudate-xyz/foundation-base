;; Deprecated snapshot of the pre-rebuild xtalk typed engine.
;; Keep for reference while the new core is rebuilt around grammar-derived builtin typing.

(ns std.lang.typed.deprecated.xtalk-infer
  (:require [std.lang.typed.xtalk-common :as types]
            [std.lang.typed.deprecated.xtalk-intrinsic :as intrinsic]
            [std.lang.typed.deprecated.xtalk-lower :as lower]
            [std.lang.typed.xtalk-ops :as ops]
            [std.lang.typed.xtalk-parse :as parse]))

(declare infer-body
         infer-type
         infer-get-key
         infer-get-idx
         infer-get-path
         infer-obj-assign
         infer-make-container
         infer-blank-container)

(defn result
  ([type] (result type []))
  ([type errors]
   {:type type
    :errors (vec errors)})
  ([type errors env]
   {:type type
    :errors (vec errors)
    :env env}))

(defn merge-errors
  [& results]
  (vec (mapcat :errors results)))

(defn resolve-local-symbol
  [sym {:keys [ns aliases]}]
  (cond
    (not (symbol? sym))
    sym

    (ops/builtin? sym)
    sym

    (= "-" (namespace sym))
    (symbol (str ns) (name sym))

    (namespace sym)
    (if-let [alias-ns (get aliases (symbol (namespace sym)))]
      (symbol (str alias-ns) (name sym))
      sym)

    ns
    (symbol (str ns) (name sym))

    :else
    sym))

(defn resolve-type
  ([type ctx]
   (resolve-type type ctx #{}))
  ([type ctx seen]
   (if (and (= :named (:kind type))
            (symbol? (:name type))
            (not (contains? seen (:name type))))
     (if-let [spec (types/get-spec (:name type))]
       (recur (:type spec) ctx (conj seen (:name type)))
       type)
     type)))

(defn type-eq?
  [left right]
  (= (types/type->data left)
     (types/type->data right)))

(defn compatible-type?
  [actual expected ctx]
  (let [actual (resolve-type actual ctx)
        expected (resolve-type expected ctx)]
    (cond
      (and (= :primitive (:kind expected))
           (= :xt/any (:name expected)))
      true

      (= expected types/+unknown-type+)
      true

      (= actual types/+unknown-type+)
      false

      (type-eq? actual expected)
      true

      (and (= :primitive (:kind actual))
           (= :primitive (:kind expected))
           (= :xt/int (:name actual))
           (= :xt/num (:name expected)))
      true

      (= :maybe (:kind expected))
      (or (compatible-type? actual types/+nil-type+ ctx)
          (compatible-type? actual (:item expected) ctx))

      (= :maybe (:kind actual))
      (and (compatible-type? types/+nil-type+ expected ctx)
           (compatible-type? (:item actual) expected ctx))

      (= :union (:kind expected))
      (some #(compatible-type? actual % ctx) (:types expected))

      (= :union (:kind actual))
      (every? #(compatible-type? % expected ctx) (:types actual))

      (and (= :record (:kind actual))
           (= :record (:kind expected)))
      (every? (fn [{:keys [name type]}]
                (or (when-let [actual-field (some #(when (= name (:name %)) %)
                                                 (:fields actual))]
                      (compatible-type? (:type actual-field) type ctx))
                    (when-let [open (:open actual)]
                      (compatible-type? (:value open) type ctx))))
              (:fields expected))

      (and (= :record (:kind actual))
           (= :dict (:kind expected)))
      (and (every? (fn [{:keys [type]}]
                     (compatible-type? type (:value expected) ctx))
                   (:fields actual))
           (if-let [open (:open actual)]
             (and (compatible-type? (:key open) (:key expected) ctx)
                  (compatible-type? (:value open) (:value expected) ctx))
             true))

      (and (= :dict (:kind actual))
           (= :dict (:kind expected)))
      (and (compatible-type? (:key actual) (:key expected) ctx)
           (compatible-type? (:value actual) (:value expected) ctx))

      (and (= :apply (:kind actual))
           (= :apply (:kind expected)))
      (and (= (:target actual) (:target expected))
           (= (count (:args actual)) (count (:args expected)))
           (every? true?
                   (map #(compatible-type? %1 %2 ctx)
                        (:args actual)
                        (:args expected))))

      (and (= :fn (:kind actual))
           (= :fn (:kind expected)))
      (and (= (count (:inputs actual))
              (count (:inputs expected)))
           (every? true?
                   (map #(compatible-type? %1 %2 ctx)
                        (:inputs actual)
                        (:inputs expected)))
           (compatible-type? (:output actual) (:output expected) ctx))

      :else
      false)))

(defn literal-type
  [form]
  (cond
    (nil? form) types/+nil-type+
    (string? form) types/+str-type+
    (integer? form) types/+int-type+
    (number? form) types/+num-type+
    (boolean? form) types/+bool-type+
    (keyword? form) types/+kw-type+
    (map? form) (let [static-fields (->> form
                                         (keep (fn [[k v]]
                                                 (when (or (keyword? k)
                                                           (string? k))
                                                   {:name (types/field-key k)
                                                    :type (literal-type v)
                                                    :optional? false})))
                                         vec)
                      dynamic-values (->> form
                                          (keep (fn [[k v]]
                                                  (when-not (or (keyword? k)
                                                                (string? k))
                                                    (literal-type v))))
                                          vec)
                      out (cond-> {:kind :record
                                   :fields static-fields}
                            (seq dynamic-values)
                            (assoc :open {:key types/+unknown-type+
                                          :value (types/union-type dynamic-values)}))]
                 out)
    (vector? form) {:kind :array
                    :item (if (empty? form)
                            types/+unknown-type+
                            (types/union-type (map literal-type form)))}
    :else types/+unknown-type+))

(defn field-literal
  [form]
  (cond
    (keyword? form) (types/field-key form)
    (string? form) (types/field-key form)
    :else nil))

(defn literal-key-type
  [form]
  (cond
    (keyword? form) types/+kw-type+
    (string? form) types/+str-type+
    :else types/+unknown-type+))

(defn maybe-register-function!
  [resolved-sym]
  (let [ns-sym (some-> resolved-sym namespace symbol)]
    (when ns-sym
      (when-not (types/get-function resolved-sym)
        (try
          (-> ns-sym
              parse/analyze-namespace
              parse/register-types!)
          (catch clojure.lang.ExceptionInfo ex
            nil)))
      (types/get-function resolved-sym))))

(defn lookup-symbol-type
  [sym {:keys [env] :as ctx}]
  (or (get env sym)
      (let [resolved (resolve-local-symbol sym ctx)]
        (some-> (or (types/get-function resolved)
                    (maybe-register-function! resolved))
                types/fn-type))
      types/+unknown-type+))

(defn field-access-type
  [type key ctx]
  (let [type (resolve-type type ctx)
        key' (if (or (keyword? key)
                     (string? key)
                     (symbol? key))
               (types/field-key key)
               key)]
    (cond
      (= :maybe (:kind type))
      (types/maybe-type (field-access-type (:item type) key' ctx))

      (= :record (:kind type))
      (or (some (fn [{field-name :name field-type :type}]
                  (when (= key' field-name)
                    field-type))
                (:fields type))
          (when-let [open (:open type)]
            (when (compatible-type? (literal-key-type key') (:key open) ctx)
              (types/maybe-type (:value open))))
          types/+unknown-type+)

      (= :dict (:kind type))
      (types/maybe-type (:value type))

      :else
      types/+unknown-type+)))

(defn binding-decl
  [target ctx]
  (cond
    (symbol? target)
    {:symbol target
     :type (some-> target meta :- (types/normalize-return-meta ctx))}

    (and (seq? target)
         (= 2 (count target))
         (symbol? (second target)))
    {:symbol (second target)
     :type (types/normalize-type (first target) ctx)}

    :else
    nil))

(defn infer-map
  [form ctx]
  (let [entries (mapv (fn [[k v]]
                        (let [value-out (infer-type v ctx)]
                          (if (or (keyword? k)
                                  (string? k))
                            {:field-name (types/field-key k)
                             :value-out value-out}
                            {:dynamic-key-out (infer-type k ctx)
                             :value-out value-out})))
                      form)
        static-fields (->> entries
                           (keep (fn [{:keys [field-name value-out]}]
                                   (when field-name
                                     {:name field-name
                                      :type (:type value-out)
                                      :optional? false})))
                           vec)
        dynamic-entries (->> entries
                             (keep (fn [{:keys [dynamic-key-out value-out]}]
                                     (when dynamic-key-out
                                       {:key-type (:type dynamic-key-out)
                                        :value-type (:type value-out)
                                        :errors (concat (:errors dynamic-key-out)
                                                        (:errors value-out))})))
                             vec)
        open (when (seq dynamic-entries)
               {:key (types/union-type (map :key-type dynamic-entries))
                :value (types/union-type (map :value-type dynamic-entries))})
        errors (vec (mapcat (fn [{:keys [value-out dynamic-key-out]}]
                              (concat (:errors value-out)
                                      (:errors dynamic-key-out)))
                            entries))]
    (result (cond-> {:kind :record
                     :fields static-fields}
              open (assoc :open open))
            errors)))

(defn infer-vector
  [form ctx]
  (let [item-results (mapv #(infer-type % ctx) form)]
    (result {:kind :array
             :item (if (empty? item-results)
                     types/+unknown-type+
                     (types/union-type (map :type item-results)))}
            (mapcat :errors item-results))))

(defn binding-updates
  [target expr-type ctx]
  (cond
    (symbol? target)
    {target expr-type}

    (and (vector? target)
         (every? symbol? target))
    (let [resolved (resolve-type expr-type ctx)
          item-type (cond
                      (= :array (:kind resolved))
                      (:item resolved)

                      (= :tuple (:kind resolved))
                      nil

                      :else
                      types/+unknown-type+)]
      (if (= :tuple (:kind resolved))
        (into {}
              (map-indexed (fn [idx sym]
                             [sym (or (nth (:types resolved) idx nil)
                                      types/+unknown-type+)]))
              target)
        (into {}
              (map (fn [sym]
                     [sym item-type]))
              target)))

    (and (set? target)
         (every? symbol? target))
    (let [resolved (resolve-type expr-type ctx)]
      (into {}
            (map (fn [sym]
                   [sym (field-access-type resolved (types/field-key sym) ctx)]))
            target))

    :else
    nil))

(defn infer-binding-form
  [[_ target expr] ctx]
  (let [expr-out (infer-type expr ctx)
        updates (binding-updates target (:type expr-out) ctx)
        env (or (:env ctx) {})
        next-env (if updates
                   (merge env updates)
                   env)
        errors (cond-> (vec (:errors expr-out))
                 (nil? updates)
                 (conj {:tag :unsupported-binding-target
                        :target target
                        :form expr}))]
    (result (:type expr-out) errors next-env)))

(defn infer-let
  [[_ bindings & body] ctx]
  (loop [env (:env ctx)
         errors []
         [target expr & more] bindings]
    (if (nil? target)
      (let [out (infer-body body (assoc ctx :env env))]
        (result (:type out)
                (concat errors (:errors out))))
      (let [{:keys [symbol type]} (or (binding-decl target ctx)
                                      (throw (ex-info "Unsupported let binding target"
                                                      {:target target})))
            inferred (infer-type expr (assoc ctx :env env))
            binding-errors (cond-> (:errors inferred)
                             (and type
                                  (not (compatible-type? (:type inferred) type ctx)))
                             (conj {:tag :binding-type-mismatch
                                    :form expr
                                    :binding symbol
                                    :expected (types/type->data type)
                                    :actual (types/type->data (:type inferred))}))
            bound-type (or type (:type inferred))]
        (recur (assoc env symbol bound-type)
               (concat errors binding-errors)
               more)))))

(defn infer-if
  [[_ cond-expr then-expr else-expr] ctx]
  (let [cond-out (infer-type cond-expr ctx)
        then-out (infer-type then-expr ctx)
        else-out (infer-type else-expr ctx)]
    (result (types/union-type [(:type then-out) (:type else-out)])
            (merge-errors cond-out then-out else-out))))

(defn infer-ternary
  [[_ cond-expr then-expr else-expr] ctx]
  (infer-if (list 'if cond-expr then-expr else-expr) ctx))

(defn infer-or
  [[_ & args] ctx]
  (let [arg-results (mapv #(infer-type % ctx) args)]
    (result (if (seq arg-results)
              (types/union-type (map :type arg-results))
              types/+nil-type+)
            (mapcat :errors arg-results))))

(defn infer-when
  [[_ cond-expr & body] ctx]
  (let [cond-out (infer-type cond-expr ctx)
        body-out (infer-body body ctx)]
    (result (types/union-type [types/+nil-type+ (:type body-out)])
            (merge-errors cond-out body-out))))

(defn infer-anon-fn
  [[_ args & body] ctx]
  (let [input-types (mapv (fn [_] types/+unknown-type+) args)
        env (merge (:env ctx)
                   (zipmap args input-types))
        body-out (infer-body body (assoc ctx :env env))]
    (result {:kind :fn
             :inputs input-types
             :output (:type body-out)}
            (:errors body-out))))

(defn merge-record-fields
  [left-fields right-fields]
  (vals
   (reduce (fn [acc field]
             (assoc acc (:name field) field))
           (into {}
                 (map (fn [field]
                        [(:name field) field]))
                  left-fields)
            right-fields)))

(defn merge-open-types
  [left right]
  (cond
    (and left right)
    {:key (types/union-type [(:key left) (:key right)])
     :value (types/union-type [(:value left) (:value right)])}

    left
    left

    :else
    right))

(defn infer-obj-assign
  [[_ target-expr & source-exprs] ctx]
  (let [parts (mapv #(infer-type % ctx) (cons target-expr source-exprs))
        part-types (mapv (comp #(resolve-type % ctx) :type) parts)
        record-fields (reduce (fn [acc part-type]
                                (if (= :record (:kind part-type))
                                  (merge-record-fields acc (:fields part-type))
                                  acc))
                              []
                              part-types)
        open-type (reduce (fn [acc part-type]
                            (cond
                              (= :record (:kind part-type))
                              (merge-open-types acc (:open part-type))

                              (= :dict (:kind part-type))
                              (merge-open-types acc {:key (:key part-type)
                                                     :value (:value part-type)})

                              :else
                              acc))
                          nil
                          part-types)]
    (result (cond
              (or (seq record-fields) open-type)
              (cond-> {:kind :record
                       :fields (vec record-fields)}
                open-type (assoc :open open-type))

              :else
              types/+unknown-type+)
            (mapcat :errors parts))))

(defn arrayify-type
  [type ctx]
  (let [type (resolve-type type ctx)]
    (cond
      (= :array (:kind type))
      type

      (= :union (:kind type))
      {:kind :array
       :item (types/union-type
              (map (fn [item-type]
                     (let [item-type (resolve-type item-type ctx)]
                       (if (= :array (:kind item-type))
                         (:item item-type)
                         item-type)))
                   (:types type)))}

      :else
      {:kind :array
       :item type})))

(defn infer-make-container
  [[_ initial-expr type-expr opts-expr] ctx]
  (let [initial-out (infer-type initial-expr ctx)
        type-out (infer-type type-expr ctx)
        opts-out (infer-type opts-expr ctx)
        initial-type (resolve-type (:type initial-out) ctx)
        opts-type (resolve-type (:type opts-out) ctx)
        data-type (cond
                    (= :fn (:kind initial-type))
                    (:output initial-type)

                    (= :union (:kind initial-type))
                    (types/union-type
                     (map (fn [member-type]
                            (let [member-type (resolve-type member-type ctx)]
                              (if (= :fn (:kind member-type))
                                (:output member-type)
                                member-type)))
                          (:types initial-type)))

                    :else
                    initial-type)
        base-fields [{:name "::" :type (:type type-out) :optional? false}
                     {:name "listeners"
                      :type {:kind :named
                             :name 'xt.lang.event-common/EventListenerMap}
                      :optional? false}
                     {:name "data" :type data-type :optional? false}
                     {:name "initial"
                      :type {:kind :fn
                             :inputs []
                             :output data-type}
                      :optional? false}]
        extra-fields (if (= :record (:kind opts-type))
                       (:fields opts-type)
                       [])
        out-type {:kind :record
                  :fields (vec (merge-record-fields base-fields extra-fields))}]
    (result out-type
            (merge-errors initial-out type-out opts-out))))

(defn infer-blank-container
  [[_ type-expr opts-expr] ctx]
  (let [type-out (infer-type type-expr ctx)
        opts-out (infer-type opts-expr ctx)
        opts-type (resolve-type (:type opts-out) ctx)
        base-fields [{:name "::" :type (:type type-out) :optional? false}
                     {:name "listeners"
                      :type {:kind :named
                             :name 'xt.lang.event-common/EventListenerMap}
                      :optional? false}]
        extra-fields (if (= :record (:kind opts-type))
                       (:fields opts-type)
                       [])
        open-type (cond
                    (= :record (:kind opts-type)) (:open opts-type)
                    (= :dict (:kind opts-type)) {:key (:key opts-type)
                                                 :value (:value opts-type)}
                    :else nil)
        out-type (cond-> {:kind :record
                          :fields (vec (merge-record-fields base-fields extra-fields))}
                   open-type (assoc :open open-type))]
    (result out-type
            (merge-errors type-out opts-out))))

(defn intrinsic-callbacks
  []
  {:result result
   :infer-type infer-type
   :resolve-type resolve-type
   :arrayify-type arrayify-type
   :infer-get-key infer-get-key
   :infer-get-path infer-get-path
   :infer-obj-assign infer-obj-assign
   :infer-make-container infer-make-container
   :infer-blank-container infer-blank-container})

(defn infer-builtin-form
  [builtin-op form ctx]
  (condp = builtin-op
    'x:get-key (infer-get-key form ctx)
    'x:get-idx (infer-get-idx form ctx)
    'x:get-path (infer-get-path form ctx)
    'x:nil? (result types/+bool-type+
                    (merge-errors (infer-type (second form) ctx)))
    'x:not-nil? (result types/+bool-type+
                        (merge-errors (infer-type (second form) ctx)))
    'x:len (result types/+int-type+
                   (merge-errors (infer-type (second form) ctx)))
    'x:cat (result types/+str-type+
                   (mapcat :errors (map #(infer-type % ctx) (rest form))))
    'x:json-encode (let [arg-out (infer-type (second form) ctx)]
                     (result types/+str-type+
                             (:errors arg-out)))
    'x:str-split (let [arg-outs (mapv #(infer-type % ctx) (rest form))]
                   (result {:kind :array
                            :item types/+str-type+}
                           (mapcat :errors arg-outs)))
    'x:str-join (let [arg-outs (mapv #(infer-type % ctx) (rest form))]
                  (result types/+str-type+
                          (mapcat :errors arg-outs)))
    'x:is-function? (result types/+bool-type+
                            (merge-errors (infer-type (second form) ctx)))
    'x:obj-keys (let [arg-out (infer-type (second form) ctx)]
                  (result {:kind :array
                           :item types/+str-type+}
                          (:errors arg-out)))
    'x:obj-assign (infer-obj-assign form ctx)
    nil))

(defn infer-function-call
  [[callee & args :as form] ctx]
  (let [callee-type (cond
                      (symbol? callee)
                      (lookup-symbol-type callee ctx)

                      :else
                      (:type (infer-type callee ctx)))
        arg-results (mapv #(infer-type % ctx) args)
        errors (vec (mapcat :errors arg-results))
        callee-type (resolve-type callee-type ctx)]
    (if (= :fn (:kind callee-type))
      (let [arity-ok? (= (count args) (count (:inputs callee-type)))
            arity-errors (if arity-ok?
                           []
                           [{:tag :call-arity-mismatch
                             :form form
                             :expected (count (:inputs callee-type))
                             :actual (count args)}])
            arg-errors (if arity-ok?
                         (mapcat (fn [arg-result expected arg-form]
                                   (when-not (compatible-type? (:type arg-result) expected ctx)
                                     [{:tag :call-arg-type-mismatch
                                       :form arg-form
                                       :expected (types/type->data expected)
                                       :actual (types/type->data (:type arg-result))}]))
                                 arg-results
                                 (:inputs callee-type)
                                 args)
                         [])]
        (result (:output callee-type)
                (concat errors arity-errors arg-errors)))
      (if (or (= callee-type types/+unknown-type+)
              (and (= :primitive (:kind callee-type))
                   (= :xt/any (:name callee-type))))
        (result types/+unknown-type+ errors)
        (result types/+unknown-type+
                (conj errors
                      {:tag :not-callable
                       :form form
                       :actual (types/type->data callee-type)}))))))

(defn infer-get-key
  [[_ obj-expr key-expr] ctx]
  (let [obj-out (infer-type obj-expr ctx)
        key-out (infer-type key-expr ctx)
        key (field-literal key-expr)
        obj-type (resolve-type (:type obj-out) ctx)
        errors (merge-errors obj-out key-out)
        errors (cond-> errors
                 (and (= :dict (:kind obj-type))
                      (not (compatible-type? (:type key-out) (:key obj-type) ctx)))
                 (conj {:tag :key-type-mismatch
                        :form key-expr
                        :expected (types/type->data (:key obj-type))
                        :actual (types/type->data (:type key-out))}))]
    (result (if key
              (field-access-type obj-type key ctx)
              (cond
                (= :dict (:kind obj-type))
                (types/maybe-type (:value obj-type))

                (and (= :record (:kind obj-type))
                     (:open obj-type))
                (types/maybe-type (get-in obj-type [:open :value]))

                :else
                types/+unknown-type+))
            errors)))

(defn infer-get-idx
  [[_ obj-expr idx-expr] ctx]
  (let [obj-out (infer-type obj-expr ctx)
        idx-out (infer-type idx-expr ctx)
        obj-type (resolve-type (:type obj-out) ctx)
        idx-literal (when (integer? idx-expr)
                      idx-expr)
        out-type (cond
                   (= :array (:kind obj-type))
                   (types/maybe-type (:item obj-type))

                   (= :tuple (:kind obj-type))
                   (if (some? idx-literal)
                     (or (nth (:types obj-type) idx-literal nil)
                         types/+unknown-type+)
                     (types/maybe-type (types/union-type (:types obj-type))))

                   :else
                   types/+unknown-type+)]
    (result out-type
            (merge-errors obj-out idx-out))))

(defn infer-get-path
  [[_ obj-expr path-expr default-expr] ctx]
  (let [obj-out (infer-type obj-expr ctx)
        default-out (when (some? default-expr)
                      (infer-type default-expr ctx))
        path (if (vector? path-expr)
               (mapv field-literal path-expr)
               [])
        base-type (reduce (fn [type key]
                            (field-access-type type key ctx))
                          (:type obj-out)
                          path)
        out-type (if default-out
                   (types/union-type [base-type (:type default-out)])
                   base-type)]
    (result out-type
            (concat (:errors obj-out)
                    (:errors default-out)))))

(defn infer-dot
  [[_ obj-expr key-or-path] ctx]
  (if (vector? key-or-path)
    (infer-get-path (list 'x:get-path obj-expr key-or-path nil) ctx)
    (infer-get-key (list 'x:get-key obj-expr key-or-path) ctx)))

(defn infer-body
  [body ctx]
  (loop [last-type types/+nil-type+
         errors []
         env (:env ctx)
         [form & more] body]
    (if (nil? form)
      (result last-type errors env)
      (let [out (infer-type form (assoc ctx :env env))]
        (recur (:type out)
               (concat errors (:errors out))
               (or (:env out) env)
               more)))))

(defn infer-type
  [form ctx]
  (cond
    (or (nil? form)
        (string? form)
        (number? form)
        (boolean? form)
        (keyword? form))
    (result (literal-type form))

    (map? form)
    (infer-map form ctx)

    (vector? form)
    (infer-vector form ctx)

    (symbol? form)
    (result (lookup-symbol-type form ctx))

    (seq? form)
    (let [lowered (lower/lower-form form ctx)]
      (if (not= lowered form)
        (infer-type lowered ctx)
        (let [op (first form)
              resolved-op (if (symbol? op)
                            (resolve-local-symbol op ctx)
                            op)
              builtin-op (if (symbol? resolved-op)
                           (ops/canonical-symbol resolved-op)
                           resolved-op)
              intrinsic-out (intrinsic/infer-intrinsic form ctx (intrinsic-callbacks))
              builtin-out (when (symbol? builtin-op)
                            (infer-builtin-form builtin-op form ctx))]
          (if intrinsic-out
            intrinsic-out
            (if builtin-out
              builtin-out
              (case op
                do (infer-body (rest form) ctx)
                return (infer-type (second form) ctx)
                let (infer-let form ctx)
                var (infer-binding-form form ctx)
                := (infer-binding-form form ctx)
                if (infer-if form ctx)
                when (infer-when form ctx)
                fn (infer-anon-fn form ctx)
                = (result types/+bool-type+
                          (mapcat :errors (map #(infer-type % ctx) (rest form))))
                == (result types/+bool-type+
                           (mapcat :errors (map #(infer-type % ctx) (rest form))))
                not= (result types/+bool-type+
                             (mapcat :errors (map #(infer-type % ctx) (rest form))))
                and (result types/+bool-type+
                            (mapcat :errors (map #(infer-type % ctx) (rest form))))
                or (infer-or form ctx)
                not (result types/+bool-type+
                            (merge-errors (infer-type (second form) ctx)))
                xt.lang.base-lib/not-empty? (result types/+bool-type+
                                                    (merge-errors (infer-type (second form) ctx)))
                xt.lang.base-lib/is-empty? (result types/+bool-type+
                                                   (merge-errors (infer-type (second form) ctx)))
                xt.lang.base-lib/arrayify (let [arg-out (infer-type (second form) ctx)
                                                arg-type (arrayify-type (:type arg-out) ctx)]
                                            (result arg-type
                                                    (:errors arg-out)))
                xt.lang.event-common/make-container (infer-make-container form ctx)
                (infer-function-call form ctx)))))))

    :else
    (result types/+unknown-type+
            [{:tag :unsupported-form
              :form form}])))
