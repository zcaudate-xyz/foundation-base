(ns lib.redis
  (:require [lib.docker :as docker]
            [lib.redis.bench :as bench]
            [lib.redis.event :as event]
            [net.resp.connection :as conn]
            [net.resp.pool :as pool]
            [std.lib.component :as component]
            [std.lib.foundation :as f])
  (:import (hara.net.resp SocketConnection)))

(def ^:dynamic *default*
  {:host "localhost"
   :port 6379})

(defn client-steps
  "clients steps for start up and shutdown"
  {:added "3.0"}
  ([]
   [{:key :container
     :setup     docker/start-runtime
     :teardown  docker/stop-runtime}
    {:key :bench
     :setup     bench/bench-start
     :teardown  bench/bench-stop}
    {:key :events
     :start event/start:events-redis}
    {:key :notify
     :start event/start:notify-redis
     :stop  event/stop:notify-redis}]))


;;
;; Pool
;;


(def ^{:arglists '([conn])} client:stop  (component/wrap-stop  pool/pool:stop  (client-steps)))
(def ^{:arglists '([conn])} client:kill  (component/wrap-stop  pool/pool:kill  (client-steps)))

(defn client-string
  "creates a cliet string"
  {:added "4.0"}
  [{:keys [host port pool]}]
  (str "#rt.redis.client " (merge {:host host :port port}
                                   (component/info pool))))

(defn client-start
  "starts the client"
  {:added "4.0"}
  [{:keys [id host port] :as client}]
  (let [pool (pool/pool {:id id
                         :host host
                         :port port
                         :tag "redis.pool"
                         :path [:redis :pool]})]
    (pool/pool:start (assoc client :pool pool))))

(def ^{:arglists '([conn])} client:start (component/wrap-start client-start (client-steps)))

(defn client-create
  "creates a redis client"
  {:added "3.0"}
  ([{:keys [id env mode host port] :as m}]
   (let [id   (or id (f/sid))
         mode (or mode :eval)
         m    (merge m
                     (conn/test:config)
                     {:host host
                      :port port})]
     (merge m {:id id
               :tag :redis
               :mode mode
               :runtime (atom {:scripts {} :listeners {}})}))))
