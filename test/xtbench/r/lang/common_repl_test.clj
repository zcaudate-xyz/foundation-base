(ns
 xtbench.r.lang.common-repl-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script- :r {:runtime :basic, :require [[xt.lang.common-repl :as k]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-repl/return-encode, :added "4.0"}
(fact
 "returns the encoded "
 ^{:hidden true}
 (json/read (!.R (k/return-encode {:data [1 2 3]} "<id>" "<key>")))
 =>
 {"key" "<key>", "id" "<id>", "value" {"data" [1 2 3]}, "type" "data"})

^{:refer xt.lang.common-repl/return-wrap, :added "4.0"}
(fact
 "returns a wrapped call"
 ^{:hidden true}
 (json/read (!.R (k/return-wrap (fn:> 1))))
 =>
 {"key" nil, "id" nil, "value" 1, "type" "data"})

^{:refer xt.lang.common-repl/return-eval, :added "4.0"}
(fact
 "evaluates a returns a string"
 ^{:hidden true}
 (json/read (!.R (k/return-eval "1")))
 =>
 {"key" nil, "id" nil, "value" 1, "type" "data"})
