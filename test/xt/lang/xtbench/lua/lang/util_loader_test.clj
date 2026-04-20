(ns
 xtbench.lua.lang.util-loader-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :config {:program :resty},
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.common-data :as xtd]
   [xt.lang.common-spec :as xt]
   [xt.lang.common-repl :as repl]
   [xt.lang.util-loader :as loader]
   [lua.nginx :as n]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-loader/new-task, :added "4.0"}
(fact
 "creates a new task"
 ^{:hidden true}
 (set
  (!.lua
   (xt/x:obj-keys
    (loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))}))))
 =>
 #{"args" "id" "::" "load_fn" "deps"})

^{:refer xt.lang.util-loader/task-load, :added "4.0"}
(fact
 "loads a task"
 ^{:hidden true}
 (!.lua
  (loader/task-load
   (loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})))
 =>
 "A"
 (!.lua
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
 (!.lua
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
 (!.lua
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
 (!.lua
  (xtd/tree-get-spec
   (loader/new-loader
    [(loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})
     (loader/new-task "B" ["A"] [] {:load-fn (fn [] (return "B"))})
     (loader/new-task "C" ["B"] [] {:load-fn (fn [] (return "C"))})])))
 =>
 {"tasks"
  {"C"
   {"args" {},
    "id" "string",
    "load_fn" "function",
    "deps" ["string"],
    "::" "string"},
   "B"
   {"args" {},
    "id" "string",
    "load_fn" "function",
    "deps" ["string"],
    "::" "string"},
   "A"
   {"args" {},
    "id" "string",
    "load_fn" "function",
    "deps" {},
    "::" "string"}},
  "completed" {},
  "loading" {},
  "order" ["string" "string" "string"],
  "::" "string"})

^{:refer xt.lang.util-loader/list-incomplete, :added "4.0"}
(fact
 "lists incomplete tasks"
 ^{:hidden true}
 (set
  (!.lua
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
  :lua
  (var
   loader
   (loader/new-loader
    [(loader/new-task "A" [] [] {:load-fn (fn [] (return "A"))})]))
  (loader/load-tasks-single
   loader
   "A"
   (fn [id done] (repl/notify [id done]))
   nil
   nil))
 =>
 ["A" true])

^{:refer xt.lang.util-loader/load-tasks, :added "4.0"}
(fact
 "load tasks"
 ^{:hidden true}
 (set
  (notify/wait-on
   :lua
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
   :lua
   (:=
    (!:G loader)
    (loader/new-loader
     [(loader/new-task
       "A"
       []
       []
       {:load-fn (fn [] (n/sleep 0.1) (return "A"))})
      (loader/new-task
       "B"
       ["A"]
       []
       {:load-fn (fn [] (n/sleep 0.1) (return "B"))})
      (loader/new-task
       "C"
       ["B"]
       []
       {:load-fn (fn [] (n/sleep 0.1) (return "C"))})]))
   (loader/load-tasks
    loader
    nil
    (fn [] (repl/notify (loader/list-completed loader))))))
 =>
 #{"C" "B" "A"}
 (notify/wait-on
  :lua
  (:=
   (!:G loader)
   (loader/new-loader
    [(loader/new-task
      "A"
      []
      []
      {:load-fn (fn [] (n/sleep 0.1) (return "A"))})
     (loader/new-task
      "B"
      ["A"]
      []
      {:load-fn (fn [] (n/sleep 0.1) (error "B"))})
     (loader/new-task
      "C"
      ["B"]
      []
      {:load-fn (fn [] (n/sleep 0.1) (return "C"))})]))
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
  :lua
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
        (return
         (xt/x:with-delay (fn [] (:= (!:G A) (xt/x:now-ms))) 100)))})
     (loader/new-task
      "B"
      ["A"]
      []
      {:load-fn
       (fn
        []
        (return
         (xt/x:with-delay (fn [] (:= (!:G B) (xt/x:now-ms))) 100)))})
     (loader/new-task
      "C"
      ["B"]
      []
      {:load-fn
       (fn
        []
        (return
         (xt/x:with-delay
          (fn [] (:= (!:G C) (xt/x:now-ms)))
          100)))})]))
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
