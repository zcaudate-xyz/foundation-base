(ns hara.rt.postgres-test
  (:require [hara.rt.postgres :as pg]
            [hara.rt.postgres.base.application :as app]
            [hara.lang :as l])
  (:use code.test))

^{:refer hara.rt.postgres/purge-postgres :added "4.0"}
(fact "purges the hara.rt.postgres library. Used for debugging")

^{:refer hara.rt.postgres/purge-scratch :added "4.0"}
(fact "purges the hara.rt.postgres scratch library. Used for debugging")
