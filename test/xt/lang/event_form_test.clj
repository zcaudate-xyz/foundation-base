(ns xt.lang.event-form-test
  (:require [net.http :as http]
            [std.json :as json]
            [std.lang :as l]
            [std.lang.interface.type-notify :as interface]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :xtalk
  {:require [[xt.lang.event-form :as form]
             [xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.event-form :as form]]})

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.event-form :as form]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.event-form :as form]]})

(defn.xt walk
  [obj pre-fn post-fn]
  (:= obj (pre-fn obj))
  (cond (xt/x:nil? obj)
        (return (post-fn obj))

        (xt/x:is-object? obj)
        (do (var out := {})
            (xt/for:object [[k v] obj]
              (xt/x:set-key out k (-/walk v pre-fn post-fn)))
            (return (post-fn out)))

        (xt/x:is-array? obj)
        (do (var out := [])
            (xt/for:array [e obj]
              (xt/x:arr-push out (-/walk e pre-fn post-fn)))
            (return (post-fn out)))

        :else
        (return (post-fn obj))))

(defn.xt get-data
  [obj]
  (var data-fn
       (fn [obj]
         (if (or (xt/x:is-string? obj)
                 (xt/x:is-number? obj)
                 (xt/x:is-boolean? obj)
                 (xt/x:is-object? obj)
                 (xt/x:is-array? obj)
                 (xt/x:nil? obj))
           (return obj)
           (return (xt/x:cat "<" (k/type-native obj) ">")))))
  (return (-/walk obj k/identity data-fn)))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(defn.xt test-form
  []
  (return
   (form/make-form
    (fn:> {:login ""})
    {:login [["is-required"
              {:message "Required field."
               :check  (fn [v rec]
                         (return (and (k/not-nil? v)
                                      (< 0 (xt/x:len v)))))}]]})))

^{:refer xt.lang.event-form/remove-listener :adopt true :added "4.0"}
(fact "removes all listeners")

^{:refer xt.lang.event-form/list-listeners :adopt true :added "4.0"
  :setup [(def +out+
            (contains-in
             [(contains ["a1" "b2" "c3"] :in-any-order)
              {"callback" "<function>",
               "pred" "<function>",
               "meta"
               {"listener/id" "b2",
                "form/fields" ["login"],
                "listener/type" "form"}}
              (contains ["a1" "c3"] :in-any-order)]))]}
(fact "lists all listeners"
  ^:hidden
  
  (!.js
   (var f (-/test-form))
   (form/add-listener f "a1" ["login"]  (fn:>) nil)
   (form/add-listener f "b2" ["login"]  (fn:>) nil)
   (form/add-listener f "c3" ["login"]  (fn:>) nil)
   [(form/list-listeners f)
    (-/get-data (form/remove-listener f  "b2"))
    (form/list-listeners f)])
  => +out+
  
  (!.lua
   (var f (-/test-form))
   (form/add-listener f "a1" ["login"]  (fn:>) nil)
   (form/add-listener f "b2" ["login"]  (fn:>) nil)
   (form/add-listener f "c3" ["login"]  (fn:>) nil)
   [(form/list-listeners f)
    (-/get-data (form/remove-listener f  "b2"))
    (form/list-listeners f)])
  => +out+
    
  (!.py
   (var f (-/test-form))
   (form/add-listener f "a1" ["login"]  (fn:>) nil)
   (form/add-listener f "b2" ["login"]  (fn:>) nil)
   (form/add-listener f "c3" ["login"]  (fn:>) nil)
   [(form/list-listeners f)
    (-/get-data (form/remove-listener f  "b2"))
    (form/list-listeners f)])
  => +out+)

^{:refer xt.lang.event-form/make-form :added "4.0"
  :setup [(def +out+
            {"initial" "<function>",
             "::" "event.form",
             "listeners" {},
             "validators"
             {"login"
              [["is-required" {"message" "Required field.", "check" "<function>"}]]},
             "data" {"login" ""},
             "result"
             {"status" "pending",
              "fields" {"login" {"status" "pending"}},
              "::" "validation.result"}})]}
(fact "creates a form"
  ^:hidden
  
  (!.js
   (-/get-data
    (form/make-form
     (fn:> {:login ""})
     {:login [["is-required"
               {:message "Required field."
                :check  (fn [v rec]
                          (return (and (k/not-nil? v)
                                       (< 0 (xt/x:len v)))))}]]})))
  => +out+

  (!.lua
   (-/get-data
    (form/make-form
     (fn:> {:login ""})
     {:login [["is-required"
               {:message "Required field."
                :check  (fn [v rec]
                          (return (and (k/not-nil? v)
                                       (< 0 (xt/x:len v)))))}]]})))
  => +out+

  (!.py
   (-/get-data
    (form/make-form
     (fn:> {:login ""})
     {:login [["is-required"
               {:message "Required field."
                :check  (fn [v rec]
                          (return (and (k/not-nil? v)
                                       (< 0 (xt/x:len v)))))}]]})))
  
  => +out+)

