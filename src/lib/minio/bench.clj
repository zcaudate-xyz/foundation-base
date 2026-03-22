(ns lib.minio.bench
  (:require [clojure.string]
            [std.fs :as fs]
            [std.json :as json]
            [std.lang :as l]
            [std.lib.collection :as collection]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.future :as future]
            [std.lib.network :as network]
            [std.lib.os :as os]))

(def +bench-path+ "test-bench/minio")

(defonce ^:dynamic *active* (atom {}))

(defn all-minio-ports
  "gets all active minio ports"
  {:added "4.0"}
  ([]
   (->> (os/sh "lsof" "-i" "-P" "-n" {:wrap false})
        (clojure.string/split-lines)
        (drop 1)
        (filter #(clojure.string/starts-with? % "minio"))
        (keep #(re-find #"^minio\s*(\d+).*\:(\d+) \(LISTEN\)$" %))
        (map (fn [arr]
               (mapv f/parse-long (drop 1 arr))))
        (group-by second)
        (collection/map-vals (comp set (partial map first))))))

(defn start-minio-server
  "starts the minio server in a given directory"
  {:added "4.0"}
  [{:keys [port console init]} type root-dir]
  (let [port (or port (network/port:check-available 0))
        console-port (or console (network/port:check-available 0))
        _ (fs/create-directory root-dir)]
    (-> (if (not (get @*active* port))
          (swap! *active*
                 (fn [m]
                   (let [process (os/sh {:args [#_#_#_
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
                         thread  (-> (future/future (os/sh-wait process))
                                     (future/on:complete (fn [_ _]
                                                      (let [out (os/sh-output process)]
                                                        (when (not= 0 (:exit out))
                                                          (env/prn out)))
                                                      (swap! *active* dissoc port))))]
                     (network/wait-for-port "localhost" port
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
        (os/sh-close)
        (os/sh-exit)
        (os/sh-wait)))
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
                _   (network/wait-for-port "127.0.0.1" (:port m))]
            out))
        ports))

(defn stop-minio-array
  "stops a minio array"
  {:added "4.0"}
  [ports]
  (map (fn [port]
         (stop-minio-server port :array))
       ports))

