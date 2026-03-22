(ns rt.basic.server-basic
  (:require [std.concurrent :as cc]
            [std.json :as json]
            [std.lib.component]
            [std.lib.foundation]
            [std.lib.future]
            [std.lib.impl :refer [defimpl]]
            [std.lib.network])
  (:import (java.net Socket ServerSocket)))

(defonce ^:dynamic *env*
  (atom {}))

;;
;; Server
;;

(defn get-port
  "gets the port given lang and id"
  {:added "4.0"}
  ([{:keys [lang id]}]
   (get-port lang id *env*))
  ([lang id env]
   (:port (get-in @env [lang id]))))

(defn wait-ready
  "wait until server is ready"
  {:added "4.0"}
  ([{:keys [lang id]}]
   (wait-ready lang id))
  ([lang id]
   (let [ready (:ready (get-in @*env* [lang id]))]
     (if ready
       (deref ready 5000 {:status "timeout"})
       (std.lib.foundation/error "Env not found")))))

(defn- rt-server-string-props [{:keys [type lang id port count] :as server}]
  [type lang id port @count])

(defn- rt-server-string [{:keys [type lang id port count] :as server}]
  (str "#rt.server" (rt-server-string-props server)))

(defimpl RuntimeServer [type lang id port count ready state]
  :string rt-server-string)

(defn run-basic-server
  "runs a basic socket server"
  {:added "4.0"}
  ([{:keys [port]} state ready]
   (let [server    (ServerSocket. port)
         loop-fn   (fn []
                     (loop []
                       (when (not (.isClosed server))
                         (try
                           (let [socket   (.accept server)
                                 _        (if @state
                                            (std.lib.component/stop (:relay @state)))
                                 relay    (cc/relay {:type :socket
                                                     :port port
                                                     :attached socket})
                                 _  (reset! state {:relay relay
                                                   :socket socket})
                                 _  (deliver ready true)])
                           (catch java.net.SocketException e))
                         (recur)))
                     (swap! state (fn [{:keys [relay ^Socket socket]}]
                                    (if relay (std.lib.component/stop relay))
                                    (if socket (.close socket))
                                    nil)))
         thread   (cc/thread {:start true
                              :handler loop-fn})]
     {:instance server
      :thread thread})))

(def +encode+
  {:none  {:read  identity
           :write identity}
   :edn   {:read  read-string
           :write pr-str}
   :json  {:read  identity
           :write json/write}})

(defn get-encoding
  "gets the encoding to use
 
   (get-encoding :json)
   => map?"
  {:added "4.0"}
  [encode]
  (cond (nil? encode)
        (:json +encode+)

        (keyword? encode)
        (or (get +encode+ encode)
            (std.lib.foundation/error "Encoding not available" {:input encode}))

        (map? encode)
        (merge (:none +encode+)
               encode)

        :else (std.lib.foundation/error "Invalid input" {:input encode})))

