(ns xt.substrate.page-spec-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.substrate.page-spec :as page-spec]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.page-spec/resolver-type :added "4.1"}
(fact "uses snake_case resolver defaults"
  (!.js
    [(page-spec/resolver-type {})
     (page-spec/view-default-input {"default_input" ["task-1"]})
     (page-spec/view-source {"use" {"source" "archive"}})
     (page-spec/resolver-action {"action" "entry/get"})])
  => ["fn/local" ["task-1"] "archive" "entry/get"])

^{:refer xt.substrate.page-spec/view-deps :added "4.1"}
(fact "normalizes dependency groups from snake_case specs"
  (!.js
    [(page-spec/view-deps {"deps" ["list"]})
     (page-spec/view-deps {"deps" {"views" ["list"]
                                   "state" ["selected_id"]}})])
  => [{"views" ["list"] "state" []}
      {"views" ["list"] "state" ["selected_id"]}])
