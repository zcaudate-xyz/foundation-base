(ns rt.postgres-test
  (:require [rt.postgres :as pg]
            [rt.postgres.base.application :as app]
            [std.lang :as l])
  (:use code.test))

^{:refer rt.postgres/purge-postgres :added "4.0"}
(fact "purges the rt.postgres library. Used for debugging")

^{:refer rt.postgres/purge-scratch :added "4.0"}
(fact "purges the rt.postgres scratch library. Used for debugging")
