(ns xt.ui.state.collection-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.state.collection :as collection]]})

^{:refer xt.ui.state.collection/create :added "4.1"}
(fact "creates empty paging state and respects explicit collection options"
  (!.js (collection/create {}))
  => {"items" [] "query" {} "page" 0 "page_size" 25 "total" 0
      "selected" {} "pending" false "error" nil}
  (!.js (collection/create {"items" [{"id" "a"}] "query" {"q" "Ada"} "page" 2 "page_size" 10 "total" 41}))
  => {"items" [{"id" "a"}] "query" {"q" "Ada"} "page" 2 "page_size" 10 "total" 41
      "selected" {} "pending" false "error" nil})

^{:refer xt.ui.state.collection/set-items! :added "4.1"}
(fact "replaces items, derives missing totals and clears pending errors"
  (!.js
   (var current (collection/create {}))
   (xt/x:set-key current "pending" true)
   (xt/x:set-key current "error" "offline")
   (collection/set-items! current [{"id" "a"} {"id" "b"}] nil)
   (var loaded [(. current ["items"]) (. current ["total"]) (. current ["pending"]) (. current ["error"])])
   (collection/set-items! current [] 100)
   (var total (. current ["total"]))
   (collection/set-items! current nil nil)
   [loaded total (. current ["items"]) (. current ["total"])])
  => [[[ {"id" "a"} {"id" "b"}] 2 false nil] 100 [] 0])

^{:refer xt.ui.state.collection/set-query! :added "4.1"}
(fact "copies nested query data, resets the page and preserves unrelated filters"
  (!.js
   (var query {"filter" {"name" "Ada" "active" true}})
   (var current (collection/create {"query" query "page" 4}))
   (collection/set-query! current ["filter" "name"] "Grace")
   [query (. current ["query"]) (. current ["page"])])
  => [{"filter" {"name" "Ada" "active" true}}
      {"filter" {"name" "Grace" "active" true}} 0])

^{:refer xt.ui.state.collection/set-page! :added "4.1"}
(fact "sets the page without changing queries and defaults nil to the first page"
  (!.js
   (var current (collection/create {"query" {"q" "Ada"}}))
   (collection/set-page! current 3)
   (var page (. current ["page"]))
   (collection/set-page! current nil)
   [page (. current ["page"]) (. current ["query"])])
  => [3 0 {"q" "Ada"}])

^{:refer xt.ui.state.collection/select! :added "4.1"}
(fact "selection is idempotent and false or missing flags remove only the requested id"
  (!.js
   (var current (collection/create {}))
   (collection/select! current "a" true)
   (collection/select! current "a" true)
   (collection/select! current "b" true)
   (collection/select! current "a" false)
   (collection/select! current "absent" nil)
   [(. current ["selected"]) (collection/selected-ids current)])
  => [{"b" true} ["b"]])

^{:refer xt.ui.state.collection/selected-ids :added "4.1"}
(fact "returns selected identifiers without changing the selection"
  (!.js
   (var current (collection/create {}))
   (var empty (collection/selected-ids current))
   (collection/select! current "a" true)
   [empty (collection/selected-ids current) (collection/selected-ids current)])
  => [[] ["a"] ["a"]])
