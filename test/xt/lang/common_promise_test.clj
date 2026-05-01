(ns xt.lang.common-promise-test
  (:require [std.lang              :as l]
            [xt.lang.common-notify :as notify])
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
(fact "detects the xt.promise wrapper"

  (!.js
    [(common-promise/promise-native? (common-promise/make-resolve-state 1))
     (common-promise/promise-native? {"::" "not.promise"})
     (common-promise/promise-native? 1)])
  => [true false false]

  (!.lua
    [(common-promise/promise-native? (common-promise/make-resolve-state 1))
     (common-promise/promise-native? {"::" "not.promise"})
     (common-promise/promise-native? 1)])
  => [true false false]

  (!.py
    [(common-promise/promise-native? (common-promise/make-resolve-state 1))
     (common-promise/promise-native? {"::" "not.promise"})
     (common-promise/promise-native? 1)])
  => [true false false])

^{:refer xt.lang.common-promise/make-resolve-state :added "4.1"}
(fact "creates a resolved wrapper"

  (!.js
    (var p (common-promise/make-resolve-state 7))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "value")])
  => ["xt.promise" "resolved" 7]

  (!.lua
    (var p (common-promise/make-resolve-state 7))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "value")])
  => ["xt.promise" "resolved" 7]

  (!.py
    (var p (common-promise/make-resolve-state 7))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "value")])
  => ["xt.promise" "resolved" 7])

^{:refer xt.lang.common-promise/make-rejected-state :added "4.1"}
(fact "creates a rejected wrapper"

  (!.js
    (var p (common-promise/make-rejected-state "boom"))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "error")])
  => ["xt.promise" "rejected" "boom"]

  (!.lua
    (var p (common-promise/make-rejected-state "boom"))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "error")])
  => ["xt.promise" "rejected" "boom"]

  (!.py
    (var p (common-promise/make-rejected-state "boom"))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "error")])
  => ["xt.promise" "rejected" "boom"])

^{:refer xt.lang.common-promise/make-pending-state :added "4.1"}
(fact "creates a pending wrapper with children"

  (!.js
    (var p (common-promise/make-pending-state false))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "is_async")
     (xt/x:len (xt/x:get-key p "children"))])
  => ["xt.promise" "pending" false 0]

  (!.lua
    (var p (common-promise/make-pending-state false))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "is_async")
     (xt/x:len (xt/x:get-key p "children"))])
  => ["xt.promise" "pending" false 0]

  (!.py
    (var p (common-promise/make-pending-state false))
    [(xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "is_async")
     (xt/x:len (xt/x:get-key p "children"))])
  => ["xt.promise" "pending" false 0])

^{:refer xt.lang.common-promise/internal-settle-action :added "4.1"}
(fact "settles a pending promise and dispatches subscribed children"

  (!.js
    (var seen [])
    (var p (common-promise/make-pending-state nil))
    (xt/x:arr-push
     (xt/x:get-key p "children")
     {"child" (common-promise/make-pending-state nil)})
    (common-promise/internal-settle-action
     p
     "resolved"
     5
     (fn [parent entry drive-fn]
       (xt/x:arr-push seen [(xt/x:get-key parent "status")
                            (xt/x:get-key parent "value")])))
    [(xt/x:get-key p "status")
     (xt/x:get-key p "value")
     (xt/x:get-key p "error")
     (xt/x:len (xt/x:get-key p "children"))
     seen])
  => ["resolved" 5 nil 0 [["resolved" 5]]]

  (!.lua
    (var seen [])
    (var p (common-promise/make-pending-state nil))
    (xt/x:arr-push
     (xt/x:get-key p "children")
     {"child" (common-promise/make-pending-state nil)})
    (common-promise/internal-settle-action
     p
     "resolved"
     5
     (fn [parent entry drive-fn]
       (xt/x:arr-push seen [(xt/x:get-key parent "status")
                            (xt/x:get-key parent "value")])))
    [(xt/x:get-key p "status")
     (xt/x:get-key p "value")
     (xt/x:get-key p "error")
     (xt/x:len (xt/x:get-key p "children"))
     seen])
  => ["resolved" 5 nil 0 [["resolved" 5]]]

  (!.py
    (var seen [])
    (var p (common-promise/make-pending-state nil))
    (xt/x:arr-push
     (xt/x:get-key p "children")
     {"child" (common-promise/make-pending-state nil)})
    (common-promise/internal-settle-action
     p
     "resolved"
     5
     (fn [parent entry drive-fn]
       (xt/x:arr-push seen [(xt/x:get-key parent "status")
                            (xt/x:get-key parent "value")])))
    [(xt/x:get-key p "status")
     (xt/x:get-key p "value")
     (xt/x:get-key p "error")
     (xt/x:len (xt/x:get-key p "children"))
     seen])
  => ["resolved" 5 nil 0 [["resolved" 5]]])

