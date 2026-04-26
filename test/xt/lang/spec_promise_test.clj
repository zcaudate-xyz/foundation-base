(ns xt.lang.spec-promise-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-base :as xt])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:python :lua]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-promise :as spec-promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-promise :as spec-promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [python.core.common-promise]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-promise :as spec-promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [lua.core.common-promise]]})

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
  => 7

  (notify/wait-on :python
    (spec-promise/x:promise-then
     (spec-promise/x:promise
      (fn []
        (return 5)))
     (fn [value]
       (repl/notify (+ value 2)))))
  => 7

  (notify/wait-on :lua
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
  => "boom"

  (l/with:print-all
    (notify/wait-on :python
      (spec-promise/x:promise-catch
       (spec-promise/x:promise
        (fn []
          (throw "boom")))
       (fn [err]
         (repl/notify err)
         (return err)))))
  => "boom"

  (l/with:print-all
    (notify/wait-on :lua
      (spec-promise/x:promise-catch
       (spec-promise/x:promise
        (fn []
          (throw "boom")))
       (fn [err]
         (repl/notify err)
         (return err)))))
  => "boom")

^{:refer xt.lang.spec-promise/x:promise-finally :added "4.1"}
(fact "runs cleanup without changing the resolved value"

  (notify/wait-on :js
    (var out [])
    (spec-promise/x:promise-then
     (spec-promise/x:promise-finally
      (spec-promise/x:promise-then
       (spec-promise/x:promise
        (fn []
          (return 5)))
       (fn [value]
         (xt/x:arr-push out "then")
         (return (+ value 2))))
      (fn []
        (xt/x:arr-push out "finally")))
     (fn [value]
       (return (repl/notify [out value])))))
  => [["then" "finally"] 7]
  
  (l/with:print-all
    (notify/wait-on :python
      (var out [])
      (spec-promise/x:promise-then
       (spec-promise/x:promise-finally
        (spec-promise/x:promise-then
         (spec-promise/x:promise
          (fn []
            (return 5)))
         (fn [value]
           (xt/x:arr-push out "then")
           (return (+ value 2))))
        (fn []
          (xt/x:arr-push out "finally")))
       (fn [value]
         (return (repl/notify [out value]))))))
  => [["then" "finally"] 7]

  (l/with:print-all
    (notify/wait-on :lua
      (var out [])
      (spec-promise/x:promise-then
       (spec-promise/x:promise-finally
        (spec-promise/x:promise-then
         (spec-promise/x:promise
          (fn []
            (return 5)))
         (fn [value]
           (xt/x:arr-push out "then")
           (return (+ value 2))))
        (fn []
          (xt/x:arr-push out "finally")))
       (fn [value]
         (return (repl/notify [out value]))))))
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
  => [true false]

  (!.py
    (var p
         (spec-promise/x:promise
          (fn []
            (return 1))))
    [(spec-promise/x:promise-native? p)
     (spec-promise/x:promise-native? 1)])
  => [true false]

  (!.lua
    (var p
         (spec-promise/x:promise
          (fn []
            (return 1))))
    [(spec-promise/x:promise-native? p)
     (spec-promise/x:promise-native? 1)])
  => [true false])

^{:refer xt.lang.spec-promise/x:with-delay :added "4.1"}
(fact "delays asynchronous js computations"

  (notify/wait-on :js
    (spec-promise/x:with-delay 100
                               (fn []
                                 (repl/notify "OK"))))
  => "OK"

  (notify/wait-on :python
    (spec-promise/x:with-delay 100
                               (fn []
                                 (repl/notify "OK"))))
  => "OK"

  (notify/wait-on :lua
    (spec-promise/x:with-delay 100
                               (fn []
                                 (repl/notify "OK"))))
  => "OK")

(comment

  (s/seedgen-benchadd '[xt.lang.spec-promise] {:lang [:dart] :write true})
  (s/seedgen-langadd 'xt.lang.common-promise {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-promise {:lang [:lua :python] :write true}))
