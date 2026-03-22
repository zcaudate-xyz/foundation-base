(ns std.lang.interface.type-notify
  (:require [clojure.set]
            [std.concurrent :as cc]
            [std.json :as json]
            [std.lang.base.impl :as impl]
            [std.lib.atom]
            [std.lib.collection]
            [std.lib.component]
            [std.lib.env]
            [std.lib.foundation]
            [std.lib.future]
            [std.lib.impl :refer [defimpl]]
            [std.lib.network]
            [std.lib.resource]
            [std.protocol.component :as component]
            [std.string.common])
  (:import (java.net ServerSocket Socket InetSocketAddress) (com.sun.net.httpserver HttpExchange HttpHandler HttpServer)))

(def ^:dynamic *notify-capture*
  (atom []))

(def ^:dynamic *notify-override* nil)

(defn has-sink?
  "checks that sink exists"
  {:added "4.0"}
  [{:keys [sinks]} id]
  (contains? @sinks id))

(defn get-sink
  "gets a sink from the notification app server"
  {:added "4.0"}
  [{:keys [sinks]} id]
  (std.lib.atom/swap-return! sinks
    (fn [m]
      (let [q (get m id)
            curr (or q (atom nil))]
        [curr (if q m (assoc m id curr))]))))

(defn clear-sink
  "clears a sink"
  {:added "4.0"}
  [{:keys [sinks]} id]
  (std.lib.atom/swap-return! sinks
    (fn [m]
      [(get m id) (dissoc m id)])))

(defn add-listener
  "adds a listener to the sink"
  {:added "4.0"}
  [notify id sink-key f]
  (let [sink (get-sink notify id)]
    (add-watch sink sink-key
               (fn [_ _ _ v]
                 (f (std.lib.collection/map-keys keyword v))))))

(defn remove-listener
  "removes a listener from the sink"
  {:added "4.0"}
  [notify id sink-key]
  (let [sink (get-sink notify id)]
    (remove-watch sink sink-key)))

;;
;;
;;

(defn get-oneshot-id
  "registers a oneshot id for the app server"
  {:added "4.0"}
  [{:keys [oneshots]}]
  (std.lib.atom/swap-return! oneshots
    (fn [set]
      (loop [id (str "oneshot/" (std.lib.foundation/sid))]
        (if (get set id)
          (recur (str "oneshot/" (std.lib.foundation/sid)))
          [id (conj set id)])))))

(defn remove-oneshot-id
  "removes a oneshot id"
  {:added "4.0"}
  [{:keys [oneshots]} id]
  (std.lib.atom/swap-return! oneshots
    (fn [set]
      [(get set id) (disj set id)])))

