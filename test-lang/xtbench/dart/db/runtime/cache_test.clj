(ns xtbench.dart.db.runtime.cache-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-data :as xtd]
             [xt.db.runtime.cache :as cache]
             [xt.db.runtime.cache-util :as util]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.cache/cache-process-event-sync :added "4.1"}
(fact "flattens input payloads and merges cache rows for add events"

  (!.dt
   (var flat
        (cache/cache-process-event-sync
         {:rows {}}
         "input"
         {"UserAccount" [sample/RootUser]}
         sample/Schema
         sample/SchemaLookup
         nil))
   [(xtd/get-in flat ["UserAccount"
                      "00000000-0000-0000-0000-000000000000"
                      "data"
                      "nickname"])
    (xtd/get-in flat ["UserProfile"
                      "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                      "ref_links"
                      "account"
                      "00000000-0000-0000-0000-000000000000"])])
  => ["root" true]

  (!.dt
   (var cache {:rows {}})
   (var touched
        (cache/cache-process-event-sync
         cache
         "add"
         {"UserAccount" [sample/RootUser]}
         sample/Schema
         sample/SchemaLookup
         nil))
   [(xtd/arr-lookup touched)
    (xtd/get-in cache ["rows"
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "data"
                       "nickname"])
    (xtd/get-in cache ["rows"
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "rev_links"
                       "profile"
                       "c4643895-b0ce-44cc-b07b-2386bf18d43b"])])
  => [{"UserAccount" true "UserProfile" true}
      "root"
      true])

^{:refer xt.db.runtime.cache/cache-process-event-remove :added "4.1"}
(fact "describes delete inputs and removes cache rows in table order"

  (!.dt
   (cache/cache-process-event-remove
    {:rows {}}
    "input"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    nil))
  => [["UserAccount" ["00000000-0000-0000-0000-000000000000"]]
      ["UserProfile" ["c4643895-b0ce-44cc-b07b-2386bf18d43b"]]]

  (!.dt
   (var cache {:rows {}})
   (cache/cache-process-event-sync
    cache
    "add"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    nil)
   (var touched
        (cache/cache-process-event-remove
         cache
         "remove"
         {"UserAccount" [sample/RootUser]}
         sample/Schema
         sample/SchemaLookup
         nil))
   [(xtd/arr-lookup touched)
    (xtd/get-in cache ["rows" "UserAccount" "00000000-0000-0000-0000-000000000000"])
    (xtd/get-in cache ["rows" "UserProfile" "c4643895-b0ce-44cc-b07b-2386bf18d43b"])])
  => [{"UserAccount" true "UserProfile" true}
      nil
      nil])

^{:refer xt.db.runtime.cache/cache-pull-sync :added "4.1"}
(fact "pulls linked data back out of the cache rows"

  (!.dt
   (var cache {:rows {}})
   (cache/cache-process-event-sync
    cache
    "add"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    nil)
   (cache/cache-pull-sync
    cache
    sample/Schema
    ["UserAccount"
     {:id "00000000-0000-0000-0000-000000000000"}
     ["nickname"
      ["profile" ["first_name"]]]]
    nil))
  => [{"nickname" "root"
       "profile" [{"first_name" "Root"}]}])

^{:refer xt.db.runtime.cache/cache-delete-sync :added "4.1"}
(fact "deletes rows directly from the cache db"

  (!.dt
   (var cache {:rows {}})
   (cache/cache-process-event-sync
    cache
    "add"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    nil)
   (var removed
        (cache/cache-delete-sync
         cache
         sample/Schema
         "UserProfile"
         ["c4643895-b0ce-44cc-b07b-2386bf18d43b"]
         nil))
   [(xtd/get-in removed [0 "record" "data" "id"])
     (util/has-entry (. cache ["rows"]) "UserProfile" "c4643895-b0ce-44cc-b07b-2386bf18d43b")
     (xtd/get-in cache ["rows"
                        "UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "rev_links"
                       "profile"])])
   => ["c4643895-b0ce-44cc-b07b-2386bf18d43b" false nil])

^{:refer xt.db.runtime.cache/cache-clear :added "4.1"}
(fact "clears cache rows in place"

  (!.dt
   (var cache {:rows {}})
   (cache/cache-process-event-sync
    cache
    "add"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    nil)
   [(cache/cache-clear cache)
    (. cache ["rows"])])
  => [true {}])
