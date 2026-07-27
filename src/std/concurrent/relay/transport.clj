(ns std.concurrent.relay.transport
  (:require [clojure.string]
            [std.concurrent.bus :as bus]
            [std.concurrent.queue :as q]
            [std.lib.atom :as atom]
            [std.lib.foundation :as f]
            [std.lib.time :as time]
            [std.protocol.component :as protocol.component])
  (:refer-clojure :exclude [send])
  (:import (java.io InputStream OutputStream InputStreamReader BufferedReader ByteArrayOutputStream)))

(defn bytes-output
  "creates a byte output stream"
  {:added "4.0"}
  (^ByteArrayOutputStream []
   (ByteArrayOutputStream.)))

(defn mock-input-stream
  "creates a mock input stream"
  {:added "4.0"}
  [out]
  (proxy [java.io.InputStream] []
    (available [] (count (first @out)))
    (readAllBytes []
      (atom/swap-return! out
        (fn [arr]
          [(.getBytes (clojure.string/join arr)) []])))
    (readNBytes [n]
      (atom/swap-return! out
        (fn [arr]
          (if (pos? (- (count (first arr)) n))
            [(.getBytes ^String (subs (first arr) 0 n))
             (vec (cons (subs (first arr) n)
                        (rest arr)))]
            [(.getBytes ^String (first arr)) (vec (rest arr))]))))))

(defn read-bytes-limit
  "reads a limited number of bytes from stream"
  {:added "4.0"}
  ([{:keys [^InputStream raw]} f f-timeout limit timeout interval]
   (let [start   (time/time-ms)
         output  (loop [line  ""
                        limit limit]
                   (cond (zero? limit)
                         (f line)
                         
                         (< (- (time/time-ms) start)
                            timeout)
                         (let [n (.available raw)]
                           (if (zero? n)
                             (do (Thread/sleep ^long interval)
                                 (recur line limit))
                             (let [needed  (min n limit)
                                   s (String. (.readNBytes raw needed))]
                               (recur (str line s) (- limit needed)))))

                         :else
                         (f-timeout line)))]
     {:start start
      :end (time/time-ns)})))

(defn read-bytes-line
  "reads individual lines from stream"
  {:added "4.0"}
  ([{:keys [^InputStream raw]} f f-timeout timeout interval]
   (let [start   (time/time-ms)
         output  (loop [line ""]
                   (cond (< (- (time/time-ms) start)
                            timeout)
                         (let [n (.available raw)]
                           (if (zero? n)
                             (do (Thread/sleep ^long interval)
                                 (recur line))
                             (let [s (String. (.readNBytes raw n))]
                               (if (clojure.string/ends-with? s "\n")
                                 (f (str line s))
                                 (recur (str line s))))))

                         :else
                         (f-timeout line)))]
     {:start start
      :end (time/time-ns)})))

(defn read-bytes-some
  "reads until timeout"
  {:added "4.0"}
  ([{:keys [^InputStream raw]} f timeout interval]
   (let [start   (time/time-ns)
         counter (f/counter 0)
         max     (inc (quot timeout interval))]
     (loop []
       (if (< @counter max)
         (let [n (.available raw)]
           (if (zero? n)
             (do (f/inc! counter)
                 (Thread/sleep ^long interval)
                 (recur))
             (do (f/reset! counter 0)
                 (f (.readNBytes raw n))
                 (recur))))))
     {:checks @counter
      :start start
      :end (time/time-ns)})))

;;
;;
;;

(defn op-count
  "outputs the count in the stream"
  {:added "4.0"}
  ([{:keys [^InputStream raw] :as istream}]
   (let [t (time/time-ns)]
     {:start t
      :end t
      :count (.available raw)})))

(defn op-clean
  "cleans the current input stream"
  {:added "4.0"}
  ([{:keys [^InputStream raw] :as istream}]
   (let [n (.available raw)
         _ (.readNBytes raw n)
         t (time/time-ns)]
     {:start t
      :end t
      :dropped n})))

(defn op-clean-some
  "cleans until timeout"
  {:added "4.0"}
  ([istream timeout interval]
   (let [counter (volatile! 0)
         count-fn (fn [^bytes bytes]
                    (vswap! counter + (count bytes)))
         stats (read-bytes-some istream count-fn timeout interval)]
     (assoc stats :dropped @counter))))

(defn op-read-all-bytes
  "reads all bytes from the stream"
  {:added "4.0"}
  ([{:keys [^InputStream raw] :as istream}]
   (let [start (time/time-ns)
         bytes (.readAllBytes raw)
         end   (time/time-ns)]
     {:output bytes :start start :end end})))

