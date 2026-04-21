(ns rt.postgres.runtime.builtin-test
  (:use code.test)
  (:require [rt.postgres.runtime.builtin :as builtin]))

^{:refer rt.postgres.runtime.builtin/pg-tmpl :added "4.1"}
(fact "create a postgres template"

  (builtin/pg-tmpl 'hello)
  => '(def$.pg hello hello))
