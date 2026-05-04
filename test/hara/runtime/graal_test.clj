(ns hara.runtime.graal-test
  (:require [hara.runtime.graal :refer :all]
            [hara.lang :as l]
            [std.lib.component :as component]
            [std.lib.foundation :as f])
  (:use code.test)
  (:import (org.graalvm.polyglot Context)))

(l/script- :js
  {:runtime :graal
   :require [[xt.lang.common-lib :as k]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer hara.runtime.graal/eval-raw.test :adopt true :added "3.0"}
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

^{:refer hara.runtime.graal/add-resource-path :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "adds resource path to context"

  (add-resource-path +js+ "assets")
  => +js+)

^{:refer hara.runtime.graal/add-system-path :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "adds system path to context"

  (add-system-path +js+ ".")
  => +js+)

^{:refer hara.runtime.graal/make-raw :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "creates the base graal context"

  (str (.eval ^Context +js+ "js" "1 + 1"))
  => "2")

^{:refer hara.runtime.graal/close-raw :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "closes the base graal context"
  (close-raw +js+)
  => nil)

^{:refer hara.runtime.graal/raw-lang :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "gets the language context"

  (raw-lang +js+)
  => :js)

^{:refer hara.runtime.graal/eval-raw :added "3.0"
  :setup [(def +js+ (make-raw {:lang :js}))]}
(fact "performs an exec expression"

  (str (eval-raw +js+ "1 + 1"))
  => "2")

^{:refer hara.runtime.graal/unwrap :added "4.1"}
(fact "unwraps polyglot values to Clojure primitives"

  (let [ctx (make-raw {:lang :js})]
    [(unwrap (.eval ^Context ctx "js" "\"hello\""))
     (unwrap (.eval ^Context ctx "js" "42"))
     (unwrap (.eval ^Context ctx "js" "3.14"))
     (unwrap (.eval ^Context ctx "js" "true"))
     (unwrap (.eval ^Context ctx "js" "null"))])
  => ["hello" 42 3.14 true nil])

^{:refer hara.runtime.graal/eval-graal :added "4.0"}
(fact "evals body in the runtime"

  (str (eval-graal (l/rt :js)
                   "1+1"))
  => "2")

^{:refer hara.runtime.graal/invoke-graal :added "4.0"}
(fact "invokes a pointer in the runtime"

  (invoke-graal (l/rt :js)
                k/sub
                [1 2])
  => -1)

^{:refer hara.runtime.graal/start-graal :added "3.0"}
(fact "starts the graal runtime"
  (start-graal (rt-graal:create {:lang :js}))
  => rt-graal?)

^{:refer hara.runtime.graal/stop-graal :added "3.0"}
(fact "stops the graal runtime"
  (stop-graal (start-graal (rt-graal:create {:lang :js})))
  => rt-graal?)

^{:refer hara.runtime.graal/rt-graal:create :added "4.0"}
(fact "creates a graal runtime"

  (f/-> (rt-graal:create {:lang :js})
        (component/start)
        (component/stop))
  => rt-graal?)

^{:refer hara.runtime.graal/rt-graal :added "3.0"}
(fact "creates and starts a graal runtime"
  (rt-graal {:lang :js})
  => rt-graal?)

^{:refer hara.runtime.graal/rt-graal? :added "3.0"}
(fact "checks that object is a graal runtime"
  (rt-graal? (rt-graal:create {:lang :js}))
  => true)