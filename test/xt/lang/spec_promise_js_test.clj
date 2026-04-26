(ns xt.lang.spec-promise-js-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-promise/x:promise-then :added "4.1"}
(fact "chains a resolved js promise"

  (notify/wait-on :js
    (spec-promise/x:promise-then
     (spec-promise/x:promise
      (fn []
        (return 5)))
     (fn [value]
       (repl/notify (+ value 2)))))
  => 7)

^{:refer xt.lang.spec-promise/x:promise-catch :added "4.1"}
(fact "recovers a rejected js promise"
  (notify/wait-on :js
    (spec-promise/x:promise-catch
     (spec-promise/x:promise
      (fn []
        (throw "boom")))
     (fn [err]
       (repl/notify err)
       (return err))))
  => "boom")

^{:refer xt.lang.spec-promise/x:promise-finally :added "4.1"}
(fact "runs cleanup without changing the resolved value"

  (notify/wait-on :js
    (var out [])
    (. (spec-promise/x:promise-finally
        (spec-promise/x:promise-then
         (spec-promise/x:promise
          (fn []
            (return 5)))
         (fn [value]
           (. out (push "then"))
           (return (+ value 2))))
        (fn []
          (. out (push "finally"))
          (return "ignored")))
       (then (fn [value]
               (repl/notify [out value])))))
  => [["then" "finally"] 7])

^{:refer xt.lang.spec-promise/x:promise-native? :added "4.1"}
(fact "detects native js promises"

  (!.js
    (var p
         (spec-promise/x:promise
          (fn []
            (return 1))))
    [(spec-promise/x:promise-native? p)
     (spec-promise/x:promise-native? 1)])
  => [true false])
