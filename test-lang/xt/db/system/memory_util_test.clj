(ns xt.db.system.memory-util-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.db.system.memory-util :as util]
             [xt.db.text.base-flatten :as f]
             [xt.db.helpers.data-main-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.db.system.memory-util :as util]
             [xt.db.text.base-flatten :as f]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.memory-util/has-entry :added "4.1"}
(fact "checks if entry exists"

  (!.js
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {}) nil)
    (util/has-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => true

  (!.py
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {}) nil)
    (util/has-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => true)

^{:refer xt.db.system.memory-util/get-entry :added "4.1"
  :setup [(def +account-get-entry-check+
            (just-in
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
                "password_updated" number?
                "is_super" true}},
              "t" number?}))]}
(fact "gets entry by id"

  (!.js
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (util/get-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => +account-get-entry-check+

  (!.py
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (util/get-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => +account-get-entry-check+)

^{:refer xt.db.system.memory-util/swap-if-entry :added "4.1"}
(fact "modifies entry if exists"

  (!.js
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (-> (util/swap-if-entry rows
                            "UserAccount" "00000000-0000-0000-0000-000000000000"
                            (fn [record]
                              (return (xtd/set-in record ["data" "foo"] "hello"))))
        (xtd/get-in ["record" "data" "foo"])))
  => "hello"

  (!.py
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (-> (util/swap-if-entry rows
                            "UserAccount" "00000000-0000-0000-0000-000000000000"
                            (fn [record]
                              (return (xtd/set-in record ["data" "foo"] "hello"))))
        (xtd/get-in ["record" "data" "foo"])))
  => "hello")

^{:refer xt.db.system.memory-util/merge-single :added "4.1"}
(fact "merges a single entry"

  (!.js
    (util/merge-single {}
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000001"
                       {:id "00000000-0000-0000-0000-000000000001"
                        :data {}
                        :ref-links {}
                        :rev-links {}}
                       k/identity))
  => (just {"record" {"ref_links" {},
                      "id" "00000000-0000-0000-0000-000000000001",
                      "rev_links" {},
                      "data" {}},
            "t" number?})

  (!.py
    (util/merge-single {}
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000001"
                       {:id "00000000-0000-0000-0000-000000000001"
                        :data {}
                        :ref-links {}
                        :rev-links {}}
                       k/identity))
  => (just {"record" {"ref_links" {},
                      "id" "00000000-0000-0000-0000-000000000001",
                      "rev_links" {},
                      "data" {}},
            "t" number?}))

^{:refer xt.db.system.memory-util/merge-bulk :added "4.1"
  :setup [(def +account-merge-bulk-check+
            (just-in
             [{"UserProfile"
               {"c4643895-b0ce-44cc-b07b-2386bf18d43b"
                {"record"
                 {"ref_links"
                  {"account"
                   {"00000000-0000-0000-0000-000000000000" true}},
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
                  {"profile"
                   {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}},
                  "data"
                  {"is_official" false,
                   "nickname" "root",
                   "id" "00000000-0000-0000-0000-000000000000",
                   "is_suspended" false,
                   "password_updated" number?
                   "is_super" true}},
                 "t" number?}}}
              ["00000000-0000-0000-0000-000000000000"]]))]}
(fact "merges flattened data into the store"

  (!.js
    (var rows {})
    [(util/merge-bulk rows (f/flatten sample/Schema
                                      "UserAccount"
                                      sample/RootUser
                                      {})
                      nil)
     (util/get-ids rows "UserAccount")])
  => +account-merge-bulk-check+

  (!.py
    (var rows {})
    [(util/merge-bulk rows (f/flatten sample/Schema
                                      "UserAccount"
                                      sample/RootUser
                                      {})
                      nil)
     (util/get-ids rows "UserAccount")])
  => +account-merge-bulk-check+)

^{:refer xt.db.system.memory-util/get-ids :added "4.1"}
(fact "gets ids for a table"

  (!.js
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (util/get-ids rows "UserAccount"))
  => ["00000000-0000-0000-0000-000000000000"]

  (!.py
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (util/get-ids rows "UserAccount"))
  => ["00000000-0000-0000-0000-000000000000"])

^{:refer xt.db.system.memory-util/all-records :added "4.1"
  :setup [(def +account-all-records-check+
            (just-in
             {"00000000-0000-0000-0000-000000000000"
              {"ref_links" {},
               "id" "00000000-0000-0000-0000-000000000000",
               "rev_links"
               {"profile"
                {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}},
               "data"
               {"is_official" false,
                "nickname" "root",
                "id" "00000000-0000-0000-0000-000000000000",
                "is_suspended" false,
                "password_updated" number?
                "is_super" true}}}))]}
(fact "returns all records"

  (!.js
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {}) nil)
    (util/all-records rows "UserAccount"))
  => +account-all-records-check+

  (!.py
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {}) nil)
    (util/all-records rows "UserAccount"))
  => +account-all-records-check+)

