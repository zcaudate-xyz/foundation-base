(ns lib.redis.bench
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
            [std.string.case]
            [std.string.common]))

(def +bench-path+ "test-bench/redis")

(defonce ^:dynamic *active* (atom {}))

(defn all-redis-ports
  "gets all active redis ports"
  {:added "4.0"}
  ([]
   (->> (std.lib.os/sh "lsof" "-i" "-P" "-n" {:wrap false})
        (std.string.common/split-lines)
        (drop 1)
        (filter #(std.string.common/starts-with? % "redis-"))
        (keep #(re-find #"^redis-\w+\s*(\d+).*\:(\d+) \(LISTEN\)$" %))
        (map (fn [arr]
               (mapv std.lib.foundation/parse-long (drop 1 arr))))
        (group-by second)
        (std.lib.collection/map-vals (comp set (partial map first))))))

(defn config-to-args
  "convert config map to args"
  {:added "4.0"}
  [m]
  (->> m
       (map (fn [[k v]]
              (str (std.string.case/snake-case (name k))
                   " "
                   (cond (true? v)
                         "yes"
                         
                         (false? v)
                         "no"
                         
                         :else (str v)))))
       (std.string.common/join "\n")))

(defn start-redis-server
  "starts the redis server in a given directory"
  {:added "4.0"}
  [{:keys [port init]} type root-dir]
  (let [port (or port (std.lib.network/port:check-available 0))
        redis-conf "redis.conf"
        _ (fs/create-directory root-dir)
        _ (if (or (not (fs/exists? (str root-dir "/" redis-conf)))
                  init)
            (spit (str root-dir "/" redis-conf)
                  (str "port " port
                       "\nprotected-mode no"
                       (if (map? init)
                         (str "\n" (config-to-args init))))))]
    (-> (if (not (get @*active* port))
          (swap! *active*
                 (fn [m]
                   (let [process (std.lib.os/sh
                                  {:args ["redis-server" (str "./" redis-conf)]
                                   :wait false
                                   :root root-dir})
                         thread  (-> (std.lib.future/future (std.lib.os/sh-wait process))
                                     (std.lib.future/on:complete (fn [_ _]
                                                      (try (let [out (std.lib.os/sh-output process)]
                                                             (when (not= 0 (:exit out))
                                                               (std.lib.env/prn out)))
                                                           (catch Throwable t))
                                                      (swap! *active* dissoc port))))]
                     (std.lib.network/wait-for-port "localhost" port
                                      {:timeout 1000})
                     (assoc m port {:type type
                                    :port port
                                    :root root-dir
                                    :process process
                                    :thread thread}))))
          @*active*)
        (get port))))

(defn stop-redis-server
  "stop the redis server"
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
  [{:keys [port] :as redis} type]
  (let [root-dir (case type
                   :scratch (str (fs/create-tmpdir))
                   (str +bench-path+ "/" port))
        entry  (start-redis-server redis type root-dir)]
    (assoc redis :port (:port entry))))

(defn bench-stop
  "stops the bench"
  {:added "4.0"}
  [{:keys [port bench] :as redis} _]
  (let [{:keys [type process]} (get @*active* port)]
    (stop-redis-server port bench)
    redis))

(defn start-redis-array
  "starts a redis array"
  {:added "4.0"}
  [ports]
  (mapv (fn [port]
          (let [m (if (number? port)
                    {:port port}
                    port)
                out (start-redis-server m :array (str +bench-path+ "/" (:port m)))]
            out))
        ports))

(defn stop-redis-array
  "stops a redis array"
  {:added "4.0"}
  [ports]
  (map (fn [port]
         (stop-redis-server port :array))
       ports))

