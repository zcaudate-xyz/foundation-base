(ns xt.db.system.client-memory-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.db.system.client-memory :as client]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.client-memory/client? :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-memory/client :added "4.1"}
(fact "creates a tagged memory client"

  (!.js
    (client/client nil))
  => {"::" "db.client.memory", "rows" {}})

^{:refer xt.db.system.client-memory/process-event-sync :added "4.1"}
(fact "flattens input and merges add events into client rows"
  
  (!.js
    (var db (client/client nil))
    (var touched (client/process-event-sync db
                                            "add"
                                            {"UserAccount" [sample/RootUser]}
                                            sample/Schema
                                            sample/SchemaLookup
                                            nil))
    db)
  => (contains-in
      {"::" "db.client.memory",
       "rows"
       {"UserProfile"
        {"c4643895-b0ce-44cc-b07b-2386bf18d43b"
         {"record"
          {"ref_links"
           {"account" {"00000000-0000-0000-0000-000000000000" true}},
           "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
           "rev_links" {},
           "data"
           {"detail" {"hello" "world"},
            "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
            "last_name" "User",
            "first_name" "Root",
            "language" "en"}},
          "t" number?}},
        "UserAccount"
        {"00000000-0000-0000-0000-000000000000"
         {"record"
          {"ref_links" {},
           "id" "00000000-0000-0000-0000-000000000000",
           "rev_links"
           {"profile" {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}},
           "data"
           {"is_official" false,
            "nickname" "root",
            "id" "00000000-0000-0000-0000-000000000000",
            "is_suspended" false,
            "password_updated" 1630408723423619,
            "is_super" true}},
          "t" number?}}}}))

^{:refer xt.db.system.client-memory/process-event-remove :added "4.1"}
(fact "removes nested data from the memory client"

  (!.js
    (var db (client/client nil))
    (client/process-event-sync db
                               "add"
                               {"UserAccount" [sample/RootUser]}
                               sample/Schema
                               sample/SchemaLookup
                               nil)
    (var touched (client/process-event-remove db
                                              "remove"
                                              {"UserAccount" [sample/RootUser]}
                                              sample/Schema
                                              sample/SchemaLookup
                                              nil))
    touched)
  => ["UserAccount" "UserProfile"])

^{:refer xt.db.system.client-memory/pull-sync :added "4.1"}
(fact "fetches tree ir and shorthand query forms from the memory client"

  (!.js
    (var db (client/client nil))
    (client/process-event-sync db
                               "add"
                               {"UserAccount" [sample/RootUser]}
                               sample/Schema
                               sample/SchemaLookup
                               nil)
    (client/pull-sync
     db
     sample/Schema
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
       "custom" []}]
     nil))
  => [{"nickname" "root"
       "profile" [{"first_name" "Root"}]}]

  (!.js
    (var db (client/client nil))
    (client/process-event-sync db
                               "add"
                               {"UserAccount" [sample/RootUser]}
                               sample/Schema
                               sample/SchemaLookup
                               nil)
    (client/pull-sync
     db
     sample/Schema
     ["UserAccount"
      {"id" "00000000-0000-0000-0000-000000000000"}
      ["nickname"]]
     nil))
  => [{"nickname" "root"}])

^{:refer xt.db.system.client-memory/pull :added "4.1"}
(fact "pulls data using tree"
  
  (notify/wait-on :js
    (var db (client/client nil))
    (client/process-event-sync db
                               "add"
                               {"UserAccount" [sample/RootUser]}
                               sample/Schema
                               sample/SchemaLookup
                               nil)
    (-> (client/pull
         db
         sample/Schema
         ["UserAccount"
          {"id" "00000000-0000-0000-0000-000000000000"}
          ["nickname"]]
         nil)
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => [{"nickname" "root"}])

^{:refer xt.db.system.client-memory/delete-sync :added "4.1"}
(fact "deletes ids directly from the memory client"

  (!.js
    (var db (client/client nil))
    (client/process-event-sync db
                               "add"
                               {"UserAccount" [sample/RootUser]}
                               sample/Schema
                               sample/SchemaLookup
                               nil)
    (client/delete-sync
     db
     sample/Schema
     "UserAccount"
     ["00000000-0000-0000-0000-000000000000"]
     nil)
    (xtd/get-in db ["rows"
                    "UserAccount"
                    "00000000-0000-0000-0000-000000000000"]))
  => nil)

^{:refer xt.db.system.client-memory/clear :added "4.1"}
(fact "clears the memory client rows"

  (!.js
    (var db (client/client nil))
    (client/process-event-sync db
                               "add"
                               {"UserAccount" [sample/RootUser]}
                               sample/Schema
                               sample/SchemaLookup
                               nil)
    [(client/clear db)
     (xt/x:obj-keys (. db ["rows"]))])
  => [true []])
