(ns rt.postgres.script.addon-test
  (:use code.test)
  (:require [rt.postgres.script.addon :as addon]
            [rt.postgres.script.scratch :as scratch]
            [std.lang :as l]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[rt.postgres :as pg]
             [rt.postgres.script.scratch :as scratch]]
   :import [["pgcrypto"]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

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
  
  (!.pg
   (:bigint (pg/time-ms)))
  => integer?)

^{:refer rt.postgres.script.addon/time-us :added "4.0"}
(fact "returns the time in us"
  ^:hidden
  
  (!.pg
   (:bigint (pg/time-us)))
  => integer?)

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
(fact "shorthand for getting the field-id for a linked map")

^{:refer rt.postgres.script.addon/map:rel :added "4.0"}
(fact "basic map across relation")

^{:refer rt.postgres.script.addon/map:js :added "4.0"}
(fact "basic map across json")

^{:refer rt.postgres.script.addon/do:reduce :added "4.0"}
(fact "basic reduce macro")

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
(fact "update macro")

^{:refer rt.postgres.script.addon/b:insert :added "4.0"}
(fact "insert macro")

^{:refer rt.postgres.script.addon/b:delete :added "4.0"}
(fact "delete macro")

^{:refer rt.postgres.script.addon/perform :added "4.0"}
(fact "perform macro")

^{:refer rt.postgres.script.addon/random-enum :added "4.0"}
(fact "gets random enum"
  ^:hidden
  
  (keyword (pg/random-enum scratch/EnumStatus))
  => #{:pending :error :success})

(comment
  (./import))


^{:refer rt.postgres.script.addon/ARRAY :added "4.0"}
(fact "TODO")

^{:refer rt.postgres.script.addon/id :added "4.0"}
(fact "TODO")

^{:refer rt.postgres.script.addon/full :added "4.0"}
(fact "TODO")

^{:refer rt.postgres.script.addon/full-str :added "4.0"}
(fact "TODO")

^{:refer rt.postgres.script.addon/do:plpgsql :added "4.0"}
(fact "TODO")