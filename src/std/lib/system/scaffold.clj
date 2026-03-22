(ns std.lib.system.scaffold
  (:require [std.lib.component :as component]
            [std.lib.env :as env]
            [std.lib.foundation :as h]
            [std.lib.system.type :as type]))

(defonce ^:dynamic *registry* (atom {}))

(defonce ^:dynamic *running* (atom #{}))

(def ^:dynamic *timeout* 5000)

(defn scaffold:register
  "registers a scaffold in the namespace"
  {:added "3.0"}
  ([]
   (scaffold:register {}))
  ([m]
   (scaffold:register (env/ns-sym) m))
  ([ns {:keys [instance topology config options wrap]
        :or {instance  (env/ns-get ns "*instance*")
             topology  (env/ns-get ns "*topology*")
             config    (env/ns-get ns "config")
             options   {}
             wrap      identity}}]
   (let [ns-key (keyword (str ns))]
     (swap! *registry* assoc ns-key {:instance instance
                                     :topology topology
                                     :config config
                                     :options options
                                     :wrap wrap})
     ns-key)))

(defn scaffold:deregister
  "deregisters a scaffold in the namespace"
  {:added "3.0"}
  ([]
   (scaffold:deregister (env/ns-sym)))
  ([ns]
   (let [ns-key (keyword (str ns))]
     (swap! *registry* dissoc ns-key)
     ns-key)))

(defn scaffold:current
  "returns the current scaffold"
  {:added "3.0"}
  ([]
   (scaffold:current (env/ns-sym)))
  ([ns]
   (get @*registry* (keyword (str ns)))))

(defn scaffold:create
  "creates a system"
  {:added "3.0"}
  ([] (scaffold:create (env/ns-sym)))
  ([ns]
   (let [{:keys [topology config options]} (scaffold:current ns)
         config (if (fn? config) (config) config)]
     (type/system topology config options))))

(defn scaffold:new
  "creates and starts a system"
  {:added "3.0"}
  ([]
   (scaffold:new (env/ns-sym)))
  ([ns]
   (let [sys (scaffold:create ns)]
     (component/start sys))))

(defn scaffold:stop
  "stops the system"
  {:added "3.0"}
  ([]
   (scaffold:stop (env/ns-sym)))
  ([ns]
   (let [{:keys [instance]} (scaffold:current ns)]
     (if (and instance (deref instance))
       #_(log/block {:log/label "STOP SYSTEM"
                     :log/state (str ns)
                     :meter/reflect true
                     :console/retain true
                     :console/style {:header {:label {:text :yellow}
                                              :position {:display {:default false}}
                                              :date  {:text :yellow}}}})
       (do (component/stop @instance)
           (swap! *running* disj (str ns))
           nil)))))

(defn scaffold:start
  "starts the system"
  {:added "3.0"}
  ([]
   (scaffold:start (env/ns-sym)))
  ([ns]
   (let [{:keys [instance]} (scaffold:current ns)]
     (if instance
       (if-not (deref instance)
         #_(log/block {:log/label "START SYSTEM"
                       :log/state (str ns)
                       :meter/reflect true
                       :console/retain true
                       :console/style {:header {:label {:text :yellow}
                                                :position {:display {:default false}}
                                                :date  {:text :yellow}}}})
         (let [sys (scaffold:new ns)]
           (reset! instance sys)
           (swap! *running* conj (str ns))
           sys)
         (throw (ex-info "Already started" {:instance (keys (deref instance))})))))))

(defn scaffold:clear
  "clears the current system"
  {:added "3.0"}
  ([]
   (scaffold:clear (env/ns-sym)))
  ([ns]
   (let [{:keys [instance]} (scaffold:current ns)]
     (if (deref instance)
       (do (swap! *running* disj (str ns))
           (reset! instance nil))
       (throw (ex-info "Already cleared" {:instance (deref instance)}))))))

(defn scaffold:restart
  "restarts the system"
  {:added "3.0"}
  ([]
   (scaffold:restart (env/ns-sym)))
  ([ns]
   (let [{:keys [instance]} (scaffold:current ns)]
     (if (deref instance) (scaffold:stop ns))
     (scaffold:start ns))))

(comment
  #_(defn wait-for
      "TODO"
      {:added "3.0"}
      ([keys]
       (wait-for keys {}))
      ([keys callback]
       (wait-for (env/ns-sym) keys callback))
      ([ns keys callback]
       (common/wait-for
        (create-system ns)
        keys
        (merge {:start  (fn []
                          (log/status (merge {:log/label "COMPONENT CHECK"
                                              :log/state keys}
                                             (profile/item-style {:label (profile/on-grey :yellow)
                                                                  :hide  [:meter :status
                                                                          [:header :position]]}))
                                      nil))
                :success (fn [_ key]
                           (log/status (merge {:log/label "COMPONENT ONLINE"
                                               :log/state key}
                                              (profile/item-style {:label (profile/on-grey :green)
                                                                   :hide  [:meter :status
                                                                           [:header :date]
                                                                           [:header :position]]}))
                                       nil))
                :failure (fn [_ key]
                           (log/status (merge {:log/label "COMPONENT CHECK FAILED"
                                               :log/state key}
                                              (profile/item-style {:label (profile/on-color :red)
                                                                   :hide  [:meter
                                                                           [:header :position]]}))
                                       nil))
                :error   (fn [_ key]
                           (let [t (or (:timeout callback) *timeout*)]
                             (log/status (merge {:log/label "COMPONENT RETRY"
                                                 :log/state [key t]
                                                 :log/message (format "WAITING %dms for %s"
                                                                      t (name key))}
                                                (profile/item-style {:label (profile/on-grey :white)
                                                                     :hide  [:meter :status [:header :position]]}))
                                         nil)))}
               callback)))))

(defn scaffold:registered
  "lists all registered scaffolds"
  {:added "3.0"}
  ([]
   (vals @*registry*)))

(defn scaffold:all
  "lists all running scaffolds"
  {:added "3.0"}
  ([]
   (sort @*running*)))

(defn scaffold:stop-all
  "kills all running scaffolds"
  {:added "3.0"}
  ([]
   (mapv (fn [ns] (scaffold:stop ns))
         @*running*)))
