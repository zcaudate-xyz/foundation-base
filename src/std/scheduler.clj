(ns std.scheduler
  (:require [std.protocol.component :as protocol.component]
            [std.protocol.track :as protocol.track]
            [std.concurrent :as cc]
            [std.scheduler.spawn :as spawn]
            [std.scheduler.common :as common]
            [std.scheduler.types :as types]
            [std.lib :as h :refer [defimpl]]))

(defonce ^:dynamic *no-limit* false)

(common/gen-spawn
 (get-spawn   spawn/get-spawn)
 (stop-spawn  spawn/stop-spawn)
 (kill-spawn  spawn/kill-spawn))

(common/gen-spawn-all
 (get-all-spawn   spawn/get-all-spawn)
 (count-spawn spawn/count-spawn)
 (list-spawn spawn/list-spawn)
 (latest-spawn spawn/latest-spawn)
 (earliest-spawn spawn/earliest-spawn)
 (list-stopped spawn/list-stopped)
 (latest-stopped spawn/latest-stopped)
 (latest spawn/latest)
 (stop-all-spawn spawn/stop-all)
 (kill-all-spawn spawn/kill-all)
 (clear spawn/clear)
 (get-state spawn/get-state)
 (get-program spawn/get-program))

(defn runner:start
  "starts up the runner
 
   (-> (runner:start |runner|)
       (runner:info))
   =>  {:executors {:core {:threads 0, :active 0, :queued 0, :terminated false},
                    :scheduler {:threads 0, :active 0, :queued 0, :terminated false}},
        :programs {}}"
  {:added "3.0"}
  ([{:keys [runtime] :as runner}]
   (let [rt (common/new-runtime runner)]
     (reset! runtime rt))
   runner))

(defn runner:stop
  "stops the runner"
  {:added "3.0"}
  ([{:keys [runtime] :as runner}]
   (stop-all-spawn runner)
   (common/stop-runtime @runtime)
   runner))

(defn runner:kill
  "kills the runner"
  {:added "3.0"}
  ([{:keys [runtime] :as runner}]
   (kill-all-spawn runner)
   (common/kill-runtime @runtime)
   runner))

(defn runner:started?
  "checks if runner is started
 
   (runner:started? |run|)
   => true"
  {:added "3.0"}
  ([{:keys [runtime] :as runner}]
   (let [{:keys [core scheduler]} @runtime]
     (and (cc/executor:started? core)
          (cc/executor:started? scheduler)))))

(defn runner:stopped?
  "checks if runner has stopped
 
   (-> (doto |run| (runner:kill))
       (runner:stopped?))
   => true"
  {:added "3.0"}
  ([{:keys [runtime] :as runner}]
   (let [{:keys [core scheduler]} @runtime]
     (and (cc/executor:stopped? core)
          (cc/executor:stopped? scheduler)))))

(defn runner:health
  "returns health of runner
 
   (runner:health |run|)
   => {:status :ok}"
  {:added "3.0"}
  ([runner]
   (if (runner:started? runner)
     {:status :ok}
     {:status :not-healthy})))

