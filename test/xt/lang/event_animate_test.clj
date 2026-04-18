(ns xt.lang.event-animate-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.event-animate :as base-animate]
             [xt.lang.event-animate-mock :as mock]
             [js.core :as j]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.event-animate :as base-animate]
             [xt.lang.event-animate-mock :as mock]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.event-animate :as base-animate]
             [xt.lang.event-animate-mock :as mock]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})


^{:refer xt.lang.event-animate/new-derived :added "4.0"}
(fact "creates a new derived value"
  ^:hidden
  
  (!.js
   (mock/get-value
    (base-animate/new-derived mock/MOCK
                            (fn:> [a b c] (+ a b c))
                            [(mock/new-observed 1)
                             (mock/new-observed 2)
                             (mock/new-observed 3)])))
  => 6

  (!.lua
   (mock/get-value
    (base-animate/new-derived mock/MOCK
                            (fn:> [a b c] (+ a b c))
                            [(mock/new-observed 1)
                             (mock/new-observed 2)
                             (mock/new-observed 3)])))
  => 6)

^{:refer xt.lang.event-animate/listen-single :added "4.0"}
(fact "listens to a single observer"
  ^:hidden
  
  (!.js
   (var ref {:current {}})
   (var obs (mock/new-observed 0.5))
   (var get-style (fn:> [e] (. e
                               ["current"]
                               ["props"]
                               ["style"])))
   (base-animate/listen-single mock/MOCK
                             ref
                             obs
                             (fn:> [v]
                               {:style {:opacity (+ 0.2 v)}}))
   [(get-style ref)
    (mock/set-value obs 0.4)
    (get-style ref)
    (mock/set-value obs 0.3)
    (get-style ref)])
  => [{"opacity" 0.7}
      nil
      {"opacity" 0.6000000000000001}
      nil
      {"opacity" 0.5}]
  
  (!.lua
   (var ref {:current {}})
   (var obs (mock/new-observed 0.5))
   (var get-style (fn:> [e] (. e
                               ["current"]
                               ["props"]
                               ["style"])))
   (base-animate/listen-single mock/MOCK
                             ref
                             obs
                             (fn:> [v]
                               {:style {:opacity (+ 0.2 v)}}))
   [(get-style ref)
    (mock/set-value obs 0.4)
    (get-style ref)
    (mock/set-value obs 0.3)
    (get-style ref)])
  => [{"opacity" 0.7} nil
      {"opacity" 0.6} nil
      {"opacity" 0.5}])

^{:refer xt.lang.event-animate/listen-array :added "4.0"}
(fact "listens to array for changes"
  ^:hidden
  
  (!.js
   (var ref {:current {}})
   (var o1 (mock/new-observed 0.1))
   (var o2 (mock/new-observed 0.2))
   (var get-style (fn:> [e] (. e
                               ["current"]
                               ["props"]
                               ["style"])))
   (base-animate/listen-array mock/MOCK
                            ref
                            [o1 o2]
                            (fn [o1 o2]
                              (return {:style {:opacity (+ 0.1 o1 o2)}})))
   [(get-style ref)
    (mock/set-value o1 0.4)
    (get-style ref)
    (mock/set-value o2 0.3)
    (get-style ref)])
  => [{"opacity" 0.4} nil {"opacity" 0.7} nil {"opacity" 0.8}]
  
  (!.lua
   (var ref {:current {}})
   (var o1 (mock/new-observed 0.1))
   (var o2 (mock/new-observed 0.2))
   (var get-style (fn:> [e] (. e
                               ["current"]
                               ["props"]
                               ["style"])))
   (base-animate/listen-array mock/MOCK
                            ref
                            [o1 o2]
                            (fn [o1 o2]
                              (return {:style {:opacity (+ 0.1 o1 o2)}})))
   [(get-style ref)
    (mock/set-value o1 0.4)
    (get-style ref)
    (mock/set-value o2 0.3)
    (get-style ref)])
  => [{"opacity" 0.4} nil {"opacity" 0.7} nil {"opacity" 0.8}])

