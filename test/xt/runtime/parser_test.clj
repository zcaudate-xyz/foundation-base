(ns xt.runtime.parser-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.runtime.interface-common :as ic]
             [xt.runtime.parser-common :as pc]
             [xt.runtime.parser :as p]
             [xt.runtime.reader :as rdr]
             [xt.runtime.type-hashmap :as hm]
             [xt.runtime.type-hashset :as hs]
             [xt.runtime.type-keyword :as kw]
             [xt.runtime.type-list :as list]
             [xt.runtime.type-symbol :as sym]
             [xt.runtime.type-syntax :as syn]
             [xt.runtime.type-vector :as vec]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.runtime.interface-common :as ic]
             [xt.runtime.parser-common :as pc]
             [xt.runtime.parser :as p]
             [xt.runtime.reader :as rdr]
             [xt.runtime.type-hashmap :as hm]
             [xt.runtime.type-hashset :as hs]
             [xt.runtime.type-keyword :as kw]
             [xt.runtime.type-list :as list]
             [xt.runtime.type-symbol :as sym]
             [xt.runtime.type-syntax :as syn]
             [xt.runtime.type-vector :as vec]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.parser-common/whitespace? :added "4.1"}
(fact "detects reader whitespace"

  (!.js
   [(pc/whitespace? " ")
    (pc/whitespace? ",")
    (pc/whitespace? "a")])
  => [true true false]

  (!.lua
   [(pc/whitespace? " ")
    (pc/whitespace? ",")
    (pc/whitespace? "a")])
  => [true true false])

^{:refer xt.runtime.parser-common/token-boundary? :added "4.1"}
(fact "detects token boundaries"

  (!.js
   [(pc/token-boundary? " ")
    (pc/token-boundary? "(")
    (pc/token-boundary? "a")])
  => [true true false]

  (!.lua
   [(pc/token-boundary? " ")
    (pc/token-boundary? "(")
    (pc/token-boundary? "a")])
  => [true true false])

^{:refer xt.runtime.parser-common/read-comment :added "4.1"}
(fact "consumes line comments"

  (!.js
   (var reader (rdr/create "note\nx"))
   (pc/read-comment reader)
   (rdr/read-char reader))
  => "x"

  (!.lua
   (var reader (rdr/create "note\nx"))
   (pc/read-comment reader)
   (rdr/read-char reader))
  => "x")

^{:refer xt.runtime.parser-common/skip-whitespace :added "4.1"}
(fact "skips whitespace and comments"

  (!.js
   (var reader (rdr/create " \n; note\nabc"))
   [(pc/skip-whitespace reader)
    (rdr/read-char reader)])
  => ["a" "a"]

  (!.lua
   (var reader (rdr/create " \n; note\nabc"))
   [(pc/skip-whitespace reader)
    (rdr/read-char reader)])
  => ["a" "a"])

^{:refer xt.runtime.parser-common/digit? :added "4.1"}
(fact "detects digit chars"

  (!.js
   [(pc/digit? "1")
    (pc/digit? "a")])
  => [true false]

  (!.lua
   [(pc/digit? "1")
    (pc/digit? "a")])
  => [true false])

^{:refer xt.runtime.parser-common/numeric-leading? :added "4.1"}
(fact "detects numeric-looking tokens"

  (!.js
   [(pc/numeric-leading? "12")
    (pc/numeric-leading? "-3")
    (pc/numeric-leading? "abc")])
  => [true true false]

  (!.lua
   [(pc/numeric-leading? "12")
    (pc/numeric-leading? "-3")
    (pc/numeric-leading? "abc")])
  => [true true false])

^{:refer xt.runtime.parser-common/match-number :added "4.1"}
(fact "matches integer and decimal tokens"

  (!.js
   [(pc/match-number "12")
    (pc/match-number "-3.5")
    (== nil (pc/match-number "abc"))])
  => [12 -3.5 true]

  (!.lua
   [(pc/match-number "12")
    (pc/match-number "-3.5")
    (== nil (pc/match-number "abc"))])
  => [12 -3.5 true])

