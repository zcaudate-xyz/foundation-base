(ns lua.nginx.task
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :lua
  {:require [[xt.lang.base-lib :as k]
             [xt.sys.cache-common :as cache]
             [lua.nginx :as n :include [:uuid]]]
   :export  [MODULE]})

;;
;; tasks are instantiated via a loop
;; the loop itself. It is a readonly
;; situation with the task-loop itself
;; providing registration and reregistration
;; handling
;;

(defmacro.lua ^{:style/indent 0}
  INSTANCE-KEY
  "creates a new instance key
 
   (t/INSTANCE-KEY \"ticker\" \"s1334\")
   => \"__task__:ticker:s1334\""
  {:added "4.0"}
  [gid uid]
  (list 'cat "__task__:" gid ":" uid))

(defn.lua task-register
  "registers a task group"
  {:added "4.0"}
  [gid metadata setup]
  (var meta (cache/meta-get "task"))
  
  (if (k/get-key meta gid)
    (return false)
    (do
      (n/log n/ALERT (cat "\nREGISTERED TASK [" gid "]"))
      (cache/meta-assoc "task" gid
                        (k/obj-assign {:id gid
                                       :instances {}}
                                      metadata))
      (if setup (setup))
      (return true))))

(defn.lua task-unregister
  "unregisters a task ground"
  {:added "4.0"}
  [gid teardown]
  (var meta (cache/meta-get "task"))
  
  (if (not (k/get-key meta gid))
    (return false))

  (var instances (. meta [gid] ["instances"]))
  
  (when (== nil (next instances))
    (cache/meta-dissoc "task" gid)
    (if teardown (teardown))
    (return true))

  (error "INSTANCES STILL RUNNING"))

(defn.lua task-add-instance
  "adds task instance information"
  {:added "4.0"}
  [gid uid instdata]
  (var meta (cache/meta-get "task"))

  (if (not (k/get-key meta gid))
    (error "NO META FOUND"))

  (if (. meta [gid] ["instances"] [uid])
    (error "INSTANCE ALREADY ADDED"))

  (:= (. meta
         [gid]
         ["instances"]
         [uid])
      (k/obj-assign {:id uid
                     :group gid
                     :started (os.time)}
                    instdata))
  
  (cache/meta-assoc "task"
                    gid
                    (k/get-key meta gid)
                    instdata)
  (return true))

(defn.lua task-remove-instance
  "removes task instance information"
  {:added "4.0"}
  [gid uid]
  (var meta (cache/meta-get "task"))
  
  (if (not (k/get-key meta gid))
    (error "NO META FOUND"))

  (when (. meta [gid] ["instances"] [uid])
    (:= (. meta
           [gid]
           ["instances"]
           [uid])
        nil)
    (cache/meta-assoc "task"
                  gid
                  (k/get-key meta gid)))
  (return true))

(defn.lua task-meta
  "gets the task meta"
  {:added "4.0"}
  [gid]
  (var meta (cache/meta-get "task"))
  
  (if (not (k/get-key meta gid))
    (return {})
    #_(error "NO META FOUND"))

  (return (k/get-key meta gid)))

(defn.lua task-list
  "lists all running tasks"
  {:added "4.0"}
  [gid]
  (return (k/obj-keys
           (. (-/task-meta gid)
              ["instances"]))))

(defn.lua task-count
  "counts the number of running instances"
  {:added "4.0"}
  [gid]
  (return (len (-/task-list gid))))

(defn.lua task-signal-stop
  "sets a flag so the the task look will stop"
  {:added "4.0"}
  [gid uid]
  (cache/set (cache/cache :GLOBAL)
             (-/INSTANCE-KEY gid uid)
             true)
  (return true))

(defn.lua task-loop
  "constructs a task loop"
  {:added "4.0"}
  [gid
   uid
   handlers
   instdata]
  (var g (cache/cache :GLOBAL))
  (-/task-add-instance gid uid instdata)
  
  (var #{setup
         main
         check
         teardown} handlers)
  
  (var vars (:? setup (setup uid) []))
  
  ^LOOP
  (while (and (not (n/worker-exiting))
              (not (cache/get g (-/INSTANCE-KEY gid uid))))
    
    (if main (main vars))
    (if (and check (check vars))
      (break)
      (n/sleep 0.1)))
  
  (if teardown (teardown vars))
  (cache/del g (-/INSTANCE-KEY gid uid))
  (-/task-remove-instance gid uid))

(defn.lua task-start
  "starts the thread loop"
  {:added "4.0"}
  [gid
   handlers
   instdata]
  (var uid (n/uuid))
  (var meta (-/task-meta gid))
  (when (or (k/nil? (. meta max))
            (< (k/len (k/obj-keys (or (. meta instances) {})))
               (. meta max)))
    (n/start-task (fn []
                    (n/log n/ALERT (cat "\nSTARTING TASK [" gid "] - " uid))
                    (-/task-loop gid uid handlers instdata)
                    (n/log n/ALERT (cat "\nSTOPPING TASK [" gid "] - " uid))))
    (return uid)))

(def.lua MODULE (!:module))

(comment
  (./import)
  )
