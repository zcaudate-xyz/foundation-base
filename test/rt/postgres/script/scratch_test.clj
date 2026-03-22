(ns rt.postgres.script.test.scratch-v1-test
  (:require [rt.postgres]
            [rt.postgres.script.test.scratch-v1 :as scratch]
            [std.lang :as l])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[rt.postgres.script.test.scratch-v1 :as scratch]
             [rt.postgres :as pg]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer rt.postgres.script.test.scratch-v1/as-array :added "4.0"}
(fact "returns a jsonb array"
  ^:hidden
  
  (l/emit-as :postgres `[(scratch/as-array "{}")])
  => string?)

^{:refer rt.postgres.script.test.scratch-v1/TaskCache :added "4.0"}
(fact "constructs a task cache")

^{:refer rt.postgres.script.test.scratch-v1/Task :added "4.0"}
(fact "constructs a task")

^{:refer rt.postgres.script.test.scratch-v1/Entry :added "4.0"}
(fact "construcs an entry")

^{:refer rt.postgres.script.test.scratch-v1/as-upper :added "4.0"}
(fact "converts to upper case"
  ^:hidden
  
  (l/emit-as :postgres `[(scratch/as-upper "abc")])
  => "\"scratch\".as_upper('abc')")

^{:refer rt.postgres.script.test.scratch-v1/ping :added "4.0"}
(fact "tests that the db is working"
  ^:hidden
  
  (l/emit-as :postgres `[(scratch/ping)])
  => "\"scratch\".ping()")

^{:refer rt.postgres.script.test.scratch-v1/ping-ok :added "4.0"}
(fact "tests that the db is working with json"
  ^:hidden
  
  (l/emit-as :postgres `[(scratch/ping-ok)])
  => "\"scratch\".ping_ok()")

^{:refer rt.postgres.script.test.scratch-v1/echo :added "4.0"}
(fact "tests that the db is working with echo json"
  ^:hidden
  
  (l/emit-as :postgres `[(scratch/echo {:hello "world"})])
  => "\"scratch\".echo(jsonb_build_object('hello','world'))")

^{:refer rt.postgres.script.test.scratch-v1/addf :added "4.0"}
(fact "adds two values"
  ^:hidden
  
  (l/emit-as :postgres `[(scratch/addf 1 2)])
  => "\"scratch\".addf(1,2)")

^{:refer rt.postgres.script.test.scratch-v1/subf :added "4.0"}
(fact "subtracts two values"
  ^:hidden
  
  (l/emit-as :postgres `[(scratch/subf 1 2)])
  => "\"scratch\".subf(1,2)")

^{:refer rt.postgres.script.test.scratch-v1/mulf :added "4.0"}
(fact "multiplies two values"
  ^:hidden
  
  (l/emit-as :postgres `[(scratch/mulf 1 2)])
  => "\"scratch\".mulf(1,2)")

^{:refer rt.postgres.script.test.scratch-v1/divf :added "4.0"}
(fact "divide two values"
  ^:hidden
  
  (l/emit-as :postgres `[(scratch/divf 1 2)])
  => "\"scratch\".divf(1,2)")

^{:refer rt.postgres.script.test.scratch-v1/insert-task :added "4.0"}
(fact "inserts a task"
  ^:hidden
  
  (l/emit-as :postgres `[(scratch/insert-task "h1" "success" {})])
  => "\"scratch\".insert_task('h1','success',jsonb_build_object())")

^{:refer rt.postgres.script.test.scratch-v1/insert-entry :added "4.0"}
(fact "inserts an entry"
  ^:hidden
  
  (l/emit-as :postgres `[(scratch/insert-entry "main" {} {})])
  => "\"scratch\".insert_entry('main',jsonb_build_object(),jsonb_build_object())")

(comment

  )
