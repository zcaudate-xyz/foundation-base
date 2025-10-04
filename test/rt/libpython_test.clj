(ns rt.libpython-test
  (:use code.test)
  (:require [rt.libpython :as lp]
            [std.lib :as h]
            [std.lang :as l]
            [std.concurrent :as cc]))

(l/script- :python
  {:runtime :libpython
   :require [[xt.lang.base-lib :as k]]})

(defn.py add10
  [x]
  (return (+ x 10)))

^{:refer rt.libpython/TESTING :added "3.0"}
(fact "performs an exec expression"
  ^:hidden
  
  (add10 1)
  => 11

  (type (!.py
          (var b 2)
          -/add10))
  => :pyobject
  
  ((!.py
     (var b 2)
     -/add10)
   1)
  => 11)

^{:refer rt.libpython/eval-raw :added "3.0"}
(fact "performs an exec expression"
  ^:hidden
  
  (get-in (lp/eval-raw nil "OUT = 1 + 1")
          [:globals "OUT"])
  => 2)

^{:refer rt.libpython/eval-libpython :added "4.0"}
(fact "evals body in the runtime"
  
  (lp/eval-libpython nil
                     "OUT = 1 + 1")
  => 2)

^{:refer rt.libpython/invoke-libpython :added "4.0"}
(fact "invokes a pointer in the runtime"

  (lp/invoke-libpython (l/rt :python)
                       k/sub
                       [1 2])
  => -1)

^{:refer rt.libpython/start-libpython :added "3.0"}
(fact "starts the libpython runtime")

^{:refer rt.libpython/stop-libpython :added "3.0"}
(fact "stops the libpython runtime")

^{:refer rt.libpython/rt-libpython:create :added "4.0"}
(fact "creates a libpython runtime"

  (h/-> (lp/rt-libpython:create {:lang :js})
        (h/start)
        (h/stop))
  => lp/rt-libpython?)

^{:refer rt.libpython/rt-libpython :added "4.0"}
(fact "creates a libpython rt")

^{:refer rt.libpython/rt-libpython? :added "4.0"}
(fact "checks object is a libpython rt")
