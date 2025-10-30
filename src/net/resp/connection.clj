(ns net.resp.connection
  (:require [std.protocol.component :as protocol.component]
            [std.protocol.request :as protocol.request]
            [std.protocol.track :as protocol.track]
            [std.protocol.wire :as protocol.wire]
            [std.lib.component.track :as track]
            [std.lib :as h :refer [defimpl]]
            [net.resp.wire :as wire])
  (:import (hara.net.resp SocketConnection
                          SocketConnection$Pipeline)
           (java.net Socket))
  (:refer-clojure :exclude [read]))

(def ^:dynamic *close* nil)

(defn ^"[Ljava.lang.Object;" input-array
  "protects against wrong inputs
 
   (seq (input-array [1 2 3]))
   => '(\"1\" \"2\" \"3\")"
  {:added "3.0"}
  [command]
  (into-array Object (map (fn [e]
                            (if (or (string? e)
                                    (bytes? e))
                              e
                              (str e)))
                          command)))

;;
;; Connection Pipeline
;;

(defn ^SocketConnection$Pipeline pipeline
  "retrieves the connection pipeline"
  {:added "3.0"}
  ([^SocketConnection connection]
   (.pipeline connection)))

(defn pipeline?
  "checks if object is instance of pipeline"
  {:added "3.0"}
  ([obj]
   (instance? SocketConnection$Pipeline obj)))

(defn pipeline:read
  "reads from the pipeline"
  {:added "3.0"}
  ([^SocketConnection$Pipeline pipeline]
   (.read pipeline)))

(defn pipeline:write
  "sends a request tot the pipeline"
  {:added "3.0"}
  ([^SocketConnection$Pipeline pipeline input]
   (.call pipeline (input-array input))))

(h/extend-impl SocketConnection$Pipeline
               :prefix "pipeline:"
               :protocols [std.protocol.wire/IWire
                           :include [-read -write]])

;;
;; Connection
;;

(defn connection:read
  "reads from the connection"
  {:added "3.0"}
  ([^SocketConnection connection]
   (.read connection)))

(defn connection:write
  "writes to the connection"
  {:added "3.0"}
  ([^SocketConnection connection input]
   (cond (vector? input)
         (.write connection (input-array input))

         (bytes? input)
         (.write connection ^bytes input)

         (string? input)
         (.write connection (.getBytes ^String input))

         (integer? input)
         (.write connection ^long input)

         :else
         (throw (ex-info "Not Valid" {:input input})))))

(defn connection:value
  "writes a string value to the connection"
  {:added "3.0"}
  ([^SocketConnection connection ^String val]
   (.writeString connection val)))

(defn connection:throw
  "writes an exception to the connection"
  {:added "3.0"}
  ([^SocketConnection connection ^Throwable t]
   (.write connection t)))

(defn connection:close
  "closes the connection"
  {:added "3.0"}
  ([^SocketConnection connection]
   (clojure.core/doto connection
     (track/untrack)
     (-> (.-socket) (.close)))))

(defn connection:request-single
  "requests the connection command"
  {:added "3.0"}
  ([^SocketConnection connection command]
   (connection:request-single connection command nil))
  ([^SocketConnection connection command _]
   (let [out (.call connection (input-array command))]
     (if *close* (connection:close connection))
     out)))

(defn connection:process-single
  "processes output data"
  {:added "3.0"}
  ([_ data {:keys [format deserialize string]}]
   (let [format (cond deserialize
                      format

                      (not (false? string)) :string
                      :else :bytes)]
     (wire/coerce data format))))

(defn connection:request-bulk
  "sends a multi command to the connection"
  {:added "3.0"}
  ([^SocketConnection connection commands]
   (connection:request-bulk connection commands nil))
  ([^SocketConnection connection commands _]
   (let [out (h/-> (pipeline connection)
                   (reduce (fn [pl command]
                             (protocol.wire/-write pl command))
                           %
                           commands)
                   (protocol.wire/-read))]
     (if *close* (connection:close connection))
     out)))

(defn connection:process-bulk
  "processes the returned responses"
  {:added "3.0"}
  ([connection inputs outputs _]
   (mapv (partial connection:process-single connection)
         outputs
         (map meta inputs))))

