(ns rt.basic.impl.process-lua-test
  (:require [rt.basic.impl.process-lua :refer :all]
            [std.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :oneshot})

^{:refer rt.basic.impl.process-lua/CANARY :adopt true  :added "4.0"}
(fact "EVALUATE lua code"
  
  (!.lua (+ 1 2 3 4))
  => 10)

^{:refer rt.basic.impl.process-lua/default-oneshot-wrap :adopt true :added "4.0"}
(fact "wraps with the eval wrapper"

  (default-oneshot-wrap "1")
  => string?)

^{:refer rt.basic.impl.process-lua/default-body-transform :added "4.0"}
(fact "transform code for return"

  (default-body-transform [1 2 3] {})
  => '(do
        (defn OUT-FN [] (return [1 2 3]))
        (return (OUT-FN)))

  (default-body-transform [1 2 3] {:bulk true})
  => '(do
        (defn OUT-FN [] 1 2 (return 3))
        (return (OUT-FN)))

  (l/emit-as :lua [(default-body-transform '(do (defn add-10 [x] (return (+ x 10)))
                                                (add-10 5))
                                          {})])
  => #"local function add_10\(x\)")

^{:refer rt.basic.impl.process-lua/default-basic-client :adopt true :added "4.0"}
(fact "wraps with the eval wrapper"

  (default-basic-client 19000)
  => string?)


^{:refer rt.basic.impl.process-lua/default-body-wrap :added "4.1"}
(fact "wraps forms in a local helper"
  (default-body-wrap '[(defn add-10 [x] (return (+ x 10)))
                       (add-10 5)])
  => '(do
        (defn OUT-FN []
          (defn add-10 [x] (return (+ x 10)))
          (return (add-10 5)))
        (return (OUT-FN))))

^{:refer rt.basic.impl.process-lua/normalize-forms :added "4.1"}
(fact "normalizes a top-level do body"
  (normalize-forms '(do (defn add-10 [x] (return (+ x 10)))
                        (add-10 5))
                   {})
  => '((defn add-10 [x] (return (+ x 10)))
       (add-10 5))

  (normalize-forms '[1 2 3] {:bulk true})
  => '[1 2 3])

^{:refer rt.basic.impl.process-lua/mark-inline-defs :added "4.1"}
(fact "marks inline defs as inner"
  (-> (mark-inline-defs '((defn add-10 [x] (return (+ x 10)))
                          (add-10 5)))
      first
      second
      meta
      :inner)
  => true)
