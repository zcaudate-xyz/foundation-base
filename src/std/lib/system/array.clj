(ns std.lib.system.array
  (:require [std.lib.collection :as coll]
            [std.lib.component :as comp]
            [std.lib.foundation :as h]
            [std.lib.impl :refer [defimpl]]
            [std.lib.system.common :as common]
            [std.protocol.component :as protocol.component]))

(declare array? start-array stop-array)

(defn info-array
  "returns the info of elements within the array
 
   (str (info-array [(map->Database nil) (map->Database nil)]))
   => \"[#db{} #db{}]\""
  {:added "3.0"}
  ([arr]
   (mapv (fn [v]
           (cond (or (common/system? v)
                     (array? v)
                     (not (comp/component? v)))
                 v

                 :else
                 (reduce (fn [m [k v]]
                           (cond (comp/component? v) ;; for displaying internal keys
                                 (update-in m ['*] (fnil #(conj % k) []))

                                 :else
                                 (assoc m k v)))
                         (coll/empty-record v)
                         v)))
         arr)))

(defn health-array
  "returns the health of the array
 
   (health-array [(map->Database nil) (map->Database nil)])
   => {:status :ok}"
  {:added "3.0"}
  ([carr]
   (let [results (map comp/health carr)]
     (if (some #(-> % :status (not= :ok)) results)
       {:status :not-healthy
        :results results}
       {:status :ok}))))

(deftype ComponentArray [arr]
  Object
  (toString [this]
    (let [{:keys [tag display]} (meta this)]
      (str "#"
           (or tag "arr")
           (if display
             (display this)
             (info-array this)))))

  protocol.component/IComponent
  (-start [this] (start-array this))
  (-stop  [this] (stop-array  this))

  protocol.component/IComponentQuery
  (-health [this] (health-array this))
  (-info [this level] (info-array this))

  clojure.lang.Seqable
  (seq [this] (seq arr))

  clojure.lang.IObj
  (withMeta [this m]
    (ComponentArray. (with-meta arr m)))

  clojure.lang.IMeta
  (meta [this] (meta arr))

  clojure.lang.Counted
  (count [this] (count arr))

  clojure.lang.Indexed
  (nth [this i]
    (nth arr i nil))

  (nth [ova i not-found]
    (nth arr i not-found)))

(defmethod print-method ComponentArray
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn start-array
  "starts an array of components
 
   (start-array [(map->Database nil) (map->Database nil)])"
  {:added "3.0"}
  ([carr]
   (with-meta
     (ComponentArray. (mapv comp/start (seq carr)))
     (meta carr))))

(defn stop-array
  "stops an array of components"
  {:added "3.0"}
  ([carr]
   (with-meta
     (ComponentArray. (mapv comp/stop (seq carr)))
     (meta carr))))

(defn array
  "constructs a system array
 
   (array {:constructor map->Database} [{:id 1} {:id 2}])"
  {:added "3.0"}
  ([{:keys [constructor defaults]} config]
   (if (vector? config)
     (let [defaults (coll/merge-nested (meta config) defaults)]
       (ComponentArray. (mapv (fn [entry]
                                (if (map? entry)
                                  (constructor (coll/merge-nested defaults entry))
                                  entry))
                              config)))
     (throw (ex-info (str "Config " config " has to be a vector.") {:config config})))))

(defn array?
  "checks if object is a system array
 
   (array? (array {:constructor map->Database} [{}]))
   => true"
  {:added "3.0"}
  ([x]
   (instance? ComponentArray x)))
