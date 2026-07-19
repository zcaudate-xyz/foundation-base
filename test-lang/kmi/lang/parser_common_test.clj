(ns kmi.lang.parser-common-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.parser-common :as pc]
             [xt.lang.spec-base :as xt]
             [kmi.lang.reader :as rdr]
             [kmi.lang.type-hashmap :as hm]
             [kmi.lang.type-keyword :as kw]
             [kmi.lang.type-symbol :as sym]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.parser-common/whitespace? :added "4.1"}
(fact "checks if a char is reader whitespace"

  (!.js [(pc/whitespace? " ")
         (pc/whitespace? "\n")
         (pc/whitespace? "\r")
         (pc/whitespace? "\t")
         (pc/whitespace? ",")
         (pc/whitespace? "a")
         (pc/whitespace? "(")
         (== false (pc/whitespace? nil))])
  => [true true true true true false false true])

^{:refer kmi.lang.parser-common/token-boundary? :added "4.1"}
(fact "checks if a char terminates a token"

  (!.js [(pc/token-boundary? "(")
         (pc/token-boundary? "`")])
  => [true true])

^{:refer kmi.lang.parser-common/read-comment :added "4.1"}
(fact "consumes characters until end of line"

  (!.js (var reader (rdr/create "; comment text\nrest"))
        (rdr/read-char reader)
        [(== nil (pc/read-comment reader))
         (rdr/read-char reader)])
  => [true "r"]

  (!.js (var reader (rdr/create "; trailing"))
        (rdr/read-char reader)
        [(== nil (pc/read-comment reader))
         (== nil (rdr/read-char reader))])
  => [true true])

^{:refer kmi.lang.parser-common/skip-whitespace :added "4.1"}
(fact "advances the reader past whitespace and comments"

  (!.js (var reader (rdr/create "   ; a comment\n  hello"))
        [(pc/skip-whitespace reader)
         (rdr/read-char reader)
         (rdr/read-char reader)])
  => ["h" "h" "e"]

  (!.js (var reader (rdr/create "; comment\nx"))
        (pc/skip-whitespace reader))
  => "x"

  (!.js (== nil (pc/skip-whitespace (rdr/create ",\t\n"))))
  => true)

^{:refer kmi.lang.parser-common/digit? :added "4.1"}
(fact "checks if a char is a digit"

  (!.js [(pc/digit? "0")
         (pc/digit? "9")
         (pc/digit? "5")
         (pc/digit? "a")
         (pc/digit? " ")
         (== false (pc/digit? nil))])
  => [true true true false false true])

^{:refer kmi.lang.parser-common/numeric-leading? :added "4.1"}
(fact "checks if a token looks numeric from the first char"

  (!.js [(pc/numeric-leading? "123")
         (pc/numeric-leading? "+123")
         (pc/numeric-leading? "-123")
         (pc/numeric-leading? "+.")
         (pc/numeric-leading? ".5")
         (pc/numeric-leading? ".")
         (pc/numeric-leading? "+")
         (pc/numeric-leading? "-")
         (pc/numeric-leading? "abc")
         (pc/numeric-leading? "")])
  => [true true true true true true false false false false])

^{:refer kmi.lang.parser-common/match-number :added "4.1"}
(fact "parses integer and decimal tokens"

  (!.js [(pc/match-number "123")
         (pc/match-number "-45")
         (pc/match-number "+3.14")
         (pc/match-number ".5")
         (pc/match-number "1.")
         (pc/match-number "0")])
  => [123 -45 3.14 0.5 1 0]

  (!.js [(== nil (pc/match-number ""))
         (== nil (pc/match-number "abc"))
         (== nil (pc/match-number ".."))
         (== nil (pc/match-number "+."))
         (== nil (pc/match-number "1.2.3"))
         (== nil (pc/match-number "+"))])
  => [true true true true true true])

^{:refer kmi.lang.parser-common/read-token :added "4.1"}
(fact "reads a token until a boundary character"

  (!.js (var reader (rdr/create "ello world"))
        (pc/read-token reader "h"))
  => "hello"

  (!.js (var reader (rdr/create "123]"))
        (pc/read-token reader (rdr/read-char reader)))
  => "123"

  (!.js (var reader (rdr/create ""))
        (pc/read-token reader "x"))
  => "x")

^{:refer kmi.lang.parser-common/interpret-token :added "4.1"}
(fact "interprets tokens into runtime values"

  (!.js [(== nil (pc/interpret-token (rdr/create "") "nil"))
         (pc/interpret-token (rdr/create "") "true")
         (pc/interpret-token (rdr/create "") "false")
         (pc/interpret-token (rdr/create "") "123")
         (== (pc/interpret-token (rdr/create "") "/")
             (sym/symbol nil "/"))
         (== (pc/interpret-token (rdr/create "") ":hello")
             (kw/keyword nil "hello"))
         (== (pc/interpret-token (rdr/create "") "my-ns/my-name")
             (sym/symbol "my-ns" "my-name"))])
  => [true true false 123 true true true]

  (!.js (pc/interpret-token (rdr/create "") "1.2.3"))
  => (throws)

  (!.js (pc/interpret-token (rdr/create "") ":"))
  => (throws)

  (!.js (pc/interpret-token (rdr/create "") ":/"))
  => (throws))

^{:refer kmi.lang.parser-common/normalise-meta :added "4.1"}
(fact "normalises metadata shorthand into a map"

  (!.js (var out (pc/normalise-meta "tag"))
        [(hm/hashmap-lookup-key out (kw/keyword nil "tag") "missing")
         (xt/x:get-key out "_size")])
  => ["tag" 1]

  (!.js (var s (sym/symbol nil "tag"))
        (var out (pc/normalise-meta s))
        [(== (hm/hashmap-lookup-key out (kw/keyword nil "tag") "missing") s)
         (xt/x:get-key out "_size")])
  => [true 1]

  (!.js (var k (kw/keyword nil "tag"))
        (var out (pc/normalise-meta k))
        [(hm/hashmap-lookup-key out k false)
         (xt/x:get-key out "_size")])
  => [true 1]

  (!.js (pc/normalise-meta 42))
  => 42)

^{:refer kmi.lang.parser-common/read-string-body :added "4.1"}
(fact "reads the body of a double-quoted string"

  (!.js (var reader (rdr/create "hello\""))
        (pc/read-string-body reader))
  => "hello"

  (!.js (var reader (rdr/create "\""))
        (pc/read-string-body reader))
  => ""

  (!.js (var reader (rdr/create "hello\\\"rest\""))
        (pc/read-string-body reader))
  => "hello\"rest"

  (!.js (var reader (rdr/create "line1\\nline2\""))
        (pc/read-string-body reader))
  => "line1\nline2"

  (!.js (var reader (rdr/create "no end"))
        (pc/read-string-body reader))
  => (throws))
