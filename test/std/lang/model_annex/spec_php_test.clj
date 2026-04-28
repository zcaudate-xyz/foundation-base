(ns std.lang.model-annex.spec-php-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lang.base.impl :as impl]
            [std.lang.model-annex.spec-php :as spec-php]))

(l/script :php)

^{:refer std.lang.model-annex.spec-php/+grammar+ :added "4.1"}
(fact "emits explicit PHP variables and collections"
  (l/emit-as :php ['(var $value {:a [1 2 3]})])
  => "$value = ['a' => [1, 2, 3]]")

^{:refer std.lang.model-annex.spec-php/+grammar+ :added "4.1"}
(fact "emits shared function templates for top-level defn"
  (l/emit-as :php ['(defn add [$a $b]
                     (return (+ $a $b)))])
  => #"function add\(\$a, \$b\)\s*\{\n  return \$a \+ \$b;\n\}")

^{:refer std.lang.model-annex.spec-php/+grammar+ :added "4.1"}
(fact "emits anonymous functions with explicit PHP args"
  (l/emit-as :php ['(fn [$a $b]
                     (return (+ $a $b)))])
  => #"function \(\$a, \$b\)\s*\{\n  return \$a \+ \$b;\n\}")

^{:refer std.lang.model-annex.spec-php/+grammar+ :added "4.1"}
(fact "emits method, static, and constructor calls through shared invoke/index emitters"
  [(l/emit-as :php ['(. $obj (method $arg))])
   (l/emit-as :php ['($ MyClass staticMethod $arg)])
   (l/emit-as :php ['(new MyClass $arg)])]
  => ["$obj->method($arg)"
      "MyClass::staticMethod($arg)"
      "new MyClass($arg)"])

^{:refer std.lang.model-annex.spec-php/+grammar+ :added "4.1"}
(fact "emits raw PHP script bodies"
  (impl/emit-script '(do (var $a 1)
                         (echo $a))
                    {:lang :php})
  => "$a = 1;\necho $a;")
