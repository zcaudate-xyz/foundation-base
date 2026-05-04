(ns hara.common.emit-common-macro-test
  (:require [hara.common.emit-common :as common :refer :all]
            [hara.common.emit-helper :as helper]
            [hara.common.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer hara.common.emit-common/emit-macro.-> :adopt true :added "4.0"}
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

^{:refer hara.common.emit-common/emit-macro.->> :adopt true :added "4.0"}
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


^{:refer hara.common.emit-common/emit-macro.xor :adopt true :added "4.0"}
(fact "emit for macros structures"

  (emit-common-loop '(xor A B)
                   +grammar+
                   {})
  => "(:? A B (not B))"
  
  (emit-common '(xor A B)
               +grammar+
               {})
  => "A ? B : !B")
