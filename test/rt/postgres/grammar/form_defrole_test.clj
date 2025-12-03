(ns rt.postgres.grammar.form-defrole-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-defrole :refer :all]
            [std.lang :as l]
            [rt.postgres.grammar.common :as common]))

^{:refer rt.postgres.grammar.form-defrole/pg-defrole-access :added "4.0"}
(fact "creates defrole access form"
  ^:hidden
  
  (pg-defrole-access {:select ['table]} 'role {})
  => vector?)

^{:refer rt.postgres.grammar.form-defrole/pg-defrole :added "4.0"}
(fact "creates defrole form"
  ^:hidden
  
  (with-redefs [l/macro-opts (fn [] {})
                common/block-do-suppress (fn [x] x)]
    (pg-defrole '(defrole role {:grant [other]})))
  => '(do (do [:drop :owned-by role] [:drop-role :if-exists role]) (do [:create-role role :inherit]) (do [:grant role :to other])))
