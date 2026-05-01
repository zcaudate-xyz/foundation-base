(ns xt.lang.common-promise-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-base :as xt])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-promise :as common-promise]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.spec-base :as xt]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-promise :as common-promise]
              [xt.lang.common-repl :as repl]
              [xt.lang.spec-promise :as spec-promise]
              [xt.lang.spec-base :as xt]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-promise :as common-promise]
              [xt.lang.common-repl :as repl]
              [xt.lang.spec-promise :as spec-promise]
              [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-promise/promise-native? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-promise/make-resolve-state :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-promise/make-rejected-state :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-promise/make-pending-state :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-promise/internal-settle-action :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-promise/internal-link-action :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-promise/internal-adopt-action :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-promise/internal-drive-action :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-promise/promise :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-promise/promise-run :added "4.1"}
(fact "common promise helpers use the :: xt.promise wrapper"
  (!.js
    (var p (common-promise/promise-run 5))
    [(common-promise/promise-native? p)
     (xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "value")])
  => [true "xt.promise" "resolved" 5]

  (!.py
    (var p (common-promise/promise-run 5))
    [(common-promise/promise-native? p)
     (xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "value")])
  => [true "xt.promise" "resolved" 5]

  (!.lua
    (var p (common-promise/promise-run 5))
    [(common-promise/promise-native? p)
     (xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "value")])
  => [true "xt.promise" "resolved" 5])

^{:refer xt.lang.common-promise/promise-then :added "4.1"}
(fact "common promise helpers resolve delayed async work through the wrapper"

  (notify/wait-on :js
    (common-promise/promise-then
     (common-promise/promise
      (fn []
        (return 5)))
     (repl/>notify)))
  => 5

  (notify/wait-on :python
    (common-promise/promise-then
     (common-promise/promise
      (fn []
        (return 5)))
     (repl/>notify)))
  => 5

  (notify/wait-on :lua
    (common-promise/promise-then
     (common-promise/promise
      (fn []
        (return 5)))
     (repl/>notify)))
  => 5)

^{:refer xt.lang.common-promise/promise-catch :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-promise/promise-all :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-promise/promise-finally :added "4.1"}
(fact "common promise helpers preserve errors and cleanup order"
  
  (notify/wait-on :js
    (var out [])
    (common-promise/promise-catch
     (common-promise/promise-finally
      (common-promise/promise
       (fn []
         (throw (xt/x:ex "boom" {:a 1}))))
      (fn []
        (xt/x:arr-push out "finally")))
     (fn [err]
       (repl/notify [out
                     (xt/x:ex-native? err)
                     (xt/x:get-key (xt/x:ex-data err) "a")]))))
  => [["finally"] true 1]

  
  (notify/wait-on :python
    (var out [])
    (common-promise/promise-catch
     (common-promise/promise-finally
      (common-promise/promise
       (fn []
         (throw (xt/x:ex "boom" {:a 1}))))
      (fn []
        (xt/x:arr-push out "finally")))
     (fn [err]
       (repl/notify [out
                     (xt/x:ex-native? err)
                     (xt/x:get-key (xt/x:ex-data err) "a")]))))
  => [["finally"] true 1]
  
  (notify/wait-on :lua
    (var out [])
    (common-promise/promise-catch
     (common-promise/promise-finally
      (common-promise/promise
       (fn []
         (throw (xt/x:ex "boom" {:a 1}))))
      (fn []
        (xt/x:arr-push out "finally")))
     (fn [err]
       (repl/notify [out
                     (xt/x:ex-native? err)
                     (xt/x:get-key (xt/x:ex-data err) "a")]))))
  => [["finally"] true 1])


^{:refer xt.lang.common-promise/with-delay :added "4.1"}
(fact "delays thunk execution for both supported argument orders"
  (notify/wait-on :js
    (common-promise/promise-then
     (common-promise/with-delay 10
                                (fn []
                                  (return "ms-first")))
     (repl/>notify)))
  => "ms-first"

  (notify/wait-on :js
    (common-promise/promise-then
     (common-promise/with-delay (fn []
                                  (return "fn-first"))
                                10)
     (repl/>notify)))
  => "fn-first")
