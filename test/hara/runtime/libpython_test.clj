(ns hara.runtime.libpython-test
  (:require [libpython-clj2.python :as python]
            [hara.runtime.libpython :as lp]
            [std.concurrent :as cc]
            [hara.lang :as l])
  (:use code.test))

(l/script- :python
  {:runtime :libpython
   :require [[xt.lang.common-lib :as k]]})

(defn.py add10
  [x]
  (return (+ x 10)))

^{:refer hara.runtime.libpython/TESTING :added "3.0"}
(fact "performs an exec expression"
  ;; Skips actual python exec
  )

^{:refer hara.runtime.libpython/eval-raw :added "3.0"}
(fact "performs an exec expression"
  (with-redefs [python/initialize! (fn [] nil)
                python/run-simple-string (fn [s] {:globals {"OUT" 2}})]
    (get-in (lp/eval-raw nil "OUT = 1 + 1")
            [:globals "OUT"]))
  => 2)

^{:refer hara.runtime.libpython/eval-libpython :added "4.0"}
(fact "evals body in the runtime"
  (with-redefs [python/initialize! (fn [] nil)
                python/run-simple-string (fn [s] {:globals {"OUT" 2}})]
    (lp/eval-libpython nil
                       "OUT = 1 + 1"))
  => 2)

^{:refer hara.runtime.libpython/invoke-libpython :added "4.0"}
(fact "invokes a pointer in the runtime"
  ;; delegates
  )

^{:refer hara.runtime.libpython/start-libpython :added "3.0"}
(fact "starts the libpython runtime"
  (with-redefs [python/initialize! (fn [] nil)]
    (lp/start-libpython {}))
  => {})

^{:refer hara.runtime.libpython/stop-libpython :added "3.0"}
(fact "stops the libpython runtime"
  (lp/stop-libpython {}) => {})

^{:refer hara.runtime.libpython/rt-libpython:create :added "4.0"}
(fact "creates a libpython runtime"
  (lp/rt-libpython:create {:lang :js})
  => map?)

^{:refer hara.runtime.libpython/rt-libpython :added "4.0"}
(fact "creates a libpython rt"
  (with-redefs [lp/start-libpython (fn [rt] (assoc rt :started true))]
    (lp/rt-libpython {}))
  => (contains {:started true}))

^{:refer hara.runtime.libpython/rt-libpython? :added "4.0"}
(fact "checks object is a libpython rt"
  (lp/rt-libpython? (lp/rt-libpython:create {}))
  => true)
