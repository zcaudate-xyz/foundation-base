(ns rt.postgres.script.builtin-test
  (:use code.test)
  (:require [rt.postgres.script.builtin :as builtin]))

^{:refer rt.postgres.script.builtin/pg-tmpl :added "4.1"}
(fact "create a postgres template"
  ^:hidden
  
  (builtin/pg-tmpl 'hello)
  => '(def$.pg hello hello))
