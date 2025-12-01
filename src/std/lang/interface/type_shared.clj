(ns std.lang.interface.type-shared
  (:require [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]
            [std.lib :as h :refer [defimpl]]))

(defonce ^:dynamic *groups*
  (atom {}))

(defn get-groups
  "gets all shared groups
 
   (shared/get-groups)
   ;; (:hara/rt.postgres :hara/rt.redis :hara/rt.nginx :hara/rt.cpython.shared :hara/rt.luajit.shared)
   => vector?"
  {:added "4.0"}
  []
  (vec (keys @*groups*)))

(defn get-group-count
  "gets the group count for a type and id
 
   (shared/get-group-count :hara/rt.redis)
   ;; {:default 21, :test 2}
   => map?"
  {:added "4.0"}
  ([]
   (h/map-juxt [identity
                get-group-count]
               (get-groups)))
  ([type & [id]]
   (if id
     (or (get-in @*groups*
                 [type id :count])
         0)
     (h/map-vals :count
                 (get @*groups* type)))))

;(:hara/rt.postgres :hara/rt.redis :hara/rt.nginx :hara/rt.cpython.shared :hara/rt.luajit.shared)

(defn update-group-count
  "updates the group counte"
  {:added "4.0"}
  [type id f]
  (swap! *groups*
         (fn [m]
           (update-in m [type id :count] f))))

(defn get-group-instance
  "gets the group instance"
  {:added "4.0"}
  [type id]
  (get-in @*groups*
          [type id :instance]))

(defn set-group-instance
  "sets the group instance"
  {:added "4.0"}
  [type id instance & [count config client]]
  (swap! *groups*
         (fn [m]
           (cond-> m
             :then  (assoc-in [type id :instance]
                              instance)
             count  (assoc-in [type id :count]
                              count)
             config (assoc-in [type id :config]
                              config)
             client (assoc-in [type id :client]
                              client)))))

(defn update-group-instance
  "updates the group instance"
  {:added "4.0"}
  [type id f]
  (swap! *groups*
         (fn [m]
           (update-in m [type id :instance] f))))

(defn restart-group-instance
  "restarts the group instance"
  {:added "4.0"}
  [type id]
  (-> (swap! *groups*
             (fn [m]
               (let [{:keys [config client instance]} (get-in m [type id])]
                 (if instance
                   (h/stop instance))
                 (assoc-in m [type id :instance]
                           (h/start ((:constructor client)
                                     config))))))
      (get-in [type id :instance])))


(defn remove-group-instance
  "removes the group instance"
  {:added "4.0"}
  [type id]
  (swap! *groups*
         (fn [m]
           (update-in m [type] dissoc id))))

(defn start-shared
  "starts a shared runtime client"
  {:added "4.0"}
  [{:keys [id client temp config] :as rt}]
  (let [{:keys [type constructor]} client
        instance (get-group-instance type id)]
    (cond (not instance)
          (set-group-instance type id (h/start (constructor config))
                              1
                              config
                              client)
          
          :else
          (update-group-count type id (fnil inc 0))))
  rt)

(defn stop-shared
  "stops a shared runtime client"
  {:added "4.0"}
  [{:keys [id client temp config] :as rt}]
  (let [{:keys [type constructor]} client
        cnt      (get-group-count type id)
        instance (get-group-instance type id)]
    (cond (not instance) nil

          (and (or temp
                   (= id :default))
               (<= cnt 1))
          (do (h/stop instance)
              (remove-group-instance type id))
          
          :else
          (update-group-count type id (fnil dec 0))))
  rt)

(def kill-shared stop-shared)

(def wrap-shared
  (fn [f client id config]
    (fn [shared & args]
      (apply f
             (merge
              (get-in @*groups*
                      [(:type client) id :instance])
              config)
             args))))

(defn- rt-shared-string [{:keys [tag lang] :as rt}]
  (str "#rt.shared" (:tag (get-group-instance (-> rt :client :type)
                                              (-> rt :id)))
       [lang]))

(defimpl SharedRuntime
  [id client temp config]
  :suffix "-shared"
  :string rt-shared-string
  :protocols [protocol.component/IComponent
              protocol.context/IContext
              :method
              {-raw-eval    (wrap-shared protocol.context/-raw-eval client id config),
               -init-ptr    (wrap-shared protocol.context/-init-ptr client id config),
               -tags-ptr    (wrap-shared protocol.context/-tags-ptr client id config),
               -deref-ptr   (wrap-shared protocol.context/-deref-ptr client id config),
               -display-ptr (wrap-shared protocol.context/-display-ptr client id config),
               -invoke-ptr  (wrap-shared protocol.context/-invoke-ptr client id config),
               -transform-in-ptr (wrap-shared protocol.context/-transform-in-ptr client id config),
               -transform-out-ptr (wrap-shared protocol.context/-transform-out-ptr client id config)}
              protocol.context/IContextLifeCycle
              :method
              {-has-module?  (wrap-shared protocol.context/-has-module? client id config),
               -setup-module (wrap-shared protocol.context/-setup-module client id config),
               -teardown-module (wrap-shared protocol.context/-teardown-module client id config),
               -has-ptr?  (wrap-shared protocol.context/-has-ptr? client id config),
               -setup-ptr (wrap-shared protocol.context/-setup-module client id config)
               -teardown-ptr (wrap-shared protocol.context/-teardown-ptr client id config)}])

(defn rt-shared:create
  "creates a shared runtime client"
  {:added "4.0"}
  [{:rt/keys [id client temp] :as m}]
  (let [rtks   (h/qualified-keys m :rt)
        config (apply dissoc m (keys rtks))]
    (map->SharedRuntime (merge m
                                {:id (or id :default)
                                 :config config}
                                (h/unqualify-keys rtks)))))

(defn rt-shared
  "creates and starts and shared runtime client"
  {:added "4.0"}
  ([m]
   (-> (rt-shared:create m)
       (h/start))))

(defn rt-is-shared?
  "checks if a runtime is shared"
  {:added "4.0"}
  [obj]
  (= "std.lang.interface.type_shared.SharedRuntime"
     (.getName ^Class (type obj))))

(defn rt-get-inner
  "gets the inner runtime"
  {:added "4.0"}
  [rt]
  (if (rt-is-shared? rt)
    (get-group-instance (-> rt :client :type)
                        (-> rt :id))
    rt))

