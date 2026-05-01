(ns xt.event.util-validate-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.event.util-validate :as validate]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.event.util-validate :as validate]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.event.util-validate :as validate]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-promise/x:promise-run :added "4.1"}
(fact "wraps raw values through the promise interface"

  (!.js
   (xt/x:is-function? spec-promise/x:promise-run))
  => true

  (!.lua
   (xt/x:is-function? spec-promise/x:promise-run))
  => true

  (!.py
   (xt/x:is-function? spec-promise/x:promise-run))
  => true)

^{:refer xt.event.util-validate/validate-step :added "4.1"}
(fact "validates a single guard and marks the field"

  (notify/wait-on :js
    (var data {:first "hello"})
    (var guards [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return (and (xt/x:not-nil? v)
                                                        (< 0 (xt/x:len v)))))}]])
    (var result {:fields {:first {:status "pending"}}})
    (validate/validate-step data "first" guards 0 result nil
                            (fn [success out]
                              (repl/notify out))))
  => {"fields" {"first" {"status" "ok"}}}

  (notify/wait-on :lua
    (var data {:first "hello"})
    (var guards [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return (and (xt/x:not-nil? v)
                                                        (< 0 (xt/x:len v)))))}]])
    (var result {:fields {:first {:status "pending"}}})
    (validate/validate-step data "first" guards 0 result nil
                            (fn [success out]
                              (repl/notify out))))
  => {"fields" {"first" {"status" "ok"}}}

  (notify/wait-on :python
    (var data {:first "hello"})
    (var guards [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return (and (xt/x:not-nil? v)
                                                        (< 0 (xt/x:len v)))))}]])
    (var result {:fields {:first {:status "pending"}}})
    (validate/validate-step data "first" guards 0 result nil
                            (fn [success out]
                              (repl/notify out))))
  => {"fields" {"first" {"status" "ok"}}})

^{:refer xt.event.util-validate/validate-field :added "4.1"}
(fact "validates a field through ordered guards"

  (notify/wait-on :js
    (var data {:first "hello"})
    (var check-async
         (fn [v rec]
           (return
            (spec-promise/x:with-delay
             50
             (fn []
               (return (and (xt/x:not-nil? v)
                            (< 0 (xt/x:len v)))))))))
    (var validators
         {:first [["is-not-empty-1" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (return (and (xt/x:not-nil? v)
                                                           (< 0 (xt/x:len v)))))}]
                  ["is-not-empty-2" {:message "Must not be empty"
                                     :check check-async}]]})
    (var result (validate/create-result validators))
    (validate/validate-field data "first" validators result nil
                             (fn [success result]
                               (repl/notify result))))
  => {"::" "validation.result"
      "status" "pending"
      "fields" {"first" {"status" "ok"}}}

  (notify/wait-on :js
    (var data {:first "hello"})
    (var validators
         {:first [["is-not-empty-1" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (return true))}]
                  ["is-not-empty-2" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (return false))}]]})
    (var result (validate/create-result validators))
    (validate/validate-field data "first" validators result nil
                             (fn [success result]
                               (repl/notify result))))
  => {"::" "validation.result"
      "status" "errored"
      "fields" {"first" {"status" "errored"
                         "id" "is-not-empty-2"
                         "data" "hello"
                         "message" "Must not be empty"}}}

  (notify/wait-on :lua
    (var data {:first "hello"})
    (var check-async
         (fn [v rec]
           (return
            (spec-promise/x:with-delay
             50
             (fn []
               (return (and (xt/x:not-nil? v)
                            (< 0 (xt/x:len v)))))))))
    (var validators
         {:first [["is-not-empty-1" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (return (and (xt/x:not-nil? v)
                                                           (< 0 (xt/x:len v)))))}]
                  ["is-not-empty-2" {:message "Must not be empty"
                                     :check check-async}]]})
    (var result (validate/create-result validators))
    (validate/validate-field data "first" validators result nil
                             (fn [success result]
                               (repl/notify result))))
  => {"::" "validation.result"
      "status" "pending"
      "fields" {"first" {"status" "ok"}}}

  (notify/wait-on :lua
    (var data {:first "hello"})
    (var validators
         {:first [["is-not-empty-1" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (return true))}]
                  ["is-not-empty-2" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (return false))}]]})
    (var result (validate/create-result validators))
    (validate/validate-field data "first" validators result nil
                             (fn [success result]
                               (repl/notify result))))
  => {"::" "validation.result"
      "status" "errored"
      "fields" {"first" {"status" "errored"
                         "id" "is-not-empty-2"
                         "data" "hello"
                         "message" "Must not be empty"}}}

  (notify/wait-on :python
    (var data {:first "hello"})
    (var check-async
         (fn [v rec]
           (return
            (spec-promise/x:with-delay
             50
             (fn []
               (return (and (xt/x:not-nil? v)
                            (< 0 (xt/x:len v)))))))))
    (var validators
         {:first [["is-not-empty-1" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (return (and (xt/x:not-nil? v)
                                                           (< 0 (xt/x:len v)))))}]
                  ["is-not-empty-2" {:message "Must not be empty"
                                     :check check-async}]]})
    (var result (validate/create-result validators))
    (validate/validate-field data "first" validators result nil
                             (fn [success result]
                               (repl/notify result))))
  => {"::" "validation.result"
      "status" "pending"
      "fields" {"first" {"status" "ok"}}}

  (notify/wait-on :python
    (var data {:first "hello"})
    (var validators
         {:first [["is-not-empty-1" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (return true))}]
                  ["is-not-empty-2" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (return false))}]]})
    (var result (validate/create-result validators))
    (validate/validate-field data "first" validators result nil
                             (fn [success result]
                               (repl/notify result))))
  => {"::" "validation.result"
      "status" "errored"
      "fields" {"first" {"status" "errored"
                         "id" "is-not-empty-2"
                         "data" "hello"
                         "message" "Must not be empty"}}})

