(ns rt.postgres.script.addon-test
  (:use code.test)
  (:require [rt.postgres.script.addon :as addon]
            [rt.postgres.script.scratch :as scratch]
            [rt.postgres :as pg]
            [std.lang :as l]
            [std.lib :as h]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[rt.postgres :as pg]
             [rt.postgres.script.scratch :as scratch]]
   :import [["pgcrypto"]]})

;; Removed global setup to avoid connection attempt
(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)
             (rt.postgres/exec [:create-schema :if-not-exists :scratch])]
  :teardown [(l/rt:stop)]})

^{:refer rt.postgres.script.addon/exec :added "4.0"}
(fact "executes an sql statement"
  ^:hidden

  (addon/exec [:select 1])
  => 1)

^{:refer rt.postgres.script.addon/ARRAY :added "4.0"}
(fact "creates an array"
  ^:hidden
  
  (addon/ARRAY 1 2 3 4)
  => '(1 2 3 4))

^{:refer rt.postgres.script.addon/id :added "4.0"}
(fact "gets the id of an object"
  ^:hidden
  
  (str (addon/id {:id "40ff7565-2f3d-4ac0-acb5-3527370eb646"}))
  => "40ff7565-2f3d-4ac0-acb5-3527370eb646")

^{:refer rt.postgres.script.addon/full :added "4.0"}
(fact "gets the full jsonb for table or function"
  ^:hidden

  (addon/full scratch/Task)
  => ["scratch" "Task"])

^{:refer rt.postgres.script.addon/full-str :added "4.0"}
(fact "gets the full json str form table or function"
  ^:hidden

  (addon/full-str scratch/Task)
  => "[\"scratch\",\"Task\"]")`5

^{:refer rt.postgres.script.addon/rand-hex :added "4.0"}
(fact "generates random hex"
  ^:hidden
  
  (str (addon/rand-hex 10))
  ;; "678d6e26a1"
  => string?)

^{:refer rt.postgres.script.addon/sha1 :added "4.0"}
(fact "calculates the sha1"
  ^:hidden
  
  (addon/sha1 "hello")
  => (any nil
          "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d"))

^{:refer rt.postgres.script.addon/client-list :added "4.0"}
(fact "gets the client list for pg"
  ^:hidden
  
  (count (addon/client-list))
  => pos?)

^{:refer rt.postgres.script.addon/time-ms :added "4.0"}
(fact "returns the time in ms"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/time-ms)])
  => string?)

^{:refer rt.postgres.script.addon/time-us :added "4.0"}
(fact "returns the time in us"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/time-us)])
  => string?)

^{:refer rt.postgres.script.addon/throw :added "4.0"
  :setup [(l/rt:stop)]}
(fact "raises a json exception"
  ^:hidden
  
  (addon/throw {:a 1})
  => "RAISE EXCEPTION USING DETAIL = (jsonb_build_object('a',1))::TEXT,MESSAGE = 'nil'")

^{:refer rt.postgres.script.addon/error :added "4.0"}
(fact "raises a json error with value"
  ^:hidden
  
  (addon/error [1 2 3 4])
  => "RAISE EXCEPTION USING\n  DETAIL = (jsonb_build_object('status','error','value',jsonb_build_array(1,2,3,4)))::TEXT,\n  MESSAGE = 'nil'\n")

^{:refer rt.postgres.script.addon/assert :added "4.0"}
(fact "asserts given a block"
  ^:hidden
  
  (addon/assert '(exists 1) [:wrong {}])
  => string?)

^{:refer rt.postgres.script.addon/case :added "4.0"}
(fact "builds a case form"
  ^:hidden
  
  (pg/case 1 2 3 4)
  => "CASE WHEN 1 THEN 2\nWHEN 3 THEN 4\nEND"

  ((:template @pg/case) 1 2 3 4)
  => '(% [:case :when (:% 1) :then (:% 2)
          \\ :when (:% 3) :then (:% 4)
          \\ :end]))

^{:refer rt.postgres.script.addon/field-id :added "4.0"}
(fact "shorthand for getting the field-id for a linked map"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/field-id m :field)])
  => "coalesce(m ->> ':field_id',(m -> :field) ->> 'id')")

^{:refer rt.postgres.script.addon/map:rel :added "4.0"}
(fact "basic map across relation"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/map:rel f rel)])
  => "jsonb_agg(f(o_ret)) FROM rel AS o_ret")

^{:refer rt.postgres.script.addon/map:js :added "4.0"}
(fact "basic map across json"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/map:js f arr)])
  => "coalesce(jsonb_agg(f(o_ret)),jsonb_build_array())\nFROM jsonb_array_elements(arr) AS o_ret")

^{:refer rt.postgres.script.addon/do:reduce :added "4.0"}
(fact "basic reduce macro"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/do:reduce out f :type arr)])
  => string?)

^{:refer rt.postgres.script.addon/b:select :added "4.0"
  :setup [(l/rt:restart)]}
(fact "basic select macro"
  ^:hidden
  
  (pg/b:select 1)
  => 1)

^{:refer rt.postgres.script.addon/ret :added "4.0"}
(fact "returns a value alias for select"
  ^:hidden
  
  (pg/ret 1)
  => 1)

^{:refer rt.postgres.script.addon/b:update :added "4.0"}
(fact "update macro"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/b:update t {:a 1})])
  => "UPDATE t \"a\" = 1")

^{:refer rt.postgres.script.addon/b:insert :added "4.0"}
(fact "insert macro"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/b:insert t {:a 1})])
  => "INSERT t \"a\" = 1")

^{:refer rt.postgres.script.addon/b:delete :added "4.0"}
(fact "delete macro"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/b:delete t)])
  => "DELETE t")

^{:refer rt.postgres.script.addon/perform :added "4.0"}
(fact "perform macro"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/perform 1)])
  => "PERFORM 1")

^{:refer rt.postgres.script.addon/random-enum :added "4.0"}
(fact "gets random enum"
  ^:hidden
  
  (keyword (addon/random-enum scratch/EnumStatus))
  => #{:pending :error :success})

^{:refer rt.postgres.script.addon/do:plpgsql :added "4.0"}
(fact "creates a do block"
  ^:hidden
  
  (pg/do:plpgsql
   '(let [(:integer a) 1
          (:integer b) 1]
      (:= a (+ a b))))
  => nil)

^{:refer rt.postgres.script.addon/name :added "4.0"}
(fact "gets the name of a table"
  ^:hidden
  
  (pg/name scratch/Task)
  => "Task")

^{:refer rt.postgres.script.addon/coord :added "4.0"}
(fact "gets the coordinate of a row"
  ^:hidden

  (addon/coord scratch/Task "id")
  => ["scratch" "Task" "id"])

^{:refer rt.postgres.script.addon/get-stack-diagnostics :added "4.0"}
(fact "gets the stack diagnostics"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/get-stack-diagnostics)])
  => string?)

^{:refer rt.postgres.script.addon/map:js-text :added "4.0"}
(fact "maps across json"
  ^:hidden
  
  (l/emit-as :postgres '[(rt.postgres/map:js-text f arr)])
  => "coalesce(jsonb_agg(f(o_ret)),jsonb_build_array())\nFROM jsonb_array_elements_text(arr) AS o_ret")

(comment
  (./import))
