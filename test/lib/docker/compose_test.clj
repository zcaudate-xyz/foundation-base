(ns lib.docker.compose-test
  (:use code.test)
  (:require [lib.docker.compose :as compose]
            [std.lib :as h]))

(defn entry-redis-mq
  [{:keys [name image ip]}]
  {:compose   [[:image (or image "redis")]
               [:volumes [(format "./volumes/%s/data:/opt/redis/data"
                                  name)]]
               [:command ["redis-server" "--bind" "0.0.0.0" "--dir"
                          "/opt/redis/data" "--appendonly" "yes"]]
               [:healthcheck
                {:test ["CMD" "redis-cli" "ping"]
                 :interval "1m"}]]
   :export    {:APP_EV_HOST (or ip (h/strn name))
               :APP_EV_PORT 6379}})

(defn entry-minio
  [{:keys [name image ip]}]
  {:compose  [[:image (or image "minio/minio")]
              
              [:command ["minio" "server" "/data" "--console-address" ":9001"]]
              [:volumes [(format "./volumes/%s/data:/data"
                                 name)]]
              [:ports ["9001:9001"]]
              [:environment
               {:MINIO_ROOT_USER "minioadmin"
                :MINIO_ROOT_PASSWORD "minioadmin"
                :MINIO_DEFAULT_BUCKETS "image:public,static:public"}]
              [:healthcheck
               {:test ["CMD" "curl" "-f" (format "http://%s:9000/minio/health/live"
                                                 name)]
                :interval "1m"}]]
   :export   {:APP_MINIO_HOST  (or ip (h/strn name))
              :APP_MINIO_PORT  9000
              :APP_MINIO_USER  "minioadmin"
              :APP_MINIO_PASS  "minioadmin"
              :APP_MINIO_URL_IMAGE "image"}})

(defn entry-app-server
  [{:keys [name ip image]}]
  {:compose   [[:image  (or image "app-server")]
               [:depends_on ["minio" "redis-mq"]]
               [:healthcheck
                {:test ["CMD" "curl" "-f" (str "http://" name)]
                 :interval "1m"}]]
   :export    {:APP_WEB_HOST  (or ip (h/strn name))
               :APP_WEB_PORT  80
               :APP_WEB_SECURED  "no"}})

^{:refer lib.docker.compose/create-compose-single :guard true :added "4.0"}
(fact "executes a shell command"
  ^:hidden
  
  (compose/create-compose-single
   [[:image "postgres:14"]
    [:environment {:POSTGRES_USER "postgres",
                   :POSTGRES_PASSWORD "postgres",
                   :POSTGRES_DB "statstrade"}]
    [:volumes 
     ["./volumes/app-db/data:/var/lib/postgresql/data"]]
    [:healthcheck {:test ["CMD" "pg_isready" "-U" "postgres"], :interval "1m"}]]
   {:environment {:EV_HOST "172.1.0.20", :EV_PORT 6379},
    :depends-on ["redis"],
    :network :stats_dev_internal
    :ip-address "172.1.0.10"})
  => [[:image "postgres:14"]
      [:environment
       {:POSTGRES_USER "postgres",
        :POSTGRES_PASSWORD "postgres",
        :POSTGRES_DB "statstrade",
        :EV_HOST "172.1.0.20",
        :EV_PORT 6379}]
      [:volumes ["./volumes/app-db/data:/var/lib/postgresql/data"]]
      [:healthcheck
       {:test ["CMD" "pg_isready" "-U" "postgres"], :interval "1m"}]
      [:depends_on ["redis"]]
      [:networks {:stats_dev_internal {:ipv4_address "172.1.0.10"}}]])

^{:refer lib.docker.compose/create-compose :added "4.0"}
(fact "executes a docker command"
  ^:hidden
  
  (compose/create-compose
   {:config   {:minio          {:type :minio
                                :ip "172.1.0.10"}
               :redis-mq       {:type :redis.mq
                                :ip "172.1.0.20"}
               :app            {:type :app
                                :image "app-server-2"
                                :ip "172.1.0.50"
                                :ports {80 8080}
                                :deps [:redis-mq
                                       :minio]}}
    :entries  {:minio    #'entry-minio
               :redis.mq #'entry-redis-mq
               :app      #'entry-app-server}
    :network  :app-demo})
  => [[:minio
       [[:image "minio/minio"]
        [:command
         ["minio" "server" "/data" "--console-address" ":9001"]]
        [:volumes ["./volumes/minio/data:/data"]]
        [:ports ["9001:9001"]]
        [:environment
         {:MINIO_ROOT_USER "minioadmin",
          :MINIO_ROOT_PASSWORD "minioadmin",
          :MINIO_DEFAULT_BUCKETS "image:public,static:public"}]
        [:healthcheck
         {:test
          ["CMD" "curl" "-f" "http://minio:9000/minio/health/live"],
          :interval "1m"}]
        [:networks {:app-demo {:ipv4_address "172.1.0.10"}}]]]
      [:redis-mq
       [[:image "redis"]
        [:volumes ["./volumes/redis-mq/data:/opt/redis/data"]]
        [:command
         ["redis-server"
          "--bind"
          "0.0.0.0"
          "--dir"
          "/opt/redis/data"
          "--appendonly"
          "yes"]]
        [:healthcheck
         {:test ["CMD" "redis-cli" "ping"], :interval "1m"}]
        [:networks {:app-demo {:ipv4_address "172.1.0.20"}}]]]
      [:app
       [[:image "app-server-2"]
        [:depends_on ["redis-mq" "minio"]]
        [:healthcheck
         {:test ["CMD" "curl" "-f" "http://app"], :interval "1m"}]
        [:environment
         {:APP_EV_HOST "172.1.0.20",
          :APP_EV_PORT 6379,
          :APP_MINIO_HOST "172.1.0.10",
          :APP_MINIO_PORT 9000,
          :APP_MINIO_USER "minioadmin",
          :APP_MINIO_PASS "minioadmin",
          :APP_MINIO_URL_IMAGE "image"}]
        [:depends_on ["redis-mq" "minio"]]
        [:image "app-server-2"]
        [:ports "80:8080"]
        [:networks {:app-demo {:ipv4_address "172.1.0.50"}}]]]])


