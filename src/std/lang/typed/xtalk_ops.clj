(ns std.lang.typed.xtalk-ops
  (:require [clojure.string :as str]
            [std.lang.base.grammar-spec]
            [std.lang.base.grammar-xtalk]))

(def +op-source-namespaces+
  '[std.lang.base.grammar-spec
    std.lang.base.grammar-xtalk])

(defn op-table-vars
  [ns-sym]
  (->> (ns-publics ns-sym)
       (keep (fn [[sym v]]
               (when (str/starts-with? (name sym) "+op-")
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

(defn builtin?
  [sym]
  (boolean (builtin-entry sym)))
