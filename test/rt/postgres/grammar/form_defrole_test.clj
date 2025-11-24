(ns rt.postgres.grammar.form-defrole-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-defrole :refer :all]
            [std.lang :as l]
            [rt.postgres.grammar.common :as common]))

^{:refer rt.postgres.grammar.form-defrole/pg-defrole-access :added "4.0"}
(fact "creates defrole access form"
  (pg-defrole-access {:select ['table]} 'role {})
  => vector?)

^{:refer rt.postgres.grammar.form-defrole/pg-defrole :added "4.0"}
(fact "creates defrole form"
  (with-redefs [l/macro-opts (fn [] {})
                common/block-do-suppress (fn [x] x)]
    (pg-defrole '(defrole role {:grant [other]}))
    => list?))
