(ns xt.db.runtime.model-view-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.runtime.model-view :as model-view]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.model-view/normalize-sources :added "4.1"}
(fact "normalizes shared primary and caching sources"
  (!.js
    (var out
         (model-view/normalize-sources
          {"primary" {"kind" "sqlite"}}
          {"cache_alt" {"sync_from" "primary"}}))
    [(. out ["primary"] ["kind"])
     (. out ["caching"] ["sync_from"])
     (. out ["cache_alt"] ["sync_from"])])
  => ["sqlite" "primary" "primary"])

^{:refer xt.db.runtime.model-view/normalize-view-source :added "4.1"}
(fact "normalizes view source declarations"
  (!.js
    [(model-view/normalize-view-source {"source" "primary"})
     (model-view/normalize-view-source {"use" {"source" "archive"}})
     (model-view/normalize-view-source {})])
  => ["primary" "archive" "caching"])
