(ns rt.postgres.grammar.form-defpartition
  (:require [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.common-tracker :as tracker]
            [rt.postgres.grammar.common :as common]
            [std.lang.base.grammar-spec :as grammar-spec]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lib.schema :as schema]
            [std.lib :as h]))

(defn pg-defpartition-format
  "formats an input form"
  {:added "4.0"}
  [form]
  (let [[mdefn [op sym spec params]] (grammar-spec/format-defn form)
        {:keys [track public] :as msym} (meta sym)
        fmeta {:static/tracker (if track (tracker/map->Tracker @(resolve (first track))))
               :static/public public
               :static/dbtype :table}]
    [fmeta
     (list op (with-meta sym (merge msym fmeta))
           spec
           params)]))

(defn pg-defpartition
  "creates a partition statement"
  {:added "4.0"}
  ([[_ sym spec params]]
   (let [mopts (preprocess/macro-opts)
         {:static/keys [schema]
          :keys [existing]} (meta sym)
         ttok     (common/pg-full-token sym schema)

         ;; New syntax: (defpartition.pg Sym [Parent] {:for ... :partition-by ...})
         parent-sym (first spec)
         _ (when-not parent-sym (h/error "Partition must have a parent table in spec vector"))

         ;; Handle -/ namespace for parent
         parent-schema (let [ns (namespace parent-sym)]
                         (if (= ns "-")
                           schema ;; Use current schema if -/
                           ns))   ;; Otherwise use explicit namespace or nil

         parent-tok (common/pg-full-token (symbol (name parent-sym)) parent-schema)

         for-values (:for params)
         for-clause (if for-values
                      (if (vector? for-values)
                         (list :for :values :in (list 'quote for-values)) ;; List partition
                         (list :for :values
                               :from (list 'quote (list (first for-values)))
                               :to   (list 'quote (list (second for-values))))) ;; Range partition
                      (h/error "Partition must have :for values"))

         partition-by-clause (if-let [pb (:partition-by params)]
                               (list :partition-by (first pb) (list 'quote (second pb))))]

     (if (not existing)
       `(do [:create-table :if-not-exists ~ttok
             :partition-of ~parent-tok
             ~@for-clause
             ~@partition-by-clause])
       ""))))
