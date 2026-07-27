(ns tahto.model.sql.spec-common-test
  (:use code.test)
  (:require [tahto.model.sql.spec-common :refer :all]))

^{:refer tahto.model.sql.spec-common/build-features :added "4.1"}
(fact "builds sql spec features")

^{:refer tahto.model.sql.spec-common/build-template :added "4.1"}
(fact "builds sql spec templates")

^{:refer tahto.model.sql.spec-common/build-grammar :added "4.1"}
(fact "builds sql spec grammar")

^{:refer tahto.model.sql.spec-common/build-meta :added "4.1"}
(fact "builds sql spec metadata")

^{:refer tahto.model.sql.spec-common/build-book :added "4.1"}
(fact "builds sql spec books")
