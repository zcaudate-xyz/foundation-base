(ns std.lang.typed.xtalk-common
  (:require [clojure.string :as str]))

(def +primitive-types+
  #{:xt/any
    :xt/unknown
    :xt/nil
    :xt/bool
    :xt/int
    :xt/num
    :xt/str
    :xt/kw
    :xt/fn
    :xt/obj})

(def +unknown-type+ {:kind :primitive :name :xt/unknown})
(def +nil-type+     {:kind :primitive :name :xt/nil})
(def +bool-type+    {:kind :primitive :name :xt/bool})
(def +int-type+     {:kind :primitive :name :xt/int})
(def +num-type+     {:kind :primitive :name :xt/num})
(def +str-type+     {:kind :primitive :name :xt/str})
(def +kw-type+      {:kind :primitive :name :xt/kw})

(defrecord XtSpecDef [ns name type spec-meta])
(defrecord XtArg [name type modifiers])
(defrecord XtFnDef [ns name inputs output body-meta raw-body spec])
(defrecord XtValueDef [ns name type body-meta raw-value spec])
(defrecord XtRegistryEntry [symbol spec fn macro value])

(defonce ^:dynamic *type-registry* (atom {}))

(declare normalize-type)

(defn primitive-type
  [kw]
  {:kind :primitive :name kw})

(defn snake-case-string
  [s]
  (str/replace s "-" "_"))

(defn maybe-type
  [type]
  {:kind :maybe :item type})

