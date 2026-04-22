(ns kmi.queue.sorted-test
  (:require [rt.redis]
            [std.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :redis.client
   :config {:port 17003
            :bench true}
   :require [[kmi.redis :as r]
             [kmi.queue.common :as mq]
             [kmi.queue.sorted :as sorted]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(defn reset-z
  []
  (!.lua
   (r/call "FLUSHDB")
   [(r/call "ZADD" (mq/mq-path "test:set" "p1") 1 "a" 2 "b")
    (r/call "ZADD" (mq/mq-path "test:set" "p2") 3 "c" 4 "d")
    (r/call "ZADD" (mq/mq-path "test:set" "p3") 5 "e" 7 "f")]))

^{:refer kmi.queue.sorted/mq-sorted-queue-length :added "3.0"
  :setup [(reset-z)]}
(fact "returns the sorted queue length"

  (sorted/mq-sorted-queue-length "test:set"  "p1")
  => 2)

^{:refer kmi.queue.sorted/mq-sorted-queue-earliest :added "3.0"
  :setup [(reset-z)]}
(fact "returns the sorted queue earliest"

  (sorted/mq-sorted-queue-earliest "test:set"  "p1")
  => 1)

^{:refer kmi.queue.sorted/mq-sorted-queue-latest :added "3.0"
  :setup [(reset-z)]}
(fact "returns the sorted queue latest"

  (sorted/mq-sorted-queue-latest "test:set"  "p1")
  => 2)

^{:refer kmi.queue.sorted/mq-sorted-queue-items :added "3.0"
  :setup [(reset-z)]}
(fact "gets all items in the queue"

  (r/call "ZRANGEBYSCORE"
          (mq/mq-path "test:set" "p1")
          "0" "+inf" "WITHSCORES" "LIMIT" 0 100)
  => ["a" "1" "b" "2"]

  (mq/mq-index "p1-0" "p1" "-inf")

  (sorted/mq-sorted-queue-items  "test:set" "p1" "p1-0" "p1-100" 100)
  => [["p1-1" "a"] ["p1-2" "b"]])

^{:refer kmi.queue.sorted/mq-sorted-queue-get :added "3.0"}
(fact "gets the queue element"

  (sorted/mq-sorted-queue-get  "test:set" "p1" "p1-0")
  => "a")

^{:refer kmi.queue.sorted/mq-sorted-group-set-latest :added "3.0"
  :setup [(reset-z)]}
(fact "sets sorted groups to the latest"

  (sorted/mq-sorted-group-set-latest "test:set:_:p1"
                                  "test:set:_:p1:__group__"
                                  "default")
  => 2)

^{:refer kmi.queue.sorted/mq-sorted-group-init :added "3.0"
  :setup [(reset-z)]}
(fact "initialises the sorted groups"

  (sorted/mq-sorted-group-init "test:set" "p1" "default" "earliest")
  => 0

  (sorted/mq-sorted-group-init "test:set" "p2" "default" "latest")
  => 4)

^{:refer kmi.queue.sorted/mq-sorted-group-waiting :added "3.0"
  :setup [(reset-z)]}
(fact "returns number of waiting elements"

  (sorted/mq-sorted-group-waiting "test:set" "p1" "default")
  => 2)

^{:refer kmi.queue.sorted/mq-sorted-group-outdated :added "3.0"
  :setup [(reset-z)]}
(fact "returns if queue is outdated"

  (sorted/mq-sorted-group-outdated "test:set" "p1" "default")
  => true)

^{:refer kmi.queue.sorted/mq-sorted-queue-trim :added "3.0"
  :setup [(reset-z)]}
(fact "trims items in the queue"

  (sorted/mq-sorted-queue-trim "test:set" "p1" 0)
  => 2

  (!.lua
   (r/call "ZRANGE" "test:set:_:p1"  0 -1))
  => {})

^{:refer kmi.queue.sorted/mq-sorted-read :added "3.0"
  :setup [(reset-z)]}
(fact "reads items based on sorted queue"

  (sorted/mq-sorted-read "test:set" "p1" "default" "c00" 10)
  => [["p1-1" "a"] ["p1-2" "b"]])

^{:refer kmi.queue.sorted/mq-sorted-read-hold :added "3.0"
  :setup [(reset-z)]}
(fact "read hold based on sorted queue"

  (sorted/mq-sorted-read-hold "test:set" "p1" "default" "c00" 1 1)
  => [["p1-1" "a"]]

  (sorted/mq-sorted-read-hold "test:set" "p1" "default" "c00" 1 1)
  => "LOCKED")

^{:refer kmi.queue.sorted/mq-sorted-read-release :added "3.0"
  :setup [(reset-z)]}
(fact "read release based on sorted queue"

  (sorted/mq-sorted-read-release "test:set" "p1" "default" "c00" 1 1)
  => [["p1-1" "a"]]

  (sorted/mq-sorted-read-release "test:set" "p1" "default" "c00" 1 1)
  => [["p1-2" "b"]])

^{:refer kmi.queue.sorted/mq-sorted-queue-length-all :added "3.0"
  :setup [(reset-z)]}
(fact "returns lengths of all queues"

  (sort (sorted/mq-sorted-queue-length-all "test:set"))
  => [["p1" 2] ["p2" 2] ["p3" 2]])

^{:refer kmi.queue.sorted/mq-sorted-group-init-uninitialised :added "3.0"
  :setup [(reset-z)]}
(fact "initialises uninitialised partitions"

  (-> (sorted/mq-sorted-group-init-uninitialised "test:set" "default" "earliest")
      sort)
  => [["p1" 0] ["p2" 2] ["p3" 4]])

^{:refer kmi.queue.sorted/mq-sorted-group-init-all :added "3.0"
  :setup [(reset-z)]}
(fact "initialises all partitions"

  (-> (sorted/mq-sorted-group-init-all "test:set" "default" "current")
      sort)
  => [["p1" 2] ["p2" 4] ["p3" 7]]

  (-> (sorted/mq-sorted-group-init-all "test:set" "default" "earliest")
      sort)
  => [["p1" 0] ["p2" 2] ["p3" 4]])

^{:refer kmi.queue.sorted/mq-sorted-group-outdated-all :added "3.0"
  :setup [(reset-z)]}
(fact "lists all outdated queues"

  (-> (sorted/mq-sorted-group-outdated-all "test:set" "default")
      (sort))
  => ["p1" "p2" "p3"])

^{:refer kmi.queue.sorted/mq-sorted-group-waiting-all :added "3.0"
  :setup [(reset-z)]}
(fact "lists all waiting queues"

  (-> (sorted/mq-sorted-group-waiting-all "test:set" "default")
      (sort))
  => [["p1" 2] ["p2" 2] ["p3" 2]])

^{:refer kmi.queue.sorted/mq-sequential-read :added "3.0"
  :setup [(reset-z)]}
(fact "reads items based on sorted read"

  (sorted/mq-sorted-group-init "test:set" "p3" "default" "earliest")

  (sorted/mq-sequential-read "test:set" "p3" "default" "c00" 10)
  =>  [["p3-5" "e"]])

^{:refer kmi.queue.sorted/mq-sequential-read-hold :added "3.0"
  :setup [(reset-z)]}
(fact "read hold for sequential type")

^{:refer kmi.queue.sorted/mq-sequential-read-release :added "3.0"
  :setup [(reset-z)]}
(fact "read release for sequential type")
