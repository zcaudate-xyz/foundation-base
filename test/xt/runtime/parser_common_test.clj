(ns xt.runtime.parser-common-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.parser-common :as pc]
             [xt.runtime.reader :as rdr]
             [xt.runtime.type-hashmap :as hm]
             [xt.runtime.type-keyword :as kw]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.parser-common/whitespace? :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.parser-common/token-boundary? :added "4.1"}
(fact
  [(pc/token-boundary? "(")
   (pc/token-boundary? "`")]
  => [true true])

^{:refer xt.runtime.parser-common/read-comment :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.parser-common/skip-whitespace :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.parser-common/digit? :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.parser-common/numeric-leading? :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.parser-common/match-number :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.parser-common/read-token :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.parser-common/interpret-token :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.parser-common/normalise-meta :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.parser-common/read-string-body :added "4.1"}
(fact "TODO")