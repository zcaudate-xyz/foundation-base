(ns rt.postgres-test
  (:require [rt.postgres :as pg]
            [rt.postgres.grammar.common-application :as app]
            [std.lang :as l])
  (:use code.test))

^{:refer rt.postgres/purge-postgres :added "4.0"}
(fact "purges the rt.postgres library. Used for debugging")

^{:refer rt.postgres/purge-scratch :added "4.0"}
(fact "purges the rt.postgres scratch library. Used for debugging")

^{:refer rt.postgres/get-rev :added "4.0"}
(fact "formats access table"
  (pg/get-rev {:reverse {:table "table" :clause {:a 1}}} 'sym {:b 2})
  => '(rt.postgres/g:get "table" {:where {:a 1 :b 2}}))
