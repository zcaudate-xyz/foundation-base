(ns kmi.lang.parser-forms-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [kmi.lang.common-util :as ic]
             [kmi.lang.parser-core :as core]
             [kmi.lang.parser-forms :as forms]
             [kmi.lang.reader :as rdr]
             [kmi.lang.type-hashmap :as hm]
             [kmi.lang.type-hashset :as hs]
             [kmi.lang.protocol-base :as p]
             [kmi.lang.type-keyword :as kw]
             [kmi.lang.type-list :as list]
             [kmi.lang.type-syntax :as syn]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.parser-forms/read-delimited :added "4.1" :id kmi-seed-1}
(fact ""

  (!.js (var reader (rdr/create "1 2]"))
        (forms/read-delimited reader "]" (fn [reader] (return (core/read reader)))))
  => [1 2])

^{:refer kmi.lang.parser-forms/read-list :added "4.1" :id kmi-seed-2}
(fact ""

  (!.js (list/list-to-array (forms/read-list (rdr/create "1 2 3)") (fn [reader] (return (core/read reader))))))
  => [1 2 3])

^{:refer kmi.lang.parser-forms/read-vector :added "4.1" :id kmi-seed-3}
(fact ""

  (!.js (p/to-array (forms/read-vector (rdr/create "1 2 3]") (fn [reader] (return (core/read reader))))))
  => [1 2 3])

^{:refer kmi.lang.parser-forms/read-map :added "4.1" :id kmi-seed-4}
(fact ""

  (!.js (var out (forms/read-map (rdr/create ":a 1 :b 2}") (fn [reader] (return (core/read reader)))))
        [(xt/x:get-key out "_size")
         (hm/hashmap-lookup-key out (kw/keyword nil "a") "missing")])
  => [2 1])

^{:refer kmi.lang.parser-forms/read-set :added "4.1" :id kmi-seed-5}
(fact ""

  (!.js (var out (forms/read-set (rdr/create ":a :b :a}") (fn [reader] (return (core/read reader)))))
        [(xt/x:get-key out "_size")
         (hs/hashset-has? out (kw/keyword nil "a"))])
  => [2 true])

^{:refer kmi.lang.parser-forms/read-required :added "4.1" :id kmi-seed-6}
(fact ""

  (!.js
   (xt/x:get-key (forms/read-required (rdr/create "hello") (fn [reader] (return (core/read reader)))) "_name"))
  => "hello"

  (!.js
   (forms/read-required (rdr/create "") (fn [reader] (return (core/read reader)))))
  => (throws))

