(ns
 xtbench.python.lang.util-loader-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.common-data :as xtd]
   [xt.lang.common-spec :as xt]
   [xt.lang.common-repl :as repl]
   [xt.lang.util-loader :as loader]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-loader/new-task, :added "4.0"}
(fact
 "creates a new task"
 ^{:hidden true}
 (set
  (!.py
   (xt/x:obj-keys
    (loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))}))))
 =>
 #{"args" "id" "::" "load_fn" "deps"})

^{:refer xt.lang.util-loader/task-load, :added "4.0"}
(fact
 "loads a task"
 ^{:hidden true}
 (!.py
  (loader/task-load
   (loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})))
 =>
 "A"
 (!.py
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
 (!.py
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
 (!.py
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
 (!.py
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
  (!.py
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
  :python
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
  :python
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
