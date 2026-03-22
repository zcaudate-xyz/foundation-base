(ns lib.minio
  (:require [lib.minio.bench :as bench]
            [std.lib.foundation]))

(std.lib.foundation/intern-in bench/start-minio-array
             bench/stop-minio-array
             bench/all-minio-ports)
