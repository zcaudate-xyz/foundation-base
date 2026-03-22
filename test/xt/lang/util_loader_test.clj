(ns xt.lang.util-loader-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.util-loader :as loader]
             [js.core :as j]]})

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.util-loader :as loader]
             [lua.nginx :as n]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-loader/new-task :added "4.0"}
(fact "creates a new task"
  ^:hidden
  
  (set
   (!.js
    (k/obj-keys
     (loader/new-task
      "A" [] []
      {:load-fn (fn []
                  (return "A"))}))))
  => #{"args" "id" "load_fn" "deps" "::"}

  (set
   (!.lua
    (k/obj-keys
     (loader/new-task
      "A" [] []
      {:load-fn (fn []
                  (return "A"))}))))
  => #{"args" "id" "load_fn" "deps" "::"})

^{:refer xt.lang.util-loader/task-load :added "4.0"}
(fact "loads a task"
  ^:hidden
  
  (!.js
   (loader/task-load
    (loader/new-task
     "A" [] []
     {:load-fn (fn []
                 (return "A"))})))
  => "A"

  (!.lua
   (loader/task-load
    (loader/new-task
     "A" [] []
     {:load-fn (fn []
                 (return "A"))})))
  => "A"

  (!.js
   (loader/task-load
    (loader/new-task
     "A" [] []
     {:get-fn  (fn:> "B")
      :check-fn (fn:> [res] (== "B" res))
      :load-fn (fn:> "A")})))
  => "B"

  (!.lua
   (loader/task-load
    (loader/new-task
     "A" [] []
     {:get-fn  (fn:> "B")
      :check-fn (fn:> [res] (== "B" res))
      :load-fn (fn:> "A")})))
  => "B")

^{:refer xt.lang.util-loader/task-unload :added "4.0"}
(fact "unloads a task"
  ^:hidden
  
  (!.lua
   (loader/task-unload
    (loader/new-task
     "A" [] []
     {:get-fn  (fn:> "B")
      :check-fn (fn:> [res] (== "B" res))
      :unload-fn (fn:> "A")})))
  => true

  (!.lua
   (loader/task-unload
    (loader/new-task
     "A" [] []
     {:get-fn  (fn:> "B")
      :check-fn (fn:> [res] false)
      :unload-fn (fn:> "A")})))
  => false)

^{:refer xt.lang.util-loader/new-loader-blank :added "4.0"}
(fact "creates a blank loader")

^{:refer xt.lang.util-loader/add-tasks :added "4.0"}
(fact "add tasks to a loader")

^{:refer xt.lang.util-loader/new-loader :added "4.0"}
(fact "creates a new loader"
  ^:hidden
  
  (!.js
   (loader/new-loader [(loader/new-task
                        "A" [] []
                        {:load-fn (fn []
                                    (return "A"))})
                       (loader/new-task
                        "B" ["A"] []
                        {:load-fn (fn []
                                    (return "B"))})
                       (loader/new-task
                        "C" ["B"] []
                        {:load-fn (fn []
                                    (return "C"))})]))
  => {"tasks"
      {"C" {"args" [], "id" "C", "deps" ["B"], "::" "loader.task"},
       "B" {"args" [], "id" "B", "deps" ["A"], "::" "loader.task"},
       "A" {"args" [], "id" "A", "deps" [], "::" "loader.task"}},
      "errored" nil,
      "completed" {},
      "loading" {},
      "order" ["A" "B" "C"],
      "::" "loader"}
  
  (!.lua
   (k/get-spec
    (loader/new-loader [(loader/new-task
                        "A" [] []
                        {:load-fn (fn []
                                    (return "A"))})
                        (loader/new-task
                         "B" ["A"] []
                         {:load-fn (fn []
                                     (return "B"))})
                        (loader/new-task
                         "C" ["B"] []
                         {:load-fn (fn []
                                     (return "C"))})])))
  => {"tasks"
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

^{:refer xt.lang.util-loader/list-loading :added "4.0"}
(fact "lists all loading ids")

^{:refer xt.lang.util-loader/list-completed :added "4.0"}
(fact "lists all completed ids")

^{:refer xt.lang.util-loader/list-incomplete :added "4.0"}
(fact "lists incomplete tasks"
  ^:hidden
  
  (set (!.js
        (loader/list-incomplete
         (loader/new-loader [(loader/new-task
                              "A" [] []
                              {:load-fn (fn []
                                          (return "A"))})
                             (loader/new-task
                              "B" ["A"] []
                              {:load-fn (fn []
                                          (return "B"))})
                             (loader/new-task
                              "C" ["B"] []
                              {:load-fn (fn []
                                          (return "C"))})]))))
  => #{"C" "B" "A"}

  (set (!.lua
        (loader/list-incomplete
         (loader/new-loader [(loader/new-task
                              "A" [] []
                              {:load-fn (fn []
                                          (return "A"))})
                             (loader/new-task
                              "B" ["A"] []
                              {:load-fn (fn []
                                          (return "B"))})
                             (loader/new-task
                              "C" ["B"] []
                              {:load-fn (fn []
                                          (return "C"))})]))))
  => #{"C" "B" "A"})

^{:refer xt.lang.util-loader/list-waiting :added "4.0"}
(fact "lists all waiting ids")

^{:refer xt.lang.util-loader/load-tasks-single :added "4.0"}
(fact "loads a single task"
  ^:hidden

  (notify/wait-on :js
    (var loader (loader/new-loader [(loader/new-task
                                     "A" [] []
                                     {:load-fn (fn []
                                                 (return "A"))})]))
    (loader/load-tasks-single loader "A"
                              (fn [id done]
                                (repl/notify [id done]))))
  => ["A" true]


  (notify/wait-on :js
    (var loader (loader/new-loader [(loader/new-task
                                     "A" [] []
                                     {:get-fn  (fn:> "B")
                                      :check-fn (fn:> [res] (== "B" res))
                                      :load-fn (fn:> "A")})]))
    (loader/load-tasks-single loader "A"
                              (fn [id done]
                                (repl/notify [id done]))))
  => ["A" true]

  
  (notify/wait-on :lua
    (var loader (loader/new-loader [(loader/new-task
                                     "A" [] []
                                     {:load-fn (fn []
                                                  (return "A"))})]))
    (loader/load-tasks-single loader "A"
                               (fn [id done]
                                 (repl/notify [id done]))))
  => ["A" true])

^{:refer xt.lang.util-loader/load-tasks :added "4.0"}
(fact "load tasks"
  ^:hidden
  
  ;; NO SLEEP
  
  (set
   (notify/wait-on :js
     (var loader (loader/new-loader [(loader/new-task
                                      "A" [] []
                                      {:load-fn (fn []
                                                  (return "A"))})
                                     (loader/new-task
                                      "B" ["A"] []
                                      {:load-fn (fn []
                                                  (return "B"))})
                                     (loader/new-task
                                      "C" ["B"] []
                                      {:load-fn (fn []
                                                  (return "C"))})] ))
     (loader/load-tasks loader
                        nil
                        (fn []
                          (repl/notify
                           (loader/list-completed loader))))))
  => #{"C" "B" "A"}
  
  (set 
   (notify/wait-on :lua
     (var loader (loader/new-loader [(loader/new-task
                                      "A" [] []
                                      {:load-fn (fn []
                                                  (return "A"))})
                                     (loader/new-task
                                      "B" ["A"] []
                                      {:load-fn (fn []
                                                  (return "B"))})
                                     (loader/new-task
                                      "C" ["B"] []
                                      {:load-fn (fn []
                                                  (return "C"))})]))
     (loader/load-tasks loader
                        nil
                        (fn []
                          (repl/notify
                           (loader/list-completed loader))))))
  => #{"C" "B" "A"}

  
  ;; WITH SLEEP
  
  (set
   (notify/wait-on :js
         (:= (!:G loader) (loader/new-loader
                           [(loader/new-task
                             "A" [] []
                             {:load-fn (fn []
                                         (return (j/future-delayed [100]
                                                   (return "A"))))})
                            (loader/new-task
                             "B" ["A"] []
                             {:load-fn (fn []
                                         (return (j/future-delayed [100]
                                                   (return "B"))))})
                            (loader/new-task
                             "C" ["B"] []
                             {:load-fn (fn []
                                         (return (j/future-delayed [100]
                                                   (return "C"))))})]))
         (loader/load-tasks loader
                            nil
                            (fn []
                              (repl/notify
                               (loader/list-completed loader))))))
  => #{"C" "B" "A"}
  
  (set
   (notify/wait-on :lua
     (:= (!:G loader) (loader/new-loader
                       [(loader/new-task
                              "A" [] []
                              {:load-fn (fn []
                                          (n/sleep 0.1)
                                          (return "A"))})
                             (loader/new-task
                              "B" ["A"] []
                              {:load-fn (fn []
                                          (n/sleep 0.1)
                                          (return "B"))})
                             (loader/new-task
                              "C" ["B"] []
                              {:load-fn (fn []
                                          (n/sleep 0.1)
                                          (return "C"))})]))
     (loader/load-tasks loader
                            nil
                            (fn []
                              (repl/notify
                               (loader/list-completed loader))))))
  => #{"C" "B" "A"}
  
  ;; WITH ERROR
  
  (notify/wait-on :js
    (:= (!:G loader) (loader/new-loader
                      [(loader/new-task
                        "A" [] []
                        {:load-fn (fn []
                                   (return (j/future-delayed [100]
                                             (return "A"))))})
                       (loader/new-task
                        "B" ["A"] []
                        {:load-fn (fn []
                                    (return (j/future-delayed [100]
                                              (throw "B"))))})
                       (loader/new-task
                        "C" ["B"] []
                        {:load-fn (fn []
                                    (return (j/future-delayed [100]
                                              (return "C"))))})]))
    (loader/load-tasks loader
                       nil
                       (fn []
                         (repl/notify
                          (loader/list-completed loader)))))
  => ["A"]

  (notify/wait-on :lua
    (:= (!:G loader) (loader/new-loader
                       [(loader/new-task
                         "A" [] []
                         {:load-fn (fn []
                                     (n/sleep 0.1)
                                     (return "A"))})
                        (loader/new-task
                         "B" ["A"] []
                         {:load-fn (fn []
                                     (n/sleep 0.1)
                                     (error "B"))})
                        (loader/new-task
                         "C" ["B"] []
                         {:load-fn (fn []
                                     (n/sleep 0.1)
                                     (return "C"))})]))
     (loader/load-tasks loader
                        nil
                        (fn []
                          (repl/notify
                           (loader/list-completed loader)))))
  => ["A"])

^{:refer xt.lang.util-loader/unload-tasks :added "4.0"}
(fact "unload tasks")

^{:refer xt.lang.util-loader/load-tasks.global :adopt true :added "4.0"}
(fact "load tasks"
  ^:hidden
  
  (notify/wait-on :js
    (:= (!:G loader) (loader/new-loader
                       [(loader/new-task
                         "A" [] []
                         {:load-fn (fn []
                                     (return (j/future-delayed [100]
                                               (:= (!:G A) (k/now-ms)))))})
                        (loader/new-task
                         "B" ["A"] []
                         {:load-fn (fn []
                                     (return (j/future-delayed [100]
                                               (:= (!:G B) (k/now-ms)))))})
                        (loader/new-task
                         "C" ["B"] []
                         {:load-fn (fn []
                                     (return (j/future-delayed [100]
                                               (:= (!:G C) (k/now-ms)))))})]))
    (loader/load-tasks loader
                        nil
                        (fn []
                          (repl/notify  [(k/floor (/ (- B A) 100))
                                         (k/floor (/ (- C A) 100))]))))
  => (contains [number? 2]))
