(ns python.core.common-promise-test
  (:require [python.core.common-promise]
            [std.lang :as l])
  (:use code.test))

^{:refer python.core.common-promise/async-run :added "4.1"}
(fact "python common promise helpers emit low-level async operations"
  (let [out (l/emit-as :python ['(do (python.core.common-promise/async-run thunk)
                                     (python.core.common-promise/async-bind handle on_value on_error)
                                     (python.core.common-promise/with-delay ms thunk))])]
    [(boolean (re-find #"python\.core\.common_promise\.async_run\(thunk\)" out))
     (boolean (re-find #"python\.core\.common_promise\.async_bind\(handle,\s*on_value,\s*on_error\)" out))
     (boolean (re-find #"python\.core\.common_promise\.with_delay\(ms,\s*thunk\)" out))])
  => [true true true])
