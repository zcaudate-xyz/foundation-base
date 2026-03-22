(ns rt.basic.type-container-test
  (:require [lib.docker :as docker]
            [rt.basic.type-container :refer :all]
            [std.lang :as l])
  (:use code.test))

^{:refer rt.basic.type-container/start-container-process :added "4.0"}
(fact "starts the container"
  (with-redefs [docker/start-runtime (fn [& _] {:id "test-container"})]
    (start-container-process :python
                             {:exec ["python" "-c"]
                              :bootstrap (fn [port opts] "1+1")}
                             0
                             {:host "localhost"}))
  => {:id "test-container"})

^{:refer rt.basic.type-container/start-container :added "4.0"}
(fact "starts a container"
  (with-redefs [docker/start-runtime (fn [& _] {:id "test-container"})]
    (start-container :python
                     {:program :python
                      :process {:exec ["python"]}
                      :exec ["python"]}
                     0
                     {:runtime :basic}))
  => {:id "test-container"})

^{:refer rt.basic.type-container/stop-container :added "4.0"}
(fact "stops a container"
  (with-redefs [docker/stop-container (fn [_] :stopped)]
    (stop-container {:id "test"}))
  => :stopped)
