(ns xtbench.dart.event.base-form-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do 
  (l/script- :xtalk
    {:require [[xt.event.base-form :as form]
               [xt.lang.spec-base :as xt]]})
  
  (defn.xt make-login-form []
  (return
   (form/make-form
    (fn:> {:login ""})
    {:login [["is-required"
              {:message "Required field."
               :check (fn [v rec]
                        (return
                         (and (not (xt/x:nil? v))
                              (< 0 (xt/x:len v)))))}]]})))
  
  (defn.xt make-flag-form []
    (return
     (form/make-form
      (fn:> {:flag false})
      {:flag []}))))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
              [xt.event.base-form :as form]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.base-form/check-event :added "4.1"}
(fact "checks field overlap"

  (!.dt
   [(form/check-event {:fields ["a" "b" "c"]} ["a"])
    (form/check-event {:fields ["a" "b" "c"]} [])
    (form/check-event {:fields ["a" "b" "c"]} ["b" "d"])])
  => [true false true])

^{:refer xt.event.base-form/make-form :added "4.1"}
(fact "manages form data and listeners"

  (!.dt
   (var f (form/make-form
           (fn:> {:login ""})
           {:login [["is-required"
                     {:message "Required field."
                      :check (fn [v rec]
                               (return
                                (and (k/not-nil? v)
                                     (< 0 (xt/x:len v)))))}]]}))
   (var calls [])
   (form/add-listener f
                      "a1"
                      ["login"]
                       (fn [id data t meta]
                         (xt/x:arr-push calls "a1"))
                      nil)
   (form/set-field f "login" "user")
   [(form/list-listeners f)
    calls
    (form/get-field f "login")
    (form/get-data f)
    (. (form/remove-listener f "a1") ["meta"])
    (form/list-listeners f)])
  => (just-in
      [["a1"]
       ["a1"]
       "user"
       {"login" "user"}
       {"listener/id" "a1"
        "listener/type" "form"
        "form/fields" ["login"]}
       []]))

