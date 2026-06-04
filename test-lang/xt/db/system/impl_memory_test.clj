(ns xt.db.system.impl-memory-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-memory :as impl]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-memory/memory-client :added "4.1"}
(fact "creates the thin memory client record with stored schema context"
  
  (!.js
    (impl/memory-client sample/Schema
                        sample/SchemaLookup
                        {"mode" "memory"}))
  => (contains-in
      {"::" "db.client.memory"
       "schema" map?
       "lookup" map?
       "opts" {"mode" "memory"}}))

^{:refer xt.db.system.impl-memory/record-add-sync :added "4.1"}
(fact "adds records directly to the memory client rows"

  (!.js
    (var client (impl/memory-client sample/Schema
                                    sample/SchemaLookup
                                    nil))
    (impl/record-add-sync
     client
     "UserAccount"
     [sample/RootUser])
    [(xtd/get-in client ["rows"
                         "UserAccount"
                         "00000000-0000-0000-0000-000000000000"
                         "record"
                         "data"
                         "nickname"])
     (xtd/get-in client ["rows"
                         "UserProfile"
                         "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                         "record"
                         "data"
                         "first_name"])])
  => ["root" "Root"])

^{:refer xt.db.system.impl-memory/pull-sync :added "4.1"}
(fact "pull-sync reads tree and shorthand query forms from the client context"

  (!.js
    (var client (impl/memory-client sample/Schema
                                    sample/SchemaLookup
                                    nil))
    (impl/record-add-sync client
                          "UserAccount"
                          [sample/RootUser])
    (impl/pull-sync
     client
     ["UserAccount"
      {"where" [{"id" "00000000-0000-0000-0000-000000000000"}]
       "data" ["nickname"]
       "links" [["profile"
                 "reverse"
                 ["UserProfile"
                  {"where" []
                   "data" ["first_name"]
                   "links" []
                   "custom" []}]]]
       "custom" []}]))
  => [{"nickname" "root"
       "profile" [{"first_name" "Root"}]}]

  (!.js
    (var client (impl/memory-client sample/Schema
                                    sample/SchemaLookup
                                    nil))
    (impl/record-add-sync client
                          "UserAccount"
                          [sample/RootUser])
    (impl/pull-sync
     client
     ["UserAccount"
      {"id" "00000000-0000-0000-0000-000000000000"}
      ["nickname"]]))
  => [{"nickname" "root"}])

^{:refer xt.db.system.impl-memory/pull :added "4.1"}
(fact "pull reads through async semantics"

  (notify/wait-on :js
    (var client (impl/memory-client sample/Schema
                                    sample/SchemaLookup
                                    nil))
    (impl/record-add-sync client
                          "UserAccount"
                          [sample/RootUser])
    (-> (impl/pull
         client
         ["UserAccount"
          {"id" "00000000-0000-0000-0000-000000000000"}
          ["nickname"]])
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => [{"nickname" "root"}])

^{:refer xt.db.system.impl-memory/record-add :added "4.1"}
(fact "record-add writes through async semantics"

  (notify/wait-on :js
    (var client (impl/memory-client sample/Schema
                                    sample/SchemaLookup
                                    nil))
    (-> (impl/record-add
         client
         "UserAccount"
         [{"id" "USER-10" "nickname" "echo"}])
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (xtd/get-in client ["rows"
                                "UserAccount"
                                "USER-10"
                                "record"
                                "data"]))))))
  => {"id" "USER-10" "nickname" "echo"})

^{:refer xt.db.system.impl-memory/record-delete-sync :added "4.1"}
(fact "record-delete-sync removes ids from memory rows"

  (!.js
    (var client (impl/memory-client sample/Schema
                                    sample/SchemaLookup
                                    nil))
    (impl/record-add-sync client
                          "UserAccount"
                          [sample/RootUser])
    (impl/record-delete-sync
     client
     "UserAccount"
     ["00000000-0000-0000-0000-000000000000"])
    (xtd/get-in client ["rows"
                        "UserAccount"
                        "00000000-0000-0000-0000-000000000000"]))
  => nil)

^{:refer xt.db.system.impl-memory/record-delete :added "4.1"}
(fact "record-delete removes ids with async semantics"

  (notify/wait-on :js
    (var client (impl/memory-client sample/Schema
                                    sample/SchemaLookup
                                    nil))
    (impl/record-add-sync client
                          "UserAccount"
                          [sample/RootUser])
    (-> (impl/record-delete
         client
         "UserAccount"
         ["00000000-0000-0000-0000-000000000000"])
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (xtd/get-in client ["rows"
                                "UserAccount"
                                "00000000-0000-0000-0000-000000000000"]))))))
  => nil)
