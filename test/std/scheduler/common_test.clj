(ns std.scheduler.common-test
  (:use code.test)
  (:require [std.scheduler.common :refer :all]))

(declare -rt-)

^{:refer std.scheduler.common/new-runtime :added "3.0"
  :teardown [(kill-runtime -rt-)]}
(fact "contructs a new runtime for runner"

  (def -rt- (new-runtime)))

^{:refer std.scheduler.common/stop-runtime :added "3.0"}
(fact "stops the executors in the new instance"

  (stop-runtime (new-runtime)))

^{:refer std.scheduler.common/kill-runtime :added "3.0"}
(fact "kills all objects in the runtime"

  (kill-runtime (new-runtime)))

^{:refer std.scheduler.common/all-ids :added "3.0"}
(fact "returns all running program ids"
  (all-ids (fn [r id] (str "program-" id))
           {:runtime (atom {:running {:p1 {} :p2 {}}})})
  => {:p1 "program-p1" :p2 "program-p2"})

(defn -dummy-spawn- [runtime arg]
  (str "spawned-" arg))

^{:refer std.scheduler.common/spawn-form :added "3.0"}
(fact "generate a spawn/runtime form"
  (eval (spawn-form '-dummy- `-dummy-spawn-))
  (-dummy- {:runtime "rt"} "foo")
  => "spawned-foo")

^{:refer std.scheduler.common/gen-spawn :added "3.0"}
(fact "generates a spawn/runtime forms"
  (eval (gen-spawn [-dummy-gen- -dummy-spawn-]))
  (-dummy-gen- {:runtime "rt"} "bar")
  => "spawned-bar")

(defn -dummy-spawn-all- [runtime id]
  (str "all-" id))

^{:refer std.scheduler.common/spawn-all-form :added "3.0"}
(fact "generates all forms"
  (eval (spawn-all-form '-dummy-all- `-dummy-spawn-all-))
  (-dummy-all- {:runtime (atom {:running {:p1 {} :p2 {}}})})
  => {:p1 "all-p1" :p2 "all-p2"})

^{:refer std.scheduler.common/gen-spawn-all :added "3.0"}
(fact "generates all spawn/runiime forms"
  (eval (gen-spawn-all [-dummy-gen-all- -dummy-spawn-all-]))
  (-dummy-gen-all- {:runtime (atom {:running {:p1 {} :p2 {}}})})
  => {:p1 "all-p1" :p2 "all-p2"})
