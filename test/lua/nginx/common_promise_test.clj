(ns lua.nginx.common-promise-test
  (:require [lua.nginx.common-promise]
            [std.lang :as l])
  (:use code.test))

^{:refer lua.nginx.common-promise/promise-wrapper? :added "4.1"}
(fact "TODO")

^{:refer lua.nginx.common-promise/promise-native? :added "4.1"}
(fact "TODO")

^{:refer lua.nginx.common-promise/promise-resolve :added "4.1"}
(fact "TODO")

^{:refer lua.nginx.common-promise/promise-reject :added "4.1"}
(fact "TODO")

^{:refer lua.nginx.common-promise/promise-pending :added "4.1"}
(fact "TODO")

^{:refer lua.nginx.common-promise/promise-settle :added "4.1"}
(fact "TODO")

^{:refer lua.nginx.common-promise/promise-await :added "4.1"}
(fact "TODO")

^{:refer lua.nginx.common-promise/promise :added "4.1"}
(fact "TODO")

^{:refer lua.nginx.common-promise/promise-then :added "4.1"}
(fact "TODO")

^{:refer lua.nginx.common-promise/promise-catch :added "4.1"}
(fact "TODO")

^{:refer lua.nginx.common-promise/promise-finally :added "4.1"}
(fact "TODO")

^{:refer lua.nginx.common-promise/with-delay :added "4.1"}
(fact "nginx promise helpers export full promise chaining support"
  (let [out (l/emit-as :lua.nginx ['(do (lua.nginx.common-promise/promise thunk)
                                        (lua.nginx.common-promise/promise-then promise on_value)
                                        (lua.nginx.common-promise/promise-catch promise on_error)
                                        (lua.nginx.common-promise/promise-finally promise on_done)
                                        (lua.nginx.common-promise/promise-native? promise)
                                        (lua.nginx.common-promise/with-delay ms thunk))])]
    [(boolean (re-find #"lua\.nginx\.common_promise\.promise\(thunk\)" out))
     (boolean (re-find #"lua\.nginx\.common_promise\.promise_then\(promise,\s*on_value\)" out))
     (boolean (re-find #"lua\.nginx\.common_promise\.promise_catch\(promise,\s*on_error\)" out))
     (boolean (re-find #"lua\.nginx\.common_promise\.promise_finally\(promise,\s*on_done\)" out))
     (boolean (re-find #"lua\.nginx\.common_promise\.promise_nativep\(promise\)" out))
     (boolean (re-find #"lua\.nginx\.common_promise\.with_delay\(ms,\s*thunk\)" out))])
  => [true true true true true true])