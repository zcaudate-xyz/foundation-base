(ns kmi.queue.stream
  (:require [std.lang :as l]))

(l/script :lua
  {:require [[xt.lang.base-lib :as k]
             [kmi.redis :as r]
             [kmi.queue.common :as q]]
   :static {:lang/lint-globals #{redis}}})

;;
;; STREAM
;;

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text]
                    :out :integer}}
  mq-stream-queue-length
  "gets the length of queue"
  {:added "3.0"}
  ([key partition]
   (local k-queue (q/mq-path key partition))
   (return (tonumber (r/call "XLEN" k-queue)))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text]
                    :out :text}}
  mq-stream-queue-earliest
  "gets the earliest id"
  {:added "3.0"}
  ([key partition]
   (local k-queue (q/mq-path key partition))
   (local result nil)

   (pcall (fn [] (:= result (r/call "XINFO" "STREAM" k-queue))))

   (cond (not result)
         (return nil)

         (== (. result [13])
             "recorded-first-entry-id")
         (return (. result [14]))

         :else
         (return (. result [12] [1])))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text]
                    :out :text}}
  mq-stream-queue-latest
  "gets the latest id"
  {:added "3.0"}
  ([key partition]
   (local k-queue (q/mq-path key partition))
   (local result nil)

   (pcall (fn [] (:= result (r/call "XINFO" "STREAM" k-queue))))
   (if result
     (return (. result [8])))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text :text :integer]
                    :out :array}}
  mq-stream-queue-items
  "gets items in the queue"
  {:added "3.0"}
  ([key partition start finish count]
   (local k-queue (q/mq-path key partition))
   (local start (or start "-"))
   (local finish (or finish "+"))
   (local result nil)
   (pcall (fn []
            (:= result (r/call "XRANGE" k-queue start finish "COUNT" (or count 1000)))))
   (return result)))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text :text :integer]
                    :out :array}}
  mq-stream-queue-revitems
  "gets rev items
 
   (map second (stream/mq-stream-queue-revitems \"test:stream\" \"p1\" \"+\"  \"-\" 10))
   =>  [[\"id\" \"2\"] [\"id\" \"1\"]]"
  {:added "4.0"}
  ([key partition finish start count]
   (local k-queue (q/mq-path key partition))
   (local start (or start "-"))
   (local finish (or finish "+"))
   (local result nil)
   (pcall (fn []
            (:= result (r/call "XREVRANGE" k-queue finish start "COUNT" (or count 1000)))))
   (return result)))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :array}}
  mq-stream-queue-get
  "gets an item in the queue
 
   (stream/mq-stream-queue-get \"test:stream\" \"p1\"
                               (stream/mq-stream-queue-earliest \"test:stream\" \"p1\"))
   => [\"id\" \"1\"]"
  {:added "3.0"}
  ([key partition id]
   (local k-queue (q/mq-path key partition))
   (local result nil)
   (pcall (fn [] (:= result (r/call "XRANGE" k-queue id id "COUNT" 1))))

   (if (and result (< 0 (len result)))
     (return (. result [1] [2])))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :integer]
                    :out :integer}}
  mq-stream-queue-trim
  "trims the stream to a given size
 
   (stream/mq-stream-queue-trim \"test:stream\" \"p1\"
                                10)
   => integer?"
  {:added "3.0"}
  ([key partition size]
   (local k-queue (q/mq-path key partition))
   (return (r/call "XTRIM" k-queue "MAXLEN" "~" size))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :array}}
  mq-stream-group-pending
  "gets all pending ids in the queue"
  {:added "3.0"}
  ([key partition group]
   (local k-queue (q/mq-path key partition))
   (return (k/arr-map (r/call "XPENDING" k-queue group "-" "+" 100)
                   k/first))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :text}}
  mq-stream-group-get-id
  "gets the id for a group"
  {:added "3.0"}
  ([key partition group]
   (local k-queue (q/mq-path key partition))
   (local '[info-groups mq-common-group-id] '[(r/call "XINFO" "GROUPS" k-queue) "0-0"])
   (k/for:array [[i arr] info-groups]
     (if (== group (. arr [2])) (:= mq-common-group-id (. arr [8]))))
   (return mq-common-group-id)))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text :text]
                    :out :text}}
  mq-stream-group-set-id
  "sets the id for a group"
  {:added "3.0"}
  ([key partition group id]
   (local k-queue (q/mq-path key partition))
   (return (r/call "XGROUP" "SETID" k-queue group id))))

