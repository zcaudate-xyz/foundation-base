(ns xt.db.system.memory-store-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.db.system.memory-store :as store]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.memory-store/set-input :added "4.1"}
(fact "prepares nested input using the same flattened shape as sql sync"

  (!.js
    (var flat (store/set-input sample/Schema
                               {"UserAccount" [sample/RootUser]}))
    [(xtd/get-in flat ["UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "data"
                       "nickname"])
     (xtd/get-in flat ["UserProfile"
                       "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                       "ref_links"
                       "account"
                       "00000000-0000-0000-0000-000000000000"])])
  => ["root" true])

^{:refer xt.db.system.memory-store/set-sync :added "4.1"}
(fact "merges nested data into cache-style store rows"

  (!.js
    (var mem (store/create-store nil))
    (var touched (store/set-sync mem
                                 sample/Schema
                                 {"UserAccount" [sample/RootUser]}
                                 nil))
    [(xtd/arr-lookup touched)
     (xtd/get-in mem ["rows"
                      "UserAccount"
                      "00000000-0000-0000-0000-000000000000"
                      "record"
                      "data"
                      "nickname"])
     (xtd/get-in mem ["rows"
                      "UserAccount"
                      "00000000-0000-0000-0000-000000000000"
                      "record"
                      "rev_links"
                      "profile"
                      "c4643895-b0ce-44cc-b07b-2386bf18d43b"])])
  => [{"UserAccount" true "UserProfile" true}
      "root"
      true])

^{:refer xt.db.system.memory-store/fetch-sync :added "4.1"}
(fact "fetches tree ir data from store rows"

  (!.js
    (var mem (store/create-store nil))
    (store/set-sync mem
                    sample/Schema
                    {"UserAccount" [sample/RootUser]}
                    nil)
    (store/fetch-sync
     mem
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
       "profile" [{"first_name" "Root"}]}])

^{:refer xt.db.system.memory-store/remove-sync :added "4.1"}
(fact "removes nested input data in table order"

  (!.js
    (var mem (store/create-store nil))
    (store/set-sync mem
                    sample/Schema
                    {"UserAccount" [sample/RootUser]}
                    nil)
    (var touched (store/remove-sync mem
                                    sample/Schema
                                    sample/SchemaLookup
                                    {"UserAccount" [sample/RootUser]}
                                    nil))
    [touched
     (xtd/get-in mem ["rows" "UserAccount" "00000000-0000-0000-0000-000000000000"])
     (xtd/get-in mem ["rows" "UserProfile" "c4643895-b0ce-44cc-b07b-2386bf18d43b"])])
  => [["UserAccount" "UserProfile"]
      nil
      nil])

^{:refer xt.db.system.memory-store/clear :added "4.1"}
(fact "clears the store rows"

  (!.js
    (var mem (store/create-store nil))
    (store/set-sync mem
                    sample/Schema
                    {"UserAccount" [sample/RootUser]}
                    nil)
    (store/clear mem)
    (xtd/get-in mem ["rows" "UserAccount"]))
  => nil)


^{:refer xt.db.system.memory-store/create-store :added "4.1"}
(fact "creates an empty rows store"

  (!.js
    (store/create-store nil))
  => {"rows" {}})

^{:refer xt.db.system.memory-store/get-rows :added "4.1"}
(fact "returns rows and initialises missing rows maps"

  (!.js
    (var current {"rows" {"Currency" {"USD" {"record" {"data" {"id" "USD"}}}}}})
    (var fresh {})
    (store/get-rows fresh)
    [(store/get-rows current)
     (. fresh ["rows"])])
  => [{"Currency" {"USD" {"record" {"data" {"id" "USD"}}}}}
      {}])

^{:refer xt.db.system.memory-store/remove-input :added "4.1"}
(fact "prepares ordered delete ids from nested data"

  (!.js
    (store/remove-input
     sample/Schema
     sample/SchemaLookup
     {"UserAccount" [sample/RootUser]}))
  => [["UserAccount" ["00000000-0000-0000-0000-000000000000"]]
      ["UserProfile" ["c4643895-b0ce-44cc-b07b-2386bf18d43b"]]])

^{:refer xt.db.system.memory-store/delete-sync :added "4.1"}
(fact "deletes ids directly from the store"

  (!.js
    (var mem (store/create-store nil))
    (store/set-sync mem
                    sample/Schema
                    {"UserAccount" [sample/RootUser]}
                    nil)
    (store/delete-sync
     mem
     sample/Schema
     "UserAccount"
     ["00000000-0000-0000-0000-000000000000"]
     nil)
    (xtd/get-in mem ["rows" "UserAccount" "00000000-0000-0000-0000-000000000000"]))
  => nil)