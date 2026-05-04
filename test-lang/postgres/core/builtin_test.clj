(ns postgres.core.builtin-test
  (:use code.test)
  (:require [postgres.core.builtin :as builtin]))

^{:refer postgres.core.builtin/pg-tmpl :added "4.1"}
(fact "create a postgres template"

  (builtin/pg-tmpl 'hello)
  => '(def$.pg hello hello))
