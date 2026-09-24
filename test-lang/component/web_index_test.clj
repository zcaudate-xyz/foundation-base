(ns lang-main.ui-components.web-index-main-test
  (:require [clojure.string :as string]
            [lang.core :as l]
            [lang.core.impl :as impl]
            [lang-main.ui-components.web-index-main])
  (:use code.test))

^{:refer lang-main.ui-components.web-index-main/AppMain :added "4.1"}
(fact "binds section and subsection selection to the browser hash route"
  (let [entry (l/sym-entry :js 'lang-main.ui-components.web-index-main/AppMain)
        form (pr-str (:form entry))
        emitted (impl/emit-str (:form entry) {:lang :js})]
    (string/includes? form "getHashRoute") => true
    (string/includes? form "useHashRoute") => true
    (string/includes? form "useRouteSegment route []") => true
    (string/includes? form "useRouteSegment route [l0]") => true
    (not (string/includes? form "ext-box/useBox lang-main.ui-components.web-index-main/Global [\"l0\"]")) => true
    (not (string/includes? form "ext-box/useBox lang-main.ui-components.web-index-main/Global [\"l1\"]")) => true
    (string/includes? emitted "makeRoute") => true
    (string/includes? emitted "useRouteSegment") => true))

^{:refer lang-main.ui-components.web-index-main/__screen__ :added "4.1"}
(fact "initializes every top-level section before route selection"
  (let [entry (l/sym-entry :js 'lang-main.ui-components.web-index-main/__screen__)
        form (pr-str (:form entry))
        emitted (impl/emit-str (:form entry) {:lang :js})]
    (string/includes? form "04-tama") => true
    (string/includes? emitted "04-tama") => true
    (string/includes? emitted "component.web_tama_slim.tama_controls()") => true))
