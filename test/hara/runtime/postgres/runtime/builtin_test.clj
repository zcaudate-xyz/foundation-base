(ns hara.runtime.postgres.runtime.builtin-test
  (:use code.test)
  (:require [hara.runtime.postgres.runtime.builtin :as builtin]))

^{:refer hara.runtime.postgres.runtime.builtin/pg-tmpl :added "4.1"}
(fact "create a postgres template"

  (builtin/pg-tmpl 'hello)
  => '(def$.pg hello hello))
