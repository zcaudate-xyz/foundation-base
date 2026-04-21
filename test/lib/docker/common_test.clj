(ns lib.docker.common-test
  (:require [lib.docker.common :refer :all]
            [lib.docker.ryuk :as ryuk]
            [std.lib.os :as os])
  (:use code.test))

^{:refer lib.docker.common/raw-exec :guard true :added "4.0"}
(fact "executes a shell command"

  (raw-exec (concat ["docker" "ps"]
                    (when *host* ["--host" *host*])
                    ["--format" "{{json .}}"])
            {})
  => coll?)


^{:refer lib.docker.common/raw-command :added "4.0"}
(fact "executes a docker command"

  (raw-command ["ps"])
  => vector?)

^{:refer lib.docker.common/get-ip :added "4.0"
  :setup [(ryuk/start-ryuk)]}
(fact "gets the ip of a container"

  (get-ip (:container-id (ryuk/start-ryuk)))
  => string?)

^{:refer lib.docker.common/list-containers :added "4.0"}
(fact "gets all local containers"

  (list-containers)
  => vector?)

^{:refer lib.docker.common/has-container? :added "4.0"}
(fact "checks that a container exists"

  (has-container? (ryuk/start-ryuk))
  => true)

^{:refer lib.docker.common/start-container :added "4.0"}
(fact "starts a container"
  (with-redefs [has-container? (constantly false)
                os/sh (fn [& _] (delay "cid-1\n"))
                get-ip (constantly "127.0.0.1")]
    (start-container {:id "hello"
                      :image "redis:latest"
                      :cmd ["redis-server"]}))
  => (contains {:container-id "cid-1\n"
                :container-ip "127.0.0.1"
                :container-name "testing_hello"}))

^{:refer lib.docker.common/stop-container :added "4.0"}
(fact "stops a container"
  (with-redefs [has-container? (constantly true)
                os/sh (fn [& _] (delay "killed"))]
    (stop-container {:id "hello"}))
  => "killed")