^{:refer xt.lang.event-animate/get-map-paths :added "4.0"}
(fact "gets map paths"
  ^:hidden

  (!.js
   (base-animate/get-map-paths mock/MOCK
                             {:a (mock/new-observed 1)
                              :b {:c (mock/new-observed 2)}}))
  => [[[] "a" {"value" 1, "::" "observed", "listeners" []}]
      [[] "b" false]
      [["b"] "c" {"value" 2, "::" "observed", "listeners" []}]]

  (set (!.lua
        (base-animate/get-map-paths mock/MOCK
                                    {:a (mock/new-observed 1)
                                     :b {:c (mock/new-observed 2)}})))
  => #{[{} "a" {"value" 1, "::" "observed", "listeners" {}}]
       [{} "b" false]
       [["b"] "c" {"value" 2, "::" "observed", "listeners" {}}]})

^{:refer xt.lang.event-animate/get-map-input :added "4.0"}
(fact "gets the map input"
  ^:hidden
  
  (!.js
   (base-animate/get-map-input
    mock/MOCK
    [[[] "a" {"value" 1, "::" "observed", "listeners" []}]
     [[] "b" false]
     [["b"] "c" {"value" 2, "::" "observed", "listeners" []}]]))
  => {"a" 1, "b" {"c" 2}}

  (!.lua
   (base-animate/get-map-input
    mock/MOCK
    [[[] "a" {"value" 1, "::" "observed", "listeners" []}]
     [[] "b" false]
     [["b"] "c" {"value" 2, "::" "observed", "listeners" []}]]))
  => {"a" 1, "b" {"c" 2}})

