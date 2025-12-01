(ns js.react.ext-form-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]
            [std.fs :as fs]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.core :as j]
             [js.react.ext-form :as ext-form]]})

(fact:global
 {:setup    [(do (l/rt:restart :js)
                 (l/rt:scaffold-imports :js))]
  :teardown  [(l/rt:stop)]})

(def.js Validatiors
  {:first    [["is-not-empty" {:message "Must not be empty"
                               :check (fn:> [v rec] (and (k/not-nil? v)
                                                         (< 0 (k/len v))))}]]
   :last     [["is-not-empty" {:message "Must not be empty"
                               :check (fn:> [v rec]
                                        (j/future-delayed [100]
                                          (return (and (k/not-nil? v)
                                                       (< 0 (k/len v))))))}]]
   :email    [["is-not-empty" {:message "Must not be empty"
                               :check (fn:> [v rec]
                                        (j/future-delayed [100]
                                                          (return (and (k/not-nil? v)
                                                       (< 0 (k/len v))))))}]]})

^{:refer js.react.ext-form/makeFree :added "4.0" :unchecked true}
(fact "makes a free form (no validation)")

^{:refer js.react.ext-form/makeFreeEdit :added "4.0" :unchecked true}
(fact "edit helper for forms")

^{:refer js.react.ext-form/checkPrint :added "4.0" :unchecked true}
(fact "checks that form should be printed")

^{:refer js.react.ext-form/makeForm :added "4.0" :unchecked true}
(fact "makes a form")

^{:refer js.react.ext-form/useListener :added "4.0" :unchecked true}
(fact "generic listener")

^{:refer js.react.ext-form/getFieldPassed :added "4.0" :unchecked true}
(fact "gets the passed status for field")

^{:refer js.react.ext-form/getFieldStatus :added "4.0" :unchecked true}
(fact "gets the id and status for a field")

^{:refer js.react.ext-form/listenFields :added "4.0" :unchecked true}
(fact "listens to multiple fields")

^{:refer js.react.ext-form/listenFieldsData :added "4.0" :unchecked true}
(fact "uses data from multiple fields in form")

^{:refer js.react.ext-form/listenField :added "4.0" :unchecked true}
(fact "gets value and result of a form field")

^{:refer js.react.ext-form/listenFieldValue :added "4.0" :unchecked true}
(fact "gets only a field value")

^{:refer js.react.ext-form/listenFieldResult :added "4.0" :unchecked true}
(fact "gets result of a form field")

^{:refer js.react.ext-form/listenForm :added "4.0" :unchecked true}
(fact "gets all form changes")

^{:refer js.react.ext-form/listenFormData :added "4.0" :unchecked true}
(fact "gets all form data")

^{:refer js.react.ext-form/listenFormResult :added "4.0" :unchecked true}
(fact "gets form validation result")

^{:refer js.react.ext-form/useSubmitField :added "4.0" :unchecked true}
(fact "gets submit field")

^{:refer js.react.ext-form/useSubmitForm :added "4.0" :unchecked true}
(fact "gets submit form")
