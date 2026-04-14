(ns xt.db.cache-util-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.db.cache-util :as data]
             [xt.db.base-flatten :as f]
             [xt.db.sample-test :as sample]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.db.cache-util :as data]
             [xt.db.base-flatten :as f]
             [xt.db.sample-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.db.cache-util :as data]
             [xt.db.base-flatten :as f]
             [xt.db.sample-test :as sample]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (l/rt:scaffold :lua)
             (l/rt:scaffold :python)
             (def +flattened+
               (!.js
                (f/flatten sample/Schema
                           "UserAccount"
                           sample/RootUser
                           {})))]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.cache-util/has-entry :added "4.0"}
(fact "checks if entry exists"
  ^:hidden

  (!.js
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/has-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => true

  (!.lua
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/has-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => true

  (!.py
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/has-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => true)

^{:refer xt.db.cache-util/get-entry :added "4.0"}
(fact "gets entry by id"
  ^:hidden

  (!.js
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/get-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => map?

  (!.lua
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/get-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => map?

  (!.py
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/get-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => map?)

^{:refer xt.db.cache-util/swap-if-entry :added "4.0"}
(fact "modifies entry if exists"
  ^:hidden
  
  (!.js
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (-> (data/swap-if-entry rows
                           "UserAccount" "00000000-0000-0000-0000-000000000000"
                           (fn [record]
                             (return (xtd/set-in record ["data" "foo"] "hello"))))
        (xtd/get-in ["record" "data" "foo"])))
  => "hello"

  (!.lua
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (-> (data/swap-if-entry rows
                           "UserAccount" "00000000-0000-0000-0000-000000000000"
                           (fn [record]
                             (return (xtd/set-in record ["data" "foo"] "hello"))))
        (xtd/get-in ["record" "data" "foo"])))
  => "hello"

  (!.py
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (-> (data/swap-if-entry rows
                           "UserAccount" "00000000-0000-0000-0000-000000000000"
                           (fn [record]
                             (return (xtd/set-in record ["data" "foo"] "hello"))))
        (xtd/get-in ["record" "data" "foo"])))
  => "hello")

^{:refer xt.db.cache-util/merge-single :added "4.0"}
(fact "merges a single entry"
  ^:hidden

  (!.js
   (data/merge-single {}
                     "UserAccount"
                     "00000000-0000-0000-0000-000000000001"
                     {:id "00000000-0000-0000-0000-000000000001"
                      :data {}
                      :ref-links {}
                      :rev-links {}}
                     k/identity))
  => (contains {"record" {"ref_links" {}, "id" "00000000-0000-0000-0000-000000000001", "rev_links" {}, "data" {}},
                "t" number?})

  (!.lua
   (data/merge-single {}
                     "UserAccount"
                     "00000000-0000-0000-0000-000000000001"
                     {:id "00000000-0000-0000-0000-000000000001"
                      :data {}
                      :ref-links {}
                      :rev-links {}}
                     k/identity))
  => (contains {"record" {"ref_links" {}, "id" "00000000-0000-0000-0000-000000000001", "rev_links" {}, "data" {}},
                "t" number?})

  (!.py
   (data/merge-single {}
                     "UserAccount"
                     "00000000-0000-0000-0000-000000000001"
                     {:id "00000000-0000-0000-0000-000000000001"
                      :data {}
                      :ref-links {}
                      :rev-links {}}
                     k/identity))
  => (contains {"record" {"ref_links" {}, "id" "00000000-0000-0000-0000-000000000001", "rev_links" {}, "data" {}},
                "t" number?}))

^{:refer xt.db.cache-util/merge-bulk :added "4.0"}
(fact "merges flattened data into the database"
  ^:hidden

  (!.js
   (var rows {})
   [(data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (data/get-ids rows "UserAccount")])
  => (contains-in
      [map? ["00000000-0000-0000-0000-000000000000"]])
  
  (!.lua
   (var rows {})
   [(data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (data/get-ids rows "UserAccount")])
  => (contains-in
      [map? ["00000000-0000-0000-0000-000000000000"]])
  
  (!.py
   (var rows {})
   [(data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (data/get-ids rows "UserAccount")])
  => (contains-in
      [map? ["00000000-0000-0000-0000-000000000000"]]))

^{:refer xt.db.cache-util/get-ids :added "4.0"}
(fact "get ids for table-key")

^{:refer xt.db.cache-util/all-records :added "4.0"}
(fact "returns all records"
  ^:hidden

  (!.js
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/all-records rows "UserAccount"))
  => map?

  (!.lua
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/all-records rows "UserAccount"))
  => map?

  (!.py
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/all-records rows "UserAccount"))
  => map?)

^{:refer xt.db.cache-util/get-changed-single :added "4.0"}
(fact "gets changed record"
  ^:hidden

  (!.js
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (var changed (-> (data/get-entry rows  "UserAccount" "00000000-0000-0000-0000-000000000000")
                    (. ["record"])
                    (xtd/clone-nested)
                    (xtd/set-in ["data" "nickname"] "hello")))
   
   (data/get-changed-single rows
                            "UserAccount" "00000000-0000-0000-0000-000000000000"
                            changed))
  => {"data" {"nickname" "hello"}}

  (!.lua
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (var changed (-> (data/get-entry rows  "UserAccount" "00000000-0000-0000-0000-000000000000")
                    (. ["record"])
                    (xtd/clone-nested)
                    (xtd/set-in ["data" "nickname"] "hello")))
   
   (data/get-changed-single rows
                            "UserAccount" "00000000-0000-0000-0000-000000000000"
                            changed))
  => {"data" {"nickname" "hello"}}

  (!.py
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (var changed (-> (data/get-entry rows  "UserAccount" "00000000-0000-0000-0000-000000000000")
                    (. ["record"])
                    (xtd/clone-nested)
                    (xtd/set-in ["data" "nickname"] "hello")))
   
   (data/get-changed-single rows
                            "UserAccount" "00000000-0000-0000-0000-000000000000"
                            changed))
  => {"data" {"nickname" "hello"}})

^{:refer xt.db.cache-util/has-changed-single :added "4.0"}
(fact "checks if record has changed"
  ^:hidden
  
  (!.js
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (var changed (-> (data/get-entry rows  "UserAccount" "00000000-0000-0000-0000-000000000000")
                    (. ["record"])
                    (xtd/clone-nested)
                    (xtd/set-in ["data" "nickname"] "hello")))
   (data/has-changed-single rows "UserAccount" "00000000-0000-0000-0000-000000000000"
                            changed))
  => true

  (!.lua
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (var changed (-> (data/get-entry rows  "UserAccount" "00000000-0000-0000-0000-000000000000")
                    (. ["record"])
                    (xtd/clone-nested)
                    (xtd/set-in ["data" "nickname"] "hello")))
   (data/has-changed-single rows "UserAccount" "00000000-0000-0000-0000-000000000000"
                            changed))
  => true

  (!.py
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (var changed (-> (data/get-entry rows  "UserAccount" "00000000-0000-0000-0000-000000000000")
                    (. ["record"])
                    (xtd/clone-nested)
                    (xtd/set-in ["data" "nickname"] "hello")))
   (data/has-changed-single rows "UserAccount" "00000000-0000-0000-0000-000000000000"
                            changed))
  => true)

^{:refer xt.db.cache-util/get-link-attrs :added "4.0"
  :setup [(def +attrs+
            {"table_link" "rev_links",
             "inverse_link" "ref_links",
             "table_key" "UserAccount",
             "table_field" "profile",
             "inverse_key" "UserProfile",
             "inverse_field" "account"})]}
(fact "find link attributes"
  ^:hidden
  
  (!.js (data/get-link-attrs sample/Schema "UserAccount" "profile"))
  => +attrs+
  
  (!.lua (data/get-link-attrs sample/Schema "UserAccount" "profile"))
  => +attrs+

  (!.py (data/get-link-attrs sample/Schema "UserAccount" "profile"))
  => +attrs+)

^{:refer xt.db.cache-util/remove-single-link-entry :added "4.0"}
(fact "removes single link for entry")

^{:refer xt.db.cache-util/remove-single-link :added "4.0"}
(fact "removes single link"
  ^:hidden

  (!.js
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/remove-single-link rows
                            sample/Schema
                            "UserAccount"
                            "00000000-0000-0000-0000-000000000000"
                            "profile"
                            "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true]

  (!.lua
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/remove-single-link rows
                            sample/Schema
                            "UserAccount"
                            "00000000-0000-0000-0000-000000000000"
                            "profile"
                            "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true]

  (!.py
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/remove-single-link rows
                            sample/Schema
                            "UserAccount"
                            "00000000-0000-0000-0000-000000000000"
                            "profile"
                            "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true])

^{:refer xt.db.cache-util/remove-single :added "4.0"}
(fact "removes a single entry"
  ^:hidden

  (!.js
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/remove-single rows
                       sample/Schema
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"))
  => vector?

  (!.js
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/remove-single rows
                       sample/Schema
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"))
  => vector?

  
  (!.py
   (var rows {})
   (data/merge-bulk rows (@! +flattened+) nil)
   (data/remove-single rows
                       sample/Schema
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"))
  => vector?)

^{:refer xt.db.cache-util/remove-bulk :added "4.0"}
(fact "removes bulk data")

^{:refer xt.db.cache-util/add-single-link-entry :added "4.0"}
(fact "adds single link entry for one side")

^{:refer xt.db.cache-util/add-single-link :added "4.0"
  :setup [(def +account+
            (!.js
             (f/flatten sample/Schema
                        "UserAccount"
                        (xtd/obj-omit sample/RootUser ["emails" "profile"])
                        {})))
          
          (def +profile+
            (dissoc (!.js
                     (f/flatten sample/Schema
                                "UserAccount"
                                sample/RootUser
                                {}))
                    "UserAccount"))]}
(fact "adds single link"
  ^:hidden
  
  (!.js
   (var rows {})
   (data/merge-bulk rows (@! +account+) nil)
   (data/merge-bulk rows (@! +profile+) nil)
   (data/add-single-link rows
                         sample/Schema
                         "UserAccount"
                         "00000000-0000-0000-0000-000000000000"
                         "profile"
                         "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true]

  (!.lua
   (var rows {})
   (data/merge-bulk rows (@! +account+) nil)
   (data/merge-bulk rows (@! +profile+) nil)
   (data/add-single-link rows
                         sample/Schema
                         "UserAccount"
                         "00000000-0000-0000-0000-000000000000"
                         "profile"
                         "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true]

  (!.py
   (var rows {})
   (data/merge-bulk rows (@! +account+) nil)
   (data/merge-bulk rows (@! +profile+) nil)
   (data/add-single-link rows
                         sample/Schema
                         "UserAccount"
                         "00000000-0000-0000-0000-000000000000"
                         "profile"
                         "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true])

^{:refer xt.db.cache-util/add-bulk-links :added "4.0"}
(fact "adding bulk links from external data (to be doubly sure)")

(comment
  (./create-tests)
  (./import)
  )
