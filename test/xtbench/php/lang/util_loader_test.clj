(ns
 xtbench.php.lang.util-loader-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-loader/new-task, :added "4.0"}
(fact
 "creates a new task"
 ^{:hidden true}
 (set
  (!.php
   (xt/x:obj-keys
    (loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))}))))
 =>
 #{"args" "id" "::" "load_fn" "deps"})

^{:refer xt.lang.util-loader/task-load, :added "4.0"}
(fact
 "loads a task"
 ^{:hidden true}
 (!.php
  (loader/task-load
   (loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})))
 =>
 "A"
 (!.php
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
 (!.php
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
 (!.php
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
 (!.php
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
  (!.php
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
  :php
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
  :php
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
