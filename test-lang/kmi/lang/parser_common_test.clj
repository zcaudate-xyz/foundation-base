(ns kmi.lang.parser-common-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.parser-common :as pc]
             [kmi.lang.reader :as rdr]
             [kmi.lang.type-hashmap :as hm]
             [kmi.lang.type-keyword :as kw]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer kmi.lang.parser-common/whitespace? :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.parser-common/token-boundary? :added "4.1"}
(fact
  [(pc/token-boundary? "(")
   (pc/token-boundary? "`")]
  => [true true])

^{:refer kmi.lang.parser-common/read-comment :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.parser-common/skip-whitespace :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.parser-common/digit? :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.parser-common/numeric-leading? :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.parser-common/match-number :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.parser-common/read-token :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.parser-common/interpret-token :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.parser-common/normalise-meta :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.parser-common/read-string-body :added "4.1"}
(fact "TODO")