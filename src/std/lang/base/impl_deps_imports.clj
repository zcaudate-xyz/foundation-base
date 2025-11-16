(ns std.lang.base.impl-deps-imports
  (:require [std.lang.base.util :as ut]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.book :as b]
            [std.lang.base.book-module :as module]
            [std.string :as str]
            [std.lib :as h]
            [clojure.set :as set]))

(defn get-entry-imports
  "gets all fragment imports from code entries"
  {:added "4.0"}
  [entries]
  (->> entries
       (filter (comp not-empty :deps-native))
       (map (juxt ut/sym-full :deps-native))
       (into {})))

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
   (let [fragments (->> (map :deps-fragment entries)
                        (apply set/union)
                        (filter (comp not seen)))]
     (if (empty? fragments)
       seen
       (get-fragment-deps
        book
        (map #(b/get-fragment-entry book %) fragments)
        (set/union seen (set fragments)))))))

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
        fnatives  (get-entry-imports fentries)
        natives   (get-entry-imports entries)]
    (get-namespace-imports (concat natives fnatives))))

(defn script-imports
  "gets the ns imports for a script"
  {:added "4.0"}
  [book entries]
  (let [ns-imports  (script-import-deps book entries)]
    (format-namespace-imports book ns-imports)))

;;
;;
;;

(defn module-imports
  "gets the ns imports for a script"
  {:added "4.0"}
  [book module-id]
  (let [module    (b/get-module book module-id)
        links     (module/module-deps-code module)
        entries   (vals (:code module))]
    {:native (script-imports book entries)
     :direct links}))

(defn module-code-deps
  "resolves all dependencies"
  {:added "3.0"}
  ([book module-ids]
   (module-code-deps book module-ids {:all #{} :graph {}}))
  ([book module-ids deps]
   (let [submap (h/map-juxt [identity
                             (comp set
                                   module/module-deps-code
                                   (partial b/get-module book))]
                            module-ids)
         ndeps  (-> deps
                    (update :graph merge submap)
                    (update :all into (concat module-ids)))
         nids  (set/difference (apply set/union (vals (:graph ndeps)))
                               (:all ndeps))]
     (if (not-empty nids)
       (module-code-deps book nids ndeps)
       ndeps))))