(defn runner:info
  "returns runner info
 
   (h/with:component [runner (runner:create {})]
                     (runner:info runner))
   => {:executors {:core {:threads 0, :active 0, :queued 0, :terminated false},
                   :scheduler {:threads 0, :active 0, :queued 0, :terminated false}},
       :programs {}}"
  {:added "3.0"}
  ([{:keys [runtime] :as runner}]
   (let [{:keys [scheduler core programs running past]} @runtime
         current-fn (fn [program-id]
                      (binding [spawn/*group* true]
                        (->> (get running program-id)
                             (h/map-vals spawn/spawn-info))))
         counts-fn  (fn [program-id]
                      (->> (concat (spawn/list-spawn runtime program-id)
                                   (spawn/list-stopped runtime program-id))
                           (map (fn [{:keys [props]}]
                                  (select-keys @props [:submitted :succeeded :errored])))
                           (apply merge-with + {:submitted 0
                                                :succeeded 0
                                                :errored 0})))
         programs (->> (map (fn [[id m]]
                              [id (-> m
                                      (select-keys [:type :policy])

                                      (assoc ;;;:past (count past)
                                       :current (current-fn id)
                                       :counts (counts-fn id)))])
                            programs)
                       (into {}))
         info-fn  (fn [executor]
                    (cc/executor:info executor :current))]
     {:executors {:core  (if core (info-fn core))
                  :scheduler (if scheduler (info-fn scheduler))}
      :programs programs})))

(defn- runner-string
  ([runner]
   (str "#runner" (runner:info runner))))

(defimpl Runner [runtime]
  :prefix "runner:"
  :string runner-string
  :protocols [std.protocol.track/ITrack
              :body {-track-path [:raw :runner]}
              protocol.component/IComponent
              :body {-props {} -remote? false}])

(defn runner?
  "checks if object is a runner
 
   (runner? |run|)
   => true"
  {:added "3.0"}
  ([obj]
   (instance? Runner obj)))

(defn runner:create
  "creates a runner
 
   (runner:create {:id \"runner\"})
   => runner?"
  {:added "3.0"}
  ([]
   (runner:create {}))
  ([m]
   (map->Runner (assoc m :runtime (atom nil)))))

(defn runner
  "creates and starts a runner
 
   (-> (runner {:id \"runner\"})
       (h/comp:kill))"
  {:added "3.0"}
  ([]
   (runner {}))
  ([m]
   (-> (runner:create m)
       (h/start))))

(defn installed?
  "checks if program is installed
 
   (test-scaffold
    (fn [runner _]
      (installed? runner :world)))
   => true"
  {:added "3.0"}
  ([{:keys [runtime] :as runner} program-id]
   (contains? (:programs @runtime) program-id)))

(defn create-program
  "creates a runner program
 
   (create-program {:type :constant
                    :id :hello
                   :interval 10})"
  {:added "3.0"}
  ([program]
   (let [{:keys [global state create-fn] :as program} (merge spawn/*defaults* program)
         state   (or state (atom (create-fn)))
         program (assoc program :state state)]
     (types/<program> program))))

(defn uninstall
  "uninstalls a program"
  {:added "3.0"}
  ([{:keys [runtime] :as runner} program-id]
   (when (installed? runner program-id)
     (stop-all-spawn runner program-id)
     (clear runner program-id)
     (swap! runtime update :programs dissoc program-id)
     runner)))

(defn install
  "installs a program"
  {:added "3.0"}
  ([{:keys [runtime] :as runner} program]
   (let [{:keys [id] :as program} (create-program program)
         _   (if (installed? runner id) (uninstall runner id))]
     (swap! runtime assoc-in [:programs id] program)
     runner)))

(defn spawn
  "spawns a runner that contains the program"
  {:added "3.0"}
  ([runner program-id]
   (spawn runner program-id {} nil))
  ([{:keys [runtime] :as runner} program-id opts spawn-id]
   (let [{:keys [max]} (get-program runner program-id)]
     (if (or *no-limit*
             (nil? max)
             (< (count-spawn runner program-id) max))
       (spawn/run runtime program-id opts spawn-id))
     runner)))

(defn unspawn
  "unspawns the running program"
  {:added "3.0"}
  ([{:keys [runtime] :as runner} program-id]
   (let [{:keys [min]} (get-program runner program-id)]
     (if (or *no-limit*
             (nil? min)
             (> (count-spawn runner program-id) min))
       (let [policy (or (get-in @runtime [:programs program-id :policy])
                        :last)
             sel-fn (case policy
                      :first spawn/earliest-spawn
                      :last spawn/latest-spawn)
             spawn (sel-fn runtime program-id)]
         (when spawn
           (spawn/stop-spawn runtime program-id (:id spawn))
           spawn)))
     runner)))

(defn trigger
  "triggers the program manually, without spawning
 
   (test-scaffold
    (fn [runner q]
      (trigger runner :world)
      (trigger runner :world)
      (count q)))
   => 2"
  {:added "3.0"}
  ([runner program-id]
   (trigger runner program-id (h/time-ms)))
  ([runner program-id t]
   (let [{:keys [merge-fn main-fn args-fn state]} (get-program runner program-id)
         args (args-fn)
         output (main-fn args t)
         _ (swap! state merge-fn output)]
     output)))

(defn set-interval
  "manually overrides the interval for a spawn/program"
  {:added "3.0"}
  ([{:keys [runtime] :as runner} program-id interval]
   (h/map-vals (fn [spawn]
                 (spawn/set-props spawn :interval interval))
               (get-all-spawn runner program-id))
   (swap! runtime assoc-in [:programs program-id :interval] interval))
  ([{:keys [runtime] :as runner} program-id spawn-id interval]
   (spawn/set-props (get-spawn runner program-id spawn-id)
                    :interval interval)))

(defn get-props
  "gets the current props map for the runner"
  {:added "3.0"}
  ([{:keys [runtime] :as runner} program-id]
   (h/map-vals (fn [spawn]
                 (spawn/get-props spawn))
               (get-all-spawn runner program-id)))
  ([{:keys [runtime] :as runner} program-id spawn-id]
   (spawn/get-props (get-spawn runner program-id spawn-id))))
