(ns lang.core.impl-deps-imports
  (:require [clojure.set :as set]
             [lang.base.book :as b]
             [lang.base.book-module :as module]
             [lang.core.impl-entry :as entry]
             [lang.base.emit-template :as impl-template]
             [lang.base.util :as ut]
             [std.lib.collection :as collection]))

(defn- materialize-import-entry
  [book entry]
  (if (= :code (:section entry))
    (impl-template/materialize-code-entry book entry)
    entry))

(defn get-entry-imports
  "gets all fragment imports from code entries"
  {:added "4.0"}
  ([entries]
   (->> entries
        (filter (comp not-empty :deps-native))
        (map (juxt ut/sym-full :deps-native))
        (into {})))
  ([book entries]
   (get-entry-imports (map (partial materialize-import-entry book) entries))))

(defn get-namespace-imports
  "merges imports for both fragment and code entries"
  {:added "4.0"}
  [imports]
  (->> imports
       (map (fn [[sym m]]
              {(symbol (namespace sym))
               m}))
       (apply merge-with #(merge-with set/union %1 %2))))

;;
;; script imports
;;

(defn get-fragment-deps
  "gets all the fragment dependencies"
  {:added "4.0"}
  ([book entries]
   (get-fragment-deps book entries #{}))
  ([book entries seen]
   (let [entries    (map (partial materialize-import-entry book) entries)
         fragments (->> (map :deps-fragment entries)
                         (apply set/union)
                         (filter (comp not seen)))]
     (if (empty? fragments)
       seen
       (get-fragment-deps
        book
        (map #(b/get-fragment-entry book %) fragments)
        (set/union seen (set fragments)))))))

(defn- emitted-native-imports
  [book entry]
  (let [fragments      (get-fragment-deps book [entry])
        source-entries (map #(b/get-fragment-entry book %) fragments)
        source-modules (->> (concat [(:module entry)]
                                    (map :module source-entries))
                            distinct
                            (map #(b/get-module book %))
                            (remove nil?))
        native-lu      (apply merge (map :native-lu source-modules))
        native         (apply merge-with merge (map :native source-modules))
        fragment-native (apply merge-with set/union (map :deps-native source-entries))
        emitted        (reduce (fn [out sym]
                                 (if (symbol? sym)
                                   (let [word   (name sym)
                                         idx    (.indexOf word ".")
                                         alias  (if (pos? idx)
                                                  (symbol (subs word 0 idx))
                                                  sym)
                                         package (get native-lu alias)]
                                     (if package
                                       (update out package (fnil conj #{}) alias)
                                       out))
                                   out))
                               {}
                               (tree-seq coll? seq (:form entry)))
        used           (merge-with set/union
                                   (:deps-native entry)
                                   fragment-native
                                   emitted)]
    (select-keys native (keys used))))

(defn- get-emitted-entry-imports
  [book entries]
  (->> entries
       (map (partial materialize-import-entry book))
       (map #(emitted-native-imports book %))
       (apply merge-with merge)))

(defn format-namespace-imports
  [book ns-imports]
  (reduce (fn [out [module-id m]]
            (let [module  (b/get-module book module-id)
                  imports (select-keys (:native module) (keys m))]
              (merge out imports)))
          {}
          ns-imports))

(defn script-import-deps
  "collect all native imports"
  {:added "4.0"}
  [book entries]
  (let [fragments (get-fragment-deps book entries)
         fentries  (map #(b/get-fragment-entry book %) fragments)
         fnatives  (get-entry-imports book fentries)
         natives   (get-entry-imports book entries)]
     (get-namespace-imports (concat natives fnatives))))

(defn- ensured-module-imports
  [book module-ids]
  (->> module-ids
       (map #(b/get-module book %))
       (remove nil?)
       (mapcat (fn [module]
                 (filter (comp :ensure val) (:native module))))
       (into {})))

(defn script-imports
  "gets the ns imports for a script"
  {:added "4.0"}
  [book entries]
  (let [module-ids (into #{} (keep :module) entries)
        imports    (get-emitted-entry-imports book entries)
        ensured    (ensured-module-imports book module-ids)]
    (merge-with merge imports ensured)))

;;
;;
;;

(defn module-imports-from-view
  "gets module-level and entry-derived imports from a prepared module view"
  {:added "4.1"}
  [book module-view]
  (let [links   (module/module-deps-code book module-view)
        entries (vals (:code module-view))
        ensured (->> (:native module-view)
                     (filter (comp :ensure val))
                     (into {}))
        native  (merge-with merge
                            (script-imports book entries)
                            ensured)]
    {:native native
     :direct links}))

(defn module-imports
  "gets the imports for a module id"
  {:added "4.0"}
  [book module-id]
  (module-imports-from-view book
                            (module/module-derived-view book
                                                        (b/get-module book module-id))))

(defn module-code-deps
  "resolves all dependencies"
  {:added "3.0"}
  ([book module-ids]
   (module-code-deps book module-ids {:all #{} :graph {}}))
  ([book module-ids deps]
    (let [submap (collection/map-juxt [identity
                             (fn [module-id]
                               (->> (b/get-module book module-id)
                                    (module/module-deps-code book)
                                    set))]
                             module-ids)
          ndeps  (-> deps
                     (update :graph merge submap)
                    (update :all into (concat module-ids)))
         nids  (set/difference (apply set/union (vals (:graph ndeps)))
                               (:all ndeps))]
     (if (not-empty nids)
       (module-code-deps book nids ndeps)
       ndeps))))
