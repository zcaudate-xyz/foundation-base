(ns code.ai.server
  (:require [org.httpkit.server :as http]
            [std.concurrent.relay :as relay]
            [std.concurrent.bus :as bus]
            [std.lib :as h]
            [std.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [std.string :as str])
  (:import (org.httpkit.server AsyncChannel))
  (:refer-clojure :exclude [send]))

(defonce *server* (atom nil))
(defonce *relays* (atom {}))
(defonce channels (atom #{}))

(comment
  (reset! *relays* {}))
(defn get-channels [] @channels)

(defn send-to-clients [type data]
  (let [message (json/write {:type type :data data})]
    (doseq [^AsyncChannel channel (get-channels)]
      (http/send! channel message))))

(defn handle-relay-output [relay-id {:keys [line type]}]
  (send-to-clients :relay/output {:id relay-id :type type :line line}))

(defn create-relay-instance [command]
  (let [id  (h/sid)
        rly (relay/relay
             {:type :process
              :id id
              :command command
              :args ["bash" "-c" command]
              :options {:receive {:custom {:op :custom-line
                                           :handler (fn [line args]
                                                      (handle-relay-output id {:line line :type :stdout}))}}
                        :error   {:custom {:op :custom-line
                                           :handler (fn [line args]
                                                      (handle-relay-output id {:line line :type :stderr}))}}}})]
    (swap! *relays* assoc id rly)
    id))

(defn stop-relay-instance [id]
  (when-let [rly (get @*relays* id)]
    (h/stop rly)
    (swap! *relays* dissoc id)))

(defn get-relay-info [id]
  (when-let [rly (get @*relays* id)]
    (let [{:keys [id type command]} rly
          started? (h/started? rly)]
      {:id id :type type :command command :running started?} )))

(defn ws-handler [request]
  (http/with-channel request channel
    (println "WebSocket channel opened:" channel)
    (swap! channels conj channel)
    
    (http/on-close channel (fn [status]
                             (println "WebSocket channel closed:" channel "status:" status)
                             (swap! channels disj channel)))
    (http/on-receive channel (fn [data]
                               (println "Received WebSocket data:" data)
                               ;; For now, echo back or handle specific requests from frontend
                               (http/send! channel (str "Echo: " data))))))


(def resource-path "resources/public")

(defn serve-resource [uri]
  (let [file-path (str resource-path uri)
        file (io/file file-path)]
    (if (and (.exists file) (.isFile file))
      (let [content-type (cond
                           (str/ends-with? uri ".html") "text/html"
                           (str/ends-with? uri ".css") "text/css"
                           (str/ends-with? uri ".js") "application/javascript"
                           (str/ends-with? uri ".json") "application/json"
                           :else "application/octet-stream")]
        {:status 200
         :headers {"Content-Type" content-type}
         :body file})
      nil)))

(defn handler [request]
  (let [{:keys [uri request-method ^org.httpkit.BytesInputStream body]} request
        parsed-body (if body
                      (json/read
                       (String. (. body
                                   (readAllBytes))))
                      {})]
    (cond
      (= uri "/ws")
      (ws-handler request)

      ;; List all relays
      (and (= uri "/relays") (= request-method :get))
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/write (mapv get-relay-info (keys @*relays*)))}

      ;; Create a new relay
      (and (= uri "/relays") (= request-method :post))
      (let [
            _ (h/prn [request-method uri
                      parsed-body
                      request
                      (parsed-body "command")])
            command (parsed-body "command")
            id (create-relay-instance command)]
        {:status 201
         :headers {"Content-Type" "application/json"}
         :body (json/write (get-relay-info id))})
      
      ;; Get relay info
      (and (re-matches #"/relay/.*" uri) (= request-method :get))
      (let [id (second (std.string/split uri #"/"))]
        (if-let [info (get-relay-info id)]
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/write info)}
          {:status 404 :body "Relay not found"}))
      
      ;; Send input to a relay
      (and (re-matches #"/relay/(.*)/input" uri)
           (= request-method :post))
      (let [id (second (re-matches #"/relay/(.*)/input" uri))
            ^String input (parsed-body "input")
            _     (h/prn :input id input)]
        (if-let [rly (get @*relays* id)]
          (do (relay/send rly input)
              {:status 200 :body "Input sent"})
          {:status 404 :body "Relay not found"}))

      ;; Stop a relay
      (and (re-matches #"/relay/(.*)/stop" uri)
           (= request-method :post))
      (let [id (second (re-matches #"/relay/(.*)/stop" uri))
            _   (h/prn :stop id)]
        (if (get @*relays* id)
          (do (stop-relay-instance id)
              {:status 200 :body "Relay stopped"})
          {:status 404 :body "Relay not found"}))
      
      (= uri "/")
      (serve-resource "/index.html")

      (= request-method :get)
      (serve-resource uri)
      
      :else
      {:status 404 :body "Not Found"})))

(defn start-server
  "Starts the HTTP server"
  {:added "4.0"}
  ([port]
   (println "Starting HTTP server on port" port)
   (reset! *server* (http/run-server #'handler {:port port}))))

(defn stop-server
  "Stops the HTTP server"
  {:added "4.0"}
  ([]
   (when @*server*
     (println "Stopping HTTP server")
     (@*server* :timeout 100)
     (reset! *server* nil)
     (doseq [id (keys @*relays*)]
       (stop-relay-instance id)))))

(comment
  (std.concurrent.relay/send
   (second (first @*relays*))
   {:op :count})

  (std.concurrent.relay/send
   (second (first @*relays*))
   "print(1 + 1)")
  
  (std.concurrent.relay/send
   (second (nth (vec @*relays*) 2))
   "setInterval(() => { console.log(new Date()) }, 1000)")
  
  (h/map-vals  #'std.concurrent.relay/relay-started?
               @*relays*
               )
  
  (h/map-vals  std.concurrent.relay/send
               @*relays*
               )
  (re-matches #"/relay/(.*)/stop"
              "/relay/ureklejs23aw/stop")
  (start-server 8080)
  (stop-server)
  )
