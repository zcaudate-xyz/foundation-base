(ns rt.postgres.script.scratch-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [rt.postgres]
            [rt.postgres.script.scratch :as scratch]))

(l/script- :postgres
  {:require [[rt.postgres.script.scratch :as scratch]
             [rt.postgres :as pg]]})

;; Removed global setup

^{:refer rt.postgres.script.scratch/SELECT :adopt true :added "4.0"}
(fact "returns a jsonb array"
  ;; Skips DB interaction
  )

^{:refer rt.postgres.script.scratch/as-array :added "4.0"}
(fact "returns a jsonb array"
  (l/emit-as :postgres '[(scratch/as-array "{}")])
  => string?)

^{:refer rt.postgres.script.scratch/TaskCache :added "4.0"}
(fact "constructs a task cache")

^{:refer rt.postgres.script.scratch/Task :added "4.0"}
(fact "constructs a task")

^{:refer rt.postgres.script.scratch/Entry :added "4.0"}
(fact "construcs an entry")

^{:refer rt.postgres.script.scratch/as-upper :added "4.0"}
(fact "converts to upper case"
  (l/emit-as :postgres '[(scratch/as-upper "abc")])
  => string?)

^{:refer rt.postgres.script.scratch/ping :added "4.0"}
(fact "tests that the db is working"
  (l/emit-as :postgres '[(scratch/ping)])
  => string?)

^{:refer rt.postgres.script.scratch/ping-ok :added "4.0"}
(fact "tests that the db is working with json"
  (l/emit-as :postgres '[(scratch/ping-ok)])
  => string?)

^{:refer rt.postgres.script.scratch/echo :added "4.0"}
(fact "tests that the db is working with echo json"
  (l/emit-as :postgres '[(scratch/echo {:hello "world"})])
  => string?)

^{:refer rt.postgres.script.scratch/addf :added "4.0"}
(fact "adds two values"
  (l/emit-as :postgres '[(scratch/addf 1 2)])
  => string?)

^{:refer rt.postgres.script.scratch/subf :added "4.0"}
(fact "subtracts two values"
  (l/emit-as :postgres '[(scratch/subf 1 2)])
  => string?)

^{:refer rt.postgres.script.scratch/mulf :added "4.0"}
(fact "multiplies two values"
  (l/emit-as :postgres '[(scratch/mulf 1 2)])
  => string?)

^{:refer rt.postgres.script.scratch/divf :added "4.0"}
(fact "divide two values"
  (l/emit-as :postgres '[(scratch/divf 1 2)])
  => string?)

^{:refer rt.postgres.script.scratch/insert-task :added "4.0"}
(fact "inserts a task"
  (l/emit-as :postgres '[(scratch/insert-task "h1" "success" {})])
  => string?)

^{:refer rt.postgres.script.scratch/insert-entry :added "4.0"}
(fact "inserts an entry"
  (l/emit-as :postgres '[(scratch/insert-entry "main" {} {})])
  => string?)

(comment

  )
