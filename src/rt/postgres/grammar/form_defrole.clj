(ns rt.postgres.grammar.form-defrole
  (:require [rt.postgres.grammar.common :as common]
            [rt.postgres.script.impl-base :as base]
            [std.lang :as l]
            [std.lang.base.util :as ut]
            [std.lib.foundation :as f]))

(defn pg-defrole-access
  "creates defrole access form"
  {:added "4.0"}
  ([{:keys [select modify]} role {:keys [schema]
                                  :as mopts}]
   (let [select-forms  (mapcat (fn form-fn [sym]
                                 (cond (symbol? sym)
                                       [[:grant :select :on-table sym :to role]]

                                       (vector? sym)
                                       (if (= (count sym) 1)
                                         (form-fn (first sym))
                                         (let [[lsym policy params] sym
                                               ptok #{(str (first role) "_" (name lsym) "_" (or (:name params) "default"))}
                                               tsch (get-in schema [:tree (keyword (name lsym))])]
                                           [[:grant :select :on-table lsym :to role]
                                            [:alter-table lsym :enable-row-level-security]
                                            [:create-policy ptok :on lsym :as-permissive
                                             \\ :for-select :to role
                                             :using (list 'quote (list [(base/t-where-transform tsch policy mopts)]))]]))
                                       
                                       :else (f/error "Not Allowed" {:input sym})))
                               select)]
     (vec select-forms))))

(defn pg-defrole
  "creates defrole form"
  {:added "4.0"}
  [[_ role-sym {:keys [grant inherit schema] :as config}]]
  (let [mopts (l/macro-opts)
        {:keys [final]} (meta role-sym)
        grant-forms    (mapv (fn [spec-sym]
                               [:grant role-sym :to spec-sym])
                             grant)
        inherit-forms  (mapv (fn [spec-sym]
                               [:grant spec-sym :to role-sym])
                             inherit)
        schema-forms   (mapv (fn [s]
                               [:grant-usage :on-schema #{s} :to role-sym])
                             schema)
        access-forms   (pg-defrole-access config role-sym mopts)]
    `(~'do ~@(if-not final
               [(common/block-do-suppress
                 (list 'do
                       [:drop :owned-by role-sym]
                       [:drop-role :if-exists role-sym]))])
      ~(common/block-do-suppress
        (list 'do [:create-role role-sym
                   (if (or inherit grant)
                     :inherit
                     :noinherit)]))
      ~@(if inherit  [(common/block-do-suppress (apply list 'do inherit-forms))])
      ~@(if grant    [(common/block-do-suppress (apply list 'do grant-forms))])
      ~@(if schema   [(common/block-do-suppress (apply list 'do schema-forms))])
      ~@(if (not-empty access-forms)
          [(common/block-do-suppress (apply list 'do access-forms))]))))
