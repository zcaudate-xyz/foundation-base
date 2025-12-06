(ns std.pipe-test
  (:use code.test)
  (:require [std.pipe :refer :all]))

^{:refer std.pipe/pipe-defaults :added "4.0"}
(fact "creates default settings for pipe task groups"
  ^:hidden
  (pipe-defaults :default)
  => {:main {:arglists '([] [entry])}})

^{:refer std.pipe/task-status :added "3.0"}
(fact "displays the task-status"
  ^:hidden
  (task-status (task {}))
  => nil)

^{:refer std.pipe/task-info :added "3.0"}
(fact "displays the task-body"
  ^:hidden
  (task-info (task {:name "hello"}))
  => {:fn 'hello})

^{:refer std.pipe/chain :added "4.0"}
(fact "chains multiple tasks together"

  ((task [(task {:main {:fn inc :argcount 1}})
          (task {:main {:fn str :argcount 1}})])
   1)
  => "2")

^{:refer std.pipe/task :added "4.0"}
(fact "creates a pipe task"

  (def +task+ (task {:main {:fn (fn [x] (inc x)) :argcount 1}}))

  (+task+ 1) => 2)

^{:refer std.pipe/invoke-intern-pipe :added "4.0"}
(fact "creates a pipe task"
  ^:hidden
  
  (invoke-intern-pipe nil '-task- {:main {:fn 'inc :argcount 1}} nil)
  => '(def -task- (clojure.core/with-meta
                    (std.pipe/task nil "-task-" {:main {:fn inc, :argcount 1}})
                    {:doc nil, :arglists (quote ([& args]))})))
