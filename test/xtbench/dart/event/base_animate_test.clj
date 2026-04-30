(ns xtbench.dart.event.base-animate-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.event.base-animate :as base-animate]
             [xt.event.base-animate-mock :as mock]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.base-animate/new-derived :added "4.1"}
(fact "derives, observes, and maps animation values"

  (!.dt
    (var ref {:current {}})
    (var obs (mock/new-observed 0.5))
    (var get-style (fn:> [e]
                         (. e
                            ["current"]
                            ["props"]
                            ["style"])))
    (base-animate/listen-single
     mock/MOCK
     ref
     obs
     (fn:> [v]
           {:style {:opacity (+ 0.2 v)}}))
    [(mock/get-value
      (base-animate/new-derived
       mock/MOCK
       (fn:> [a b c] (+ a b c))
       [(mock/new-observed 1)
        (mock/new-observed 2)
        (mock/new-observed 3)]))
     (get-style ref)
     (mock/set-value obs 0.4)
     (get-style ref)
     (base-animate/get-map-paths
      mock/MOCK
      {:a (mock/new-observed 1)
       :b {:c (mock/new-observed 2)}})
     (base-animate/get-map-input
      mock/MOCK
      [[[] "a" {"value" 1
                "::" "observed"
                "listeners" []}]
       [[] "b" false]
       [["b"] "c" {"value" 2
                   "::" "observed"
                   "listeners" []}]])])
  => (just-in
      [6
       {"opacity" 0.7}
       nil
       {"opacity" 0.6000000000000001}
       (just [[[] "a" {"value" 1
                       "::" "observed"
                       "listeners" []}]
              [[] "b" false]
              [["b"] "c" {"value" 2
                          "::" "observed"
                          "listeners" []}]]
             :in-any-order)
       {"a" 1
        "b" {"c" 2}}]))

^{:refer xt.event.base-animate/make-binary-transitions :added "4.1"}
(fact "provides animation transition helpers"

  (!.dt
    (var t (base-animate/make-binary-transitions
            mock/MOCK
            false
            {}))
    (var #{indicator
           zero-fn
           one-fn} t)
    [(mock/get-value indicator)
     (one-fn nil)
     (mock/get-value indicator)
     (zero-fn nil)
     (mock/get-value indicator)
     (base-animate/new-progressing)])
  => [0
      nil
      1
      nil
      0
      {"running" false
       "queued" []
       "animation" nil}])

