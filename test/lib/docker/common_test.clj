(ns lib.docker.common-test
  (:require [lib.docker.common :refer :all]
            [lib.docker.ryuk :as ryuk])
  (:use code.test))

^{:refer lib.docker.common/raw-exec :guard true :added "4.0"}
(fact "executes a shell command"
  ^:hidden
  
  (raw-exec (concat ["docker" "ps"]
                    (when *host* ["--host" *host*])
                    ["--format" "{{json .}}"])
            {})
  => coll?)


^{:refer lib.docker.common/raw-command :added "4.0"}
(fact "executes a docker command"
  ^:hidden
  
  (raw-command ["ps"])
  => vector?)

^{:refer lib.docker.common/get-ip :added "4.0"
  :setup [(ryuk/start-ryuk)]}
(fact "gets the ip of a container"
  ^:hidden
  
  (get-ip (:container-id (ryuk/start-ryuk)))
  => string?)

^{:refer lib.docker.common/list-containers :added "4.0"}
(fact "gets all local containers"

  (list-containers)
  => vector?)

^{:refer lib.docker.common/has-container? :added "4.0"}
(fact "checks that a container exists"
  ^:hidden
  
  (has-container? (ryuk/start-ryuk))
  => true)

^{:refer lib.docker.common/start-container :added "4.0"}
(fact "starts a container")

^{:refer lib.docker.common/stop-container :added "4.0"}
(fact "stops a container")