^{:refer xt.db.system.memory-util/get-changed-single :added "4.1"}
(fact "gets changed record"

  (!.js
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (var changed (-> (util/get-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000")
                     (. ["record"])
                     (xtd/clone-nested)
                     (xtd/set-in ["data" "nickname"] "hello")))
    (util/get-changed-single rows
                             "UserAccount" "00000000-0000-0000-0000-000000000000"
                             changed))
  => {"data" {"nickname" "hello"}}

  (!.py
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (var changed (-> (util/get-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000")
                     (. ["record"])
                     (xtd/clone-nested)
                     (xtd/set-in ["data" "nickname"] "hello")))
    (util/get-changed-single rows
                             "UserAccount" "00000000-0000-0000-0000-000000000000"
                             changed))
  => {"data" {"nickname" "hello"}})

^{:refer xt.db.system.memory-util/has-changed-single :added "4.1"}
(fact "checks if record has changed"

  (!.js
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (var changed (-> (util/get-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000")
                     (. ["record"])
                     (xtd/clone-nested)
                     (xtd/set-in ["data" "nickname"] "hello")))
    (util/has-changed-single rows
                             "UserAccount" "00000000-0000-0000-0000-000000000000"
                             changed))
  => true

  (!.py
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (var changed (-> (util/get-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000")
                     (. ["record"])
                     (xtd/clone-nested)
                     (xtd/set-in ["data" "nickname"] "hello")))
    (util/has-changed-single rows
                             "UserAccount" "00000000-0000-0000-0000-000000000000"
                             changed))
  => true)

^{:refer xt.db.system.memory-util/get-link-attrs :added "4.1"
  :setup [(def +get-link-attrs-check+
            {"table_link" "rev_links",
             "inverse_link" "ref_links",
             "table_key" "UserAccount",
             "table_field" "profile",
             "inverse_key" "UserProfile",
             "inverse_field" "account"})]}
(fact "finds link attributes"

  (!.js (util/get-link-attrs sample/Schema "UserAccount" "profile"))
  => +get-link-attrs-check+

  (!.py (util/get-link-attrs sample/Schema "UserAccount" "profile"))
  => +get-link-attrs-check+)

^{:refer xt.db.system.memory-util/remove-single-link-entry :added "4.1"}
(fact "removes a single link entry"

  (!.js
    (var rows {})
    (var removed {"id" nil})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    [(-> (util/remove-single-link-entry rows
                                        "UserAccount"
                                        "00000000-0000-0000-0000-000000000000"
                                        "rev_links"
                                        "profile"
                                        "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                                        (fn [link-id]
                                          (xtd/set-in removed ["id"] link-id)))
         (xtd/get-in ["record" "rev_links"]))
     (. removed ["id"])])
  => [{} "c4643895-b0ce-44cc-b07b-2386bf18d43b"]

  (!.py
    (var rows {})
    (var removed {"id" nil})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    [(-> (util/remove-single-link-entry rows
                                        "UserAccount"
                                        "00000000-0000-0000-0000-000000000000"
                                        "rev_links"
                                        "profile"
                                        "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                                        (fn [link-id]
                                          (xtd/set-in removed ["id"] link-id)))
         (xtd/get-in ["record" "rev_links"]))
     (. removed ["id"])])
  => [{} "c4643895-b0ce-44cc-b07b-2386bf18d43b"])

^{:refer xt.db.system.memory-util/remove-single-link :added "4.1"}
(fact "removes a single link"

  (!.js
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (util/remove-single-link rows
                             sample/Schema
                             "UserAccount"
                             "00000000-0000-0000-0000-000000000000"
                             "profile"
                             "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true]

  (!.py
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (util/remove-single-link rows
                             sample/Schema
                             "UserAccount"
                             "00000000-0000-0000-0000-000000000000"
                             "profile"
                             "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true])

^{:refer xt.db.system.memory-util/remove-single :added "4.1"
  :setup [(def +account-remove-single-check+
            (contains-in
             [{"record"
               {"ref_links" {},
                "id" "00000000-0000-0000-0000-000000000000",
                "rev_links"
                {"profile" {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}},
                "data"
                {"is_official" false,
                 "nickname" "root",
                 "id" "00000000-0000-0000-0000-000000000000",
                 "is_suspended" false,
                 "password_updated" number?
                 "is_super" true}},
               "t" number?}]))]}
(fact "removes a single entry"

  (!.js
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (util/remove-single rows
                        sample/Schema
                        "UserAccount"
                        "00000000-0000-0000-0000-000000000000"))
  => +account-remove-single-check+

  (!.py
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (util/remove-single rows
                        sample/Schema
                        "UserAccount"
                        "00000000-0000-0000-0000-000000000000"))
  => +account-remove-single-check+)

^{:refer xt.db.system.memory-util/remove-bulk :added "4.1"}
(fact "removes bulk data"

  (!.js
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (var removed (util/remove-bulk rows
                                   sample/Schema
                                   "UserAccount"
                                   ["00000000-0000-0000-0000-000000000000"]))
    [(xtd/get-in (xtd/first removed) ["record" "id"])
     (util/get-ids rows "UserAccount")])
  => (just ["00000000-0000-0000-0000-000000000000"
            empty?])

  (!.py
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (var removed (util/remove-bulk rows
                                   sample/Schema
                                   "UserAccount"
                                   ["00000000-0000-0000-0000-000000000000"]))
    [(xtd/get-in (xtd/first removed) ["record" "id"])
     (util/get-ids rows "UserAccount")])
  => (just ["00000000-0000-0000-0000-000000000000"
            empty?]))

^{:refer xt.db.system.memory-util/add-single-link-entry :added "4.1"}
(fact "adds a single link entry for one side"

  (!.js
    (var rows {})
    (var added {"id" nil})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-omit sample/RootUser ["profile"])
                                     {})
                     nil)
    [(-> (util/add-single-link-entry rows
                                     "UserAccount"
                                     "00000000-0000-0000-0000-000000000000"
                                     "rev_links"
                                     "profile"
                                     "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                                     (fn [link-id]
                                       (xtd/set-in added ["id"] link-id))
                                     "UserProfile"
                                     "account")
         (xtd/get-in ["record" "rev_links" "profile"]))
     (. added ["id"])])
  => [{"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}
      "c4643895-b0ce-44cc-b07b-2386bf18d43b"]

  (!.py
    (var rows {})
    (var added {"id" nil})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-omit sample/RootUser ["profile"])
                                     {})
                     nil)
    [(-> (util/add-single-link-entry rows
                                     "UserAccount"
                                     "00000000-0000-0000-0000-000000000000"
                                     "rev_links"
                                     "profile"
                                     "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                                     (fn [link-id]
                                       (xtd/set-in added ["id"] link-id))
                                     "UserProfile"
                                     "account")
         (xtd/get-in ["record" "rev_links" "profile"]))
     (. added ["id"])])
  => [{"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}
      "c4643895-b0ce-44cc-b07b-2386bf18d43b"])

^{:refer xt.db.system.memory-util/add-single-link :added "4.1"}
(fact "adds a single link"

  (!.js
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-omit sample/RootUser ["emails" "profile"])
                                     {})
                     nil)
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {}) nil)
    (util/add-single-link rows
                          sample/Schema
                          "UserAccount"
                          "00000000-0000-0000-0000-000000000000"
                          "profile"
                          "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true]

  (!.py
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-omit sample/RootUser ["emails" "profile"])
                                     {})
                     nil)
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {}) nil)
    (util/add-single-link rows
                          sample/Schema
                          "UserAccount"
                          "00000000-0000-0000-0000-000000000000"
                          "profile"
                          "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true])

^{:refer xt.db.system.memory-util/add-bulk-links :added "4.1"
  :setup [(def +account-add-bulk-links-check+
            (just
             [(just [{"table" "UserAccount",
                      "id" "00000000-0000-0000-0000-000000000000",
                      "field" "profile",
                      "link_id" "c4643895-b0ce-44cc-b07b-2386bf18d43b"}
                     {"table" "UserProfile",
                      "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                      "field" "account",
                      "link_id" "00000000-0000-0000-0000-000000000000"}]
                    :in-any-order)
              {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}]))]}
(fact "adds bulk links from flattened data"

  (!.js
    (var flat (f/flatten sample/Schema
                         "UserAccount"
                         sample/RootUser
                         {}))
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-omit sample/RootUser ["emails" "profile"])
                                     {})
                     nil)
    (util/merge-bulk rows (xtd/obj-omit flat ["UserAccount"]) nil)
    [(util/add-bulk-links rows sample/Schema flat)
     (xtd/get-in rows ["UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "rev_links"
                       "profile"])])
  => +account-add-bulk-links-check+

  (!.py
    (var flat (f/flatten sample/Schema
                         "UserAccount"
                         sample/RootUser
                         {}))
    (var rows {})
    (util/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-omit sample/RootUser ["emails" "profile"])
                                     {})
                     nil)
    (util/merge-bulk rows (xtd/obj-omit flat ["UserAccount"]) nil)
    [(util/add-bulk-links rows sample/Schema flat)
     (xtd/get-in rows ["UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "rev_links"
                       "profile"])])
  => +account-add-bulk-links-check+)