(defn clear-oneshot-sinks
  "clear all registered oneshot sinks"
  {:added "4.0"}
  [{:keys [oneshots sinks]}]
  (let [ids @oneshots]
    (swap! sinks (fn [m] (apply dissoc m ids)))
    (reset! oneshots #{})
    ids))

(defn process-print
  "processes `print` id option"
  {:added "4.0"}
  [msg]
  (let [{:strs [value key]} msg
        {:strs [line column namespace id data]} (second key)]
    (std.lib.env/p (str "\n" namespace (if id (str "/" id))
              (if (or line column)
                (str " (" line ":" column ")"))
              (if data (str " " data))))
    (std.lib.env/prf value true)))

(defn process-capture
  "processes `capture` id option"
  {:added "4.0"}
  [msg]
  (swap! *notify-capture* conj msg))

(defn process-message
  "processes a message recieved by the notification server"
  {:added "4.0"}
  [app input]
  (let [{:strs [id] :as msg} (json/read input)]
    (cond (= id "print")
          (process-print msg)

          (= id "capture")
          (process-capture msg)
          
          (and id
                 (or (not (std.string.common/starts-with? id  "oneshot/"))
                     (has-sink? app id)))
          (reset! (get-sink app id) msg))))

;;
;;
;;

(defn handle-notify-http
  "handler for http request"
  {:added "4.0"}
  [^HttpExchange tx app]
  (let [bytes (.readAllBytes (.getRequestBody tx))]
    (process-message app (String. bytes))))

(defn start-notify-http
  "starts http server"
  {:added "4.0"}
  ([{:keys [http-port
            sinks
            http-instance] :as app}]
   (when (not @http-instance)
     (let [server (doto (HttpServer/create
                         (InetSocketAddress. http-port)
                         0)
                    (.createContext
                     "/" (reify HttpHandler
                           (^void handle [_ ^HttpExchange tx]
                            (let [_ (#'handle-notify-http tx app)
                                  headers (.getResponseHeaders tx)
                                  _ (.set headers "Access-Control-Allow-Origin" "*")
                                  _ (.sendResponseHeaders tx 200 2)]
                              (doto (.getResponseBody tx)
                                (.write (.getBytes "OK"))
                                (.close))))))
                    (.setExecutor nil)
                    (.start))]
       (reset! http-instance server)))
   app))


(defn stop-notify-http
  "stops http server"
  {:added "4.0"}
  ([{:keys [http-instance] :as app}]
   (when-let [^HttpServer server @http-instance]
     (.stop server 0)
     (reset! http-instance nil ))
   app))

(defn handle-notify-socket
  "handler for socket request"
  {:added "4.0"}
  [^java.net.Socket socket app]
  #_(std.lib.env/prn :CONNECTED)
  (let [is  (.getInputStream socket)
        bytes (.readAllBytes is)
        _   (.close socket)]
    (process-message app (String. bytes))))

(defn start-notify-socket
  "starts socket server"
  {:added "4.0"}
  ([{:keys [socket-port 
            socket-instance
            sinks] :as app}]
   (when (not @socket-instance)
     (let [server    (ServerSocket. socket-port)
           loop-fn   (fn []
                       (loop []
                         (when (not (.isClosed server))
                           (try
                             (let [socket   (.accept server)
                                   _  (std.lib.future/future
                                        (#'handle-notify-socket socket app))])
                             (catch java.net.SocketException e))
                           (recur))))
           thread   (cc/thread {:start true
                                :handler loop-fn})]
       (reset! socket-instance {:server server
                                :thread thread})))
   app))

(defn stop-notify-socket
  "stops socket server"
  {:added "4.0"}
  ([{:keys [socket-instance] :as app}]
   (when-let [{:keys [server thread]} @socket-instance]
     (if server (.close ^ServerSocket server))
     (.interrupt ^Thread thread)
     (reset! socket-instance nil ))
   app))

(defn start-notify
  "starts both servers
 
   (notify/start-notify +server+)
   => map?"
  {:added "4.0"}
  [app]
  (start-notify-http app)
  (start-notify-socket app))

(defn stop-notify
  "stops both servers"
  {:added "4.0"}
  [app]
  (stop-notify-http app)
  (stop-notify-socket app))

(defn- notify-server-string [{:keys [http-port
                                     http-instance
                                     socket-port
                                     socket-instance
                                     oneshots
                                     sinks] :as notify}]
  (str "#notify.server" {:http   [http-port   (boolean @http-instance)]
                         :socket [socket-port (boolean @socket-instance)]
                         :sinks (clojure.set/difference (set (keys @sinks))
                                               @oneshots)
                         :oneshots (count @oneshots)}))

(defimpl NotifyServer [port sinks oneshots]
  :string notify-server-string
  :protocols [std.protocol.component/IComponent
              :suffix "-notify"
              :method {-kill stop-notify}])

(defn notify-server:create
  "creates notify serve"
  {:added "4.0"}
  [{:keys [http-port
           socket-port]}]
  (let [http-port   (or (std.lib.network/port:check-available (or http-port   18130))
                        (std.lib.network/port:check-available 0))
        socket-port (or (std.lib.network/port:check-available (or socket-port 18131))
                        (std.lib.network/port:check-available 0))]
    (map->NotifyServer {:http-port http-port
                        :http-instance (atom nil)
                        :socket-port socket-port
                        :socket-instance (atom nil)
                        :sinks   (atom {})
                        :oneshots (atom #{})})))

(defn notify-server
  "create and start notify server"
  {:added "4.0"}
  [{:keys [port] :as m}]
  (-> (notify-server:create m)
      (std.lib.component/start)))

(std.lib.resource/res:spec-add
 {:type :hara/lang.notify
  :mode {:allow #{:global}
         :default :global}
  :instance {:create #'notify-server:create
             :start std.lib.component/start
             :stop std.lib.component/stop}})

(defn default-notify
  "gets the default notify server
 
   (notify/default-notify)"
  {:added "4.0"}
  []
  (or *notify-override* 
      (std.lib.resource/res :hara/lang.notify)))

(defn default-notify:reset
  "resets the default notify server"
  {:added "4.0"}
  []
  (std.lib.resource/res:stop :hara/lang.notify))

(defn watch-oneshot
  "returns a completable future
 
   (notify/watch-oneshot +server+
                         10)
   => (contains [string? h/future?])"
  {:added "4.0"}
  [app timeout & [id]]
  (let [id (or id (get-oneshot-id app))
        q   (get-sink app id)
        pi  (std.lib.future/incomplete)
        p   (-> pi 
                (std.lib.future/future:timeout timeout)
                (std.lib.future/on:complete (fn [ret _]
                                 (remove-watch q id)
                                 (remove-oneshot-id app id)
                                 (clear-sink app id)
                                 ret)))
        _  (add-watch q id (fn [_ _ _ v]
                             (std.lib.future/future:force pi v)))]
    [id p]))