^{:refer xt.runtime.parser-common/read-token :added "4.1"}
(fact "reads tokens until the next boundary"

  (!.js
   (var reader (rdr/create "bc]"))
   (pc/read-token reader "a"))
  => "abc"

  (!.lua
   (var reader (rdr/create "bc]"))
   (pc/read-token reader "a"))
  => "abc")

^{:refer xt.runtime.parser-common/interpret-token :added "4.1"}
(fact "interprets atom tokens into runtime values"

  (!.js
   (var reader (rdr/create ""))
   (var s (pc/interpret-token reader "hello/world"))
   (var k (pc/interpret-token reader ":user/id"))
   [(== nil (pc/interpret-token reader "nil"))
    (pc/interpret-token reader "true")
    (pc/interpret-token reader "-2")
    [(. s ["::"]) (. s _ns) (. s _name)]
    [(. k ["::"]) (. k _ns) (. k _name)]])
  => [true true -2 ["symbol" "hello" "world"] ["keyword" "user" "id"]]

  (!.lua
   (var reader (rdr/create ""))
   (var s (pc/interpret-token reader "hello/world"))
   (var k (pc/interpret-token reader ":user/id"))
   [(== nil (pc/interpret-token reader "nil"))
    (pc/interpret-token reader "true")
    (pc/interpret-token reader "-2")
    [(. s ["::"]) (. s _ns) (. s _name)]
    [(. k ["::"]) (. k _ns) (. k _name)]])
  => [true true -2 ["symbol" "hello" "world"] ["keyword" "user" "id"]])

(fact "rejects invalid numeric tokens"

  (!.js
   (pc/interpret-token (rdr/create "") "12a"))
  => (throws)

  (!.lua
   (pc/interpret-token (rdr/create "") "12a"))
  => (throws))

^{:refer xt.runtime.parser-common/normalise-meta :added "4.1"}
(fact "normalises metadata shorthand"

  (!.js
   (var from-string (pc/normalise-meta "tagged"))
   (var from-keyword (pc/normalise-meta (kw/keyword nil "dynamic")))
   [(hm/hashmap-lookup-key from-string (kw/keyword nil "tag") nil)
    (hm/hashmap-lookup-key from-keyword (kw/keyword nil "dynamic") false)])
  => ["tagged" true]

  (!.lua
   (var from-string (pc/normalise-meta "tagged"))
   (var from-keyword (pc/normalise-meta (kw/keyword nil "dynamic")))
   [(hm/hashmap-lookup-key from-string (kw/keyword nil "tag") nil)
    (hm/hashmap-lookup-key from-keyword (kw/keyword nil "dynamic") false)])
  => ["tagged" true])

^{:refer xt.runtime.parser/read-delimited :added "4.1"}
(fact "reads delimited forms into arrays"

  (!.js
   (var reader (rdr/create "1 2]"))
   (p/read-delimited reader "]"))
  => [1 2])

^{:refer xt.runtime.parser-common/read-string-body :added "4.1"}
(fact "reads string bodies with simple escapes"

  (!.js
   (var reader (rdr/create "a\\n\\\"b\""))
   (pc/read-string-body reader))
  => "a\n\"b"

  (!.lua
   (var reader (rdr/create "a\\n\\\"b\""))
   (pc/read-string-body reader))
  => "a\n\"b")

^{:refer xt.runtime.parser/read-list :added "4.1"}
(fact "reads list forms"

  (!.js
   (list/list-to-array (p/read-list (rdr/create "1 2 3)"))))
  => [1 2 3])

^{:refer xt.runtime.parser/read-vector :added "4.1"}
(fact "reads vector forms"

  (!.js
   (. (p/read-vector (rdr/create "1 2 3]")) (to-array)))
  => [1 2 3])

