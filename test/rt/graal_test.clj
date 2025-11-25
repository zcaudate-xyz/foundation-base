(ns rt.graal-test
  (:use code.test)
  (:require [rt.graal :refer :all]
            [std.lang :as  l]
            [std.lib :as h])
  (:import (org.graalvm.polyglot Context)))

(l/script- :js
  {:runtime :graal
   :require [[xt.lang.base-lib :as k]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer rt.graal/eval-raw.test :adopt true :added "3.0"}
(fact "EVAL"

  (!.js
   (k/add 1 2))
  => 3
  
  (!.js (+ 1 2 3))
  => 6
  
  (!.js (+ "1" "2"))
  => "12"

  
  (k/add 9 3)
  => 12

  (k/sub 3 9)
  => -6)

^{:refer rt.graal/add-resource-path :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "adds resource path to context"

  (add-resource-path +js+ "assets")
  => +js+)

^{:refer rt.graal/add-system-path :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "adds system path to context"

  (add-system-path +js+ ".")
  => +js+)

^{:refer rt.graal/make-raw :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "creates the base graal context"

  (str (.eval ^Context +js+ "js" "1 + 1"))
  => "2")

^{:refer rt.graal/close-raw :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "closes the base graal context"
  (close-raw +js+)
  => nil)

^{:refer rt.graal/raw-lang :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "gets the language context"

  (raw-lang +js+)
  => :js)

^{:refer rt.graal/eval-raw :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "performs an exec expression"

  (str (eval-raw +js+ "1 + 1"))
  => "2")

^{:refer rt.graal/eval-graal :added "4.0"}
(fact "evals body in the runtime"

  (str (eval-graal (l/rt :js)
                   "1+1"))
  => "2")

^{:refer rt.graal/invoke-graal :added "4.0"}
(fact "invokes a pointer in the runtime"

  (invoke-graal (l/rt :js)
                k/sub
                [1 2])
  => -1)

^{:refer rt.graal/start-graal :added "3.0"}
(fact "starts the graal runtime"
  (start-graal (rt-graal:create {:lang :js}))
  => rt-graal?)

^{:refer rt.graal/stop-graal :added "3.0"}
(fact "stops the graal runtime"
  (stop-graal (start-graal (rt-graal:create {:lang :js})))
  => rt-graal?)

^{:refer rt.graal/rt-graal:create :added "4.0"}
(fact "creates a graal runtime"

  (h/-> (rt-graal:create {:lang :js})
        (h/start)
        (h/stop))
  => rt-graal?)

^{:refer rt.graal/rt-graal :added "3.0"}
(fact "creates and starts a graal runtime"
  (rt-graal {:lang :js})
  => rt-graal?)

^{:refer rt.graal/rt-graal? :added "3.0"}
(fact "checks that object is a graal runtime"
  (rt-graal? (rt-graal:create {:lang :js}))
  => true)
