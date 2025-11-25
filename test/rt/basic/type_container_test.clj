(ns rt.basic.type-container-test
  (:use code.test)
  (:require [rt.basic.type-container :refer :all]
            [std.lib :as h]
            [std.lang :as l]
            [lib.docker :as docker]))

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
