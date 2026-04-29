(ns xt.event.util-task-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:python :lua]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
              [xt.lang.common-data :as xtd]
              [xt.lang.spec-promise :as spec-promise]
              [xt.lang.common-repl :as repl]
             [xt.event.util-task :as loader]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.event.util-task :as loader]
             [python.core.common-promise]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.event.util-task :as loader]
             [lua.core.common-promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.util-task/new-task :added "4.1"}
(fact "creates a new task"
  (set
   (!.js
    (xt/x:obj-keys
     (loader/new-task
      "A" [] []
      {:load-fn (fn []
                  (return "A"))}))))
  => #{"args" "id" "load_fn" "deps" "::"})

^{:refer xt.event.util-task/task-load :added "4.1"}
(fact "loads or reuses a task result through a promise"
  (notify/wait-on :js
    (spec-promise/x:promise-then
     (loader/task-load
      (loader/new-task
       "A" [] []
       {:load-fn (fn []
                   (return "A"))}))
     repl/notify))
  => "A"

  (notify/wait-on :js
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
     repl/notify))
  => "B")

^{:refer xt.event.util-task/new-loader :added "4.1"}
(fact "creates a dependency-ordered loader"
  (!.js
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
  (notify/wait-on :js
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
  (notify/wait-on :js
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
   (notify/wait-on :js
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

  (notify/wait-on :js
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


^{:refer xt.event.util-task/promise-wrap :added "4.1"}
(fact "TODO")

^{:refer xt.event.util-task/task-unload :added "4.1"}
(fact "TODO")

^{:refer xt.event.util-task/new-loader-blank :added "4.1"}
(fact "TODO")

^{:refer xt.event.util-task/add-tasks :added "4.1"}
(fact "TODO")

^{:refer xt.event.util-task/list-loading :added "4.1"}
(fact "TODO")

^{:refer xt.event.util-task/list-completed :added "4.1"}
(fact "TODO")

^{:refer xt.event.util-task/list-incomplete :added "4.1"}
(fact "TODO")

^{:refer xt.event.util-task/list-waiting :added "4.1"}
(fact "TODO")

^{:refer xt.event.util-task/unload-tasks :added "4.1"}
(fact "TODO")