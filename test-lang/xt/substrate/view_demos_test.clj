(ns xt.substrate.view-demos-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:langs [:dart]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.substrate.view :as view]
             [xt.substrate.view-catalog :as catalog]
             [xt.substrate.view-demos :as demos]]})

^{:refer xt.substrate.view-demos/kitchen-sink-spec :added "4.1"}
(fact "the kitchen sink validates as portable, serializable grammar"
  (!.js
   (var spec (demos/kitchen-sink-spec))
   [(view/validate spec)
    (view/validate-portable spec)
    (view/json-safe? (xt/x:get-key spec "root"))])
  => [true true true])

^{:refer xt.substrate.view-demos/collect-ids :added "4.1"}
(fact "the kitchen sink uses every component in the catalog"
  (!.js
   (var ids (demos/collect-ids (demos/kitchen-sink-render {}) []))
   (var missing [])
   (xt/for:object [[component-id _] catalog/COMPONENTS]
     (when (not (demos/has-val? ids component-id))
       (xt/x:arr-push missing component-id)))
   missing)
  => [])

^{:refer xt.substrate.view-demos/web-escape-spec :added "4.1"}
(fact "the fg/ escape demo validates by default and fails portable validation"
  (!.js
   (var spec (demos/web-escape-spec))
   (var portable-rejected false)
   (try
    (view/validate-portable spec)
    (catch err (:= portable-rejected true)))
   [(view/validate spec) portable-rejected])
  => [true true])
