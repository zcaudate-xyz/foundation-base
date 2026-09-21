(ns melbourne.base-validators-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :basic
   :config {:layout :flat}
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [melbourne.base-validators :as validators]]
   :export [MODULE]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer melbourne.base-validators/is-true :added "4.0"}
(fact "always true validators"
  ^:hidden
  
  (validators/is-true)
  => ["is-true" {"message" "Always true."}])

^{:refer melbourne.base-validators/is-accepted :added "0.1"}
(fact "creates an is-accepted validator"
  ^:hidden
  
  (validators/is-accepted)
  => ["is-accepted" {"message" "Must be accepted."}])

^{:refer melbourne.base-validators/is-required :added "0.1"}
(fact "creates an is-required validator"
  ^:hidden
  
  (validators/is-required)
  => ["is-required" {"message" "Required field."}])

^{:refer melbourne.base-validators/is-not-empty :added "0.1"}
(fact "creates an is-not-empty validator"
  ^:hidden
  
  (validators/is-not-empty)
  => ["is-not-empty" {"message" "Must not be empty."}])

^{:refer melbourne.base-validators/is-at-most :added "0.1"}
(fact "creates an is-at-most validator"
  ^:hidden
  
  (validators/is-at-most 10)
  => ["is-at-most" {"message" "Must have 10 or less characters."}])

^{:refer melbourne.base-validators/is-at-least :added "0.1"}
(fact "creates an is-at-least validator"
  ^:hidden
  
  (validators/is-at-least 10)
  => ["is-at-least" {"message" "Must have 10 or more characters."}])

^{:refer melbourne.base-validators/is-length-n :added "0.1"}
(fact "creates an is-length-n validator"
  ^:hidden
  
  (validators/is-length-n 10)
  => ["is-length-n" {"message" "Must be 10 characters."}])

^{:refer melbourne.base-validators/is-same-as :added "0.1"}
(fact "creates an is-same-as validator"
  ^:hidden
  
  (validators/is-same-as "hello" "Same as Hello")
  => ["is-same-as" {"message" "Same as Hello"}])

^{:refer melbourne.base-validators/is-valid-email :added "0.1"}
(fact "creates an is-valid-email validator"
  ^:hidden
  
  (validators/is-valid-email)
  => ["is-valid-email" {"message" "Must be a valid email."}])
