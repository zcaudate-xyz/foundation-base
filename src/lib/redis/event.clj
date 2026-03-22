(ns lib.redis.event
  (:require [clojure.set]
            [lib.redis.impl.common :as common]
            [net.resp.pool :as pool]
            [net.resp.wire :as wire]
            [std.concurrent :as cc]
            [std.lib.collection :as collection]
            [std.lib.component :as component]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.future :as future]
            [std.lib.impl :as impl]))

;;
;; ACTIONS
;;

(defonce +actions+
  (atom {}))

(defn action:add
  "adds an action from registry"
  {:added "3.0"}
  ([key {:keys [subscribe unsubscribe args wrap] :as m}]
   (swap! +actions+ assoc key m)))

(defn action:remove
  "removes an action from registry"
  {:added "3.0"}
  ([key]
   (swap! +actions+ dissoc key)))

(defn action:list
  "lists action types"
  {:added "3.0"}
  ([]
   (keys (keys @+actions+))))

(defn action:get
  "gets action type"
  {:added "3.0"}
  ([key]
   (or (get @+actions+ key)
       (throw (ex-info "Not found" {:input key})))))


;;
;; LISTENER
;;


(defn listener-string
  "string description of a listener"
  {:added "3.0"}
  ([{:keys [type connection] :as listener}]
   (let [{:keys [id args raw thread]} connection]
     (str "#" (name type) (:id listener) " "
          {:id id :args args
           :running (and (not (future/future:complete? thread))
                         (component/started? raw))}))))

(impl/defimpl Listener [type id namespace handler connection]
  :string listener-string)

(defn listener?
  "checks that object is a listener"
  {:added "3.0"}
  ([obj]
   (instance? Listener obj)))

(defn listener-loop
  "creates a listener loop"
  {:added "3.0"}
  ([conn f]
   (loop []
     (let [msg (wire/read conn)]
       (f msg)
       (recur)))))

(defn listener:create
  "creates a listener"
  {:added "3.0"}
  ([{:keys [namespace pool] :as redis} type id input handler]
   (let [[raw-id raw] (cc/pool:acquire pool)
         action (action:get type)
         args   ((action :args) namespace input)
         _      (wire/call raw (into [(action :subscribe)] args))
         connection {:id raw-id
                     :args args
                     :pool pool
                     :thread (future/future
                               (->> ((action :wrap) handler redis)
                                    (listener-loop raw)))
                     :raw raw}]
     (-> {:type type :id id
          :namespace namespace
          :input input
          :handler handler
          :connection connection}
         (map->Listener)))))

(defn listener:teardown
  "tears down the listener"
  {:added "3.0"}
  ([{:keys [type connection] :as listener}]
   (let [{:keys [id args pool thread raw]} connection
         action (action:get type)
         _ (wire/write raw (into [(action :unsubscribe) args]))
         _ (future/future:cancel thread)
         _ (cc/pool:release pool id true)]
     id)))

(defn listener:add
  "adds a listener to the redis client"
  {:added "3.0"}
  ([type redis id input handler]
   (listener:add type redis id input handler {}))
  ([type {:keys [namespace runtime pool] :as redis} id input handler opts]
   (let [{:keys [listeners]} @runtime
         id (or id (keyword (f/sid)))]
     (when-not (get-in listeners [type id])
       (doto (-> (listener:create redis type id input handler)
                 (merge opts))
         (->> (swap! runtime assoc-in [:listeners type id])))))))

(defn listener:remove
  "removes a listener from the client"
  {:added "3.0"}
  ([type {:keys [runtime] :as redis} id]
   (when-let [listener (-> (:listeners @runtime)
                           (get-in [type id]))]

     (listener:teardown listener)
     (swap! runtime update :listeners collection/dissoc-nested [type id])
     listener)))

(defn listener:all
  "lists all listeners"
  {:added "3.0"}
  ([{:keys [runtime]}]
   (mapcat vals (vals (:listeners @runtime)))))

(defn listener:count
  "counts all listeners"
  {:added "3.0"}
  ([{:keys [runtime]}]
   (collection/map-vals count (:listeners @runtime))))