^{:refer xt.lang.event-form/check-event :added "4.0"}
(fact "checks that event needs to be processed"
  ^:hidden
  
  (!.js
   [(form/check-event {:fields ["a" "b" "c"]}
                      ["a"])
    (form/check-event {:fields ["a" "b" "c"]}
                      [])
    (form/check-event {:fields ["a" "b" "c"]}
                      ["b" "d"])])
  => [true false true]

  (!.lua
   [(form/check-event {:fields ["a" "b" "c"]}
                      ["a"])
    (form/check-event {:fields ["a" "b" "c"]}
                      [])
    (form/check-event {:fields ["a" "b" "c"]}
                      ["b" "d"])])
  => [true false true]

  (!.py
   [(form/check-event {:fields ["a" "b" "c"]}
                      ["a"])
    (form/check-event {:fields ["a" "b" "c"]}
                      [])
    (form/check-event {:fields ["a" "b" "c"]}
                      ["b" "d"])])
  => [true false true])

^{:refer xt.lang.event-form/add-listener :added "4.0"
  :setup [(def +out+
            {"type" "form.data",
             "fields" ["login"],
             "meta" {"listener/id" "abc",
                     "form/fields" ["login"],
                     "listener/type" "form"}})]}
(fact "adds listener to a form"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "abc" ["login"]  (repl/>notify))
    (form/set-field f "login" "test00001"))
  => +out+

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "abc" ["login"]  (repl/>notify))
    (form/set-field f "login" "test00001"))
  => +out+

  (notify/wait-on :python
    (var f (-/test-form))
    (form/add-listener f "abc" ["login"]  (repl/>notify) nil)
    (form/set-field f "login" "test00001"))
  => +out+)

^{:refer xt.lang.event-form/trigger-all :added "4.0"
  :setup [(def +out+
            {"type" "form.data",
             "fields" ["login"],
             "meta"
             {"listener/id" "abc",
              "form/fields" ["login"],
              "listener/type" "form"}})]}
(fact "triggers all fields"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "abc" ["login"]  (repl/>notify) nil)
    (form/trigger-all f "form.data"))
  => +out+

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "abc" ["login"]  (repl/>notify) nil)
    (form/trigger-all f "form.data"))
  => +out+

  (notify/wait-on :python
    (var f (-/test-form))
    (form/add-listener f "abc" ["login"]  (repl/>notify) nil)
    (form/trigger-all f "form.data"))
  => +out+)

^{:refer xt.lang.event-form/trigger-field :added "4.0"
  :setup [(def +out+
            {"type" "form.data",
             "fields" ["login"],
             "meta"
             {"listener/id" "abc",
              "form/fields" ["login"],
              "listener/type" "form"}})]}
(fact "triggers the callback"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "abc" ["login"]  (repl/>notify) nil)
    (form/trigger-field f "login" "form.data"))
  => +out+

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "abc" ["login"]  (repl/>notify) nil)
    (form/trigger-field f "login" "form.data"))
  => +out+

  (notify/wait-on :python
    (var f (-/test-form))
    (form/add-listener f "abc" ["login"]  (repl/>notify) nil)
    (form/trigger-field f "login" "form.data"))
  => +out+)

^{:refer xt.lang.event-form/set-field :added "4.0"
  :setup [(def +out+
            {"type" "form.data",
             "fields" ["login"],
             "meta"
             {"listener/id" "a1",
              "form/fields" ["login"],
              "listener/type" "form"}})]}
(fact "sets the field"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    (form/set-field f "login" "world"))
  => +out+

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    (form/set-field f "login" "world"))
  => +out+

  (notify/wait-on :python
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    (form/set-field f "login" "world"))
  => +out+)

^{:refer xt.lang.event-form/get-field :added "4.0"}
(fact "gets the field")

^{:refer xt.lang.event-form/toggle-field :added "4.0"}
(fact "toggles the field")

^{:refer xt.lang.event-form/field-fn :added "4.0"
  :setup [(def +out+
            {"type" "form.data",
             "fields" ["login"],
             "meta"
             {"listener/id" "a1",
              "form/fields" ["login"],
              "listener/type" "form"}})]}
(fact "constructs a field function"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    ((form/field-fn f "login")  "world"))
  => +out+

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    ((form/field-fn f "login")  "world"))
  => +out+
  
  (notify/wait-on :python
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    ((form/field-fn f "login")  "world"))
  => +out+)

^{:refer xt.lang.event-form/get-result :added "4.0"}
(fact "gets the validation result")

^{:refer xt.lang.event-form/get-field-result :added "4.0"}
(fact "gets the validation status"
  ^:hidden
  
  (!.js
   (var f (-/test-form))
   (form/get-field-result f "login"))
  => {"status" "pending"}

  (!.lua
   (var f (-/test-form))
   (form/get-field-result f "login"))
  => {"status" "pending"}

  (!.py
   (var f (-/test-form))
   (form/get-field-result f "login"))
  => {"status" "pending"})

^{:refer xt.lang.event-form/get-data :added "4.0"}
(fact "gets the data")

^{:refer xt.lang.event-form/set-data :added "4.0"
  :setup [(def +out+
            {"type" "form.data",
             "fields" ["login"],
             "meta"
             {"listener/id" "a1",
              "form/fields" ["login"],
              "listener/type" "form"}})]}
