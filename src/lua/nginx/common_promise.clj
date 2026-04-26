(ns lua.nginx.common-promise
  (:require [std.lang :as l]))

(l/script :lua.nginx
  {})

(defn.lua with-delay
  "sleeps before invoking a thunk, accepting either (ms thunk) or (thunk ms)"
  {:added "4.1"}
  [a b]
  (var thunk (:? (== "function" (type a)) a b))
  (var ms (:? (== "function" (type a)) b a))
  (return (ngx.thread.spawn
           (fn []
             (ngx.sleep (/ ms 1000))
             (return (thunk))))))
