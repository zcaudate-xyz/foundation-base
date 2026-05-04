(ns hara.runtime.postgres-test
  (:require [hara.runtime.postgres :as pg]
            [hara.runtime.postgres.base.application :as app]
            [hara.lang :as l])
  (:use code.test))

^{:refer hara.runtime.postgres/purge-postgres :added "4.0"}
(fact "purges the hara.runtime.postgres library. Used for debugging")

^{:refer hara.runtime.postgres/purge-scratch :added "4.0"}
(fact "purges the hara.runtime.postgres scratch library. Used for debugging")
