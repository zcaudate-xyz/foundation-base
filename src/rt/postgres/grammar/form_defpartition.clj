(ns rt.postgres.grammar.form-defpartition
  (:require [rt.postgres.grammar.common :as common]
            [std.lang.base.grammar-spec :as grammar-spec]
            [std.string :as str]
            [std.lib :as h]))

(defn pg-partition-name
  "constructs partition name"
  {:added "4.0"}
  [base val stack]
  (let [parts (cons base (cons val stack))]
    (clojure.string/join "_" (map h/strn parts))))

(defn pg-partition-quote-id
  "quotes an identifier if needed"
  [s]
  (str "\"" s "\""))

(defn pg-partition-full-name
  [schema table]
  (if schema
    (str schema "." (pg-partition-quote-id table))
    (pg-partition-quote-id table)))

(defn pg-partition-def
  "recursive definition for partition"
  {:added "4.0"}
  [parent-sym base-name current-spec remaining-specs stack]
  (let [{:keys [use in default]} current-spec
        next-spec (first remaining-specs)
        next-col  (when-let [u (:use next-spec)] (clojure.string/replace (name u) "-" "_"))
        parent-ns (namespace parent-sym)
        [schema-prefix table-name] (if parent-ns
                                     [nil base-name]
                                     (let [parts (clojure.string/split (name parent-sym) #"\.")]
                                       (if (> (count parts) 1)
                                         [(first parts) (last parts)]
                                         [nil base-name])))
        
        full-parent (if parent-ns
                      (str parent-ns "." (pg-partition-quote-id table-name))
                      (pg-partition-full-name schema-prefix table-name))]
    
    (if default
      (let [new-name (str table-name "_default")
            full-new (if parent-ns
                       (str parent-ns "." (pg-partition-quote-id new-name))
                       (pg-partition-full-name schema-prefix new-name))]
        (list
         (vec (concat [:create-table :if-not-exists (list 'raw full-new)
                       :partition-of (list 'raw full-parent) :default]
                      (if next-col
                        [:partition-by :list (list 'quote (list (symbol next-col)))]
                        [])
                      #_[\;]))))
      
      (mapcat (fn [val]
                (let [new-name (pg-partition-name table-name val stack)
                      new-sym  (cond parent-ns (symbol parent-ns new-name)
                                     schema-prefix (symbol (str schema-prefix "." new-name))
                                     :else (symbol new-name))
                      full-new (if parent-ns
                                 (str parent-ns "." (pg-partition-quote-id new-name))
                                 (pg-partition-full-name schema-prefix new-name))]
                  
                  (concat
                   [(vec (concat [:create-table :if-not-exists (list 'raw full-new)
                                  :partition-of (list 'raw full-parent)
                                  :for :values :in (list 'quote (list val))]
                                 (if next-col
                                   [:partition-by :list (list 'quote (list (symbol next-col)))]
                                   [])
                                 #_[\;]))]
                   (if (seq remaining-specs)
                     (pg-partition-def new-sym table-name next-spec (rest remaining-specs) (cons val stack))
                     nil))))
              in))))

(defn pg-defpartition
  "defpartition block"
  {:added "4.0"}
  [[_ sym doc? attr? [parent] specs :as form]]
  (let [[{:keys [doc] :as mdefn}
         [_ sym [parent] specs]] (grammar-spec/format-defn form)
        base-name (name parent)
        statements (pg-partition-def parent base-name (first specs) (rest specs) [])]
     (apply list 'do statements)))