^{:refer xt.lang.common-promise/internal-link-action :added "4.1"}
(fact "subscribes child promises to pending parents"

  (!.js
    (var parent (common-promise/make-pending-state nil))
    (var child (common-promise/make-pending-state nil))
    (common-promise/internal-link-action
     parent
     child
     (fn [x] (return (+ x 1)))
     (fn [err] (return err))
     common-promise/internal-drive-action)
    (var entry (xt/x:first (xt/x:get-key parent "children")))
    [(xt/x:len (xt/x:get-key parent "children"))
     (xt/x:is-function? (xt/x:get-key entry "resolve"))
     (xt/x:is-function? (xt/x:get-key entry "reject"))
     (xt/x:get-key child "status")])
  => [1 true true "pending"]

  (!.lua
    (var parent (common-promise/make-pending-state nil))
    (var child (common-promise/make-pending-state nil))
    (common-promise/internal-link-action
     parent
     child
     (fn [x] (return (+ x 1)))
     (fn [err] (return err))
     common-promise/internal-drive-action)
    (var entry (xt/x:first (xt/x:get-key parent "children")))
    [(xt/x:len (xt/x:get-key parent "children"))
     (xt/x:is-function? (xt/x:get-key entry "resolve"))
     (xt/x:is-function? (xt/x:get-key entry "reject"))
     (xt/x:get-key child "status")])
  => [1 true true "pending"]

  (!.py
    (var parent (common-promise/make-pending-state nil))
    (var child (common-promise/make-pending-state nil))
    (common-promise/internal-link-action
     parent
     child
     (fn [x] (return (+ x 1)))
     (fn [err] (return err))
     common-promise/internal-drive-action)
    (var entry (xt/x:first (xt/x:get-key parent "children")))
    [(xt/x:len (xt/x:get-key parent "children"))
     (xt/x:is-function? (xt/x:get-key entry "resolve"))
     (xt/x:is-function? (xt/x:get-key entry "reject"))
     (xt/x:get-key child "status")])
  => [1 true true "pending"])

^{:refer xt.lang.common-promise/internal-adopt-action :added "4.1"}
(fact "adopts raw values and resolved wrappers"

  (!.js
    (var raw-target (common-promise/make-pending-state nil))
    (var wrapped-target (common-promise/make-pending-state nil))
    (common-promise/internal-adopt-action
     raw-target
     5
     common-promise/internal-drive-action)
    (common-promise/internal-adopt-action
     wrapped-target
     (common-promise/make-resolve-state 7)
     common-promise/internal-drive-action)
    [(xt/x:get-key raw-target "status")
     (xt/x:get-key raw-target "value")
     (xt/x:get-key wrapped-target "status")
     (xt/x:get-key wrapped-target "value")])
  => ["resolved" 5 "resolved" 7]

  (!.lua
    (var raw-target (common-promise/make-pending-state nil))
    (var wrapped-target (common-promise/make-pending-state nil))
    (common-promise/internal-adopt-action
     raw-target
     5
     common-promise/internal-drive-action)
    (common-promise/internal-adopt-action
     wrapped-target
     (common-promise/make-resolve-state 7)
     common-promise/internal-drive-action)
    [(xt/x:get-key raw-target "status")
     (xt/x:get-key raw-target "value")
     (xt/x:get-key wrapped-target "status")
     (xt/x:get-key wrapped-target "value")])
  => ["resolved" 5 "resolved" 7]

  (!.py
    (var raw-target (common-promise/make-pending-state nil))
    (var wrapped-target (common-promise/make-pending-state nil))
    (common-promise/internal-adopt-action
     raw-target
     5
     common-promise/internal-drive-action)
    (common-promise/internal-adopt-action
     wrapped-target
     (common-promise/make-resolve-state 7)
     common-promise/internal-drive-action)
    [(xt/x:get-key raw-target "status")
     (xt/x:get-key raw-target "value")
     (xt/x:get-key wrapped-target "status")
     (xt/x:get-key wrapped-target "value")])
  => ["resolved" 5 "resolved" 7])