^{:refer xt.db.system.memory-util/add-bulk :added "4.1"
  :setup [(def +account-add-bulk-check+
            (just
             [(just [{"table" "UserAccount",
                      "id" "00000000-0000-0000-0000-000000000000",
                      "field" "profile",
                      "link_id" "c4643895-b0ce-44cc-b07b-2386bf18d43b"}
                     {"table" "UserProfile",
                      "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                      "field" "account",
                      "link_id" "00000000-0000-0000-0000-000000000000"}]
                    :in-any-order)
              {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}
              {"account" {"00000000-0000-0000-0000-000000000000" true}}]))]}
(fact "merges nested data and adds links"

  (!.js
    (var rows {})
    [(util/add-bulk rows sample/Schema {"UserAccount" [sample/RootUser]})
     (xtd/get-in rows ["UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "rev_links"
                       "profile"])
     (xtd/get-in rows ["UserProfile"
                       "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                       "record"
                       "ref_links"])])
  => +account-add-bulk-check+

  (!.py
    (var rows {})
    [(util/add-bulk rows sample/Schema {"UserAccount" [sample/RootUser]})
     (xtd/get-in rows ["UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "rev_links"
                       "profile"])
     (xtd/get-in rows ["UserProfile"
                       "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                       "record"
                       "ref_links"])])
  => +account-add-bulk-check+)
