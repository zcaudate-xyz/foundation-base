(ns documentation.xt-lang
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-string :as xts]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

[[:hero {:title "xt.lang"
         :subtitle "Portable language primitives and common libraries."
         :lead "`xt.lang` defines reusable xtalk libraries that target JS, Lua, Python, Dart, and other runtimes through hara.lang emission."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Target languages differ in collection APIs, nil handling, string operations, promises, modules, and resource access. `xt.lang` gives generated programs one shared vocabulary for those behaviors."

[[:chapter {:title "How to use it" :link "usage"}]]

"A hara script requires the libraries it needs, then emitted xtalk code calls those portable helpers. Application examples in `src-build/play/*xtalk*` and tests under `test-lang/xt/lang` show this pattern."

(comment
  (l/script :xtalk
    {:require [[xt.lang.spec-base :as xt]
               [xt.lang.common-data :as data]
               [xt.lang.common-string :as string]]}))

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Portable iteration"}]]

"`xt.lang.spec-base` provides loop forms that expand to idiomatic loops on each target: `for:array`, `for:object`, and `for:index`."

(fact "iterate arrays and objects portably"
  ^{:refer xt.lang.spec-base/for:array :added "4.0"}
  (!.js
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3]

  ^{:refer xt.lang.spec-base/for:object :added "4.0"}
  (!.js
    (var out [])
    (xt/for:object [[k v] {:a 1 :b 2}]
      (xt/x:arr-push out [k v]))
    out)
  => (just [["a" 1] ["b" 2]] :in-any-order))

[[:section {:title "Type predicates"}]]

"`xt.lang.common-lib` exposes portable type checks so emitted code behaves consistently even when target truthiness differs."

(fact "check object type"
  ^{:refer xt.lang.common-lib/is-object? :added "4.0"}
  (!.js
    [(k/is-object? {:a 1})
     (k/is-object? [1 2 3])])
  => [true false]

  ^{:refer xt.lang.common-lib/is-string? :added "4.0"}
  (!.js
    [(k/is-string? "hello")
     (k/is-string? 1)])
  => [true false]

  ^{:refer xt.lang.common-lib/is-array? :added "4.0"}
  (!.js
    [(k/is-array? [1 2 3])
     (k/is-array? {:a 1})])
  => [true false])

[[:section {:title "String helpers"}]]

"`xt.lang.common-string` provides portable split, join, replace, and symbol-path helpers."

(fact "split, join, and inspect symbol paths"
  ^{:refer xt.lang.common-string/split :added "4.0"}
  (!.js
    (xts/split "hello/world" "/"))
  => ["hello" "world"]

  ^{:refer xt.lang.common-string/join :added "4.0"}
  (!.js
    (xts/join "/" ["hello" "world"]))
  => "hello/world"

  ^{:refer xt.lang.common-string/sym-pair :added "4.0"}
  (!.js
    (xts/sym-pair "xt.lang/common-string"))
  => ["xt.lang" "common-string"])

[[:section {:title "End-to-end: a tiny word counter"}]]

"Combining loops and string helpers gives a portable word count that runs the same in every target runtime."

(fact "count words in a sentence"
  ^{:refer xt.lang.common-string/split :added "4.0"}
  (!.js
    (var words (xts/split "hello portable world" " "))
    (var count 0)
    (xt/for:array [w words]
      (:= count (+ count 1)))
    count)
  => 3)

[[:chapter {:title "Internal usage" :link "internal"}]]

"The common libraries are used by xt.db, xt.event, xt.net, xt.substrate, and by generated single-source examples. The `test-lang/xtbench` tests exercise cross-target parity for these helpers."

[[:chapter {:title "API" :link "api"}]]
