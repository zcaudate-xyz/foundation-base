(ns xt.db.impl-cache-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.db.impl-cache :as impl-cache]
              [xt.lang.common-lib :as k]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-repl :as repl]
              [xt.db.base-flatten :as f]
              [xt.db.cache-util :as ut]
             [xt.db.sample-test :as sample]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.db.impl-cache :as impl-cache]
              [xt.lang.common-lib :as k]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-repl :as repl]
              [xt.db.base-flatten :as f]
              [xt.db.cache-util :as ut]
             [xt.db.sample-test :as sample]]})

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:scaffold :js)
                 true)
             (do (l/rt:scaffold :lua)
                 true)
             (do (!.js (:= (!:G INSTANCE)
                           {:rows {}}))
                 (!.lua (:= (!:G INSTANCE)
                            {:rows {}})))]
  :teardown [(l/rt:stop)]})


^{:refer xt.db.impl-cache/cache-process-event-remove.lua :adopt true :added "4.0"
  :setup [(!.lua
           (impl-cache/cache-process-event-sync
            INSTANCE
            "add"
            {"UserAccount" [sample/RootUser]}
            sample/Schema
            sample/SchemaLookup
            nil))]}
(fact "removes data from database"
  ^:hidden
  
  (!.lua
   (impl-cache/cache-pull-sync
    INSTANCE
    sample/Schema
    ["UserAccount"
     ["nickname"
      ["profile"
       ["first_name"]]]]
    nil))
  => [{"nickname" "root", "profile" [{"first_name" "Root"}]}]
  
  (!.lua
   (impl-cache/cache-process-event-remove
    INSTANCE
    "input"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    nil))
  => [["UserAccount" ["00000000-0000-0000-0000-000000000000"]]
      ["UserProfile" ["c4643895-b0ce-44cc-b07b-2386bf18d43b"]]]

  (sort (!.lua
        (impl-cache/cache-process-event-remove
         INSTANCE
         "remove" {"UserAccount" [sample/RootUser]}
         sample/Schema
         sample/SchemaLookup
         nil)))
  => ["UserAccount" "UserProfile"]
  
  (!.lua
   (impl-cache/cache-pull-sync
    INSTANCE
    sample/Schema
    ["UserAccount"
     ["nickname"
      ["profile"
       ["first_name"]]]]
    nil))
  => empty?)

^{:refer xt.db.impl-cache/cache-process-event-sync :added "4.0"}
(fact "processes event sync data from database")

^{:refer xt.db.impl-cache/cache-process-event-remove :added "4.0"
  :setup [(!.js
           (xtd/arr-sort (impl-cache/cache-process-event-sync
                          INSTANCE
                          "add"
                          {"UserAccount" [sample/RootUser]}
                          sample/Schema
                          sample/SchemaLookup
                          nil)
                         k/identity
                         k/lt))]}
(fact "removes data from database"
  ^:hidden
  
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

  (sort (!.js
        (impl-cache/cache-process-event-remove
         INSTANCE
         "remove" {"UserAccount" [sample/RootUser]}
         sample/Schema
         sample/SchemaLookup
         nil)))
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

^{:refer xt.db.impl-cache/cache-pull-sync :added "4.0"
  :setup [(def +account+
            (contains-in
             [{"is_official" false
               "nickname" "root",
               "profile"
               [{"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                 "last_name" "User",
                 "first_name" "Root",
                 "language" "en"}],
               "id" "00000000-0000-0000-0000-000000000000",
               "is_suspended" false
               "password_updated" number?
               "is_super" true}]))]}
(fact "runs a pull statement"
  ^:hidden

  [(set (!.js
         (impl-cache/cache-process-event-sync
          INSTANCE
          "add"
          {"Currency" (@! sample/+currency+)}
          sample/Schema
          sample/SchemaLookup
          nil)
         
         (impl-cache/cache-pull-sync INSTANCE
                                     sample/Schema
                                     ["Currency"
                                      ["id"]]
                                     nil)))
   (!.js
    (impl-cache/cache-process-event-sync
     INSTANCE
     "add"
     {"UserAccount" [sample/RootUser]}
     sample/Schema
     sample/SchemaLookup
     nil)
    (impl-cache/cache-pull-sync
     INSTANCE
     sample/Schema
     ["UserAccount"
      ["*/data"
       ["profile"]]]
     nil))]
  => (contains [#{{"id" "USD"} {"id" "XLM.T"} {"id" "STATS"} {"id" "XLM"}}
                +account+])
  
  
  [(set (!.lua
         (impl-cache/cache-process-event-sync
          INSTANCE
          "add"
          {"Currency" (@! sample/+currency+)}
          sample/Schema
          sample/SchemaLookup
          nil)
         
         (impl-cache/cache-pull-sync INSTANCE
                                     sample/Schema
                                     ["Currency"
                                      ["id"]]
                                     nil)))
   (!.lua
    (impl-cache/cache-process-event-sync
     INSTANCE
     "add"
     {"UserAccount" [sample/RootUser]}
     sample/Schema
     sample/SchemaLookup
     nil)
    (impl-cache/cache-pull-sync INSTANCE
                            sample/Schema
                            ["UserAccount"
                             ["*/data"
                              ["profile"]]]
                            nil))]
  => (contains [#{{"id" "USD"} {"id" "XLM.T"} {"id" "STATS"} {"id" "XLM"}}
                +account+]))

^{:refer xt.db.impl-cache/cache-delete-sync :added "4.0"}
(fact "deletes sync data from cache db")


{:refer xt.db.impl-cache/cache-pull-sync :added "4.0"}
(fact "sample of different types of pull"
  ^:hidden
  
  (!.js
   (impl-cache/cache-pull-sync INSTANCE
                               sample/Schema
                               ["Currency"
                                {:id ["like" "ST%"]}
                                ["id"]]
                               nil))
  => [{"id" "STATS"}]
  
  
  (!.js
   (impl-cache/cache-pull-sync INSTANCE
                               sample/Schema
                               ["Currency"
                                {:symbol "XLM"
                                 :id "XLM"
                                 :decimal ["lt" 0]}
                                ["id"]]
                               nil))
  => [{"id" "XLM"}]
  
  (!.js
   (impl-cache/cache-pull-sync INSTANCE
                               sample/Schema
                               ["UserAccount"
                                {:profile "c4643895-b0ce-44cc-b07b-2386bf18d43b"}
                                ["id"]]
                               nil))
  => [{"id" "00000000-0000-0000-0000-000000000000"}]


  (!.js
   (impl-cache/cache-pull-sync INSTANCE
                               sample/Schema
                               ["UserAccount"
                                {:profile "c4643895-b0ce-44cc-b07b-2386bf18d43b"}
                                ["*/data"
                                 ["profile"
                                  ["first_name"]]]]
                               nil))
  => [{"is_official" false,
       "nickname" "root",
       "profile" [{"first_name" "Root"}],
       "id" "00000000-0000-0000-0000-000000000000",
       "is_suspended" false,
       "password_updated" 1630408723423619,
       "is_super" true}])


^{:refer xt.db.impl-cache/cache-clear :added "4.0"}
(fact "clears the cache")
