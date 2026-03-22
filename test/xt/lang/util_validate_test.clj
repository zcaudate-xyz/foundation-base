(ns xt.lang.util-validate-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.util-validate :as validate]
             [js.core :as j]]})

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.util-validate :as validate]
             [lua.nginx :as n]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-validate/validate-step :added "4.0"}
(fact "validates a single step"
  ^:hidden
  
  (notify/wait-on :js
    (var data {:first "hello"})
    (var guards [["is-not-empty" {:message "Must not be empty"
                                  :check (fn:> [v rec] (and (k/not-nil? v)
                                                            (< 0 (k/len v))))}]])
    (var result {:fields {:first {:status "pending"}}})
    (validate/validate-step data "first" guards 0 result nil (fn [success result]
                                                               (repl/notify result))))
  => {"fields" {"first" {"status" "ok"}}}

  (notify/wait-on :lua
    (var data {:first "hello"})
    (var guards [["is-not-empty" {:message "Must not be empty"
                                  :check (fn:> [v rec] (and (k/not-nil? v)
                                                            (< 0 (k/len v))))}]])
    (var result {:fields {:first {:status "pending"}}})
    (validate/validate-step data "first" guards 0 result nil (fn [success result]
                                                               (repl/notify result))))
  => {"fields" {"first" {"status" "ok"}}})

^{:refer xt.lang.util-validate/validate-field :added "4.0"}
(fact "validates a single field"
  ^:hidden
  
  ;;
  ;; SINGLE ENTRY
  ;;
  (notify/wait-on :js
    (var data {:first "hello"})
   (var validators {:first    [["is-not-empty" {:message "Must not be empty"
                                                :check (fn:> [v rec] (and (k/not-nil? v)
                                                                          (< 0 (k/len v))))}]]})
   (var result (validate/create-result validators))
   (-> (validate/validate-field data
                                "first"
                                validators
                                result
                                nil
                                (fn [success result]
                                  (repl/notify result)))))
  => {"status" "pending", "fields" {"first" {"status" "ok"}}, "::" "validation.result"}

  (notify/wait-on :lua
   (var data {:first "hello"})
   (var validators {:first    [["is-not-empty" {:message "Must not be empty"
                                                :check (fn:> [v rec] (and (k/not-nil? v)
                                                                          (< 0 (k/len v))))}]]})
   (var result (validate/create-result validators))
   (-> (validate/validate-field data
                                "first"
                                validators
                                result
                                nil
                                (fn [success result]
                                  (repl/notify result)))))
  => {"status" "pending", "fields" {"first" {"status" "ok"}}, "::" "validation.result"}

  ;;
  ;; MULTI ENTRY
  ;;
  (notify/wait-on :js
   (var data {:first "hello"})
   (var validators {:first    [["is-not-empty1" {:message "Must not be empty"
                                                 :check (fn:> [v rec] (and (k/not-nil? v)
                                                                           (< 0 (k/len v))))}]
                               ["is-not-empty2" {:message "Must not be empty"
                                                 :check (fn:> [v rec]
                                                              (j/future-delayed [100]
                                                                (return (and (k/not-nil? v)
                                                                            (< 0 (k/len v))))))}]
                               ["is-not-empty3" {:message "Must not be empty"
                                                 :check (fn:> [v rec]
                                                              (j/future-delayed [100]
                                                                (return (and (k/not-nil? v)
                                                                             (< 0 (k/len v))))))}]]})
   (var result (validate/create-result validators))
   (-> (validate/validate-field data
                                "first"
                                validators
                                result
                                nil
                                (fn [success result]
                                  (repl/notify result)))))
  => {"status" "pending", "fields" {"first" {"status" "ok"}}, "::" "validation.result"}

  (notify/wait-on :lua
   (var data {:first "hello"})
   (var validators {:first    [["is-not-empty1" {:message "Must not be empty"
                                                 :check (fn [v rec]
                                                          (return (and (k/not-nil? v)
                                                                       (< 0 (k/len v)))))}]
                               ["is-not-empty2" {:message "Must not be empty"
                                                 :check (fn [v rec]
                                                          (n/sleep 0.1)
                                                          (return (and (k/not-nil? v)
                                                                       (< 0 (k/len v)))))}]
                               ["is-not-empty3" {:message "Must not be empty"
                                                 :check (fn [v rec]
                                                          (n/sleep 0.1)
                                                          (return (and (k/not-nil? v)
                                                                       (< 0 (k/len v)))))}]]})
   (var result (validate/create-result validators))
   (-> (validate/validate-field data
                                "first"
                                validators
                                result
                                nil
                                (fn [success result]
                                  (repl/notify result)))))
  => {"status" "pending", "fields" {"first" {"status" "ok"}}, "::" "validation.result"}
  
  ;;
  ;; ERROR ENTRY
  ;;
  
  (notify/wait-on :js
   (var data {:first "hello"})
   (var validators {:first  [["is-not-empty0" {:message "Must not be empty"
                                               :check (fn:> [v rec]
                                                            (j/future-delayed [100]
                                                              (return (and (k/not-nil? v)
                                                                           (< 0 (k/len v))))))}]
                             ["is-not-empty1" {:message "Must not be empty"
                                               :check (fn:> [v rec] false)}]]})
   (var result (validate/create-result validators))
   (-> (validate/validate-field data
                                "first"
                                validators
                                result
                                nil
                                (fn [success result]
                                  (repl/notify result)))))
  => {"status" "errored",
      "fields" {"first" {"message" "Must not be empty",
                         "id" "is-not-empty1", "status"
                         "errored", "data" "hello"}},
      "::" "validation.result"}

  (notify/wait-on :lua
   (var data {:first "hello"})
   (var validators {:first  [["is-not-empty0" {:message "Must not be empty"
                                               :check (fn [v rec]
                                                        (n/sleep 0.1)
                                                        (return (and (k/not-nil? v)
                                                                     (< 0 (k/len v)))))}]
                             ["is-not-empty1" {:message "Must not be empty"
                                               :check (fn:> [v rec] false)}]]})
   (var result (validate/create-result validators))
   (-> (validate/validate-field data
                                "first"
                                validators
                                result
                                nil
                                (fn [success result]
                                  (repl/notify result)))))
  => {"status" "errored",
      "fields" {"first" {"message" "Must not be empty",
                         "id" "is-not-empty1", "status"
                         "errored", "data" "hello"}},
      "::" "validation.result"})


