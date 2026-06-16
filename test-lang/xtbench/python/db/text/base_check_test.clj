(ns xtbench.python.db.text.base-check-test
  (:require [postgres.core :as pg]
            [postgres.sample.scratch-v1 :as scratch]
            [hara.lang :as l])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[xt.db.text.base-check :as chk]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.text.base-check/is-uuid? :added "4.0"}
(fact "checks that a string input is a uuid"

  (!.py
    [(chk/is-uuid? "527a67de-a499-4c51-a435-953e3272b00d")
     (chk/is-uuid? "527a67de-a499-4c51-a435-953e2b00d")])
  => [true false])

^{:refer xt.db.text.base-check/check-arg-type :added "4.0"}
(fact "checks the arg type of an input"

  (!.py
    [(chk/check-arg-type "numeric" 1.0)
     (chk/check-arg-type "integer" 1)
     (chk/check-arg-type "jsonb" {:a 1 :b 2})
     (chk/check-arg-type "citext" "hello")
     (chk/check-arg-type "text" "hello")])
  => [true true true true true])

^{:refer xt.db.text.base-check/check-args-type :added "4.0"}
(fact "checks the arg type of inputs"

  (!.py
    (chk/check-args-type [1 2]
                         [{:symbol "x", :type "numeric"}
                          {:symbol "y", :type "numeric"}]))
  => [true nil])

^{:refer xt.db.text.base-check/check-args-length :added "4.0"}
(fact "checks that input and spec are of the same length"

  (!.py
    (chk/check-args-length [1 2]
                           [{:symbol "x", :type "numeric"}
                            {:symbol "y", :type "numeric"}]))
  => [true nil])

(comment
  (s/run ['xt.db.text.base-check])
  (s/seedgen-benchadd '[xt.db.text.base-check] {:lang [:julia :dart] :write true})
  
  (s/seedgen-langadd 'xt.db.text.base-check {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.db.text.base-check {:lang [:lua :python] :write true}))
