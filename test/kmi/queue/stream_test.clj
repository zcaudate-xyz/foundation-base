(ns kmi.queue.stream-test
  (:use code.test)
  (:require [std.lang :as  l]
            [rt.redis]))

(l/script- :lua
  {:runtime :redis.client
   :config {:port 17003
            :bench true}
   :require [[xt.lang.base-lib :as k]
             [kmi.redis :as r]
             [kmi.queue.common :as mq]
             [kmi.queue.stream :as stream]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(defn reset-x
  []
  (!.lua
   (r/call "FLUSHDB")
   (r/call "XGROUP" "CREATE" "test:stream:_:p1"
           "default" "0" "MKSTREAM")
   [(r/call "XADD" (mq/mq-path "test:stream" "p1") "*" "id" 1)
    (r/call "XADD" (mq/mq-path "test:stream" "p1") "*" "id" 2)
    (r/call "XADD" (mq/mq-path "test:stream" "p2") "*" "id" 3)
    (r/call "XADD" (mq/mq-path "test:stream" "p2") "*" "id" 4)
    (r/call "XADD" (mq/mq-path "test:stream" "p3") "*" "id" 5)
    (r/call "XADD" (mq/mq-path "test:stream" "p3") "*" "id" 6)]))

^{:refer kmi.queue.stream/mq-stream-queue-length :added "3.0"
  :setup [(reset-x)]}
(fact "gets the length of queue"
  ^:hidden

  (stream/mq-stream-queue-length "test:stream" "p1")
  => integer?)

^{:refer kmi.queue.stream/mq-stream-queue-earliest :added "3.0"
  :setup [(reset-x)]}
(fact "gets the earliest id"
  ^:hidden

  (stream/mq-stream-queue-earliest "test:stream" "p1")
  => string?)

^{:refer kmi.queue.stream/mq-stream-queue-latest :added "3.0"
  :setup [(reset-x)]}
(fact "gets the latest id"
  ^:hidden

  (stream/mq-stream-queue-latest "test:stream" "p1")
  => string?)

^{:refer kmi.queue.stream/mq-stream-queue-items :added "3.0"
  :setup [(reset-x)]}
(fact "gets items in the queue"
  ^:hidden

  (map second (stream/mq-stream-queue-items "test:stream" "p1" "-" "+" 10))
  =>  [["id" "1"] ["id" "2"]])

^{:refer kmi.queue.stream/mq-stream-queue-revitems :added "4.0"}
(fact "gets rev items"

  (map second (stream/mq-stream-queue-revitems "test:stream" "p1" "+"  "-" 10))
  =>  [["id" "2"] ["id" "1"]])

^{:refer kmi.queue.stream/mq-stream-queue-get :added "3.0"}
(fact "gets an item in the queue"

  (stream/mq-stream-queue-get "test:stream" "p1"
                              (stream/mq-stream-queue-earliest "test:stream" "p1"))
  => ["id" "1"])

^{:refer kmi.queue.stream/mq-stream-queue-trim :added "3.0"}
(fact "trims the stream to a given size"

  (stream/mq-stream-queue-trim "test:stream" "p1"
                               10)
  => integer?)

^{:refer kmi.queue.stream/mq-stream-group-pending :added "3.0"
  :setup [(reset-x)]}
(fact "gets all pending ids in the queue"
  ^:hidden

  (stream/mq-stream-read "test:stream" "p1" "default" "c1" 1)
  (stream/mq-stream-read "test:stream" "p1" "default" "c2" 1)

  (count (stream/mq-stream-group-pending "test:stream" "p1" "default"))
  => 2)

^{:refer kmi.queue.stream/mq-stream-group-get-id :added "3.0"
  :setup [(reset-x)]}
(fact "gets the id for a group"
  ^:hidden

  (stream/mq-stream-group-get-id "test:stream" "p1" "default")
  => string?)

^{:refer kmi.queue.stream/mq-stream-group-set-id :added "3.0"
  :setup [(reset-x)]}
(fact "sets the id for a group"
  ^:hidden

  (stream/mq-stream-group-set-id "test:stream" "p1" "default" "0-0")
  => map?)

^{:refer kmi.queue.stream/mq-stream-group-ack :added "3.0"}
(fact "acknowledges ids for a group"

  (stream/mq-stream-group-ack "test:stream" "p1" "default"
                              ["0-0"]))

^{:refer kmi.queue.stream/mq-stream-group-exists :added "3.0"
  :setup [(reset-x)]}
(fact "checks that group exists for a partition"
  ^:hidden

  (stream/mq-stream-group-exists "test:stream" "p1" "default")
  => true

  (stream/mq-stream-group-exists "test:stream" "p2" "default")
  => false)

^{:refer kmi.queue.stream/mq-stream-group-not-exists :added "3.0"
  :setup [(reset-x)]}
(fact  "checks that group exists for a partition"
  ^:hidden

  (stream/mq-stream-group-not-exists "test:stream" "p1" "default")
  => false

  (stream/mq-stream-group-not-exists "test:stream" "p2" "default")
  => true)

^{:refer kmi.queue.stream/mq-stream-group-init :added "3.0"
  :setup [(reset-x)]}
(fact "initiates the stream group"
  ^:hidden

  (stream/mq-stream-group-init "test:stream" "p2" "default" "latest")
  => (contains
      ["name" "default" "consumers" 0 "pending" 0
       "last-delivered-id" string?])

  (stream/mq-stream-group-init "test:stream" "p2" "default" "earliest")
  => (contains-in
      ["name" "default" "consumers" 0 "pending" 0 "last-delivered-id" "0-0"]))

^{:refer kmi.queue.stream/mq-stream-group-waiting :added "3.0"
  :setup [(reset-x)]}
(fact "checks for waiting elements"
  ^:hidden

  (stream/mq-stream-group-waiting "test:stream" "p2" "default")
  => 2)

^{:refer kmi.queue.stream/mq-stream-group-outdated :added "3.0"
  :setup [(reset-x)]}
(fact "checks that new elements are available"
  ^:hidden

  (stream/mq-stream-group-outdated "test:stream" "p2" "default")
  => true)

^{:refer kmi.queue.stream/mq-stream-group-remove :added "3.0"
  :setup [(reset-x)]}
(fact "removes group from partition"
  ^:hidden

  (stream/mq-stream-group-remove "test:stream" "p1" "default")
  => 1)

^{:refer kmi.queue.stream/mq-stream-group-read :added "3.0"
  :setup [(reset-x)]}
(fact "reads from a group"
  ^:hidden

  (def -out- (stream/mq-stream-group-read "test:stream:_:p1" "default" "c00" 2))
  -out-
  => (contains-in
      [["test:stream:_:p1"
        [[string? ["id" "1"]]
         [string? ["id" "2"]]]]]))

^{:refer kmi.queue.stream/mq-stream-group-create :added "3.0"
  :setup [(reset-x)]}
(fact "creates a group"
  ^:hidden
  
  (def -out- (stream/mq-stream-group-create "test:stream" "g100" "$"))

  -out-
  => {"ok" "OK"})

^{:refer kmi.queue.stream/mq-stream-read-init :added "4.0"
  :setup [(reset-x)]}
(fact "reads from a stream"
  ^:hidden
  
  (def -out- (stream/mq-stream-read-init "test:stream" "p2" "default"
                                        {:count 2
                                         :consumer "c00"
                                         :mode "start"}))
  -out-
  => (contains-in
      [["test:stream:_:p2" [[string? ["id" "3"]]
                            [string? ["id" "4"]]]]]))

^{:refer kmi.queue.stream/mq-stream-read-raw :added "4.0"}
(fact "precursor to `read` and `read-last` methods")

^{:refer kmi.queue.stream/mq-stream-read :added "3.0"
  :setup [(reset-x)]}
(fact "reads and automatically creates a group"
  ^:hidden

  (stream/mq-stream-read "test:stream" "p2" "default" "c00" 2)
  => (just-in
      [[string? ["id" "3"]]
       [string? ["id" "4"]]])

  (stream/mq-stream-read "test:stream" "p2" "default" "c00" 2)
  => false)

^{:refer kmi.queue.stream/mq-stream-read-last :added "4.0"
  :setup [(reset-x)]}
(fact "reads and automatically creates a group, reading from the last added"
  ^:hidden
  
  (stream/mq-stream-read-last "test:stream" "p2" "g0" "c00" 2 {:mode "last"})
  => (just-in
      [[string? ["id" "4"]]])

  (stream/mq-stream-read-last "test:stream" "p2" "g0" "c00" 2)
  => false)

^{:refer kmi.queue.stream/mq-stream-read-hold :added "3.0"
  :setup [(reset-x)]}
(fact "reads and locks the queue"
  ^:hidden

  (stream/mq-stream-read-hold "test:stream" "p2" "default" "c00" 1 1)
  => (contains-in
      [[string? ["id" "3"]]])

  (stream/mq-stream-read-hold "test:stream" "p2" "default" "c00" 1 1)
  => "LOCKED")

^{:refer kmi.queue.stream/mq-stream-read-release :added "3.0"
  :setup [(reset-x)]}
(fact "reads and releases the queue if no elements"
  ^:hidden

  (stream/mq-stream-read-release "test:stream" "p2" "default" "c00" 1 1)
  => (contains-in
      [[string? ["id" "3"]]])

  (stream/mq-stream-read-release "test:stream" "p2" "default" "c00" 1 1)
  => (contains-in
      [[string? ["id" "4"]]]))

^{:refer kmi.queue.stream/mq-stream-queue-length-all :added "3.0"
  :setup [(reset-x)]}
(fact "gets the length of all queues"
  ^:hidden

  (sort (stream/mq-stream-queue-length-all "test:stream" "p2"))
  => [["p1" 2] ["p2" 2] ["p3" 2]])

^{:refer kmi.queue.stream/mq-stream-group-init-uninitialised :added "3.0"
  :setup [(reset-x)]}
(fact "initiates group for all uninitialised partitions"
  ^:hidden

  (->> (stream/mq-stream-group-init-uninitialised "test:stream" "default" "earliest")
       (map first)
       sort)
  =>  ["p2" "p3"])

^{:refer kmi.queue.stream/mq-stream-group-init-all :added "3.0"
  :setup [(reset-x)]}
(fact "initiates all groups for the stream"
  ^:hidden

  (->> (stream/mq-stream-group-init-all "test:stream" "default" "current")
       (map first)
       sort)
  =>  ["p1" "p2" "p3"])

^{:refer kmi.queue.stream/mq-stream-group-outdated-all :added "3.0"
  :setup [(reset-x)]}
(fact "returns all outdated queues"
  ^:hidden

  (-> (stream/mq-stream-group-outdated-all "test:stream" "default")
      sort)
  => ["p1" "p2" "p3"])

^{:refer kmi.queue.stream/mq-stream-group-waiting-all :added "3.0"
  :setup [(reset-x)]}
(fact "returns all waiting queues"
  ^:hidden

  (-> (stream/mq-stream-group-waiting-all "test:stream" "default")
      sort)
  =>  [["p1" 2] ["p2" 2] ["p3" 2]])

^{:refer kmi.queue.stream/mq-stream-group-remove-all :added "3.0"}
(fact "removes all groups from queue")

^{:refer kmi.queue.stream/mq-stream-group-exists-all :added "3.0"}
(fact "checks for existing group"
  ^:hidden

  (stream/mq-stream-group-exists-all "test:stream" "default")
  => ["p1"])

^{:refer kmi.queue.stream/mq-stream-group-not-exists-all :added "3.0"}
(fact "checks for groups that not exists"
  ^:hidden

  (stream/mq-stream-group-not-exists-all "test:stream" "default")
  => (contains ["p2" "p3"] :in-any-order))

^{:refer kmi.queue.stream/mq-stream-write :added "4.0"
  :setup [(stream/mq-stream-queue-trim "test:stream"
                                       "default"
                                       0)]}
(fact "writes entry to a stream"
  ^:hidden

  (!.lua
   (stream/mq-stream-write "test:stream"
                           "default"
                           (tab :a 1 :b 2)))
  => string?

  (set
   (partition 2
              (!.lua
               (stream/mq-stream-queue-get
                "test:stream"
                "default"
                (stream/mq-stream-queue-latest "test:stream"
                                               "default")))))
  => '#{("b" "2")
        ("a" "1")})

^{:refer kmi.queue.stream/mq-stream-broadcast-write :added "4.0"
  :setup [(stream/mq-stream-queue-trim "test:stream"
                                       "default"
                                       0)]}
(fact "similar to `mq-stream-stream` but writes a to a single broadcast key `_`"
  ^:hidden
  
  (!.lua
   (stream/mq-stream-broadcast-write "test:stream"
                                     "default"
                                     (cjson.encode "hello")))
  => string?
  
  (!.lua
   (stream/mq-stream-queue-get
    "test:stream"
    "default"
    (stream/mq-stream-queue-latest "test:stream"
                                   "default")))
  => ["_" "\"hello\""])

^{:refer kmi.queue.stream/mq-stream-broadcast-publish :added "4.0"}
(fact "calls publish on the broadcast channel with undecoded json array of partitions")

^{:refer kmi.queue.stream/mq-stream-broadcast-single :added "4.0"
  :setup [(stream/mq-stream-queue-trim "test:stream"
                                       "default"
                                       0)]}
(fact "writes to a single partition and publish"
  ^:hidden
  
  (!.lua
   (stream/mq-stream-broadcast-single "test:stream"
                                      "default"
                                      (cjson.encode "hello")))
  => string?)

^{:refer kmi.queue.stream/mq-stream-broadcast-multi :added "4.0"
  :setup [(r/flushdb)]}
(fact "writes to a multiple partitions with publish"
  ^:hidden

  (!.lua
   (k/json-decode
    (stream/mq-stream-broadcast-multi
     "test:stream"
     (cjson.encode [["p1" "hello1"]
                    ["p2" "hello2"]
                    ["p3" "hello3"]])
     (cjson.encode ["p1" "p3" "p2"]))))
  => vector?

  [(!.lua
    (stream/mq-stream-queue-get
     "test:stream"
     "p1"
     (stream/mq-stream-queue-latest "test:stream"
                                    "p1")))

   (!.lua
    (stream/mq-stream-queue-get
     "test:stream"
     "p2"
     (stream/mq-stream-queue-latest "test:stream"
                                    "p3")))

   (!.lua
    (stream/mq-stream-queue-get
     "test:stream"
     "p3"
     (stream/mq-stream-queue-latest "test:stream"
                                    "p3")))]
  => [["_" "\"hello1\""] ["_" "\"hello2\""] ["_" "\"hello3\""]])

(comment

  (stream/mq-stream-read-last
   "__USER__","00000000-0000-0000-0000-000000000000","USER_REDISC116FAFA-85A9-49B7-B3CD-9C8379085141","6B5EF34D-4B12-413F-8D43-C7267FA9FF0F",100)
  
  (stream/mq-stream-read-raw
   "__USER__",
   
   "6B5EF34D-4B12-413F-8D43-C7267FA9FF0F",100)

  (count (r/call "XREVRANGE"
                 "__USER__:_:00000000-0000-0000-0000-000000000000"
                 "+" "-" "COUNT" "2"))
  (-/mq-stream-group-create k-queue group
                            (k/first (:I (or (k/second k-last)))))
  
  )
