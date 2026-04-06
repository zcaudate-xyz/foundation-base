(ns lib.postgres.impl.postgresql-test
  (:use code.test)
  (:require [lib.postgres.impl.postgresql :refer :all]))

^{:refer lib.postgres.impl.postgresql/create-pool :added "4.1"}
(fact "TODO")

^{:refer lib.postgres.impl.postgresql/execute-statement :added "4.1"}
(fact "TODO")