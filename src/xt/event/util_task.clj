(ns xt.event.util-task
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-sort-topo :as xtst]
             [xt.lang.spec-promise :as spec-promise]]})

(defspec.xt promise-wrap
  [:fn [:xt/any] :xt/promise])

(defspec.xt new-task
  [:fn [:xt/any [:xt/array :xt/any] [:xt/maybe :xt/any] [:xt/maybe :xt/any]] :xt/any])

(defspec.xt task-load
  [:fn [:xt/any] :xt/promise])

(defspec.xt task-unload
  [:fn [:xt/any] :xt/promise])

(defspec.xt new-loader-blank
  [:fn [] :xt/any])

(defspec.xt add-tasks
  [:fn [:xt/any [:xt/array :xt/any]] :xt/any])

(defspec.xt new-loader
  [:fn [[:xt/array :xt/any]] :xt/any])

(defspec.xt list-loading
  [:fn [:xt/any] [:xt/array :xt/any]])

(defspec.xt list-completed
  [:fn [:xt/any] [:xt/array :xt/any]])

(defspec.xt list-incomplete
  [:fn [:xt/any] [:xt/array :xt/any]])

(defspec.xt list-waiting
  [:fn [:xt/any] [:xt/array :xt/any]])

(defspec.xt load-tasks-single
  [:fn [:xt/any :xt/any [:xt/maybe [:fn [:xt/any :xt/bool] :xt/any]] [:xt/maybe [:fn [:xt/any] :xt/any]] [:xt/maybe [:fn [:xt/any :xt/any :xt/any] :xt/promise]]] :xt/promise])

(defspec.xt load-tasks
  [:fn [:xt/any [:xt/maybe [:fn [:xt/any :xt/bool] :xt/any]] [:xt/maybe [:fn [:xt/any] :xt/any]]] :xt/promise])

(defspec.xt unload-tasks
  [:fn [:xt/any [:xt/maybe [:fn [:xt/any :xt/any] :xt/any]]] :xt/promise])

(defn.xt promise-wrap
  "normalises a value into a host promise"
  {:added "4.1"}
  [value]
  (return
   (spec-promise/x:promise
    (fn []
      (return value)))))

(defn.xt new-task
  "creates a new task"
  {:added "4.1"}
  [id deps args opts]
  (return (xt/x:obj-assign
           {"::" "loader.task"
            :id id
            :deps deps
            :args args}
           opts)))