(fact "sets the data directly"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    (form/set-data f {:login "world"}))
  => +out+

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    (form/set-data f {:login "world"}))
  => +out+

  (notify/wait-on :python
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    (form/set-data f {:login "world"}))
  => +out+)

^{:refer xt.lang.event-form/reset-all-data :added "4.0"}
(fact "resets all data"
  ^:hidden
  
  (!.js
   (var f (-/test-form))
   [(form/set-data f {:login "world"})
    (form/reset-all-data f)])
  => [[] []]

  (!.lua
   (var f (-/test-form))
   [(form/set-data f {:login "world"})
    (form/reset-all-data f)])
  => [{} {}]

  (!.py
   (var f (-/test-form))
   [(form/set-data f {:login "world"})
    (form/reset-all-data f)])
  => [[] []])

^{:refer xt.lang.event-form/reset-field-data :added "4.0"}
(fact "reset field data"
  ^:hidden
  
  (!.js
   (var f (-/test-form))
   (form/set-data f {:login "world"})
   (form/reset-field-data f "login")
   (form/get-data f))
  => {"login" ""}

  (!.lua
   (var f (-/test-form))
   (form/set-data f {:login "world"})
   (form/reset-field-data f "login")
   (form/get-data f))
  => {"login" ""}

  (!.py
   (var f (-/test-form))
   (form/set-data f {:login "world"})
   (form/reset-field-data f "login")
   (form/get-data f))
  => {"login" ""})

^{:refer xt.lang.event-form/validate-all :added "4.0"}
(fact "validates all form"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/validate-all f k/identity
                       (repl/>notify)))
  => false

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/validate-all f k/identity
                       (repl/>notify)))
  => false)

^{:refer xt.lang.event-form/validate-field :added "4.0"
  :setup [(def +out+
            {"type" "form.validation",
             "fields" ["login"],
             "meta"
             {"listener/id" "a1",
              "listener/type" "form"
              "form/fields" ["login"],}})]}
(fact "validates form field"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
   (form/validate-field f "login" k/identity))
  => +out+

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
   (form/validate-field f "login" k/identity))
  => +out+)

^{:refer xt.lang.event-form/reset-field-validator :added "4.0"
  :setup [(def +out+
            {"type" "form.validation",
             "fields" ["login"],
             "meta"
             {"listener/id" "a1",
              "listener/type" "form"
              "form/fields" ["login"],}})]}
(fact "reset field validators"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    (form/reset-field-validator f "login"))
  => +out+

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    (form/reset-field-validator f "login"))
  => +out+)

^{:refer xt.lang.event-form/reset-all-validators :added "4.0"
  :setup [(def +out+
            {"type" "form.validation",
             "fields" ["login"],
             "meta"
             {"listener/id" "a1",
              "listener/type" "form"
              "form/fields" ["login"],}})]}
(fact "reset all field validators"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    (form/reset-all-validators f))
  => +out+

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    (form/reset-all-validators f))
  => +out+)

^{:refer xt.lang.event-form/reset-all :added "4.0"}
(fact "resets data and validator result"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "a1"  ["login"] (repl/>notify) nil)
    (form/reset-all f))
  => (contains-in
      {"fields" ["login"],
       "meta"
       {"listener/id" "a1",
        "form/fields" ["login"],
        "listener/type" "form"}}))

^{:refer xt.lang.event-form/check-field-passed :added "4.0"}
(fact "checks that field has passed"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "a1" ["login"]
                       (fn []
                         (repl/notify (form/check-field-passed f "login"))))
    (form/validate-all f))
  => false

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "a1" ["login"]
                       (fn []
                         (repl/notify (form/check-field-passed f "login"))))
    (form/validate-all f))
  => false)

^{:refer xt.lang.event-form/check-field-errored :added "4.0"}
(fact  "checks that field has passed"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "a1" ["login"]
                       (fn []
                         (repl/notify (form/check-field-errored f "login"))))
    (form/validate-all f))
  => true

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "a1" ["login"]
                       (fn []
                         (repl/notify (form/check-field-errored f "login"))))
    (form/validate-all f))
  => true)

^{:refer xt.lang.event-form/check-all-passed :added "4.0"}
(fact "checks that all fields have passed"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "a1" ["login"]
                       (fn []
                         (repl/notify (form/check-all-passed f))))
    (form/validate-all f))
  => false

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "a1" ["login"]
                       (fn []
                         (repl/notify (form/check-all-passed f))))
    (form/validate-all f))
  => false)

^{:refer xt.lang.event-form/check-any-errored :added "4.0"}
(fact  "checks that any fields have errored"
  ^:hidden
  
  (notify/wait-on :js
    (var f (-/test-form))
    (form/add-listener f "a1" ["login"]
                       (fn []
                         (repl/notify (form/check-any-errored f))))
   (form/validate-all f))
  => true

  (notify/wait-on :lua
    (var f (-/test-form))
    (form/add-listener f "a1" ["login"]
                       (fn []
                         (repl/notify (form/check-any-errored f))))
   (form/validate-all f))
  => true)
