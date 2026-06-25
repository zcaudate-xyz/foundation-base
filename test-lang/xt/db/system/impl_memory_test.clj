(ns xt.db.system.impl-memory-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-memory :as impl]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-memory/pull :added "4.1"}
(fact "pull reads through async semantics"

  (!.js
    (var impl (impl/impl-memory sample/Schema
                                sample/SchemaLookup))
    (impl/record-add impl
                     "UserAccount"
                     [sample/RootUser])
    (impl/pull
     impl
     ["UserAccount"
      {"id" "00000000-0000-0000-0000-000000000000"}
      ["nickname"]]))
  => [{"nickname" "root"}])

^{:refer xt.db.system.impl-memory/pull-async :added "4.1"}
(fact "pull-async resolves through promise semantics"

  (notify/wait-on :js
    (var impl (impl/impl-memory sample/Schema
                                sample/SchemaLookup))
    (impl/record-add impl
                     "UserAccount"
                     [sample/RootUser])
    (-> (impl/pull-async
         impl
         ["UserAccount"
          {"id" "00000000-0000-0000-0000-000000000000"}
          ["nickname"]])
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => [{"nickname" "root"}])

^{:refer xt.db.system.impl-memory/record-add :added "4.1"}
(fact "record-add writes through async semantics"

  (!.js
    (var impl (impl/impl-memory sample/Schema
                                sample/SchemaLookup))
    (impl/record-add
     impl
     "UserAccount"
     [{"id" "USER-10" "nickname" "echo"}])
    (xtd/get-in impl ["rows"
                      "UserAccount"
                      "USER-10"
                      "record"
                      "data"]))
  => {"id" "USER-10" "nickname" "echo"})

^{:refer xt.db.system.impl-memory/record-delete :added "4.1"}
(fact "record-delete removes ids with async semantics"

  (!.js
    (var impl (impl/impl-memory sample/Schema
                                sample/SchemaLookup))
    (impl/record-add impl
                     "UserAccount"
                     [sample/RootUser])
    (impl/record-delete
     impl
     "UserAccount"
     ["00000000-0000-0000-0000-000000000000"])
    (xtd/get-in impl ["rows"
                      "UserAccount"
                      "00000000-0000-0000-0000-000000000000"]))
  => nil)

^{:refer xt.db.system.impl-memory/clear-db :added "4.1"}
(fact "clear-db removes all rows from the memory impl"

  (!.js
    (var impl (impl/impl-memory sample/Schema
                                sample/SchemaLookup))
    (impl/record-add impl
                     "UserAccount"
                     [sample/RootUser])
    (impl/clear-db impl)
    [(xtd/get-in impl ["rows"
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"])
     (xt/x:len (xt/x:obj-keys (xtd/get-in impl ["rows"])))])
  => [nil 0])

^{:refer xt.db.system.impl-memory/process-add-event :added "4.1"}
(fact "process-add-event merges nested data into client and links"
  
  (!.js
    (var impl (impl/impl-memory sample/Schema
                                sample/SchemaLookup))
    (var out
         (impl/process-add-event
          impl
          {"UserAccount" [sample/RootUser]}))
    [(xt/x:len out)
     (xtd/get-in impl ["rows"
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "data"
                       "nickname"])
     (xtd/get-in impl ["rows"
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "rev_links"
                       "profile"])
     (xtd/get-in impl ["rows"
                       "UserProfile"
                       "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                       "record"
                       "ref_links"
                       "account"])])
  => [2
      "root"
      {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}
      {"00000000-0000-0000-0000-000000000000" true}])

^{:refer xt.db.system.impl-memory/process-remove-event :added "4.1"}
(fact "process-remove-event removes nested data in lookup order"

  (!.js
    (var impl (impl/impl-memory sample/Schema
                                sample/SchemaLookup))
    (impl/process-add-event
     impl
     {"UserAccount" [sample/RootUser]})
    [(impl/process-remove-event
      impl
      {"UserAccount" [sample/RootUser]})
     (xtd/get-in impl ["rows"
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"])
     (xtd/get-in impl ["rows"
                       "UserProfile"
                       "c4643895-b0ce-44cc-b07b-2386bf18d43b"])])
  => [["UserAccount" "UserProfile"]
      nil
      nil])

^{:refer xt.db.system.impl-memory/rpc-call-async :added "4.1"}
(fact "rpc call not implemented")

^{:refer xt.db.system.impl-memory/impl-memory :added "4.1"}
(fact "creates the thin memory impl record with stored schema context"
  
  (!.js
    (impl/impl-memory {}
                      sample/Schema
                      sample/SchemaLookup
                      {}))
  => (contains-in
      {"::" "xt.db.system.impl_memory/ImplMemory"
       "rows" {}
       "schema" map?
       "lookup" map?}))
