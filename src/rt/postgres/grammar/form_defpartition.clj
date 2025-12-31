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

         ;; Extract :of and :for from spec/params or spec
         ;; Usually specs are odd, so it might be [:of parent :for values]
         ;; But grammar-spec/format-defn splits it into spec and params.
         ;; Let's assume the user writes (defpartition.pg my-part [:of parent :for values] ...)

         ;; If spec is a vector, we process it.
         args (apply hash-map spec)
         parent (:of args)
         for-values (:for args)

         parent-tok (if parent
                      (common/pg-full-token parent (:static/schema (meta parent)))
                      (h/error "Partition must have an :of parent table"))

         for-clause (if for-values
                      (if (vector? for-values)
                         (list :for :values :in (list 'quote for-values)) ;; List partition
                         (list :for :values :from (list 'quote (first for-values)) :to (list 'quote (second for-values)))) ;; Range partition
                      (h/error "Partition must have :for values"))]

     (if (not existing)
       `(do [:create-table :if-not-exists ~ttok
             :partition-of ~parent-tok
             ~@for-clause])
       ""))))
