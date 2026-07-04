(ns js.module-test
  (:require [hara.lang :as l]
            [js.module :refer :all]
            [xt.lang.common-module :as module])
  (:use code.test))

(l/script- :js
  {:require [[js.module :as jm]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.module/import-missing :added "4.0"}
(fact "generates imports for natives that are linked but not current"
  (with-redefs [module/current-natives (fn ([_] {})
                                         ([_ _] {}))
                module/linked-natives  (fn ([_] {"node-fetch" {:as 'fetch}})
                                         ([_ _] {}))]
    (!.js (jm/import-missing)))
  => "import fetch from 'node-fetch'"

  (with-redefs [module/current-natives (fn ([_] {"node-fetch" {:as 'fetch}})
                                         ([_ _] {}))
                module/linked-natives  (fn ([_] {"node-fetch" {:as 'fetch}})
                                         ([_ _] {}))]
    (!.js (jm/import-missing)))
  => "")

^{:refer js.module/import-set-global :added "4.0"}
(fact "sets linked natives as properties on globalThis"
  (with-redefs [module/linked-natives (fn ([_] {"uuid" {:as 'uuid}})
                                        ([_ _] {}))]
    (!.js (jm/import-set-global)))
  => "if(!globalThis.uuid){\n  Object.defineProperty(globalThis,\"uuid\",{\"value\":uuid,\"writeable\":true});\n}"

  (with-redefs [module/linked-natives (fn ([_] {})
                                        ([_ _] {}))]
    (!.js (jm/import-set-global)))
  => "")
