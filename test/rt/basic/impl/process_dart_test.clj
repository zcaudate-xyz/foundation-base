(ns rt.basic.impl.process-dart-test
  (:require [rt.basic.impl.process-dart :refer :all]
            [std.lib.os :as os])
  (:use code.test))

^{:refer rt.basic.impl.process-dart/normalize-dart-source :added "4.1"}
(fact "preserves multiline call continuations when normalizing dart"
  ^:hidden

  (normalize-dart-source "void main() {\n  print(\n    foo(1)\n  )\n}")
  => "void main() {\n  print(\n    foo(1)\n  );\n}")


^{:refer rt.basic.impl.process-dart/sh-exec-dart :added "4.1"}
(fact "executes dart twostep pipeline"
  ^:hidden
  (with-redefs [os/sh (fn [_] {:pid 1})
                os/sh-wait (fn [_] nil)
                os/sh-output (fn [_] {:exit 0 :out "42\n" :err ""})]
    (sh-exec-dart ["dart" "compile" "exe"] "void main() {}"
                  {:extension "dart"
                   :output-flag "-o"}))
  => "42")

^{:refer rt.basic.impl.process-dart/transform-form :added "4.1"}
(fact "wraps forms in standalone dart main"
  (-> (transform-form ['(+ 1 2)] {}) pr-str)
  => #"void main"

  (-> (transform-form ['(+ 1 2)] {}) pr-str)
  => #"print")