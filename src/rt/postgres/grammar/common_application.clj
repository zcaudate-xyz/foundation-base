(ns rt.postgres.grammar.common-application
  (:require [std.protocol.deps :as protocol.deps]
            [std.lang :as l]
            [std.string :as str]
            [std.lib.schema :as schema]
            [std.lib :as h :refer [defimpl]]))

(defonce ^:dynamic *applications*
  (atom {}))

(defn- application-string
  ([app]
   (str "#pg.app [" (count (:tables app)) "]\n"
        (str/layout-lines (sort (keys (:tables app))))
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

(defimpl Application [tables schema pointers]
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
  [tables links]
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
        pointers (h/map-vals (fn [m]
                               (if (not (h/pointer? m))
                                 (h/pointer (assoc m :context :lang/postgres))
                                 m))
                             links)
        app (->Application tables schema pointers)
        lu  (zipmap (map (comp keyword str) (h/deps:ordered app))
                    (range))]
    (assoc app :lu lu)))

(defn app-create
  "makes the app graph schema"
  {:added "4.0"}
  ([name & [public-only]]
   (let [entries (->> (app-modules name)
                      (mapcat (fn [module]
                                (->> (:code module)
                                     vals
                                     (filter #(-> % :op (= 'deftype)))
                                     (filter (fn [m]
                                               (if public-only
                                                 (:static/public m)
                                                 true)))))))
         links    (h/map-juxt [(comp keyword str :id)
                               (fn [entry] (select-keys entry [:id :module :section :lang]))]
                              entries)
         tentries (mapcat (comp :vec :static/schema-seed) entries)
         tables   (->> tentries
                       (partition 2)
                       (map vec)
                       (map (fn [[k v]] [(symbol (h/strn k)) v]))
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
                         (app-create-raw (:tables curr) (:pointers curr))
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