^{:refer xt.runtime.parser/read-map :added "4.1"}
(fact "reads map forms"

  (!.js
   (var out (p/read-map (rdr/create ":a 1 :b 2}")))
   [(. out _size)
    (hm/hashmap-lookup-key out (kw/keyword nil "a") "missing")
    (hm/hashmap-lookup-key out (kw/keyword nil "b") "missing")])
  => [2 1 2])

(fact "rejects odd map forms"

  (!.js
   (p/read-map (rdr/create ":a 1 :b}")))
  => (throws)

  (!.lua
   (p/read-map (rdr/create ":a 1 :b}")))
  => (throws))

^{:refer xt.runtime.parser/read-set :added "4.1"}
(fact "reads set forms"

  (!.js
   (var out (p/read-set (rdr/create ":a :b :a}")))
   [(. out _size)
    (hs/hashset-has? out (kw/keyword nil "a"))
    (hs/hashset-has? out (kw/keyword nil "b"))])
  => [2 true true])

^{:refer xt.runtime.parser/read-quote :added "4.1"}
(fact "reads quoted forms into runtime lists"

  (!.js
   (var out (list/list-to-array (p/read-quote (rdr/create "hello"))))
    [(. (xt/x:first out) _name)
     (. (xt/x:second out) _name)])
  => ["quote" "hello"])

^{:refer xt.runtime.parser/read-syntax-quote :added "4.1"}
(fact "reads syntax-quote forms into runtime lists"

  (!.js
   (var out (list/list-to-array (p/read-syntax-quote (rdr/create "hello"))))
   [(. (xt/x:first out) _name)
    (. (xt/x:second out) _name)])
  => ["syntax-quote" "hello"])

^{:refer xt.runtime.parser/read-deref :added "4.1"}
(fact "reads deref forms into runtime lists"

  (!.js
   (var out (list/list-to-array (p/read-deref (rdr/create "hello"))))
   [(. (xt/x:first out) _name)
    (. (xt/x:second out) _name)])
  => ["deref" "hello"])

^{:refer xt.runtime.parser/read-unquote :added "4.1"}
(fact "reads unquote forms into runtime lists"

  (!.js
   (var out (list/list-to-array (p/read-unquote (rdr/create "hello"))))
    [(. (xt/x:first out) _name)
     (. (xt/x:second out) _name)])
  => ["unquote" "hello"])

^{:refer xt.runtime.parser/read-unquote-splicing :added "4.1"}
(fact "reads unquote-splicing forms into runtime lists"

  (!.js
   (var out (list/list-to-array (p/read-unquote-splicing (rdr/create "hello"))))
   [(. (xt/x:first out) _name)
    (. (xt/x:second out) _name)])
  => ["unquote-splicing" "hello"])

^{:refer xt.runtime.parser/read-meta :added "4.1"}
(fact "reads metadata into syntax wrappers"

  (!.js
   (var out (p/read-meta (rdr/create ":dynamic hello")))
   (var meta (syn/get-metadata out))
   (var inner (syn/syntax out nil))
   [(ic/is-syntax? out)
    (hm/hashmap-lookup-key meta (kw/keyword nil "dynamic") false)
    (. inner _name)])
  => [true true "hello"])

^{:refer xt.runtime.parser/read-var-quote :added "4.1"}
(fact "reads var-quote forms into runtime lists"

  (!.js
   (var out (list/list-to-array (p/read-var-quote (rdr/create "hello"))))
    [(. (xt/x:first out) _name)
     (. (xt/x:second out) _name)])
  => ["var" "hello"])

^{:refer xt.runtime.parser/read-discard :added "4.1"}
(fact "reads discard forms by skipping one form"

  (!.js
   (var out (p/read-discard (rdr/create "hello world")))
   (. out _name))
  => "world")

