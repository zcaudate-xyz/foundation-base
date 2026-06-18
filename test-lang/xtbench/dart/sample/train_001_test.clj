(ns xtbench.dart.sample.train-001-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :as proto :refer [defprotocol.xt
                                                       defimpl.xt]]))

(l/script- :dart
  {:require [[xt.lang.spec-base :as xt] [xt.lang.common-protocol :as proto]] :runtime :twostep})

(defprotocol.xt IHello
  (hello-str [impl])
  (hello-prn [impl]))

(defn.xt hello-str-fn
  [impl]
  (return (xt/x:cat "hello " (xt/x:get-key impl "state"))))

(defn.xt hello-prn-fn
  [impl]
  (return (xt/x:cat "prn " (xt/x:get-key impl "state"))))

(defimpl.xt Hello
  [state client schema lookup opts]
  -/IHello
  {-/hello-prn -/hello-prn-fn
   -/hello-str -/hello-str-fn})

(fact "Hello was a function"
  
  (Hello "world" nil nil nil nil)
  => "Hello(\"world\",nil,nil,nil,nil)")