^{:refer xt.event.base-form/add-listener :added "4.1"}
(fact "adds a form listener with field metadata"

  (!.dt
   (var f (-/make-login-form))
   (var out nil)
   (form/add-listener f "abc" ["login"] (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil)
   (form/set-field f "login" "test00001")
   out)
  => {"id" "abc"
      "data" {"fields" ["login"]
              "type" "form.data"}
      "t" nil
      "meta" {"form/fields" ["login"]
              "listener/id" "abc"
              "listener/type" "form"}})

^{:refer xt.event.base-form/trigger-all :added "4.1"}
(fact "triggers listeners for all fields"

  (!.dt
   (var f (-/make-login-form))
   (var out nil)
   (form/add-listener f "abc" ["login"] (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil)
   (form/trigger-all f "form.data")
   out)
  => {"id" "abc"
      "data" {"fields" ["login"]
              "type" "form.data"}
      "t" nil
      "meta" {"form/fields" ["login"]
              "listener/id" "abc"
              "listener/type" "form"}})

^{:refer xt.event.base-form/trigger-field :added "4.1"}
(fact "triggers listeners for a single field"

  (!.dt
   (var f (-/make-login-form))
   (var out nil)
   (form/add-listener f "abc" ["login"] (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil)
   (form/trigger-field f "login" "form.data")
   out)
  => {"id" "abc"
      "data" {"fields" ["login"]
              "type" "form.data"}
      "t" nil
      "meta" {"form/fields" ["login"]
              "listener/id" "abc"
              "listener/type" "form"}})

^{:refer xt.event.base-form/set-field :added "4.1"}
(fact "sets a form field and returns triggered ids"

  (!.dt
   (var f (-/make-login-form))
   (form/add-listener f "a1" ["login"] (fn:> [_ _ _ _] nil) nil)
   [(form/set-field f "login" "world")
    (form/get-field f "login")])
  => [["a1"] "world"])

^{:refer xt.event.base-form/get-field :added "4.1"}
(fact "gets the current field value"

  (!.dt
   (var f (-/make-login-form))
   (form/set-field f "login" "world")
   (form/get-field f "login"))
  => "world")

^{:refer xt.event.base-form/toggle-field :added "4.1"}
(fact "toggles boolean fields"

  (!.dt
   (var f (-/make-flag-form))
   (form/toggle-field f "flag")
   (form/get-field f "flag"))
  => true)

^{:refer xt.event.base-form/field-fn :added "4.1"}
(fact "builds a setter function for a field"

  (!.dt
   (var f (-/make-login-form))
   (form/add-listener f "a1" ["login"] (fn:> [_ _ _ _] nil) nil)
   [((form/field-fn f "login") "world")
    (form/get-field f "login")])
  => [["a1"] "world"])

^{:refer xt.event.base-form/get-result :added "4.1"}
(fact "gets the full validation result"

  (!.dt
   (form/get-result (-/make-login-form)))
  => {"::" "validation.result"
      "fields" {"login" {"status" "pending"}}
      "status" "pending"})

^{:refer xt.event.base-form/get-field-result :added "4.1"}
(fact "gets a single field validation result"

  (!.dt
   (form/get-field-result (-/make-login-form) "login"))
  => {"status" "pending"})

^{:refer xt.event.base-form/get-data :added "4.1"}
(fact "gets the current form data"

  (!.dt
   (form/get-data (-/make-login-form)))
  => {"login" ""})

^{:refer xt.event.base-form/set-data :added "4.1"}
(fact "sets form data directly"

  (!.dt
   (var f (-/make-login-form))
   (var out nil)
   (form/add-listener f "a1" ["login"] (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil)
   (form/set-data f {:login "world"})
   [out (form/get-data f)])
  => [{"fields" ["login"]
       "meta" {"form/fields" ["login"]
               "listener/id" "a1"
               "listener/type" "form"}
       "type" "form.data"}
      {"login" "world"}])

^{:refer xt.event.base-form/reset-all-data :added "4.1"}
(fact "resets all form data to the initial state"

  (!.dt
   (var f (-/make-login-form))
   (form/set-data f {:login "world"})
   [(form/reset-all-data f)
    (form/get-data f)])
  => [[] {"login" ""}])

^{:refer xt.event.base-form/reset-field-data :added "4.1"}
(fact "resets a single field to its initial value"

  (!.dt
   (var f (-/make-login-form))
   (form/set-data f {:login "world"})
   (form/reset-field-data f "login")
   (form/get-data f))
  => {"login" ""})

^{:refer xt.event.base-form/validate-all :added "4.1"}
(fact "validates all fields and updates form state"

  (notify/wait-on :dart
    (var f (-/make-login-form))
    (form/validate-all
     f
     (fn [field status] (return nil))
     (fn [ok res]
       (repl/notify
        [ok
         (. (form/get-result f) ["status"])
         (form/check-any-errored f)]))))
  => [false "errored" true])

^{:refer xt.event.base-form/validate-field :added "4.1"}
(fact "validates one field and triggers validation listeners"

  (notify/wait-on :dart
    (var f (-/make-login-form))
    (form/add-listener
     f "a1" ["login"]
     (fn [id data t meta]
        (repl/notify
         [{"id" id "data" data "t" t "meta" meta}
          (form/get-field-result f "login")]))
     nil)
    (form/validate-field f "login" (fn [field status] (return nil)) nil))
  => [{"id" "a1"
       "data" {"fields" ["login"]
               "type" "form.validation"}
       "t" nil
       "meta" {"form/fields" ["login"]
               "listener/id" "a1"
               "listener/type" "form"}}
      {"data" ""
       "id" "is-required"
       "message" "Required field."
       "status" "errored"}])

^{:refer xt.event.base-form/reset-field-validator :added "4.1"}
(fact "triggers validation listeners when resetting a field validator"

  (!.dt
   (var f (-/make-login-form))
   (var out nil)
   (form/add-listener f "a1" ["login"] (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil)
   (form/reset-field-validator f "login")
   out)
  => {"id" "a1"
      "data" {"fields" ["login"]
              "type" "form.validation"}
      "t" nil
      "meta" {"form/fields" ["login"]
              "listener/id" "a1"
              "listener/type" "form"}})

^{:refer xt.event.base-form/reset-all-validators :added "4.1"}
(fact "resets all validator state"

  (!.dt
   (var f (-/make-login-form))
   (form/add-listener f "a1" ["login"] (fn:> [_ _ _ _] nil) nil)
   (form/reset-all-validators f)
   (form/get-result f))
  => {"::" "validation.result"
      "fields" {"login" {"status" "pending"}}
      "status" "pending"})

^{:refer xt.event.base-form/reset-all :added "4.1"}
(fact "resets both data and validation state"

  (!.dt
   (var f (-/make-login-form))
   (form/set-data f {:login "world"})
   (form/reset-all f)
   [(form/get-data f)
    (form/get-result f)])
  => [{"login" ""}
      {"::" "validation.result"
       "fields" {"login" {"status" "pending"}}
       "status" "pending"}])

^{:refer xt.event.base-form/check-field-passed :added "4.1"}
(fact "checks whether a field passed validation"

  (notify/wait-on :dart
    (var f (-/make-login-form))
    (form/set-field f "login" "world")
    (form/validate-all
     f nil
     (fn [ok res]
       (repl/notify (form/check-field-passed f "login")))))
  => true)

^{:refer xt.event.base-form/check-field-errored :added "4.1"}
(fact "checks whether a field errored"

  (notify/wait-on :dart
    (var f (-/make-login-form))
    (form/validate-all
     f nil
     (fn [ok res]
       (repl/notify (form/check-field-errored f "login")))))
  => true)

^{:refer xt.event.base-form/check-all-passed :added "4.1"}
(fact "checks whether all fields passed"

  (notify/wait-on :dart
    (var f (-/make-login-form))
    (form/set-field f "login" "world")
    (form/validate-all
     f nil
     (fn [ok res]
       (repl/notify (form/check-all-passed f)))))
  => true)

^{:refer xt.event.base-form/check-any-errored :added "4.1"}
(fact "checks whether any field errored"

  (notify/wait-on :dart
    (var f (-/make-login-form))
    (form/validate-all
     f nil
     (fn [ok res]
       (repl/notify (form/check-any-errored f)))))
  => true)
