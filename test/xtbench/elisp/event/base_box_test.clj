(ns xtbench.elisp.event.base-box-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :elisp
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.event.base-box :as box]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.base-box/make-box :added "4.1"}
(fact "creates an explicit event box"

  (!.elisp
   (var b (box/make-box (fn:> {:a 1})))
   [(. b ["::"])
    (. b ["listeners"])
    (box/get-data b [])])
  => ["event.box" {} {"a" 1}])

^{:refer xt.event.base-box/check-event :added "4.1"}
(fact "checks path matches"

  (!.elisp
   [(box/check-event {:path ["a" "b"]} [])
    (box/check-event {:path ["a" "b"]} ["a"])
    (box/check-event {:path ["a" "b"]} ["a" "c"])
    (box/check-event {:path ["a" "b"]} ["a" "b" "c"])])
  => [true true false false])

^{:refer xt.event.base-box/add-listener :added "4.1"}
(fact "triggers listeners for matching paths"

  (notify/wait-on :elisp
    (var b (box/make-box (fn:> {:a {:b 2}})))
    (box/add-listener b
                      "abc"
                      ["a"]
                      (fn [id data t meta]
                        (repl/notify {"id" id "data" data "t" t "meta" meta}))
                      nil)
    (box/set-data b ["a" "b"] 3))
  => {"id" "abc"
      "data" {"path" ["a" "b"]
              "value" 3
              "data" {"a" {"b" 3}}}
      "t" nil
      "meta" {"box/path" ["a"]
              "listener/id" "abc"
              "listener/type" "box"}})

^{:refer xt.event.base-box/get-data :added "4.1"}
(fact "gets root and nested data"

  (!.elisp
   (var b (box/make-box (fn:> {:a {:b 2}
                               :items [1 2]})))
   [(box/get-data b nil)
    (box/get-data b ["a" "b"])
    (box/get-data b "items")])
  => [{"a" {"b" 2}
       "items" [1 2]}
      2
      [1 2]])

^{:refer xt.event.base-box/set-data-raw :added "4.1"}
(fact "sets box data without triggering listeners"

  (!.elisp
    (var b (box/make-box (fn:> {:a {:b 2}})))
    (box/set-data-raw b ["a" "b"] 3)
    (box/get-data b []))
  => {"a" {"b" 3}})

^{:refer xt.event.base-box/set-data :added "4.1"}
(fact "updates and resets data"

  (!.elisp
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/set-data b "c" 3)
    (box/get-data b [])
    (box/reset-data b)
    (box/get-data b [])])
  => (just-in
      [empty? {"a" {"b" 2} "c" 3}
       empty? {"a" {"b" 2}}]))

^{:refer xt.event.base-box/del-data-raw :added "4.1"}
(fact "removes data without notifying listeners"

  (!.elisp
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/del-data-raw b ["a" "b"])
    (box/get-data b [])])
  => [true {"a" {}}])

^{:refer xt.event.base-box/del-data :added "4.1"}
(fact "removes data and returns triggered listener ids"

  (!.elisp
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/del-data b ["a" "b"])
    (box/get-data b [])])
  => (just-in
      [empty? {"a" {}}]))

^{:refer xt.event.base-box/reset-data :added "4.1"}
(fact "resets the box back to its initial value"

  (!.elisp
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/set-data b "c" 3)
    (box/get-data b [])
    (box/reset-data b)
    (box/get-data b [])])
  => (just-in
      [empty? {"a" {"b" 2}
               "c" 3}
       empty? {"a" {"b" 2}}]))

^{:refer xt.event.base-box/merge-data :added "4.1"}
(fact "merges and appends data"

  (!.elisp
   (var b (box/make-box (fn:> {:a 1 :b []})))
   (box/merge-data b [] {:c 3})
   (box/append-data b ["b"] {:title "Hello"})
   (box/get-data b []))
  => {"a" 1
      "b" [{"title" "Hello"}]
      "c" 3})

^{:refer xt.event.base-box/append-data :added "4.1"}
(fact "appends a value onto an array path"

  (!.elisp
   (var b (box/make-box (fn:> {:a []})))
   (box/append-data b ["a"] {:title "Hello"
                             :body "World"})
   (box/get-data b []))
  => {"a" [{"title" "Hello"
            "body" "World"}]})

(comment
  (s/snapto '[xt.event.base-box])
  
  (s/seedgen-benchadd '[xt.event.base-box] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.base-box]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.base-box]  {:lang [:lua :python] :write true}))
