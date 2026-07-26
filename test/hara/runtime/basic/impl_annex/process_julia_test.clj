tahto/runtime/basic/impl_annex/process_julia_test.clj:1:(ns tahto.runtime.basic.impl-annex.process-julia-test
tahto/runtime/basic/impl_annex/process_julia_test.clj:2:  (:require [tahto.runtime.basic.impl-annex.process-julia :refer :all])
  (:use code.test))

tahto/runtime/basic/impl_annex/process_julia_test.clj:5:^{:refer tahto.runtime.basic.impl-annex.process-julia/default-body-wrap :added "4.1"}
(fact "creates julia return wrapper"
  (default-body-wrap '[1 2 3])
  => '(do
        (defn OUT-FN []
          1
          2
          (return 3))
        (:= OUT (OUT-FN))))

tahto/runtime/basic/impl_annex/process_julia_test.clj:15:^{:refer tahto.runtime.basic.impl-annex.process-julia/default-body-transform :added "4.1"}
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
