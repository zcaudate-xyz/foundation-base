(ns lang.model.spec-postgres.form-defrole-test
  (:require [lang.model.spec-postgres.common :as common]
            [lang.model.spec-postgres.form-defrole :refer :all]
            [lang.core :as l])
  (:use code.test))

^{:refer lang.model.spec-postgres.form-defrole/pg-defrole-access :added "4.0"}
(fact "creates defrole access form"

  (pg-defrole-access {:select ['table]} 'role {})
  => vector?)

^{:refer lang.model.spec-postgres.form-defrole/pg-defrole :added "4.0"}
(fact "creates defrole form"

  (with-redefs [l/macro-opts (fn [] {})
                common/block-do-suppress (fn [x] x)]
    (pg-defrole '(defrole role {:grant [other]})))
  => '(do (do [:drop :owned-by role] [:drop-role :if-exists role]) (do [:create-role role :inherit]) (do [:grant role :to other])))
