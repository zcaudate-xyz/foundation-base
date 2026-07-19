(ns kmi.lang.parser-core-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.parser-core :as core]
             [kmi.lang.reader :as rdr]
             [kmi.lang.type-list :as list]
             [kmi.lang.protocol-base :as proto]
             [kmi.lang.type-hashmap :as hm]
             [kmi.lang.type-hashset :as hs]
             [kmi.lang.type-keyword :as kw]
             [kmi.lang.type-syntax :as syn]
             [kmi.lang.common-util :as ic]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.parser-core/read-delimited :added "4.1" :id kmi-seed-1}
(fact ""

  (!.js (var reader (rdr/create "1 2]"))
        (core/read-delimited reader "]"))
  => [1 2])

^{:refer kmi.lang.parser-core/read :added "4.1" :id kmi-seed-2}
(fact ""

  (!.js (var out (core/read (rdr/create "(1 2 3)")))
        (list/list-to-array out))
  => [1 2 3])

^{:refer kmi.lang.parser-core/read-string :added "4.1" :id kmi-seed-3}
(fact ""

  (!.js [(proto/to-array (core/read-string "[1 2]"))
         (== nil (core/read-string "  ; comment\n"))])
  => [[1 2] true])

^{:refer kmi.lang.parser-core/read :added "4.1" :id kmi-seed-4}
(fact "reads vectors, maps, sets, strings, atoms, and reader syntax"

  (!.js
   (var vec-out (core/read (rdr/create "[1 2 3]")))
   (proto/to-array vec-out))
  => [1 2 3]

  (!.js
   (var map-out (core/read (rdr/create "{:a 1 :b 2}")))
   [(xt/x:get-key map-out "_size")
    (hm/hashmap-lookup-key map-out (kw/keyword nil "a") "missing")
    (hm/hashmap-lookup-key map-out (kw/keyword nil "b") "missing")])
  => [2 1 2]

  (!.js
   (var set-out (core/read (rdr/create "#{:a :b :a}")))
   [(xt/x:get-key set-out "_size")
    (hs/hashset-has? set-out (kw/keyword nil "a"))
    (hs/hashset-has? set-out (kw/keyword nil "b"))])
  => [2 true true]

  (!.js
   (core/read (rdr/create "\"hello\"")))
  => "hello"

  (!.js
   [(== nil (core/read (rdr/create "nil")))
    (core/read (rdr/create "true"))
    (core/read (rdr/create "-2.5"))
    [(xt/x:get-key (core/read (rdr/create "hello/world")) "::")
     (xt/x:get-key (core/read (rdr/create "hello/world")) "_ns")
     (xt/x:get-key (core/read (rdr/create "hello/world")) "_name")]
    [(xt/x:get-key (core/read (rdr/create ":user/id")) "::")
     (xt/x:get-key (core/read (rdr/create ":user/id")) "_ns")
     (xt/x:get-key (core/read (rdr/create ":user/id")) "_name")]])
  => [true true -2.5 ["symbol" "hello" "world"] ["keyword" "user" "id"]]

  (!.js
   (var quote-out (list/list-to-array (core/read (rdr/create "'hello"))))
   [[(xt/x:get-key (xt/x:first quote-out) "_name")
     (xt/x:get-key (xt/x:second quote-out) "_name")]
    (== "hello" (xt/x:get-key (xt/x:second quote-out) "_name"))])
  => [["quote" "hello"] true]

  (!.js
   (var meta-out (core/read (rdr/create "^{:tag true} hello")))
   (var inner (syn/syntax meta-out nil))
   [(ic/is-syntax? meta-out)
    (hm/hashmap-lookup-key (syn/get-metadata meta-out) (kw/keyword nil "tag") false)
    (xt/x:get-key inner "_name")])
  => [true true "hello"]

  (!.js
   (core/read (rdr/create ")")))
  => (throws))

^{:refer kmi.lang.parser-core/read-delimited :added "4.1" :id kmi-seed-5}
(fact "reads delimited forms for different delimiters and edge cases"

  (!.js
   (var reader (rdr/create "1 2)"))
   (core/read-delimited reader ")"))
  => [1 2]

  (!.js
   (var reader (rdr/create "1 2}"))
   (core/read-delimited reader "}"))
  => [1 2]

  (!.js
   (var reader (rdr/create ":a :b]"))
   (var out (core/read-delimited reader "]"))
   [[(xt/x:get-key (xt/x:first out) "::")
     (xt/x:get-key (xt/x:first out) "_name")]
    [(xt/x:get-key (xt/x:second out) "::")
     (xt/x:get-key (xt/x:second out) "_name")]])
  => [["keyword" "a"] ["keyword" "b"]]

  (!.js
   (var reader (rdr/create ")"))
   (core/read-delimited reader ")"))
  => []

  (!.js
   (core/read-delimited (rdr/create "1 2") "]"))
  => (throws))

^{:refer kmi.lang.parser-core/read-string :added "4.1" :id kmi-seed-6}
(fact "reads forms directly from strings, skipping whitespace and comments"

  (!.js
   (var out (core/read-string "  ; ignore\n^{:tag true} [1 2]"))
   (var meta (syn/get-metadata out))
   [(proto/to-array (syn/syntax out nil))
    (xt/x:get-key meta "_size")])
  => [[1 2] 1]

  (!.js
   (var map-out (core/read-string "{:a 1 :b 2}"))
   [(xt/x:get-key map-out "_size")
    (hm/hashmap-lookup-key map-out (kw/keyword nil "a") "missing")])
  => [2 1]

  (!.js
   (var arr (list/list-to-array (core/read-string "(+ 1 2)")))
   [(xt/x:get-key (xt/x:first arr) "_name")
    (xt/x:get-idx arr (xt/x:offset 1))
    (xt/x:get-idx arr (xt/x:offset 2))])
  => ["+" 1 2]

  (!.js
   (== nil (core/read-string "   ")))
  => true

  (!.js
   (core/read-string "\"a\""))
  => "a"

  (!.js
   (core/read-string "#_ignored 42"))
  => 42)
