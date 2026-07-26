tahto/model/spec_postgres/meta_test.clj:1:(ns tahto.model.spec-postgres.meta-test
tahto/model/spec_postgres/meta_test.clj:2:  (:require [tahto.model.spec-postgres.meta :refer :all]
            [postgres.core.builtin :as builtin]
            [tahto.core :as l])
  (:use code.test))

(l/script- :postgres
  {:static {:application ["test.postgres"]
             :seed ["test/meta"]
             :all  {:schema ["test/meta"]}}})

tahto/model/spec_postgres/meta_test.clj:12:^{:refer tahto.model.spec-postgres.meta/has-function :added "4.0"}
(fact "checks for existence of a function"

  (has-function "is-email"
                "core/util")
  => '[:select
       (exists
        [:select * :from pg_catalog.pg_proc
         :where {:proname "is-email", :pronamespace
                 [:eq [:select #{oid}
                       :from pg_catalog.pg_namespace
                       :where {:nspname "core/util"}]]}])])

tahto/model/spec_postgres/meta_test.clj:25:^{:refer tahto.model.spec-postgres.meta/has-table :added "4.0"}
(fact "checks for existence of a table"

  (has-table "Op"
             "core/system")
  => '[:select
       (exists
        [:select * :from information_schema.tables
         :where {:table-schema "core/system",
                 :table-name "Op"}])])

tahto/model/spec_postgres/meta_test.clj:36:^{:refer tahto.model.spec-postgres.meta/has-enum :added "4.0"}
(fact "checks for existence of an enum"

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

tahto/model/spec_postgres/meta_test.clj:50:^{:refer tahto.model.spec-postgres.meta/has-index :added "4.0"}
(fact "cheks for the existence of an index"
  (has-index "idx" "schema")
  => vector?)

tahto/model/spec_postgres/meta_test.clj:55:^{:refer tahto.model.spec-postgres.meta/get-extensions :added "4.0"}
(fact "gets import forms"
  (get-extensions {:native {:ext {:seed true}}})
  => '(:ext))

tahto/model/spec_postgres/meta_test.clj:60:^{:refer tahto.model.spec-postgres.meta/create-extension :added "4.0"}
(fact "makes create extension forms"
  (create-extension :ext)
  => vector?)

tahto/model/spec_postgres/meta_test.clj:65:^{:refer tahto.model.spec-postgres.meta/drop-extension :added "4.0"}
(fact "makes drop extension forms"
  (drop-extension :ext)
  => vector?)

tahto/model/spec_postgres/meta_test.clj:70:^{:refer tahto.model.spec-postgres.meta/has-policy :added "4.0"}
(fact "checks that a policy exists"
  (has-policy {:static/schema "s" :static/policy-name "p" :static/policy-table "t"})
  => vector?)

tahto/model/spec_postgres/meta_test.clj:75:^{:refer tahto.model.spec-postgres.meta/drop-policy :added "4.0"}
(fact "drops a policy"
  (drop-policy {:static/schema "s" :static/policy-name "p" :static/policy-table "t"})
  => vector?)

tahto/model/spec_postgres/meta_test.clj:80:^{:refer tahto.model.spec-postgres.meta/get-schema-seed :added "4.0"}
(fact "gets schema seed for a given module"

  (get-schema-seed (l/get-module (l/runtime-library)
                                 :postgres
tahto/model/spec_postgres/meta_test.clj:85:                                 'tahto.model.spec-postgres.meta-test))
  => ["test/meta"])

tahto/model/spec_postgres/meta_test.clj:88:^{:refer tahto.model.spec-postgres.meta/has-schema :added "4.0"}
(fact "checks that schema exists"
  (has-schema "schema")
  => list?)

tahto/model/spec_postgres/meta_test.clj:93:^{:refer tahto.model.spec-postgres.meta/create-schema :added "4.0"}
(fact "creates a schema"
  (create-schema "schema")
  => vector?)

tahto/model/spec_postgres/meta_test.clj:98:^{:refer tahto.model.spec-postgres.meta/drop-schema :added "4.0"}
(fact "drops a schema"
  (drop-schema "schema")
  => vector?)

tahto/model/spec_postgres/meta_test.clj:103:^{:refer tahto.model.spec-postgres.meta/classify-ptr :added "4.0"}
(fact "classifies the pointer"

  (classify-ptr builtin/acosd)
  => '["acosd" "public" nil nil def$])
