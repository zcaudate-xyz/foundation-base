(ns lua.nginx.common-promise-test
  (:require [std.lib.env :as env]
            [hara.lang :as l])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :nginx.instance
   :test-mode true
   :require [[lua.nginx :as n]
             [lua.nginx.common-promise :as p]]})

(fact:global
 {:skip     (not (env/program-exists? "nginx"))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.common-promise/promise-wrapper? :added "4.1"}
(fact "checks whether the value is a native runtime promise wrapper"

  (!.lua (p/promise-wrapper? (p/promise-resolve 1)))
  => true

  (!.lua (p/promise-wrapper? 1))
  => false)

^{:refer lua.nginx.common-promise/promise-native? :added "4.1"}
(fact "checks whether the value is a native nginx promise wrapper"

  (!.lua (p/promise-native? (p/promise-resolve 1)))
  => true)

^{:refer lua.nginx.common-promise/promise-resolve :added "4.1"}
(fact "wraps plain values as resolved promises"

  (!.lua (var r (p/promise-resolve 42))
         [(. r ["status"]) (. r ["value"])])
  => ["resolved" 42])

^{:refer lua.nginx.common-promise/promise-reject :added "4.1"}
(fact "wraps thrown values as rejected promises"

  (!.lua (var r (p/promise-reject "err"))
         [(. r ["status"]) (. r ["error"])])
  => ["rejected" "err"])

^{:refer lua.nginx.common-promise/promise-pending :added "4.1"}
(fact "creates a pending promise backed by an nginx light thread"

  (!.lua (var r (p/promise-pending nil))
         (. r ["status"]))
  => "pending")

^{:refer lua.nginx.common-promise/promise-settle :added "4.1"}
(fact "updates a pending promise with its settled status"

  (!.lua (var r (p/promise-pending nil))
         (p/promise-settle r "resolved" 99)
         [(. r ["status"]) (. r ["value"])])
  => ["resolved" 99])

^{:refer lua.nginx.common-promise/promise-await :added "4.1"}
(fact "waits for pending nginx promises and adopts nested promise results"

  (!.lua (var r (p/promise (fn [] (return 42))))
         (var settled (p/promise-await r))
         [(. settled ["status"]) (. settled ["value"])])
  => ["resolved" 42])

^{:refer lua.nginx.common-promise/async-run :added "4.1"}
(fact "executes a thunk in an nginx light thread and returns a pending promise"

  (let [out (l/emit-as :lua.nginx ['(lua.nginx.common-promise/async-run thunk)])]
    (boolean (re-find #"lua\.nginx\.common_promise\.async_run\(thunk\)" out)))
  => true)

^{:refer lua.nginx.common-promise/async-bind :added "4.1"}
(fact "binds success and error continuations onto a promise-like value"

  (let [out (l/emit-as :lua.nginx ['(lua.nginx.common-promise/async-bind promise on_value on_error)])]
    (boolean (re-find #"lua\.nginx\.common_promise\.async_bind\(promise,\s*on_value,\s*on_error\)" out)))
  => true)

^{:refer lua.nginx.common-promise/promise :added "4.1"}
(fact "executes a thunk in an nginx light thread and returns a pending promise"

  (!.lua (var r (p/promise (fn [] (return 42))))
         (var settled (p/promise-await r))
         [(. settled ["status"]) (. settled ["value"])])
  => ["resolved" 42])

^{:refer lua.nginx.common-promise/promise-all :added "4.1"}
(fact "waits for all values in an array and preserves nginx async chaining"

  (!.lua (var r (p/promise-all [(p/promise-resolve 1)
                                (p/promise-resolve 2)
                                (p/promise-resolve 3)]))
         (var settled (p/promise-await r))
         (. settled ["value"]))
  => [1 2 3])

^{:refer lua.nginx.common-promise/promise-then :added "4.1"}
(fact "applies a continuation to resolved promises while preserving async chaining"

  (!.lua (var r (p/promise-then (p/promise-resolve 2)
                                (fn [v] (return (* v 3)))))
         (var settled (p/promise-await r))
         [(. settled ["status"]) (. settled ["value"])])
  => ["resolved" 6])

^{:refer lua.nginx.common-promise/promise-catch :added "4.1"}
(fact "applies a continuation to rejected promises while preserving async chaining"

  (!.lua (var r (p/promise-catch (p/promise-reject "bad")
                                 (fn [e] (return (cat "caught:" e)))))
         (var settled (p/promise-await r))
         [(. settled ["status"]) (. settled ["value"])])
  => ["resolved" "caught:bad"])

^{:refer lua.nginx.common-promise/promise-finally :added "4.1"}
(fact "runs a finalizer and preserves the original promise unless cleanup rejects"

  (!.lua (var flag false)
         (var r (p/promise-finally (p/promise-resolve 42)
                                   (fn [] (:= flag true)
                                     (return nil))))
         (var settled (p/promise-await r))
         [flag (. settled ["status"]) (. settled ["value"])])
  => [true "resolved" 42])

^{:refer lua.nginx.common-promise/with-delay :added "4.1"}
(fact "sleeps before invoking a thunk, returning a promise with ms first"

  (let [out (l/emit-as :lua.nginx ['(lua.nginx.common-promise/with-delay ms thunk)])]
    (boolean (re-find #"lua\.nginx\.common_promise\.with_delay\(ms,\s*thunk\)" out)))
  => true)
