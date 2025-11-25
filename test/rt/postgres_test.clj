(ns rt.postgres-test
  (:use code.test)
  (:require [rt.postgres :as pg]
            [std.lang :as l]
            [std.lib :as h]
            [rt.postgres.grammar.common-application :as app]))

^{:refer rt.postgres/purge-postgres :added "4.0"}
(fact "purges the rt.postgres library. Used for debugging"
  (with-redefs [l/purge-book! (fn [& _] nil)
                l/default-library (fn [] nil)
                l/runtime-library (fn [] nil)]
    (pg/purge-postgres))
  => nil)

^{:refer rt.postgres/purge-scratch :added "4.0"}
(fact "purges the rt.postgres scratch library. Used for debugging"
  (with-redefs [l/delete-module! (fn [& _] nil)
                l/default-library (fn [] nil)
                l/runtime-library (fn [] nil)
                app/app-clear (fn [& _] nil)]
    (pg/purge-scratch))
  => nil)

^{:refer rt.postgres/get-rev :added "4.0"}
(fact "formats access table"
  (pg/get-rev {:reverse {:table "table" :clause {:a 1}}} 'sym {:b 2})
  => '(rt.postgres/g:get "table" {:where {:a 1 :b 2}}))