(defn.lua ^{:rt/redis {:encode {:in [3]}}
            :rt/db {:in  [:text :text :text :jsonb]
                    :out :integer}}
  mq-stream-group-ack
  "acknowledges ids for a group
 
   (stream/mq-stream-group-ack \"test:stream\" \"p1\" \"default\"
                               [\"0-0\"])"
  {:added "3.0"}
  ([key partition group ids]
   (local k-queue (q/mq-path key partition))
   (return (r/call "XACK" k-queue group (unpack ids)))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :boolean}}
  mq-stream-group-exists
  "checks that group exists for a partition"
  {:added "3.0"}
  ([key partition group]
   (local k-queue (q/mq-path key partition))
   (local '[groups exists] '[(r/call "XINFO" "GROUPS" k-queue) false])
   (k/for:array [[i arr] groups]
     (if (== group (. arr [2])) (:= exists true)))
   (return exists)))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :boolean}}
  mq-stream-group-not-exists
  "checks that group exists for a partition"
  {:added "3.0"}
  ([key partition group]
   (return (not (-/mq-stream-group-exists key partition group)))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text :text]
                    :out :array}}
  mq-stream-group-init
  "initiates the stream group"
  {:added "3.0"}
  ([key partition group status]
   (local k-queue (q/mq-path key partition))
   (local k-group  (cat k-queue ":" q/K_GROUP))
   (local exists  (-/mq-stream-group-exists key partition group))
   (local '[k-id ret])
   
   (if (== status "earliest")
     (:= k-id "0")
     (:= k-id "$"))

   (if (not exists)
     (r/call "XGROUP" "CREATE" k-queue group k-id "MKSTREAM")
     (if (not= status "current")
       (r/call "XGROUP" "SETID" k-queue group k-id)))

   (local name-pred (fn [group]
                      (return)))

   (return (-> (r/call "XINFO" "GROUPS" k-queue)
               (k/arr-filter (fn [arr]
                            (return (== group (k/second arr)))))
               (k/first)))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :integer}}
  mq-stream-group-waiting
  "checks for waiting elements"
  {:added "3.0"}
  ([key partition group]
   (local k-queue (q/mq-path key partition))
   (local mq-common-group-id (-/mq-stream-group-get-id key partition group))
   (local items (r/call "XRANGE" k-queue mq-common-group-id "+"))
   (return (len items))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :boolean}}
  mq-stream-group-outdated
  "checks that new elements are available"
  {:added "3.0"}
  ([key partition group]
   (local k-queue (q/mq-path key partition))
   (local mq-common-group-id (-/mq-stream-group-get-id key partition group))
   (local stream-id (. (r/call "XINFO" "STREAM" k-queue) [8]))
   (return (not= mq-common-group-id stream-id))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :integer}}
  mq-stream-group-remove
  "removes group from partition"
  {:added "3.0"}
  ([key partition group]
   (local k-queue (q/mq-path key partition))
   (return (r/call "XGROUP" "DESTROY" k-queue group))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text :integer]
                    :out :array}}
  mq-stream-group-read
  "reads from a group"
  {:added "3.0"}
  ([key group consumer count]
   (return (r/call "XREADGROUP" "GROUP" group consumer
                   "COUNT" count "STREAMS" key ">"))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text]
                    :out :array}}
  mq-stream-group-create
  "creates a group"
  {:added "3.0"}
  ([key group latest]
   (return (r/call "XGROUP" "CREATE" key group
                   (or latest "0")
                   "MKSTREAM"))))

