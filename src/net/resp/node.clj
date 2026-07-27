(ns net.resp.node
  (:require [net.resp.connection :as conn]
            [std.concurrent :as cc]
            [std.lib.atom :as atom]
            [std.lib.env :as env]
            [std.lib.foundation :as f])
  (:import (java.net ServerSocket) (hara.net.resp SocketConnection)))

(defonce ^:dynamic *active* (atom #{}))

(defn action-eval
  "creates result from an action 
 
   (action-eval nil [\"PING\"])
   => [:string \"PONG\"]
 
   (action-eval (fn [k args]
                  [:write (apply str args)])
                [\"WRITE\" 1 2 3])
   => [:write \"123\"]"
  {:added "3.0"}
  ([handler [cmd & args]]
   (let [action (case cmd
                  "PING"  [:string (f/string (or (first args) "PONG"))]
                  "ECHO"  [:write (first args)]
                  "THROW" [:throw (ex-info (f/string (first args) {}))]
                  (try (handler cmd args)
                       (catch Throwable t [:throw t])))]
     action)))

(defn action-write
  "writes an action to the connection
 
   (action-write |conn| [:write [\"PING\"]])
   => nil
 
   (wire/coerce (wire/read |conn|) :string)
   => \"PONG\""
  {:added "3.0"}
  ([conn [tag value]]
   (case tag
     :string (conn/connection:value conn value)
     :throw  (conn/connection:throw conn value)
     :write  (conn/connection:write conn value))))

(defn handle-multi
  "handles a call to start transaction
 
   (cc/req |conn| [\"MULTI\"])
   => \"OK\"
 
   (cc/req |conn| [\"PING\"])
   => \"QUEUED\"
 
   (cc/req |conn| [\"PING\"])
   => \"QUEUED\"
 
   (cc/req |conn| [\"EXEC\"])
   => [\"PONG\" \"PONG\"]"
  {:added "3.0"}
  ([conn transact]
   (let [handle-fn (fn [{:keys [enabled] :as m}]
                     (cond enabled
                           [[:throw (ex-info "Already in transaction" {})] m]

                           :else
                           [[:string "OK"] (assoc m :enabled true)]))
         action (atom/swap-return! transact handle-fn)]
     (action-write conn action))))

(defn handle-exec
  "handles a call to perform transaction"
  {:added "3.0"}
  ([conn transact handler]
   (let [handle-fn (fn [{:keys [enabled queue] :as m}]
                     (cond (not enabled)
                           [[[:throw (ex-info "Not in transaction" {})] []]
                            m]

                           :else
                           [[[:string "OK"] queue]
                            {:enabled false :queue []}]))
         [action queue] (atom/swap-return! transact handle-fn)
         outputs (mapv (comp second #(action-eval handler %)) queue)]
     (action-write conn [:write outputs]))))

(defn handle-command
  "handles a command call"
  {:added "3.0"}
  ([conn transact handler input]
   (let [handle-fn (fn [{:keys [enabled] :as m}]
                     (cond enabled
                           [false (update m :queue conj input)]

                           :else
                           [true m]))
         out? (atom/swap-return! transact handle-fn)]
     (if out?
       (action-write conn (action-eval handler input))
       (action-write conn [:string "QUEUED"])))))

(defn handle-single
  "performs a single call"
  {:added "3.0"}
  ([^SocketConnection conn transact handler]
   (handle-single conn transact handler true))
  ([^SocketConnection conn transact handler close]
   (try
     (if-let [[cmd & args :as input] (conn/connection:read conn)]
       (case (f/string cmd)
         "QUIT"  (System/exit 0)
         "EXIT"  (env/close conn)
         "MULTI" (handle-multi conn transact)
         "EXEC"  (handle-exec conn transact handler)
         (handle-command conn transact handler (cons (f/string cmd) args)))
       (env/close conn))
     (catch Throwable t
       (conn/connection:throw conn t))
     (finally (if close (env/close conn))))))

(defn handle-loop
  "performs a loop call"
  {:added "3.0"}
  ([^SocketConnection conn transact handler]
   (loop []
     (when-not (.isClosed (.-socket conn))
       (handle-single conn transact handler false)
       (recur)))))

(defn start-node
  "starts the remote node"
  {:added "3.0"}
  ([handler port]
   (start-node handler port {}))
  ([handler port {:keys [single] :as opts}]
   (let [server    (ServerSocket. port)
         executor  (cc/executor:cached)
         handle-fn (if single
                     handle-single
                     handle-loop)
         loop-fn   (fn []
                     (when (not (.isClosed server))
                       (try
                         (let [socket   (.accept server)
                               transact (atom {:enabled false :queue []})
                               conn     (SocketConnection. socket)]
                           (cc/submit executor (fn [] (handle-fn conn transact handler))))
                         (catch java.net.SocketException e))
                       (recur)))
         thread   (cc/thread {:start true
                              ;;:daemon true
                              :handler loop-fn})
         node {:server server
               :executor executor
               :thread thread}]
     (swap! *active* conj node)
     node)))

(defn stop-node
  "stops the remote node"
  {:added "3.0"}
  ([{:keys [thread server executor] :as node}]
   (swap! *active* disj node)
   (cc/thread:interrupt thread)
   (cc/executor:stop executor)
   (env/close server)
   (Thread/sleep 100)))

(comment

  (defn client-remote
    ([{:keys [host port]} command]
     (with-open [conn (conn/connection {:host host :port port})]
       (impl/exec conn command))))

  (def -s- (start-node (fn [& args]
                         (env/prn args))
                       4567))

  (def -t- (start-node (fn [& args]
                         (env/prn args))
                       4568))
  (stop-node -s-)
  (def -c- (conn/connection {:port 4568}))
  (def -f- (future (def -c- (conn/connection {:port 4567}))
                   (cc/req -c- ["PING"])))

  (impl/exec -c- ["ECHO" "ABC"])
  (impl/exec -c- ["THROW" "ABC"])
  (impl/exec -c- ["QUIT"])

  (client-call {:port 4567}
               ["THROW" "Generic Error"])
  (client-call {:port 4567}
               ["ECHO" "Generic Error"])
  (client-call {:port 4567}
               ["PING"])
  (client-call {:port 4567}
               ["QUIT"]))
