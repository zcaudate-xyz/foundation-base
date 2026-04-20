(ns
 xtbench.js.lang.util-validate-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :js
 {:runtime :basic,
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.common-spec :as xt]
   [xt.lang.common-repl :as repl]
   [xt.lang.util-validate :as validate]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-validate/validate-step, :added "4.0"}
(fact
 "validates a single step"
 ^{:hidden true}
 (notify/wait-on
  :js
  (var data {:first "hello"})
  (var
   guards
   [["is-not-empty"
     {:message "Must not be empty",
      :check (fn:> [v rec] (and (k/not-nil? v) (< 0 (xt/x:len v))))}]])
  (var result {:fields {:first {:status "pending"}}})
  (validate/validate-step
   data
   "first"
   guards
   0
   result
   nil
   (fn [success result] (repl/notify result))))
 =>
 {"fields" {"first" {"status" "ok"}}})

^{:refer xt.lang.util-validate/validate-field, :added "4.0"}
(fact
 "validates a single field"
 ^{:hidden true}
 (notify/wait-on
  :js
  (var data {:first "hello"})
  (var
   validators
   {:first
    [["is-not-empty"
      {:message "Must not be empty",
       :check
       (fn:> [v rec] (and (k/not-nil? v) (< 0 (xt/x:len v))))}]]})
  (var result (validate/create-result validators))
  (->
   (validate/validate-field
    data
    "first"
    validators
    result
    nil
    (fn [success result] (repl/notify result)))))
 =>
 {"status" "pending",
  "fields" {"first" {"status" "ok"}},
  "::" "validation.result"}
 (notify/wait-on
  :js
  (var data {:first "hello"})
  (var
   validators
   {:first
    [["is-not-empty1"
      {:message "Must not be empty",
       :check (fn:> [v rec] (and (k/not-nil? v) (< 0 (xt/x:len v))))}]
     ["is-not-empty2"
      {:message "Must not be empty",
       :check
       (fn:>
        [v rec]
        (xt/x:with-delay
         (fn:> [] (and (k/not-nil? v) (< 0 (xt/x:len v))))
         100))}]
     ["is-not-empty3"
      {:message "Must not be empty",
       :check
       (fn:>
        [v rec]
        (xt/x:with-delay
         (fn:> [] (and (k/not-nil? v) (< 0 (xt/x:len v))))
         100))}]]})
  (var result (validate/create-result validators))
  (->
   (validate/validate-field
    data
    "first"
    validators
    result
    nil
    (fn [success result] (repl/notify result)))))
 =>
 {"status" "pending",
  "fields" {"first" {"status" "ok"}},
  "::" "validation.result"}
 (notify/wait-on
  :js
  (var data {:first "hello"})
  (var
   validators
   {:first
    [["is-not-empty0"
      {:message "Must not be empty",
       :check
       (fn:>
        [v rec]
        (xt/x:with-delay
         (fn:> [] (and (k/not-nil? v) (< 0 (xt/x:len v))))
         100))}]
     ["is-not-empty1"
      {:message "Must not be empty", :check (fn:> [v rec] false)}]]})
  (var result (validate/create-result validators))
  (->
   (validate/validate-field
    data
    "first"
    validators
    result
    nil
    (fn [success result] (repl/notify result)))))
 =>
 {"status" "errored",
  "fields"
  {"first"
   {"message" "Must not be empty",
    "id" "is-not-empty1",
    "status" "errored",
    "data" "hello"}},
  "::" "validation.result"})

^{:refer xt.lang.util-validate/validate-all, :added "4.0"}
(fact
 "validates all data"
 ^{:hidden true}
 (notify/wait-on
  :js
  (var data {:first "hello", :last "hello", :email "hello"})
  (var
   validators
   {:first
    [["is-not-empty"
      {:message "Must not be empty",
       :check
       (fn:> [v rec] (and (k/not-nil? v) (< 0 (xt/x:len v))))}]],
    :last
    [["is-not-empty"
      {:message "Must not be empty",
       :check
       (fn:>
        [v rec]
        (xt/x:with-delay
         (fn:> [] (and (k/not-nil? v) (< 0 (xt/x:len v))))
         100))}]],
    :email
    [["is-not-empty"
      {:message "Must not be empty",
       :check
       (fn:>
        [v rec]
        (xt/x:with-delay
         (fn:> [] (and (k/not-nil? v) (< 0 (xt/x:len v))))
         100))}]]})
  (:= (!:G result) (validate/create-result validators))
  (validate/validate-all
   data
   validators
   result
   nil
   (fn [success result] (repl/notify result))))
 =>
 {"status" "ok",
  "fields"
  {"email" {"status" "ok"},
   "last" {"status" "ok"},
   "first" {"status" "ok"}},
  "::" "validation.result"})
