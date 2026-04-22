(ns
 xtbench.js.db.base-check-test
 (:require
  [rt.postgres :as pg]
  [rt.postgres.test.scratch-v1 :as scratch]
  [std.lang :as l])
 (:use code.test))

(l/script-
 :js
 {:runtime :basic,
  :config {:program :nodejs},
  :require [[xt.db.base-check :as chk] [xt.lang.common-lib :as k]]})

^{:refer xt.db.base-check/is-uuid?, :added "4.0"}
(fact
 "checks that a string input is a uuid"
 ^{:hidden true}
 (!.js
  [(chk/is-uuid? "527a67de-a499-4c51-a435-953e3272b00d")
   (chk/is-uuid? "527a67de-a499-4c51-a435-953e2b00d")])
 =>
 [true false])

^{:refer xt.db.base-check/check-arg-type, :added "4.0"}
(fact
 "checks the arg type of an input"
 ^{:hidden true}
 (!.js
  [(chk/check-arg-type "numeric" 1.0)
   (chk/check-arg-type "integer" 1)
   (chk/check-arg-type "jsonb" {:a 1, :b 2})
   (chk/check-arg-type "citext" "hello")
   (chk/check-arg-type "text" "hello")])
 =>
 [true true true true true])

^{:refer xt.db.base-check/check-args-type, :added "4.0"}
(fact
 "checks the arg type of inputs"
 ^{:hidden true}
 (!.js
  (chk/check-args-type
   [1 2]
   [{:symbol "x", :type "numeric"} {:symbol "y", :type "numeric"}]))
 =>
 [true])

^{:refer xt.db.base-check/check-args-length, :added "4.0"}
(fact
 "checks that input and spec are of the same length"
 ^{:hidden true}
 (!.js
  (chk/check-args-length
   [1 2]
   [{:symbol "x", :type "numeric"} {:symbol "y", :type "numeric"}]))
 =>
 [true])

(comment
 (./import)
 (pg/bind-function scratch/addf)
 {:input
  [{:symbol "x", :type "numeric"} {:symbol "y", :type "numeric"}],
  :return "numeric",
  :schema "scratch",
  :id "addf"})
