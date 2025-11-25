(ns std.task-test
  (:use code.test)
  (:require [std.task :refer :all]
            [jvm.namespace :as namespace]))

^{:refer std.task/task-defaults :added "3.0"}
(fact "creates default settings for task groups"

  (task-defaults :default)
  => {:main {:arglists '([] [entry])}})

^{:refer std.task/map->Task :added "3.0" :adopt true}
(fact "constructs a invokable Task object"
  (-> (map->Task {:type :namespace
                  :name "list-interns"
                  :main {:fn clojure.core/ns-interns}})
      (task?))
  => true)

^{:refer std.task/task-status :added "3.0"}
(fact "displays the task-status"
  (-> (task :namespace "list-interns" ns-interns)
      (task-status))
  => :namespace)

^{:refer std.task/task-info :added "3.0"}
(fact "displays the task-body"
  (-> (task :namespace "list-interns" ns-interns)
      (task-info))
  => '{:fn list-interns})

^{:refer std.task/single-function-print :added "3.0"}
(fact "if not `:bulk`, then print function output"

  (single-function-print {})
  => {:print {:function true}})

^{:refer std.task/task :added "3.0"}
(fact "creates a task"

  (task? (task :namespace "list-interns" ns-interns))
  => true

  (task? (task :namespace
        "list-interns"
        {:main {:fn clojure.core/ns-interns}}))
  => true)

^{:refer std.task/task? :added "3.0"}
(fact "check if object is a task"

  (-> (task :namespace "list-interns" ns-interns)
      (task?))
  => true)

^{:refer std.task/invoke-intern-task :added "3.0"}
(fact "creates a form defining a task"

  (invoke-intern-task '-task- '{:template :namespace
                                :main {:fn clojure.core/ns-aliases}}) ^:hidden
  => '(def -task-
        (std.task/task :namespace "-task-"
                       {:template :namespace, :main {:fn clojure.core/ns-aliases}})))

^{:refer std.task/deftask :added "3.0"}
(comment "defines a top level task"

  (deftask -list-aliases-
    {:template :namespace
     :main clojure.core/ns-aliases
     :item {:post (comp vec sort keys)}
     :doc  "returns all aliases"}))

(comment

  (deftask -inc-
    {:template :default
     :main {:fn clojure.core/inc}}))
