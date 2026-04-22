(ns
 xtbench.r.lang.util-validate-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-validate/validate-step, :added "4.0"}
(fact
 "validates a single step"
 ^{:hidden true}
 (notify/wait-on
  :r
  (var data {:first "hello"})
  (var
   guards
   [["is-not-empty"
     {:message "Must not be empty",
      :check (fn:> [v rec] (and (k/not-nil? v) (< 0 (xt/x:len v))))}]])
  (var result {:fields {:first {:status "pending"}}})
  (validate/validate-step
   data
   "first"
   guards
   0
   result
   nil
   (fn [success result] (repl/notify result))))
 =>
 {"fields" {"first" {"status" "ok"}}})
