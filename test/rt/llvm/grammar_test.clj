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

^{:refer rt.llvm.grammar/tf-define :added "4.1"}
(fact "transforms llvm define"
  (l/emit-as :llvm
    ['(define i32 main [i32 %argc ptr %argv]
      (label entry)
      (:= %1 (add i32 %argc 1))
      (ret i32 %1))])
  => "define i32 main(i32 %argc, ptr %argv) { \n  entry:\n  %1 = add i32 %argc, 1\n  ret i32 %1 \n}")

^{:refer rt.llvm.grammar/tf-declare :added "4.1"}
(fact "transforms llvm declare"
  (l/emit-as :llvm
             ['(declare i32 printf [ptr i32])])
  
  => "declare i32 printf(ptr, i32)")

^{:refer rt.llvm.grammar/tf-alloca :added "4.1"}
(fact "memory ops"
  (l/emit-as :llvm
    ['(define i32 test_mem []
      (label entry)
      (:= %ptr (alloca i32))
      (store i32 42 ptr %ptr)
      (:= %val (load i32 ptr %ptr))
      (ret i32 %val))])
  => "define i32 test_mem() { \n  entry:\n  %ptr = alloca i32\n  store i32 42, ptr %ptr\n  %val = load i32, ptr %ptr\n  ret i32 %val \n}")

^{:refer rt.llvm.grammar/tf-label :added "4.1"}
(fact "transforms label"
  (l/emit-as :llvm
   ['(label entry)])
  => "entry:")

^{:refer rt.llvm.grammar/tf-ret :added "4.1"}
(fact "transforms ret"
  (l/emit-as :llvm
   ['(ret i32 0)])
  => "ret i32 0"

^{:refer rt.llvm.grammar/tf-call :added "4.1"}
(fact "call"
  (l/emit-as :llvm
    ['(declare i32 printf [ptr i32])

     '(define i32 call_test []
        (label entry)
        (call i32 printf [[ptr str] [i32 123]])
        (ret i32 0))])
  => "declare i32 printf(ptr, i32)\n\ndefine i32 call_test() { \n  entry:\n  call i32 printf(ptr str, i32 123)\n  ret i32 0 \n}")

^{:refer rt.llvm.grammar/tf-declare :added "4.1"}
(fact "transforms llvm declare"
  (l/emit-as :llvm
   ['(declare i32 printf [ptr i32])])
  => "declare i32 printf(ptr, i32)")

^{:refer rt.llvm.grammar/tf-label :added "4.1"}
(fact "transforms label"
  (l/emit-as :llvm
   ['(label entry)])
  => "entry:")

^{:refer rt.llvm.grammar/tf-ret :added "4.1"}
(fact "transforms ret"
  (l/emit-as :llvm
   ['(ret i32 0)])
  => "ret i32 0")
  (l/emit-as :llvm
   ['(ret void)])
  => "ret void")

^{:refer rt.llvm.grammar/tf-assign :added "4.1"}
(fact "transforms assignment"
  (l/emit-as :llvm
   ['(:= %1 2)])
  => "%1 = 2")

^{:refer rt.llvm.grammar/tf-inst-bin :added "4.1"}
(fact "helper for binary instructions"
  (l/emit-as :llvm
   ['(add i32 1 2)])
  => "add i32 1, 2")

^{:refer rt.llvm.grammar/tf-icmp :added "4.1"}
(fact "transforms icmp"
  (l/emit-as :llvm
   ['(icmp eq i32 %a %b)])
  => "icmp eq i32 %a, %b")

^{:refer rt.llvm.grammar/tf-br :added "4.1"}
(fact "transforms br"
  (l/emit-as :llvm
   ['(br %label)])
  => "br label %label"

  (l/emit-as :llvm
   ['(br %cond %label_true %label_false)])
  => "br i1 %cond, label %label_true, label %label_false")

^{:refer rt.llvm.grammar/tf-call :added "4.1"}
(fact "transforms call"
  (l/emit-as :llvm
   ['(call i32 printf [[ptr str] [i32 123]])])
  => "call i32 printf(ptr str, i32 123)")

^{:refer rt.llvm.grammar/tf-alloca :added "4.1"}
(fact "transforms alloca"
  (l/emit-as :llvm
   ['(alloca i32)])
  => "alloca i32")

^{:refer rt.llvm.grammar/tf-store :added "4.1"}
(fact "transforms store"
  (l/emit-as :llvm
   ['(store i32 42 ptr %ptr)])
  => "store i32 42, ptr %ptr")

^{:refer rt.llvm.grammar/tf-load :added "4.1"}
(fact "transforms load"
  (l/emit-as :llvm
   ['(load i32 ptr %ptr)])
  => "load i32, ptr %ptr")
