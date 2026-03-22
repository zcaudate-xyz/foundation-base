(ns std.object.framework.print-test
  (:require [std.object.framework.print :as print])
  (:use code.test))

^{:refer std.object.framework.print/assoc-print-vars :added "3.0"}
(fact "helper to assoc print vars in options"

  (print/assoc-print-vars {} {:tag "hello"})
  => {:tag "hello"})

^{:refer std.object.framework.print/format-value :added "3.0"}
(fact "formats the object into a readable string"

  (print/format-value (test.Cat. "fluffy")
                      {:tag "cat"})
  => "#cat{:name \"fluffy\"}")

^{:refer std.object.framework.print/extend-print :added "3.0"}
(fact "extend `print-method` function for a particular class"

  (macroexpand-1 '(print/extend-print test.Cat)))

(comment
  (code.manage/import))