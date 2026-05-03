(ns lua.nginx.common-promise-test
  (:require [lua.nginx.common-promise]
            [hara.lang :as l])
  (:use code.test))

^{:refer lua.nginx.common-promise/async-run :added "4.1"}
(fact "nginx common promise helpers emit low-level async operations"
  (let [out (l/emit-as :lua.nginx ['(do (lua.nginx.common-promise/async-run thunk)
                                        (lua.nginx.common-promise/async-bind handle on_value on_error)
                                        (lua.nginx.common-promise/with-delay ms thunk))])]
    [(boolean (re-find #"lua\.nginx\.common_promise\.async_run\(thunk\)" out))
     (boolean (re-find #"lua\.nginx\.common_promise\.async_bind\(handle,\s*on_value,\s*on_error\)" out))
     (boolean (re-find #"lua\.nginx\.common_promise\.with_delay\(ms,\s*thunk\)" out))])
  => [true true true])
