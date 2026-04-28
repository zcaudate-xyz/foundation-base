(ns lua.core.common-promise-test
  (:require [lua.core.common-promise]
            [std.lang :as l])
  (:use code.test))

^{:refer lua.core.common-promise/promise-native? :added "4.1"}
(fact "TODO")

^{:refer lua.core.common-promise/promise-resolve :added "4.1"}
(fact "TODO")

^{:refer lua.core.common-promise/promise-reject :added "4.1"}
(fact "TODO")

^{:refer lua.core.common-promise/promise :added "4.1"}
(fact "TODO")

^{:refer lua.core.common-promise/promise-then :added "4.1"}
(fact "TODO")

^{:refer lua.core.common-promise/promise-catch :added "4.1"}
(fact "TODO")

^{:refer lua.core.common-promise/promise-finally :added "4.1"}
(fact "TODO")

^{:refer lua.core.common-promise/with-delay :added "4.1"}
(fact "lua promise helpers compose delayed work through promise chains"
  (let [out (l/emit-as :lua ['(lua.core.common-promise/promise-then
                              (lua.core.common-promise/with-delay ms thunk)
                              on_value)])]
    [(boolean (re-find #"lua\.core\.common_promise\.promise_then" out))
     (boolean (re-find #"lua\.core\.common_promise\.with_delay\(ms,\s*thunk\)" out))])
  => [true true])