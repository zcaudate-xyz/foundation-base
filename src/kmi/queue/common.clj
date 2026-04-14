(ns kmi.queue.common
  (:require [std.lang :as l]))

(l/script :lua
  {:require [[xt.lang.common-lib :as k]
             [kmi.redis :as r]]
   :static {:lang/lint-globals #{redis}}})

;;
;; CONSTANT
;;

(def.lua K_GROUP "__group__")
(def.lua K_LOCK "__lock__")
(def.lua K_PENDING "__pending__")
(def.lua K_OFFSET "__offset__")

;;
;; MULTI
;;

(defn.lua mq-do-key
  "helper function for multi key ops
 
   (-> (!.lua
        (local f (fn [key p acc]
                   (table.insert acc p)))
        (mq/mq-do-key \"test:list\" f []))
       (sort))
   => '(\"p1\" \"p2\" \"p3\")"
  {:added "3.0"}
  ([key f acc]
   (local k-space   (cat key ":_"))
   (local k-pattern (cat k-space ":[^\\:]+$"))
   (local k-partitions (r/scan-regex k-pattern (cat k-space ":*")))
   (k/for:array [[i pfull]  k-partitions]
     (local p (. pfull (sub (+ (len key) 4))))
     (f key p acc))
   (return acc)))

(defn.lua mq-map-key
  "maps function for all partition keys"
  {:added "3.0"}
  ([key f ...]
   (local args (tab ...))
   (local map-fn (fn [key partition acc]
                   (local out (f key partition (unpack args)))
                   (table.insert acc [partition out])))
   (return (-/mq-do-key key map-fn []))))

(defn.lua mq-filter-key
  "list filtered keys"
  {:added "3.0"}
  ([key pred ...]
   (local args (tab ...))
   (local filt-fn (fn [key partition acc]
                    (if (pred key partition (unpack args))
                      (table.insert acc partition))))
   (return (-/mq-do-key key filt-fn []))))

(defn.lua mq-select-key
  "select keys for applying functions"
  {:added "3.0"}
  ([key pred pargs f fargs]
   (local sel-fn (fn [key partition acc]
                   (if (pred key partition (unpack pargs))
                     (do (local out (f key partition (unpack fargs)))
                         (table.insert acc [partition out])))))
   (return (-/mq-do-key key sel-fn []))))

;;
;; GROUP
;;

(defn.lua mq-path
  "creates a mq key fragment"
  {:added "3.0"}
  ([key partition ...]
   (return (k/join ":" [key "_" partition ...]))))

(defn.lua mq-index
  "converts id to an index"
  {:added "3.0"}
  ([id partition default]
   (return
    (:? id (k/to-number (. id (sub (+ 2 (len partition))))) default))))

(defn.lua ^{:rt/redis {}}
  mq-common-group-exists
  "check that a group exists"
  {:added "3.0"}
  ([key partition group]
   (local k-group (-/mq-path key partition -/K_GROUP))
   (return (== (r/call "HEXISTS" k-group group) 1))))

(defn.lua ^{:rt/redis {}}
  mq-common-group-not-exists
  "checks that group does not exist"
  {:added "3.0"}
  ([key partition group]
   (local k-group (-/mq-path key partition -/K_GROUP))
   (return (not= (r/call "HEXISTS" k-group group) 1))))

(defn.lua ^{:rt/redis {}}
  mq-common-group-remove
  "removes a group"
  {:added "3.0"}
  ([key partition group]
   (local k-group (-/mq-path key partition -/K_GROUP))
   (return (r/call "HDEL" k-group group))))

(defn.lua ^{:rt/redis {}}
  mq-common-group-set-id
  "sets the group id on a partition"
  {:added "3.0"}
  ([key partition group id]
   (local k-group (-/mq-path key partition -/K_GROUP))
   (return (r/call "HSET" k-group group
                   (. id (sub (+ 2 (len partition))))))))

(defn.lua ^{:rt/redis {}}
  mq-common-group-get-id
  "gets the group id on a partition"
  {:added "3.0"}
  ([key partition group]
   (local k-group (-/mq-path key partition -/K_GROUP))
   (local id (r/call "HGET" k-group group))
   (if id (:= id (cat partition "-" id)))
   (return id)))

(defn.lua ^{:rt/redis {}}
  mq-common-group-pending
  "gets the group pending"
  {:added "3.0"}
  ([key partition group]
   (local k-pending (-/mq-path key partition -/K_PENDING group))
   (return (or (r/call "SMEMBERS" k-pending) 0))))

(defn.lua ^{:rt/redis {}}
  mq-common-group-ack
  "acknowledges the pending ids"
  {:added "3.0"}
  ([key partition group ids]
   (local k-pending (-/mq-path key partition -/K_PENDING group))
   (return (r/call "SREM" k-pending (unpack ids)))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text :integer]
                    :out :integer}}
  mq-common-group-lock
  "locks a queue for the group"
  {:added "3.0"}
  ([key partition group expiry]
   (local k-lock (-/mq-path key partition -/K_LOCK group))
   (return (r/call "SET" k-lock 1 "EX" expiry))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text :integer]
                    :out :integer}}
  mq-common-group-unlock
  "unlocks a queue for the group"
  {:added "3.0"}
  ([key partition group]
   (local k-lock (-/mq-path key partition -/K_LOCK group))
   (return (r/call "DEL" k-lock))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :boolean}}
  mq-common-group-locked
  "checks if the queue is locked"
  {:added "3.0"}
  ([key partition group]
   (local k-lock (-/mq-path key partition -/K_LOCK group))
   (return (== "1" (r/call "GET" k-lock)))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :boolean}}
  mq-common-group-unlocked
  "checks that the queue is unlocked"
  {:added "3.0"}
  ([key partition group]
   (local k-lock (-/mq-path key partition -/K_LOCK group))
   (return (not= "1" (r/call "GET" k-lock)))))