^{:refer xt.lang.common-promise/internal-drive-action :added "4.1"}
(fact "drives resolved and rejected parent states into child promises"

  (!.js
    (var resolved-parent (common-promise/make-resolve-state 5))
    (var resolved-child (common-promise/make-pending-state nil))
    (common-promise/internal-drive-action
     resolved-parent
     {"child" resolved-child
      "resolve" (fn [value] (return (+ value 2)))
      "reject" nil}
     common-promise/internal-drive-action)
    (var rejected-parent (common-promise/make-rejected-state "boom"))
    (var rejected-child (common-promise/make-pending-state nil))
    (common-promise/internal-drive-action
     rejected-parent
     {"child" rejected-child
      "resolve" nil
      "reject" nil}
     common-promise/internal-drive-action)
    [(xt/x:get-key resolved-child "status")
     (xt/x:get-key resolved-child "value")
     (xt/x:get-key rejected-child "status")
     (xt/x:get-key rejected-child "error")])
  => ["resolved" 7 "rejected" "boom"]

  (!.lua
    (var resolved-parent (common-promise/make-resolve-state 5))
    (var resolved-child (common-promise/make-pending-state nil))
    (common-promise/internal-drive-action
     resolved-parent
     {"child" resolved-child
      "resolve" (fn [value] (return (+ value 2)))
      "reject" nil}
     common-promise/internal-drive-action)
    (var rejected-parent (common-promise/make-rejected-state "boom"))
    (var rejected-child (common-promise/make-pending-state nil))
    (common-promise/internal-drive-action
     rejected-parent
     {"child" rejected-child
      "resolve" nil
      "reject" nil}
     common-promise/internal-drive-action)
    [(xt/x:get-key resolved-child "status")
     (xt/x:get-key resolved-child "value")
     (xt/x:get-key rejected-child "status")
     (xt/x:get-key rejected-child "error")])
  => ["resolved" 7 "rejected" "boom"]

  (!.py
    (var resolved-parent (common-promise/make-resolve-state 5))
    (var resolved-child (common-promise/make-pending-state nil))
    (common-promise/internal-drive-action
     resolved-parent
     {"child" resolved-child
      "resolve" (fn [value] (return (+ value 2)))
      "reject" nil}
     common-promise/internal-drive-action)
    (var rejected-parent (common-promise/make-rejected-state "boom"))
    (var rejected-child (common-promise/make-pending-state nil))
    (common-promise/internal-drive-action
     rejected-parent
     {"child" rejected-child
      "resolve" nil
      "reject" nil}
     common-promise/internal-drive-action)
    [(xt/x:get-key resolved-child "status")
     (xt/x:get-key resolved-child "value")
     (xt/x:get-key rejected-child "status")
     (xt/x:get-key rejected-child "error")])
  => ["resolved" 7 "rejected" "boom"])

