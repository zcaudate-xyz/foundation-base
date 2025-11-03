(ns std.concurrent.relay
  (:require [std.protocol.component :as protocol.component]
            [std.concurrent.bus :as bus]
            [std.concurrent.queue :as q]
            [std.concurrent.relay.transport :as transport]
            [std.lib :as h :refer [defimpl]]
            [std.string :as str])
  (:refer-clojure :exclude [send])
  (:import (java.io InputStream
                    OutputStream
                    InputStreamReader
                    BufferedReader
                    ByteArrayOutputStream)))

(defonce ^:dynamic *bus* nil)

(def +init+
  (h/res:variant-add
   :hara/concurrent.bus
   {:id    :relay
    :alias :hara/relay
    :mode {:allow #{:global} :default :global}
    :instance {:setup    (fn [bus] (h/start bus) (h/set! *bus* bus) bus)
               :teardown (fn [bus] (h/set! *bus* nil) (h/stop bus) bus)}}))

(defn get-bus
  "gets the common stream bus"
  {:added "3.0"}
  []
  (or *bus*
      (h/res :hara/relay)))

(defmacro with:bus
  "sets the default relay bus"
  {:added "4.0"}
  [[bus] & body]
  `(binding [*bus* bus]
     ~@body))

(defn attach-read-passive
  "attaches a passive process to an input stream"
  {:added "4.0"}
  ([{:keys [raw return result final] :as istream} {:keys [op handler] :as message}]
   (h/future:run
    (bound-fn []
      (let [return    (h/explode (transport/process-op istream op message))
            _         (if final (h/future:force final return))]
        return)))))

(defn attach-interactive
  "attaches a bus process to an input stream"
  {:added "4.0"}
  ([istream]
   (attach-interactive istream nil))
  ([{:keys [bus] :as istream} id]
   (let [thread-handler (fn [{:keys [op] :as message}]
                          (transport/process-op istream op message))
         {:keys [thread]} @(bus/bus:open bus thread-handler {:id id})]
     thread)))

(defn- relay-stream-string
  [{:keys [type id raw bus history format]}]
  (str "#relay.stream" [type id (count history)]))

(defimpl RelayStream [type id raw bus history format]
  :string relay-stream-string)

(defn relay-stream
  "creates a relay stream"
  {:added "4.0"}
  [id type raw options]
  (let [bus      (get-bus)]
    (map->RelayStream
     (merge {:type type
             :id id
             :raw raw
             :bus bus
             :history (q/queue)
             :format  identity}
            options))))

(defn relay-stream?
  "checks if object is a relay stream"
  {:added "4.0"}
  [obj]
  (instance? RelayStream obj))

(defn- instance-map
  [id [iraw oraw] options & [m]]
  (let [istream  (relay-stream id :input  iraw (:receive options))
        ostream  (relay-stream id :output oraw (:send options))]
    (merge {:id  id
            :in  istream
            :out ostream
            :started (h/time-ns)}
           m)))

(defn make-socket-instance
  "creates a socket instance"
  {:added "4.0"}
  ([^java.net.Socket socket id options]
   (instance-map id [(.getInputStream socket)
                     (.getOutputStream socket)]
                 options
                 {:type :socket
                  :socket socket})))

(defn make-process-instance
  "creates a process instance"
  {:added "4.0"}
  ([^Process process id options]
   (let [estream    (relay-stream id :error
                                  (.getErrorStream process)
                                  (merge {:final (h/incomplete)}
                                         (:error options)))
         thread      (attach-read-passive estream (or (-> options :error :custom)
                                                      {:op :custom-line
                                                       :handler (fn [line estream]
                                                                  ;; look at saving to history, displaying, etc.
                                                                  (h/prn line))}))]
     (instance-map id [(.getInputStream process)
                       (.getOutputStream process)]
                   options
                   {:type :process
                    :process process
                    :err (assoc estream :thread thread)}))))

(defn make-instance
  "creates an instance"
  {:added "4.0"}
  ([obj]
   (make-instance obj (h/sid) {}))
  ([obj id options]
   (cond (instance? java.net.Socket obj)
         (make-socket-instance obj id options)

         (instance? Process obj)
         (make-process-instance obj id options)

         :else (h/error "Unsupported type: " 
                        {:require [java.net.Socket Process]}))))

(extend-protocol protocol.component/IComponent
  java.net.Socket
  (-stop [socket] (.close socket))
  (-kill [socket] (.close socket))

  Process
  (-stop [p] (.destroy p))
  (-kill [p] (.destroy p)))

;;
;;
;;

(defn- relay-started?
  ([{:keys [instance]}]
   (let [{:keys [type in ^Process process]} @instance
         {:keys [bus id]} in]
     (and bus
          (or (bus/bus:has-id? bus id)
              (and (= type :process)
                   (.isAlive process)))))))

(defn- relay-stopped?
  ([relay]
   (not (relay-started? relay))))

(defn relay-start
  "starts the relay"
  {:added "4.0"}
  ([{:keys [id type instance options hooks attached] :as relay}]
   (if (relay-started? relay)
     relay
     (let [obj (or attached
                   (case type
                     :socket  (h/socket (:host relay) (:port relay))
                     :process (h/sh (merge (select-keys relay [:root :env :args])
                                           {:wait false
                                            :inherit false
                                            :wrap false
                                            :output false}))))
           instance   (make-instance obj id options)
           _          (if-let [custom (get-in options [:receive :custom])]
                        (attach-read-passive (:in instance) (assoc custom
                                                                   :instance instance))
                        (attach-interactive (:in instance)
                                            (-> instance :in :id)))
           _     (reset! (:instance relay) instance)
           {:keys [setup]} hooks
           _   (if setup (setup relay))]
       relay))))

(defn relay-stop
  "stops the relay"
  {:added "4.0"}
  [{:keys [type hooks instance] :as relay}]
  (if (relay-stopped? relay)
    relay
    (let [{:keys [in type]} @instance
          {:keys [bus id]} in
          _  (and bus (h/explode (bus/bus:kill bus id)))
          _  (h/stop (get @instance type))
          _  (reset! instance nil)
          {:keys [teardown]} hooks
           _   (if teardown (teardown relay))]
      relay)))

(defn- relay-string [{:keys [type options instance]}]
  (str "#relay" [type (or options {}) @instance]))

(defimpl Relay [type options instance]           
  :string relay-string
  :prefix "relay-"                               
  :protocols [protocol.component/IComponent
              :exclude [-kill]
              protocol.component/IComponentQuery 
              :include [-started? -stopped?]])

(defn relay:create
  "creates a relay"
  {:added "4.0"}
  ([{:keys [id type] :as m}]
   (map->Relay (assoc m
                      :id (or id (h/sid))
                      :instance (atom nil)))))

(defn relay
  "creates and starts a relay"
  {:added "3.0"}
  ([{:keys [type] :as m}]
   (-> (relay:create m)
       (h/start))))

(def +read-ops+
  #{:count        
    :clean        
    :clean-some   
    :read-all     
    :read-all-bytes
    :read-some    
    :read-some-bytes
    :read-limit   
    :read-line    
    :custom-line  
    :custom})

(def +control-ops+
  #{:flush
    :raw
    :partial
    :exit
    :kill})

(defn send
  "sends command to relay"
  {:added "3.0"}
  ([{:keys [instance] :as relay} msg]
   (let [{:keys [in out]}  (or @instance
                               (h/error "Relay not started"
                                        {:relay relay :msg msg}))
         {:keys [op line]
          :as msg}    (if (string? msg)
                        {:op :read-some
                         :line msg}
                        msg)
         op  (or (transport/+read-alias+ op)
                 (+read-ops+ op)
                 (+control-ops+ op)
                 (h/error "Op not valid." {:input op
                                           :available (set (concat +control-ops+
                                                                   +read-ops+))
                                           :alias transport/+read-alias+}))]
     (case op
       :kill     (relay-stop relay)
       :exit     (do (.close ^OutputStream (:raw out))
                     (bus/bus:close (:bus in) (:id in)))
       :flush    (transport/send-write-flush out)
       :raw      (transport/send-write-raw out line)
       :partial  (transport/send-write-line out line)
       (transport/send-command in out op line msg)))))

