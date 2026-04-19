(ns
 xtbench.python.lang.event-animate-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.common-repl :as repl]
   [xt.lang.event-animate :as base-animate]
   [xt.lang.event-animate-mock :as mock]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-animate/new-derived, :added "4.0"}
(fact
 "creates a new derived value"
 ^{:hidden true}
 (!.py
  (mock/get-value
   (base-animate/new-derived
    mock/MOCK
    (fn:> [a b c] (+ a b c))
    [(mock/new-observed 1)
     (mock/new-observed 2)
     (mock/new-observed 3)])))
 =>
 6)

^{:refer xt.lang.event-animate/listen-single, :added "4.0"}
(fact
 "listens to a single observer"
 ^{:hidden true}
 (!.py
  (var ref {:current {}})
  (var obs (mock/new-observed 0.5))
  (var get-style (fn:> [e] (. e ["current"] ["props"] ["style"])))
  (base-animate/listen-single
   mock/MOCK
   ref
   obs
   (fn:> [v] {:style {:opacity (+ 0.2 v)}}))
  [(get-style ref)
   (mock/set-value obs 0.4)
   (get-style ref)
   (mock/set-value obs 0.3)
   (get-style ref)])
 =>
 [{"opacity" 0.7}
  nil
  {"opacity" 0.6000000000000001}
  nil
  {"opacity" 0.5}])

^{:refer xt.lang.event-animate/listen-array, :added "4.0"}
(fact
 "listens to array for changes"
 ^{:hidden true}
 (!.py
  (var ref {:current {}})
  (var o1 (mock/new-observed 0.1))
  (var o2 (mock/new-observed 0.2))
  (var get-style (fn:> [e] (. e ["current"] ["props"] ["style"])))
  (base-animate/listen-array
   mock/MOCK
   ref
   [o1 o2]
   (fn [o1 o2] (return {:style {:opacity (+ 0.1 o1 o2)}})))
  [(get-style ref)
   (mock/set-value o1 0.4)
   (get-style ref)
   (mock/set-value o2 0.3)
   (get-style ref)])
 =>
 [{"opacity" 0.4} nil {"opacity" 0.7} nil {"opacity" 0.8}])

^{:refer xt.lang.event-animate/get-map-paths, :added "4.0"}
(fact
 "gets map paths"
 ^{:hidden true}
 (!.py
  (base-animate/get-map-paths
   mock/MOCK
   {:a (mock/new-observed 1), :b {:c (mock/new-observed 2)}}))
 =>
 [[[] "a" {"value" 1, "::" "observed", "listeners" []}]
  [[] "b" false]
  [["b"] "c" {"value" 2, "::" "observed", "listeners" []}]])

^{:refer xt.lang.event-animate/get-map-input, :added "4.0"}
(fact
 "gets the map input"
 ^{:hidden true}
 (!.py
  (base-animate/get-map-input
   mock/MOCK
   [[[] "a" {"value" 1, "::" "observed", "listeners" []}]
    [[] "b" false]
    [["b"] "c" {"value" 2, "::" "observed", "listeners" []}]]))
 =>
 {"a" 1, "b" {"c" 2}})

^{:refer xt.lang.event-animate/listen-map, :added "4.0"}
(fact
 "listens to a map of indicators"
 ^{:hidden true}
 (!.py
  (var ref {:current {}})
  (var o1 (mock/new-observed 0.1))
  (var o2 (mock/new-observed 0.2))
  (var
   render-fn
   (fn
    [e]
    (var #{a b} e)
    (var #{c} b)
    (return {:style {:opacity (+ a c)}})))
  (base-animate/listen-map
   mock/MOCK
   ref
   {:a o1, :b {:c o2}}
   render-fn))
 =>
 {"style" {"opacity" 0.30000000000000004}})

^{:refer xt.lang.event-animate/listen-transformations, :added "4.0"}
(fact
 "converts to the necessary listeners"
 ^{:hidden true}
 (!.py
  (base-animate/listen-transformations
   mock/MOCK
   {:current {}}
   nil
   {}
   (fn:> {})))
 =>
 {})

^{:refer xt.lang.event-animate/new-progressing, :added "4.0"}
(fact
 "creates a new progressing element"
 ^{:hidden true}
 (!.py (base-animate/new-progressing))
 =>
 {"running" false, "queued" [], "animation" nil})

^{:refer xt.lang.event-animate/make-binary-transitions, :added "4.0"}
(fact
 "makes a binary transition"
 ^{:hidden true}
 (!.py
  (var t (base-animate/make-binary-transitions mock/MOCK false {}))
  (var #{one-fn zero-fn indicator} t)
  [(mock/get-value indicator)
   (one-fn nil)
   (mock/get-value indicator)
   (zero-fn nil)
   (mock/get-value indicator)])
 =>
 [0 nil 1 nil 0])

^{:refer xt.lang.event-animate/make-binary-indicator, :added "4.0"}
(fact
 "makes a binary indicator"
 ^{:hidden true}
 (!.py
  (var progress-fn (fn [_] (return nil)))
  (var
   t
   (base-animate/make-binary-indicator
    mock/MOCK
    false
    {}
    "cancel"
    (base-animate/new-progressing)
    progress-fn))
  (var #{trigger-fn indicator} t)
  [(mock/get-value indicator)
   (trigger-fn true)
   (mock/get-value indicator)
   (trigger-fn false)
   (mock/get-value indicator)])
 =>
 [0
  {"running" false, "queued" [], "animation" nil}
  1
  {"running" false, "queued" [], "animation" nil}
  0])

^{:refer xt.lang.event-animate/make-linear-indicator, :added "4.0"}
(fact
 "makes a linear indicator"
 ^{:hidden true}
 (!.py
  (var prev {:current 1})
  (var set-prev (fn [v] (:= (. prev ["current"]) v)))
  (var progress-fn (fn [_] (return nil)))
  (var
   t
   (base-animate/make-linear-indicator
    mock/MOCK
    1
    (fn:> (. prev ["current"]))
    set-prev
    {}
    "cancel"
    (base-animate/new-progressing)
    progress-fn))
  (var #{trigger-fn indicator} t)
  [(mock/get-value indicator)
   (trigger-fn 3)
   (mock/get-value indicator)
   (trigger-fn 8)
   (mock/get-value indicator)])
 =>
 [1
  {"running" false, "queued" [], "animation" nil}
  3
  {"running" false, "queued" [], "animation" nil}
  8])

^{:refer xt.lang.event-animate/make-circular-indicator, :added "4.0"}
(fact
 "makes a circular indicator"
 ^{:hidden true}
 (!.py
  (var prev {:current 1})
  (var set-prev (fn [v] (:= (. prev ["current"]) v)))
  (var progress-fn (fn [_] (return nil)))
  (var
   t
   (base-animate/make-circular-indicator
    mock/MOCK
    1
    (fn:> (. prev ["current"]))
    set-prev
    {}
    "cancel"
    10
    (base-animate/new-progressing)
    progress-fn))
  (var #{trigger-fn indicator} t)
  [(mock/get-value indicator)
   (trigger-fn -9)
   (mock/get-value indicator)
   (trigger-fn 7)
   (mock/get-value indicator)])
 =>
 [1
  {"running" false, "queued" [], "animation" nil}
  1
  {"running" false, "queued" [], "animation" nil}
  -13])
