(ns my-trace-server
  (:require [org.httpkit.server :as server]
            [clojure.java.io :as io]))

(defn- trace-handler [request]
  (when (= (:uri request) "/trace")
    (let [body (slurp (:body request))]
      (println "--- TRACE RECEIVED ---")
      (println body)
      (println "----------------------\n")))
  {:status 200 :headers {"Content-Type" "text/plain"} :body "OK"})

(defonce server-atom (atom nil))

(defn start-server
  "Starts the trace message server on the specified port.
   Usage: (start-server) or (start-server {:port 8080})"
  [& [opts]]
  (let [port (or (:port opts) 8080)
        server (server/run-server trace-handler {:port port})]
    (reset! server-atom server)
    (println (format "Trace server started on port %d" port))
    server))

(defn stop-server
  "Stops the currently running trace message server."
  []
  (when-let [server @server-atom]
    (server) ; Stop the server
    (reset! server-atom nil)
    (println "Trace server stopped.")))

;; To run this server from your terminal:
;; lein exec -p src/my_trace_server.clj -e "(my-trace-server/start-server)"
