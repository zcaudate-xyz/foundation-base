(ns xtbench.ruby.lang.spec-promise-test
  (:require [clojure.java.shell :as shell]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-base :as xt])
  (:use code.test))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.lang.spec-promise :as spec-promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(defn assert-ruby-syntax!
  [form]
  (let [tmp  (java.io.File/createTempFile "ruby-promise-" ".rb")
        code (l/emit-as :ruby [form])]
    (try
      (spit tmp code)
      (let [result (shell/sh "ruby" "-c" (.getAbsolutePath tmp))]
        (when-not (zero? (:exit result))
          (throw (ex-info "Invalid emitted Ruby"
                          {:form form
                           :code code
                           :result result}))))
      code
      (finally
        (.delete tmp)))))

^{:refer xt.lang.spec-promise/x:promise-then :added "4.1"}
(fact "chains a resolved js promise"

  (notify/wait-on :ruby
    (spec-promise/x:promise-then
     (spec-promise/x:promise
      (fn []
        (return 5)))
     (fn [value]
       (repl/notify (+ value 2)))))
  => 7)

^{:refer xt.lang.spec-promise/x:promise-catch :added "4.1"}
(fact "preserves xtalk exception data through promise rejection"

  (notify/wait-on :ruby
    (spec-promise/x:promise-catch
     (spec-promise/x:promise
      (fn []
        (throw (xt/x:ex-new "boom" {:a 1}))))
     (fn [err]
       (xt/x:print (xt/x:ex-data err))
       (repl/notify [(xt/x:ex-native? err)
                     (xt/x:get-key (xt/x:ex-data err) "a")]))))
  => [true 1])

^{:refer xt.lang.spec-promise/x:promise-catch :added "4.1"}
(fact "preserves xtalk exception data through promise rejection"

  (notify/wait-on :ruby
    (spec-promise/x:promise-catch
     (spec-promise/x:promise
      (fn []
        (throw (xt/x:ex-new "boom" {:a 1}))))
     (fn [err]
       (xt/x:print (xt/x:ex-data err))
       (repl/notify [(xt/x:ex-native? err)
                     (xt/x:get-key (xt/x:ex-data err) "a")]))))
  => [true 1])

^{:refer xt.lang.spec-promise/x:promise-finally :added "4.1"}
(fact "runs cleanup without changing the resolved value"

  (notify/wait-on :ruby
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
  => [["then" "finally"] 7])

^{:refer xt.lang.spec-promise/x:promise-native? :added "4.1"}
(fact "detects native js promises"

  (!.rb
    (var p
         (spec-promise/x:promise
          (fn []
            (return 1))))
    [(spec-promise/x:promise-native? p)
     (spec-promise/x:promise-native? 1)])
  => [true false])

^{:refer xt.lang.spec-promise/x:with-delay :added "4.1"}
(fact "delays asynchronous js computations"

  (boolean
   (assert-ruby-syntax!
    (spec-promise/x:with-delay 100
                               '(fn []
                                  (return "OK")))))
  => true

  (notify/wait-on :ruby
    (spec-promise/x:with-delay 100
                               (fn []
                                 (repl/notify "OK"))))
  => "OK")

(comment

  (s/seedgen-benchadd '[xt.lang.spec-promise] {:lang [:dart] :write true})
  (s/seedgen-langadd 'xt.lang.common-promise {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-promise {:lang [:lua :python] :write true}))
