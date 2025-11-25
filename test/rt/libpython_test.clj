(ns rt.libpython-test
  (:use code.test)
  (:require [rt.libpython :as lp]
            [std.lib :as h]
            [std.lang :as l]
            [std.concurrent :as cc]
            [libpython-clj2.python :as python]))

(l/script- :python
  {:runtime :libpython
   :require [[xt.lang.base-lib :as k]]})

(defn.py add10
  [x]
  (return (+ x 10)))

^{:refer rt.libpython/TESTING :added "3.0"}
(fact "performs an exec expression"
  ;; Skips actual python exec
  )

^{:refer rt.libpython/eval-raw :added "3.0"}
(fact "performs an exec expression"
  (with-redefs [python/initialize! (fn [] nil)
                python/run-simple-string (fn [s] {:globals {"OUT" 2}})]
    (get-in (lp/eval-raw nil "OUT = 1 + 1")
            [:globals "OUT"]))
  => 2)

^{:refer rt.libpython/eval-libpython :added "4.0"}
(fact "evals body in the runtime"
  (with-redefs [python/initialize! (fn [] nil)
                python/run-simple-string (fn [s] {:globals {"OUT" 2}})]
    (lp/eval-libpython nil
                       "OUT = 1 + 1"))
  => 2)

^{:refer rt.libpython/invoke-libpython :added "4.0"}
(fact "invokes a pointer in the runtime"
  ;; delegates
  )

^{:refer rt.libpython/start-libpython :added "3.0"}
(fact "starts the libpython runtime"
  (with-redefs [python/initialize! (fn [] nil)]
    (lp/start-libpython {}))
  => {})

^{:refer rt.libpython/stop-libpython :added "3.0"}
(fact "stops the libpython runtime"
  (lp/stop-libpython {}) => {})

^{:refer rt.libpython/rt-libpython:create :added "4.0"}
(fact "creates a libpython runtime"
  (lp/rt-libpython:create {:lang :js})
  => map?)

^{:refer rt.libpython/rt-libpython :added "4.0"}
(fact "creates a libpython rt"
  (with-redefs [lp/start-libpython (fn [rt] (assoc rt :started true))]
    (lp/rt-libpython {}))
  => (contains {:started true}))

^{:refer rt.libpython/rt-libpython? :added "4.0"}
(fact "checks object is a libpython rt"
  (lp/rt-libpython? (lp/rt-libpython:create {}))
  => true)
