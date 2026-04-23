(ns
 xtbench.js.lang.util-loader-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :js
 {:runtime :basic,
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.common-data :as xtd]
   [xt.lang.spec-base :as xt]
   [xt.lang.common-repl :as repl]
   [xt.lang.util-loader :as loader]
   [js.core :as j]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-loader/new-task, :added "4.0"}
(fact
 "creates a new task"
 ^{:hidden true}
 (set
  (!.js
   (xt/x:obj-keys
    (loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))}))))
 =>
 #{"args" "id" "::" "load_fn" "deps"})

^{:refer xt.lang.util-loader/task-load, :added "4.0"}
(fact
 "loads a task"
 ^{:hidden true}
 (!.js
  (loader/task-load
   (loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})))
 =>
 "A"
 (!.js
  (loader/task-load
   (loader/new-task
    "A"
    []
    []
    {:get-fn (fn:> "B"),
     :check-fn (fn:> [res] (== "B" res)),
     :load-fn (fn:> "A")})))
 =>
 "B")

^{:refer xt.lang.util-loader/task-unload, :added "4.0"}
(fact
 "unloads a task"
 ^{:hidden true}
 (!.js
  (loader/task-unload
   (loader/new-task
    "A"
    []
    []
    {:get-fn (fn:> "B"),
     :check-fn (fn:> [res] (== "B" res)),
     :unload-fn (fn:> "A")})))
 =>
 true
 (!.js
  (loader/task-unload
   (loader/new-task
    "A"
    []
    []
    {:get-fn (fn:> "B"),
     :check-fn (fn:> [res] false),
     :unload-fn (fn:> "A")})))
 =>
 false)

^{:refer xt.lang.util-loader/new-loader, :added "4.0"}
(fact
 "creates a new loader"
 ^{:hidden true}
 (!.js
  (loader/new-loader
   [(loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})
    (loader/new-task "B" ["A"] [] {:load-fn (fn [] (return "B"))})
    (loader/new-task "C" ["B"] [] {:load-fn (fn [] (return "C"))})]))
 =>
 {"tasks"
  {"C" {"args" [], "id" "C", "deps" ["B"], "::" "loader.task"},
   "B" {"args" [], "id" "B", "deps" ["A"], "::" "loader.task"},
   "A" {"args" [], "id" "A", "deps" [], "::" "loader.task"}},
  "errored" nil,
  "completed" {},
  "loading" {},
  "order" ["A" "B" "C"],
  "::" "loader"})

^{:refer xt.lang.util-loader/list-incomplete, :added "4.0"}
(fact
 "lists incomplete tasks"
 ^{:hidden true}
 (set
  (!.js
   (loader/list-incomplete
    (loader/new-loader
     [(loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})
      (loader/new-task "B" ["A"] [] {:load-fn (fn [] (return "B"))})
      (loader/new-task
       "C"
       ["B"]
       []
       {:load-fn (fn [] (return "C"))})]))))
 =>
 #{"C" "B" "A"})

^{:refer xt.lang.util-loader/load-tasks-single, :added "4.0"}
(fact
 "loads a single task"
 ^{:hidden true}
 (notify/wait-on
  :js
  (var
   loader
   (loader/new-loader
    [(loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})]))
  (loader/load-tasks-single
   loader
   "A"
   (fn [id done] (repl/notify [id done]))))
 =>
 ["A" true]
 (notify/wait-on
  :js
  (var
   loader
   (loader/new-loader
    [(loader/new-task
      "A"
      []
      []
      {:get-fn (fn:> "B"),
       :check-fn (fn:> [res] (== "B" res)),
       :load-fn (fn:> "A")})]))
  (loader/load-tasks-single
   loader
   "A"
   (fn [id done] (repl/notify [id done]))))
 =>
 ["A" true])

^{:refer xt.lang.util-loader/load-tasks, :added "4.0"}
(fact
 "load tasks"
 ^{:hidden true}
 (set
  (notify/wait-on
   :js
   (var
    loader
    (loader/new-loader
     [(loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})
      (loader/new-task "B" ["A"] [] {:load-fn (fn [] (return "B"))})
      (loader/new-task "C" ["B"] [] {:load-fn (fn [] (return "C"))})]))
   (loader/load-tasks
    loader
    nil
    (fn [] (repl/notify (loader/list-completed loader))))))
 =>
 #{"C" "B" "A"}
 (set
  (notify/wait-on
   :js
   (:=
    (!:G loader)
    (loader/new-loader
     [(loader/new-task
       "A"
       []
       []
       {:load-fn
        (fn [] (return (j/future-delayed [100] (return "A"))))})
      (loader/new-task
       "B"
       ["A"]
       []
       {:load-fn
        (fn [] (return (j/future-delayed [100] (return "B"))))})
      (loader/new-task
       "C"
       ["B"]
       []
       {:load-fn
        (fn [] (return (j/future-delayed [100] (return "C"))))})]))
   (loader/load-tasks
    loader
    nil
    (fn [] (repl/notify (loader/list-completed loader))))))
 =>
 #{"C" "B" "A"}
 (notify/wait-on
  :js
  (:=
   (!:G loader)
   (loader/new-loader
    [(loader/new-task
      "A"
      []
      []
      {:load-fn
       (fn [] (return (j/future-delayed [100] (return "A"))))})
     (loader/new-task
      "B"
      ["A"]
      []
      {:load-fn (fn [] (return (j/future-delayed [100] (throw "B"))))})
     (loader/new-task
      "C"
      ["B"]
      []
      {:load-fn
       (fn [] (return (j/future-delayed [100] (return "C"))))})]))
  (loader/load-tasks
   loader
   nil
   (fn [] (repl/notify (loader/list-completed loader)))))
 =>
 ["A"])

^{:refer xt.lang.util-loader/load-tasks.global,
  :adopt true,
  :added "4.0"}
(fact
 "load tasks"
 ^{:hidden true}
 (notify/wait-on
  :js
  (:=
   (!:G loader)
   (loader/new-loader
    [(loader/new-task
      "A"
      []
      []
      {:load-fn
       (fn
        []
        (return (j/future-delayed [100] (:= (!:G A) (xt/x:now-ms)))))})
     (loader/new-task
      "B"
      ["A"]
      []
      {:load-fn
       (fn
        []
        (return (j/future-delayed [100] (:= (!:G B) (xt/x:now-ms)))))})
     (loader/new-task
      "C"
      ["B"]
      []
      {:load-fn
       (fn
        []
        (return
         (j/future-delayed [100] (:= (!:G C) (xt/x:now-ms)))))})]))
  (loader/load-tasks
   loader
   nil
   (fn
    []
    (repl/notify
     [(xt/x:m-floor (/ (- B A) 100))
      (xt/x:m-floor (/ (- C A) 100))]))))
 =>
 (contains [number? 2]))
