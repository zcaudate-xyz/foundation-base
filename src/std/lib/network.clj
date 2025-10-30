(ns std.lib.network
  (:require [std.lib.env :as env]
            [std.lib.foundation :as h]
            [std.lib.time :as time]
            [std.string.common :as str]
            [std.protocol.component :as protocol.component])
  (:import (java.net Socket
                     ServerSocket
                     ConnectException)))

(defn ^java.net.Inet4Address local-host
  "returns the current host
 
   (local-host)
   ;; #object[java.net.Inet4Address 0x4523dee \"chapterhouse.base.local/127.0.0.1\"]
   => java.net.Inet4Address"
  {:added "3.0"}
  ([]
   (java.net.InetAddress/getLocalHost)))

(defn local-ip
  "returns the current ip
 
   (local-ip)
   ;; \"127.0.0.1\"
   => string?"
  {:added "3.0"}
  ([]
   (.getHostAddress (local-host))))

(defn local-hostname
  "returns the current host name
 
   (local-hostname)
   ;; \"chapterhouse.base.local\"
   => string?"
  {:added "3.0"}
  ([]
   (.getHostName (local-host))))

(defn local-shortname
  "returns the current host short name
 
   (local-shortname)
   ;; \"chapterhouse\"
   => string?"
  {:added "3.0"}
  ([]
   (-> (local-hostname)
       (str/split #"\.")
       first)))

(defn ^Socket socket
  "creates a new socket
 
   (h/suppress
    (with-open [s ^java.net.Socket (socket 51311)] s))"
  {:added "3.0"}
  ([^long port]
   (socket nil port))
  ([^String host ^long port]
   (Socket. (or host "localhost") port)))

(defn socket:port
  "gets the remote socket port"
  {:added "3.0"}
  ([^Socket socket]
   (.getPort socket)))

(defn socket:local-port
  "gets the local socket port"
  {:added "3.0"}
  ([^Socket socket]
   (.getLocalPort socket)))

(defn socket:address
  "gets the remote socket address"
  {:added "3.0"}
  ([^Socket socket]
   (str (.getInetAddress socket))))

(defn socket:local-address
  "getst the local socket address"
  {:added "3.0"}
  ([^Socket socket]
   (str (.getLocalAddress socket))))

(defn port:check-available
  "check that port is available
 
   (port:check-available 51311)
   => anything"
  {:added "4.0"}
  ([port]
   (try
     (with-open [^ServerSocket s (ServerSocket. port)]
       (.setReuseAddress s true)
       (.getLocalPort s))
     (catch Throwable t
       false))))

(defn port:get-available
  "get first available port from a range
 
   (port:get-available [51312 51313])
   => number?"
  {:added "4.0"}
  ([ports]
   (reduce (fn [_ i]
             (if (port:check-available i) (reduced i)))
           nil ports)))

(defn wait-for-port
  "waits for a port to be ready
 
   (wait-for-port \"localhost\" 51313 {:timeout 1000})
   => (throws)"
  {:added "3.0"}
  ([host port]
   (wait-for-port host port {}))
  ([host port {:keys [timeout pause] :as opts}]
   (let [t0 (time/time-ms)]
     (loop [retries 0]
       (let [[retry? timeout?]
             (try (env/close (socket host port))
                  [false false]
                  (catch ConnectException _
                    (if (or (nil? timeout)
                            (< (time/elapsed-ms t0) timeout))
                      [true false]
                      [false true])))]
         (cond retry?
               (do (Thread/sleep (long (or pause 100)))
                   (recur (inc retries)))

               :else
               (let [data {:host host
                           :port port
                           :elapsed (time/elapsed-ms t0)
                           :retries retries}]
                 (if timeout?
                   (throw (ex-info "Timed out" data))
                   data))))))))

(extend-type Socket
  protocol.component/IComponent
  (-start         [s] s)
  (-stop          [s] (.close s))
  (-kill          [s] (.close s))
  protocol.component/IComponentQuery
  (-started?      [s] (.isClosed s))
  (-stopped?      [s] (not (.isClosed s)))
  (-remote?       [s] true))

