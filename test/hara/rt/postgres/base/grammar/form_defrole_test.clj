(ns hara.rt.postgres.base.grammar.form-defrole-test
  (:require [hara.rt.postgres.base.grammar.common :as common]
            [hara.rt.postgres.base.grammar.form-defrole :refer :all]
            [hara.lang :as l])
  (:use code.test))

^{:refer hara.rt.postgres.base.grammar.form-defrole/pg-defrole-access :added "4.0"}
(fact "creates defrole access form"

  (pg-defrole-access {:select ['table]} 'role {})
  => vector?)

^{:refer hara.rt.postgres.base.grammar.form-defrole/pg-defrole :added "4.0"}
(fact "creates defrole form"

  (with-redefs [l/macro-opts (fn [] {})
                common/block-do-suppress (fn [x] x)]
    (pg-defrole '(defrole role {:grant [other]})))
  => '(do (do [:drop :owned-by role] [:drop-role :if-exists role]) (do [:create-role role :inherit]) (do [:grant role :to other])))
