(ns rt.basic.impl.process-dart-test
  (:require [rt.basic.impl.process-dart :refer :all])
  (:use code.test))

^{:refer rt.basic.impl.process-dart/normalize-dart-source :added "4.1"}
(fact "preserves multiline call continuations when normalizing dart"
  ^:hidden

  (normalize-dart-source "void main() {\n  print(\n    foo(1)\n  )\n}")
  => "void main() {\n  print(\n    foo(1)\n  );\n}")
