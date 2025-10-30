(ns std.lib.component
  (:require [std.protocol.component :as protocol.component]
            [std.lib.collection :as c]
            [std.lib.foundation :as h]
            [std.lib.component.track :as track]))

(def ^:dynamic *kill* false)

(defn impl:component
  "rewrite function compatible with std.lib.impl"
  {:added "3.0"}
  ([{:keys [name]}]
   (symbol "std.lib.component" (subs (clojure.core/name name) 1))))

(defn component?
  "checks if an instance extends IComponent
 
   (component? (Database.))
   => true"
  {:added "3.0"}
  ([obj]
   (h/suppress (extends? protocol.component/IComponent (type obj)))))

(extend-type nil
  protocol.component/IComponent
  (-start [this] this)
  (-stop  [this] this)
  (-kill  [this] this)
  (-info  [this level] {})
  (-remote? [this] false)
  (-health  [this] {:status :ok}))

(extend-type Object
  protocol.component/IComponent
  (-start [this] this)
  (-stop  [this] this)
  (-kill  [this] this)
  (-info  [this level] {})
  (-remote? [this] false)
  (-health  [this] {:status :ok})

  protocol.component/IComponentOptions
  (-get-options [this]
    (if (h/iobj? this)
      (-> (meta this)
          (c/qualified-keys :component)
          (c/unqualify-keys)))))

(defn primitive?
  "checks if a component is a primitive type
 
   (primitive? 1) => true
 
   (primitive? {}) => false"
  {:added "3.0"}
  ([x]
   (or (string? x)
       (number? x)
       (boolean? x)
       (h/regexp? x)
       (uuid? x)
       (uri? x)
       (h/url? x))))

(defn started?
  "checks if a component has been started
 
   (started? 1)
   => true
 
   (started? (start {}))
   => true
 
   (started? (Database.))
   => true"
  {:added "3.0"}
  ([component]
   (try (protocol.component/-started? component)
        (catch IllegalArgumentException e true)
        (catch AbstractMethodError e true))))

(defn stopped?
  "checks if a component has been stopped
 
   (stopped? 1)
   => false
 
   (stopped? {})
   => false
 
   (stopped? (start {}))
   => false
 
   (stopped? (Database.))
   => false"
  {:added "3.0"}
  ([component]
   (try (protocol.component/-stopped? component)
        (catch IllegalArgumentException e false)
        (catch AbstractMethodError e false))))

(defn perform-hooks
  "perform hooks before main function
 
   (perform-hooks (Database.)
                  {:init (fn [x] 1)}
                  [:init])
   => 1"
  {:added "3.0"}
  ([component functions hook-ks]
   (reduce (fn [out k]
             (let [func (or (get functions k)
                            identity)]
               (func out)))
           component
           hook-ks)))

(defn get-options
  "helper function for start and stop
 
   (get-options (Database.)
                {:init (fn [x] 1)})
   => (contains {:init fn?})"
  {:added "3.0"}
  ([component opts]
   (let [mopts (try (protocol.component/-get-options component)
                    (catch IllegalArgumentException e)
                    (catch AbstractMethodError e))]
     (merge mopts opts))))

(defn stop-raw
  "switch between stop and kill methods"
  {:added "3.0"}
  ([component]
   (if *kill*
     (try (protocol.component/-kill component)
          (catch IllegalArgumentException e
            (protocol.component/-stop component))
          (catch AbstractMethodError e
            (protocol.component/-stop component)))
     (protocol.component/-stop component))))

