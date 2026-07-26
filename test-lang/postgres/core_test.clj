(ns postgres.core-test
  (:require [postgres.core :as pg]
            [tahto.runtime.postgres.base.application :as app]
            [tahto.core :as l])
  (:use code.test))

^{:refer postgres.core/purge-postgres :added "4.0"}
(fact "purges the postgres.core library. Used for debugging")

^{:refer postgres.core/purge-scratch :added "4.0"}
(fact "purges the postgres.core scratch library. Used for debugging")
