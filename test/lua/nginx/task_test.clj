(ns lua.nginx.task-test
  (:require [rt.nginx.config :as config]
            [std.json :as json]
            [std.lang :as l])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :config  {:exec ["resty" "--http-conf" (config/create-resty-params) "-e"]}
   :require [[lua.nginx :as n]
             [lua.nginx.task :as t]
             [xt.sys.cache-common :as cache]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.task/INSTANCE-KEY :added "4.0"}
(fact "creates a new instance key"

  (t/INSTANCE-KEY "ticker" "s1334")
  => "__task__:ticker:s1334")

^{:refer lua.nginx.task/task-register :added "4.0"
  :setup [(!.lua (cache/flush (cache/cache :GLOBAL)))]}
(fact "registers a task group"

  (t/task-register "ticker" {:hello "world"})
  => true

  (t/task-register "ticker" {:hello "world"})
  => false)

^{:refer lua.nginx.task/task-unregister :added "4.0"
  :setup [(!.lua (cache/flush (cache/cache :GLOBAL)))]}
(fact "unregisters a task ground"

  (t/task-unregister "ticker")
  => false

  (t/task-register "ticker" {:hello "world"})
  => true

  (t/task-unregister "ticker")
  => true)

^{:refer lua.nginx.task/task-add-instance :added "4.0"
  :setup [(!.lua (cache/flush (cache/cache :GLOBAL)))]}
(fact "adds task instance information"

  (t/task-register "ticker" {:hello "world"})
  => true

  (t/task-meta "ticker")
  => {"hello" "world", "id" "ticker", "instances" {}}

  (t/task-add-instance "ticker"
                       "inst-0"
                       {:foo "bar"})
  => true

  ;;
  ;; Info
  ;;

  (t/task-meta "ticker")
  => (contains-in
      {"hello" "world",
       "id" "ticker",
       "instances" {"inst-0" {"foo" "bar",
                             "started" integer?
                              "group" "ticker", "id" "inst-0"}}})

  (t/task-list "ticker")
  => ["inst-0"]

  (t/task-count "ticker")
  => 1

  ;;
  ;; cannot unregister with running tasks
  ;;

  (t/task-unregister "ticker")
  => throws

  (t/task-remove-instance "ticker"
                          "inst-0")
   => true

   (t/task-count "ticker")
   => 0

   ;;
   ;; unregister when count is 0
   ;;

  (t/task-unregister "ticker")
  => true)

^{:refer lua.nginx.task/task-remove-instance :added "4.0"}
(fact "removes task instance information")

^{:refer lua.nginx.task/task-meta :added "4.0"}
(fact "gets the task meta")

^{:refer lua.nginx.task/task-list :added "4.0"}
(fact "lists all running tasks")

^{:refer lua.nginx.task/task-count :added "4.0"}
(fact "counts the number of running instances")

^{:refer lua.nginx.task/task-signal-stop :added "4.0"}
(fact "sets a flag so the the task look will stop")

^{:refer lua.nginx.task/task-loop :added "4.0"
  :setup [(!.lua (cache/flush (cache/cache :GLOBAL)))]}
(fact "constructs a task loop"


  (!.lua

   (t/task-register "null" {})

   (t/task-loop
    "null"
    "inst-0"
    {:setup (fn [] (return []))
     :main  (fn [] (n/sleep 0.1))
     :check (fn [] (return true))
     :teardown (fn [])}
    {:hello "world"}))

  (cache/meta-get "task")
  => {"null" {"id" "null", "instances" {}}})

^{:refer lua.nginx.task/task-start :added "4.0"
  :setup [(l/rt:restart)]}
(fact "starts the thread loop"

  ;;
  ;; ONCE OFF
  ;;

  (!.lua

   (t/task-register "test" {})
   (cache/set  (cache/cache :GLOBAL)
            "COUNTER"
            0)

   (t/task-start
    "test"
    {:setup (fn [] (return []))
     :check (fn [] (return true))
     :main  (fn []
              (cache/incr  (cache/cache :GLOBAL)
                        "COUNTER"
                        1)
              #_(n/sleep 0.1))}
    {:hello "world"}))
  => string?

  (-> (!.lua
       (cache/get-all (cache/cache :GLOBAL)))
      (update "__meta__:task" json/read))

  => {"__meta__:task" {"test" {"id" "test", "instances" {}}},
      "COUNTER" 1}


  ;;
  ;; FLAG
  ;;

  (!.lua

   (t/task-register "test" {})
   (cache/set  (cache/cache :GLOBAL)
            "COUNTER"
            0)

   (t/task-start
    "test"
    {:setup (fn [] (return []))
     :main  (fn []
              (cache/incr (cache/cache :GLOBAL)
                          "COUNTER"
                          1)
              (n/sleep 0.1))}
    {:hello "world"}))
  => string?

  (Thread/sleep 1000)

  (!.lua
   (cache/get-all (cache/cache :GLOBAL)))
  => (contains {"COUNTER" #(<= 5 %)})


  ;; signals to the task to stop
  (apply t/task-signal-stop "test"
         (t/task-list "test"))
  => true)

(comment
  (./import))
