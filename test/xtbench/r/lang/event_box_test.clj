(ns
 xtbench.r.lang.event-box-test
 (:require
  [rt.basic :as basic]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-box/make-box, :added "4.0"}
(fact
 "creates a box"
 ^{:hidden true}
 (!.R (box/make-box (fn:> {:a 1})))
 =>
 {"::" "event.box", "listeners" {}, "data" {"a" 1}})

^{:refer xt.lang.event-box/check-event, :added "4.0"}
(fact
 "checks that event matches path predicate"
 ^{:hidden true}
 (!.R
  [(box/check-event {:path ["a" "b"]} [])
   (box/check-event {:path ["a" "b"]} ["a"])
   (box/check-event {:path ["a" "b"]} ["a" "c"])
   (box/check-event {:path ["a" "b"]} ["a" "b" "c"])])
 =>
 [true true false false])

^{:refer xt.lang.event-box/add-listener, :added "4.0"}
(fact
 "adds a listener to box"
 ^{:hidden true}
 (notify/wait-on
  :r
  (var b (box/make-box (fn:> {:a {:b 2}})))
  (box/add-listener b "abc" ["a"] (repl/>notify))
  (box/set-data b ["a" "b"] 3))
 =>
 {"path" ["a" "b"],
  "value" 3,
  "meta"
  {"box/path" ["a"], "listener/id" "abc", "listener/type" "box"},
  "data" {"a" {"b" 3}}})

^{:refer xt.lang.event-box/set-data-raw, :added "4.0"}
(fact
 "sets the data in the box"
 ^{:hidden true}
 (!.R
  (var b (box/make-box (fn:> {:a {:b 2}})))
  (box/set-data-raw b ["c"] 3))
 =>
 {"a" {"b" 2}, "c" 3})

^{:refer xt.lang.event-box/set-data, :added "4.0"}
(fact
 "sets data with a trigger"
 ^{:hidden true}
 (!.R
  (var b (box/make-box (fn:> {:a {:b 2}})))
  [(box/set-data b "c" 3) (box/get-data b)])
 =>
 [[] {"a" {"b" 2}, "c" 3}])

^{:refer xt.lang.event-box/del-data-raw, :added "4.0"}
(fact
 "removes the data in the box"
 ^{:hidden true}
 (!.R
  (var b (box/make-box (fn:> {:a {:b 2}})))
  [(box/del-data-raw b ["a" "b"]) (box/get-data b)])
 =>
 [true {"a" {}}])

^{:refer xt.lang.event-box/del-data, :added "4.0"}
(fact
 "removes data with trigger"
 ^{:hidden true}
 (!.R
  (var b (box/make-box (fn:> {:a {:b 2}})))
  [(box/del-data b ["a" "b"]) (box/get-data b)])
 =>
 [[] {"a" {}}])

^{:refer xt.lang.event-box/merge-data, :added "4.0"}
(fact
 "merges the data in the box"
 ^{:hidden true}
 (!.R
  (var b (box/make-box (fn:> {:a 1, :b 2})))
  (box/merge-data b [] {:c 3, :d 4})
  (box/get-data b))
 =>
 {"d" 4, "a" 1, "b" 2, "c" 3})

^{:refer xt.lang.event-box/append-data, :added "4.0"}
(fact
 "merges the data in the box"
 ^{:hidden true}
 (!.R
  (var b (box/make-box (fn:> {:a []})))
  (box/append-data b ["a"] {:title "Hello", :body "World"})
  (box/get-data b))
 =>
 {"a" [{"body" "World", "title" "Hello"}]})