(defn.lua mq-stream-read-init
  "reads from a stream"
  {:added "4.0"}
  [key partition group opts]
  (local k-queue (q/mq-path key partition))
  (local #{mode consumer count} opts)
  (cond (== mode "last")
        (do (local k-last (r/call "XREVRANGE" k-queue "+" "-" "COUNT" "2"))
            (-/mq-stream-group-create k-queue group
                                      (k/first (or (k/second k-last)
                                                   {}))))
        
        (or (== nil mode)
            (== mode "start"))
        (-/mq-stream-group-create k-queue group false))
  (return (-/mq-stream-group-read k-queue group consumer count)))

(defn.lua mq-stream-read-raw
  "precursor to `read` and `read-last` methods"
  {:added "4.0"}
  ([key partition group consumer count opts]
   (local k-queue (q/mq-path key partition))
   (local res)
   (local res-fn (fn []
                   (:= res (-/mq-stream-group-read k-queue group
                                                   consumer count))))
   (local '[success err] (pcall res-fn))
   (if err
     (if (== "NOGROUP" (string.sub err 1 7))
       (do (:= res (-/mq-stream-read-init key partition group
                                          (k/obj-assign {:consumer consumer
                                                         :count count}
                                                        opts))))
       (error err)))
   (return (:? res (. res [1] [2]) res))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text :text :integer]
                    :out :array}}
  mq-stream-read
  "reads and automatically creates a group"
  {:added "3.0"}
  ([key partition group consumer count]
   (return (-/mq-stream-read-raw key partition group consumer count {}))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text :text :integer]
                    :out :array}}
  mq-stream-read-last
  "reads and automatically creates a group, reading from the last added"
  {:added "4.0"}
  ([key partition group consumer count]
   (return (-/mq-stream-read-raw key partition group consumer count {:mode "last"}))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text :text :integer :integer]
                    :out :array}}
  mq-stream-read-hold
  "reads and locks the queue"
  {:added "3.0"}
  ([key partition group consumer count expiry]
   (return (q/mq-read-hold -/mq-stream-read key partition group consumer count expiry))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text :text :integer :integer]
                    :out :array}}
  mq-stream-read-release
  "reads and releases the queue if no elements"
  {:added "3.0"}
  ([key partition group consumer count expiry]
   (return (q/mq-read-release -/mq-stream-read key partition group consumer count expiry))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text]
                    :out :array}}
  mq-stream-queue-length-all
  "gets the length of all queues"
  {:added "3.0"}
  ([key]
   (return (q/mq-map-key key -/mq-stream-queue-length))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :array}}
  mq-stream-group-init-uninitialised
  "initiates group for all uninitialised partitions"
  {:added "3.0"}
  ([key group status]
   (return (q/mq-select-key key
                            -/mq-stream-group-not-exists [group]
                            -/mq-stream-group-init [group (or status "latest")]))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :text]
                    :out :array}}
  mq-stream-group-init-all
  "initiates all groups for the stream"
  {:added "3.0"}
  ([key group status]
   (return (q/mq-map-key key -/mq-stream-group-init group
                         (or status "latest")))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text]
                    :out :array}}
  mq-stream-group-outdated-all
  "returns all outdated queues"
  {:added "3.0"}
  ([key group]
   (return (q/mq-filter-key key -/mq-stream-group-outdated group))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text]
                    :out :array}}
  mq-stream-group-waiting-all
  "returns all waiting queues"
  {:added "3.0"}
  ([key group]
   (return (q/mq-map-key key -/mq-stream-group-waiting group))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text]
                    :out :array}}
  mq-stream-group-remove-all
  "removes all groups from queue"
  {:added "3.0"}
  ([key group]
   (return (q/mq-select-key key
                            -/mq-stream-group-exists [group]
                            -/mq-stream-group-remove [group]))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text]
                    :out :array}}
  mq-stream-group-exists-all
  "checks for existing group"
  {:added "3.0"}
  ([key group]
   (return (q/mq-filter-key key -/mq-stream-group-exists group))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text]
                    :out :array}}
  mq-stream-group-not-exists-all
  "checks for groups that not exists"
  {:added "3.0"}
  ([key group]
   (return (q/mq-filter-key key -/mq-stream-group-not-exists group))))

