(ns std.lib.origin-test
  (:require [std.lib.foundation :as h]
            [std.lib.origin :as ori])
  (:use code.test))

^{:refer std.lib.origin/clear-origin :added "3.0"}
(fact "clears the `*origin*` atom"

  (ori/clear-origin)
  => {})

^{:refer std.lib.origin/set-origin :added "3.0"}
(fact "sets the origin of a namespace"

  (ori/set-origin 'std.lib.origin)
  => '{std.lib.origin-test std.lib.origin})

^{:refer std.lib.origin/unset-origin :added "3.0"
  :setup [(ori/set-origin 'std.lib.origin)]}
(fact "unsets the origin of a namespace"

  (ori/unset-origin 'std.lib.origin-test)
  => 'std.lib.origin

  (ori/unset-origin)
  => nil)

^{:refer std.lib.origin/get-origin :added "3.0"
  :setup [(ori/set-origin 'std.lib.origin)]}
(fact "get the origin of a namespace"

  (ori/get-origin)
  => 'std.lib.origin)

^{:refer std.lib.origin/defn.origin :added "3.0"}
(fact "creates a function with settable origin"

  (ori/defn.origin hello
    []
    (.getName *ns*))

  (do (ori/set-origin 'std.lib.origin)
      (hello))
  => 'std.lib.origin

  (do (ori/unset-origin)
      (hello))
  => 'std.lib.origin-test)