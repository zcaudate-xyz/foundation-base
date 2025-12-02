(ns js.cell.base-util-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.cell.base-util :as util]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.base-util/rand-id :added "4.0" :unchecked true}
(fact "prepares a rand-id"
  ^:hidden
  
  (util/rand-id "" 4)
  => string?

  (util/rand-id "id-" 4)
  => string?)

^{:refer js.cell.base-util/check-event :added "4.0" :unchecked true}
(fact "checks that trigger matches topic and event"
  ^:hidden
  
  ;; nil
  (util/check-event nil "ANY" {})
  => true
  
  ;; boolean
  (util/check-event true "ANY" {})
  => true
  
  (util/check-event false "ANY" {})
  => false

  ;; string
  (util/check-event "hello" "hello" {})
  => true

  (util/check-event "hello" "WRONG" {})
  => false

  ;; fn
  (!.js
   (util/check-event (fn:> [topic] (== topic "hello")) "hello" {}))
  => true

  (!.js
   (util/check-event (fn:> [topic] (== topic "hello")) "WRONG" {}))
  => false

  ;; map boolean
  (util/check-event {"hello" true} "hello" {})
  => true

  (util/check-event {"hello" true} "WRONG" {})
  => false

  ;; map pred
  (!.js
   (util/check-event {"hello" (fn:> [e] (. e ["ok"]))} "hello" {:ok true}))
  => true

  (!.js
   (util/check-event {"hello" (fn:> [e] (. e ["ok"]))} "hello" {:not-ok true}))
  => false)

^{:refer js.cell.base-util/arg-encode :added "4.0" :unchecked true}
(fact "encodes functions in data tree"
  ^:hidden
  
  (!.js
   (util/arg-encode [(fn:> [x] x)]))
  => [["fn" "function (x){\n      return x;\n    }"]])

^{:refer js.cell.base-util/arg-decode :added "4.0" :unchecked true}
(fact "decodes function in data tree"
  ^:hidden

  (!.js
   ((-> [k/identity]
        (util/arg-encode)
        (util/arg-decode)
        (k/first))
    "hello"))
  => "hello")