^{:refer xt.event.util-validate/validate-fields-loop :added "4.1"}
(fact "walks fields through a single validation promise chain"

  (notify/wait-on :js
    (var data {:first "hello"
               :last "world"})
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return true))}]]})
    (var result (validate/create-result validators))
    (validate/validate-fields-loop data validators result ["first" "last"] 0 nil
                                   (fn [success out]
                                     (repl/notify out))))
  => {"::" "validation.result"
      "status" "ok"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "ok"}}}

  (notify/wait-on :js
    (var data {:first "hello"
               :last ""})
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return false))}]]})
    (var result (validate/create-result validators))
    (validate/validate-fields-loop data validators result ["first" "last"] 0 nil
                                   (fn [success out]
                                     (repl/notify out))))
  => {"::" "validation.result"
      "status" "errored"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "errored"
                        "id" "is-not-empty"
                        "data" ""
                        "message" "Must not be empty"}}}

  (notify/wait-on :lua
    (var data {:first "hello"
               :last "world"})
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return true))}]]})
    (var result (validate/create-result validators))
    (validate/validate-fields-loop data validators result ["first" "last"] 0 nil
                                   (fn [success out]
                                     (repl/notify out))))
  => {"::" "validation.result"
      "status" "ok"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "ok"}}}

  (notify/wait-on :lua
    (var data {:first "hello"
               :last ""})
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return false))}]]})
    (var result (validate/create-result validators))
    (validate/validate-fields-loop data validators result ["first" "last"] 0 nil
                                   (fn [success out]
                                     (repl/notify out))))
  => {"::" "validation.result"
      "status" "errored"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "errored"
                        "id" "is-not-empty"
                        "data" ""
                        "message" "Must not be empty"}}}

  (notify/wait-on :python
    (var data {:first "hello"
               :last "world"})
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return true))}]]})
    (var result (validate/create-result validators))
    (validate/validate-fields-loop data validators result ["first" "last"] 0 nil
                                   (fn [success out]
                                     (repl/notify out))))
  => {"::" "validation.result"
      "status" "ok"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "ok"}}}

  (notify/wait-on :python
    (var data {:first "hello"
               :last ""})
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return false))}]]})
    (var result (validate/create-result validators))
    (validate/validate-fields-loop data validators result ["first" "last"] 0 nil
                                   (fn [success out]
                                     (repl/notify out))))
  => {"::" "validation.result"
      "status" "errored"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "errored"
                        "id" "is-not-empty"
                        "data" ""
                        "message" "Must not be empty"}}})

