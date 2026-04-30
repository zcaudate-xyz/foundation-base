(ns lua.core.common-promise-test
  (:require [lua.core.common-promise]
            [std.lang :as l])
  (:use code.test))

^{:refer lua.core.common-promise/async-run :added "4.1"}
(fact "lua common promise helpers emit low-level async operations"
  (let [out (l/emit-as :lua ['(do (lua.core.common-promise/async-run thunk)
                                  (lua.core.common-promise/async-bind handle on_value on_error)
                                  (lua.core.common-promise/with-delay ms thunk))])]
    [(boolean (re-find #"lua\.core\.common_promise\.async_run\(thunk\)" out))
     (boolean (re-find #"lua\.core\.common_promise\.async_bind\(handle,\s*on_value,\s*on_error\)" out))
     (boolean (re-find #"lua\.core\.common_promise\.with_delay\(ms,\s*thunk\)" out))])
  => [true true true])
