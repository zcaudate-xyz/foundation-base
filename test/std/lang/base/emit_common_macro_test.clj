(ns std.lang.base.emit-common-macro-test
  (:require [std.lang.base.emit-common :as common :refer :all]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit-common/emit-macro.-> :adopt true :added "4.0"}
(fact "emit for macros structures"

  (emit-common-loop '(-> A
                        B
                        C)
                   +grammar+
                   {})
  => "(C (B A))"

  (emit-common '(-> A
                    B
                    C)
               +grammar+
               {})
  => "C(B(A))"
  

  (emit-common-loop '(-> (+ 1 2)
                        (F (+ 3 4))
                        (G (+ 5 6)))
                   +grammar+
                   {})
  => "(G (F (+ 1 2) (+ 3 4)) (+ 5 6))"

  (emit-common '(-> (+ 1 2)
                    (F (+ 3 4))
                    (G (+ 5 6)))
               +grammar+
               {})
  => "G(F(1 + 2,3 + 4),5 + 6)")

^{:refer std.lang.base.emit-common/emit-macro.->> :adopt true :added "4.0"}
(fact "emit for macros structures"

  (emit-common-loop '(->> A
                         B
                         C)
                   +grammar+
                   {})
  => "(C (B A))"

  (emit-common '(->> A
                     B
                     C)
               +grammar+
               {})
  => "C(B(A))"

  (emit-common-loop '(->> (+ 1 2)
                         (F (+ 3 4))
                         (G (+ 5 6)))
                   +grammar+
                   {})
  => "(G (+ 5 6) (F (+ 3 4) (+ 1 2)))"

  (emit-common '(->> (+ 1 2)
                     (F (+ 3 4))
                     (G (+ 5 6)))
               +grammar+
               {})
  => "G(5 + 6,F(3 + 4,1 + 2))")


^{:refer std.lang.base.emit-common/emit-macro.xor :adopt true :added "4.0"}
(fact "emit for macros structures"

  (emit-common-loop '(xor A B)
                   +grammar+
                   {})
  => "(:? A B (not B))"
  
  (emit-common '(xor A B)
               +grammar+
               {})
  => "A ? B : !B")
