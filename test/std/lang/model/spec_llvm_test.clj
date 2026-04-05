(ns std.lang.model.spec-llvm-test
  (:use code.test)
  (:require [std.lang.model.spec-llvm :refer :all]))

^{:refer std.lang.model.spec-llvm/tf-define :added "4.1"}
(fact "transforms define forms"
  (tf-define (list 'define 'i32 (symbol "@add") ['i32 '%a 'i32 '%b]
                   '(ret i32 %a)))
  => '(:- "define" i32 "@add(i32 %a, i32 %b)"
          (:- "{"
              (\\ \\ (\| (do (ret i32 %a))))
              (:- "\n}"))))

^{:refer std.lang.model.spec-llvm/tf-declare :added "4.1"}
(fact "transforms declare forms"
  (tf-declare (list 'declare 'i32 (symbol "@puts") ['i8*]))
  => '(:- "declare" i32 "@puts(i8*)"))

^{:refer std.lang.model.spec-llvm/tf-label :added "4.1"}
(fact "transforms labels"
  (tf-label '(label %entry))
  => '(:- "%entry:"))

^{:refer std.lang.model.spec-llvm/tf-ret :added "4.1"}
(fact "transforms ret instructions"
  [(tf-ret '(ret void))
   (tf-ret '(ret i32 %value))]
  => ['(:- "ret" void)
      '(:- "ret" i32 %value)])

^{:refer std.lang.model.spec-llvm/tf-assign :added "4.1"}
(fact "transforms assignments"
  (tf-assign '(:= %out (call i32 @next [])))
  => '(:- %out "=" (call i32 @next [])))

^{:refer std.lang.model.spec-llvm/tf-inst-bin :added "4.1"}
(fact "transforms binary instructions"
  ((tf-inst-bin "add") '(add i32 %a %b))
  => '(:- "add" i32 "%a," %b))

^{:refer std.lang.model.spec-llvm/tf-icmp :added "4.1"}
(fact "transforms icmp instructions"
  (tf-icmp '(icmp eq i32 %a %b))
  => '(:- "icmp" eq i32 "%a," %b))

^{:refer std.lang.model.spec-llvm/tf-br :added "4.1"}
(fact "transforms branch instructions"
  [(tf-br '(br %exit))
   (tf-br '(br %cond %then %else))]
  => ['(:- "br" "label" %exit)
      '(:- "br" "i1" "%cond," "label %then," "label %else")])

^{:refer std.lang.model.spec-llvm/tf-call :added "4.1"}
(fact "transforms call instructions"
  (tf-call (list 'call 'i32 (symbol "@puts") [['i8* '%msg] ['i32 '%len]]))
  => '(:- "call" i32 "@puts(i8* %msg, i32 %len)"))

^{:refer std.lang.model.spec-llvm/tf-alloca :added "4.1"}
(fact "transforms alloca instructions"
  [(tf-alloca '(alloca i32))
   (tf-alloca '(alloca i32 [i32 4]))]
  => ['(:- "alloca" i32)
      '(:- "alloca" i32 "," i32 4)])

^{:refer std.lang.model.spec-llvm/tf-store :added "4.1"}
(fact "transforms store instructions"
  (tf-store '(store i32 %value i32* %ptr))
  => '(:- "store" i32 "%value," i32* %ptr))

^{:refer std.lang.model.spec-llvm/tf-load :added "4.1"}
(fact "transforms load instructions"
  (tf-load '(load i32 i32* %ptr))
  => '(:- "load" "i32," i32* %ptr))
