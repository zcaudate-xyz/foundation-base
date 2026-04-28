(ns python.core.common-promise-test
  (:require [python.core.common-promise]
            [std.lang :as l])
  (:use code.test))

^{:refer python.core.common-promise/promise-wrapper? :added "4.1"}
(fact "TODO")

^{:refer python.core.common-promise/promise-native? :added "4.1"}
(fact "TODO")

^{:refer python.core.common-promise/promise-reject :added "4.1"}
(fact "TODO")

^{:refer python.core.common-promise/promise-awaitable :added "4.1"}
(fact "TODO")

^{:refer python.core.common-promise/promise-wrap :added "4.1"}
(fact "TODO")

^{:refer python.core.common-promise/promise :added "4.1"}
(fact "TODO")

^{:refer python.core.common-promise/promise-then :added "4.1"}
(fact "TODO")

^{:refer python.core.common-promise/promise-catch :added "4.1"}
(fact "TODO")

^{:refer python.core.common-promise/promise-finally :added "4.1"}
(fact "TODO")

^{:refer python.core.common-promise/with-delay :added "4.1"}
(fact "python promise helpers compose delayed work through promise chains"
  (let [out (l/emit-as :python ['(python.core.common-promise/promise-then
                                 (python.core.common-promise/with-delay ms thunk)
                                 on_value)])]
    [(boolean (re-find #"python\.core\.common_promise\.promise_then" out))
     (boolean (re-find #"python\.core\.common_promise\.with_delay\(ms,\s*thunk\)" out))])
  => [true true])