(ns lib.minio.bench
  (:require [std.fs :as fs]
            [std.json :as json]
            [std.lang :as l]
            [std.lib.collection]
            [std.lib.env]
            [std.lib.foundation]
            [std.lib.future]
            [std.lib.impl :refer [defimpl]]
            [std.lib.network]
            [std.lib.os]
            [std.string.common]))

(def +bench-path+ "test-bench/minio")

(defonce ^:dynamic *active* (atom {}))

(defn all-minio-ports
  "gets all active minio ports"
  {:added "4.0"}
  ([]
   (->> (std.lib.os/sh "lsof" "-i" "-P" "-n" {:wrap false})
        (std.string.common/split-lines)
        (drop 1)
        (filter #(std.string.common/starts-with? % "minio"))
        (keep #(re-find #"^minio\s*(\d+).*\:(\d+) \(LISTEN\)$" %))
        (map (fn [arr]
               (mapv std.lib.foundation/parse-long (drop 1 arr))))
        (group-by second)
        (std.lib.collection/map-vals (comp set (partial map first))))))

(defn start-minio-server
  "starts the minio server in a given directory"
  {:added "4.0"}
  [{:keys [port console init]} type root-dir]
  (let [port (or port (std.lib.network/port:check-available 0))
        console-port (or console (std.lib.network/port:check-available 0))
        _ (fs/create-directory root-dir)]
    (-> (if (not (get @*active* port))
          (swap! *active*
                 (fn [m]
                   (let [process (std.lib.os/sh {:args [#_#_#_
                                               "MINIO_ROOT_USER=admin"
                                               "MINIO_ROOT_PASSWORD=password"
                                               "MINIO_BROWSER=off" 
                                               "minio" "server" "./data"
                                               "--address" (str ":" port)
                                               "--console-address" (str ":" console-port)]
                                        #_#_
                                        :env  {"MINIO_ROOT_USER" "admin"
                                               "MINIO_ROOT_PASSWORD" "password"
                                               "MINIO_BROWSER" "off"}
                                        :wait false
                                        :root root-dir})
                         thread  (-> (std.lib.future/future (std.lib.os/sh-wait process))
                                     (std.lib.future/on:complete (fn [_ _]
                                                      (let [out (std.lib.os/sh-output process)]
                                                        (when (not= 0 (:exit out))
                                                          (std.lib.env/prn out)))
                                                      (swap! *active* dissoc port))))]
                     (std.lib.network/wait-for-port "localhost" port
                                      {:timeout 3000})
                     (assoc m port {:type type
                                    :port port
                                    :root root-dir
                                    :process process
                                    :thread thread}))))
          @*active*)
        (get port))))

(defn stop-minio-server
  "stop the minio server"
  {:added "4.0"}
  [port stop-type]
  (let [{:keys [type process] :as entry} (get @*active* port)]
    (if (= type stop-type)
      (doto process
        (std.lib.os/sh-close)
        (std.lib.os/sh-exit)
        (std.lib.os/sh-wait)))
    entry))

(defn bench-start
  "starts the bench"
  {:added "4.0"}
  [{:keys [port] :as minio} type]
  (let [root-dir (case type
                   :scratch (str (fs/create-tmpdir))
                   (str +bench-path+ "/" port))
        entry  (start-minio-server minio type root-dir)]
    (assoc minio :port (:port entry))))

(defn bench-stop
  "stops the bench"
  {:added "4.0"}
  [{:keys [port bench] :as minio} _]
  (let [{:keys [type process]} (get @*active* port)]
    (stop-minio-server port bench)
    minio))

(defn start-minio-array
  "starts a minio array"
  {:added "4.0"}
  [ports]
  (mapv (fn [port]
          (let [m (if (number? port)
                    {:port port}
                    port)
                out (start-minio-server m :array (str +bench-path+ "/" (:port m)))
                _   (std.lib.network/wait-for-port "127.0.0.1" (:port m))]
            out))
        ports))

(defn stop-minio-array
  "stops a minio array"
  {:added "4.0"}
  [ports]
  (map (fn [port]
         (stop-minio-server port :array))
       ports))

