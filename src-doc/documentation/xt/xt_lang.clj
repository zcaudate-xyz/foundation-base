(ns documentation.xt-lang
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

[[:hero {:title "xt.lang"
         :subtitle "Portable language primitives and common libraries."
         :lead "`xt.lang` defines reusable xtalk libraries that target JS, Lua, Python, Dart, and other runtimes through hara.lang emission."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Target languages differ in collection APIs, nil handling, string operations, promises, modules, and resource access. `xt.lang` gives generated programs one shared vocabulary for those behaviors."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

(fact "check array type"
  ^{:refer xt.lang.common-lib/is-array? :added "4.0"}
  (!.js
    [(k/is-array? [1 2 3])
     (k/is-array? {:a 1})])
  => [true false])

(fact "check string type"
  ^{:refer xt.lang.common-lib/is-string? :added "4.0"}
  (!.js
    [(k/is-string? "hello")
     (k/is-string? 1)])
  => [true false])

(fact "check object type"
  ^{:refer xt.lang.common-lib/is-object? :added "4.0"}
  (!.js
    [(k/is-object? {:a 1})
     (k/is-object? [1 2 3])])
  => [true false])

[[:chapter {:title "API" :link "api"}]]
