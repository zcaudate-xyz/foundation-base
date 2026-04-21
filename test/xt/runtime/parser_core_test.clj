(ns xt.runtime.parser-core-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.parser-core :as core]
             [xt.runtime.reader :as rdr]
             [xt.runtime.type-list :as list]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.parser-core/read-delimited :added "4.1"}
(fact
  (!.js (var reader (rdr/create "1 2]"))
        (core/read-delimited reader "]"))
  => [1 2])

^{:refer xt.runtime.parser-core/read :added "4.1"}
(fact
  (!.js (var out (core/read (rdr/create "(1 2 3)")))
        (list/list-to-array out))
  => [1 2 3])

^{:refer xt.runtime.parser-core/read-string :added "4.1"}
(fact
  (!.js [(. (core/read-string "[1 2]") (to-array))
         (== nil (core/read-string "  ; comment\n"))])
  => [[1 2] true])
