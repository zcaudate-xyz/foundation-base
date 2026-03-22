(ns rt.basic.server-websocket
  (:require [org.httpkit.server :as server]
            [rt.basic.server-basic :as basic]
            [std.fs :as fs]
            [std.json :as json]
            [std.lang :as l]
            [std.lib.foundation :as f]
            [std.lib.network :as network]))

(def +pre-arranged+
  {:dev/web-main 29001})

(defn raw-eval-websocket-server
  "raw eval for websocket connection"
  {:added "4.0"}
  ([{:keys [id lang]} body & [timeout]]
   (let [{:keys [channel return]} (get-in @basic/*env* [lang id])]
     (cond (not @channel)
           {:status "not-connected"}
           
           :else 
           (let [msg-id  (str (f/uuid))
                 p   (promise)
                 _   (swap! return assoc msg-id p)
                 _   (server/send! @channel
                                   (json/write {:id msg-id :body body}))
                 output (deref p (or timeout 1000)
                               {:status "timeout"})]
             (if (= "ok" (:status output))
               (:body output)
               output))))))

(defn create-websocket-handler-receive
  "gets the websocket handler"
  {:added "4.0"}
  [msg return channel]
  (cond (= msg "ping")
        (server/send! channel "pong")

        :else
        (let [{:keys [id] :as data} (json/read msg json/+keyword-case-mapper+)]
          (when-let [p  (get @return id)]
            (deliver p data)
            (swap! return dissoc id)))))

(defn create-websocket-handler
  "creates the websocket handler"
  {:added "4.0"}
  [state return ready]
  (fn [request]
    (server/with-channel request channel
      (deliver ready true)
      (if @state (server/close @state))
      (reset! state channel)
      (server/on-close channel
                       (fn [status]
                         (if (= @state channel)
                           (reset! state nil))))
      (server/on-receive channel
                         (fn [msg]
                           (#'create-websocket-handler-receive msg return channel))))))

(defn create-websocket-server
  "creates the websocket server"
  {:added "4.0"}
  [id lang port _]
  (let [port     (network/port:check-available (or (+pre-arranged+ id)
                                             port
                                             0))
        count    (atom 0)
        channel  (atom nil)
        return   (atom {})
        stop     (atom nil)
        ready    (promise)
        handler  (create-websocket-handler channel return ready)
        port     (if (boolean? port) 0 port)
        stop-fn  (server/run-server handler {:port port})
        _ (reset! stop stop-fn)]
    (basic/map->RuntimeServer
     {:type :server/websocket
      :id   id
      :lang lang
      :port port
      :count count
      :channel channel
      :return return
      :stop stop
      :raw-eval raw-eval-websocket-server
      :ready ready})))
