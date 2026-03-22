(ns lib.minio.bench-test
  (:require [lib.minio.bench :refer :all])
  (:use code.test))

^{:refer lib.minio.bench/all-minio-ports :added "4.0"}
(fact "gets all active minio ports")

^{:refer lib.minio.bench/start-minio-server :added "4.0"}
(fact "starts the minio server in a given directory")

^{:refer lib.minio.bench/stop-minio-server :added "4.0"}
(fact "stop the minio server")

^{:refer lib.minio.bench/bench-start :added "4.0"}
(fact "starts the bench")

^{:refer lib.minio.bench/bench-stop :added "4.0"}
(fact "stops the bench")

^{:refer lib.minio.bench/start-minio-array :added "4.0"}
(fact "starts a minio array")

^{:refer lib.minio.bench/stop-minio-array :added "4.0"}
(fact "stops a minio array")
