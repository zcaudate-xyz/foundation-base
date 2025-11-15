(ns std.lang.base.impl-deps-imports
  (:require [std.lang.base.util :as ut]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.book :as b]
            [std.string :as str]
            [std.lib :as h]
            [clojure.set :as set]))

(defn collect-entry-imports
  "gets all fragment imports from code entries"
  {:added "4.0"}
  [entries]
  (->> entries
       (filter (comp not-empty :deps-native) )
       (map (juxt ut/sym-full :deps-native))
       (into {})))

(defn collect-script-fragment-deps
  "gets all the fragment dependencies"
  {:added "4.0"}
  ([book entries]
   (collect-script-fragment-deps book entries #{}))
  ([book entries seen]
   (let [fragments (->> (map :deps-fragment entries)
                        (apply set/union)
                        (filter (comp not seen)))]
     (if (empty? fragments)
       seen
       (collect-script-fragment-deps
        book
        (map #(b/get-fragment-entry book %) fragments)
        (set/union seen (set fragments)))))))

(defn collect-script-import-deps
  "collect all native imports"
  {:added "4.0"}
  [book entries]
  (let [fragments (collect-script-fragment-deps book entries)
        fentries  (map #(b/get-fragment-entry book %) fragments)
        fnatives  (collect-entry-imports fentries)
        natives   (collect-entry-imports entries)]
    [natives fnatives fragments]))

(defn build-script-import-ns
  "merges imports for both fragment and code entries"
  {:added "4.0"}
  [natives fragment-natives]
  (->> (concat natives fragment-natives)
       (map (fn [[sym m]]
              {(symbol (namespace sym))
               m}))
       (apply merge-with #(merge-with set/union %1 %2))))

(defn build-script-imports
  "gets the ns imports for a script"
  {:added "4.0"}
  [book entries]
  (let [[natives
         fnatives
         fragments] (collect-script-import-deps book entries)

        
        ;; imports are all done at the front
        ns-natives  (build-script-import-ns natives fnatives)
        imports     (reduce (fn [out [module-id m]]
                              (let [module  (b/get-module book module-id)
                                    imports (select-keys (:native module) (keys m))]
                                (merge out imports)))
                            {}
                            ns-natives)]
    imports))



  
