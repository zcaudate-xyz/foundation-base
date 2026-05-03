(ns xtbench.dart.event.util-task-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.event.util-task :as loader]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.util-task/new-task :added "4.1"}
(fact "creates a new task"

  (set
   (!.dt
    (xt/x:obj-keys
     (loader/new-task
      "A" [] []
      {:load-fn (fn []
                  (return "A"))}))))
  => #{"args" "id" "load_fn" "deps" "::"})

^{:refer xt.event.util-task/task-load :added "4.1"}
(fact "loads or reuses a task result through a promise"

  (notify/wait-on :dart
    (spec-promise/x:promise-then
     (loader/task-load
      (loader/new-task
       "A" [] []
       {:load-fn (fn []
                   (return "A"))}))
     (repl/>notify)))
  => "A"

  (notify/wait-on :dart
    (spec-promise/x:promise-then
     (loader/task-load
      (loader/new-task
       "A" [] []
       {:get-fn (fn []
                  (return "B"))
        :check-fn (fn [res]
                    (return (== "B" res)))
        :load-fn (fn []
                   (return "A"))}))
     (repl/>notify)))
  => "B")

^{:refer xt.event.util-task/new-loader :added "4.1"}
(fact "creates a dependency-ordered loader"

  (!.dt
   (var instance (loader/new-loader
                  [(loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})
                   (loader/new-task "B" ["A"] [] {:load-fn (fn [] (return "B"))})
                   (loader/new-task "C" ["B"] [] {:load-fn (fn [] (return "C"))})]))
   {"order" (. instance ["order"])
    "incomplete" (loader/list-incomplete instance)
    "waiting" (loader/list-waiting instance)})
  => (contains-in {"order" ["A" "B" "C"]
                   "incomplete" ["A" "B" "C"]
                   "waiting" ["A"]}))

^{:refer xt.event.util-task/load-tasks-single :added "4.1"}
(fact "loads a single task and updates loader state"

  (notify/wait-on :dart
    (var instance
         (loader/new-loader
          [(loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})]))
    (loader/load-tasks-single
     instance
     "A"
     (fn [id done]
       (repl/notify {"id" id
                     "done" done
                     "completed" (loader/list-completed instance)}))
     nil
     nil))
  => {"id" "A"
      "done" true
      "completed" ["A"]})

^{:refer xt.event.util-task/load-tasks :added "4.1"}
(fact "loads tasks in dependency order and rejects on the first task error"

  (notify/wait-on :dart
    (var instance
         (loader/new-loader
          [(loader/new-task
            "A" [] []
            {:load-fn (fn []
                        (return
                         (spec-promise/x:with-delay
                          50
                          (fn []
                            (return "A")))))})
           (loader/new-task
            "B" ["A"] []
            {:load-fn (fn []
                        (return
                         (spec-promise/x:with-delay
                          50
                          (fn []
                            (return "B")))))})
           (loader/new-task
            "C" ["B"] []
            {:load-fn (fn []
                        (return
                         (spec-promise/x:with-delay
                          50
                          (fn []
                            (return "C")))))})]))
    (spec-promise/x:promise-then
     (loader/load-tasks instance nil nil)
     (fn [_]
       (repl/notify (loader/list-completed instance)))))

  (set
   (notify/wait-on :dart
     (var instance
          (loader/new-loader
           [(loader/new-task
             "A" [] []
             {:load-fn (fn []
                         (return
                          (spec-promise/x:with-delay
                           50
                           (fn []
                             (return "A")))))})
            (loader/new-task
             "B" ["A"] []
             {:load-fn (fn []
                         (return
                          (spec-promise/x:with-delay
                           50
                           (fn []
                             (return "B")))))})
            (loader/new-task
             "C" ["B"] []
             {:load-fn (fn []
                         (return
                          (spec-promise/x:with-delay
                           50
                           (fn []
                             (return "C")))))})]))
     (spec-promise/x:promise-then
      (loader/load-tasks instance nil nil)
      (fn [_]
        (repl/notify (loader/list-completed instance))))))
  => #{"A" "B" "C"}

  (notify/wait-on :dart
    (var instance
         (loader/new-loader
          [(loader/new-task
            "A" [] []
            {:load-fn (fn []
                        (return
                         (spec-promise/x:with-delay
                          50
                          (fn []
                            (return "A")))))})
           (loader/new-task
             "B" ["A"] []
             {:load-fn (fn []
                         (return
                          (spec-promise/x:with-delay
                           50
                           (fn []
                             (xt/x:err "B")))))})
           (loader/new-task
            "C" ["B"] []
            {:load-fn (fn []
                        (return
                         (spec-promise/x:with-delay
                          50
                          (fn []
                            (return "C")))))})]))
    (spec-promise/x:promise-catch
     (loader/load-tasks instance nil nil)
     (fn [_]
       (repl/notify (loader/list-completed instance)))))
  => ["A"])

