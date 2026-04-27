(ns xt.db.schema.base-check-test
  (:require [rt.postgres :as pg]
            [rt.postgres.test.scratch-v1 :as scratch]
            [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :oneshot
   :require [[xt.db.schema.base-check :as chk]
             [xt.lang.common-lib :as k]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.schema.base-check :as chk]
             [xt.lang.common-lib :as k]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.schema.base-check :as chk]
             [xt.lang.common-lib :as k]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.schema.base-check/is-uuid? :added "4.0"}
(fact "checks that a string input is a uuid"

  (!.js
    [(chk/is-uuid? "527a67de-a499-4c51-a435-953e3272b00d")
     (chk/is-uuid? "527a67de-a499-4c51-a435-953e2b00d")])
  => [true false]

  (!.lua
    [(chk/is-uuid? "527a67de-a499-4c51-a435-953e3272b00d")
     (chk/is-uuid? "527a67de-a499-4c51-a435-953e2b00d")])
  => [true false]

  (!.py
    [(chk/is-uuid? "527a67de-a499-4c51-a435-953e3272b00d")
     (chk/is-uuid? "527a67de-a499-4c51-a435-953e2b00d")])
  => [true false])

^{:refer xt.db.schema.base-check/check-arg-type :added "4.0"}
(fact "checks the arg type of an input"

  (!.js
    [(chk/check-arg-type "numeric" 1.0)
     (chk/check-arg-type "integer" 1)
     (chk/check-arg-type "jsonb" {:a 1 :b 2})
     (chk/check-arg-type "citext" "hello")
     (chk/check-arg-type "text" "hello")])
  => [true true true true true]

  (!.lua
    [(chk/check-arg-type "numeric" 1.0)
     (chk/check-arg-type "integer" 1)
     (chk/check-arg-type "jsonb" {:a 1 :b 2})
     (chk/check-arg-type "citext" "hello")
     (chk/check-arg-type "text" "hello")])
  => [true true true true true]

  (!.py
    [(chk/check-arg-type "numeric" 1.0)
     (chk/check-arg-type "integer" 1)
     (chk/check-arg-type "jsonb" {:a 1 :b 2})
     (chk/check-arg-type "citext" "hello")
     (chk/check-arg-type "text" "hello")])
  => [true true true true true])

^{:refer xt.db.schema.base-check/check-args-type :added "4.0"}
(fact "checks the arg type of inputs"

  (!.js
    (chk/check-args-type [1 2]
                         [{:symbol "x", :type "numeric"}
                          {:symbol "y", :type "numeric"}]))
  => [true]

  (!.lua
    (chk/check-args-type [1 2]
                         [{:symbol "x", :type "numeric"}
                          {:symbol "y", :type "numeric"}]))
  => [true]

  (!.py
    (chk/check-args-type [1 2]
                         [{:symbol "x", :type "numeric"}
                          {:symbol "y", :type "numeric"}]))
  => [true])

^{:refer xt.db.schema.base-check/check-args-length :added "4.0"}
(fact "checks that input and spec are of the same length"

  (!.js
    (chk/check-args-length [1 2]
                           [{:symbol "x", :type "numeric"}
                            {:symbol "y", :type "numeric"}]))
  => [true]

  (!.lua
    (chk/check-args-length [1 2]
                           [{:symbol "x", :type "numeric"}
                            {:symbol "y", :type "numeric"}]))
  => [true]

  (!.py
    (chk/check-args-length [1 2]
                           [{:symbol "x", :type "numeric"}
                            {:symbol "y", :type "numeric"}]))
  => [true])

(comment
  (s/run ['xt.db.schema.base-check])
  (s/seedgen-benchadd '[xt.db.schema.base-check] {:lang [:julia :dart] :write true})
  
  (s/seedgen-langadd 'xt.db.schema.base-check {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.db.schema.base-check {:lang [:lua :python] :write true}))
