(ns std.lib.schema.find-test
  (:require [std.lib.schema.find :refer :all])
  (:use code.test))

^{:refer std.lib.schema.find/all-attrs :added "3.0"}
(fact "finds all attributes satisfying `f` in a schema"

  (all-attrs
   {:account/number [{:ident   :account/number
                      :type    :long}]
    :account/date   [{:ident   :account/date
                      :type    :instant}]}
   (fn [attr] (= (:type attr) :long)))
  => {:account/number [{:type :long, :ident :account/number}]})

^{:refer std.lib.schema.find/all-idents :added "3.0"}
(fact "finds all idents satisfying `f` in a schema"

  (all-idents
   {:account/number [{:ident   :account/number
                      :type    :long}]
    :account/date   [{:ident   :account/date
                      :type    :instant}]}
   (fn [attr] (= (:type attr) :long)))
  => [:account/number])

^{:refer std.lib.schema.find/is-reverse-ref? :added "3.0"}
(fact "predicate for reverse ref"

  (is-reverse-ref? {:ident  :email/accounts
                    :type   :ref
                    :ref    {:type :reverse}})
  => true)
