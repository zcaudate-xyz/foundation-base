(ns kmi.queue.common-test
  (:require [rt.redis]
            [std.lang :as l]
            [std.lib.network :as network])
  (:use code.test))

(l/script- :lua
  {:runtime :redis.client
   :config {:port 17003
            :bench true}
   :require [[xt.lang.base-lib :as k :include [:fn]]
             [kmi.redis :as r]
             [kmi.queue.common :as mq]
             [kmi.queue.list :as list]]})

(fact:global
 {:setup  [(l/rt:restart)
           (network/wait-for-port "localhost" 17003
                            {:timeout 500})]
  :teardown [(l/rt:stop)]})

(defn reset-l
  []
  (!.lua
   (r/call "FLUSHDB")
   [(r/call "RPUSH" (mq/mq-path "test:list" "p1") 1 2)
    (r/call "RPUSH" (mq/mq-path "test:list" "p2") 3 4)
    (r/call "RPUSH" (mq/mq-path "test:list" "p3") 5 6)]))

^{:refer kmi.queue.common/mq-do-key :added "3.0"
  :setup [(reset-l)]}
(fact "helper function for multi key ops"

  (-> (!.lua
       (local f (fn [key p acc]
                  (table.insert acc p)))
       (mq/mq-do-key "test:list" f []))
      (sort))
  => '("p1" "p2" "p3"))

^{:refer kmi.queue.common/mq-map-key :added "3.0"
  :setup [(reset-l)]}
(fact "maps function for all partition keys"
  ^:hidden

  (-> (!.lua
       (mq/mq-map-key "test:list" k/identity))
      (sort))
  => '(["p1" "test:list"]
       ["p2" "test:list"]
       ["p3" "test:list"])

  (-> (!.lua
       (list/mq-list-read "test:list" "p1" "default" "default" 1)
       (mq/mq-map-key "test:list" list/mq-list-group-waiting "default"))
      (sort))
  => '(["p1" 1] ["p2" 2] ["p3" 2]))

^{:refer kmi.queue.common/mq-filter-key :added "3.0"
  :setup [(reset-l)]}
(fact "list filtered keys"
  ^:hidden

  (list/mq-list-read "test:list" "p1" "default" "default" 100)
  => ["0" ["1" "2"]]

  (-> (!.lua
       (mq/mq-filter-key "test:list"
                      list/mq-list-group-outdated
                      "default"))
      (sort))
  => ["p2" "p3"])

^{:refer kmi.queue.common/mq-select-key :added "3.0"
  :setup [(reset-l)]}
(fact "select keys for applying functions"
  ^:hidden

  (list/mq-list-read "test:list" "p1" "default" "default" 1)
  => ["0" ["1"]]

  (-> (!.lua
       (mq/mq-select-key "test:list"
                      list/mq-list-group-outdated
                      ["default"]
                      list/mq-list-read
                      ["default" "default" 100]))
      (sort))
  => [["p1" ["1" ["2"]]]
      ["p2" ["0" ["3" "4"]]]
      ["p3" ["0" ["5" "6"]]]])

^{:refer kmi.queue.common/mq-path :added "3.0"}
(fact "creates a mq key fragment"
  ^:hidden

  (mq/mq-path "test:list" "part" "__meta__")
  => "test:list:_:part:__meta__")

^{:refer kmi.queue.common/mq-index :added "3.0"}
(fact "converts id to an index"
  ^:hidden

  (mq/mq-index "p1-0" "p1" -1)
  => 0

  (mq/mq-index false "p1" "+inf")
  => "+inf")

^{:refer kmi.queue.common/mq-common-group-exists :added "3.0"}
(fact "check that a group exists"
  ^:hidden

  (mq/mq-common-group-exists "test:list" "A" "default")
  => false)

^{:refer kmi.queue.common/mq-common-group-not-exists :added "3.0"}
(fact "checks that group does not exist"
  ^:hidden

  (mq/mq-common-group-not-exists "test:list" "A" "default")
  => true)

^{:refer kmi.queue.common/mq-common-group-remove :added "3.0"}
(fact "removes a group"
  ^:hidden

  (mq/mq-common-group-remove "test:list" "A" "default")
  => number?)

^{:refer kmi.queue.common/mq-common-group-set-id :added "3.0"}
(fact "sets the group id on a partition"
  ^:hidden

  (do (mq/mq-common-group-set-id "test:list" "A" "default" "A-0")
      (mq/mq-common-group-get-id "test:list" "A" "default"))
  => "A-0")

^{:refer kmi.queue.common/mq-common-group-get-id :added "3.0"}
(fact "gets the group id on a partition")

^{:refer kmi.queue.common/mq-common-group-pending :added "3.0"
  :setup [(reset-l)]}
(fact "gets the group pending"
  ^:hidden

  (list/mq-list-read "test:list" "p1" "default" "default" 1)
  (mq/mq-common-group-pending "test:list" "p1" "default")
  => ["p1-0"])

^{:refer kmi.queue.common/mq-common-group-ack :added "3.0"
  :setup [(reset-l)]}
(fact "acknowledges the pending ids"
  ^:hidden

  (do (list/mq-list-read "test:list" "p1" "default" "default" 1)
      (mq/mq-common-group-ack "test:list" "p1" "default" ["p1-0"])
      (mq/mq-common-group-pending "test:list" "p1" "default"))
  => empty?)

^{:refer kmi.queue.common/mq-common-group-lock :added "3.0"
  :setup [(reset-l)]}
(fact "locks a queue for the group"
  ^:hidden

  (mq/mq-common-group-lock "test:list" "p1" "default" 1)

  (list/mq-list-read-hold "test:list" "p1" "default" "default" 1 1)
  => "LOCKED")

^{:refer kmi.queue.common/mq-common-group-unlock :added "3.0"
  :setup [(reset-l)]}
(fact "unlocks a queue for the group"
  ^:hidden

  (list/mq-list-read-hold "test:list" "p1" "default" "default" 1 1)
  => ["0" ["1"]]

  (mq/mq-common-group-unlock "test:list" "p1" "default")
  => 1

  (list/mq-list-read-hold "test:list" "p1" "default" "default" 1 1)
  =>  ["1" ["2"]])

^{:refer kmi.queue.common/mq-common-group-locked :added "3.0"
  :setup [(reset-l)]}
(fact "checks if the queue is locked"
  ^:hidden

  (list/mq-list-read-hold "test:list" "p1" "default" "default" 1 1)
  => ["0" ["1"]]

  (mq/mq-common-group-locked "test:list" "p1" "default")
  => true

  (do (mq/mq-common-group-unlock "test:list" "p1" "default")
      (mq/mq-common-group-locked "test:list" "p1" "default"))
  => false)

^{:refer kmi.queue.common/mq-common-group-unlocked :added "3.0"}
(fact "checks that the queue is unlocked")

^{:refer kmi.queue.common/mq-common-group-exists-all :added "3.0"
  :setup [(reset-l)]}
(fact "all partitions that the group exists in"
  ^:hidden

  (do (list/mq-list-read "test:list" "p1" "default" "default" 1)
      (mq/mq-common-group-exists-all "test:list" "default"))
  => ["p1"])

^{:refer kmi.queue.common/mq-common-group-not-exists-all :added "3.0"
  :setup [(reset-l)]}
(fact "all partitions that the group does not exists in"
  ^:hidden

  (do (list/mq-list-read "test:list" "p1" "default" "default" 1)
      (sort (mq/mq-common-group-not-exists-all "test:list" "default")))
  => ["p2" "p3"])

^{:refer kmi.queue.common/mq-common-group-locked-all :added "3.0"
  :setup [(reset-l)]}
(fact "checks for all locked partitions"
  ^:hidden

  (mq/mq-common-group-lock "test:list" "p1" "default" 1)

  (mq/mq-common-group-locked-all "test:list" "default")
  => ["p1"])

^{:refer kmi.queue.common/mq-common-group-unlocked-all :added "3.0"
  :setup [(reset-l)]}
(fact "checks for all unlocked partitions"
  ^:hidden

  (mq/mq-common-group-lock "test:list" "p1" "default" 1)

  (sort (mq/mq-common-group-unlocked-all "test:list" "default"))
  => ["p2" "p3"])

^{:refer kmi.queue.common/mq-common-group-remove-all :added "3.0"
  :setup [(reset-l)]}
(fact "removes all partitions"
  ^:hidden

  (do (list/mq-list-read "test:list" "p1" "default" "default" 1)
      (mq/mq-common-group-remove-all "test:list" "default"))
  => [["p1" 1]])

^{:refer kmi.queue.common/mq-common-group-pending-all :added "3.0"
  :setup [(reset-l)]}
(fact "checks for all partitions the group is pending"
  ^:hidden

  (do (list/mq-list-read "test:list" "p1" "default" "default" 1)
      (mq/mq-common-group-pending-all "test:list" "default"))
  =>  [["p1" ["p1-0"]]])

^{:refer kmi.queue.common/mq-common-queue-partitions :added "3.0"
  :setup [(reset-l)]}
(fact "returns all queue partitions"
  ^:hidden

  (sort (mq/mq-common-queue-partitions "test:list"))
  => ["p1" "p2" "p3"])

^{:refer kmi.queue.common/mq-common-queue-clear :added "3.0"
  :setup [(reset-l)]}
(fact "removes all partitions"
  ^:hidden

  (mq/mq-common-queue-clear "test:list")
  => (contains [3 (contains ["test:list:_:p3" "test:list:_:p1" "test:list:_:p2"]
                            :in-any-order)]))

^{:refer kmi.queue.common/mq-common-queue-purge :added "3.0"
  :setup [(reset-l)]}
(fact "removes all sub keys associated"
  ^:hidden

  (list/mq-list-read "test:list" "p1" "default" "default" 1)

  (mq/mq-common-queue-purge "test:list")
  => (contains [5
                (contains ["test:list:_:p1:__group__"
                           "test:list:_:p2"
                           "test:list:_:p1"
                           "test:list:_:p3"
                           "test:list:_:p1:__pending__:default"]
                          :in-any-order)]))

^{:refer kmi.queue.common/mq-read-expiry :added "3.0"
  :setup [(reset-l)]}
(fact "helper function for mq-read-hold and mq-read-release"
  ^:hidden

  (!.lua
   (mq/mq-read-expiry list/mq-list-read
                   "test:list" "p1" "default" "default" 1 5 false))
  => ["0" ["1"]]

  (!.lua
   (mq/mq-read-expiry list/mq-list-read
                   "test:list" "p1" "default" "OTHER" 1 5 true))
  => "LOCKED"

  (!.lua
   (mq/mq-read-expiry list/mq-list-read
                   "test:list" "p1" "default" "default" 1 5 false))
  => ["1" ["2"]])

^{:refer kmi.queue.common/mq-read-hold :added "3.0"
  :setup [(reset-l)]}
(fact "only read if the partition is not locked"
  ^:hidden

  (!.lua
   (mq/mq-read-hold list/mq-list-read
                 "test:list" "p1" "default" "default" 1 5))
  => ["0" ["1"]]

  (!.lua
   (mq/mq-read-hold list/mq-list-read
                 "test:list" "p1" "default" "OTHER" 1 5))
  => "LOCKED")

^{:refer kmi.queue.common/mq-read-release :added "3.0"
  :setup [(reset-l)]}
(fact "only reads and unlocks if stream is empty"
  ^:hidden

  (!.lua
   (mq/mq-read-release list/mq-list-read
                       "test:list" "p1" "default" "default" 2 5))
  => ["0" ["1" "2"]]

  (!.lua
   (mq/mq-read-release list/mq-list-read
                       "test:list" "p1" "default" "OTHER" 1 5))
  => nil)

^{:refer kmi.queue.common/mq-notify :added "4.0"}
(fact "notify __mq__ events"

  (mq/mq-notify "test:list" ["default"])
  => integer?)
