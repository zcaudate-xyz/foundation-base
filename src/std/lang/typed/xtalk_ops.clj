(ns std.lang.typed.xtalk-ops
  (:require [clojure.string :as str]
            [std.lang.typed.xtalk-common :as types]
            [std.lang.base.grammar-spec]
            [std.lang.base.grammar-xtalk]))

(def +op-source-namespaces+
  '[std.lang.base.grammar-spec
    std.lang.base.grammar-xtalk])

(defn op-table-vars
  [ns-sym]
  (->> (ns-publics ns-sym)
        (keep (fn [[sym v]]
                (when (or (str/starts-with? (name sym) "+op-")
                          (str/starts-with? (name sym) "+xt-"))
                  v)))))

(defn op-entries
  []
  (->> +op-source-namespaces+
       (mapcat op-table-vars)
       (mapcat (fn [v]
                 (let [value @v]
                   (if (sequential? value)
                     value
                     []))))
       vec))

(defn canonical-symbol-from-entry
  [entry]
  (if (and (= :alias (:emit entry))
           (symbol? (:raw entry)))
    (:raw entry)
    (or (some (fn [sym]
                (when (and (symbol? sym)
                           (str/starts-with? (name sym) "x:"))
                  sym))
              (sort-by str (:symbol entry)))
        (first (sort-by str (:symbol entry))))))

(defonce +op-index+
  (delay
    (let [entries (op-entries)
          by-symbol (reduce (fn [acc entry]
                              (reduce (fn [inner sym]
                                        (assoc inner sym entry))
                                      acc
                                      (:symbol entry)))
                            {}
                            entries)
          by-raw (reduce (fn [acc entry]
                           (if (symbol? (:raw entry))
                             (assoc acc (:raw entry) entry)
                             acc))
                         {}
                         entries)]
      {:entries entries
       :by-symbol by-symbol
       :by-raw by-raw})))

(defn builtin-entry
  [sym]
  (or (get-in @+op-index+ [:by-symbol sym])
      (get-in @+op-index+ [:by-raw sym])))

(defn canonical-entry
  [sym]
  (when-let [entry (builtin-entry sym)]
    (assoc entry :canonical-symbol (canonical-symbol-from-entry entry))))

(defn canonical-symbol
  [sym]
  (if-let [entry (canonical-entry sym)]
    (:canonical-symbol entry)
    sym))

(defn op-arglists
  [entry]
  (get-in entry [:op-spec :arglists]))

(defn op-type-forms
  [entry]
  (let [op-spec (:op-spec entry)]
    (cond
      (nil? op-spec) []
      (:types op-spec) (vec (:types op-spec))
      (:type op-spec) [(:type op-spec)]
      :else [])))

(defn op-types
  [entry]
  (mapv #(types/normalize-type % {:ns nil :aliases {}})
        (op-type-forms entry)))

(defn builtin-type
  [sym]
  (when-let [entry (canonical-entry sym)]
    (let [fn-types (op-types entry)]
      (when (seq fn-types)
        (types/union-type fn-types)))))

(defn builtin?
  [sym]
  (boolean (builtin-entry sym)))
