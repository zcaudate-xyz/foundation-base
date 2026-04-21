(ns kmi.queue.list-test
  (:require [rt.redis]
            [std.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :redis.client
   :config {:port 17003
            :bench true}
   :require [[kmi.redis :as r]
             [kmi.queue.common :as mq]
             [kmi.queue.list :as list]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(defn reset-l
  []
  (!.lua
   (r/call "FLUSHDB")
   [(r/call "RPUSH" (mq/mq-path "test:list" "p1") 1 2)
    (r/call "RPUSH" (mq/mq-path "test:list" "p2") 3 4)
    (r/call "RPUSH" (mq/mq-path "test:list" "p3") 5 6)]))

^{:refer kmi.queue.list/mq-list-group-init-uninitialised.init :added "3.0"
  :setup [(reset-l)] :adopt true}
(fact "initiates uninitialised groups"

  (-> (list/mq-list-group-init-uninitialised "test:list" "default" "earliest")
      sort)
  =>  [["p1" 0] ["p2" 0] ["p3" 0]])

^{:refer kmi.queue.list/mq-list-group-set-latest :added "3.0"
  :setup [(reset-l)]}
(fact "sets the group to latest"

  (list/mq-list-group-set-latest "test:list:_:p1:__group__"
                                 "p1"
                                 "test:list:_:p1"
                                 "0")
  => 2)

^{:refer kmi.queue.list/mq-list-queue-params :added "3.0"
  :setup [(reset-l)]}
(fact "gets the list queue params"

  (!.lua
   [(list/mq-list-queue-params "test:list" "p1")])
  => ["test:list:_:p1"
      "test:list:_:p1:__offset__"
      0])

^{:refer kmi.queue.list/mq-list-group-init :added "3.0"
  :setup [(reset-l)]}
(fact "initiates the list queue"

  (list/mq-list-group-init "test:list" "p1" "default" "current")
  => 2

  (list/mq-list-group-init "test:list" "p1" "default" "earliest")
  => 0

  (list/mq-list-group-init "test:list" "p1" "default" "latest")
  => 2)

^{:refer kmi.queue.list/mq-list-group-waiting :added "3.0"
  :setup [(reset-l)]}
(fact "indicates if there are unprocessed entries"

  (list/mq-list-group-waiting "test:list" "p1" "default")
  => 2)

^{:refer kmi.queue.list/mq-list-group-outdated :added "3.0"
  :setup [(reset-l)]}
(fact "indicates if there are waiting entries"

  (list/mq-list-group-outdated "test:list" "p1" "default")
  => true)

^{:refer kmi.queue.list/mq-list-queue-length :added "3.0"
  :setup [(reset-l)]}
(fact "gets list queue length"

  (list/mq-list-queue-length "test:list" "p1")
  => 2)

^{:refer kmi.queue.list/mq-list-queue-earliest :added "3.0"
  :setup [(reset-l)]}
(fact "gets list queue earliest"

  (list/mq-list-queue-earliest "test:list" "p1")
  => 0)

^{:refer kmi.queue.list/mq-list-queue-latest :added "3.0"
  :setup [(reset-l)]}
(fact "gets list queue latest"

  (list/mq-list-queue-latest "test:list" "p1")
  => 2)

^{:refer kmi.queue.list/mq-list-queue-items :added "3.0"
  :setup [(reset-l)]}
(fact "grabs the list items"

  (list/mq-list-queue-items "test:list" "p1"
                         "p1-0" "p1-100" 100)
  => [0 ["1" "2"]])

^{:refer kmi.queue.list/mq-list-queue-get :added "3.0"
  :setup [(reset-l)]}
(fact "gets the first item in the list"

  (list/mq-list-queue-get "test:list" "p1" "p1-0")
  => "1")

^{:refer kmi.queue.list/mq-list-queue-trim :added "3.0"
  :setup [(reset-l)]}
(fact "trims the list to a given size"

  (!.lua
   (r/call "RPUSH" "test:list:_:p1" 3 4 5 6 7 8 9))

  (list/mq-list-queue-trim "test:list" "p1" 5)
  => [4 4]

  (!.lua
   (r/call "LRANGE" "test:list:_:p1" 0 -1))
  => ["5" "6" "7" "8" "9"])

^{:refer kmi.queue.list/mq-list-read :added "3.0"
  :setup [(reset-l)]}
(fact "read elements from a list"

  (list/mq-list-read "test:list" "p1" "default" "default" 1)
  => ["0" ["1"]])

^{:refer kmi.queue.list/mq-list-read-hold :added "3.0"
  :setup [(reset-l)]}
(fact "mq-read-hold on list"

  (list/mq-list-read-hold "test:list" "p1" "default" "default" 1 1)
  => ["0" ["1"]]

  (def -out- (list/mq-list-read-hold "test:list" "p1" "default" "default" 1 1))
  -out-
  => "LOCKED")

^{:refer kmi.queue.list/mq-list-read-release :added "3.0"
  :setup [(reset-l)]}
(fact "mq-read-release on list"

  (list/mq-list-read-hold "test:list" "p1" "default" "default" 1 1)
  => ["0" ["1"]]

  (list/mq-list-read-release "test:list" "p1" "default" "default" 1 1)
  => ["1" ["2"]]

  ;; Initially will be locked
  (def -s1- (list/mq-list-read-hold "test:list" "p1" "default" "default" 1 1))
  -s1-
  => "LOCKED"

  ;; Lock released if no elements available
  (def -s2- (list/mq-list-read-release "test:list" "p1" "default" "default" 1 1))
  (not -s2-) => true

  ;; Additional mq-read-holds have no locking
  (def -s3- (list/mq-list-read-hold "test:list" "p1" "default" "default" 1 1))
  (not -s3-) => true

  (def -s4- (list/mq-list-read-hold "test:list" "p1" "default" "default" 1 1))
  (not -s4-) => true)

^{:refer kmi.queue.list/mq-list-group-init-uninitialised :added "3.0"
  :setup [(reset-l)]}
(fact "initiates uninitialised groups"

  (-> (list/mq-list-group-init-uninitialised "test:list" "default")
      sort)
  => [["p1" 2] ["p2" 2] ["p3" 2]])

^{:refer kmi.queue.list/mq-list-queue-length-all :added "3.0"
  :setup [(reset-l)]}
(fact "returns all lengths in the queue"

  (sort (list/mq-list-queue-length-all "test:list"))
  => [["p1" 2] ["p2" 2] ["p3" 2]])

^{:refer kmi.queue.list/mq-list-group-init-all :added "3.0"
  :setup [(reset-l)]}
(fact "initiates all groups"

  (-> (list/mq-list-group-init-all "test:list" "default" "current")
      sort)
  => [["p1" 2] ["p2" 2] ["p3" 2]]

  (-> (list/mq-list-group-init-all "test:list" "default" "earliest")
      sort)
  =>  [["p1" 0] ["p2" 0] ["p3" 0]]

  (-> (list/mq-list-group-init-all "test:list" "default" "current")
      sort)
  =>  [["p1" 0] ["p2" 0] ["p3" 0]]

  (-> (list/mq-list-group-init-all "test:list" "default" "latest")
      sort)
  => [["p1" 2] ["p2" 2] ["p3" 2]])

^{:refer kmi.queue.list/mq-list-group-outdated-all :added "3.0"
  :setup [(reset-l)]}
(fact "returns all outdated partitions on a queue"

  (-> (list/mq-list-group-outdated-all "test:list" "default")
      sort)
  => ["p1" "p2" "p3"]

  (do (list/mq-list-group-init "test:list" "p1" "default" "latest")
      (-> (list/mq-list-group-outdated-all "test:list" "default")
          sort))
  => ["p2" "p3"])

^{:refer kmi.queue.list/mq-list-group-waiting-all :added "3.0"
  :setup [(reset-l)]}
(fact "returns all partitions having waiting items on a queue"

  (-> (list/mq-list-group-waiting-all "test:list" "default")
      sort)
  => [["p1" 2] ["p2" 2] ["p3" 2]]

  (do (list/mq-list-group-init "test:list" "p1" "default" "latest")
      (-> (list/mq-list-group-waiting-all "test:list" "default")
          sort))
  => [["p1" 0] ["p2" 2] ["p3" 2]])

^{:refer kmi.queue.list/mq-list-write :added "4.0"}
(fact "writes a given value to list"

  (list/mq-list-write "test:list" "p10" "hello")
  => integer?

  (list/mq-list-queue-items "test:list" "p10")
  => (contains-in
      [integer? ["hello"]]))
