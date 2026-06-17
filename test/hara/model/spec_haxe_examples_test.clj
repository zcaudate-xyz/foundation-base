(ns hara.model.spec-haxe-examples-test
  (:require [hara.lang :as l]
            [hara.lang.pointer :as ptr]
            [haxe.examples :as examples])
  (:use code.test))

^{:refer haxe.examples/hello :added "4.1"}
(fact "emits a hello function"
  (ptr/ptr-display (deref #'examples/hello) {})
  => "function hello(name) {\n  return \"Hello, \" + name + \"!\";\n}")

^{:refer haxe.examples/factorial :added "4.1"}
(fact "emits a recursive factorial function"
  (ptr/ptr-display (deref #'examples/factorial) {})
  => "function factorial(n) {\n  if(n <= 1){\n    return 1;\n  }\n  else{\n    return n * factorial(n - 1);\n  }\n}")

^{:refer haxe.examples/circle-area :added "4.1"}
(fact "emits a function using a top-level constant"
  (ptr/ptr-display (deref #'examples/circle-area) {})
  => "function circle_area(r) {\n  return pi * r * r;\n}")
