(ns ^{:config {:role :internal}}
  postgres.typed.typed-view-internal
  (:require [postgres.gen.bind-macro :as bind]
            [postgres.typed.typed-common :as types]
            [std.string.case :as string-case]))

(defn dataview-entry
  "Builds a dataview select or return entry from a typed table."
  {:added "4.1"}
  [kind table opts]
  (letfn [(fail [message data]
            (throw (ex-info message data)))
          (normalize-name [value]
            (when-not (or (keyword? value)
                          (symbol? value)
                          (string? value))
              (fail "Dataview column and argument names must be keywords, symbols, or strings"
                    {:value value}))
            (string-case/snake-case (name value)))
          (normalize-type [value]
            (let [value (if (vector? value) (first value) value)
                  value (if (types/type-ref? value)
                          (case (:kind value)
                            :primitive (:name value)
                            :enum :enum
                            :ref :ref
                            (:kind value))
                          value)]
              (cond
                (keyword? value) value
                (symbol? value) (keyword (name value))
                (string? value) (keyword value)
                :else nil)))
          (column-type [column]
            (normalize-type (:type column)))
          (compatible-type? [actual expected]
            (or (= actual expected)
                (and (= expected :enum)
                     (contains? #{:enum :text :citext} actual))
                (and (= expected :citext) (= actual :text))
                (and (= expected :text) (= actual :citext))
                (and (= expected :ref)
                     (contains? #{:ref :uuid :text :citext} actual))))
          (parse-args [args]
            (when-not (vector? args)
              (fail "Dataview :args must be a vector of type/symbol pairs"
                    {:args args}))
            (when (odd? (count args))
              (fail "Dataview :args must contain type/symbol pairs" {:args args}))
            (let [parsed (mapv (fn [[type sym]]
                                 (let [type (normalize-type type)]
                                   (when-not (and type
                                                  (symbol? sym)
                                                  (nil? (namespace sym)))
                                     (fail "Dataview arguments require a supported type and an unqualified symbol"
                                           {:type type :symbol sym :args args}))
                                   {:source sym
                                    :symbol (normalize-name sym)
                                    :type (name type)
                                    :type-key type}))
                               (partition 2 args))
                  duplicate-symbols (->> parsed
                                         (group-by :symbol)
                                         (keep (fn [[symbol matches]]
                                                 (when (< 1 (count matches)) symbol)))
                                         seq)]
              (when duplicate-symbols
                (fail "Dataview argument names must be unique after normalization"
                      {:symbols (vec duplicate-symbols)}))
              parsed))
          (query-symbols [value]
            (cond
              (and (symbol? value) (nil? (namespace value))) [value]
              (map? value) (mapcat query-symbols (vals value))
              (coll? value) (mapcat query-symbols value)
              :else []))
          (normalize-select-query [query columns]
            (when-not (map? query)
              (fail "Dataview :select must be a column query map" {:select query}))
            (reduce-kv
             (fn [out field value]
               (let [field (normalize-name field)]
                 (when-not (contains? columns field)
                   (fail "Dataview :select contains an unknown table column"
                         {:table (:name table) :column field}))
                 (when (contains? out field)
                   (fail "Dataview :select has duplicate columns after normalization"
                         {:table (:name table) :column field}))
                 (assoc out field value)))
             {}
             query))
          (return-identity [opts columns args]
            (let [source (if (contains? opts :identity)
                           (:identity opts)
                           (:primary-key table))
                  source (if (sequential? source) source [source])]
              (when-not (and (vector? source) (seq source) (every? some? source))
                (fail "Dataview return identity must be a non-empty vector"
                      {:table (:name table) :identity source}))
              (let [identity (mapv normalize-name source)
                    descriptors (mapv (fn [column]
                                        (or (get columns column)
                                            (fail "Dataview return identity contains an unknown column"
                                                  {:table (:name table)
                                                   :column column
                                                   :identity identity})))
                                      identity)]
                (when (< (count args) (count identity))
                  (fail "Dataview return arguments must begin with its identity values"
                        {:table (:name table)
                         :identity identity
                         :expected (count identity)
                         :actual (count args)}))
                (doseq [[column arg] (map vector descriptors args)]
                  (when-not (compatible-type? (column-type column) (:type-key arg))
                    (fail "Dataview return identity argument type does not match its column"
                          {:table (:name table)
                           :column (normalize-name (:name column))
                           :expected (column-type column)
                           :actual (:type-key arg)
                           :argument (:source arg)})))
                identity)))]
    (when-not (contains? #{:select :return} kind)
      (fail "Dataview kind must be :select or :return" {:kind kind}))
    (when-not (types/table-def? table)
      (fail "Dataview entries require a typed TableDef" {:table table}))
    (when-not (map? opts)
      (fail "Dataview options must be a map" {:options opts}))
    (let [columns (into {}
                        (map (fn [column]
                               [(normalize-name (:name column)) column]))
                        (:columns table))
          args (parse-args (or (:args opts) []))
          arg-symbols (set (map :source args))
          query-key (if (= :select kind) :select :return)]
      (when-not (contains? opts query-key)
        (fail (str "Dataview options require :" (name query-key))
              {:options opts :required query-key}))
      (let [query (get opts query-key)
            query (if (= :select kind)
                    (normalize-select-query query columns)
                    query)
            query-symbols (set (query-symbols (if (= :select kind)
                                                (vals query) query)))
            unknown-symbols (seq (remove arg-symbols query-symbols))]
        (when unknown-symbols
          (fail "Dataview query uses undeclared argument symbols"
                {:symbols (vec unknown-symbols)
                 :declared (vec (sort-by str arg-symbols))}))
        (let [identity (when (= :return kind)
                         (return-identity opts columns args))
              view (cond-> {:table (:name table)
                            :type (name kind)
                            :query (bind/transform-query
                                    query arg-symbols)}
                     identity (assoc :identity identity))]
          {:input (mapv #(select-keys % [:symbol :type]) args)
           :view view})))))
