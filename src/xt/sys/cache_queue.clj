(ns xt.sys.cache-queue
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.sys.cache-common :as cache]]
   :export  [MODULE]})

;;
;;
;; this is a very simple ring buffer based queue 
;; implemented on the shared dict structure. It assumes
;; that groups will read their keys at a rate often
;; enough so that the items are not overwritten
;; this is used for broadcasting ticker values
;; via websockets
;;
;; because this is assumed to be operating with websocket
;; it does not track how many groups are registered so
;; it is up to the websocket code to do the setup and
;; teardown of the group indices.
;; the websockets using this queue has to call cleanup
;; to delete the group index
;;
;; This also means that the queue has no way of efficiently
;; getting and updating all the registered readers
;; of a given queue
;;
;; __size__  <int>
;; __index__ <int>
;; __buffer__:<idx> item
;; __group__:<id> bidx
;; __groupcount__ <int>
;; 
;;


(def$.xt META-KEY "__meta__:queue")

(defmacro.xt ^{:standalone "__index"}
  INDEX-KEY
  "gets the index key"
  {:added "4.0"}
  [queue]
  (list 'x:cat queue ":__index__"))

(defmacro.xt ^{:standalone "__groupcount"}
  GROUPCOUNT-KEY
  "gets the groupcount key"
  {:added "4.0"}
  [queue]
  (list 'x:cat queue ":__groupcount__"))

(defmacro.xt ^{:standalone "__size"}
  GROUP-KEY
  "gets the group key"
  {:added "4.0"}
  [queue gid]
  (list 'x:cat queue ":__group__:" gid))

(defmacro.xt ^{:standalone "__buffer__"}
  BUFFER-KEY
  "gets the buffer key"
  {:added "4.0"}
  [queue idx]
  (list 'x:cat queue ":__buffer__:" idx))

(defn.xt queue-meta
  "gets the queue meta"
  {:added "4.0"}
  [cache]
  (return (k/json-decode (or (x:cache-get cache "__meta__:queue")
                           "{}"))))

(defn.xt buffer-item
  "gets the buffer item"
  {:added "4.0"}
  [cache queue idx]
  (return (cache/get cache (-/BUFFER-KEY queue idx))))

(defn.xt buffer-get-index
  "gets the index in the buffer"
  {:added "4.0"}
  ([cache queue]
   (var index (cache/get cache (-/INDEX-KEY queue)))
   (if (k/nil? index)
     (k/err (k/cat "Not found - Index, Queue: " queue))
     (return (k/to-number index)))))

(defn.xt buffer-set-index
  "sets the index for a buffer"
  {:added "4.0"}
  ([cache queue idx]
   (return (cache/set cache (-/INDEX-KEY queue) (k/to-string idx)))))

(defn.xt queue-groupcount
  "gets the queue group count"
  {:added "4.0"}
  ([cache queue]
   (var count (cache/get cache (-/GROUPCOUNT-KEY queue)))
   (if (k/nil? count)
     (k/err (k/cat "Not found - Count, Queue: " queue))
     (return (k/to-number count)))))

(defn.xt queue-meta-assoc
  "sets a key in the queue meta"
  {:added "4.0"}
  [cache key item]
  (var meta (-/queue-meta cache))
  (k/set-key meta key item)
  (cache/set cache -/META-KEY (k/json-encode meta))
  (return meta))

(defn.xt queue-meta-dissoc
  "removes a key in the queue meta"
  {:added "4.0"}
  [cache key]
  (var meta (-/queue-meta cache))
  (var out  (k/get-key meta key))
  (k/del-key meta key)
  (cache/set cache -/META-KEY (k/json-encode meta))
  (return out))

(defn.xt create-queue
  "creates a queue"
  {:added "4.0"}
  ([cache queue size]
   (var idx (cache/get cache (-/INDEX-KEY queue)))
   (if idx (return idx))
   (-/queue-meta-assoc cache queue {:size size})
   (cache/set cache (-/GROUPCOUNT-KEY queue) 0)
   (return (cache/set cache (-/INDEX-KEY queue) (- size 1)))))

(defn.xt list-queues
  "lists all queues"
  {:added "4.0"}
  ([cache]
   (var meta (-/queue-meta cache))
   (return (k/obj-keys meta))))

(defn.xt list-queue-groups
  "lists all queue groups"
  {:added "4.0"}
  ([cache queue]
   (var ks (cache/list-keys cache))
   (var group-key (-/GROUP-KEY queue ""))
   (return (k/arr-keep ks
                       (fn:> [k]
                         (:? (k/starts-with? k group-key)
                             (k/sym-name k)))
		       k/identity))))

(defn.xt purge-queue
  "removes all groups and items in a queue"
  {:added "4.0"}
  ([cache queue]
   (var meta (-/queue-meta cache))
   (var qdata (. meta [queue]))
   (if (not qdata) (return false))
   
   (var #{groups size} qdata)
   (cache/del cache (-/INDEX-KEY queue))
   (cache/del cache (-/GROUPCOUNT-KEY queue))

   ;; groups
   (k/for:index [idx [0 (- size 1)]]
     (cache/del cache (-/BUFFER-KEY queue idx)))
   
   ;; buffers
   (var group-key (-/GROUP-KEY queue ""))
   (k/for:array [k (cache/list-keys cache)]
     (if (k/starts-with? k group-key)
       (cache/del cache k)))
   (return (-/queue-meta-dissoc cache queue))))

(defn.xt reset-queue
  "resets a group's index"
  {:added "4.0"}
  ([cache queue]
   (var meta (-/queue-meta cache))
   (var qdata (. meta [queue]))
   (if (not qdata) (return false))
   (var #{groups size} qdata)
   (var ridx (- size 1))
   (cache/set cache (-/INDEX-KEY queue) ridx)
   ;; buffers
   (var group-key (-/GROUP-KEY queue ""))
   (k/for:array [k (cache/list-keys cache)]
     (if (k/starts-with? k group-key)
       (cache/set cache k ridx)))))

(defn.xt has-queue
  "checks that queue exists"
  {:added "4.0"}
  ([cache queue]
   (return (k/not-nil? (cache/get cache (-/INDEX-KEY queue))))))

(defn.xt has-group
  "checks that group exists"
  {:added "4.0"}
  ([cache queue group-id]
   (return (k/not-nil?  (cache/get cache (-/GROUP-KEY queue group-id))))))

(defn.xt push-item
  "pushes an item into the queue"
  {:added "4.0"}
  ([cache queue item size]
   (var  idx (-/buffer-get-index cache queue))
   (var nidx (:? (< idx (- size 1)) (+ idx 1) 0))
   (cache/set cache (-/BUFFER-KEY queue nidx)
              item)
   (-/buffer-set-index cache queue nidx)
   (return nidx)))

(defn.xt push-items
  "pushes items into the queue"
  {:added "4.0"}
  ([cache queue items size]
   (var idx (-/buffer-get-index cache queue))
   (var nidx idx)
   (k/for:array [item items]
     (:= nidx (:? (< nidx (- size 1)) (+ nidx 1) 0))
     (cache/set cache (-/BUFFER-KEY queue nidx) item))
   (-/buffer-set-index cache queue nidx)
   (return nidx)))

;;
;; groups are partially managed by the queue
;; it only keeps track of the number of readers
;; as logic can be implemented to delete the
;; queue if readers reach zero, but it leaves
;; tracking of readers to other parts of the
;; system
;;

(defn.xt group-set-index
  "sets an index for a group"
  {:added "4.0"}
  ([cache queue group-id gidx]
   (return (cache/set cache (-/GROUP-KEY queue group-id)
                      (k/to-string gidx)))))

(defn.xt group-get-index
  "gets an index for a group"
  {:added "4.0"}
  ([cache queue group-id]
   (var gidx (cache/get cache (-/GROUP-KEY queue group-id)))
   (if (k/nil? gidx)
     (k/err (k/cat "Not found - Group: " group-id ", Queue: " queue))
     (return (k/to-number gidx)))))

(defn.xt group-setup
  "setup a group"
  {:added "4.0"}
  ([cache queue group-id]
   (var bidx  (-/buffer-get-index cache queue))
   (var gkey  (-/GROUP-KEY queue group-id))
   (var gidx  (cache/get cache gkey))
   (if gidx   (return gidx))
   (cache/set  cache gkey bidx)
   (cache/incr cache (-/GROUPCOUNT-KEY queue) 1)
   (return bidx)))

(defn.xt group-read
  "reads all items in the group"
  {:added "4.0"}
  ([cache queue group-id size]
   (var gidx (-/group-get-index  cache queue group-id))
   (var bidx (-/buffer-get-index cache queue))
   (var res [])
   (cond (> bidx gidx)
         (do (k/for:index [i [(+ gidx 1) (+ bidx (x:offset-rlen 1))]]
               (x:arr-push res (cache/get cache (-/BUFFER-KEY queue i))))
             (-/group-set-index cache queue group-id bidx)
             (return [(k/len res) res bidx]))

         (< bidx gidx)
         (do (k/for:index [i [(+ gidx 1) (x:offset-rlen size)]]
               (x:arr-push res (cache/get cache (-/BUFFER-KEY queue i))))
             (k/for:index [i [0 (+ bidx (x:offset-rlen 1))]]
               (x:arr-push res (cache/get cache (-/BUFFER-KEY queue i))))
             (-/group-set-index cache queue group-id bidx)
             (return [(k/len res) res bidx]))

         :else
         (return [0]))))

(defn.xt group-teardown
  "tears down the group"
  {:added "4.0"}
  ([cache queue group-id]
   (var bidx (-/buffer-get-index cache queue))
   (var gidx (-/group-get-index  cache queue group-id))
   (when gidx
     (cache/del cache (-/GROUP-KEY queue group-id))
     (cache/incr cache (-/GROUPCOUNT-KEY queue) -1)
     (return true))
   (return false)))

(defn.xt queue-default
  "pushes a queue, if not there creates a queue and default group"
  {:added "4.0"}
  [cache queue item size]
  (k/for:try [[ok err] (-/push-item cache
                                    queue
                                    item
                                    size)]
    {:success (return ok)
     :error (do (-/create-queue cache
                                queue
                                size)
                (-/group-setup cache
                               queue
                               "default"))})
  (return (-/push-item cache
                       queue
                       item
                       size)))

(def.xt MODULE (!:module))




(comment
  (./create-tests)
  (./ns:reset))
