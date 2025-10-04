(ns rt.postgres.script.scratch-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [rt.postgres]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"
             :temp :create}
   :require [[rt.postgres.script.scratch :as scratch]
             [rt.postgres :as pg]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup-to :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer rt.postgres.script.scratch/SELECT :adopt true :added "4.0"
  :setup [(pg/t:delete scratch/Entry)
          (doseq [i (range 110)]
            (pg/t:insert scratch/Entry
              {:name (str "A-" i)
               :tags '(js ["A"])}
              {:track {}}))
          
          (doseq [i (range 100)]
            (pg/t:insert scratch/Entry
              {:name (str "B-" i)
               :tags '(js ["B"])}
              {:track {}}))]}
(fact "returns a jsonb array"
  ^:hidden

  (pg/t:select scratch/Entry
    {:returning #{:name}
     :order-by [:name]
     :limit 5
     :offset 0
     :as :raw})
  => '("A-0" "A-1" "A-10" "A-100" "A-101")
  
  (pg/t:select scratch/Entry
    {:returning #{:name}
     :order-by [:name]
     :limit 5
     :offset 5
     :as :raw})
  => '("A-102" "A-103" "A-104" "A-105" "A-106")

  (pg/t:select scratch/Entry
    {:returning #{:name}
     :order-by [:name]
     :order-sort :desc
     :limit 5
     :offset 0
     :as :raw})
  => '("B-99" "B-98" "B-97" "B-96" "B-95"))

^{:refer rt.postgres.script.scratch/as-array :added "4.0"}
(fact "returns a jsonb array"
  ^:hidden
  
  (scratch/as-array {})
  => [])

^{:refer rt.postgres.script.scratch/TaskCache :added "4.0"}
(fact "constructs a task cache")

^{:refer rt.postgres.script.scratch/Task :added "4.0"}
(fact "constructs a task")

^{:refer rt.postgres.script.scratch/Entry :added "4.0"}
(fact "construcs an entry")

^{:refer rt.postgres.script.scratch/as-upper :added "4.0"}
(fact "converts to upper case")

^{:refer rt.postgres.script.scratch/ping :added "4.0"}
(fact "tests that the db is working"
  ^:hidden
  
  (scratch/ping)
  => "pong")

^{:refer rt.postgres.script.scratch/ping-ok :added "4.0"}
(fact "tests that the db is working with json"
  ^:hidden
  
  (scratch/ping-ok)
  => {:reply "ok"})

^{:refer rt.postgres.script.scratch/echo :added "4.0"}
(fact "tests that the db is working with echo json"
  ^:hidden
  
  (scratch/echo {:hello "world"})
  => {:hello "world"})

^{:refer rt.postgres.script.scratch/addf :added "4.0"}
(fact "adds two values"
  ^:hidden
  
  (scratch/addf 1 2)
  => 3M)

^{:refer rt.postgres.script.scratch/subf :added "4.0"}
(fact "subtracts two values"
  ^:hidden
  
  (scratch/subf 1 2)
  => -1M)

^{:refer rt.postgres.script.scratch/mulf :added "4.0"}
(fact "multiplies two values"
  ^:hidden
  
  (scratch/mulf 1 2)
  => 2M)

^{:refer rt.postgres.script.scratch/divf :added "4.0"}
(fact "divide two values"
  ^:hidden
  
  (scratch/divf 1 2)
  => 0.50000000000000000000M)

^{:refer rt.postgres.script.scratch/insert-task :added "4.0"}
(fact "inserts a task"
  ^:hidden
  
  (scratch/insert-task "h1" "success" {})
  => map?)

^{:refer rt.postgres.script.scratch/insert-entry :added "4.0"}
(fact "inserts an entry"
  ^:hidden
  
  (scratch/insert-entry "main" {} {})
  => map?)

(comment

  
  )
