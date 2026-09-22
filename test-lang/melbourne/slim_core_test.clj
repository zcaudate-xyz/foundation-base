(ns melbourne.slim-core-test
  (:use code.test)
  (:require [clojure.string :as string]
            [lang.core :as l]
            [melbourne.slim-core :refer :all]))

^{:refer melbourne.slim-core/useLocalPrimitives :added "4.1"}
(fact "keeps local control defaults in the shared core"
  (let [entry (l/sym-entry :js 'melbourne.slim-core/useLocalPrimitives)
        form  (pr-str (:form entry))]
    (:op entry) => 'defn
    (:lang entry) => :js
    (contains? (:deps-fragment entry) 'js.react/local) => true
    (string/includes? form "showHeader") => true
    (string/includes? form "showScroll") => true))

^{:refer melbourne.slim-core/useRoutePrimitives :added "4.1"}
(fact "keeps route-backed control parameters in the shared core"
  (let [form (pr-str (:form (l/sym-entry :js 'melbourne.slim-core/useRoutePrimitives)))]
    (string/includes? form "\"section\"") => true
    (string/includes? form "\"create\"") => true
    (string/includes? form "\"modify\"") => true
    (string/includes? form "\"detail\"") => true
    (string/includes? form "\"orderBy\"") => true))

^{:refer melbourne.slim-core/useListControl :added "4.1"}
(fact "derives list state and reset behavior from the shared control"
  (let [entry (l/sym-entry :js 'melbourne.slim-core/useListControl)
        form  (pr-str (:form entry))]
    (contains? (:deps-fragment entry) 'js.react/local) => true
    (string/includes? form "\"modify\"") => true
    (string/includes? form "\"detail\"") => true
    (string/includes? form "\"create\"") => true
    (string/includes? form "setShowDetail nil") => true
    (string/includes? form "setShowCreate false") => true))

^{:refer melbourne.slim-core/useRouteControl :added "4.1"}
(fact "composes route primitives with list control"
  (let [form (pr-str (:form (l/sym-entry :js 'melbourne.slim-core/useRouteControl)))]
    (string/includes? form "melbourne.slim-core/useRoutePrimitives") => true
    (string/includes? form "melbourne.slim-core/useListControl") => true
    (not (string/includes? form "melbourne.ui-")) => true))

^{:refer melbourne.slim-core/useLocalControl :added "4.1"}
(fact "composes local primitives with list control and overrides"
  (let [form (pr-str (:form (l/sym-entry :js 'melbourne.slim-core/useLocalControl)))]
    (string/includes? form "melbourne.slim-core/useLocalPrimitives") => true
    (string/includes? form "melbourne.slim-core/useListControl") => true
    (string/includes? form "control m") => true))

^{:refer melbourne.slim-core/getParentProps :added "4.1"}
(fact "passes only the supported parent contract"
  (pr-str (:form (l/sym-entry :js 'melbourne.slim-core/getParentProps)))
  => "(defn getParentProps [props] (return (xt.lang.common-data/obj-pick props [\"entry\" \"data\" \"parent\" \"display\" \"control\" \"actions\"])))")

^{:refer melbourne.slim-core/useParentControl :added "4.1"}
(fact "restores parent visibility around child control state"
  (let [entry (l/sym-entry :js 'melbourne.slim-core/useParentControl)
        form  (pr-str (:form entry))]
    (contains? (:deps-fragment entry) 'js.react/init) => true
    (contains? (:deps-fragment entry) 'js.react/watch) => true
    (string/includes? form "setShowScroll false") => true
    (string/includes? form "setShowHeader false") => true
    (string/includes? form "setShowHeader true") => true))
