(ns rt.basic.type-twostep-gcc-test
  (:use code.test)
  (:require [std.lang :as l]))

(l/script- :c
  {:runtime :twostep})

(l/script- :rust
  {:runtime :twostep})

;;
;; TODO: ADD a c-style function
;;


(fact "can return a value"
  
  (!.c
    (+ 1 2 3))
  => 6)


;;
;; TODO: ADD a rust-style function
;;

(fact "can return a value"
  
  (!.rs
    (+ 1 2 3))
  => 6)