(defn.lua ^{:rt/redis {}}
  mq-common-group-exists-all
  "all partitions that the group exists in"
  {:added "3.0"}
  ([key group]
   (return (-/mq-filter-key key -/mq-common-group-exists group))))

(defn.lua ^{:rt/redis {}}
  mq-common-group-not-exists-all
  "all partitions that the group does not exists in"
  {:added "3.0"}
  ([key group]
   (return (-/mq-filter-key key -/mq-common-group-not-exists group))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text]
                    :out :array}}
  mq-common-group-locked-all
  "checks for all locked partitions"
  {:added "3.0"}
  ([key group]
   (return (-/mq-filter-key key -/mq-common-group-locked group))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text]
                    :out :array}}
  mq-common-group-unlocked-all
  "checks for all unlocked partitions"
  {:added "3.0"}
  ([key group]
   (return (-/mq-filter-key key -/mq-common-group-unlocked group))))

(defn.lua ^{:rt/redis {}}
  mq-common-group-remove-all
  "removes all partitions"
  {:added "3.0"}
  ([key group]
   (return (-/mq-select-key key
                            -/mq-common-group-exists [group]
                            -/mq-common-group-remove [group]))))

(defn.lua ^{:rt/redis {}}
  mq-common-group-pending-all
  "checks for all partitions the group is pending"
  {:added "3.0"}
  ([key group]
   (return (-> (-/mq-map-key key -/mq-common-group-pending group)
               (k/arr-filter (fn [arr]
                                 (var p (. arr [2]))
                                 (return (and p (< 0 (len p))))))))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text]
                    :out :array}}
  mq-common-queue-partitions
  "returns all queue partitions"
  {:added "3.0"}
  ([key]
   (local skey (cat key ":_"))
   (return (r/scan-sub skey))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text]
                    :out :array}}
  mq-common-queue-clear
  "removes all partitions"
  {:added "3.0"}
  ([key]
   (local partitions (r/scan-level (cat key ":_")))
   (return (:? (and partitions (< 0 (len partitions)))
               [(r/call "DEL" (unpack partitions)) partitions]
               [0]))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text]
                    :out :array}}
  mq-common-queue-purge
  "removes all sub keys associated"
  {:added "3.0"}
  ([key]
   (local ks (r/call "KEYS" (cat key ":*")))
   (return (:? (and ks (< 0 (len ks)))
               [(r/call "DEL" (unpack ks)) ks]
               [0]))))

;;
;; READ
;;

(defn.lua ^{:rt/redis {}}
  mq-read-expiry
  "helper function for mq-read-hold and mq-read-release"
  {:added "3.0"}
  ([f key partition group consumer count expiry check]
   (local k-lock (-/mq-path key partition -/K_LOCK group))

   (if (and check (r/call "GET" k-lock)) (return "LOCKED"))

   (local ret (f key partition group consumer count))
   (if ret
     (do (r/call "SET" k-lock "1" "EX" expiry)
         (return ret))
     (do (r/call "DEL" k-lock)
         (return nil)))))

(defn.lua ^{:rt/redis {}}
  mq-read-hold
  "only read if the partition is not locked"
  {:added "3.0"}
  ([f key partition group consumer count expiry]
   (return (-/mq-read-expiry f key partition group consumer count expiry true))))

(defn.lua ^{:rt/redis {}}
  mq-read-release
  "only reads and unlocks if stream is empty"
  {:added "3.0"}
  ([f key partition group consumer count expiry]
   (return (-/mq-read-expiry f key partition group consumer count expiry false))))

(defn.lua mq-notify
  "notify __mq__ events
 
   (mq/mq-notify \"test:list\" [\"default\"])
   => integer?"
  {:added "4.0"}
  ([key partitions]
   (return (r/call "PUBLISH" (cat "__mq__:" key)
                   (cjson.encode {"_" partitions})))))

(def.lua mq-common-table
  {"group_lock"            -/mq-common-group-lock
   "group_unlock"          -/mq-common-group-unlock
   "group_locked"          -/mq-common-group-locked
   "group_unlocked"        -/mq-common-group-unlocked
   "group_locked_all"      -/mq-common-group-locked-all
   "group_unlocked_all"    -/mq-common-group-unlocked-all
   "queue_partitions"      -/mq-common-queue-partitions
   "queue_clear"           -/mq-common-queue-clear
   "queue_purge"           -/mq-common-queue-purge})

(def.lua mq-base-table
  (-> {"group_get_id"          -/mq-common-group-get-id
       "group_set_id"          -/mq-common-group-set-id
       "group_pending"         -/mq-common-group-pending
       "group_exists"          -/mq-common-group-exists
       "group_not_exists"      -/mq-common-group-not-exists
       "group_remove"          -/mq-common-group-remove
       "group_ack"             -/mq-common-group-ack
       "group_pending_all"     -/mq-common-group-pending-all
       "group_exists_all"      -/mq-common-group-exists-all
       "group_not_exists_all"  -/mq-common-group-not-exists-all
       "group_remove_all"      -/mq-common-group-remove-all}
      (k/obj-assign -/mq-common-table)))