(defn connection:transact-start
  "command to start transaction
 
   (connection:transact-start nil)
   => [\"MULTI\"]"
  {:added "3.0"}
  ([connection]
   ["MULTI"]))

(defn connection:transact-end
  "command to end transaction
 
   (connection:transact-end nil)
   => [\"EXEC\"]"
  {:added "3.0"}
  ([connection]
   ["EXEC"]))

(defn connection:transact-combine
  "not valid for rdp protocol"
  {:added "3.0"}
  ([_ data]
   (h/error  "Transaction cannot be combined")))

(defn connection:info
  "outputs connection info"
  {:added "3.0"}
  ([^SocketConnection connection]
   (let [socket (.-socket connection)]
     {:host [(h/socket:address socket)]
      :port [(h/socket:port socket)
             (h/socket:local-port socket)]})))

(defn connection:started?
  "checks that connection has started"
  {:added "3.0"}
  ([^SocketConnection connection]
   (let [^Socket socket (.-socket connection)]
     (and (.isConnected socket)
          (not (.isClosed socket))))))

(defn connection:stopped?
  "checks that connection has stopped"
  {:added "3.0"}
  ([^SocketConnection connection]
   (not (connection:started? connection))))

(defn connection:health
  "checks on the health of the connection
 
   (connection:health |conn|)
   => {:status :ok}"
  {:added "3.0"}
  ([^SocketConnection connection]
   (let [status (try
                  (-> (connection:request-single connection ["PING"])
                      (h/string)
                      (= "PONG"))
                  (catch Throwable t))]
     (if status
       {:status :ok}
       {:status :not-healthy}))))

(defn connection-string
  "returns the string"
  {:added "3.0"}
  [connection]
  (str "#redis.socket " (connection:info connection)))

(h/extend-impl SocketConnection
               :prefix "connection:"
               :string connection-string
               :protocols [std.protocol.wire/IWire
                           protocol.request/IRequest
                           protocol.request/IRequestTransact
                           protocol.component/IComponent
                           :include [-stop -started? -health]
                           :method {-stop connection:close
                                    -remote true}

                           protocol.track/ITrack
                           :body {-track-path [:redis :socket]}])

(defn connection?
  "checks if instance is type connection
 
   (connection? |conn|)
   => true"
  {:added "3.0"}
  ([obj]
   (instance? SocketConnection obj)))

(defn ^SocketConnection connection
  "creates a connection
 
   (def |c| (connection {:host \"localhost\"
                         :port 4456}))
   (connection? |c|)
   => true
 
   (connection:close |c|)"
  {:added "3.0"}
  ([{:keys [^String host ^long port]}]
   (let [socket (Socket. host port)
         connection (SocketConnection. socket)]
     (clojure.core/doto connection
       (track/track)))))

(def test:activate
  (fn []
    (require 'lib.docker)
    (eval '(lib.docker/start-reaped
            {:group "testing"
             :id "redis"
             :image "redis:6.0.9"
             :ports [6379]}))))

(def test:deactivate
  (fn []
    (require 'lib.docker)
    (eval '(lib.docker/stop-container {:group "testing" :id "redis"}))))

(defn test:config
  "creates a container and gets config
 
   (test:config)
   => map?"
  {:added "3.0"}
  ([]
   (let [#_#_m  (test:activate)]
     {:type :redis
      :host "127.0.0.1" #_(:container-ip m)
      :port 6379})))

(defn test:connection
  "creates a test connection
   
   (test:connection)
   => connection?"
  {:added "3.0"}
  ([]
   (test:connection nil))
  ([m]
   (connection (merge (test:config) m))))

(defmacro with-test:connection
  "creates an runs statements using a test connection
 
   (with-connection [conn {:port 17001}]
     (cc/bulk conn
             (fn []
               (cc/req conn [\"FLUSHDB\"])
               (cc/req conn [\"SET\" \"TEST:A\" 1])
               (cc/req conn [\"KEYS\" \"*\"]))
             {}))
   => [\"OK\" \"OK\" [\"TEST:A\"]]"
  {:added "3.0" :style/indent 1}
  ([conn & body]
   `(let [~conn (test:connection)]
      (try ~@body
           (finally (connection:close ~conn))))))

(defmacro with-connection
  "creates a temporary connection and runs code
 
   (with-connection [conn  {:port 17001}]
     (cc/bulk conn
             (fn []
               (cc/req conn [\"FLUSHDB\"])
               (cc/req conn [\"SET\" \"TEST:A\" 1])
               (cc/req conn [\"KEYS\" \"*\"]))
             {}))
   => [\"OK\" \"OK\" [\"TEST:A\"]]"
  {:added "3.0"}
  ([[conn opts] & body]
   `(let [~conn (connection ~opts)]
      (try ~@body
           (finally (connection:close ~conn))))))
