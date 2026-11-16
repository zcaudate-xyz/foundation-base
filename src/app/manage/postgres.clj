(ns app.manage.postgres
  "Database DSL publication and seed/registry adapters. No SQL is installed."
  (:require [app.manage.make :as make]
            [app.manage.site-map :as site-map]
            [lang.core :as l]
            [postgres.core :as pg]
            [postgres.gen.rpc :as rpc]
            [postgres.typed :as typed]
            [lang.runtime.annex.postgres.base.application :as application]))

(defn context
  [{:keys [application scaffold]}]
  (require scaffold)
  (application/app-rebuild-tables application)
  (let [base (typed/load-app application)
        ctx (typed/load-full application)
        namespaces (set (map str (:namespaces ctx)))
        registry (into (:registry base) (filter #(namespaces (:ns (val %))) (:registry ctx)))]
    ;; A JVM may have several applications loaded. Publish only this one's entries.
    (merge ctx (typed/load-registry registry))))

(defn declarations
  "Returns canonical, portable table/RPC declarations from the authored application."
  [config]
  (let [ctx (context config)
        portable (typed/import-edn-string
                  (typed/export-edn-string ctx))
        entries (concat (vals (get-in portable [:typed :tables]))
                        (filter #(= (:rpc-namespace config) (:ns %))
                                (vals (get-in portable [:typed :functions]))))
        grouped (group-by #(symbol (:name %)) entries)]
    (when (some #(> (count %) 1) (vals grouped))
      (throw (ex-info "Seed exports have colliding names" {:names (keys grouped)})))
    (into (sorted-map) (map (fn [[name entries]] [name (first entries)]) grouped))))

(defn intern-seed!
  "Exports database-owned typed declarations as seed vars, removing deleted exports."
  [target config]
  (let [target (the-ns target) entries (declarations config)]
    (doseq [[name v] (ns-publics target)
            :when (and (:seed/declaration (meta v)) (not (contains? entries name)))]
      (ns-unmap target name))
    (doseq [[name entry] entries]
      (when-let [v (ns-resolve target name)]
        (when-not (:seed/declaration (meta v))
          (throw (ex-info "Seed export conflicts with an authored var" {:name name}))))
      (alter-meta! (intern target name entry) assoc :seed/declaration true))
    nil))

(defn rpc-spec [config entry]
  (rpc/rpc-client-spec entry {:schema (:rpc-schema config)}))

(defn registry
  "Supplies the shared site-map resolver from PostgreSQL DSL and configured seed/group owners."
  [{:keys [application seed-namespace group-namespaces rpc-namespace] :as config}]
  (let [entries (declarations config)
        rpcs (into {} (for [[_ entry] entries :when (= rpc-namespace (:ns entry))]
                        [(:name entry) entry]))
        app (pg/app application)
        schema (site-map/worker-schema (pg/bind-schema (:schema app)))]
    {:seed-namespaces [seed-namespace] :group-namespaces group-namespaces
     :resolve (fn [qualified]
                (when-let [v (requiring-resolve qualified)]
                  (when (or (not= (namespace qualified) (str seed-namespace))
                            (:seed/declaration (meta v)))
                    (var-get v))))
     :rpc? #(= rpc-namespace (:ns %)) :get-rpc rpcs
     :rpc-spec (partial rpc-spec config) :schema (constantly schema)
     :table-schema (fn [table]
                     (let [pointer (get-in app [:pointers (symbol table)])]
                       (or (:static/schema (l/get-entry pointer))
                           (throw (ex-info "Table has no SQL schema" {:table table})))))}))

(defn target
  "Constructs SQL and portable typed-EDN publication from an application declaration."
  [{:keys [scaffold output-root depends sources emit] :as config}]
  {:depends (vec depends) :sources (vec sources) :outputs [output-root]
   :render (fn [context-map]
             (let [ctx (context config)
                   sql (make/render-sections context-map
                         {:load [scaffold]
                          :sections [{:type :module.schema :lang :postgres :main scaffold
                                      :target output-root :file "schema.sql" :emit emit}]})]
               (assoc sql (str output-root "/schema.edn")
                      (typed/export-edn-string ctx))))})
