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
  => '(do (return [1 2 3]))

  (default-body-transform [1 2 3] {:bulk true})
  => '(do 1 2 (return 3)))

^{:refer rt.basic.impl.process-lua/default-basic-client :adopt true :added "4.0"}
(fact "wraps with the eval wrapper"

  (default-basic-client 19000)
  => string?)
