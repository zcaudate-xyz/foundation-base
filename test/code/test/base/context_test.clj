(ns code.test.base.context-test
  (:require [code.test.base.context :as context]
            [code.test :refer :all]))

(fact "new-context returns a map with expected keys"
  (context/new-context)
  => (contains {:eval-fact false
                :eval-mode false
                :eval-meta nil
                :eval-global nil
                :eval-check nil
                :eval-current-ns nil
                :run-id true
                :registry (satisfies #(instance? clojure.lang.Atom %))
                :accumulator (satisfies #(instance? clojure.lang.Atom %))
                :errors nil
                :results nil
                :timeout 60000
                :print #{:print-throw :print-failed :print-timeout :print-bulk}}))

(fact "with-new-context binds dynamic variables"
  (context/with-new-context {:eval-mode true}
    context/*eval-mode*) => true

  (context/with-new-context {:print #{:a}}
    context/*print*) => #{:a})

(fact "check default values"
  context/*eval-fact* => false
  context/*eval-mode* => true ;; defonce value
  context/*print* => #{:print-throw :print-failed :print-timeout :print-bulk})


^{:refer code.test.base.context/new-context :added "4.1"}
(fact "TODO")

^{:refer code.test.base.context/with-new-context :added "4.1"}
(fact "TODO")