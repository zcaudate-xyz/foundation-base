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
(fact (pc/whitespace? " ") => true)

^{:refer xt.runtime.parser-common/token-boundary? :added "4.1"}
(fact
  [(pc/token-boundary? "(")
   (pc/token-boundary? "`")]
  => [true true])

^{:refer xt.runtime.parser-common/read-comment :added "4.1"}
(fact
  (!.js (var reader (rdr/create "note\nx"))
        (pc/read-comment reader)
        (rdr/read-char reader))
  => "x")

^{:refer xt.runtime.parser-common/skip-whitespace :added "4.1"}
(fact
  (!.js (var reader (rdr/create " \n; note\nabc"))
        (pc/skip-whitespace reader)
        (rdr/read-char reader))
  => "a")

^{:refer xt.runtime.parser-common/digit? :added "4.1"}
(fact (pc/digit? "1") => true)

^{:refer xt.runtime.parser-common/numeric-leading? :added "4.1"}
(fact (pc/numeric-leading? "-3") => true)

^{:refer xt.runtime.parser-common/match-number :added "4.1"}
(fact (pc/match-number "-3.5") => -3.5)

^{:refer xt.runtime.parser-common/read-token :added "4.1"}
(fact
  (!.js (var reader (rdr/create "bc]"))
        (pc/read-token reader "a"))
  => "abc")

^{:refer xt.runtime.parser-common/interpret-token :added "4.1"}
(fact
  (!.js (var reader (rdr/create ""))
        (var out (pc/interpret-token reader ":user/id"))
        [(. out _ns) (. out _name)])
  => ["user" "id"])

^{:refer xt.runtime.parser-common/normalise-meta :added "4.1"}
(fact
  (!.js (var out (pc/normalise-meta (kw/keyword nil "dynamic")))
        (hm/hashmap-lookup-key out (kw/keyword nil "dynamic") false))
  => true)

^{:refer xt.runtime.parser-common/read-string-body :added "4.1"}
(fact
  (!.js (var reader (rdr/create "a\\n\\\"b\""))
        (pc/read-string-body reader))
  => "a\n\"b")