^{:refer xt.runtime.parser/read-dispatch :added "4.1"}
(fact "reads dispatch set forms"

  (!.js
   (var out (p/read-dispatch (rdr/create "{:a :b}")))
   [(. out _size)
    (hs/hashset-has? out (kw/keyword nil "a"))
    (hs/hashset-has? out (kw/keyword nil "b"))])
  => [2 true true])

(fact "reads var-quote and discard dispatch forms"

  (!.js
   (var quoted (list/list-to-array (p/read-dispatch (rdr/create "'hello"))))
   (var out (p/read-dispatch (rdr/create "_hello world")))
   [[(. (xt/x:first quoted) _name)
     (. (xt/x:second quoted) _name)]
     (. out _name)])
  => [["var" "hello"] "world"])

(fact "rejects unsupported dispatch macros"

  (!.js
   (p/read-dispatch (rdr/create "?x")))
  => (throws)

  (!.lua
   (p/read-dispatch (rdr/create "?x")))
  => (throws))

(fact "rejects EOF for prefix reader syntax"

  (!.js
   (p/read (rdr/create "'")))
  => (throws)

  (!.js
   (p/read (rdr/create "`")))
  => (throws)

  (!.js
   (p/read (rdr/create "@")))
  => (throws)

  (!.js
   (p/read (rdr/create "~")))
  => (throws)

  (!.js
   (p/read (rdr/create "^:tag")))
  => (throws)

  (!.js
   (p/read (rdr/create "#'")))
  => (throws)

  (!.js
   (p/read (rdr/create "#_skip")))
  => (throws))

^{:refer xt.runtime.parser/read :added "4.1"}
(fact "reads atom and collection forms from readers"

  (!.js
   (var list-out (p/read (rdr/create "(1 2 3)")))
   (var vec-out (p/read (rdr/create "[1 2 3]")))
   [(list/list-to-array list-out)
     (. vec-out (to-array))
     (p/read (rdr/create "true"))])
  => [[1 2 3] [1 2 3] true])

(fact "reads deref, unquote, syntax-quote, and dispatch reader syntax"

  (!.js
   (var syntax-out (list/list-to-array (p/read (rdr/create "`hello"))))
   (var deref-out (list/list-to-array (p/read (rdr/create "@hello"))))
   (var unquote-out (list/list-to-array (p/read (rdr/create "~hello"))))
   (var splice-out (list/list-to-array (p/read (rdr/create "~@hello"))))
   (var var-out (list/list-to-array (p/read (rdr/create "#'hello"))))
   (var discard-out (p/read (rdr/create "#_hello world")))
   [[(. (xt/x:first syntax-out) _name)
      (. (xt/x:second syntax-out) _name)]
     [(. (xt/x:first deref-out) _name)
       (. (xt/x:second deref-out) _name)]
    [(. (xt/x:first unquote-out) _name)
     (. (xt/x:second unquote-out) _name)]
     [(. (xt/x:first splice-out) _name)
      (. (xt/x:second splice-out) _name)]
     [(. (xt/x:first var-out) _name)
      (. (xt/x:second var-out) _name)]
     (. discard-out _name)])
  => [["syntax-quote" "hello"]
      ["deref" "hello"]
      ["unquote" "hello"]
      ["unquote-splicing" "hello"]
      ["var" "hello"]
      "world"])

(fact "rejects unmatched delimiters"

  (!.js
   (p/read (rdr/create ")")))
  => (throws)

  (!.lua
   (p/read (rdr/create ")")))
  => (throws))

^{:refer xt.runtime.parser/read-string :added "4.1"}
(fact "reads forms directly from strings"

  (!.js
   (var out (p/read-string "  ; ignore\n^{:tag true} [1 2]"))
   (var meta (syn/get-metadata out))
   [(. (syn/syntax out nil) (to-array))
    (. meta _size)])
  => [[1 2] 1])

(fact "returns nil for empty or comment-only strings"

  (!.js
   (== nil (p/read-string "  ; comment\n")))
  => true)
