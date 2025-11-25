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
