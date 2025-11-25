(ns std.scheduler.spawn-test
  (:use code.test)
  (:require [std.scheduler.spawn :refer :all]
            [std.scheduler.common :as common]
            [std.lib :as h]))

(def +program+
  {:type :basic
   :main-fn (fn [& args])
   :create-fn (fn [] "hello")})

(fact "gets the spawn status"
  (let [spawn (create-spawn +program+ "spawn.1")]
    (spawn-status spawn))
  => :created)

(fact "gets the spawn info"
  (let [spawn (create-spawn +program+ "spawn.1")]
    (spawn-info spawn))
  => (contains {:id "spawn.1"
                :status :created}))

(fact "checks if spawn exists"
  (let [spawn (create-spawn +program+ "spawn.1")]
    (spawn? spawn))
  => true)

(fact "sends a result"
  (let [spawn (create-spawn +program+ "spawn.1")]
    (send-result spawn "hello")
    (deref (:output spawn)))
  => "hello")

(fact "creates a basic handler"
  (let [spawn (create-spawn +program+ "spawn.1")]
    (create-handler-basic +program+ spawn))
  => fn?)

(fact "creates a constant handler"
  (let [spawn (create-spawn +program+ "spawn.1")]
    (create-handler-constant +program+ spawn))
  => fn?)

(fact "wraps a schedule"
  (let [spawn (create-spawn +program+ "spawn.1")]
    (wrap-schedule +program+ spawn (fn [& args])))
  => fn?)

(fact "stops a spawn"
  (let [runtime (atom (common/new-runtime))
        spawn (create-spawn +program+ "spawn.1")]
    (swap! runtime assoc-in [:running :test "spawn.1"] spawn)
    (stop-spawn runtime :test "spawn.1")
    (get-in @runtime [:past :test]))
  => (just [spawn?]))

(fact "clears all spawns"
  (let [runtime (atom (common/new-runtime))]
    (clear runtime :test)
    (get-in @runtime [:programs :test]))
  => nil)

(fact "saves past spawns"
  (let [runtime (atom (common/new-runtime))
        spawn (create-spawn +program+ "spawn.1")]
    (spawn-save-past runtime :test spawn)
    (get-in @runtime [:past :test]))
  => (just [spawn?]))


^{:refer std.scheduler.spawn/spawn-status :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/spawn-info :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/spawn? :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/create-spawn :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/set-props :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/get-props :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/get-job :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/update-job :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/remove-job :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/add-job :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/list-jobs :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/list-job-ids :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/count-jobs :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/send-result :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/handler-run :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/create-handler-basic :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/create-handler-constant :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/create-handler :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/schedule-timing :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/wrap-schedule :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/spawn-save-past :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/spawn-loop :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/run :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/get-all-spawn :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/get-spawn :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/count-spawn :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/list-spawn :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/latest-spawn :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/earliest-spawn :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/list-stopped :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/latest-stopped :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/latest :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/stop-spawn :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/kill-spawn :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/stop-all :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/kill-all :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/clear :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/get-state :added "4.0"}
(fact "TODO")

^{:refer std.scheduler.spawn/get-program :added "4.0"}
(fact "TODO")