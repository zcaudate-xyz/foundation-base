(ns rt.postgres.grammar.form-deftype-hydrate
  (:require [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.common :as common]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.book :as book]
            [std.lang.base.util :as ut]
            [std.lib.schema :as schema]
            [std.lib :as h]))

(defn pg-deftype-hydrate-check-link
  "checks a link making sure it exists and is correct type"
  {:added "4.0"}
  [snapshot link type]
  (let [book (snap/get-book snapshot :postgres)
        {:static/keys [dbtype]
         :as entry}  (book/get-base-entry book
                                          (:module link)
                                          (:id link)
                                          (:section link))]
    (cond (not entry)
          (h/error "Entry not found." {:input link})

          (not= dbtype type)
          (h/error "Entry type not correct." {:type type
                                              :input entry})

          :else true)))

(defn pg-deftype-hydrate-link
  "resolves the link for hydration"
  {:added "4.0"}
  ([sym module {:keys [ns] :as ref}]
   (if (and (= "-" (namespace ns))
            (= (name sym) (name ns)))
     [{:section :code
       :lang  :postgres
       :module (:id module)
       :id (symbol (name sym))} false]
     [(select-keys @(or (resolve ns)
                        (h/error "Not found" {:input ref}))
                   [:id :module :lang :section])
      true])))

(defn pg-deftype-hydrate-process-sql
  "processes the sql attribute"
  {:added "4.0"}
  ([sql k attrs]
   (if (:process sql)
     (assoc sql :process
            (h/prewalk
             (fn [x]
               (if (symbol? x)
                 (h/var-sym (or (resolve x)
                                (h/error "Cannot resolve symbol"
                                         {:symbol x
                                          :col k
                                          :attrs attrs})))
                 x))
             (:process sql)))
     sql)))

(defn pg-deftype-hydrate-process-foreign
  "processes the foreign attribute"
  {:added "4.0"}
  ([foreign resolve-link-fn snapshot]
   (h/map-vals (fn [f-spec]
                 (if (:ns f-spec)
                   (let [[link check] (resolve-link-fn f-spec)]
                     (when check (pg-deftype-hydrate-check-link snapshot link :table))
                     (merge f-spec {:ns (keyword (name (:ns f-spec)))
                                    :link link}))
                   f-spec))
               foreign)))

(defn pg-deftype-hydrate-process-ref
  "processes the ref type"
  {:added "4.0"}
  ([k {:keys [ref] :as attrs} resolve-link-fn snapshot]
   (cond (vector? ref)
         [k {:type :ref,
             :required true,
             :ref (merge {:ns  (str (first ref) "." (second ref))
                          :current {:id (second ref)
                                    :schema (first ref)
                                    :type (nth ref 2)}}
                         (nth ref 3))
             :scope :-/ref}]

         :else
         (let [[link check] (resolve-link-fn ref)
               attrs (update attrs :ref
                             merge {:ns   (keyword (name (:ns ref)))
                                    :link link})
               _ (if check (pg-deftype-hydrate-check-link snapshot link :table))]
           [k attrs]))))

(defn pg-deftype-hydrate-process-enum
  "processes the enum type"
  {:added "4.0"}
  ([k attrs snapshot]
   (let [enum-var  (or (resolve (-> attrs :enum :ns))
                       (h/error "Not found" {:input (:enum attrs)}))
         link      (select-keys @enum-var
                                [:id :module :lang :section])
         _ (pg-deftype-hydrate-check-link snapshot link :enum)
         attrs (assoc-in attrs [:enum :ns] (ut/sym-full link))]
     [k attrs])))

(defn pg-deftype-hydrate-attr
  "hydrates a single attribute"
  {:added "4.0"}
  ([k {:keys [type primary ref sql scope foreign] :as attrs}
    {:keys [resolve-link-fn snapshot capture] :as mopts}]
   (let [sql     (if sql (pg-deftype-hydrate-process-sql sql k attrs))
         foreign (if foreign (pg-deftype-hydrate-process-foreign foreign resolve-link-fn snapshot))
         attrs   (cond-> attrs
                   sql (assoc :sql sql)
                   foreign (assoc :foreign foreign))]
     (if primary
       (vswap! capture conj (assoc (select-keys attrs [:type :enum :ref])
                                   :id k)))
     (cond (= :ref type)
           (pg-deftype-hydrate-process-ref k attrs resolve-link-fn snapshot)

           (= :enum type)
           (pg-deftype-hydrate-process-enum k attrs snapshot)

           :else
           [k attrs]))))

(defn pg-deftype-hydrate-spec
  "hydrates the spec"
  {:added "4.0"}
  ([spec mopts]
   (->> (partition 2 spec)
        (mapcat (fn [[k attrs]]
                  (pg-deftype-hydrate-attr k attrs mopts)))
        vec)))

(defn pg-deftype-hydrate
  "hydrates the form with linked ref information"
  {:added "4.0"}
  ([[op sym spec params] grammar {:keys [module
                                         book
                                         snapshot]
                                  :as mopts}]
   (let [resolve-link-fn (partial pg-deftype-hydrate-link sym module)
         capture (volatile! [])
         spec    (pg-deftype-hydrate-spec spec {:resolve-link-fn resolve-link-fn
                                                :snapshot snapshot
                                                :capture capture})
         presch  (schema/schema [(keyword (str sym)) spec])
         hmeta   (assoc (common/pg-hydrate-module-static module)
                        :static/schema-seed presch
                        :static/schema-primary (cond (empty? @capture)
                                                     (h/error "Primary not available")

                                                     (= 1 (count @capture))
                                                     (first @capture)

                                                     :else
                                                     @capture))]
     [hmeta
      (list op (with-meta sym
                 (merge (meta sym) hmeta))
            spec
            params)])))

(defn pg-deftype-hydrate-hook
  "updates the application schema"
  {:added "4.0"}
  ([entry]
   (let [{:static/keys [schema-seed
                        application
                        schema]
          :keys [id]} entry
         rec (get (into {} (map vec (partition 2 (:vec schema-seed))))
                  (keyword (str id)))]
     (doseq [name application]
       (when-not (= rec (get-in @app/*applications* [name :tables id]))
         (swap! app/*applications*
                (fn [m]
                  (-> m
                      (assoc-in [name :tables id] rec)
                      (assoc-in [name :pointers id] (select-keys entry [:id :module :lang :section])))))
         (app/app-rebuild name))))))