(defn union-type
  [types]
  (let [flat (->> types
                  (mapcat (fn [type]
                            (if (= :union (:kind type))
                              (:types type)
                              [type])))
                  (remove nil?)
                  (reduce (fn [out type]
                            (if (some #(= % type) out)
                              out
                              (conj out type)))
                          []))]
    (cond
      (empty? flat) +unknown-type+
      (= 1 (count flat)) (first flat)
      :else {:kind :union :types flat})))

(defn type-key
  [ns-sym type-name]
  (if ns-sym
    (symbol (str ns-sym) (name type-name))
    (symbol (name type-name))))

(defn valid-key?
  [key]
  (boolean
   (and (symbol? key)
        (namespace key)
        (seq (namespace key))
        (seq (name key)))))

(defn clear-registry!
  []
  (reset! *type-registry* {}))

(defn make-registry-entry
  [sym]
  (->XtRegistryEntry sym nil nil nil nil))

(defn get-entry
  [sym]
  (get @*type-registry* sym))

(defn entry-declarations
  [entry]
  (cond-> {}
    (:spec entry) (assoc :spec (:spec entry))
    (:fn entry) (assoc :fn (:fn entry))
    (:macro entry) (assoc :macro (:macro entry))
    (:value entry) (assoc :value (:value entry))))

(defn entry-kinds
  [entry]
  (keys (entry-declarations entry)))

(defn entry-primary
  [entry]
  (or (:spec entry)
      (:fn entry)
      (:value entry)))

(defn entry-primary-kind
  [entry]
  (cond
    (:spec entry) :spec
    (:fn entry) :fn
    (:value entry) :value
    :else nil))

(defn get-declaration
  [sym kind]
  (get (entry-declarations (get-entry sym)) kind))

(defn get-spec
  [sym]
  (get-declaration sym :spec))

(defn get-function
  [sym]
  (get-declaration sym :fn))

(defn get-macro
  [sym]
  (get-declaration sym :macro))

(defn get-value
  [sym]
  (get-declaration sym :value))

(defn get-type
  [sym]
  (some-> sym get-entry entry-primary))

(defn list-specs
  []
  (->> @*type-registry*
        vals
        (mapcat #(vals (select-keys (entry-declarations %) [:spec])))))

(defn list-functions
  []
  (->> @*type-registry*
        vals
        (mapcat #(vals (select-keys (entry-declarations %) [:fn])))))

(defn list-macros
  []
  (->> @*type-registry*
        vals
        (mapcat #(vals (select-keys (entry-declarations %) [:macro])))))

(defn list-values
  []
  (->> @*type-registry*
        vals
        (mapcat #(vals (select-keys (entry-declarations %) [:value])))))

(defn list-entries
  []
  (vals @*type-registry*))

(defn register-entry!
  [sym key value]
  (when-not (valid-key? sym)
    (throw (ex-info "Invalid registry key. Must be a namespaced symbol."
                    {:key sym})))
  (swap! *type-registry*
         (fn [registry]
           (let [entry (or (get registry sym)
                           (make-registry-entry sym))]
             (assoc registry sym (assoc entry key value)))))
  value)

(defn register-spec!
  [sym spec]
  (register-entry! sym :spec spec))

(defn register-function!
  [sym fn-def]
  (register-entry! sym :fn fn-def))

(defn register-macro!
  [sym macro-def]
  (register-entry! sym :macro macro-def))

(defn register-value!
  [sym value-def]
  (register-entry! sym :value value-def))

(defn make-spec-def
  [ns-sym type-name type spec-meta]
  (->XtSpecDef (some-> ns-sym str) (name type-name) type spec-meta))

(defn make-arg
  [name type modifiers]
  (->XtArg name type (vec modifiers)))

(defn make-fn-def
  [ns-sym fn-name inputs output body-meta raw-body spec]
  (->XtFnDef (some-> ns-sym str) (name fn-name) (vec inputs) output body-meta (vec raw-body) spec))

(defn make-value-def
  [ns-sym value-name type body-meta raw-value spec]
  (->XtValueDef (some-> ns-sym str) (name value-name) type body-meta raw-value spec))

(defn spec-def?
  [x]
  (instance? XtSpecDef x))

(defn fn-def?
  [x]
  (and (instance? XtFnDef x)
       (not (get-in x [:body-meta :macro]))))

(defn macro-def?
  [x]
  (and (instance? XtFnDef x)
       (true? (get-in x [:body-meta :macro]))))

(defn value-def?
  [x]
  (instance? XtValueDef x))

(defn declaration-kind
  [x]
  (cond
    (spec-def? x) :spec
    (fn-def? x) :fn
    (macro-def? x) :macro
    (value-def? x) :value
    (instance? XtRegistryEntry x) (entry-primary-kind x)
    :else nil))

(defn field-key
  [field]
  (cond
    (keyword? field) (if-let [field-ns (namespace field)]
                       (str (snake-case-string field-ns)
                            "/"
                            (snake-case-string (name field)))
                       (snake-case-string (name field)))
    (symbol? field) (if-let [field-ns (namespace field)]
                      (str (snake-case-string field-ns)
                           "/"
                           (snake-case-string (name field)))
                      (snake-case-string (name field)))
    (string? field) (snake-case-string field)
    :else (throw (ex-info "Invalid record field key"
                          {:field field}))))

(defn likely-type-symbol?
  [sym]
  (let [name-str (name sym)]
    (or (namespace sym)
        (re-find #"^[A-Z]" name-str))))

(defn resolve-type-symbol
  [sym {:keys [ns aliases]}]
  (cond
    (not (symbol? sym))
    sym

    (namespace sym)
    (if-let [alias-ns (get aliases (symbol (namespace sym)))]
      (symbol (str alias-ns) (name sym))
      sym)

    ns
    (symbol (str ns) (name sym))

    :else
    sym))

(defn normalize-record-field
  [[field-name field-type] ctx]
  (let [type (normalize-type field-type ctx)]
    {:name (field-key field-name)
     :type type
     :optional? (= :maybe (:kind type))}))

(defn normalize-apply-target
  [target ctx]
  (cond
    (symbol? target) (resolve-type-symbol target ctx)
    :else target))

(defn normalize-type
  [form ctx]
  (cond
    (nil? form)
    +nil-type+

    (keyword? form)
    (if (contains? +primitive-types+ form)
      (primitive-type form)
      {:kind :keyword :name form})

    (symbol? form)
    {:kind :named :name (resolve-type-symbol form ctx)}

    (string? form)
    {:kind :named :name form}

    (vector? form)
    (let [[op & args] form]
      (case op
        :maybe {:kind :maybe
                :item (normalize-type (first args) ctx)}
        :xt/maybe {:kind :maybe
                   :item (normalize-type (first args) ctx)}
        :or {:kind :union
             :types (mapv #(normalize-type % ctx) args)}
        :and {:kind :intersection
              :types (mapv #(normalize-type % ctx) args)}
        :tuple {:kind :tuple
                :types (mapv #(normalize-type % ctx) args)}
        :array {:kind :array
                :item (normalize-type (first args) ctx)}
        :xt/array {:kind :array
                   :item (normalize-type (first args) ctx)}
        :dict {:kind :dict
               :key (normalize-type (first args) ctx)
               :value (normalize-type (second args) ctx)}
        :xt/dict {:kind :dict
                  :key (normalize-type (first args) ctx)
                  :value (normalize-type (second args) ctx)}
        :record {:kind :record
                  :fields (mapv #(normalize-record-field % ctx) args)}
        :xt/record {:kind :record
                    :fields (mapv #(normalize-record-field % ctx) args)}
        :fn {:kind :fn
             :inputs (mapv #(normalize-type % ctx) (first args))
             :output (normalize-type (second args) ctx)}
        :> {:kind :apply
            :target (normalize-apply-target (first args) ctx)
            :args (mapv #(normalize-type % ctx) (rest args))}
        {:kind :tuple
         :types (mapv #(normalize-type % ctx) form)}))

    :else
    (throw (ex-info "Unsupported type form"
                    {:form form :context ctx}))))

(defn normalize-return-meta
  [ret ctx]
  (let [body (if (and (vector? ret) (= 1 (count ret)))
               (first ret)
               ret)]
    (normalize-type body ctx)))

(defn fn-type
  [fn-def]
  {:kind :fn
   :inputs (mapv :type (:inputs fn-def))
   :output (:output fn-def)})

(defn type->data
  [type]
  (cond
    (instance? XtSpecDef type)
    {:ns (:ns type)
     :name (:name type)
     :type (type->data (:type type))}

    (instance? XtArg type)
    {:name (:name type)
     :type (type->data (:type type))}

    (instance? XtFnDef type)
    {:ns (:ns type)
     :name (:name type)
     :inputs (mapv type->data (:inputs type))
     :output (type->data (:output type))}

    (map? type)
     (cond-> {:kind (:kind type)}
       (:name type) (assoc :name (:name type))
       (:item type) (assoc :item (type->data (:item type)))
       (:types type) (assoc :types (mapv type->data (:types type)))
       (:inputs type) (assoc :inputs (mapv type->data (:inputs type)))
       (:output type) (assoc :output (type->data (:output type)))
       (:key type) (assoc :key (type->data (:key type)))
       (:value type) (assoc :value (type->data (:value type)))
       (:open type) (assoc :open {:key (type->data (get-in type [:open :key]))
                                  :value (type->data (get-in type [:open :value]))})
       (:target type) (assoc :target (:target type))
       (:args type) (assoc :args (mapv type->data (:args type)))
       (:fields type) (assoc :fields (mapv (fn [{:keys [name type optional?]}]
                                            {:name name
                                            :type (type->data type)
                                            :optional? optional?})
                                         (:fields type))))

    (vector? type)
    (mapv type->data type)

    :else
    type))

(defn type-string
  [type]
  (pr-str (type->data type)))

(defn current-function-symbol
  [fn-def]
  (type-key (some-> fn-def :ns symbol) (:name fn-def)))