(defn start
  "starts a component/array/system
 
   (start (Database.))
   => {:status \"started\"}"
  {:added "3.0"}
  ([component]
   (start component {}))
  ([component opts]
   (let [{:keys [setup hooks functions]} (get-options component opts)
         {:keys [pre-start post-start]} hooks
         setup     (or setup identity)
         component   (-> component
                         (perform-hooks functions pre-start)
                         (protocol.component/-start)
                         (setup)
                         (perform-hooks functions post-start))
         component (track/track component)]
     component)))

(defn stop
  "stops a component/array/system
 
   (stop (start (Database.))) => {}"
  {:added "3.0"}
  ([component]
   (stop component {}))
  ([component opts]
   (let [component (track/untrack component)
         {:keys [teardown hooks functions]} (get-options component opts)
         {:keys [pre-stop post-stop]} hooks
         teardown  (or teardown identity)
         component (-> component
                       (perform-hooks functions pre-stop)
                       (teardown)
                       (stop-raw)
                       (perform-hooks functions post-stop))]
     component)))

(defn kill
  "kills a systems, or if method is not defined stops it
 
   (kill (start (Database.))) => {}"
  {:added "3.0"}
  ([component]
   (kill component {}))
  ([component opts]
   (binding [*kill* true]
     (stop component opts))))

(defn info
  "returns info regarding the component
 
   (info (Database.))
   => {:info true}"
  {:added "3.0"}
  ([component]
   (info component :default))
  ([component level]
   (try (protocol.component/-info component level)
        (catch IllegalArgumentException e {})
        (catch AbstractMethodError e {}))))

(defn health
  "returns the health of the component
 
   (health (Database.))
   => {:status :ok}"
  {:added "3.0"}
  ([component]
   (try (protocol.component/-health component)
        (catch AbstractMethodError e (throw e))
        (catch Throwable t {:status :errored}))))

(defn remote?
  "returns whether the component connects remotely
 
   (remote? (Database.))
   => false"
  {:added "3.0"}
  ([component]
   (try (protocol.component/-remote? component)
        (catch AbstractMethodError e false))))

(defn all-props
  "lists all props in the component
 
   (all-props (Database.))
   => [:interval]"
  {:added "3.0"}
  ([component]
   (try (keys (protocol.component/-props component))
        (catch AbstractMethodError e))))

(defn get-prop
  "gets a prop in the component
 
   (get-prop (Database.) :interval)
   => 10"
  {:added "3.0"}
  ([component k]
   (let [getter (-> (protocol.component/-props component)
                    (get-in [k :get]))]
     (getter))))

(defn set-prop
  "sets a prop in the component
 
   (set-prop (Database.) :interval 3)
   => (throws)"
  {:added "3.0"}
  ([component k value]
   (let [setter (-> (protocol.component/-props component)
                    (get-in [k :set]))]
     (setter value))))

(defmacro with
  "do tests with an active component
 
   (with [db (Database.)]
         (started? db))
   => true"
  {:added "3.0" :style/indent 1}
  ([[var expr & more] & body]
   `(let [~var  ~expr
          ~var  (start ~var)]
      (try
        ~(if (empty? more)
           `(do ~@body)
           `(with [~@more] ~@body))
        (finally (stop ~var))))))

(defn wrap-start
  "wraps the start function with more steps"
  {:added "4.0"}
  ([f steps]
   (fn [rt]
     (h/-> rt
           (reduce (fn [rt {:keys [key setup]}]
                     (if-let [data (get rt key)]
                       (setup rt data)
                       rt))
                   %
                   (filter :setup steps))
           (f)
           (reduce (fn [rt {:keys [key start]}]
                     (if-let [data (get rt key)]
                       (start rt data)
                       rt))
                   %
                   (filter :start steps))))))

(defn wrap-stop
  "wraps the stop function with more steps"
  {:added "4.0"}
  ([f steps]
   (fn [rt]
     (h/-> rt
           (reduce (fn [rt {:keys [key stop]}]
                     (if-let [data (get rt key)]
                       (stop rt data)
                       rt))
                   %
                   (filter :stop steps))
           (f)
           (reduce (fn [rt {:keys [key teardown]}]
                     (if-let [data (get rt key)]
                       (teardown rt data)
                       rt))
                   %
                   (filter :teardown steps))))))

(track/tracked:action:add :stop     stop
                          :start    start
                          :started? started?
                          :stopped? stopped?
                          :kill     kill
                          :info     info
                          :health   health
                          :remote?  remote?
                          :all-props all-props
                          :get-prop get-prop
                          :set-prop set-prop)

