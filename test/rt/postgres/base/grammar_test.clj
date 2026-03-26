(ns rt.postgres.base.grammar-test
  (:use code.test)
  (:require [rt.postgres :as pg]
            [rt.postgres.base.grammar :refer :all]
            [rt.postgres.test.scratch-v1 :as scratch]
            [std.lang :as l]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[rt.postgres :as pg]
             [rt.postgres.system :as sys]
             [rt.postgres.test.scratch-v1 :as scratch]]})

(fact:global
 {:setup    [(l/rt:restart)
             (rt.postgres/exec [:create-schema :if-not-exists :scratch])
             (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer rt.postgres.base.grammar/CANARY :adopt true :added "4.0"}
(fact "stops the postgres runtime"

  (try
    (scratch/addf 1.0 2.0)
    (catch Throwable t
      (spit "/tmp/debug_canary.txt" (str "DEBUG: CANARY failed: " (.getMessage t) "\n") :append true)
      (when (instance? java.sql.SQLException t)
        (spit "/tmp/debug_canary.txt" (str "DEBUG: Next Exception: " (.getNextException t) "\n") :append true))
      (spit "/tmp/debug_canary.txt" (with-out-str (.printStackTrace t)) :append true)
      (throw t)))
  => 3M)

^{:refer rt.postgres.base.grammar/CANARY.select :adopt true :added "4.0"}
(fact "BASIC SELECT"
  ^:hidden

  (!.pg
   [:select (pg/jsonb-agg '("A" "B"))])
  => [{:f1 "A", :f2 "B"}]

  (mapv str
        (!.pg
         [:select (pg/array-agg '("A" "B"))]))
  => ["(A,B)"]

  (!.pg
   [:select * :from '("A" "B")])
  => (throws)

  (!.pg
   '("A" "B"))
  => "(A,B)"

  (!.pg
   (array "A" "B"))
  => '("A" "B")

  (!.pg
   [:select * :from (unnest (array "A" "B"))])
  => '("A" "B")

  (!.pg
   [:select '(a b)
    :from (unnest (array "A" "B")) a
    :cross-join (unnest (array "X" "Y")) b])
  => '("(A,X)" "(A,Y)" "(B,X)" "(B,Y)"))

^{:refer rt.postgres.base.grammar/CANARY.json :adopt true :added "4.0"}
(fact "BASIC JSON SELECT"
  ^:hidden

  (!.pg
   [:select o
    :from '([:select '[[(== c "a") o]]
             :from (pg/jsonb-array-elements-text (js ["a" "b" "c"]))
             c])
    o])
  => '(true false false)

  (!.pg
   [:select true
    :from (pg/jsonb-array-elements-text (js ["a" "b" "c"])) c
    :where c := "a"])
  => true

  (!.pg
   [:select *
    :from (pg/jsonb-array-elements-text (js ["a" "b" "c"])) c
    :where c := "a"])
  => "a"

  (!.pg
   [:select *
    :from (pg/jsonb-array-elements (js [{:id "a"}
                                        {:id "b"}
                                        {:id "c"}])) c
    :where (:->> c "id") := "a"])
  => {:id "a"}

  (set
   (mapv std.json/read
         (!.pg
          [:select :all c
           :from (pg/jsonb-array-elements (js [{:id "a" :data 2}
                                               {:id "b"}
                                               {:id "c"}
                                               {:id "a" :data 1}])) c
           :where (:->> c "id") := "a"])))
  => #{{"id" "a", "data" 2}
       {"id" "a", "data" 1}})

^{:refer rt.postgres.base.grammar/pg-tf-free-data :added "4.1"}
(fact "transforms free data form to quoted structure"
  (pg-tf-free-data ['>-> '[[a b] [c d]]])
  => '(quote ((quote [[a b] [c d]]))))

^{:refer rt.postgres.base.grammar/pg-tf-free-vec :added "4.1"}
(fact "transforms free vec form to quoted vector"
  (pg-tf-free-vec ['--- '[a b c]]) => '(quote [a b c])

  (pg-tf-free-vec ['--- '[1 2 3]]) => '(quote [1 2 3]))

^{:refer rt.postgres.base.grammar/pg-vector :added "4.1"}
(fact "handles array with js meta by emitting through pg-tf-js"
  (pg-vector [1 2 3] {} {})
  => "1 2 3"

  (pg-vector ^:js [1 2 3] {} {})
  => "(jsonb-build-array 1 2 3)")
