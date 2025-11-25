(ns rt.postgres.grammar.meta-test
  (:use code.test)
  (:require [rt.postgres.grammar.meta :refer :all]
            [rt.postgres.script.builtin :as builtin]
            [std.lang :as l]))

(l/script- :postgres
  {:static {:application ["test.postgres"]
             :seed ["test/meta"]
             :all  {:schema ["test/meta"]}}})

^{:refer rt.postgres.grammar.meta/has-function :added "4.0"}
(fact "checks for existence of a function"
  ^:hidden
  
  (has-function "is-email"
                "core/util")
  => '[:select
       (exists
        [:select * :from pg_catalog.pg_proc
         :where {:proname "is-email", :pronamespace
                 [:eq [:select #{oid}
                       :from pg_catalog.pg_namespace
                       :where {:nspname "core/util"}]]}])])

^{:refer rt.postgres.grammar.meta/has-table :added "4.0"}
(fact "checks for existence of a table"
  ^:hidden
  
  (has-table "Op"
             "core/system")
  => '[:select
       (exists
        [:select * :from information_schema.tables
         :where {:table-schema "core/system",
                 :table-name "Op"}])])

^{:refer rt.postgres.grammar.meta/has-enum :added "4.0"}
(fact "checks for existence of an enum"
  ^:hidden

  (has-enum "EnumPrediction"
            "core/system")
  => '[:select
       (exists
        [:select * :from pg_catalog.pg_type
         :where {:proname "EnumPrediction",
                 :pronamespace
                 [:eq [:select #{oid}
                       :from pg_catalog.pg_namespace
                       :where {:nspname "core/system"}]]}])])

^{:refer rt.postgres.grammar.meta/has-index :added "4.0"}
(fact "cheks for the existence of an index"
  (has-index "idx" "schema")
  => vector?)

^{:refer rt.postgres.grammar.meta/get-extensions :added "4.0"}
(fact "gets import forms"
  (get-extensions {:native {:ext {:seed true}}})
  => '(:ext))

^{:refer rt.postgres.grammar.meta/create-extension :added "4.0"}
(fact "makes create extension forms"
  (create-extension :ext)
  => vector?)

^{:refer rt.postgres.grammar.meta/drop-extension :added "4.0"}
(fact "makes drop extension forms"
  (drop-extension :ext)
  => vector?)

^{:refer rt.postgres.grammar.meta/get-schema-seed :added "4.0"}
(fact "gets schema seed for a given module"
  ^:hidden
  
  (get-schema-seed (l/get-module (l/runtime-library)
                                 :postgres
                                 'rt.postgres.grammar.meta-test))
  => ["test/meta"])

^{:refer rt.postgres.grammar.meta/has-schema :added "4.0"}
(fact "checks that schema exists"
  (has-schema "schema")
  => list?)

^{:refer rt.postgres.grammar.meta/create-schema :added "4.0"}
(fact "creates a schema"
  (create-schema "schema")
  => vector?)

^{:refer rt.postgres.grammar.meta/drop-schema :added "4.0"}
(fact "drops a schema"
  (drop-schema "schema")
  => vector?)

^{:refer rt.postgres.grammar.meta/classify-ptr :added "4.0"}
(fact "classifies the pointer"
  ^:hidden
  
  (classify-ptr builtin/acosd)
  => '["acosd" "public" nil nil def$])


^{:refer rt.postgres.grammar.meta/has-policy :added "4.0"}
(fact "checks that a policy exists"
  (has-policy {:static/schema "s" :static/policy-name "p" :static/policy-table "t"})
  => vector?)

^{:refer rt.postgres.grammar.meta/drop-policy :added "4.0"}
(fact "drops a policy"
  (drop-policy {:static/schema "s" :static/policy-name "p" :static/policy-table "t"})
  => vector?)
