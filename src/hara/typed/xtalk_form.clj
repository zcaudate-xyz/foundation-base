(ns hara.typed.xtalk-form
  (:require [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-compat :as compat]
            [hara.typed.xtalk-env :as env]))

(declare infer-body)

(defn infer-map
  [form ctx]
  (let [entries (mapv (fn [[k v]]
                        (let [value-out ((:infer ctx) v ctx)]
                          (if (or (keyword? k)
                                  (string? k))
                            {:field-name (types/field-key k)
                             :value-out value-out}
                            {:dynamic-key-out ((:infer ctx) k ctx)
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
    (compat/result (cond-> {:kind :record
                            :fields static-fields}
                     open (assoc :open open))
                   errors)))

(defn infer-vector
  [form ctx]
  (let [item-results (mapv #((:infer ctx) % ctx) form)]
    (compat/result (if (empty? item-results)
                     {:kind :tuple
                      :types []}
                     {:kind :tuple
                      :types (mapv :type item-results)})
                   (mapcat :errors item-results))))

(defn infer-binding-form
  [[_ target expr] ctx]
  (let [expr-out ((:infer ctx) expr ctx)
        updates (env/binding-updates target (:type expr-out) ctx)
        env (or (:env ctx) {})
        next-env (if updates
                   (merge env updates)
                   env)
        errors (cond-> (vec (:errors expr-out))
                 (and (nil? updates)
                      (not (env/dynamic-assignment-target? target)))
                  (conj {:tag :unsupported-binding-target
                         :target target
                         :form expr}))]
    (compat/result (:type expr-out) errors next-env)))

(defn infer-let
  [[_ bindings & body] ctx]
  (loop [env (:env ctx)
         errors []
         [target expr & more] bindings]
    (if (nil? target)
      (let [out (infer-body body (assoc ctx :env env))]
        (compat/result (:type out)
                       (concat errors (:errors out))))
      (let [{:keys [symbol type] :as binding} (env/binding-decl target ctx)
            inferred ((:infer ctx) expr (assoc ctx :env env))
            updates (if binding
                      {symbol (or type (:type inferred))}
                      (env/binding-updates target (:type inferred) ctx))
            binding-errors (cond-> (:errors inferred)
                             (nil? updates)
                             (conj {:tag :unsupported-binding-target
                                    :target target
                                    :form expr})
                             (and type
                                  (not (compat/compatible-type? (:type inferred) type ctx)))
                             (conj {:tag :binding-type-mismatch
                                    :form expr
                                    :binding symbol
                                    :expected (types/type->data type)
                                    :actual (types/type->data (:type inferred))}))
            next-env (if updates
                       (merge env updates)
                       env)]
        (recur next-env
               (concat errors binding-errors)
               more)))))

(defn infer-if
  [[_ cond-expr then-expr else-expr] ctx]
  (let [cond-out ((:infer ctx) cond-expr ctx)
        then-out ((:infer ctx) then-expr ctx)
        else-out ((:infer ctx) else-expr ctx)]
    (compat/result (types/union-type [(:type then-out) (:type else-out)])
                   (compat/merge-errors cond-out then-out else-out))))

(defn infer-ternary
  [[_ cond-expr then-expr else-expr] ctx]
  (infer-if (list 'if cond-expr then-expr else-expr) ctx))

(defn infer-or
  [[_ & args] ctx]
  (let [arg-results (mapv #((:infer ctx) % ctx) args)
        non-nil-type (fn strip-nil-type [type]
                       (let [type (compat/resolve-type type ctx)]
                         (cond
                           (= type types/+nil-type+)
                           nil

                           (= :maybe (:kind type))
                           (strip-nil-type (:item type))

                           (= :union (:kind type))
                           (let [items (keep strip-nil-type (:types type))]
                             (when (seq items)
                               (types/union-type items)))

                           :else
                           type)))
        result-types (keep #(non-nil-type (:type %)) arg-results)]
    (compat/result (if (seq result-types)
                     (types/union-type result-types)
                     types/+nil-type+)
                   (mapcat :errors arg-results))))

(defn infer-when
  [[_ cond-expr & body] ctx]
  (let [cond-out ((:infer ctx) cond-expr ctx)
        body-out (infer-body body ctx)]
    (compat/result (types/union-type [types/+nil-type+ (:type body-out)])
                   (compat/merge-errors cond-out body-out))))

(defn infer-cond
  [[_ & clauses] ctx]
  (loop [result-types []
         errors []
         [test-expr result-expr & more] clauses]
    (if (nil? test-expr)
      (compat/result (if (seq result-types)
                       (types/union-type result-types)
                       types/+nil-type+)
                     errors)
      (let [test-out (when-not (= :else test-expr)
                       ((:infer ctx) test-expr ctx))
            result-out (if (some? result-expr)
                         ((:infer ctx) result-expr ctx)
                         (compat/result types/+nil-type+))]
        (recur (conj result-types (:type result-out))
               (concat errors
                       (when test-out (:errors test-out))
                       (:errors result-out))
               more)))))

(defn infer-while
  [[_ cond-expr & body] ctx]
  (let [cond-out ((:infer ctx) cond-expr ctx)
        body-out (infer-body body ctx)]
    (compat/result types/+nil-type+
                   (compat/merge-errors cond-out body-out))))

(defn infer-yield
  [[_ value-expr] ctx]
  (let [value-out ((:infer ctx) value-expr ctx)]
    (compat/result types/+nil-type+
                   (:errors value-out))))

(defn infer-anon-fn
  [[_ args & body] ctx]
  (let [input-types (mapv (fn [_] types/+unknown-type+) args)
        env (merge (:env ctx)
                   (zipmap args input-types))
        body-out (infer-body body (assoc ctx :env env))]
    (compat/result {:kind :fn
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
  (let [parts (mapv #((:infer ctx) % ctx) (cons target-expr source-exprs))
        part-types (mapv (comp #(compat/resolve-type % ctx) :type) parts)
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
    (compat/result (cond
                     (or (seq record-fields) open-type)
                     (cond-> {:kind :record
                              :fields (vec record-fields)}
                       open-type (assoc :open open-type))

                     :else
                     types/+unknown-type+)
                   (mapcat :errors parts))))

(defn arrayify-type
  [type ctx]
  (let [type (compat/resolve-type type ctx)]
    (cond
      (= :array (:kind type))
      type

      (= :union (:kind type))
      {:kind :array
       :item (types/union-type
              (map (fn [item-type]
                     (let [item-type (compat/resolve-type item-type ctx)]
                       (if (= :array (:kind item-type))
                         (:item item-type)
                         item-type)))
                   (:types type)))}

      :else
      {:kind :array
       :item type})))

(defn infer-make-container
  [[op initial-expr type-expr opts-expr] ctx]
  (let [listener-map-name 'xt.event.base-listener/EventListenerMap
        initial-out ((:infer ctx) initial-expr ctx)
        type-out ((:infer ctx) type-expr ctx)
        opts-out ((:infer ctx) opts-expr ctx)
        initial-type (compat/resolve-type (:type initial-out) ctx)
        opts-type (compat/resolve-type (:type opts-out) ctx)
        data-type (cond
                    (= :fn (:kind initial-type))
                    (:output initial-type)

                    (= :union (:kind initial-type))
                    (types/union-type
                     (map (fn [member-type]
                            (let [member-type (compat/resolve-type member-type ctx)]
                              (if (= :fn (:kind member-type))
                                (:output member-type)
                                member-type)))
                          (:types initial-type)))

                    :else
                    initial-type)
        base-fields [{:name "::" :type (:type type-out) :optional? false}
                     {:name "listeners"
                      :type {:kind :named
                             :name listener-map-name}
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
    (compat/result out-type
                   (compat/merge-errors initial-out type-out opts-out))))

(defn infer-blank-container
  [[op type-expr opts-expr] ctx]
  (let [listener-map-name 'xt.event.base-listener/EventListenerMap
        type-out ((:infer ctx) type-expr ctx)
        opts-out ((:infer ctx) opts-expr ctx)
        opts-type (compat/resolve-type (:type opts-out) ctx)
        base-fields [{:name "::" :type (:type type-out) :optional? false}
                     {:name "listeners"
                      :type {:kind :named
                             :name listener-map-name}
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
    (compat/result out-type
                   (compat/merge-errors type-out opts-out))))

(defn apply-default-type
  [base-type default-type]
  (types/union-type [(if (= :maybe (:kind base-type))
                       (:item base-type)
                       base-type)
                     default-type]))

(defn infer-fixed-output
  [out-type]
  (fn [form ctx]
    (let [arg-outs (mapv #((:infer ctx) % ctx) (rest form))]
      (compat/result out-type
                     (mapcat :errors arg-outs)))))

(defn infer-obj-vals
  [[_ obj-expr] ctx]
  (let [obj-out ((:infer ctx) obj-expr ctx)]
    (compat/result {:kind :array
                    :item (compat/object-value-type (:type obj-out) ctx)}
                   (:errors obj-out))))

(defn infer-obj-pairs
  [[_ obj-expr] ctx]
  (let [obj-out ((:infer ctx) obj-expr ctx)]
    (compat/result {:kind :array
                    :item {:kind :tuple
                           :types [types/+str-type+
                                   (compat/object-value-type (:type obj-out) ctx)]}}
                   (:errors obj-out))))

(defn infer-obj-clone
  [[_ obj-expr] ctx]
  (let [obj-out ((:infer ctx) obj-expr ctx)
        obj-type (compat/resolve-type (:type obj-out) ctx)
        out-type (case (:kind obj-type)
                   :record obj-type
                   :dict obj-type
                   (if (= :maybe (:kind obj-type))
                     (let [item-type (compat/resolve-type (:item obj-type) ctx)]
                       (if (contains? #{:record :dict} (:kind item-type))
                         item-type
                         {:kind :record :fields []}))
                     {:kind :record :fields []}))]
    (compat/result out-type
                   (:errors obj-out))))

(defn infer-arr-clone
  [[_ arr-expr] ctx]
  (let [arr-out ((:infer ctx) arr-expr ctx)
        arr-type (compat/resolve-type (:type arr-out) ctx)
        out-type (case (:kind arr-type)
                   :array arr-type
                   :tuple {:kind :array
                           :item (types/union-type (:types arr-type))}
                   types/+unknown-type+)]
    (compat/result out-type
                   (:errors arr-out))))

(defn infer-get-key
  [[_ obj-expr key-expr default-expr] ctx]
  (let [obj-out ((:infer ctx) obj-expr ctx)
        key-out ((:infer ctx) key-expr ctx)
        default-out (when (some? default-expr)
                      ((:infer ctx) default-expr ctx))
        key (compat/field-literal key-expr)
        obj-type (compat/resolve-type (:type obj-out) ctx)
        errors (compat/merge-errors obj-out key-out)
        errors (cond-> errors
                 (and (= :dict (:kind obj-type))
                      (not (compat/compatible-type? (:type key-out) (:key obj-type) ctx)))
                 (conj {:tag :key-type-mismatch
                        :form key-expr
                        :expected (types/type->data (:key obj-type))
                        :actual (types/type->data (:type key-out))}))
        base-type (if key
                    (compat/field-access-type obj-type key ctx)
                    (cond
                      (= :dict (:kind obj-type))
                      (types/maybe-type (:value obj-type))

                      (and (= :record (:kind obj-type))
                           (:open obj-type))
                      (types/maybe-type (get-in obj-type [:open :value]))

                      :else
                      types/+unknown-type+))
        out-type (if default-out
                   (apply-default-type base-type (:type default-out))
                   base-type)]
    (compat/result out-type
                   (concat errors (:errors default-out)))))

(defn infer-get-idx
  [[_ obj-expr idx-expr default-expr] ctx]
  (let [obj-out ((:infer ctx) obj-expr ctx)
        idx-out ((:infer ctx) idx-expr ctx)
        default-out (when (some? default-expr)
                      ((:infer ctx) default-expr ctx))
        obj-type (compat/resolve-type (:type obj-out) ctx)
        idx-literal (when (integer? idx-expr)
                      idx-expr)
        base-type (cond
                    (= :array (:kind obj-type))
                    (types/maybe-type (:item obj-type))

                    (= :tuple (:kind obj-type))
                    (if (some? idx-literal)
                      (or (nth (:types obj-type) idx-literal nil)
                          types/+unknown-type+)
                      (types/maybe-type (types/union-type (:types obj-type))))

                    :else
                    types/+unknown-type+)
        out-type (if default-out
                   (apply-default-type base-type (:type default-out))
                   base-type)]
    (compat/result out-type
                   (concat (compat/merge-errors obj-out idx-out)
                           (:errors default-out)))))

(defn infer-get-path
  [[_ obj-expr path-expr default-expr] ctx]
  (let [obj-out ((:infer ctx) obj-expr ctx)
        default-out (when (some? default-expr)
                      ((:infer ctx) default-expr ctx))
        path (if (vector? path-expr)
               (mapv compat/field-literal path-expr)
               [])
        base-type (reduce (fn [type key]
                            (compat/field-access-type type key ctx))
                          (:type obj-out)
                          path)
        out-type (if default-out
                   (apply-default-type base-type (:type default-out))
                   base-type)]
    (compat/result out-type
                   (concat (:errors obj-out)
                           (:errors default-out)))))

(defn infer-dot
  [[_ obj-expr key-or-path] ctx]
  (if (vector? key-or-path)
    (infer-get-path (list 'x:get-path obj-expr key-or-path nil) ctx)
    (infer-get-key (list 'x:get-key obj-expr key-or-path) ctx)))

(defn infer-free
  [[_ & args] ctx]
  (if-let [value (first args)]
    ((:infer ctx) value ctx)
    (compat/result types/+unknown-type+)))

(defn infer-keyword-call
  [[callee target default-expr] ctx]
  (if (some? default-expr)
    (infer-get-key (list 'x:get-key target callee default-expr) ctx)
    (infer-get-key (list 'x:get-key target callee) ctx)))

(defn infer-body
  [body ctx]
  (loop [last-type types/+nil-type+
         errors []
         env (:env ctx)
         [form & more] body]
    (if (nil? form)
      (compat/result last-type errors env)
      (let [out ((:infer ctx) form (assoc ctx :env env))]
        (recur (:type out)
               (concat errors (:errors out))
               (or (:env out) env)
               more)))))