^{:refer xt.lang.event-animate/listen-map :added "4.0"}
(fact "listens to a map of indicators"
  ^:hidden

  (!.js
   (var ref {:current {}})
   (var o1 (mock/new-observed 0.1))
   (var o2 (mock/new-observed 0.2))
   
   (base-animate/listen-map
    mock/MOCK
    ref
    {:a o1
     :b {:c o2}}
    (fn [e]
      (var #{a b} e)
      (var #{c} b)
      (return {:style {:opacity (+ a c)}}))))
  => {"style" {"opacity" 0.30000000000000004}}

  (!.lua
   (var ref {:current {}})
   (var o1 (mock/new-observed 0.1))
   (var o2 (mock/new-observed 0.2))
   
   (base-animate/listen-map
    mock/MOCK
    ref
    {:a o1
     :b {:c o2}}
    (fn [e]
      (var #{a b} e)
      (var #{c} b)
      (return {:style {:opacity (+ a c)}}))))
  => {"style" {"opacity" 0.3}})

^{:refer xt.lang.event-animate/listen-transformations :added "4.0"}
(fact "converts to the necessary listeners"
  ^:hidden

  (!.js
   (base-animate/listen-transformations
    mock/MOCK
    {:current {}}
    nil
    {}
    (fn:> {})))
  => {}
  
  (!.lua
   (base-animate/listen-transformations
    mock/MOCK
    {:current {}}
    nil
    {}
    (fn:> {})))
  => {})

^{:refer xt.lang.event-animate/new-progressing :added "4.0"}
(fact "creates a new progressing element"
  ^:hidden
  
  (!.js
   (base-animate/new-progressing))
  => {"running" false, "queued" [], "animation" nil}

  (!.lua
   (base-animate/new-progressing))
  => {"running" false, "queued" {}})

^{:refer xt.lang.event-animate/run-with-cancel :added "4.0"}
(fact "runs with cancel")

^{:refer xt.lang.event-animate/animate-chained-cleanup :added "4.0"}
(fact "runs with cleanup")

^{:refer xt.lang.event-animate/animate-chained-one :added "4.0"}
(fact "runs with single chain")

^{:refer xt.lang.event-animate/animate-chained-all :added "4.0"}
(fact "runs with all chained")

^{:refer xt.lang.event-animate/run-with-chained :added "4.0"}
(fact "runs chained")

^{:refer xt.lang.event-animate/run-with :added "4.0"}
(fact "runs effects")

^{:refer xt.lang.event-animate/make-binary-transitions :added "4.0"}
(fact "makes a binary transition"
  ^:hidden
  
  (!.js
   (var t (base-animate/make-binary-transitions
           mock/MOCK
           false
           {}))
   (var #{indicator
          zero-fn
          one-fn} t)
   [(mock/get-value indicator)
    (one-fn)
    (mock/get-value indicator)
    (zero-fn)
    (mock/get-value indicator)])
  => [0 nil 1 nil 0]

  (!.lua
   (var t (base-animate/make-binary-transitions
           mock/MOCK
           false
           {}))
   (var #{indicator
          zero-fn
          one-fn} t)
   [(mock/get-value indicator)
    (one-fn)
    (mock/get-value indicator)
    (zero-fn)
    (mock/get-value indicator)])
  => [0 nil 1 nil 0])

^{:refer xt.lang.event-animate/make-binary-indicator :added "4.0"}
(fact "makes a binary indicator"
  ^:hidden
  
  ^{:lang-exceptions {:lua {:skip true}}}
  (!.js
   (var t (base-animate/make-binary-indicator
           mock/MOCK
           false
           {}
           "cancel"
           (base-animate/new-progressing)
           (fn:>)))
   (var #{indicator trigger-fn} t)
   [(mock/get-value indicator)
     (trigger-fn true)
     (mock/get-value indicator)
     (trigger-fn false)
     (mock/get-value indicator)])
  => [0 {"running" false, "queued" []}
      1 {"running" false, "queued" []}
      0])

^{:refer xt.lang.event-animate/make-linear-indicator :added "4.0"}
(fact  "makes a linear indicator"
  ^:hidden
  
  (!.js
   (var prev {:current 1})
   
   (var t (base-animate/make-linear-indicator
           mock/MOCK
           1
           (fn:> (. prev ["current"]))
           (fn [v] (:= (. prev ["current"])
                       v))
           {}
           "cancel"
           (base-animate/new-progressing)
           (fn:>)))
   (var #{indicator trigger-fn} t)
   [(mock/get-value indicator)
    (trigger-fn 3)
    (mock/get-value indicator)
    (trigger-fn 8)
    (mock/get-value indicator)])
  => [1 {"running" false, "queued" []} 3 {"running" false, "queued" []} 8]

  (!.lua
   (var prev {:current 1})
   
   (var t (base-animate/make-linear-indicator
           mock/MOCK
           1
           (fn:> (. prev ["current"]))
           (fn [v] (:= (. prev ["current"])
                       v))
           {}
           "cancel"
           (base-animate/new-progressing)
           (fn:>)))
   (var #{indicator trigger-fn} t)
   [(mock/get-value indicator)
    (trigger-fn 3)
    (mock/get-value indicator)
    (trigger-fn 8)
    (mock/get-value indicator)])
  => [1 {"running" false, "queued" {}} 3 {"running" false, "queued" {}} 8])

^{:refer xt.lang.event-animate/make-circular-indicator :added "4.0"}
(fact "makes a circular indicator"
  ^:hidden
  
  (!.js
   (var prev {:current 1})
   (var t (base-animate/make-circular-indicator
           mock/MOCK
           1
           (fn:> (. prev ["current"]))
           (fn [v] (:= (. prev ["current"])
                       v))
           {}
           "cancel"
           10
           (base-animate/new-progressing)
           (fn:>)))
   (var #{indicator trigger-fn} t)
   [(mock/get-value indicator)
    (trigger-fn -9)
    (mock/get-value indicator)
    (trigger-fn 7)
    (mock/get-value indicator)])
  => [1 {"running" false, "queued" []} 1 {"running" false, "queued" []} -13]

  (!.lua
   (var prev {:current 1})
   (var t (base-animate/make-circular-indicator
           mock/MOCK
           1
           (fn:> (. prev ["current"]))
           (fn [v] (:= (. prev ["current"])
                       v))
           {}
           "cancel"
           10
           (base-animate/new-progressing)
           (fn:>)))
   (var #{indicator trigger-fn} t)
   [(mock/get-value indicator)
    (trigger-fn -9)
    (mock/get-value indicator)
    (trigger-fn 7)
    (mock/get-value indicator)])
  => [1 {"running" false, "queued" {}} 1 {"running" false, "queued" {}} -13])
