(ns postgres.core.variant-test
  (:require [postgres.core :as pg]
            [postgres.core.variant :as variant])
  (:use code.test))

^{:refer postgres.core.variant/defvariant.pg :added "4.1"}
(fact "defvariant.pg is installed through postgres.core and emits no SQL form"
  [(macroexpand
    '(variant/defvariant.pg tua/AccessRole
       [:class-table "Org"]
       [:scope {:type :array :items {:type :text}}]))
   (macroexpand
    '(pg/defvariant.pg tua/AccessRole
       [:class-table "Org"]
       [:scope {:type :array :items {:type :text}}]))]
  => [nil nil])

(fact "defvariant.pg validates its declaration boundary"
  (macroexpand
   '(pg/defvariant.pg AccessRole
      [:class-table "Org"]
      [:scope {:type :array}]))
  => (throws))

(fact "defvariant.pg requires a literal class-table selector"
  (macroexpand
   '(pg/defvariant.pg tua/AccessRole
      [:class-table :Org]
      [:scope {:type :array}]))
  => (throws))
