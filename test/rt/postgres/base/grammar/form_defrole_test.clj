(ns rt.postgres.base.grammar.form-defrole-test
  (:require [rt.postgres.base.grammar.common :as common]
            [rt.postgres.base.grammar.form-defrole :refer :all]
            [std.lang :as l])
  (:use code.test))

^{:refer rt.postgres.base.grammar.form-defrole/pg-defrole-access :added "4.0"}
(fact "creates defrole access form"
  ^:hidden
  
  (pg-defrole-access {:select ['table]} 'role {})
  => vector?)

^{:refer rt.postgres.base.grammar.form-defrole/pg-defrole :added "4.0"}
(fact "creates defrole form"
  ^:hidden
  
  (with-redefs [l/macro-opts (fn [] {})
                common/block-do-suppress (fn [x] x)]
    (pg-defrole '(defrole role {:grant [other]})))
  => '(do (do [:drop :owned-by role] [:drop-role :if-exists role]) (do [:create-role role :inherit]) (do [:grant role :to other])))
