(ns std.lang.base.emit-block-test
  (:use code.test)
  (:require [std.lang.base.emit-block :refer :all]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lib :as h]))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit-block/emit-statement :added "3.0"}
(fact "emits a statement given grammar"
  ^:hidden

  (binding [common/*emit-fn* common/emit-common]
    (emit-statement '(+ 1 2) +grammar+ {}))
  => "1 + 2;")

^{:refer std.lang.base.emit-block/emit-do :added "3.0"}
(fact "emits a do block"
  ^:hidden

  (binding [common/*emit-fn* common/emit-common]
    (emit-do '((add 1 2) (add 3 4)) +grammar+ {}))
  => "add(1,2);\nadd(3,4);")

^{:refer std.lang.base.emit-block/emit-do* :added "3.0"}
(fact "like do but removes the statment at the end, useful for macros"
  (binding [common/*emit-fn* common/emit-common]
    (emit-do* '((add 1 2) (add 3 4)) +grammar+ {}))
  => "add(1,2);\nadd(3,4)")

^{:refer std.lang.base.emit-block/block-options :added "3.0"}
(fact "gets the block options"
  ^:hidden

  (block-options :for {:parameter {:space "|"}} :parameter +grammar+)
  => {:statement ";",
      :sep ",",
      :space "|",
      :static ".",
      :start "(",
      :line-spacing 1,
      :assign "=",
      :namespace-full "____",
      :apply ".",
      :access ".",
      :end ")",
      :namespace ".",
      :range ":"})

^{:refer std.lang.base.emit-block/emit-block-body :added "3.0"}
(fact "helper to emit a block body"
  ^:hidden

  (emit-block-body :while
                   {}
                   '[(add 1 2 3)
                     (add 1 2 3)]
                   +grammar+
                   {})
  
  => (std.string/| "{"
                   "  (add 1 2 3);"
                   "  (add 1 2 3);"
                   "}"))

^{:refer std.lang.base.emit-block/parse-params :added "3.0"}
(fact "parses params for a block"
  ^:hidden

  (parse-params '[i v :in (pairs)])
  => '[:statement [i v :in (pairs)]]

  (parse-params '(< x 1))
  => '[:raw (< x 1)])

^{:refer std.lang.base.emit-block/emit-params-statement :added "3.0"}
(fact "emits the params for statement"
  ^:hidden

  (emit-params-statement :for {} '[i v :in (pairs x)] +grammar+ {})
  => "i, v in (pairs x)")

^{:refer std.lang.base.emit-block/emit-params :added "3.0"}
(fact "constructs string to for loop args"
  ^:hidden

  (emit-params :for {:parameter {:sep ";" :space " "}}
               '[(:= i 1) (< i 1) (inc i)]
               +grammar+
               {})
  => "((:= i 1); (< i 1); (inc i))"

  (emit-params :for {:parameter {:statement ";"}}
               '[[(:= i 1) (:= j 0)] (< (* i j) 1) [(inc i) (inc j)]]
               +grammar+
               {})
  => "((:= i 1), (:= j 0); (< (* i j) 1); (inc i), (inc j))")

^{:refer std.lang.base.emit-block/emit-block-control :added "3.0"}
(fact "emits a control form code"
  ^:hidden

  (emit-block-controls :catch
                       (get-in +grammar+ '[:reserved try :block])
                       (get-in +grammar+ '[:reserved try :block :control])
                       '{:catch [(catch e (print e))]}
                       +grammar+ {})
  => "\ncatch(e){\n  (print e);\n}")

^{:refer std.lang.base.emit-block/emit-block-controls :added "3.0"}
(fact "emits control blocks for a form"
  ^:hidden

  (emit-block-controls :catch
                       (get-in +grammar+ '[:reserved try :block])
                       (get-in +grammar+ '[:reserved try :block :control])
                       '{:catch [(catch e (print e))]
                         :finally [(finally print 123)]}
                       +grammar+ {})
  => (std.string/|
             ""
             "catch(e){"
             "  (print e);"
             "}"
             "finally{"
             "  print;"
             "  123;"
             "}"))

^{:refer std.lang.base.emit-block/emit-block-setup :added "4.0"}
(fact "parses main and control blocks"
  ^:hidden
  
  (emit-block-setup :br
                    (get-in +grammar+ '[:reserved br* :block])
                    '(br*
                      (if (= x 1) (pr 1) (pr 2))
                      (elseif (= x 2) (pr 3) (pr 4))
                      (elseif (= x 3) (pr 5) (pr 6))
                      (elseif (= x 4) (pr 7))
                      (else (pr 8)))
                    +grammar+
                    {})
  => '["" nil ()
       {:if [(if (= x 1) (pr 1) (pr 2))],
        :elseif [(elseif (= x 2) (pr 3) (pr 4))
                 (elseif (= x 3) (pr 5) (pr 6))
                 (elseif (= x 4) (pr 7))],
        :else [(else (pr 8))]}
       nil])

^{:refer std.lang.base.emit-block/emit-block-inner :added "4.0"}
(fact "returns the inner block"
  ^:hidden
  
  (emit-block-inner :switch
                    (get-in +grammar+ '[:reserved switch :block])
                    '(switch
                      [(type obj)]
                      (case [:A] (return A))
                      (case [:B] (return B))
                      (default (return X)))
                    +grammar+
                    {})
  => (std.string/|
      "switch((type obj)){"
      "  case A :"
      "    (return A);"
      "  "
      "  case B :"
      "    (return B);"
      "  "
      "  default:"
      "    (return X);"
      "}"))

^{:refer std.lang.base.emit-block/emit-block-standard :added "3.0"}
(fact "emits a generic block"
  ^:hidden

  (binding [common/*emit-fn* common/emit-common]
    (emit-block-standard :br
                         (get-in +grammar+ '[:reserved br* :block])
                         '(br
                           (if (== x 1) (pr 1) (pr 2))
                           (elseif (== x 2) (pr 3) (pr 4))
                           (elseif (== x 3) (pr 5) (pr 6))
                           (elseif (== x 4) (pr 7))
                           (else (pr 8)))
                         +grammar+
                         {}))
  
  => (std.string/|
      "if(x == 1){"
      "  pr(1);"
      "  pr(2);"
      "}"
      "elseif(x == 2){"
      "  pr(3);"
      "  pr(4);"
      "}"
      "elseif(x == 3){"
      "  pr(5);"
      "  pr(6);"
      "}"
      "elseif(x == 4){"
      "  pr(7);"
      "}"
      "else{"
      "  pr(8);"
      "}"))

^{:refer std.lang.base.emit-block/emit-block :added "3.0"}
(fact "emits a minimal block expression"
  ^:hidden
  
  (emit-block :while
              '(while (< 1 2)
                 (add 1 2 3)
                 (add 1 2 3))
              +grammar+
              {})
  => (std.string/|
   "while((< 1 2)){"
   "  (add 1 2 3);"
   "  (add 1 2 3);"
   "}"))


^{:refer std.lang.base.emit-block/test-block-loop :added "4.0"}
(fact "emits with blocks"
  (test-block-loop '(+ 1 2) +grammar+ {})
  => "1 + 2")

^{:refer std.lang.base.emit-block/test-block-emit :added "4.0"}
(fact "emit with blocks"
  (test-block-emit '(+ 1 2) +grammar+ {})
  => "1 + 2")
