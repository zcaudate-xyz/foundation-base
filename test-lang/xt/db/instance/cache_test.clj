(ns xt.db.impl.cache-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.impl.cache :as impl-cache]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:scaffold :js)
                 true)
             (do (!.js (:= (!:G INSTANCE) {:rows {}}))
                 true)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.impl.cache/cache-process-event-remove :added "4.0"}
(fact "processes and removes cached data"

  (!.js
   (:= (!:G INSTANCE) {:rows {}})
   (xtd/arr-sort (impl-cache/cache-process-event-sync
                  INSTANCE
                  "add"
                  {"UserAccount" [sample/RootUser]}
                  sample/Schema
                  sample/SchemaLookup
                  nil)
                 k/identity
                 k/lt))
  => ["UserAccount" "UserProfile"]

  (!.js
   (impl-cache/cache-pull-sync
    INSTANCE
    sample/Schema
    ["UserAccount"
     ["nickname"
      ["profile"
       ["first_name"]]]]
    nil))
  => [{"nickname" "root", "profile" [{"first_name" "Root"}]}]

  (!.js
   (impl-cache/cache-process-event-remove
    INSTANCE
    "input"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    nil))
  => [["UserAccount" ["00000000-0000-0000-0000-000000000000"]]
      ["UserProfile" ["c4643895-b0ce-44cc-b07b-2386bf18d43b"]]]

  (!.js
   (xtd/arr-sort
    (impl-cache/cache-process-event-remove
     INSTANCE
     "remove"
     {"UserAccount" [sample/RootUser]}
     sample/Schema
     sample/SchemaLookup
     nil)
    k/identity
    k/lt))
  => ["UserAccount" "UserProfile"]

  (!.js
   (impl-cache/cache-pull-sync
    INSTANCE
    sample/Schema
    ["UserAccount"
     ["nickname"
      ["profile"
       ["first_name"]]]]
    nil))
  => empty?)

^{:refer xt.db.impl.cache/cache-clear :added "4.0"}
(fact "clears cache rows"

  (!.js
   (:= (!:G INSTANCE) {:rows {}})
   (impl-cache/cache-process-event-sync
    INSTANCE
    "add"
    {"Currency" (@! sample/+currency+)}
    sample/Schema
    sample/SchemaLookup
    nil)
   (impl-cache/cache-clear INSTANCE)
   (impl-cache/cache-pull-sync
    INSTANCE
    sample/Schema
    ["Currency" ["id"]]
    nil))
  => empty?)