^{:refer xt.event.util-validate/validate-all :added "4.1"}
(fact "validates all fields through a single promise chain"

  (notify/wait-on :js
    (var data {:first "hello"
               :last "world"})
    (var check-async
         (fn [v rec]
           (return
            (spec-promise/x:with-delay
             50
             (fn []
               (return true))))))
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check check-async}]]})
    (var result (validate/create-result validators))
    (validate/validate-all data validators result nil
                           (fn [success result]
                             (repl/notify result))))
  => {"::" "validation.result"
      "status" "ok"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "ok"}}}

  (notify/wait-on :js
    (var data {:first "hello"
               :last ""})
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return false))}]]})
    (var result (validate/create-result validators))
    (validate/validate-all data validators result nil
                           (fn [success result]
                             (repl/notify result))))
  => {"::" "validation.result"
      "status" "errored"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "errored"
                        "id" "is-not-empty"
                        "data" ""
                        "message" "Must not be empty"}}}

  (notify/wait-on :lua
    (var data {:first "hello"
               :last "world"})
    (var check-async
         (fn [v rec]
           (return
            (spec-promise/x:with-delay
             50
             (fn []
               (return true))))))
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check check-async}]]})
    (var result (validate/create-result validators))
    (validate/validate-all data validators result nil
                           (fn [success result]
                             (repl/notify result))))
  => {"::" "validation.result"
      "status" "ok"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "ok"}}}

  (notify/wait-on :lua
    (var data {:first "hello"
               :last ""})
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return false))}]]})
    (var result (validate/create-result validators))
    (validate/validate-all data validators result nil
                           (fn [success result]
                             (repl/notify result))))
  => {"::" "validation.result"
      "status" "errored"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "errored"
                        "id" "is-not-empty"
                        "data" ""
                        "message" "Must not be empty"}}}

  (notify/wait-on :python
    (var data {:first "hello"
               :last "world"})
    (var check-async
         (fn [v rec]
           (return
            (spec-promise/x:with-delay
             50
             (fn []
               (return true))))))
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check check-async}]]})
    (var result (validate/create-result validators))
    (validate/validate-all data validators result nil
                           (fn [success result]
                             (repl/notify result))))
  => {"::" "validation.result"
      "status" "ok"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "ok"}}}

  (notify/wait-on :python
    (var data {:first "hello"
               :last ""})
    (var validators
         {:first [["is-not-empty" {:message "Must not be empty"
                                   :check (fn [v rec]
                                            (return true))}]]
          :last [["is-not-empty" {:message "Must not be empty"
                                  :check (fn [v rec]
                                           (return false))}]]})
    (var result (validate/create-result validators))
    (validate/validate-all data validators result nil
                           (fn [success result]
                             (repl/notify result))))
  => {"::" "validation.result"
      "status" "errored"
      "fields" {"first" {"status" "ok"}
                "last" {"status" "errored"
                        "id" "is-not-empty"
                        "data" ""
                        "message" "Must not be empty"}}})

^{:refer xt.event.util-validate/create-result :added "4.1"}
(fact "creates a pending validation result"

  (!.js
   (validate/create-result {:first [] :last []}))
  => {"::" "validation.result"
      "status" "pending"
      "fields" {"first" {"status" "pending"}
                "last" {"status" "pending"}}}

  (!.lua
   (validate/create-result {:first [] :last []}))
  => {"::" "validation.result"
      "status" "pending"
      "fields" {"first" {"status" "pending"}
                "last" {"status" "pending"}}}

  (!.py
   (validate/create-result {:first [] :last []}))
  => {"::" "validation.result"
      "status" "pending"
      "fields" {"first" {"status" "pending"}
                "last" {"status" "pending"}}})

(comment
  (s/snapto)
  (s/run '[xt.event.util-validate])
  
  (s/seedgen-langremove '[xt.event.util-] {:lang [:lua] :write true})
  
  (s/seedgen-benchadd '[xt.event.util-validate] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.util-validate]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.util-validate]  {:lang [:lua :python] :write true}))