^{:refer xt.lang.common-promise/promise :added "4.1"}
(fact "wraps thunk execution in the common promise model"

  (!.js
    (common-promise/promise-native?
     (common-promise/promise
      (fn []
        (return 1)))))
  => true

  (notify/wait-on :js
    (common-promise/promise-then
     (common-promise/promise
      (fn []
        (return 9)))
     (repl/>notify)))
  => 9

  (!.lua
    (common-promise/promise-native?
     (common-promise/promise
      (fn []
        (return 1)))))
  => true

  (notify/wait-on :lua
    (common-promise/promise-then
     (common-promise/promise
      (fn []
        (return 9)))
     (repl/>notify)))
  => 9

  (!.py
    (common-promise/promise-native?
     (common-promise/promise
      (fn []
        (return 1)))))
  => true

  (notify/wait-on :python
    (common-promise/promise-then
     (common-promise/promise
      (fn []
        (return 9)))
     (repl/>notify)))
  => 9)

^{:refer xt.lang.common-promise/promise-run :added "4.1"}
(fact "common promise helpers use the :: xt.promise wrapper"

  (!.js
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
  => [true "xt.promise" "resolved" 5]

  (!.py
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

  (notify/wait-on :lua
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
  => 5)

^{:refer xt.lang.common-promise/promise-catch :added "4.1"}
(fact "preserves xtalk exception data through rejection"

  (notify/wait-on :js
    (common-promise/promise-catch
     (common-promise/promise
      (fn []
        (throw (xt/x:ex "boom" {:a 1}))))
     (fn [err]
       (repl/notify [(xt/x:ex-native? err)
                     (xt/x:get-key (xt/x:ex-data err) "a")]))))
  => [true 1]

  (notify/wait-on :lua
    (common-promise/promise-catch
     (common-promise/promise
      (fn []
        (throw (xt/x:ex "boom" {:a 1}))))
     (fn [err]
       (repl/notify [(xt/x:ex-native? err)
                     (xt/x:get-key (xt/x:ex-data err) "a")]))))
  => [true 1]

  (notify/wait-on :python
    (common-promise/promise-catch
     (common-promise/promise
      (fn []
        (throw (xt/x:ex "boom" {:a 1}))))
     (fn [err]
       (repl/notify [(xt/x:ex-native? err)
                     (xt/x:get-key (xt/x:ex-data err) "a")]))))
  => [true 1])

^{:refer xt.lang.common-promise/promise-all :added "4.1"}
(fact "waits for promise values in order"

  (notify/wait-on :js
    (common-promise/promise-then
     (common-promise/promise-all
      [(common-promise/promise
        (fn []
          (return "a")))
       "b"
       (common-promise/promise-run 3)])
     (repl/>notify)))
  => ["a" "b" 3]

  (notify/wait-on :lua
    (common-promise/promise-then
     (common-promise/promise-all
      [(common-promise/promise
        (fn []
          (return "a")))
       "b"
       (common-promise/promise-run 3)])
     (repl/>notify)))
  => ["a" "b" 3]

  (notify/wait-on :python
    (common-promise/promise-then
     (common-promise/promise-all
      [(common-promise/promise
        (fn []
          (return "a")))
       "b"
       (common-promise/promise-run 3)])
     (repl/>notify)))
  => ["a" "b" 3])

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
  => [["finally"] true 1])

^{:refer xt.lang.common-promise/with-delay :added "4.1"}
(fact "delays thunk execution with ms first"

  (notify/wait-on :js
    (common-promise/promise-then
     (common-promise/with-delay 10
                                (fn []
                                  (return "ms-first")))
     (repl/>notify)))
  => "ms-first"

  (notify/wait-on :lua
    (common-promise/promise-then
     (common-promise/with-delay 10
                                (fn []
                                  (return "ms-first")))
     (repl/>notify)))
  => "ms-first"

  (notify/wait-on :python
    (common-promise/promise-then
     (common-promise/with-delay 10
                                (fn []
                                  (return "ms-first")))
     (repl/>notify)))
  => "ms-first")

(comment
  (s/snapto '[xt.lang.common-promise])
  
  (s/seedgen-langadd '[xt.lang.common-promise] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.lang.common-promise] {:lang [:lua :python] :write true}))
