(ns lua.aws.debug-test
  (:require [lib.minio :as minio]
            [hara.lang :as l]
            [std.lib.env :as env])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :test-mode true
   :config {:program :resty}
   :require [[lua.core :as u]
             [lua.nginx :as n]
             [lua.nginx.http-client :as http]
             [lua.aws.s3 :as s3]
             [lua.aws.common :as common]
             [xt.lang.spec-base :as xt]]})

(defn- ci?
  []
  (boolean (System/getenv "CI")))

(fact:global
 {:skip     (or (ci?)
                (not (env/program-exists? "resty"))
                (not (env/program-exists? "minio")))
  :setup    [(l/rt:restart)
             (minio/start-minio-array [{:port 4489
                                        :console 4499}])
             (Thread/sleep 1000)]
  :teardown [(l/rt:stop)
             (minio/stop-minio-array [{:port 4489
                                        :console 4499}])]})

^{:refer lua.aws.debug/raw :added "4.0"}
(fact "auth request"
  (!.lua
   (var res (s3/s3-request {:method "GET"
                            :route "test"}
                          {:port 4489}
                          {}))
   (return res))
  => map?)
