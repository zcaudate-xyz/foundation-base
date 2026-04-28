(ns xt.event.base-box-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.event.base-box :as box]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.base-box/make-box :added "4.1"}
(fact "creates an explicit event box"
  (!.js
   (var b (box/make-box (fn:> {:a 1})))
   [(. b ["::"])
    (. b ["listeners"])
    (box/get-data b [])])
  => ["event.box" {} {"a" 1}])

^{:refer xt.event.base-box/check-event :added "4.1"}
(fact "checks path matches"
  (!.js
   [(box/check-event {:path ["a" "b"]} [])
    (box/check-event {:path ["a" "b"]} ["a"])
    (box/check-event {:path ["a" "b"]} ["a" "c"])
    (box/check-event {:path ["a" "b"]} ["a" "b" "c"])])
  => [true true false false])

^{:refer xt.event.base-box/add-listener :added "4.1"}
(fact "triggers listeners for matching paths"
  (notify/wait-on :js
    (var b (box/make-box (fn:> {:a {:b 2}})))
    (box/add-listener b
                      "abc"
                      ["a"]
                      (repl/>notify)
                      nil)
    (box/set-data b ["a" "b"] 3))
  => {"path" ["a" "b"]
      "value" 3
      "meta" {"box/path" ["a"]
              "listener/id" "abc"
              "listener/type" "box"}
      "data" {"a" {"b" 3}}})

^{:refer xt.event.base-box/set-data :added "4.1"}
(fact "updates and resets data"
  (!.js
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/set-data b "c" 3)
    (box/get-data b [])
    (box/reset-data b)
    (box/get-data b [])])
  => [[] {"a" {"b" 2} "c" 3}
      [] {"a" {"b" 2}}])

^{:refer xt.event.base-box/merge-data :added "4.1"}
(fact "merges and appends data"
  (!.js
   (var b (box/make-box (fn:> {:a 1 :b []})))
   (box/merge-data b [] {:c 3})
   (box/append-data b ["b"] {:title "Hello"})
   (box/get-data b []))
  => {"a" 1
      "b" [{"title" "Hello"}]
      "c" 3})
