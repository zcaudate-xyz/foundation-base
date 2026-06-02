(ns xt.db.runtime.cache-manual-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.runtime.cache :as cache]
             [xt.db.runtime.cache-pull :as pull]
             [xt.db.text.base-flatten :as f]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.cache-manual/basic-cache-ops :added "4.1"}
(fact "demonstrates cache setup, data insertion, and tree-based pull without cache-view"

  (!.js
    ;; 1. Minimal schema
    (var schema sample/SchemaCurrency)

    ;; 2. Create empty cache and put nested data in
    (var cache {:rows {}})
    (cache/cache-process-event-sync
     cache
     "add"
     {"Currency" (@! sample/+currency+)}
     schema
     sample/SchemaLookup
     nil)

    ;; 3. Pull with tree notation - single record by id
    (cache/cache-pull-sync
     cache
     schema
     ["Currency"
      {"id" "USD"}
      ["id" "name"]]))
  => [{"id" "USD" "name" "US Dollar"}]
  
  (!.js
    ;; Pull all fiat currencies
    (var schema sample/SchemaCurrency)
    (var cache {:rows {}})
    (cache/cache-process-event-sync
     cache
     "add"
     {"Currency" (@! sample/+currency+)}
     schema
     sample/SchemaLookup
     nil)
    (cache/cache-pull-sync
     cache
     schema
     ["Currency"
      {"type" "fiat"}
      ["id" "name"]]
     nil))
  => [{"id" "USD" "name" "US Dollar"}]

  (!.js
    ;; Pull wallet with forward link (owner) expanded
    (var schema sample/Schema)
    (var cache {:rows {}})
    (cache/cache-process-event-sync
     cache
     "add"
     {"UserAccount" [{"id" "user-1" "nickname" "root"}]
      "Wallet" [{"id" "wallet-1"
                 "owner" {"id" "user-1"}}]}
     schema
     sample/SchemaLookup
     nil)
    (cache/cache-pull-sync
     cache
     schema
     ["Wallet"
      {"id" "wallet-1"}
      ["id"
       ["owner" ["nickname"]]]]
     nil))
  => [{"id" "wallet-1"
       "owner" [{"nickname" "root"}]}]

  (!.js
    ;; Pull using bulk "in" filter
    (var schema sample/SchemaCurrency)
    (var cache {:rows {}})
    (cache/cache-process-event-sync
     cache
     "add"
     {"Currency" (@! sample/+currency+)}
     schema
     sample/SchemaLookup
     nil)
    (xtd/arr-sort
     (cache/cache-pull-sync
      cache
      schema
      ["Currency"
       {"id" ["in" [["USD" "XLM"]]]}
       ["id" "name"]]
      nil)
     (fn:> [row] (xtd/get-in row ["id"]))
     xt/x:str-comp))
  => [{"id" "USD" "name" "US Dollar"}
      {"id" "XLM" "name" "Stellar Coin"}]

  (!.js
    ;; Low-level pull API with :where / :returning map
    (var schema sample/SchemaCurrency)
    (var cache {:rows {}})
    (cache/cache-process-event-sync
     cache
     "add"
     {"Currency" (@! sample/+currency+)}
     schema
     sample/SchemaLookup
     nil)
    (pull/pull
     (xt/x:get-key cache "rows")
     schema
     "Currency"
     {:where {"type" "crypto"}
      :returning ["id" "name"]}))
  => [{"id" "XLM" "name" "Stellar Coin"}
      {"id" "XLM.T" "name" "Stellar TestNet Coin"}])
