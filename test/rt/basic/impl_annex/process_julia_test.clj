(ns rt.basic.impl-annex.process-julia-test
  (:require [rt.basic.impl-annex.process-julia :refer :all])
  (:use code.test))

^{:refer rt.basic.impl-annex.process-julia/default-body-wrap :added "4.1"}
(fact "creates julia return wrapper"
  (default-body-wrap '[1 2 3])
  => '(do
        (defn OUT-FN []
          1
          2
          (return 3))
        (:= OUT (OUT-FN))))

^{:refer rt.basic.impl-annex.process-julia/default-body-transform :added "4.1"}
(fact "standard julia transforms"
  (default-body-transform '[1 2 3] {})
  => '(do
        (defn OUT-FN []
          (return [1 2 3]))
        (:= OUT (OUT-FN)))

  (default-body-transform '[1 2 3] {:bulk true})
  => '(do
        (defn OUT-FN []
          1
          2
          (return 3))
        (:= OUT (OUT-FN))))
