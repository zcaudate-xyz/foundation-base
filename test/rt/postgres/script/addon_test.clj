(ns rt.postgres.script.addon-test
  (:use code.test)
  (:require [rt.postgres.script.addon :as addon]
            [rt.postgres.script.scratch :as scratch]
            [std.lang :as l]
            [std.lib :as h]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[rt.postgres :as pg]
             [rt.postgres.script.scratch :as scratch]]
   :import [["pgcrypto"]]})

;; Removed global setup to avoid connection attempt

^{:refer rt.postgres.script.addon/exec :added "4.0"}
(fact "executes an sql statement"
  ^:hidden

  (pg/exec [:select 1])
  => 1)

^{:refer rt.postgres.script.addon/ARRAY :added "4.0"}
(fact "creates an array"
  ^:hidden
  
  (pg/ARRAY 1 2 3 4)
  => '(1 2 3 4))

^{:refer rt.postgres.script.addon/id :added "4.0"}
(fact "gets the id of an object"
  ^:hidden
  
  (str (pg/id {:id "40ff7565-2f3d-4ac0-acb5-3527370eb646"}))
  => "40ff7565-2f3d-4ac0-acb5-3527370eb646")

^{:refer rt.postgres.script.addon/full :added "4.0"}
(fact "gets the full jsonb for table or function"
  ^:hidden

  (pg/full scratch/Task)
  => ["scratch" "Task"])

^{:refer rt.postgres.script.addon/full-str :added "4.0"}
(fact "gets the full json str form table or function"
  ^:hidden

  (pg/full-str scratch/Task)
  => "[\"scratch\",\"Task\"]")

^{:refer rt.postgres.script.addon/rand-hex :added "4.0"}
(fact "generates random hex"
  ^:hidden
  
  (str (pg/rand-hex 10))
  ;; "678d6e26a1"
  => string?)

^{:refer rt.postgres.script.addon/sha1 :added "4.0"}
(fact "calculates the sha1"
  ^:hidden
  
  (pg/sha1 "hello")
  => "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d")

^{:refer rt.postgres.script.addon/client-list :added "4.0"}
(fact "gets the client list for pg"
  ^:hidden
  
  (count (pg/client-list))
  => pos?)

^{:refer rt.postgres.script.addon/time-ms :added "4.0"}
(fact "returns the time in ms"
  ^:hidden
  
  (l/emit-as :postgres '[(pg/time-ms)])
  => string?)

^{:refer rt.postgres.script.addon/time-us :added "4.0"}
(fact "returns the time in us"
  ^:hidden
  
  (l/emit-as :postgres '[(pg/time-us)])
  => string?)

^{:refer rt.postgres.script.addon/throw :added "4.0"
  :setup [(l/rt:stop)]}
(fact "raises a json exception"
  ^:hidden
  
  (pg/throw {:a 1})
  => "RAISE EXCEPTION USING DETAIL = jsonb_build_object('a',1)")

^{:refer rt.postgres.script.addon/error :added "4.0"}
(fact "raises a json error with value"
  ^:hidden
  
  (pg/error [1 2 3 4])
  => "RAISE EXCEPTION USING DETAIL = jsonb_build_object('status','error','value',jsonb_build_array(1,2,3,4))")

^{:refer rt.postgres.script.addon/assert :added "4.0"}
(fact "asserts given a block"
  ^:hidden
  
  (pg/assert '(exists 1) [:wrong {}])
  => (std.string/|
      "IF NOT (exists(1)) THEN"
      "  RAISE EXCEPTION USING DETAIL = jsonb_build_object('status','error','tag','wrong');"
      "END IF;"))

^{:refer rt.postgres.script.addon/case :added "4.0"}
(fact "builds a case form"

  (pg/case 1 2 3 4)
  => "CASE WHEN 1 THEN 2\nWHEN 3 THEN 4\nEND"

  ((:template @pg/case) 1 2 3 4)
  => '(% [:case :when (:% 1) :then (:% 2)
          \\ :when (:% 3) :then (:% 4)
          \\ :end]))

^{:refer rt.postgres.script.addon/field-id :added "4.0"}
(fact "shorthand for getting the field-id for a linked map"
  (l/emit-as :postgres '[(pg/field-id m :field)])
  => "COALESCE(m->>'field_id',m->'field'->>'id')")

^{:refer rt.postgres.script.addon/map:rel :added "4.0"}
(fact "basic map across relation"
  (l/emit-as :postgres '[(pg/map:rel f rel)])
  => "(SELECT jsonb_agg(f(o_ret)) FROM rel AS o_ret)")

^{:refer rt.postgres.script.addon/map:js :added "4.0"}
(fact "basic map across json"
  (l/emit-as :postgres '[(pg/map:js f arr)])
  => "(SELECT COALESCE(jsonb_agg(f(o_ret)),'[]') FROM jsonb_array_elements(arr) AS o_ret)")

^{:refer rt.postgres.script.addon/do:reduce :added "4.0"}
(fact "basic reduce macro"
  (l/emit-as :postgres '[(pg/do:reduce out f :type arr)])
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
  (l/emit-as :postgres '[(pg/b:update t {:a 1})])
  => "UPDATE t SET a = 1")

^{:refer rt.postgres.script.addon/b:insert :added "4.0"}
(fact "insert macro"
  (l/emit-as :postgres '[(pg/b:insert t {:a 1})])
  => "INSERT INTO t (a) VALUES (1)")

^{:refer rt.postgres.script.addon/b:delete :added "4.0"}
(fact "delete macro"
  (l/emit-as :postgres '[(pg/b:delete t)])
  => "DELETE FROM t")

^{:refer rt.postgres.script.addon/perform :added "4.0"}
(fact "perform macro"
  (l/emit-as :postgres '[(pg/perform 1)])
  => "PERFORM 1;")

^{:refer rt.postgres.script.addon/random-enum :added "4.0"}
(fact "gets random enum"
  ^:hidden
  
  (keyword (pg/random-enum scratch/EnumStatus))
  => #{:pending :error :success})

^{:refer rt.postgres.script.addon/do:plpgsql :added "4.0"}
(fact "creates a do block"
  ^:hidden
  
  (pg/do:plpgsql
   '(let [(:integer a) 1
          (:integer b) 1]
      (:= a (+ a b))))
  => nil)

(comment
  (./import))


^{:refer rt.postgres.script.addon/name :added "4.0"}
(fact "gets the name of a table"
  (pg/name scratch/Task)
  => "Task")

^{:refer rt.postgres.script.addon/coord :added "4.0"}
(fact "gets the coordinate of a row"
  (pg/coord scratch/Task "id")
  => '(jsonb-build-array "scratch" "Task" "id"))

^{:refer rt.postgres.script.addon/get-stack-diagnostics :added "4.0"}
(fact "gets the stack diagnostics"
  (l/emit-as :postgres '[(pg/get-stack-diagnostics)])
  => string?)

^{:refer rt.postgres.script.addon/map:js-text :added "4.0"}
(fact "maps across json"
  (l/emit-as :postgres '[(pg/map:js-text f arr)])
  => "(SELECT COALESCE(jsonb_agg(f(o_ret)),'[]') FROM jsonb_array_elements_text(arr) AS o_ret)")