(defn.xt task-load
  "loads a task and resolves to the current or newly loaded value"
  {:added "4.1"}
  [task]
  (var #{id assert-fn load-fn args get-fn check-fn load-no-check} task)
  (when (xt/x:is-function? assert-fn)
    (when (not (assert-fn))
      (xt/x:err (xt/x:cat "Assertion Failed - " id))))
  (when (not= true load-no-check)
    (var curr (:? (xt/x:is-function? get-fn)
                  (get-fn)))
    (var check (:? (xt/x:is-function? check-fn)
                   (check-fn curr)))
    (when (== true check)
      (return (-/promise-wrap curr))))
  (when (xt/x:is-function? args)
    (:= args (args)))
  (:= args (:? (xt/x:nil? args) [] args))
  (return (-/promise-wrap (xt/x:apply load-fn args))))

(defn.xt task-unload
  "unloads a task and resolves to whether unload happened"
  {:added "4.1"}
  [task]
  (var #{unload-fn get-fn check-fn unload-no-check} task)
  (when (not= true unload-no-check)
    (var curr (:? (xt/x:is-function? get-fn)
                  (get-fn)))
    (var check (:? (xt/x:is-function? check-fn)
                   (check-fn curr)))
    (when (not= true check)
      (return (-/promise-wrap false))))
  (return
   (spec-promise/x:promise-then
    (-/promise-wrap (unload-fn))
    (fn [_]
      (return true)))))

(defn.xt new-loader-blank
  "creates a blank loader"
  {:added "4.1"}
  []
  (return {"::" "loader"
           :completed {}
           :loading {}
           :errored nil
           :order []
           :tasks {}}))

(defn.xt add-tasks
  "adds tasks to a loader"
  {:added "4.1"}
  [loader tasks]
  (var prev (xt/x:get-key loader "tasks"))
  (var all (xt/x:arr-assign (xt/x:obj-vals prev)
                            tasks))
  (var deps (xt/x:arr-map all
                          (fn [e]
                            (return
                             [(xt/x:get-key e "id")
                              (xt/x:get-key e "deps")]))))
  (return (xt/x:obj-assign
           loader
           {:order (xtst/sort-topo deps)
            :tasks (xtd/arr-juxt all
                                 (fn [e]
                                   (return (xt/x:get-key e "id")))
                                 xtd/clone-nested)})))

(defn.xt new-loader
  "creates a new loader"
  {:added "4.1"}
  [tasks]
  (return (-/add-tasks (-/new-loader-blank)
                       tasks)))

(defn.xt list-loading
  "lists loading ids"
  {:added "4.1"}
  [loader]
  (return (xt/x:obj-keys (xt/x:get-key loader "loading"))))

(defn.xt list-completed
  "lists completed ids"
  {:added "4.1"}
  [loader]
  (return (xt/x:obj-keys (xt/x:get-key loader "completed"))))

(defn.xt list-incomplete
  "lists incomplete ids"
  {:added "4.1"}
  [loader]
  (var #{tasks completed} loader)
  (var out [])
  (xt/for:object [[id task] tasks]
    (when (not= true (xt/x:get-key completed id))
      (xt/x:arr-push out id)))
  (return out))

(defn.xt list-waiting
  "lists ids whose dependencies are satisfied"
  {:added "4.1"}
  [loader]
  (var #{tasks loading completed} loader)
  (var out [])
  (xt/for:object [[id task] tasks]
    (when (and (not= true (xt/x:get-key loading id))
               (not= true (xt/x:get-key completed id))
               (xt/x:arr-every (xt/x:get-key task "deps")
                               (fn [dep-id]
                                 (return (== true (xt/x:get-key completed dep-id))))))
      (xt/x:arr-push out id)))
  (return out))

(defn.xt load-tasks-single
  "loads a single task and optionally continues the load loop"
  {:added "4.1"}
  [loader id hook-fn complete-fn loop-fn]
  (var #{tasks loading completed} loader)
  (var task (xt/x:get-key tasks id))
  (xt/x:set-key loading id true)
  (return
   (spec-promise/x:promise-catch
    (spec-promise/x:promise-then
     (-/task-load task)
     (fn [res]
       (xt/x:del-key loading id)
       (xt/x:set-key completed id true)
       (when (xt/x:not-nil? hook-fn)
         (hook-fn id true))
       (return (:? (xt/x:not-nil? loop-fn)
                   (loop-fn loader hook-fn complete-fn)
                   res))))
    (fn [err]
      (xt/x:del-key loading id)
      (xt/x:set-key loader "errored" id)
      (when (xt/x:not-nil? hook-fn)
        (hook-fn id false))
      (when (xt/x:not-nil? complete-fn)
        (complete-fn err))
      (xt/x:throw err)))))

(defn.xt load-tasks
  "loads tasks in dependency order and resolves when the loader settles"
  {:added "4.1"}
  [loader hook-fn complete-fn]
  (var #{errored order} loader)
  (when (xt/x:not-nil? errored)
    (return
     (spec-promise/x:promise
      (fn []
        (xt/x:throw (xt/x:cat "ERR - Task Errored - " errored))))))
  (var waiting (-/list-waiting loader))
  (when (< 0 (xt/x:len waiting))
    (var waiting-lu (xtd/arr-juxt waiting
                                  (fn [id]
                                    (return id))
                                  (fn [id]
                                    (return true))))
    (var next-id
         (xt/x:first
          (xtd/arr-keep order
                        (fn [id]
                          (when (== true (xt/x:get-key waiting-lu id))
                            (return id))))))
    (return (-/load-tasks-single loader next-id hook-fn complete-fn -/load-tasks)))
  (var incomplete (-/list-incomplete loader))
  (when (== 0 (xt/x:len incomplete))
    (when (xt/x:not-nil? complete-fn)
      (complete-fn true))
    (return (-/promise-wrap loader)))
  (return (-/promise-wrap loader)))

(defn.xt unload-tasks
  "unloads completed tasks in reverse dependency order"
  {:added "4.1"}
  [loader hook-fn]
  (var #{order completed tasks} loader)
  (var rorder (xt/x:arr-reverse order))
  (var unload-loop
       (fn [ids out]
         (when (== 0 (xt/x:len ids))
           (return (-/promise-wrap out)))
         (var id (xt/x:first ids))
          (var rest (xt/x:arr-slice ids 1 (xt/x:len ids)))
         (when (not= true (xt/x:get-key completed id))
           (return (unload-loop rest out)))
         (xt/x:del-key completed id)
         (var task (xt/x:get-key tasks id))
         (return
          (spec-promise/x:promise-then
           (-/task-unload task)
           (fn [unloaded]
             (when (xt/x:not-nil? hook-fn)
               (hook-fn id unloaded))
             (xt/x:arr-push out [id unloaded])
             (return (unload-loop rest out)))))))
  (return (unload-loop rorder [])))
