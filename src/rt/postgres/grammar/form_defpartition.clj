(ns rt.postgres.grammar.form-defpartition
  (:require [rt.postgres.grammar.common :as common]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.util :as ut]
            [std.lib :as h]
            [std.string :as str]))

(defn pg-defpartition
  "creates a partition definition"
  {:added "4.0"}
  ([[_ sym spec]]
   (let [mopts (preprocess/macro-opts)
         {:keys [of for-values partition-by default]} (apply hash-map spec)
         {:static/keys [schema]} (meta sym)
         ttok     (common/pg-full-token sym schema)
         parent   (common/pg-linked-token of (assoc mopts :lang :postgres))

         for-values-clause (if default
                             [:default]
                             (cond
                               (:in for-values) [:in (list 'quote (:in for-values))]
                               (:from for-values) [:from (:from for-values) :to (:to for-values)]
                               (vector? for-values) [:in (list 'quote for-values)]
                               :else (h/error "Invalid values specification" {:input for-values})))

         partition-by-clause (if partition-by
                               (let [[method & cols] partition-by]
                                 (list '% [:partition-by method (list 'quote cols)])))]

     [:create-table :if-not-exists ttok
      :partition-of parent
      :for :values for-values-clause
      partition-by-clause])))
