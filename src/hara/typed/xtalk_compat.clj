(ns hara.typed.xtalk-compat
  (:require [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-ops :as ops]))

(defn result
  ([type] (result type []))
  ([type errors]
   {:type type
    :errors (vec errors)})
  ([type errors env]
   {:type type
    :errors (vec errors)
    :env env}))

(defn with-loc
  [result loc]
  (if (and loc (seq (:errors result)))
    (update result :errors
            (fn [errors]
              (mapv #(assoc % :loc (or (:loc %) loc)) errors)))
    result))

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
     (let [name (:name type)
           spec (or (types/get-spec name)
                    (when (namespace name)
                      (try
                        (when-let [register! (requiring-resolve
                                              'hara.typed.xtalk-analysis/analyze-and-register!)]
                          (register! (symbol (namespace name))))
                        (types/get-spec name)
                        (catch Throwable _
                          nil))))]
       (if spec
         (recur (:type spec) ctx (conj seen name))
         type))
     type)))

(defn type-eq?
  [left right]
  (= left right))

(defn any-type?
  [type]
  (and (= :primitive (:kind type))
       (= :xt/any (:name type))))

(declare compatible-type?)

(defn fn-input-compatible?
  [actual expected ctx]
  (or (= actual types/+unknown-type+)
      (= expected types/+unknown-type+)
      (any-type? actual)
      (any-type? expected)
      (compatible-type? actual expected ctx)
      (compatible-type? expected actual ctx)))

(defn compatible-type?
  [actual expected ctx]
  (let [actual (resolve-type actual ctx)
        expected (resolve-type expected ctx)]
    (cond
      (any-type? expected)
      true

      (any-type? actual)
      true

      (= expected types/+unknown-type+)
      true

      (= actual types/+unknown-type+)
      true

      (type-eq? actual expected)
      true

      (and (= :primitive (:kind actual))
           (= :primitive (:kind expected))
           (= :xt/int (:name actual))
           (= :xt/num (:name expected)))
      true

      (= :maybe (:kind expected))
      (or (compatible-type? actual types/+nil-type+ ctx)
          (and (= :maybe (:kind actual))
               (compatible-type? (:item actual) (:item expected) ctx))
          (compatible-type? actual (:item expected) ctx)
          (and (= :union (:kind actual))
               (every? #(compatible-type? % expected ctx) (:types actual))))

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

      (and (= :array (:kind actual))
           (= :array (:kind expected)))
      (compatible-type? (:item actual) (:item expected) ctx)

      (and (= :tuple (:kind actual))
           (= :tuple (:kind expected)))
      (and (= (count (:types actual))
              (count (:types expected)))
           (every? true?
                   (map #(compatible-type? %1 %2 ctx)
                        (:types actual)
                        (:types expected))))

      (and (= :tuple (:kind actual))
           (= :array (:kind expected)))
      (every? #(compatible-type? % (:item expected) ctx)
              (:types actual))

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
      (and (<= (count (:inputs actual))
               (count (:inputs expected)))
           (every? true?
                    (map #(fn-input-compatible? %1 %2 ctx)
                         (:inputs actual)
                         (take (count (:inputs actual))
                               (:inputs expected))))
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

(defn object-value-type
  [type ctx]
  (let [type (resolve-type type ctx)]
    (cond
      (= :maybe (:kind type))
      (types/maybe-type (object-value-type (:item type) ctx))

      (= :record (:kind type))
      (let [value-types (concat (map :type (:fields type))
                                (when-let [open (:open type)]
                                  [(:value open)]))]
        (if (seq value-types)
          (types/union-type value-types)
          types/+unknown-type+))

      (= :dict (:kind type))
      (:value type)

      :else
      types/+unknown-type+)))
