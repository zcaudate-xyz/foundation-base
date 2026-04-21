(ns rt.postgres.runtime.addon-test
  (:require [rt.postgres :as pg]
            [rt.postgres.runtime.addon :as addon]
            [rt.postgres.test.scratch-v1 :as scratch]
            [std.lang :as l])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[rt.postgres :as pg]
             [rt.postgres.test.scratch-v1 :as scratch]]
   :import [["pgcrypto"]]})

;; Removed global setup to avoid connection attempt
(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)
             (rt.postgres/exec [:create-schema :if-not-exists :scratch])]
  :teardown [(l/rt:stop)]})

^{:refer rt.postgres.runtime.addon/exec :added "4.0"}
(fact "executes an sql statement"

  (addon/exec [:select 1])
  => 1)

^{:refer rt.postgres.runtime.addon/ARRAY :added "4.0"}
(fact "creates an array"

  (addon/ARRAY 1 2 3 4)
  => '(1 2 3 4))

^{:refer rt.postgres.runtime.addon/id :added "4.0"}
(fact "gets the id of an object"

  (str (addon/id {:id "40ff7565-2f3d-4ac0-acb5-3527370eb646"}))
  => "40ff7565-2f3d-4ac0-acb5-3527370eb646")

^{:refer rt.postgres.runtime.addon/full :added "4.0"}
(fact "gets the full jsonb for table or function"

  (addon/full scratch/Task)
  => ["scratch" "Task"])

^{:refer rt.postgres.runtime.addon/full-str :added "4.0"}
(fact "gets the full json str form table or function"

  (addon/full-str scratch/Task)
  => "[\"scratch\",\"Task\"]")

^{:refer rt.postgres.runtime.addon/rand-hex :added "4.0"}
(fact "generates random hex"

  (str (addon/rand-hex 10))
  ;; "678d6e26a1"
  => string?)

^{:refer rt.postgres.runtime.addon/sha1 :added "4.0"}
(fact "calculates the sha1"

  (addon/sha1 "hello")
  => (any nil
          "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d"))

^{:refer rt.postgres.runtime.addon/client-list :added "4.0"}
(fact "gets the client list for pg"

  (count (addon/client-list))
  => pos?)

^{:refer rt.postgres.runtime.addon/time-ms :added "4.0"}
(fact "returns the time in ms"

  (l/emit-as :postgres '[(rt.postgres/time-ms)])
  => string?)

^{:refer rt.postgres.runtime.addon/time-us :added "4.0"}
(fact "returns the time in us"

  (l/emit-as :postgres '[(rt.postgres/time-us)])
  => string?)

^{:refer rt.postgres.runtime.addon/throw :added "4.0"
  :setup [(l/rt:stop)]}
(fact "raises a json exception"

  (addon/throw {:a 1})
  => "RAISE EXCEPTION USING DETAIL = (jsonb_build_object('a',1))::TEXT,MESSAGE = 'nil'")

^{:refer rt.postgres.runtime.addon/error :added "4.0"}
(fact "raises a json error with value"

  (addon/error [1 2 3 4])
  => "RAISE EXCEPTION USING\n  DETAIL = (jsonb_build_object('status','error','value',jsonb_build_array(1,2,3,4)))::TEXT,\n  MESSAGE = 'nil'\n")

^{:refer rt.postgres.runtime.addon/assert :added "4.0"}
(fact "asserts given a block"

  (addon/assert '(exists 1) [:wrong {}])
  => string?)

^{:refer rt.postgres.runtime.addon/case :added "4.0"}
(fact "builds a case form"

  (pg/case 1 2 3 4)
  => "CASE WHEN 1 THEN 2\nWHEN 3 THEN 4\nEND"

  ((:template @pg/case) 1 2 3 4)
  => '(% [:case :when (:% 1) :then (:% 2)
          \\ :when (:% 3) :then (:% 4)
          \\ :end]))

^{:refer rt.postgres.runtime.addon/field-id :added "4.0"}
(fact "shorthand for getting the field-id for a linked map"

  (l/emit-as :postgres '[(rt.postgres/field-id m :field)])
  => "coalesce(m ->> ':field_id',(m -> :field) ->> 'id')")

^{:refer rt.postgres.runtime.addon/map:rel :added "4.0"}
(fact "basic map across relation"

  (l/emit-as :postgres '[(rt.postgres/map:rel f rel)])
  => "jsonb_agg(f(o_ret)) FROM rel AS o_ret")

^{:refer rt.postgres.runtime.addon/map:js :added "4.0"}
(fact "basic map across json"

  (l/emit-as :postgres '[(rt.postgres/map:js f arr)])
  => "coalesce(jsonb_agg(f(o_ret)),jsonb_build_array())\nFROM jsonb_array_elements(arr) AS o_ret")

^{:refer rt.postgres.runtime.addon/do:reduce :added "4.0"}
(fact "basic reduce macro"

  (l/emit-as :postgres '[(rt.postgres/do:reduce out f :type arr)])
  => string?)

^{:refer rt.postgres.runtime.addon/b:select :added "4.0"
  :setup [(l/rt:restart)]}
(fact "basic select macro"

  (pg/b:select 1)
  => 1)

^{:refer rt.postgres.runtime.addon/ret :added "4.0"}
(fact "returns a value alias for select"

  (pg/ret 1)
  => 1)

^{:refer rt.postgres.runtime.addon/b:update :added "4.0"}
(fact "update macro"

  (l/emit-as :postgres '[(rt.postgres/b:update t {:a 1})])
  => "UPDATE t \"a\" = 1")

^{:refer rt.postgres.runtime.addon/b:insert :added "4.0"}
(fact "insert macro"

  (l/emit-as :postgres '[(rt.postgres/b:insert t {:a 1})])
  => "INSERT t \"a\" = 1")

^{:refer rt.postgres.runtime.addon/b:delete :added "4.0"}
(fact "delete macro"

  (l/emit-as :postgres '[(rt.postgres/b:delete t)])
  => "DELETE t")

^{:refer rt.postgres.runtime.addon/perform :added "4.0"}
(fact "perform macro"

  (l/emit-as :postgres '[(rt.postgres/perform 1)])
  => "PERFORM 1")

^{:refer rt.postgres.runtime.addon/random-enum :added "4.0"}
(fact "gets random enum"

  (keyword (addon/random-enum scratch/EnumStatus))
  => #{:pending :error :success})

^{:refer rt.postgres.runtime.addon/do:plpgsql :added "4.0"}
(fact "creates a do block"

  (pg/do:plpgsql
   '(let [(:integer a) 1
          (:integer b) 1]
      (:= a (+ a b))))
  => 0)

^{:refer rt.postgres.runtime.addon/name :added "4.0"}
(fact "gets the name of a table"

  (pg/name scratch/Task)
  => "Task")

^{:refer rt.postgres.runtime.addon/coord :added "4.0"}
(fact "gets the coordinate of a row"

  (addon/coord scratch/Task "id")
  => ["scratch" "Task" "id"])

^{:refer rt.postgres.runtime.addon/get-stack-diagnostics :added "4.0"}
(fact "gets the stack diagnostics"

  (l/emit-as :postgres '[(rt.postgres/get-stack-diagnostics)])
  => string?)

^{:refer rt.postgres.runtime.addon/map:js-text :added "4.0"}
(fact "maps across json"

  (l/emit-as :postgres '[(rt.postgres/map:js-text f arr)])
  => "coalesce(jsonb_agg(f(o_ret)),jsonb_build_array())\nFROM jsonb_array_elements_text(arr) AS o_ret")

(comment
  (./import))
