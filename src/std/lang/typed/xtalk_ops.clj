(ns std.lang.typed.xtalk-ops
  (:require [clojure.string :as str]
            [std.lang.typed.xtalk-common :as types]
            [std.lang.base.grammar-spec]
            [std.lang.base.grammar-xtalk]))

(def +op-source-namespaces+
  '[std.lang.base.grammar-spec
    std.lang.base.grammar-xtalk])

(def +legacy-op-aliases+
  '{xt.lang.common-lib/len x:len
    xt.lang.common-lib/arr-clone x:arr-clone
    xt.lang.common-lib/first x:first
    xt.lang.common-lib/second x:second
    xt.lang.common-lib/get-key x:get-key
    xt.lang.common-lib/obj-keys x:obj-keys
    xt.lang.common-lib/json-encode x:json-encode
    xt.lang.common-lib/json-decode x:json-decode
    xt.lang.common-lib/cat x:cat
    xt.lang.common-lib/fn? x:is-function?
    xt.lang.common-lib/arr? x:is-array?
    xt.lang.common-lib/obj? x:is-object?})

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
  (or (some (fn [sym]
              (when (and (symbol? sym)
                         (str/starts-with? (name sym) "x:"))
                sym))
            (sort-by str (:symbol entry)))
      (first (sort-by str (:symbol entry)))))

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
      (get-in @+op-index+ [:by-raw sym])
      (when-let [alias-sym (get +legacy-op-aliases+ sym)]
        (get-in @+op-index+ [:by-symbol alias-sym]))))

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
  ([entry]
   (op-types entry {:ns nil :aliases {}}))
  ([entry ctx]
   (mapv #(types/normalize-type % ctx)
         (op-type-forms entry))))

(defn builtin-type
  ([sym]
   (builtin-type sym {:ns nil :aliases {}}))
  ([sym ctx]
  (when-let [entry (canonical-entry sym)]
    (let [fn-types (op-types entry ctx)]
      (when (seq fn-types)
        (types/union-type fn-types))))))

(defn builtin?
  [sym]
  (boolean (builtin-entry sym)))
