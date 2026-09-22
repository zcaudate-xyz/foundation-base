(ns component.web-index-test
  (:require [clojure.string :as string]
            [lang.core :as l]
            [lang.core.impl :as impl]
            [component.web-index])
  (:use code.test))

^{:refer component.web-index/AppMain :added "4.1"}
(fact "binds section and subsection selection to the browser hash route"
  (let [entry (l/sym-entry :js 'component.web-index/AppMain)
        form (pr-str (:form entry))
        emitted (impl/emit-str (:form entry) {:lang :js})]
    (string/includes? form "getHashRoute") => true
    (string/includes? form "useHashRoute") => true
    (string/includes? form "useRouteSegment route []") => true
    (string/includes? form "useRouteSegment route [l0]") => true
    (not (string/includes? form "ext-box/useBox component.web-index/Global [\"l0\"]")) => true
    (not (string/includes? form "ext-box/useBox component.web-index/Global [\"l1\"]")) => true
    (string/includes? emitted "makeRoute") => true
    (string/includes? emitted "useRouteSegment") => true))

^{:refer component.web-index/__screen__ :added "4.1"}
(fact "initializes every top-level section before route selection"
  (let [entry (l/sym-entry :js 'component.web-index/__screen__)
        form (pr-str (:form entry))
        emitted (impl/emit-str (:form entry) {:lang :js})]
    (string/includes? form "04-tama") => true
    (string/includes? emitted "04-tama") => true
    (string/includes? emitted "component.web_tama_slim.tama_controls()") => true))