(defn op-read-all
  "reads all bytes from stream as string"
  {:added "4.0"}
  ([istream]
   (update (op-read-all-bytes istream)
           :output f/string)))

(defn op-read-some-bytes
  "read bytes from stream until timeout"
  {:added "4.0"}
  ([istream timeout interval]
   (let [baos (ByteArrayOutputStream.)
         save-fn (fn [^bytes bytes] (.writeBytes baos bytes))
         stats (read-bytes-some istream save-fn timeout interval)]
     (assoc stats :output (.toByteArray baos)))))

(defn op-read-some
  "read bytes from stream until timeout as string"
  {:added "4.0"}
  ([istream timeout interval]
   (update (op-read-some-bytes istream timeout interval)
           :output f/string)))

(defn op-read-line
  "read line from stream or until timeout"
  {:added "4.0"}
  ([istream timeout interval]
   (let [out  (volatile! nil)
         save-fn (fn [s] (vreset! out s))
         stats (read-bytes-line istream save-fn save-fn timeout interval)]
     (assoc stats :output @out))))

(defn op-read-limit
  "reads an limited amount of characters from stream or until timeout"
  {:added "4.0"}
  ([istream limit timeout interval]
   (let [out  (volatile! nil)
         save-fn (fn [s] (vreset! out s))
         stats (read-bytes-limit istream save-fn save-fn limit timeout interval)]
     (assoc stats :output @out))))

(defn process-by-line
  "process each line using a function"
  {:added "4.0"}
  ([{:keys [raw] :as istream} line-fn msg]
   (let [start (time/time-ns)]
     (with-open [reader (BufferedReader. (InputStreamReader. raw))]
       (try
         (loop [line (.readLine reader)]
           (when line
             (line-fn line msg)
             (recur (.readLine reader))))
         (catch java.io.IOException e)))
     {:start start :end (time/time-ns)})))

(defn process-by-handler
  "process the input with a function"
  {:added "4.0"}
  ([{:keys [raw] :as istream} handler msg]
   (let [start (time/time-ns)]
     (try
       (handler raw msg)
       (catch java.io.IOException e))
     {:start start :end (time/time-ns)})))

(def +read-alias+
  {:line    :read-line
   :string  :read-some
   :bytes   :read-some-bytes
   :all     :read-all})

(defn process-op
  "processes an op given a command"
  {:added "4.0"}
  ([istream op {:keys [timeout interval limit handler] :as msg}]
   (let [timeout  (or timeout  (:timeout  istream) 120)
         interval (or interval (:interval istream) 15)
         op    (or (+read-alias+ op)
                   op)
         status   (case op
                    :count        (op-count istream)
                    :clean        (op-clean istream)
                    :clean-some   (op-clean-some timeout interval)
                    :read-all     (op-read-all istream)
                    :read-all-bytes  (op-read-all-bytes istream)
                    :read-some    (op-read-some istream timeout interval)
                    :read-some-bytes (op-read-some-bytes istream timeout interval)
                    :read-limit   (op-read-limit istream (or limit 0) timeout interval)
                    :read-line    (op-read-line  istream timeout interval)
                    :custom-line  (process-by-line istream handler msg)
                    :custom       (process-by-handler istream handler msg))]
     status)))

(defn send-write-raw
  "sends a raw write command"
  {:added "4.0"}
  ([{:keys [raw] :as ostream} ^bytes bytes]
   (doto ^OutputStream raw
     (.write bytes))))

(defn send-write-flush
  "sends a raw flush command"
  {:added "4.0"}
  ([{:keys [raw] :as ostream}]
   (doto ^OutputStream raw
     (.flush))))

(defn send-write-line
  "sends a command with a newline and flush"
  {:added "4.0"}
  ([{:keys [raw] :as ostream} ^String line]
   (doto ^OutputStream raw
     (.write (.getBytes line))
     (.write (.getBytes "\n"))
     (.flush))))

(defn send-command
  "sends a command to output and then waits on the input"
  {:added "4.0"}
  ([istream
    ostream
    op
    ^String line
    {:keys [direct] :as msg}]
   (do (if line
         (send-write-line ostream line))
       (if (and (:bus istream)
                (not (false? direct)))
         (bus/bus:send (:bus istream) (:id istream) (dissoc msg :line))
         (process-op istream op (dissoc msg :line))))))
