tahto/model/spec_postgres/form_defrole_test.clj:1:(ns tahto.model.spec-postgres.form-defrole-test
tahto/model/spec_postgres/form_defrole_test.clj:2:  (:require [tahto.model.spec-postgres.common :as common]
tahto/model/spec_postgres/form_defrole_test.clj:3:            [tahto.model.spec-postgres.form-defrole :refer :all]
            [tahto.core :as l])
  (:use code.test))

tahto/model/spec_postgres/form_defrole_test.clj:7:^{:refer tahto.model.spec-postgres.form-defrole/pg-defrole-access :added "4.0"}
(fact "creates defrole access form"

  (pg-defrole-access {:select ['table]} 'role {})
  => vector?)

tahto/model/spec_postgres/form_defrole_test.clj:13:^{:refer tahto.model.spec-postgres.form-defrole/pg-defrole :added "4.0"}
(fact "creates defrole form"

  (with-redefs [l/macro-opts (fn [] {})
                common/block-do-suppress (fn [x] x)]
    (pg-defrole '(defrole role {:grant [other]})))
  => '(do (do [:drop :owned-by role] [:drop-role :if-exists role]) (do [:create-role role :inherit]) (do [:grant role :to other])))