^{:refer xt.lang.spec-promise/x:promise-run :added "4.1"}
(fact "wraps raw values and preserves resolved results"

  (notify/wait-on :dart
    (spec-promise/x:promise-then
     (spec-promise/x:promise-run "A")
     (repl/>notify)))
  => "A"

  (notify/wait-on :dart
    (spec-promise/x:promise-then
     (spec-promise/x:promise-run
      (spec-promise/x:promise
       (fn []
         (return "B"))))
     (repl/>notify)))
  => "B")

^{:refer xt.event.util-task/task-unload :added "4.1"}
(fact "unloads only tasks that currently pass their unload check"

  (notify/wait-on :dart
    (spec-promise/x:promise-then
     (loader/task-unload
      (loader/new-task
       "A" [] []
       {:get-fn (fn [] (return "loaded"))
        :check-fn (fn [res] (return (== res "loaded")))
        :unload-fn (fn [] (return "gone"))}))
     (repl/>notify)))
  => true

  (notify/wait-on :dart
    (spec-promise/x:promise-then
     (loader/task-unload
      (loader/new-task
       "A" [] []
       {:get-fn (fn [] (return nil))
        :check-fn (fn [res] (return (== res "loaded")))
        :unload-fn (fn [] (return "gone"))}))
     (repl/>notify)))
  => false)

^{:refer xt.event.util-task/new-loader-blank :added "4.1"}
(fact "creates an empty loader state"

  (!.dt
   (loader/new-loader-blank))
  => {"::" "loader"
      "completed" {}
      "loading" {}
      "errored" nil
      "order" []
      "tasks" {}})

^{:refer xt.event.util-task/add-tasks :added "4.1"}
(fact "adds tasks and recalculates dependency order"

  (!.dt
   (var instance
        (loader/add-tasks
         (loader/new-loader-blank)
         [(loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})
          (loader/new-task "B" ["A"] [] {:load-fn (fn [] (return "B"))})]))
   {"order" (. instance ["order"])
    "tasks" (xt/x:obj-keys (. instance ["tasks"]))})
  => {"order" ["A" "B"]
      "tasks" ["A" "B"]})

^{:refer xt.event.util-task/list-loading :added "4.1"}
(fact "lists all currently loading task ids"

  (set
   (!.dt
    (var instance (loader/new-loader-blank))
    (xt/x:set-key (. instance ["loading"]) "A" true)
    (xt/x:set-key (. instance ["loading"]) "B" true)
    (loader/list-loading instance)))
  => #{"A" "B"})

^{:refer xt.event.util-task/list-completed :added "4.1"}
(fact "lists all completed task ids"

  (set
   (!.dt
    (var instance (loader/new-loader-blank))
    (xt/x:set-key (. instance ["completed"]) "A" true)
    (xt/x:set-key (. instance ["completed"]) "B" true)
    (loader/list-completed instance)))
  => #{"A" "B"})

^{:refer xt.event.util-task/list-incomplete :added "4.1"}
(fact "lists tasks that have not completed yet"

  (set
   (!.dt
    (var instance
         (loader/new-loader
          [(loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})
           (loader/new-task "B" ["A"] [] {:load-fn (fn [] (return "B"))})
           (loader/new-task "C" ["B"] [] {:load-fn (fn [] (return "C"))})]))
    (xt/x:set-key (. instance ["completed"]) "A" true)
    (loader/list-incomplete instance)))
  => #{"B" "C"})

^{:refer xt.event.util-task/list-waiting :added "4.1"}
(fact "lists tasks whose dependencies are satisfied"

  (set
   (!.dt
    (var instance
         (loader/new-loader
          [(loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})
           (loader/new-task "B" ["A"] [] {:load-fn (fn [] (return "B"))})
           (loader/new-task "C" ["B"] [] {:load-fn (fn [] (return "C"))})]))
    (xt/x:set-key (. instance ["completed"]) "A" true)
    (loader/list-waiting instance)))
  => #{"B"})

^{:refer xt.event.util-task/unload-tasks :added "4.1"}
(fact "unloads completed tasks in reverse dependency order"

  (notify/wait-on :dart
    (var seen [])
    (var instance
         (loader/new-loader
          [(loader/new-task "A" [] []
                            {:unload-no-check true
                             :unload-fn (fn [] (return nil))})
           (loader/new-task "B" ["A"] []
                            {:unload-no-check true
                             :unload-fn (fn [] (return nil))})]))
    (xt/x:set-key (. instance ["completed"]) "A" true)
    (xt/x:set-key (. instance ["completed"]) "B" true)
    (spec-promise/x:promise-then
     (loader/unload-tasks instance
                          (fn [id unloaded]
                            (x:arr-push seen [id unloaded])))
     (fn [result]
       (repl/notify {"result" result
                     "seen" seen
                     "completed" (loader/list-completed instance)}))))
  => {"result" [["B" true]
                ["A" true]]
      "seen" [["B" true]
              ["A" true]]
      "completed" []})

(comment
  (s/snapto)
  (s/run '[xt.event.util-task])
  
  (s/seedgen-benchadd '[xt.event.util-task] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.util-task]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.util-task]  {:lang [:lua :python] :write true}))
