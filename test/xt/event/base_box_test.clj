(ns xt.event.base-box-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.event.base-box :as box]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.event.base-box :as box]]})

(l/script- :python
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
  => ["event.box" {} {"a" 1}]

  (!.lua
   (var b (box/make-box (fn:> {:a 1})))
   [(. b ["::"])
    (. b ["listeners"])
    (box/get-data b [])])
  => ["event.box" {} {"a" 1}]

  (!.py
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
  => [true true false false]

  (!.lua
   [(box/check-event {:path ["a" "b"]} [])
    (box/check-event {:path ["a" "b"]} ["a"])
    (box/check-event {:path ["a" "b"]} ["a" "c"])
    (box/check-event {:path ["a" "b"]} ["a" "b" "c"])])
  => [true true false false]

  (!.py
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
      "data" {"a" {"b" 3}}}

  (notify/wait-on :lua
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
      "data" {"a" {"b" 3}}}

  (notify/wait-on :python
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

^{:refer xt.event.base-box/get-data :added "4.1"}
(fact "gets root and nested data"

  (!.js
   (var b (box/make-box (fn:> {:a {:b 2}
                               :items [1 2]})))
   [(box/get-data b nil)
    (box/get-data b ["a" "b"])
    (box/get-data b "items")])
  => [{"a" {"b" 2}
       "items" [1 2]}
      2
      [1 2]]

  (!.lua
   (var b (box/make-box (fn:> {:a {:b 2}
                               :items [1 2]})))
   [(box/get-data b nil)
    (box/get-data b ["a" "b"])
    (box/get-data b "items")])
  => [{"a" {"b" 2}
       "items" [1 2]}
      2
      [1 2]]

  (!.py
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

  (!.js
    (var b (box/make-box (fn:> {:a {:b 2}})))
    (box/set-data-raw b ["a" "b"] 3)
    (box/get-data b []))
  => {"a" {"b" 3}}

  (!.lua
    (var b (box/make-box (fn:> {:a {:b 2}})))
    (box/set-data-raw b ["a" "b"] 3)
    (box/get-data b []))
  => {"a" {"b" 3}}

  (!.py
    (var b (box/make-box (fn:> {:a {:b 2}})))
    (box/set-data-raw b ["a" "b"] 3)
    (box/get-data b []))
  => {"a" {"b" 3}})

^{:refer xt.event.base-box/set-data :added "4.1"}
(fact "updates and resets data"

  (!.js
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/set-data b "c" 3)
    (box/get-data b [])
    (box/reset-data b)
    (box/get-data b [])])
  => (just-in
      [empty? {"a" {"b" 2} "c" 3}
       empty? {"a" {"b" 2}}])

  (!.lua
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/set-data b "c" 3)
    (box/get-data b [])
    (box/reset-data b)
    (box/get-data b [])])
  => (just-in
      [empty? {"a" {"b" 2} "c" 3}
       empty? {"a" {"b" 2}}])

  (!.py
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/set-data b "c" 3)
    (box/get-data b [])
    (box/reset-data b)
    (box/get-data b [])])
  => [[] {"a" {"b" 2} "c" 3}
      [] {"a" {"b" 2}}])

^{:refer xt.event.base-box/del-data-raw :added "4.1"}
(fact "removes data without notifying listeners"

  (!.js
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/del-data-raw b ["a" "b"])
    (box/get-data b [])])
  => [true {"a" {}}]

  (!.lua
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/del-data-raw b ["a" "b"])
    (box/get-data b [])])
  => [true {"a" {}}]

  (!.py
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/del-data-raw b ["a" "b"])
    (box/get-data b [])])
  => [true {"a" {}}])

^{:refer xt.event.base-box/del-data :added "4.1"}
(fact "removes data and returns triggered listener ids"

  (!.js
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/del-data b ["a" "b"])
    (box/get-data b [])])
  => (just-in
      [empty? {"a" {}}])

  (!.lua
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/del-data b ["a" "b"])
    (box/get-data b [])])
  => (just-in
      [empty? {"a" {}}])

  (!.py
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/del-data b ["a" "b"])
    (box/get-data b [])])
  => [[] {"a" {}}])

^{:refer xt.event.base-box/reset-data :added "4.1"}
(fact "resets the box back to its initial value"

  (!.js
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/set-data b "c" 3)
    (box/get-data b [])
    (box/reset-data b)
    (box/get-data b [])])
  => (just-in
      [empty? {"a" {"b" 2}
               "c" 3}
       empty? {"a" {"b" 2}}])

  (!.lua
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/set-data b "c" 3)
    (box/get-data b [])
    (box/reset-data b)
    (box/get-data b [])])
  => (just-in
      [empty? {"a" {"b" 2}
               "c" 3}
       empty? {"a" {"b" 2}}])

  (!.py
   (var b (box/make-box (fn:> {:a {:b 2}})))
   [(box/set-data b "c" 3)
    (box/get-data b [])
    (box/reset-data b)
    (box/get-data b [])])
  => [[] {"a" {"b" 2}
          "c" 3}
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
      "c" 3}

  (!.lua
   (var b (box/make-box (fn:> {:a 1 :b []})))
   (box/merge-data b [] {:c 3})
   (box/append-data b ["b"] {:title "Hello"})
   (box/get-data b []))
  => {"a" 1
      "b" [{"title" "Hello"}]
      "c" 3}

  (!.py
   (var b (box/make-box (fn:> {:a 1 :b []})))
   (box/merge-data b [] {:c 3})
   (box/append-data b ["b"] {:title "Hello"})
   (box/get-data b []))
  => {"a" 1
      "b" [{"title" "Hello"}]
      "c" 3})

^{:refer xt.event.base-box/append-data :added "4.1"}
(fact "appends a value onto an array path"

  (!.js
   (var b (box/make-box (fn:> {:a []})))
   (box/append-data b ["a"] {:title "Hello"
                             :body "World"})
   (box/get-data b []))
  => {"a" [{"title" "Hello"
            "body" "World"}]}

  (!.lua
   (var b (box/make-box (fn:> {:a []})))
   (box/append-data b ["a"] {:title "Hello"
                             :body "World"})
   (box/get-data b []))
  => {"a" [{"title" "Hello"
            "body" "World"}]}

  (!.py
   (var b (box/make-box (fn:> {:a []})))
   (box/append-data b ["a"] {:title "Hello"
                             :body "World"})
   (box/get-data b []))
  => {"a" [{"title" "Hello"
            "body" "World"}]})

(comment
  (s/snapto '[xt.cell])
  
  (s/seedgen-benchadd '[xt.event.base-box] {:lang [:ruby] :write true})
  (s/seedgen-langadd '[xt.event.base-box]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.base-box]  {:lang [:lua :python] :write true}))
