(ns std.lib.deps
  (:require [clojure.set :as set]
            [std.lib.collection :as c]
            [std.lib.foundation :as h]
            [std.lib.impl :as impl]
            [std.lib.sort :as sort]
            [std.protocol.deps :as protocol.deps]))

(impl/build-impl {}
                 protocol.deps/IDeps
                 protocol.deps/IDepsCompile
                 protocol.deps/IDepsMutate
                 protocol.deps/IDepsTeardown)

(defn deps-map
  "creates a map of deps"
  {:added "3.0"}
  ([context ids]
   (c/map-juxt [identity
                (comp set (partial protocol.deps/-get-deps context))]
               ids)))

(defn deps-resolve
  "resolves all dependencies"
  {:added "3.0"}
  ([context ids]
   (deps-resolve context ids {:all #{} :graph {}}))
  ([context ids deps]
   (let [submap (deps-map context ids)
         ndeps  (-> deps
                    (update :graph merge submap)
                    (update :all into (concat ids)))
         nids  (set/difference (apply set/union (vals (:graph ndeps)))
                               (:all ndeps))]
     (if (not-empty nids)
       (deps-resolve context nids ndeps)
       ndeps))))

(defn deps-ordered
  "orders dependencies 
 
   (deps-ordered |ctx|)
   => '(:c :b :a)"
  {:added "3.0"}
  ([context]
   (deps-ordered context (protocol.deps/-list-entries context)))
  ([context ids]
   (-> (deps-resolve context ids)
       :graph
       (sort/topological-sort))))

(defn construct
  "builds an object from context
 
   (construct |ctx|)
   => #{:c :b :a}"
  {:added "3.0"}
  ([context]
   (construct context (protocol.deps/-list-entries context)))
  ([context ids]
   (construct context ids (protocol.deps/-init-construct context)))
  ([context ids acc]
   (let [ids (deps-ordered context ids)]
     (reduce (partial protocol.deps/-step-construct context)
             acc
             ids))))

(defn deconstruct
  "deconstructs an object from context
 
   (deconstruct |ctx| #{:c :b :a} [:a])
   => #{}"
  {:added "3.0"}
  ([context acc]
   (deconstruct context acc (protocol.deps/-init-construct context)))
  ([context acc ids]
   (let [ids (reverse (deps-ordered context ids))]
     (reduce (partial protocol.deps/-step-deconstruct context)
             acc
             ids))))

(defn dependents-direct
  "returns list of direct dependents
 
   (dependents-direct |ctx| :c)
   => #{:b}"
  {:added "3.0"}
  ([context id]
   (dependents-direct context id (protocol.deps/-list-entries context)))
  ([context id selected]
   (set (filter (comp #(% id) set (partial protocol.deps/-get-deps context))
                selected))))

(defn dependents-topological
  "constructs a topological graph of dependents
 
   (dependents-topological |ctx| [:c] [:a :b :c])
   => {:c #{:b}}"
  {:added "3.0"}
  ([context ids selected]
   (reduce (fn [out id]
             (assoc out id (dependents-direct context id selected)))
           {}
           ids)))

(defn dependents-all
  "returns graph of all dependents"
  {:added "3.0"}
  ([context id]
   (dependents-all context id (protocol.deps/-list-entries context)))
  ([context id selected]
   (loop [acc {id (dependents-direct context id selected)}]
     (let [curr (set (keys acc))
           next (apply set/union (vals acc))
           diff (set/difference next curr)]
       (if (empty? diff)
         acc
         (recur (merge acc (dependents-topological context diff selected))))))))

(defn dependents-ordered
  "returns ordered depenedents
 
   (dependents-ordered |ctx| :c)
   => [:a :b :c]"
  {:added "3.0"}
  ([context id]
   (dependents-ordered context id (protocol.deps/-list-entries context)))
  ([context id selected]
   (->> (dependents-all context id selected)
        (sort/topological-sort))))

(defn dependents-refresh
  "refresh all dependents"
  {:added "3.0"}
  ([context id]
   (dependents-refresh context id (protocol.deps/-list-entries context)))
  ([context id selected]
   (let [dependents (dependents-ordered context id selected)]
     [(reduce protocol.deps/-refresh-entry context dependents)
      dependents])))

(defn unload-entry
  "unloads itself as well as all dependents for a given id"
  {:added "3.0"}
  ([context id]
   (let [deps (dependents-ordered context id)]
     [(reduce protocol.deps/-remove-entry context deps)
      deps])))

(defn reload-entry
  "unloads and reloads itself and all dependents"
  {:added "3.0"}
  ([context id]
   (let [deps (dependents-ordered context id)]
     [(h/-> context
            (reduce protocol.deps/-remove-entry % deps)
            (reduce protocol.deps/-refresh-entry % (reverse deps)))
      deps])))
