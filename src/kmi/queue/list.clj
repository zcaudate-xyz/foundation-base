(ns kmi.queue.list
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :lua
  {:runtime :redis
   :require [[xt.lang.base-lib :as k]
             [kmi.redis :as r]
             [kmi.queue.common :as q]]
   :static {:lang/lint-globals #{redis}}})

;;
;; LIST
;;

(defn.lua ^{:rt/redis {}}
  mq-list-group-set-latest
  "sets the group to latest"
  {:added "3.0"}
  ([k-group group k-queue c-offset]
   (local c-total (r/call "LLEN" k-queue))
   (local ret (+ c-offset c-total))
   (r/call "HSET" k-group group ret)
   (return ret)))

(defn.lua ^{:rt/redis {}}
  mq-list-queue-params
  "gets the list queue params"
  {:added "3.0"}
  ([key partition]
   (local k-queue  (q/mq-path key partition))
   (local k-offset (cat k-queue ":" q/K_OFFSET))
   (local c-offset (or (r/call "GET" k-offset) 0))
   (return k-queue k-offset c-offset)))

(defn.lua ^{:rt/redis {}}
  mq-list-group-init
  "initiates the list queue"
  {:added "3.0"}
  ([key partition group status]
   (local '[k-queue k-offset c-offset] (-/mq-list-queue-params key partition))
   (local k-group  (cat k-queue ":" q/K_GROUP))

   (local ret)
   (cond (== status "earliest")
         (do (:= ret c-offset)
             (r/call "HSET" k-group group ret))

         (== status "latest")
         (:= ret (-/mq-list-group-set-latest k-group group k-queue c-offset))

         (== status "current")
         (:= ret (r/call "HGET" k-group group)))
   (if (not ret)
     (:= ret (-/mq-list-group-set-latest k-group group k-queue c-offset)))
   (return (tonumber ret))))

(defn.lua ^{:rt/redis {}} mq-list-group-waiting
  "indicates if there are unprocessed entries"
  {:added "3.0"}
  ([key partition group]
   (local '[k-queue k-offset c-offset] (-/mq-list-queue-params key partition))
   (local k-group  (cat k-queue ":" q/K_GROUP))
   (local '[counter total] '[(or (r/call "HGET" k-group group) 0)
                             (r/call "LLEN" k-queue)])
   (return (- total (- counter c-offset)))))

(defn.lua ^{:rt/redis {}} mq-list-group-outdated
  "indicates if there are waiting entries"
  {:added "3.0"}
  ([key partition group]
   (return (< 0 (-/mq-list-group-waiting key partition group)))))

(defn.lua ^{:rt/redis {}} mq-list-queue-length
  "gets list queue length"
  {:added "3.0"}
  ([key partition]
   (local k-queue (q/mq-path key partition))
   (return (r/call "LLEN" k-queue))))

(defn.lua ^{:rt/redis {}} mq-list-queue-earliest
  "gets list queue earliest"
  {:added "3.0"}
  ([key partition]
   (local k-offset (q/mq-path key partition q/K_OFFSET))
   (return (r/as-num (r/call "GET" k-offset)))))

(defn.lua ^{:rt/redis {}} mq-list-queue-latest
  "gets list queue latest"
  {:added "3.0"}
  ([key partition]
   (local k-queue  (q/mq-path key partition))
   (local k-offset (cat k-queue ":" q/K_OFFSET))
   (return (+ (r/call "LLEN" k-queue)
              (or (r/call "GET" k-offset) 0)))))

(defn.lua ^{:rt/redis {}}
  mq-list-queue-items
  "grabs the list items"
  {:added "3.0"}
  ([key partition start finish count]
   (local '[k-queue k-offset c-offset] (-/mq-list-queue-params key partition))
   (local '[idx-0 idx-1]      '[(q/mq-index start partition c-offset)
                                (q/mq-index finish partition -1)])
   (local '[c-start c-finish] '[(- idx-0 c-offset)
                                (:? (< idx-1 0)
                                    idx-1
                                    (- idx-1 c-offset))])
   (local items (r/call "LRANGE" k-queue c-start c-finish))
   (return
    (:? (< 0 (len items)) [(k/to-number idx-0) items] nil))))

(defn.lua ^{:rt/redis {}} mq-list-queue-get
  "gets the first item in the list
 
   (list/mq-list-queue-get \"test:list\" \"p1\" \"p1-0\")
   => \"1\""
  {:added "3.0"}
  ([key partition id]
   (local '[k-queue k-offset c-offset] (-/mq-list-queue-params key partition))
   (local idx   (q/mq-index id partition 0))
   (local items (r/call "LRANGE" k-queue idx (+ idx)))
   (return
    (:? (< 0 (len items)) (. items [1]) nil))))

(defn.lua ^{:rt/redis {}} mq-list-queue-trim
  "trims the list to a given size"
  {:added "3.0"}
  ([key partition size]
   (local k-queue  (q/mq-path key partition))
   (local k-offset (cat k-queue ":" q/K_OFFSET))
   (local c-total (r/call "LLEN" k-queue))
   (local l-trim (- c-total size))
   (if (> l-trim 0)
     (do (local ln (r/call "LTRIM" k-queue l-trim -1))
         (return [l-trim (r/call "INCRBY" k-offset l-trim)]))
     (return [0 (or (r/call "GET" k-offset) "0")]))))

(defn.lua ^{:rt/redis {}}
  mq-list-read
  "read elements from a list"
  {:added "3.0"}
  ([key partition group consumer count]
   (local '[k-queue k-offset c-offset] (-/mq-list-queue-params key partition))
   (local '[k-group k-pending] '[(cat k-queue ":" q/K_GROUP)
                                 (cat k-queue ":" q/K_PENDING ":" group)])
   (local counter (or (r/call "HGET" k-group group) "0"))
   (local start (- counter c-offset))
   (local items (r/call "LRANGE"
                        k-queue
                        start
                        (- (+ start count) 1)))

   (local '[keys n] '[{} 0])
   (k/for:object [[k v] items]
     (:= n (+ n 1))
     (:= (. keys [n]) (cat partition "-" (- (+ k counter) 1))))

   (if (< 0 (len keys))
     (do (r/call "SADD" k-pending (unpack keys))
         (r/call "HSET" k-group group (+ counter (len items)))
         (return [counter items]))
     (return nil))))

(defn.lua ^{:rt/redis {}}
  mq-list-read-hold
  "mq-read-hold on list"
  {:added "3.0"}
  ([key partition group consumer count expiry]
   (return (q/mq-read-hold -/mq-list-read key partition group consumer count expiry))))

(defn.lua ^{:rt/redis {}}
  mq-list-read-release
  "mq-read-release on list"
  {:added "3.0"}
  ([key partition group consumer count expiry]
   (return (q/mq-read-release -/mq-list-read key partition group consumer count expiry))))

(defn.lua ^{:rt/redis {}} mq-list-group-init-uninitialised
  "initiates uninitialised groups"
  {:added "3.0"}
  ([key group status]
   (return (q/mq-select-key key
                            q/mq-common-group-not-exists [group]
                            -/mq-list-group-init [group (or status "latest")]))))

(defn.lua ^{:rt/redis {}} mq-list-queue-length-all
  "returns all lengths in the queue"
  {:added "3.0"}
  ([key]
   (return (q/mq-map-key key -/mq-list-queue-length))))

(defn.lua ^{:rt/redis {}} mq-list-group-init-all
  "initiates all groups"
  {:added "3.0"}
  ([key group status]
   (return (q/mq-map-key key -/mq-list-group-init group
                           (or status "latest")))))

(defn.lua ^{:rt/redis {}} mq-list-group-outdated-all
  "returns all outdated partitions on a queue"
  {:added "3.0"}
  ([key group]
   (return (q/mq-filter-key key -/mq-list-group-outdated group))))

(defn.lua ^{:rt/redis {}} mq-list-group-waiting-all
  "returns all partitions having waiting items on a queue"
  {:added "3.0"}
  ([key group]
   (return (q/mq-map-key key -/mq-list-group-waiting group))))

;;
;;

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :integer}}
  mq-list-write
  "writes a given value to list"
  {:added "4.0"}
  ([key partition item]
   (return (r/call "RPUSH" (q/mq-path key partition) item))))

(def.lua mq-list-table-manage
  (-> {"group_init"         -/mq-list-group-init
       "group_outdated"     -/mq-list-group-outdated
       "group_waiting"      -/mq-list-group-waiting
       "queue_length"       -/mq-list-queue-length
       "queue_earliest"     -/mq-list-queue-earliest
       "queue_latest"       -/mq-list-queue-latest
       "queue_trim"         -/mq-list-queue-trim

       "queue_length_all"   -/mq-list-queue-length-all

       "group_init_uninitialised" -/mq-list-group-init-uninitialised
       "group_init_all"     -/mq-list-group-init-all
       "group_outdated_all" -/mq-list-group-outdated-all
       "group_waiting_all"  -/mq-list-group-waiting-all}
      (k/obj-assign q/mq-base-table)))

(def.lua mq-list-table-get
  {"read_group"         -/mq-list-read
   "read_hold"          -/mq-list-read-hold
   "read_release"       -/mq-list-read-release
   "queue_items"        -/mq-list-queue-items
   "queue_get"          -/mq-list-queue-get})



