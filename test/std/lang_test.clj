(ns std.lang-test
  (:use code.test)
  (:require [std.lang :as l]
            [js.core]
            [xt.lang.base-lib :as k]))

^{:refer std.lang/CANARY :adopt true :added "4.0"}
(fact "test for lang")

^{:refer std.lang/rt:space :added "4.0"}
(fact "will return space if not found (no default space)")

^{:refer std.lang/get-entry :added "4.0"}
(fact "gets the entry if pointer")

^{:refer std.lang/as-lua :added "4.0"}
(fact "change `[]` to `{}`")

^{:refer std.lang/rt:invoke :added "4.0"}
(fact "invokes code in the given namespace"
  ^:hidden
  
  (l/rt:invoke 'js.core :js
               '[(+ 1 2 3)])
  => "1 + 2 + 3;")

^{:refer std.lang/force-require :added "4.0"}
(fact "forces requiring on namespaces until compilation error occurs")

^{:refer std.lang/force-reload :added "4.0"}
(fact "forces reloading of all dependent namespaces")