^{:refer xt.lang.util-validate/validate-all :added "4.0"}
(fact "validates all data"
  ^:hidden
  
  (notify/wait-on :js
   (var data {:first "hello"
              :last "hello"
              :email "hello"})
   (var validators
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
   (:= (!:G result) (validate/create-result validators))
   (validate/validate-all data validators result
                          nil
                          (fn [success result]
                            (repl/notify result))))
  => {"status" "ok",
      "fields" {"email" {"status" "ok"},
                "last" {"status" "ok"},
                "first" {"status" "ok"}},
      "::" "validation.result"}

  (notify/wait-on :lua
   (var data {:first "hello"
              :last "hello"
              :email "hello"})
   (var validators
        {:first    [["is-not-empty" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (return (and (k/not-nil? v)
                                                           (< 0 (k/len v)))))}]]
         :last     [["is-not-empty" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (n/sleep 0.1)
                                              (return (and (k/not-nil? v)
                                                           (< 0 (k/len v)))))}]]
         :email    [["is-not-empty" {:message "Must not be empty"
                                     :check (fn [v rec]
                                              (n/sleep 0.1)
                                              (return (and (k/not-nil? v)
                                                           (< 0 (k/len v)))))}]]})
   (:= (!:G result) (validate/create-result validators))
   (validate/validate-all data validators result
                          nil
                          (fn [success result]
                            (repl/notify result))))
  => {"status" "ok",
      "fields" {"email" {"status" "ok"},
                "last" {"status" "ok"},
                "first" {"status" "ok"}},
      "::" "validation.result"})

^{:refer xt.lang.util-validate/create-result :added "4.0"}
(fact "creates a result datastructure")
