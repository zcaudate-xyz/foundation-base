(ns rt.basic.type-twostep-gcc-test
  (:use code.test)
  (:require [rt.basic.type-common :as common]
            [std.lang :as l]))

(l/script- :c
  {:runtime :twostep})

(l/script- :rust
  {:runtime :twostep})

(def CANARY-GCC
  (common/program-exists? "gcc"))

(def CANARY-RUSTC
  (common/program-exists? "rustc"))

;;
;; TODO: ADD a c-style function
;;


(fact "can return a value"
  (if CANARY-GCC
    (!.c
      (+ 1 2 3))
    :gcc-unavailable)
  => (any 6
           :gcc-unavailable))


;;
;; TODO: ADD a rust-style function
;;

(fact "can return a value"
  (if CANARY-RUSTC
    (!.rs
      (+ 1 2 3))
    :rustc-unavailable)
  => (any 6
           :rustc-unavailable))
