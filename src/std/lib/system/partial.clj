(ns std.lib.system.partial
  (:require [clojure.set :as set]
            [std.lib.collection :as coll]
            [std.lib.component :as component]
            [std.lib.foundation :as h]
            [std.lib.system.common :as common]
            [std.lib.system.topology :as topology]
            [std.lib.system.type :as type]))

(def ^:dynamic *timeout* 5000)

(def ^:dynamic *callback* {})

(defn valid-subcomponents
  "returns only the components that will work (for partial systems)"
  {:added "3.0"}
  ([full-topology keys]
   (let [expose-keys (topology/get-exposed full-topology)
         valid-keys (set (concat expose-keys keys))
         sub-keys (->> full-topology
                       topology/get-dependencies
                       topology/all-dependencies
                       (coll/map-entries (fn [[k v]] [k (conj v k)])))]
     (reduce-kv (fn [arr k v]
                  (if (set/superset? valid-keys v)
                    (conj arr k)
                    arr))
                []
                sub-keys))))

(defn system-subkeys
  "returns the subcomponents connect with the system"
  {:added "3.0"}
  ([system keys]
   (let [dependencies (->> keys
                           (map (fn [k]
                                  (get-in (meta system) [:dependencies k])))
                           (apply set/union))
         subkeys  (set/difference dependencies keys)]
     (if (empty? subkeys)
       keys
       (set/union keys (system-subkeys system subkeys))))))

(defn subsystem
  "returns the subsystem given certain keys
 
   (subsystem +sys+ #{:entry})"
  {:added "3.0"}
  ([system keys]
   (let [subkeys (system-subkeys system keys)
         {:keys [build order dependencies]} (meta system)]
     (with-meta (type/map->ComponentSystem (select-keys system subkeys))
       {:build (select-keys build subkeys)
        :order (filter subkeys order)
        :dependencies (select-keys build subkeys)}))))

(defn wait
  "wait for a system entry to come online"
  {:added "3.0"}
  ([system key]
   (wait system key *callback*))
  ([system key {:keys [success error failure max-retries timeout] :as callback}]
   (loop [current-retries 0]
     (if (component/with [sub (subsystem system #{key})]
           (= :ok (:status (component/health (get sub key)))))
       (if success (success system key))
       (if (or (nil? max-retries)
               (< current-retries max-retries))
         (do
           (Thread/sleep (or timeout *timeout*))
           (if error (error system key))
           (recur (inc current-retries)))
         (when failure
           (failure system key)
           (throw (ex-info "Max retries reached" {:max-retries max-retries}))))))))

(defn wait-for
  "wait for all system entries to come online"
  {:added "3.0"}
  ([system keys]
   (wait-for system keys *callback*))
  ([system keys {:keys [start final] :as callback}]
   (let [invalid (remove #(contains? system %) keys)]
     (if-not (empty? invalid)
       (throw (ex-info "Invalid keys" {:invalid invalid
                                       :options (clojure.core/keys system)}))))
   (if start (start))
   (doseq [key keys]
     (wait system key callback))
   (if final (final))))