(defn get-relay
  "gets the relay associated with the server
 
   (get-relay (start-server \"test\" :lua nil))
   => nil"
  {:added "4.0"}
  [{:keys [state] :as record}]
  (:relay @state))

(defn ping-relay
  "checks if the relay is still valid
 
   (ping-relay (start-server \"test\" :lua nil))
   => false"
  {:added "4.0"}
  [{:keys [state] :as record}]
  (if-let [relay (get-relay record)]
    (try (-> ^Socket (:attached relay)
             (.getOutputStream)
             (.write (.getBytes "<PING>\n")))
         true
         (catch Throwable t
           (std.lib.component/stop relay)
           (reset! state nil)
           false))
    false))

(defn raw-eval-basic-server
  "performs raw eval"
  {:added "4.0"}
  [{:keys [state encode] :as record} body & [timeout]]
  (let [disconnect-fn (fn []
                        (swap! state (fn [{:keys [relay ^Socket socket]}]
                                       (if relay (std.lib.component/stop relay))
                                       (if socket (.close socket))
                                       nil))
                        {:status "disconnected"})]
    (if-let [relay (get-relay record)]
      (try (let [{:keys [output]}  @(cc/send relay
                                             {:op :line
                                              :line ((:write encode) body)
                                              :timeout (or timeout 1000)})
                 _ (cc/send relay {:op :clean})]
             (if output
               ((:read encode) output)
               (do {:status "timeout"
                    :connected (ping-relay record)})))
           (catch com.fasterxml.jackson.databind.exc.MismatchedInputException e
             (disconnect-fn))
           (catch java.net.SocketException e
             (disconnect-fn)))
      {:status "not-connected"})))

(defn create-basic-server
  "creates a basic server
 
   (create-basic-server \"test\" :lua nil :json)
   => map?"
  {:added "4.0"}
  [id lang port encode]
  (let [encode   (get-encoding encode)
        port     (std.lib.network/port:check-available (or port 0))
        count    (atom 0)
        state    (atom nil)
        return   (atom {})
        ready    (promise)
        server   (run-basic-server {:port port}
                                   state
                                   ready)]
    (map->RuntimeServer
     {:type :server/basic
      :id     id
      :lang   lang
      :port   port
      :state  state
      :count  count
      :server server
      :encode encode
      :stop   (atom (fn []
                      (.close ^ServerSocket (:instance server))))
      :raw-eval raw-eval-basic-server
      :return return
      :ready ready})))

(defn start-server
  "start server function
 
   (start-server \"test\" :lua nil)
   => map?"
  {:added "4.0"}
  [id lang port & [f encode]]
  (let [f  (or f create-basic-server)
        record (or (get-in @*env* [lang id])
                   (get-in (swap! *env* assoc-in [lang id]
                                  (f id lang port encode))
                           [lang id]))
        {:keys [count]} record
        _  (swap! count inc)]
    record))

(defn get-server
  "gets a server given id
 
   (get-server \"test\" :lua)
   => map?"
  {:added "4.0"}
  [id lang]
  (get-in @*env* [lang id]))

(defn stop-server
  "stops a server
 
   (loop []
     (if (stop-server \"test\" :lua)
      (recur)))"
  {:added "4.0"}
  [id lang]
  (when-let [{:keys [count stop] :as record} (get-server id lang)]
    (when (not (pos? (swap! count dec)))
      (swap! *env* update lang dissoc id)
      (@stop))
    record))




(comment
  (./create-tests)
  (ping-relay
   (get-server "hello"
               :lua))
  
  (let [p (std.lib.future/future (raw-eval-basic-server (get-server "hello"
                                                       :lua)
                                          (pr-str '(+ 1 2 3 4))))]
    (let [msg  @(cc/send -c1- {:op :line})
          _    (def -line- (json/read (:output msg)))
          _    @(cc/send -c1- {:op :partial
                               :line (json/write (eval (read-string -line-)))})])
    @p)
  
  ()
  
  )


(comment
  (-> (get-relay (get-server "hello" :lua))
      :attached
      (.getOutputStream)
      (.write (.getBytes "1")))
  (.close (:instance (:server (get-server "hello" :lua))))
          
  (.isAlive (:thread (:server )))
  
  (stop-server "hello" :lua)
  
  
  (get)
  
  
  (start-basic-server "hello"
                      :lua
                      nil)
  
  (def -out- (atom []))

  (cc/relay:bus)
  
  (std.lib.component/stop -c2-)

  (std.lib.component/stop -c1-)
  
  (cc/bus:kill-all
   (cc/relay:bus)
   )
  (std.lib.foundation/error)
  
  ()

  (std.lib.component/start -c1-)
  
  [(do 
     (def -c1- (cc/relay {:type :socket
                          :port (:port (get-server "hello"
                                                   :lua))
                          
                          :options {:in  {:quiet true}}}))
     (std.lib.component/stop -c1-)
     (cc/relay:bus))
   (def -c2- (cc/relay {:type :socket
                        :port (:port (get-server "hello"
                                                 :lua))
                        
                        :options {:in  {:quiet true}}}))]
  
  (do (def -c1- (cc/relay {:type :socket
                           :port (:port (get-server "hello"
                                                    :lua))
                           
                           :options {:in  {:quiet true}}}))
      
      (std.lib.component/stop -c1-))
  
  @(cc/send -c1- {:op :string})
  
  (do (cc/send -c1- {:op :partial
                     :line (str "hello" (rand))})
      @(cc/send (:relay @(:state (get-server "hello"
                                             :lua)))
                {:op :line
                 :line (str "hello" (rand))
                 :timeout 100}))
  
  @(cc/send (:relay @(:state (get-server "hello"
                                         :lua)))
            {:op :string})
  
  (.available
   (.getInputStream (:socket  @(:instance
                                (:relay @(:state (get-server "hello"
                                                             :lua)))))))
  
  
  @(cc/send 
    (:relay @(:state (get-server "hello"
                                 :lua)))
    "hello")
  
  (std.lib.component/stop -c1-
          )
  ((dissoc -c1- :bus))
  
  )
