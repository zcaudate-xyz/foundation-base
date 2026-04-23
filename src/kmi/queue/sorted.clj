(ns kmi.queue.sorted
  (:require [std.lang :as l]))

(l/script :lua
  {:runtime :redis
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
              [kmi.redis :as r]
              [kmi.queue.common :as q]]
   :static {:lang/lint-globals #{redis}}})

;;
;; SORTED
;;

(defn.lua ^{:rt/redis {}} mq-sorted-queue-length
  "returns the sorted queue length"
  {:added "3.0"}
  ([key partition]
   (local k-queue (q/mq-path key partition))
   (return (r/call "ZCARD" k-queue))))

(defn.lua ^{:rt/redis {}} mq-sorted-queue-earliest
  "returns the sorted queue earliest"
  {:added "3.0"}
  ([key partition]
   (local k-queue (q/mq-path key partition))
   (return (-> (r/call "ZRANGEBYSCORE" k-queue
                       "-inf" "+inf" "WITHSCORES" "LIMIT" 0 1)
               (. [2])
               (tonumber)))))

(defn.lua ^{:rt/redis {}} mq-sorted-queue-latest
  "returns the sorted queue latest"
  {:added "3.0"}
  ([key partition]
   (local k-queue (q/mq-path key partition))
   (return (-> (r/call "ZREVRANGEBYSCORE"
                       k-queue "+inf" "-inf" "WITHSCORES" "LIMIT" 0 1)
               (. [2])
               (tonumber)))))

(defn.lua  ^{:rt/redis {}} mq-sorted-queue-items
  "gets all items in the queue"
  {:added "3.0"}
  ([key partition start finish count]
   (local k-queue (q/mq-path key partition))
   (local '[idx-0 idx-1] '[(q/mq-index start partition "-inf")
                           (q/mq-index finish partition "+inf")])
   (return (-> (r/call "ZRANGEBYSCORE"
                       k-queue idx-0 idx-1 "WITHSCORES" "LIMIT" 0 count)
               (xtd/from-flat (fn [out k v]
 		               (table.insert out [(cat partition "-" v) k])
 			       (return out))
                             [])))))

(defn.lua ^{:rt/redis {}} mq-sorted-queue-get
  "gets the queue element"
  {:added "3.0"}
  ([key partition id]
   (local k-queue (q/mq-path key partition))
   (local idx (q/mq-index id partition "-inf"))
   (return (. (r/call "ZRANGEBYSCORE" k-queue idx "+inf" "WITHSCORES" "LIMIT" 0 1)
              [1]))))

(defn.lua ^{:rt/redis {}} mq-sorted-group-set-latest
  "sets sorted groups to the latest"
  {:added "3.0"}
  ([k-queue k-group group]
   (local items (r/call "ZREVRANGEBYSCORE" k-queue, "+inf", "-inf",
                        "WITHSCORES", "LIMIT", 0, 1))
   (local ret (or (. items [2]) -1))
   (r/call "HSET" k-group group ret)
   (return (tonumber ret))))

(defn.lua ^{:rt/redis {}} mq-sorted-group-init
  "initialises the sorted groups"
  {:added "3.0"}
  ([key partition group status]
   (local k-queue (q/mq-path key partition))
   (local k-group (cat k-queue ":" q/K_GROUP))

   (local ret)
   (cond (== status "earliest")
         (do (local items (r/call "ZRANGEBYSCORE" k-queue, "-inf", "+inf",
                                  "WITHSCORES", "LIMIT", 0, 1))
             (:= ret (- (or (. items [2]) 0) 1))
             (r/call "HSET" k-group group ret))

         (== status "latest")
         (:= ret (-/mq-sorted-group-set-latest k-queue k-group group))

         (== status "current")
         (do (:= ret (r/call "HGET" k-group group))
             (if (not ret)
               (:= ret (-/mq-sorted-group-set-latest k-queue k-group group)))))
   (return (tonumber ret))))

(defn.lua ^{:rt/redis {}} mq-sorted-group-waiting
  "returns number of waiting elements"
  {:added "3.0"}
  ([key partition group]
   (local k-queue (q/mq-path key partition))
   (local k-group  (cat k-queue ":" q/K_GROUP))
   (local start (or (r/call "HGET" k-group group) "-inf"))
   (return (tonumber (r/call "ZCOUNT" k-queue start "+inf")))))

(defn.lua ^{:rt/redis {}} mq-sorted-group-outdated
  "returns if queue is outdated"
  {:added "3.0"}
  ([key partition group]
   (return (< 0 (-/mq-sorted-group-waiting key partition group)))))

(defn.lua ^{:rt/redis {}} mq-sorted-queue-trim
  "trims items in the queue"
  {:added "3.0"}
  ([key partition size]
   (local k-queue (q/mq-path key partition))
   (local c-total (r/call "ZCARD" k-queue))
   (return (:? (< size c-total)
               (r/call "ZREMRANGEBYRANK" k-queue 0 (- c-total size))
               0))))

(defn.lua  ^{:rt/redis {}} mq-sorted-read
  "reads items based on sorted queue"
  {:added "3.0"}
  ([key partition group consumer count]
   (local k-queue (q/mq-path key partition))
   (local '[k-group k-pending] '[(cat k-queue ":" q/K_GROUP)
                                 (cat k-queue ":" q/K_PENDING ":" group)])
   (local start (or (r/call "HGET" k-group group) "-inf"))
   (local items (r/call "ZRANGEBYSCORE" k-queue, start, "+inf",
                        "WITHSCORES", "LIMIT", 0, count))

   (local '[res keys] '[[] []])
    (xt/for:index [i [1 (/ (len items) 2)]]
      (local idx (. items [(* i 2)]))
      (local key (cat partition "-" idx))
     (:= (. res [i]) [key, (. items [(- (* i 2) 1)])])
     (:= (. keys [i]) key))

   (if (< 0 (len keys))
     (do (r/call "SADD" k-pending (unpack keys))
         (r/call "HSET" k-group group (+ (. items [(len items)]) 1))
         (return res))
     (return nil))))

(defn.lua  ^{:rt/redis {}} mq-sorted-read-hold
  "read hold based on sorted queue"
  {:added "3.0"}
  ([key partition group consumer count expiry]
   (return (q/mq-read-hold -/mq-sorted-read key partition group consumer count expiry))))

(defn.lua  ^{:rt/redis {}} mq-sorted-read-release
  "read release based on sorted queue"
  {:added "3.0"}
  ([key partition group consumer count expiry]
   (return (q/mq-read-release -/mq-sorted-read key partition group consumer count expiry))))

(defn.lua ^{:rt/redis {}} mq-sorted-queue-length-all
  "returns lengths of all queues"
  {:added "3.0"}
  ([key]
   (return (q/mq-map-key key -/mq-sorted-queue-length))))

(defn.lua ^{:rt/redis {}} mq-sorted-group-init-uninitialised
  "initialises uninitialised partitions"
  {:added "3.0"}
  ([key group status]
   (return (q/mq-select-key key
                         q/mq-common-group-not-exists [group]
                         -/mq-sorted-group-init [group (or status "latest")]))))

(defn.lua ^{:rt/redis {}} mq-sorted-group-init-all
  "initialises all partitions"
  {:added "3.0"}
  ([key group status]
   (return (q/mq-map-key key -/mq-sorted-group-init group
                         (or status "latest")))))

(defn.lua ^{:rt/redis {}} mq-sorted-group-outdated-all
  "lists all outdated queues"
  {:added "3.0"}
  ([key group]
   (return (q/mq-filter-key key -/mq-sorted-group-outdated group))))

(defn.lua ^{:rt/redis {}} mq-sorted-group-waiting-all
  "lists all waiting queues
 
   (-> (sorted/mq-sorted-group-waiting-all \"test:set\" \"default\")
       (sort))
   => [[\"p1\" 2] [\"p2\" 2] [\"p3\" 2]]"
  {:added "3.0"}
  ([key group]
   (return (q/mq-map-key key -/mq-sorted-group-waiting group))))

(def.lua mq-sorted-table-manage
  (-> {"group_init"         -/mq-sorted-group-init
       "group_outdated"     -/mq-sorted-group-outdated
       "group_waiting"      -/mq-sorted-group-waiting
       "queue_length"       -/mq-sorted-queue-length
       "queue_earliest"     -/mq-sorted-queue-earliest
       "queue_latest"       -/mq-sorted-queue-latest
       "queue_trim"         -/mq-sorted-queue-trim

       "queue_length_all"   -/mq-sorted-queue-length-all

       "group_init_uninitialised" -/mq-sorted-group-init-uninitialised
       "group_init_all"     -/mq-sorted-group-init-all
       "group_outdated_all" -/mq-sorted-group-outdated-all
       "group_waiting_all"  -/mq-sorted-group-waiting-all}
      (xtd/obj-assign q/mq-base-table)))

(def.lua mq-sorted-table-get
  {"read_group"         -/mq-sorted-read
   "read_hold"          -/mq-sorted-read-hold
   "read_release"       -/mq-sorted-read-release
   "queue_items"        -/mq-sorted-queue-items
   "queue_get"          -/mq-sorted-queue-get})


;;
;; SEQUENTIAL
;; 


(defn.lua mq-sequential-read
  "reads items based on sorted read"
  {:added "3.0"}
  ([key partition group consumer count]
   (local k-queue (q/mq-path key partition))
   (local '[k-group k-pending] '[(cat k-queue ":" q/K_GROUP)
                                 (cat k-queue ":" q/K_PENDING ":" group)])
   (local start (r/call "HGET" k-group group))

   (if (not start) (return nil))

   (local items (r/call "ZRANGEBYSCORE", k-queue, (+ start 1), (+ start 1 count),
                        "WITHSCORES", "LIMIT" 0 count))

   (local '[res keys i idx prev]
          '[[] [] nil nil start])
   (for [(:= i 1) (/ (len items) 2)]
     (:= idx (. items [(* i 2)]))
     (if (== 1 (- idx prev))
       (do (local key (cat partition "-" idx))
           (:= (. res [i]) [key, (. items [(- (* i 2) 1)])])
           (:= (. keys [i]) key)
           (:= prev idx))
       (break)))

   (if (< 0 (len keys))
     (do (r/call "SADD" k-pending (unpack keys))
         (r/call "HSET" k-group group (+ start (len keys)))
         (return res))
     (return nil))))

(defn.lua mq-sequential-read-hold
  "read hold for sequential type"
  {:added "3.0"}
  ([key partition group consumer count expiry]
   (return (q/mq-read-hold -/mq-sequential-read key partition group consumer count expiry))))

(defn.lua mq-sequential-read-release
  "read release for sequential type"
  {:added "3.0"}
  ([key partition group consumer count expiry]
   (return (q/mq-read-release -/mq-sequential-read key partition group consumer count expiry))))

(def.lua mq-sequential-table-get
  (->> {"read_group"         -/mq-sequential-read
        "read_hold"          -/mq-sequential-read-hold
        "read_release"       -/mq-sequential-read-release}
       (xtd/obj-assign -/mq-sorted-table-get)))
