(ns std.lang.base.emit-block-op-test
  (:use code.test)
  (:require [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-block :as block]
            [std.lang.base.grammer :as grammer]
            [std.lib :as h]))

(def +reserved+
  (-> (grammer/build)
      (grammer/to-reserved)))

(def +grammer+
  (grammer/grammer :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit-block/test-block-loop.do :adopt true :added "4.0"}
(fact "emit do"

  (block/test-block-loop '(do (+ 1 2 3)
                                    (+ 4 5 6))
                               +grammer+
                               {})
  => "(+ 1 2 3);\n(+ 4 5 6);"

  (block/test-block-emit '(do (+ 1 2 3)
                                (+ 4 5 6))
                           +grammer+
                           {})
  => "1 + 2 + 3;\n4 + 5 + 6;"

  (block/test-block-emit '(do (\\ 1 2 3)
                                (\\ hello)
                                (+ 4 5 6))
                           +grammer+
                           {})
  => "1 2 3\nhello\n4 + 5 + 6;")

^{:refer std.lang.base.emit-block/test-block-loop.do* :adopt true :added "4.0"}
(fact "emit do*"

  (block/test-block-loop '(do* (+ 1 2 3)
                                     (+ 4 5 6))
                               +grammer+
                               {})
  => "(+ 1 2 3);\n(+ 4 5 6)"
  
  (block/test-block-emit '(do* (+ 1 2 3)
                                (+ 4 5 6))
                           +grammer+
                           {})
  => "1 + 2 + 3;\n4 + 5 + 6")

^{:refer std.lang.base.emit-block/test-block-loop.for :adopt true :added "4.0"}
(fact "emit for"

  (block/test-block-loop '(for [(:= i 0)
                                  (<= i 10)
                                  (:++ i)]
                              (print (:float i)))
                               +grammer+
                               {})

  =>  (std.string/|
       "for((:= i 0), (<= i 10), (:++ i)){"
       "  (print (:float i));"
       "}")
  (block/test-block-emit '(for [(:= i 0)
                                  (<= i 10)
                                  (:++ i)]
                              (print (:float i)))
                           +grammer+
                           {})
  => (std.string/|
      "for(i = 0, i <= 10, ++i){"
      "  print(((float)i));"
      "}"))


^{:refer std.lang.base.emit-block/test-block-loop.forange
  :adopt true :added "4.0"}
(fact "emit forange"

  (block/test-block-loop '(forange [i 10] (print i))
                               +grammer+
                               {})
  => "(for [(var i 0) (< i 10) [(:= i (+ i 1))]] (print i))")

^{:refer std.lang.base.emit-block/test-block-loop.if :adopt true :added "4.0"}
(fact "emit if"

  (block/test-block-loop '(if (< i 1)
                                  (return y)
                                  (:= x 1))
                               +grammer+
                               {})
  => "(br* (if (< i 1) (return y)) (else (:= x 1)))"

  
  (block/test-block-emit '(if (< i 1)
                              (return y)
                              (:= x 1))
                           +grammer+
                           {})
  =>  (std.string/|
       "if(i < 1){"
       "  return y;"
       "}"
       "else{"
       "  x = 1;"
       "}"))

^{:refer std.lang.base.emit-block/test-block-loop.when :adopt true :added "4.0"}
(fact "emit if"

  (block/test-block-loop '(when (< i 1)
                                  (:= x 1)
                                  (return y))
                               +grammer+
                               {})
  => "(br* (if (< i 1) (:= x 1) (return y)))"
  
  (block/test-block-emit '(when (< i 1)
                              (:= x 1)
                              (return y))
                           +grammer+
                           {})
  => (std.string/|
      "if(i < 1){"
      "  x = 1;"
      "  return y;"
      "}"))

^{:refer std.lang.base.emit-block/test-block-loop.switch :adopt true :added "4.0"}
(fact "emit switch"

  (block/test-block-loop '(case hello
                                  "A" 1
                                  "B" 2
                                  3)
                               +grammer+
                               {})
  => "(switch [hello] (case [\"A\"] 1) (case [\"B\"] 2) (default 3))"
  
  (block/test-block-emit '(case hello
                              "A" 1
                              "B" 2
                              3)
                           +grammer+
                           {})
  (std.string/|
   "switch(hello){"
   "  case \"A\":"
   "    1;"
   "  "
   "  case \"B\":"
   "    2;"
   "  "
   "  default:"
   "    3;"
   "}"))

^{:refer std.lang.base.emit-block/test-block-loop.block :adopt true :added "4.0"}
(fact "emit block"

  (block/test-block-loop '(block
                                 (block (+ 1 2 3)))
                               +grammer+
                               {})
  => "{\n  (block (+ 1 2 3))\n}"

  
  (block/test-block-emit '(block
                                 (block (+ 1 2 3)))
                           +grammer+
                           {})
  
  => (std.string/|
      "{"
      "  {"
      "    1 + 2 + 3;"
      "  }"
      "}"))

^{:refer std.lang.base.emit-block/test-block-loop.try :adopt true :added "4.0"}
(fact "emit try"

  (block/test-block-loop '(try
                                  (block (+ 1 2 3))
                                  (catch e (return e)))
                               +grammer+
                               {})
  => (std.string/|
      "try{"
      "  (block (+ 1 2 3))"
      "}"
      "catch(e){"
      "  (return e);"
      "}")
  
  (block/test-block-emit '(try
                              (block (+ 1 2 3))
                              (catch e (return e)))
                           +grammer+
                           {})
  
  => (std.string/|
      "try{"
      "  {"
      "    1 + 2 + 3;"
      "  }"
      "}"
      "catch(e){"
      "  return e;"
      "}"))
