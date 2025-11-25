(ns std.scheduler.common-test
  (:use code.test)
  (:require [std.scheduler.common :refer :all]
            [std.scheduler :as scheduler]
            [std.scheduler.spawn :as spawn]))

(fact "creates a new runtime"
  (new-runtime)
  => (contains {:running {}
                :programs {}
                :executors anything}))

(fact "stops the runtime"
  (let [runtime (new-runtime)]
    (stop-runtime runtime)
    (and (.isShutdown (:scheduler runtime))
         (.isShutdown (:core runtime))))
  => true)

(fact "kills the runtime"
  (let [runtime (new-runtime)]
    (kill-runtime runtime)
    (and (.isTerminated (:scheduler runtime))
         (.isTerminated (:core runtime))))
  => true)

(fact "lists all ids"
  (let [runner (scheduler/runner:create)]
    (all-ids (fn [runner id] id) runner))
  => {})

(fact "creates a spawn form"
  (first (spawn-form '[my-spawn scheduler/spawn]))
  => `defn)

(fact "generates a spawn function"
  (eval '(do (gen-spawn [my-spawn scheduler/spawn])
             (var my-spawn)))
  => var?)

(fact "creates a spawn-all form"
  (first (spawn-all-form '[my-stop-all spawn/stop-all]))
  => `defn)

(fact "generates a spawn-all function"
  (eval '(do (gen-spawn-all [my-stop-all spawn/stop-all])
             (var my-stop-all)))
  => var?)
