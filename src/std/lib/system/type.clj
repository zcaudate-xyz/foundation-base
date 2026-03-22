(ns std.lib.system.type
  (:require [std.lib.collection :as coll]
            [std.lib.component :as component]
            [std.lib.foundation :as h]
            [std.lib.impl :refer [defimpl]]
            [std.lib.sort :as sort]
            [std.lib.system.array :as array]
            [std.lib.system.common :as common]
            [std.lib.system.topology :as topology]
            [std.protocol.component :as protocol.component]
            [std.protocol.track :as protocol.track]))

(declare start-system stop-system kill-system info-system health-system)

(defonce ^:dynamic *stop-fn* component/stop)

(defn info-system
  "gets the info for the system"
  {:added "3.0"}
  ([sys]
   (reduce (fn [m [k v]]
             (cond (or (common/system? v)
                       (array/array? v)
                       (not (component/component? v)))
                   (assoc m k v)

                   :else
                   (try
                     (assoc m k (reduce (fn [m [k v]]
                                          (cond (component/component? v)
                                                (update-in m ['*] (fnil #(conj % k) []))

                                                :else
                                                (assoc m k v)))
                                        (coll/empty-record v)
                                        v))
                     (catch Throwable t
                       v))))
           {} sys)))

(defn health-system
  "gets the health for the system"
  {:added "3.0"}
  ([system]
   (let [results (reduce-kv (fn [out k v]
                              (assoc out k (component/health v)))
                            {}
                            system)]
     (if (every? #(-> % :status (= :ok)) (vals results))
       {:status :ok}
       {:status :not-healthy
        :results results}))))

(defn remote?-system
  "gets the remote status for the system"
  {:added "3.0"}
  ([system]
   (boolean (some component/remote? (vals system)))))

(defn system-string
  "get the string for the system"
  {:added "3.0"}
  ([sys]
   (let [{:keys [tag display]} (meta sys)]
     (str "#" (or tag "sys") " "
          (if display
            (display sys)
            (info-system sys))))))

(defimpl ComponentSystem []
  :suffix "-system"
  :string system-string
  :protocols [common/ISystem
              protocol.component/IComponent
              :exclude [-started? -stopped?]
              :body {-props  {}}
              protocol.track/ITrack
              :body {-track-path [:system]}])

(defn system
  "creates a system of components
 
   ;; The topology specifies how the system is linked
   (def topo {:db        [map->Database]
              :files     [[map->Filesystem]]
              :catalogs  [[map->Catalog] [:files {:type :element :as :fs}] :db]})"
  {:added "3.0"}
  ([topology config]
   (system topology config {:partial false}))
  ([topology config {:keys [partial? tag display notify] :as opts}]
   (let [build   (topology/long-form topology)
         expose  (topology/get-exposed build)
         dependencies (topology/get-dependencies build)
         order (sort/topological-sort dependencies)
         initial  (apply dissoc build expose)]
     (-> (reduce-kv (fn [sys k {:keys [constructor compile defaults] :as build}]
                      (let [cfg (get config k)]
                        (assoc sys k (cond (= compile :array)
                                           (array/array (update build :defaults merge defaults)
                                                        cfg)

                                           :else
                                           (constructor (coll/merge-nested defaults cfg))))))
                    (ComponentSystem.)
                    initial)
         (with-meta (merge {:build   build
                            :order   order
                            :dependencies dependencies}
                           opts))))))

(defn system-import
  "imports a component into the system"
  {:added "3.0"}
  ([component system import]
   (reduce-kv (fn [out k v]
                (let [{:keys [type as]} (get import k)
                      as (or as k)
                      subsystem (get system k)]
                  (cond (array/array? out)
                        (cond->> (seq out)
                          (= type :element) (map #(assoc %2 as %1) subsystem)
                          (= type :single)  (map #(assoc % as subsystem))
                          :then (array/->ComponentArray))

                        :else
                        (assoc out as subsystem))))
              component
              import)))

(defn system-expose
  "exposes a component into the system"
  {:added "3.0"}
  ([_ system {:keys [in function] :as opts}]
   (let [subsystem (get system in)]
     (cond (array/array? subsystem)
           (->> (sequence subsystem)
                (map function)
                (array/->ComponentArray))

           :else
           (function subsystem)))))

(defn start-system
  "starts a system"
  {:added "3.0"}
  ([system]
   (let [{:keys [build order notify] :as meta} (meta system)]
     (reduce (fn [out k]
               (let [{:keys [type import setup hooks functions] :as opts} (get build k)
                     {:keys [pre-start post-start]} hooks
                     {:keys [on-start on-started]} notify
                     _ (if on-start (on-start system k))
                     component (get out k)
                     setup     (or setup identity)
                     result    (cond-> (component/perform-hooks component functions pre-start)
                                 (= type :build)
                                 (system-import out import)

                                 (= type :expose)
                                 (system-expose out opts)

                                 :finally
                                 (-> component/start
                                     setup
                                     (component/perform-hooks functions post-start)))
                     system   (assoc out k result)
                     _ (if on-started (on-started system k))]
                 system))
             system
             order))))

(defn system-deport
  "deports a component from the system"
  {:added "3.0"}
  ([component import]
   (reduce-kv (fn [out k v]
                (let [{:keys [type as]} (get import k)]
                  (cond (array/array? out)
                        (->> (seq out)
                             (map #(dissoc % as))
                             (array/->ComponentArray))

                        :else
                        (dissoc out as))))
              component
              import)))

(defn stop-system
  "stops a system"
  {:added "3.0"}
  ([system]
   (let [{:keys [build order notify] :as meta} (meta system)]
     (reduce (fn [out k]
               (let [{:keys [type import teardown hooks functions] :as opts} (get build k)
                     {:keys [pre-stop post-stop]} hooks
                     {:keys [on-stop on-stopped]} notify
                     _  (if on-stop (on-stop system k))
                     component (get out k)
                     teardown  (or teardown identity)
                     component (-> component
                                   (component/perform-hooks functions pre-stop)
                                   (teardown))
                     system    (cond (= type :build)
                                     (assoc out k (-> component
                                                      (*stop-fn*)
                                                      (system-deport import)
                                                      (component/perform-hooks functions post-stop)))

                                     (= type :expose)
                                     (dissoc out k))
                     _  (if on-stopped (on-stopped system k))]
                 system))
             system
             (reverse order)))))

(defn kill-system
  "kills the system (stopping immediately)"
  {:added "3.0"}
  ([system]
   (binding [*stop-fn* component/kill]
     (stop-system system))))