(defn listener:list
  "lists all listeners
 
   (-> (doto |client|
         (subscribe  :foo  [\"foo\"] prn)
         (psubscribe :bar \"*\" prn))
       (listener:list))
   => {:subscribe [:foo] :psubscribe [:bar]}"
  {:added "3.0"}
  ([{:keys [runtime]}]
   (collection/map-vals keys (:listeners @runtime))))

(defn listener:get
  "gets a client listener
 
   (-> (doto |client|
         (subscribe  :foo  [\"foo\"] prn))
       (listener:get :subscribe :foo))
   => listener?"
  {:added "3.0"}
  ([{:keys [runtime]} type id]
   (get-in (:listeners @runtime)
           [type id])))


;;
;; SUBSCRIBE
;;


(defn- identity:args [_ args] (collection/seqify args))

(defn subscribe:wrap
  "wrapper for the subscribe delivery function"
  {:added "3.0"}
  ([handler {:keys [format] :as redis}]
   (fn [msg]
     (let [[type channel data] (seq msg)]
       (if (= (wire/coerce type :string) "message")
         (handler (wire/coerce channel :string)
                  redis
                  nil
                  (wire/coerce data format)))))))

(action:add :subscribe
            {:subscribe   "SUBSCRIBE"
             :unsubscribe "UNSUBSCRIBE"
             :args identity:args
             :wrap subscribe:wrap})

(defn subscribe
  "subscribes to a channel on the cache"
  {:added "3.0"}
  ([redis id channels handler]
   (listener:add :subscribe redis id channels handler)))

(defn unsubscribe
  "unsubscribes from a channel"
  {:added "3.0"}
  ([redis id]
   (listener:remove :subscribe redis id)))

;;
;; PSUBSCRIBE
;;

(defn psubscribe:wrap
  "wrapper for the psubscribe delivery function"
  {:added "3.0"}
  ([handler {:keys [format] :as redis}]
   (fn [msg]
     (let [[type pattern channel data] (seq msg)]
       (if (= (wire/coerce type :string) "pmessage")
         (handler (wire/coerce channel :string)
                  redis
                  nil
                  (wire/coerce data format)))))))

(action:add :psubscribe
            {:subscribe   "PSUBSCRIBE"
             :unsubscribe "PUNSUBSCRIBE"
             :args identity:args
             :wrap psubscribe:wrap})

(defn psubscribe
  "subscribes to a pattern on the cache"
  {:added "3.0"}
  ([redis id pattern handler]
   (listener:add :psubscribe redis id pattern handler)))

(defn punsubscribe
  "unsubscribes from the pattern"
  {:added "3.0"}
  ([redis id]
   (listener:remove :psubscribe redis id)))

;;
;; NOTIFY 
;;

(def +default+
  #{:string :generic :hash :list :expired :stream})

(def +lu+
  {:generic "g" :hash "h" :string "$" :list "l"
   :set "s" :sorted "z" :stream "t" :expired "x" :evicted "e"})

(def +rlu+ (collection/map-entries (fn [[k v]] [(first v) k]) +lu+))

(defn events-string
  "creates a string from a set of enums"
  {:added "3.0"}
  ([events]
   (apply str (keep +lu+ events))))

(defn events-parse
  "creates a set of enums from a string"
  {:added "3.0"}
  ([^String s]
   (set (keep +rlu+ s))))

(defn config:get
  "gets the config for notifications"
  {:added "3.0"}
  ([redis]
   (config:get redis nil))
  ([redis opts]
   ((comp events-parse f/strn second)
    (pool/pool:request-single redis ["CONFIG" "GET" "notify-keyspace-events"] opts))))

(defn config:set
  "sets the config for notifications"
  {:added "3.0"}
  ([redis events]
   (config:set redis events nil))
  ([redis events opts]
   (let [s    (events-string events)
         opts (cc/req:opts opts)]
     (pool/pool:request-single redis ["CONFIG" "SET" "notify-keyspace-events" (str "K" s)]
                               opts))))

(defn config:add
  "adds config notifications"
  {:added "3.0"}
  ([redis events]
   (config:add redis events nil))
  ([redis events opts]
   (do (config:set redis (apply conj (config:get redis opts) events) opts)
       (config:get redis opts))))

(defn config:remove
  "removes config notifications"
  {:added "3.0"}
  ([redis events]
   (config:remove redis events nil))
  ([redis events opts]
   (let [current (config:get redis)]
     (config:set redis (clojure.set/difference current (set events)))
     (config:get redis opts))))

(defn notify:args
  "produces the notify args"
  {:added "3.0"}
  ([namespace inputs]
   (mapv #(str "__keyspace@*:" (common/make-key namespace %))
         (collection/seqify inputs))))

(defn notify:wrap
  "wrapper for the notify delivery function"
  {:added "3.0"}
  ([handler {:keys [namespace] :as redis}]
   (fn [msg]
     (let [[type _ ^String path cmd :as msg] (wire/coerce msg :string)]
       (cond (= type "pmessage")
             (let [i   (.indexOf path ":")
                   key (subs path (inc i))]
               (env/explode
                (handler redis (common/unmake-key namespace key)))))))))

(action:add :notify
            {:subscribe   "PSUBSCRIBE"
             :unsubscribe "PUNSUBSCRIBE"
             :args notify:args
             :wrap notify:wrap})

(defn notify
  "notifications for a given client"
  {:added "3.0"}
  ([redis id pattern handler]
   (listener:add :notify redis id pattern handler {:pattern pattern})))

(defn unnotify
  "removes notifications for a given client"
  {:added "3.0"}
  ([redis id]
   (listener:remove :notify redis id)))

(defn has-notify
  "checks that a given notify listener is installed"
  {:added "3.0"}
  ([redis id]
   (boolean (get-in @(:runtime redis) [:listeners :notify id]))))

(defn list-notify
  "lists all notify listeners for a client"
  {:added "3.0"}
  ([redis]
   (collection/map-vals #(select-keys % [:pattern :handler])
               (get-in @(:runtime redis) [:listeners :notify]))))

(defn start:events-redis
  "creates action for `:events`"
  {:added "3.0"}
  ([redis events]
   (config:set redis events)
   redis))

(defn start:notify-redis
  "creates action for `:notify`"
  {:added "3.0"}
  ([redis listeners]
   (doseq [[id {:keys [pattern handler]}] listeners]
     (notify redis id pattern handler))
   redis))

(defn stop:notify-redis
  "stop action for `:notify` field"
  {:added "3.0"}
  ([redis listeners]
   (doseq [[id _] listeners]
     (unnotify redis id))
   redis))

(comment
  impl/*default*)
