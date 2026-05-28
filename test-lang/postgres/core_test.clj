(ns postgres.core-test
  (:require [postgres.core :as pg]
            [hara.runtime.postgres.base.application :as app]
            [hara.lang :as l])
  (:use code.test))

^{:refer postgres.core/purge-postgres :added "4.0"}
(fact "purges the postgres.core library. Used for debugging")

^{:refer postgres.core/purge-scratch :added "4.0"}
(fact "purges the postgres.core scratch library. Used for debugging")
