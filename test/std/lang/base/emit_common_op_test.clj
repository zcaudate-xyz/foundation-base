(ns std.lang.base.emit-common-op-test
  (:require [std.lang.base.emit-common :as common :refer :all]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit-common/emit-common-loop :adopt true :added "4.0"}
(fact "emit raw ops"

  [:discard]
  (emit-common-loop '(\- A "B" :C)
                   +grammar+
                   {})
  => ""

  [:free]
  (emit-common-loop '(:- A "B" :C)
                   +grammar+
                   {})
  => "A B C"
  
  [:comment]
  (emit-common-loop '(:# (+ 1 2) 2 3 4 5)
                   +grammar+
                   {})
  => "// (+ 1 2) 2 3 4 5"

  [:token]
  (emit-common-loop '(super)
                   +grammar+
                   {})
  => "super()"

  (emit-common-loop '(super)
                   (assoc-in +grammar+ [:reserved 'super :emit] :token)
                   {})
  => "super"
  
  [:pre]
  (emit-common-loop '(not A)
                   +grammar+
                   {})
  => "!A"
  
  (emit-common-loop '(not (+ 1 1))
                   +grammar+
                   {})
  => "!((+ 1 1))"

  [:post]
  (emit-common-loop '(factorial (+ 1 1))
                   (assoc-in +grammar+
                             [:reserved 'factorial]
                             '{:op :factorial, :symbol #{factorial}, :emit :post, :raw "!"})
                   {})
  => "((+ 1 1))!"

  [:infix]
  (emit-common-loop '(+ (+ 1 1))
                 +grammar+
                 {})
  => "((+ 1 1))"

  [:infix-]
  (emit-common-loop '(- (+ 1 1))
                 +grammar+
                 {})
  => "-((+ 1 1))"

  (emit-common-loop '(- (+ 1 1)
                     1)
                 +grammar+
                 {})
  => "((+ 1 1)) - 1"

  [:infix*]
  (emit-common-loop '(/ (+ 1 1))
                 +grammar+
                 {})
  => "1 / ((+ 1 1))"
  
  (emit-common-loop '(/ (+ 1 1)
                       1)
                 +grammar+
                 {})
  => "((+ 1 1)) / 1"

  [:infix-if]
  (emit-common-loop '(:? (+ 1 2) B)
                  +grammar+
                  {})
  => "((+ 1 2)) ? B : nil"
  
  (emit-common-loop '(:? (+ 1 2) B)
                    +grammar+
                    {})
  => "((+ 1 2)) ? B : nil"
   
  (emit-common-loop '(:? (+ 1 2) B C)
                 +grammar+
                 {})
  => "((+ 1 2)) ? B : C"
   
  (emit-common-loop '(:? (+ 1 2) B C)
                 +grammar+
                 {})
  => "((+ 1 2)) ? B : C"

  (emit-common-loop '(:? (+ 1 2) B :else C)
                 +grammar+
                 {})
  => "((+ 1 2)) ? B : C"

  (emit-common-loop '(:? (+ 1 2) B C D)
                 +grammar+
                 {})
  => (throws)

  (emit-common-loop '(:? (+ 1 2) B
                      C D
                      :else E)
                 +grammar+
                 {})
  => "((+ 1 2)) ? B : ((:? C D E))"

  [:bi]
  (emit-common-loop '(== (+ 1 2) B)
                  +grammar+
                  {})
  => "((+ 1 2)) == B"

  (emit-common-loop '(== (+ 1 2) B C)
                  +grammar+
                  {})
  => (throws)

  [:between]
  (emit-common-loop '(:to (+ 1 2) B)
                   (assoc-in +grammar+
                           [:reserved :to]
                           {:op :to :symbol #{:to}  :emit :between  :raw ".."})
                 {})
  => "((+ 1 2))..B"
  
  [:assign]
  (emit-common-loop '(:= A (+ 1 2))
                   +grammar+
                   {})
  => "A = ((+ 1 2))"

  [:invoke]
  (emit-common-loop '(:hello (+ 1 2))
                   (assoc-in +grammar+
                             [:reserved :hello]
                             {:op :hello :symbol #{:hello} :emit :invoke  :raw "HELLO"})
                   
                   {})
  => "HELLO((+ 1 2))"

  [:new]
  (emit-common-loop '(new Array (+ 1 2) 2 3)
                   +grammar+
                   
                   {})
  => "new Array((+ 1 2),2,3)"

  [:index]
  (emit-common-loop '(. A B)
                   +grammar+
                   {})
  => "A.B"

  (emit-common-loop '(. (+ 1 2) B)
                   +grammar+
                   {})
  => "((+ 1 2)).B"
  
  (emit-common-loop '(. A [B])
                   +grammar+
                   
                   {})
  => "A[B]"

  (emit-common-loop '(. A (hello "world"))
                   +grammar+
                   
                   {})
  => "A.hello(\"world\")"
  
  (emit-common-loop '(. (+ A B) [A])
                   +grammar+
                   
                   {})
  => "((+ A B))[A]"

  [:return]
  (emit-common-loop '(return (+ 1 2) A)
                   +grammar+
                   {})
  => "return (+ 1 2), A"
  
  [:decorate]
  (emit-common-loop '(!:decorate [opts] (+ 1 2 3))
                   +grammar+
                   {})
  => "(+ 1 2 3)"

  [:with-global]
  (emit-common-loop '(!:G HELLO)
                   (assoc-in +grammar+ [:token :symbol :global]
                             (fn [sym _ _]
                               (list '. 'GLOBAL [(str sym)])))
                   {})
  => "(. GLOBAL [\"HELLO\"])"
  
  [:throw]
  (emit-common-loop '(this)
                   +grammar+
                   {})
  => (throws))


^{:refer std.lang.base.emit-common/emit-common :adopt true :added "4.0"}
(fact "emit main raw"

  [:discard]
  (emit-common '(\- A "B" :C)
               +grammar+
               {})
  => ""

  [:free]
  (emit-common '(:- (+ 1 (+ 2 3)) "B" :C)
               +grammar+
               {})
  => "1 + (2 + 3) B C"
  
  [:comment]
  (emit-common '(:# (+ 1 2) 2 3 4 5)
               +grammar+
               {})
  => "// 1 + 2 2 3 4 5"

  
  [:token]
  (emit-common '(super)
               (assoc-in +grammar+ [:reserved 'super :emit] :token)
               {})
  => "super"
  
  [:pre]
  (emit-common '(not A)
               +grammar+
               {})
  => "!A"
  
  (emit-common '(not (+ 1 1))
               +grammar+
               {})
  => "!(1 + 1)"

  [:post]
  (emit-common '(factorial (+ 1 1))
               (assoc-in +grammar+
                         [:reserved 'factorial]
                         '{:op :factorial, :symbol #{factorial}, :emit :post, :raw "!"})
               {})
  => "(1 + 1)!"

  [:infix]
  (emit-common '(+ (+ 1 1))
               +grammar+
               {})
  => "(1 + 1)"

  [:infix-]
  (emit-common '(- (+ 1 1))
               +grammar+
               {})
  => "-(1 + 1)"

  (emit-common '(- (+ 1 1)
                   1)
               +grammar+
               {})
  => "(1 + 1) - 1"

  [:infix*]
  (emit-common '(/ (+ 1 1))
               +grammar+
               {})
  => "1 / (1 + 1)"
  
  (emit-common '(/ (+ 1 1)
                   1)
               +grammar+
               {})
  => "(1 + 1) / 1"

  [:infix-if]
  (emit-common '(:? (+ 1 2) B)
               +grammar+
               {})
  => "(1 + 2) ? B : nil"
  
  (emit-common '(:? (+ 1 2) B)
               +grammar+
               {})
  => "(1 + 2) ? B : nil"
  
  (emit-common '(:? (+ 1 2) B C)
               +grammar+
               {})
  => "(1 + 2) ? B : C"
  
  (emit-common '(:? (+ 1 2) B C)
               +grammar+
               {})
  => "(1 + 2) ? B : C"

  (emit-common '(:? (+ 1 2) B :else C)
               +grammar+
               {})
  => "(1 + 2) ? B : C"

  (emit-common '(:? (+ 1 2) B C D)
               +grammar+
               {})
  => (throws)

  (emit-common '(:? (+ 1 2) B
                    C D
                    :else E)
               +grammar+
               {})
  => "(1 + 2) ? B : (C ? D : E)"

  [:bi]
  (emit-common '(== (+ 1 2) B)
               +grammar+
               {})
  => "(1 + 2) == B"

  (emit-common '(== (+ 1 2) B C)
               +grammar+
               {})
  => (throws)

  [:between]
  (emit-common '(:to (+ 1 2) B)
               (assoc-in +grammar+
                         [:reserved :to]
                         {:op :to :symbol #{:to}  :emit :between  :raw ".."})
               {})
  => "(1 + 2)..B"
  
  [:assign]
  (emit-common '(:= A (+ 1 2))
               +grammar+
               {})
  => "A = (1 + 2)"

  [:invoke]
  (emit-common '(:hello (+ 1 2))
               (assoc-in +grammar+
                         [:reserved :hello]
                         {:op :hello :symbol #{:hello} :emit :invoke  :raw "HELLO"})
               
               {})
  => "HELLO(1 + 2)"

  [:new]
  (emit-common '(new Array (+ 1 2) 2 3)
               +grammar+
               
               {})
  => "new Array(1 + 2,2,3)"

  [:index]
  (emit-common '(. A B)
               +grammar+
               {})
  => "A.B"

  (emit-common '(. (+ 1 2) B)
               +grammar+
               {})
  => "(1 + 2).B"
  
  (emit-common '(. A [B])
               +grammar+
               
               {})
  => "A[B]"

  (emit-common '(. A (hello "world"))
               +grammar+
               
               {})
  => "A.hello(\"world\")"
  
  (emit-common '(. (+ A B) [A])
               +grammar+
               
               {})
  => "(A + B)[A]"
  
  [:return]
  (emit-common '(return (+ 1 2) A)
               +grammar+
               {})
  => "return 1 + 2, A"

  [:decorate]
  (emit-common '(!:decorate [opts] (+ 1 2 3))
               +grammar+
               {})
  => "1 + 2 + 3"

  [:with-global]
  (emit-common '(!:G HELLO)
               (assoc-in +grammar+ [:token :symbol :global]
                         (fn [sym _ _]
                           (list '. 'GLOBAL [(str sym)])))
               {})
  => "GLOBAL[\"HELLO\"]"
  
  
  [:throw]
  (emit-common '(this)
               +grammar+
               {})
  => (throws))