^{:refer kmi.lang.parser-forms/read-quote :added "4.1" :id kmi-seed-7}
(fact ""

  (!.js (var out (list/list-to-array (forms/read-quote (rdr/create "hello") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:get-key (xt/x:second out) "_name")])
  => ["quote" "hello"])

^{:refer kmi.lang.parser-forms/read-syntax-quote :added "4.1" :id kmi-seed-8}
(fact ""

  (!.js (var out (list/list-to-array (forms/read-syntax-quote (rdr/create "hello") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:get-key (xt/x:second out) "_name")])
  => ["syntax-quote" "hello"])

^{:refer kmi.lang.parser-forms/read-deref :added "4.1" :id kmi-seed-9}
(fact ""

  (!.js (var out (list/list-to-array (forms/read-deref (rdr/create "hello") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:get-key (xt/x:second out) "_name")])
  => ["deref" "hello"])

^{:refer kmi.lang.parser-forms/read-unquote :added "4.1" :id kmi-seed-10}
(fact ""

  (!.js (var out (list/list-to-array (forms/read-unquote (rdr/create "hello") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:get-key (xt/x:second out) "_name")])
  => ["unquote" "hello"])

^{:refer kmi.lang.parser-forms/read-unquote-splicing :added "4.1" :id kmi-seed-11}
(fact ""

  (!.js (var out (list/list-to-array (forms/read-unquote-splicing (rdr/create "hello") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:get-key (xt/x:second out) "_name")])
  => ["unquote-splicing" "hello"])

^{:refer kmi.lang.parser-forms/read-unquote :id kmi-extra-1}
(fact "promotes ~@ to unquote-splicing in form readers"

  (!.js (var out (list/list-to-array (forms/read-unquote (rdr/create "@hello") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:get-key (xt/x:second out) "_name")])
  => ["unquote-splicing" "hello"])

^{:refer kmi.lang.parser-forms/read-meta :added "4.1" :id kmi-seed-12}
(fact ""

  (!.js (var out (forms/read-meta (rdr/create ":dynamic hello") (fn [reader] (return (core/read reader)))))
        (var meta (syn/get-metadata out))
        [(ic/is-syntax? out)
         (hm/hashmap-lookup-key meta (kw/keyword nil "dynamic") false)])
  => [true true])

^{:refer kmi.lang.parser-forms/read-var-quote :added "4.1" :id kmi-seed-13}
(fact ""

  (!.js (var out (list/list-to-array (forms/read-var-quote (rdr/create "hello") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:get-key (xt/x:second out) "_name")])
  => ["var" "hello"])

^{:refer kmi.lang.parser-forms/read-discard :added "4.1" :id kmi-seed-14}
(fact ""

  (!.js (var out (forms/read-discard (rdr/create "hello world") (fn [reader] (return (core/read reader)))))
        (xt/x:get-key out "_name"))
  => "world")

^{:refer kmi.lang.parser-forms/read-dispatch :added "4.1" :id kmi-seed-15}
(fact ""

  (!.js (var out (forms/read-dispatch (rdr/create "{:a :b}") (fn [reader] (return (core/read reader)))))
        [(xt/x:get-key out "_size")
         (hs/hashset-has? out (kw/keyword nil "b"))])
  => [2 true])

^{:refer kmi.lang.parser-forms/read-dispatch :id kmi-extra-2}
(fact "dispatch helpers cover #' and #_"

  (!.js (var quoted (list/list-to-array (forms/read-dispatch (rdr/create "'hello") (fn [reader] (return (core/read reader))))))
         (var out (forms/read-discard (rdr/create "skip keep") (fn [reader] (return (core/read reader)))))
         [[(xt/x:get-key (xt/x:first quoted) "_name")
           (xt/x:get-key (xt/x:second quoted) "_name")]
          (xt/x:get-key out "_name")])
  => [["var" "hello"] "keep"])

^{:refer kmi.lang.parser-forms/read-required :id kmi-extra-3}
(fact "prefix form helpers reject EOF"

  (!.js
   (forms/read-quote (rdr/create "") (fn [reader] (return (core/read reader)))))
  => (throws)

  (!.js
   (forms/read-unquote (rdr/create "") (fn [reader] (return (core/read reader)))))
  => (throws)

  (!.js
   (forms/read-meta (rdr/create ":tag") (fn [reader] (return (core/read reader)))))
  => (throws)

  (!.js
   (forms/read-discard (rdr/create "skip") (fn [reader] (return (core/read reader)))))
  => (throws))

^{:refer kmi.lang.parser-forms/read-delimited :added "4.1" :id kmi-seed-16}
(fact "handles empty input and EOF before closing delimiter"

  (!.js (forms/read-delimited (rdr/create "]") "]" (fn [reader] (return (core/read reader)))))
  => []

  (!.js (forms/read-delimited (rdr/create "1 2") "]" (fn [reader] (return (core/read reader)))))
  => (throws))

^{:refer kmi.lang.parser-forms/read-list :added "4.1" :id kmi-seed-17}
(fact "handles empty lists and EOF"

  (!.js (list/list-to-array (forms/read-list (rdr/create ")") (fn [reader] (return (core/read reader))))))
  => []

  (!.js (forms/read-list (rdr/create "") (fn [reader] (return (core/read reader)))))
  => (throws))

^{:refer kmi.lang.parser-forms/read-vector :added "4.1" :id kmi-seed-18}
(fact "handles empty vectors and EOF"

  (!.js (p/to-array (forms/read-vector (rdr/create "]") (fn [reader] (return (core/read reader))))))
  => []

  (!.js (forms/read-vector (rdr/create "") (fn [reader] (return (core/read reader)))))
  => (throws))

^{:refer kmi.lang.parser-forms/read-map :added "4.1" :id kmi-seed-19}
(fact "handles empty maps and rejects odd number of entries"

  (!.js (var out (forms/read-map (rdr/create "}") (fn [reader] (return (core/read reader)))))
        [(xt/x:get-key out "_size")])
  => [0]

  (!.js (forms/read-map (rdr/create ":a}") (fn [reader] (return (core/read reader)))))
  => (throws))

^{:refer kmi.lang.parser-forms/read-set :added "4.1" :id kmi-seed-20}
(fact "handles empty sets"

  (!.js (var out (forms/read-set (rdr/create "}") (fn [reader] (return (core/read reader)))))
        [(xt/x:get-key out "_size")])
  => [0])

^{:refer kmi.lang.parser-forms/read-required :added "4.1" :id kmi-seed-21}
(fact "reads required forms and throws on EOF"

  (!.js (forms/read-required (rdr/create "42") (fn [reader] (return (core/read reader)))))
  => 42

  (!.js (forms/read-required (rdr/create "") (fn [reader] (return (core/read reader)))))
  => (throws))

^{:refer kmi.lang.parser-forms/read-quote :added "4.1" :id kmi-seed-22}
(fact "quotes non-symbol forms"

  (!.js (var out (list/list-to-array (forms/read-quote (rdr/create "42") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:second out)])
  => ["quote" 42])

^{:refer kmi.lang.parser-forms/read-syntax-quote :added "4.1" :id kmi-seed-23}
(fact "syntax-quotes non-symbol forms"

  (!.js (var out (list/list-to-array (forms/read-syntax-quote (rdr/create "42") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:second out)])
  => ["syntax-quote" 42])

^{:refer kmi.lang.parser-forms/read-deref :added "4.1" :id kmi-seed-24}
(fact "derefs non-symbol forms"

  (!.js (var out (list/list-to-array (forms/read-deref (rdr/create "42") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:second out)])
  => ["deref" 42])

^{:refer kmi.lang.parser-forms/read-unquote-splicing :added "4.1" :id kmi-seed-25}
(fact "reads unquote-splicing directly"

  (!.js (var out (list/list-to-array (forms/read-unquote-splicing (rdr/create "42") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:second out)])
  => ["unquote-splicing" 42])

^{:refer kmi.lang.parser-forms/read-unquote :added "4.1" :id kmi-seed-26}
(fact "reads plain unquote without splicing"

  (!.js (var out (list/list-to-array (forms/read-unquote (rdr/create "42") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:second out)])
  => ["unquote" 42])

^{:refer kmi.lang.parser-forms/read-meta :added "4.1" :id kmi-seed-27}
(fact "reads map metadata"

  (!.js (var out (forms/read-meta (rdr/create "{:a 1} hello") (fn [reader] (return (core/read reader)))))
        (var meta (syn/get-metadata out))
        [(ic/is-syntax? out)
         (hm/hashmap-lookup-key meta (kw/keyword nil "a") false)])
  => [true 1])

^{:refer kmi.lang.parser-forms/read-var-quote :added "4.1" :id kmi-seed-28}
(fact "var-quotes non-symbol forms"

  (!.js (var out (list/list-to-array (forms/read-var-quote (rdr/create "42") (fn [reader] (return (core/read reader))))))
        [(xt/x:get-key (xt/x:first out) "_name")
         (xt/x:second out)])
  => ["var" 42])

^{:refer kmi.lang.parser-forms/read-discard :added "4.1" :id kmi-seed-29}
(fact "discards the next form and returns the following one"

  (!.js (forms/read-discard (rdr/create "1 2 3") (fn [reader] (return (core/read reader)))))
  => 2)

^{:refer kmi.lang.parser-forms/read-dispatch :added "4.1" :id kmi-seed-30}
(fact "rejects unknown and EOF dispatch macros"

  (!.js (forms/read-dispatch (rdr/create "x") (fn [reader] (return (core/read reader)))))
  => (throws)

  (!.js (forms/read-dispatch (rdr/create "") (fn [reader] (return (core/read reader)))))
  => (throws))
