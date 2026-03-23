(ns rt.postgres.grammar.common-application
  (:require [rt.postgres.grammar.typed-common :as typed]
            [rt.postgres.grammar.typed-parse :as tparse]
            [std.lang :as l]
            [std.lib.collection :as collection]
            [std.lib.context.pointer :as ptr]
            [std.lib.deps :as deps]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl]
            [std.lib.schema :as schema]
            [std.protocol.deps :as protocol.deps]
            [std.string.prose :as prose]))

(defonce ^:dynamic *applications*
  (atom {}))

(defn- application-string
  ([app]
   (str "#pg.app [" (count (:tables app)) "]\n"
        (prose/layout-lines (sort (keys (:tables app))))
        "\n")))

(defn- app-list-entries
  ([app]
   (keys (get app :tables))))

(defn- app-get-entry
  ([app id]
   (get-in app [:schema :tree (name id)])))

(defn- app-get-deps
  ([app id]
   (->> (get-in app [:tables id])
        (partition 2)
        (map vec)
        (into {})
        (keep (fn [[k m]]
                (-> m :ref :ns)))
        (map (comp symbol name))
        (set))))

(impl/defimpl Application [tables schema pointers]
  :string application-string
  :protocols [protocol.deps/IDeps
              :prefix "app-"])

(defn app-modules
  "checks for modules related to a given application"
  {:added "4.0"}
  ([name]
   (->> (l/get-book (l/runtime-library) :postgres)
        :modules
        vals
        (filter (fn [module] (->> module :static :application (some (fn [x] (= x name)))))))))

(defn app-create-raw
  "creates a schema from tables and links"
  {:added "4.0"}
  [tables links & [typed-info]]
  (let [ref-fn   (fn [{:keys [ref] :as attrs}]
                   (let [rkey (symbol (namespace (:key ref)))]
                     {:link (select-keys (get links rkey)
                                         [:id :module :lang :section])}))
        schema   (schema/with:ref-fn [ref-fn]
                   (->> (mapcat (fn [[k v]]
                                  [(keyword
                                    (name k))
                                   v])
                                tables)
                        (vec)
                        (schema/schema)))
        pointers (collection/map-vals (fn [m]
                               (if (not (ptr/pointer? m))
                                 (ptr/pointer (assoc m :context :lang/postgres))
                                 m))
                             links)
        app (->Application tables schema pointers)
        lu  (zipmap (map (comp keyword str) (deps/deps-ordered app))
                    (range))]
    (cond-> (assoc app :lu lu)
      typed-info (assoc :typed typed-info))))

(defn- module-typed
  [modules]
  (->> modules
       (map (comp tparse/analyze-namespace #(symbol (str (:id %)))))
       (map (fn [analysis] (assoc analysis :tables [])))
       (map typed/analysis->typed)
       (apply typed/merge-typed)))

(defn- app-create-typed
  [tables modules]
  (typed/merge-typed (-> tables tparse/analyze-tables typed/analysis->typed)
                     (module-typed modules)))

(defn app-create
  "makes the app graph schema"
  {:added "4.0"}
  ([name & [public-only]]
   (let [modules  (app-modules name)
         entries (->> modules
                      (mapcat (fn [module]
                                (->> (:code module)
                                     vals
                                     (filter #(-> % :op (= 'deftype)))
                                     (filter (fn [m]
                                               (if public-only
                                                 (:static/public m)
                                                 true)))))))
         links    (collection/map-juxt [(comp keyword str :id)
                               (fn [entry] (select-keys entry [:id :module :section :lang]))]
                              entries)
         tentries (mapcat (comp :vec :static/schema-seed) entries)
         tables   (->> tentries
                       (partition 2)
                       (map vec)
                       (map (fn [[k v]] [(symbol (f/strn k)) v]))
                       (into {}))]
     (app-create-raw tables links))))

(defn app-clear
  "clears the entry for an app"
  {:added "4.0"}
  ([name]
   (-> (swap! *applications* dissoc name)
       (get name))))

(defn app-rebuild
  "rebuilds the app schema"
  {:added "4.0"}
  ([name]
   (-> (swap! *applications*
              (fn [m]
                (assoc m name
                       (if-let [curr (get m name)]
                         (app-create-raw (:tables curr)
                                         (:pointers curr)
                                         (app-create-typed (:tables curr)
                                                           (app-modules name)))
                         (app-create name)))))
       (get name))))

(defn app-rebuild-tables
  "initiate rebuild of app schema"
  {:added "4.0"}
  ([name]
   (-> (swap! *applications* assoc name (app-create name))
       (get name))))

(defn app-list
  "rebuilds the app schema"
  {:added "4.0"}
  ([]
   (keys @*applications*)))

(defn app
  "gets an app"
  {:added "4.0"}
  ([]
   @*applications*)
  ([name]
   (get @*applications* name)))

(defn app-schema
  "gets the app schema"
  {:added "4.0"}
  ([name]
   (:schema (get @*applications* name))))

(defn app-typed
  "gets the app typed payload"
  {:added "4.1"}
  ([name]
   (or (:typed (get @*applications* name))
       (when-let [curr (get @*applications* name)]
         (let [typed-info (app-create-typed (:tables curr)
                                            (app-modules name))]
           (get-in (swap! *applications* assoc-in [name :typed] typed-info)
                   [name :typed]))))))
