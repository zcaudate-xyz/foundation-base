(ns xt.lang.util-loader
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt new-task
  "creates a new task"
  {:added "4.0"}
  [id deps args opts]
  (var #{load-fn
         check-fn
         unload-fn
         assert-fn
         get-fn
         load-no-check
         unload-no-check} opts)
  (return (k/obj-assign
           {"::" "loader.task"
            :id id
            :deps deps
            :args args}
           opts)))

(defn.xt task-load
  "loads a task"
  {:added "4.0"}
  [task]
  (var #{id assert-fn load-fn args get-fn check-fn load-no-check} task)
  (when (k/fn? assert-fn)
    (when (not (assert-fn))
      (k/err (k/cat "Assertion Failed - " id))))
  (when (not= true load-no-check)
    (var curr  (:? (k/fn? get-fn)
                   (get-fn)))
    (var check (:? (k/fn? check-fn)
                   (check-fn curr)))
    (when (== true check)
      (return curr)))

  (when (k/fn? args)
    (:= args args))
  (return (load-fn args)))

(defn.xt task-unload
  "unloads a task"
  {:added "4.0"}
  [task]
  (var #{unload-fn get-fn check-fn unload-no-check} task)
  (when (not= true unload-no-check)
    (var curr  (:? (k/fn? get-fn)
                   (get-fn)))
    (var check (:? (k/fn? check-fn)
                   (check-fn curr)))
    (when (not= true check) (return false)))
  (unload-fn)
  (return true))

(defn.xt new-loader-blank
  "creates a blank loader"
  {:added "4.0"}
  []
  (return {"::" "loader"
           :completed {}
           :loading   {}
           :errored   nil
           :order     []
           :tasks     {}}))

(defn.xt add-tasks
  "add tasks to a loader"
  {:added "4.0"}
  [loader tasks]
  (var prev (k/get-key loader "tasks"))
  (var all  (k/arr-append (k/obj-vals prev)
                          tasks))
  (var deps (k/arr-map all
                       (fn:> [e] [(k/get-key e "id")
                                  (k/get-key e "deps")])))
  (return (k/obj-assign
           loader
           {:order     (k/sort-topo deps)
            :tasks     (k/arr-juxt all
                                   k/id-fn
                                   k/clone-nested)})))

(defn.xt new-loader
  "creates a new loader"
  {:added "4.0"}
  [tasks]
  (return (-/add-tasks (-/new-loader-blank)
                       tasks)))

(defn.xt list-loading
  "lists all loading ids"
  {:added "4.0"}
  [loader]
  (return (k/obj-keys (k/get-key loader "loading"))))

(defn.xt list-completed
  "lists all completed ids"
  {:added "4.0"}
  [loader]
  (return (k/obj-keys (k/get-key loader "completed"))))

(defn.xt list-incomplete
  "lists incomplete tasks"
  {:added "4.0"}
  [loader]
  (var #{tasks loading completed} loader)
  (var out [])
  (k/for:object [[id task] tasks]
    (when (not (k/get-key completed id))
      (x:arr-push out id)))
  (return out))

(defn.xt list-waiting
  "lists all waiting ids"
  {:added "4.0"}
  [loader]
  (var #{tasks loading completed} loader)
  (var out [])
  (k/for:object [[id task] tasks]
    (when (and (not (k/get-key loading id))
               (not (k/get-key completed id))
               (k/arr-every (k/get-key task "deps")
                            (fn [id]
                              (return (k/get-key completed id)))))
      (x:arr-push out id)))
  (return out))

(defn.xt load-tasks-single
  "loads a single task"
  {:added "4.0"}
  [loader id hook-fn complete-fn loop-fn]
  (var #{tasks loading completed} loader)
  (var task (k/get-key tasks id))
  (k/set-key loading id true)
  (return (k/for:async [[res err] (-/task-load task)]
            {:success (do (k/del-key loading id)
                          (k/set-key completed id true)
                          (when hook-fn (hook-fn id true))
                          (when loop-fn
                            (return (loop-fn loader hook-fn complete-fn))))
             :error   (do (k/del-key loading id)
                          (k/set-key loader "errored" id)
                          (when hook-fn     (hook-fn id false))
                          (when complete-fn (complete-fn err))
                          (return nil))})))

(defn.xt load-tasks
  "load tasks"
  {:added "4.0"}
  [loader hook-fn complete-fn]
  (var #{tasks errored} loader)
  (when (k/not-nil? errored)
    (k/err (k/cat "ERR - Task Errored - " errored)))
  (var waiting (-/list-waiting loader))
  (when (< 0 (k/len waiting))
    (k/for:array [id waiting]
      (-/load-tasks-single loader id hook-fn complete-fn -/load-tasks))
    (return waiting))

  (var incomplete (-/list-incomplete loader))
  (when (== 0 (k/len incomplete))
    (when complete-fn (complete-fn true))
    (return)))

(defn.xt unload-tasks
  "unload tasks"
  {:added "4.0"}
  [loader hook-fn]
  (var #{order completed tasks} loader)
  (var rorder (k/arr-reverse order))
  (var unload-task
       (fn [id]
         (when (k/get-key completed id)
           (k/del-key completed id)
           (var task (k/get-key tasks id))
           (var unloaded (-/task-unload task))
           (hook-fn id unloaded)
           (return [id unloaded]))))
  (return (k/arr-keep rorder unload-task)))