^{:refer xt.event.base-animate/listen-single :added "4.1"}
(fact "listens to a single observed value"

  (!.dt
    (var ref {:current {}})
    (var obs (mock/new-observed 0.5))
    (var get-style (fn:> [e] (. e ["current"] ["props"] ["style"])))
    (base-animate/listen-single
     mock/MOCK
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
      {"opacity" 0.5}])

^{:refer xt.event.base-animate/listen-array :added "4.1"}
(fact "listens to an array of observed values"

  (!.dt
    (var ref {:current {}})
    (var o1 (mock/new-observed 0.1))
    (var o2 (mock/new-observed 0.2))
    (var get-style (fn:> [e] (. e ["current"] ["props"] ["style"])))
    (base-animate/listen-array
     mock/MOCK
     ref
     [o1 o2]
     (fn [a b]
       (return {:style {:opacity (+ 0.1 a b)}})))
    [(get-style ref)
     (mock/set-value o1 0.4)
     (get-style ref)
     (mock/set-value o2 0.3)
     (get-style ref)])
  => [{"opacity" 0.4}
      nil
      {"opacity" 0.7}
      nil
      {"opacity" 0.8}])

^{:refer xt.event.base-animate/get-map-paths-inner :added "4.1"}
(fact "collects animated map paths recursively"

  (!.dt
    (base-animate/get-map-paths-inner
     mock/MOCK
     {:a (mock/new-observed 1)
      :b {:c (mock/new-observed 2)}}
     []
     []))
  => [[[] "a" {"::" "observed" "listeners" [] "value" 1}]
      [[] "b" false]
      [["b"] "c" {"::" "observed" "listeners" [] "value" 2}]])

^{:refer xt.event.base-animate/get-map-paths :added "4.1"}
(fact "collects animated map paths"

  (!.dt
    (base-animate/get-map-paths
     mock/MOCK
     {:a (mock/new-observed 1)
      :b {:c (mock/new-observed 2)}}))
  => [[[] "a" {"::" "observed" "listeners" [] "value" 1}]
      [[] "b" false]
      [["b"] "c" {"::" "observed" "listeners" [] "value" 2}]])

^{:refer xt.event.base-animate/get-map-input :added "4.1"}
(fact "converts collected paths back into nested input"

  (!.dt
    (base-animate/get-map-input
     mock/MOCK
     [[[] "a" {"::" "observed" "listeners" [] "value" 1}]
      [[] "b" false]
      [["b"] "c" {"::" "observed" "listeners" [] "value" 2}]]))
  => {"a" 1
      "b" {"c" 2}})

^{:refer xt.event.base-animate/listen-map :added "4.1"}
(fact "listens to a nested map of indicators"

  (!.dt
    (var ref {:current {}})
    (var o1 (mock/new-observed 0.1))
    (var o2 (mock/new-observed 0.2))
    (base-animate/listen-map
     mock/MOCK
     ref
     {:a o1
      :b {:c o2}}
     (fn [m]
       (var #{a b} m)
       (var #{c} b)
       (return {:style {:opacity (+ a c)}})))
    (. ref ["current"] ["props"]))
  => {"style" {"opacity" 0.30000000000000004}})

^{:refer xt.event.base-animate/listen-transformations :added "4.1"}
(fact "builds listeners from transformation trees"

  (!.dt
    (var ref {:current {}})
    (var obs (mock/new-observed 0.2))
    [(base-animate/listen-transformations mock/MOCK {:current {}} nil {} (fn:> {}))
     (base-animate/listen-transformations
      mock/MOCK
      ref
      obs
      (fn [v chord]
        (return {:style {:opacity (+ v 0.2)}}))
      (fn:> {}))
     (. ref ["current"] ["props"])])
  => [{} {"style" {"opacity" 0.4}} {"style" {"opacity" 0.4}}])

^{:refer xt.event.base-animate/new-progressing :added "4.1"}
(fact "creates a fresh progressing record"

  (!.dt
    (base-animate/new-progressing))
  => {"animation" nil
      "queued" []
      "running" false})

^{:refer xt.event.base-animate/run-with-cancel :added "4.1"}
(fact "cancels the active transition before starting a new one"

  (!.dt
    (var stopped [])
    (var progress [])
    (var impl {:stop-transition (fn [anim]
                                  (xt/x:arr-push stopped anim))})
    (var progressing {"running" true
                      "queued" []
                      "animation" "prev"})
    (base-animate/run-with-cancel
     impl
     (fn [finish]
       (return "next"))
     progressing
     (fn [e]
       (xt/x:arr-push progress (. e ["status"]))))
    [progressing
     stopped
     progress])
  => (just-in
      [(contains-in {"animation" "next"
                     "queued" []
                     "running" false})
       ["prev"]
       ["running"]]))

^{:refer xt.event.base-animate/animate-chained-cleanup :added "4.1"}
(fact "resets chained progress state"

  (!.dt
    (var progress [])
    (var progressing {"running" true
                      "queued" [(fn:> nil)]
                      "animation" "prev"})
    (base-animate/animate-chained-cleanup
     mock/MOCK
     progressing
     (fn [e]
       (xt/x:arr-push progress (. e ["status"]))))
    [progressing
     progress])
  => (just-in
      [(contains-in {"animation" nil
                     "queued" []
                     "running" false})
       ["cleanup"]]))

^{:refer xt.event.base-animate/animate-chained-one :added "4.1"}
(fact "starts the next queued chained-one animation"

  (!.dt
    (var progress [])
    (var progressing {"running" false
                      "queued" [(fn [cb] (return "anim1"))]
                      "animation" nil})
    (base-animate/animate-chained-one
     mock/MOCK
     progressing
     (fn [e]
       (xt/x:arr-push progress (. e ["status"]))))
    [(. progressing ["running"])
     (. progressing ["animation"])
     (xt/x:len (. progressing ["queued"]))
     progress])
  => [true "anim1" 1 ["running"]])

^{:refer xt.event.base-animate/animate-chained-all :added "4.1"}
(fact "consumes queued chained-all animations one at a time"

  (!.dt
    (var progress [])
    (var progressing {"running" false
                      "queued" [(fn [cb] (return "anim1"))
                                (fn [cb] (return "anim2"))]
                      "animation" nil})
    (base-animate/animate-chained-all
     mock/MOCK
     progressing
     (fn [e]
       (xt/x:arr-push progress (. e ["status"]))))
    [(. progressing ["running"])
     (. progressing ["animation"])
     (xt/x:len (. progressing ["queued"]))
     progress])
  => [true "anim1" 1 ["running"]])

^{:refer xt.event.base-animate/run-with-chained :added "4.1"}
(fact "queues chained-one animations while one is already running"

  (!.dt
    (var progressing (base-animate/new-progressing))
    (base-animate/run-with-chained
     mock/MOCK
     "chained-one"
     (fn [cb] (return "anim1"))
     progressing
     nil)
    (base-animate/run-with-chained
     mock/MOCK
     "chained-one"
     (fn [cb] (return "anim2"))
     progressing
     nil)
    [(. progressing ["running"])
     (. progressing ["animation"])
     (xt/x:len (. progressing ["queued"]))])
  => [true "anim1" 1])

^{:refer xt.event.base-animate/run-with :added "4.1"}
(fact "dispatches to the requested run strategy"

  (!.dt
    (var progressing (base-animate/new-progressing))
    (base-animate/run-with
     mock/MOCK
     "cancel"
     (fn [finish] (return "anim"))
     progressing
     nil)
    progressing)
  => (contains-in {"animation" "anim"
                   "queued" []
                   "running" false}))

^{:refer xt.event.base-animate/make-binary-indicator :added "4.1"}
(fact "creates a binary indicator trigger"

  (!.dt
    (var t (base-animate/make-binary-indicator
            mock/MOCK
            false
            {}
            "cancel"
            (base-animate/new-progressing)
            (fn [_] (return nil))))
    (var #{indicator trigger-fn} t)
    [(mock/get-value indicator)
     (trigger-fn true)
     (mock/get-value indicator)
     (trigger-fn false)
     (mock/get-value indicator)])
  => (just-in
      [0
       (contains-in {"queued" []
                     "running" false})
       1
       (contains-in {"queued" []
                     "running" false})
       0]))

^{:refer xt.event.base-animate/make-linear-indicator-inner :added "4.1"}
(fact "supports conditional linear indicator updates"

  (!.dt
    (var prev {"current" 1})
    (var set-prev (fn [v] (:= (. prev ["current"]) v)))
    (var t (base-animate/make-linear-indicator-inner
            mock/MOCK
            1
            (fn:> (. prev ["current"]))
            set-prev
            {}
            "cancel"
            (base-animate/new-progressing)
            nil
            (fn:> true)))
    (var #{indicator trigger-fn} t)
    [(mock/get-value indicator)
     (trigger-fn 3)
     (mock/get-value indicator)
     (trigger-fn 8)
     (mock/get-value indicator)
     (. prev ["current"])])
  => (just-in
      [1
       (contains-in {"queued" []
                     "running" false})
       3
       (contains-in {"queued" []
                     "running" false})
       8
       8]))

^{:refer xt.event.base-animate/make-linear-indicator :added "4.1"}
(fact "creates a linear indicator"

  (!.dt
    (var prev {"current" 1})
    (var set-prev (fn [v] (:= (. prev ["current"]) v)))
    (var t (base-animate/make-linear-indicator
            mock/MOCK
            1
            (fn:> (. prev ["current"]))
            set-prev
            {}
            "cancel"
            (base-animate/new-progressing)
            (fn [_] (return nil))))
    (var #{indicator trigger-fn} t)
    [(mock/get-value indicator)
     (trigger-fn 3)
     (mock/get-value indicator)
     (trigger-fn 8)
     (mock/get-value indicator)])
  => (just-in
      [1
       (contains-in {"queued" []
                     "running" false})
       3
       (contains-in {"queued" []
                     "running" false})
       8]))

^{:refer xt.event.base-animate/make-circular-indicator-inner :added "4.1"}
(fact "supports conditional circular indicator updates"

  (!.dt
    (var prev {"current" 1})
    (var set-prev (fn [v] (:= (. prev ["current"]) v)))
    (var t (base-animate/make-circular-indicator-inner
            mock/MOCK
            1
            (fn:> (. prev ["current"]))
            set-prev
            {}
            "cancel"
            10
            (base-animate/new-progressing)
            nil
            (fn:> true)))
    (var #{indicator trigger-fn} t)
    [(mock/get-value indicator)
     (trigger-fn 3)
     (mock/get-value indicator)
     (trigger-fn 4)
     (mock/get-value indicator)
     (. prev ["current"])])
  => (just-in
      [1
       (contains-in {"queued" []
                     "running" false})
       3
       (contains-in {"queued" []
                     "running" false})
       4
       4]))

^{:refer xt.event.base-animate/make-circular-indicator :added "4.1"}
(fact "creates a circular indicator using modulo offsets"

  (!.dt
    (var prev {"current" 1})
    (var set-prev (fn [v] (:= (. prev ["current"]) v)))
    (var t (base-animate/make-circular-indicator
            mock/MOCK
            1
            (fn:> (. prev ["current"]))
            set-prev
            {}
            "cancel"
            10
            (base-animate/new-progressing)
            (fn [_] (return nil))))
    (var #{indicator trigger-fn} t)
    [(mock/get-value indicator)
     (trigger-fn -9)
     (mock/get-value indicator)
     (trigger-fn 7)
     (mock/get-value indicator)])
  => (just-in
      [1
       (contains-in {"queued" []
                     "running" false})
       1
       (contains-in {"queued" []
                     "running" false})
       -13]))

(comment
  (s/snapto '[xt.event.base-animate])
  
  (s/seedgen-benchadd '[xt.event.base-animate] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.base-animate]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.base-animate]  {:lang [:lua :python] :write true}))
