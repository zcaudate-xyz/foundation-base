(ns xt.runtime.parser-forms-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.runtime.interface-common :as ic]
             [xt.runtime.parser-core :as core]
             [xt.runtime.parser-forms :as forms]
             [xt.runtime.reader :as rdr]
             [xt.runtime.type-hashmap :as hm]
             [xt.runtime.type-hashset :as hs]
             [xt.runtime.type-keyword :as kw]
             [xt.runtime.type-list :as list]
             [xt.runtime.type-syntax :as syn]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.parser-forms/read-delimited :added "4.1"}
(fact
  (!.js (var reader (rdr/create "1 2]"))
        (forms/read-delimited reader "]" core/read))
  => [1 2])

^{:refer xt.runtime.parser-forms/read-list :added "4.1"}
(fact
  (!.js (list/list-to-array (forms/read-list (rdr/create "1 2 3)") core/read)))
  => [1 2 3])

^{:refer xt.runtime.parser-forms/read-vector :added "4.1"}
(fact
  (!.js (. (forms/read-vector (rdr/create "1 2 3]") core/read) (to-array)))
  => [1 2 3])

^{:refer xt.runtime.parser-forms/read-map :added "4.1"}
(fact
  (!.js (var out (forms/read-map (rdr/create ":a 1 :b 2}") core/read))
        [(. out _size)
         (hm/hashmap-lookup-key out (kw/keyword nil "a") "missing")])
  => [2 1])

^{:refer xt.runtime.parser-forms/read-set :added "4.1"}
(fact
  (!.js (var out (forms/read-set (rdr/create ":a :b :a}") core/read))
        [(. out _size)
         (hs/hashset-has? out (kw/keyword nil "a"))])
  => [2 true])

^{:refer xt.runtime.parser-forms/read-required :added "4.1"}
(fact
  (!.js
   (. (forms/read-required (rdr/create "hello") core/read) _name))
  => "hello"

  (!.js
   (forms/read-required (rdr/create "") core/read))
  => (throws))

^{:refer xt.runtime.parser-forms/read-quote :added "4.1"}
(fact
  (!.js (var out (list/list-to-array (forms/read-quote (rdr/create "hello") core/read)))
        [(. (xt/x:first out) _name)
         (. (xt/x:second out) _name)])
  => ["quote" "hello"])

^{:refer xt.runtime.parser-forms/read-syntax-quote :added "4.1"}
(fact
  (!.js (var out (list/list-to-array (forms/read-syntax-quote (rdr/create "hello") core/read)))
        [(. (xt/x:first out) _name)
         (. (xt/x:second out) _name)])
  => ["syntax-quote" "hello"])

^{:refer xt.runtime.parser-forms/read-deref :added "4.1"}
(fact
  (!.js (var out (list/list-to-array (forms/read-deref (rdr/create "hello") core/read)))
        [(. (xt/x:first out) _name)
         (. (xt/x:second out) _name)])
  => ["deref" "hello"])

^{:refer xt.runtime.parser-forms/read-unquote :added "4.1"}
(fact
  (!.js (var out (list/list-to-array (forms/read-unquote (rdr/create "hello") core/read)))
        [(. (xt/x:first out) _name)
         (. (xt/x:second out) _name)])
  => ["unquote" "hello"])

^{:refer xt.runtime.parser-forms/read-unquote-splicing :added "4.1"}
(fact
  (!.js (var out (list/list-to-array (forms/read-unquote-splicing (rdr/create "hello") core/read)))
        [(. (xt/x:first out) _name)
         (. (xt/x:second out) _name)])
  => ["unquote-splicing" "hello"])

(fact "promotes ~@ to unquote-splicing in form readers"
  (!.js (var out (list/list-to-array (forms/read-unquote (rdr/create "@hello") core/read)))
        [(. (xt/x:first out) _name)
         (. (xt/x:second out) _name)])
  => ["unquote-splicing" "hello"])

^{:refer xt.runtime.parser-forms/read-meta :added "4.1"}
(fact
  (!.js (var out (forms/read-meta (rdr/create ":dynamic hello") core/read))
        (var meta (syn/get-metadata out))
        [(ic/is-syntax? out)
         (hm/hashmap-lookup-key meta (kw/keyword nil "dynamic") false)])
  => [true true])

^{:refer xt.runtime.parser-forms/read-var-quote :added "4.1"}
(fact
  (!.js (var out (list/list-to-array (forms/read-var-quote (rdr/create "hello") core/read)))
        [(. (xt/x:first out) _name)
         (. (xt/x:second out) _name)])
  => ["var" "hello"])

^{:refer xt.runtime.parser-forms/read-discard :added "4.1"}
(fact
  (!.js (var out (forms/read-discard (rdr/create "hello world") core/read))
        (. out _name))
  => "world")

^{:refer xt.runtime.parser-forms/read-dispatch :added "4.1"}
(fact
  (!.js (var out (forms/read-dispatch (rdr/create "{:a :b}") core/read))
        [(. out _size)
         (hs/hashset-has? out (kw/keyword nil "b"))])
  => [2 true])

(fact "dispatch helpers cover #' and #_"
  (!.js (var quoted (list/list-to-array (forms/read-dispatch (rdr/create "'hello") core/read)))
         (var out (forms/read-discard (rdr/create "skip keep") core/read))
         [[(. (xt/x:first quoted) _name)
           (. (xt/x:second quoted) _name)]
          (. out _name)])
  => [["var" "hello"] "keep"])

(fact "prefix form helpers reject EOF"
  (!.js
   (forms/read-quote (rdr/create "") core/read))
  => (throws)

  (!.js
   (forms/read-unquote (rdr/create "") core/read))
  => (throws)

  (!.js
   (forms/read-meta (rdr/create ":tag") core/read))
  => (throws)

  (!.js
   (forms/read-discard (rdr/create "skip") core/read))
  => (throws))