(defn.lua ^{:rt/redis {:encode {:in [2]}}
            :rt/db {:in  [:text :text :jsonb]
                    :out :text}}
  mq-stream-write
  "writes entry to a stream"
  {:added "4.0"}
  ([key partition m]
   (return (r/call "XADD" (cat key ":_:" partition) "*" (unpack (k/to-flat m))))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :jsonb]
                    :out :text}}
  mq-stream-broadcast-write
  "similar to `mq-stream-stream` but writes a to a single broadcast key `_`"
  {:added "4.0"}
  ([key partition pkg]
   (return (r/call "XADD" (cat key ":_:" partition) "*" "_" pkg))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :jsonb]
                    :out :integer}}
  mq-stream-broadcast-publish
  "calls publish on the broadcast channel with undecoded json array of partitions"
  {:added "4.0"}
  ([key partitions]
   (return (r/call "PUBLISH" key partitions))))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :text :jsonb]
                    :out :text}}
  mq-stream-broadcast-single
  "writes to a single partition and publish"
  {:added "4.0"}
  ([key partition pkg]
   (local out (r/call "XADD" (cat key ":_:" partition) "*" "_" pkg))
   (r/call "PUBLISH" key (cat "[\"" partition "\"]"))
   (return out)))

(defn.lua ^{:rt/redis {}
            :rt/db {:in  [:text :jsonb :jsonb]
                    :out :jsonb}}
  mq-stream-broadcast-multi
  "writes to a multiple partitions with publish"
  {:added "4.0"}
  ([key entry-list ids]
   (var entries (cjson.decode entry-list))
   (var out [])
   (k/for:array [e entries]
     (var partition (k/first e))
     (var pkg (k/second e))
     (table.insert out (r/call "XADD" (cat key ":_:" partition) "*" "_" (cjson.encode pkg))))
   (when ids
     (r/call "PUBLISH" key ids))
   (return (cjson.encode out))))

(def.lua mq-stream-table-manage
  (-> {"group_init"            -/mq-stream-group-init
       "group_init_uninitialised" -/mq-stream-group-init-uninitialised
       "group_init_all"        -/mq-stream-group-init-all
       "group_outdated"        -/mq-stream-group-outdated
       "group_outdated_all"    -/mq-stream-group-outdated-all
       "group_waiting"         -/mq-stream-group-waiting
       "group_waiting_all"     -/mq-stream-group-waiting-all
       "queue_length"          -/mq-stream-queue-length
       "queue_earliest"        -/mq-stream-queue-earliest
       "queue_latest"          -/mq-stream-queue-latest
       "queue_length_all"      -/mq-stream-queue-length-all

       "group_ack"             -/mq-stream-group-ack
       "group_get_id"          -/mq-stream-group-get-id
       "group_set_id"          -/mq-stream-group-set-id
       "group_pending"         -/mq-stream-group-pending
       "group_exists"          -/mq-stream-group-exists
       "group_exists_all"      -/mq-stream-group-exists-all
       "group_not_exists"      -/mq-stream-group-not-exists
       "group_not_exists_all"  -/mq-stream-group-not-exists-all
       "group_remove"          -/mq-stream-group-remove
       "group_remove_all"      -/mq-stream-group-remove-all}
      (k/obj-assign q/mq-common-table)))

(def.lua mq-stream-table-get
  {"read_group"           -/mq-stream-read
   "read_hold"            -/mq-stream-read-hold
   "read_release"         -/mq-stream-read-release
   "queue_items"          -/mq-stream-queue-items
   "queue_get"            -/mq-stream-queue-get})
  
