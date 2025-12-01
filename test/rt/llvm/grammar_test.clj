(ns rt.llvm.grammar-test
  (:use code.test)
  (:require [rt.llvm.grammar :refer :all]
            [std.lang :as l]
            [std.lib :as h]
            [std.lib.context.registry :as registry]))

;; Register the runtime just in case
(def +init-rt+
  (h/swap-return! registry/*registry*
                  (fn [reg]
                    (let [new-reg (assoc reg :llvm {:context :llvm
                                                    :rt {:default {:key :default
                                                                   :resource :hara/context.rt.null
                                                                   :config {}}}})]
                      [new-reg new-reg]))))

(fact "basic emit tests"
  (l/emit-as :llvm
    ['(define i32 main [i32 %argc ptr %argv]
      (label entry)
      (:= %1 (add i32 %argc 1))
      (ret i32 %1))])
  => "define i32 main(i32 %argc, ptr %argv) { \n  entry:\n  %1 = add i32 %argc, 1\n  ret i32 %1 \n}")

(fact "control flow"
  (l/emit-as :llvm
    ['(define void foo [i1 %cond]
      (label entry)
      (br %cond %label_true %label_false)

      (label label_true)
      (ret void)

      (label label_false)
      (ret void))])
  => "define void foo(i1 %cond) { \n  entry:\n  br i1 %cond , label %label_true , label %label_false\n  label_true:\n  ret void\n  label_false:\n  ret void \n}")

(fact "memory ops"
  (l/emit-as :llvm
    ['(define i32 test_mem []
      (label entry)
      (:= %ptr (alloca i32))
      (store i32 42 ptr %ptr)
      (:= %val (load i32 ptr %ptr))
      (ret i32 %val))])
  => "define i32 test_mem() { \n  entry:\n  %ptr = alloca i32\n  store i32 42, ptr %ptr\n  %val = load i32 , ptr %ptr\n  ret i32 %val \n}")

(fact "icmp"
  (l/emit-as :llvm
    ['(define i1 check [i32 %a i32 %b]
      (label entry)
      (:= %res (icmp eq i32 %a %b))
      (ret i1 %res))])
  => "define i1 check(i32 %a, i32 %b) { \n  entry:\n  %res = icmp eq i32 %a, %b\n  ret i1 %res \n}")

(fact "call"
  (l/emit-as :llvm
    ['(declare i32 printf [ptr i32])

     '(define i32 call_test []
        (label entry)
        (call i32 printf [[ptr str] [i32 123]])
        (ret i32 0))])
  => "declare i32 printf(ptr, i32)\n\ndefine i32 call_test() { \n  entry:\n  call i32 printf(ptr str, i32 123)\n  ret i32 0 \n}")
