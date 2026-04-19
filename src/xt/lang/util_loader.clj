(ns xt.lang.util-loader
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-sort-topo :as xtst]]})

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
  (return (xt/x:obj-assign
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
  (when (xt/x:is-function? assert-fn)
    (when (not (assert-fn))
      (xt/x:err (xt/x:cat "Assertion Failed - " id))))
  (when (not= true load-no-check)
    (var curr  (:? (xt/x:is-function? get-fn)
                   (get-fn)))
    (var check (:? (xt/x:is-function? check-fn)
                   (check-fn curr)))
  (when (== true check)
      (return curr)))

  (when (xt/x:is-function? args)
    (:= args (args)))
  (:= args (:? (xt/x:nil? args) [] args))
  (return (xt/x:apply load-fn args)))

(defn.xt task-unload
  "unloads a task"
  {:added "4.0"}
  [task]
  (var #{unload-fn get-fn check-fn unload-no-check} task)
  (when (not= true unload-no-check)
    (var curr  (:? (xt/x:is-function? get-fn)
                   (get-fn)))
    (var check (:? (xt/x:is-function? check-fn)
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
  (var prev (xt/x:get-key loader "tasks"))
  (var all  (xt/x:arr-append (xt/x:obj-vals prev)
                          tasks))
  (var deps (xt/x:arr-map all
                          (fn [e]
                            (return
                             [(xt/x:get-key e "id")
                              (xt/x:get-key e "deps")]))))
  (return (xt/x:obj-assign
           loader
           {:order     (xtst/sort-topo deps)
            :tasks     (xtd/arr-juxt all
                                     (fn [e]
                                       (return
                                        (xt/x:get-key e "id")))
                                     xtd/clone-nested)})))

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
  (return (xt/x:obj-keys (xt/x:get-key loader "loading"))))

(defn.xt list-completed
  "lists all completed ids"
  {:added "4.0"}
  [loader]
  (return (xt/x:obj-keys (xt/x:get-key loader "completed"))))

(defn.xt list-incomplete
  "lists incomplete tasks"
  {:added "4.0"}
  [loader]
  (var #{tasks loading completed} loader)
  (var out [])
  (xt/for:object [[id task] tasks]
    (when (not= true (xt/x:get-key completed id))
      (xt/x:arr-push out id)))
  (return out))

(defn.xt list-waiting
  "lists all waiting ids"
  {:added "4.0"}
  [loader]
  (var #{tasks loading completed} loader)
  (var out [])
  (xt/for:object [[id task] tasks]
    (when (and (not= true (xt/x:get-key loading id))
               (not= true (xt/x:get-key completed id))
               (xt/x:arr-every (xt/x:get-key task "deps")
                             (fn [id]
                               (return (== true (xt/x:get-key completed id))))))
      (xt/x:arr-push out id)))
  (return out))

(defn.xt load-tasks-single
  "loads a single task"
  {:added "4.0"}
  [loader id hook-fn complete-fn loop-fn]
  (var #{tasks loading completed} loader)
  (var task (xt/x:get-key tasks id))
  (xt/x:set-key loading id true)
  (return (xt/for:async [[res err] (-/task-load task)]
            {:success (do (xt/x:del-key loading id)
                          (xt/x:set-key completed id true)
                          (when (xt/x:not-nil? hook-fn) (hook-fn id true))
                          (when (xt/x:not-nil? loop-fn)
                            (return (loop-fn loader hook-fn complete-fn))))
              :error   (do (xt/x:del-key loading id)
                           (xt/x:set-key loader "errored" id)
                           (when (xt/x:not-nil? hook-fn)     (hook-fn id false))
                           (when (xt/x:not-nil? complete-fn) (complete-fn err))
                           (return nil))})))

(defn.xt load-tasks
  "load tasks"
  {:added "4.0"}
  [loader hook-fn complete-fn]
  (var #{tasks errored} loader)
  (when (xt/x:not-nil? errored)
    (xt/x:err (xt/x:cat "ERR - Task Errored - " errored)))
  (var waiting (-/list-waiting loader))
  (when (< 0 (xt/x:len waiting))
    (xt/for:array [id waiting]
      (-/load-tasks-single loader id hook-fn complete-fn -/load-tasks))
    (return waiting))

  (var incomplete (-/list-incomplete loader))
  (when (== 0 (xt/x:len incomplete))
    (when (xt/x:not-nil? complete-fn) (complete-fn true))
    (return)))

(defn.xt unload-tasks
  "unload tasks"
  {:added "4.0"}
  [loader hook-fn]
  (var #{order completed tasks} loader)
  (var rorder (xt/x:arr-reverse order))
  (var unload-task
       (fn [id]
         (when (== true (xt/x:get-key completed id))
           (xt/x:del-key completed id)
           (var task (xt/x:get-key tasks id))
           (var unloaded (-/task-unload task))
           (hook-fn id unloaded)
           (return [id unloaded]))))
  (return (xt/x:arr-keep rorder unload-task)))
