(ns hara.model.annex.spec-php-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.lang.impl :as impl]
            [hara.model.annex.spec-php :as spec-php]))

(l/script :php)

^{:refer hara.model.annex.spec-php/+grammar+ :added "4.1"}
(fact "emits explicit PHP variables and collections"
  (l/emit-as :php ['(var $value {:a [1 2 3]})])
  => "$value = ['a' => [1, 2, 3]]")

^{:refer hara.model.annex.spec-php/+grammar+ :added "4.1"}
(fact "emits shared function templates for top-level defn"
  (l/emit-as :php ['(defn add [$a $b]
                     (return (+ $a $b)))])
  => #"function add\(\$a, \$b\)\s*\{\n  return \$a \+ \$b;\n\}")

^{:refer hara.model.annex.spec-php/+grammar+ :added "4.1"}
(fact "emits anonymous functions with explicit PHP args"
  (l/emit-as :php ['(fn [$a $b]
                     (return (+ $a $b)))])
  => #"function \(\$a, \$b\)\s*\{\n  return \$a \+ \$b;\n\}")

^{:refer hara.model.annex.spec-php/+grammar+ :added "4.1"}
(fact "rewrites bare lexical locals for PHP emission"
  (l/emit-as :php ['(do
                     (var out [])
                     (var entries [0 1 2])
                     (for:array [i entries]
                       (xt/x:arr-push out i))
                     out)])
  => #"\$out = \[\];\n\$entries = \[0, 1, 2\];\nforeach \(\$entries as \$i\)")

^{:refer hara.model.annex.spec-php/+grammar+ :added "4.1"}
(fact "rewrites bare function params for seedgen-generated PHP forms"
  (l/emit-as :php ['((fn [x]
                       (return (+ x 1)))
                     2)])
  => #"\(function \(\$x\)\{\s+return \$x \+ 1;\s+\}\)\(2\)")

^{:refer hara.model.annex.spec-php/+grammar+ :added "4.1"}
(fact "emits method, static, and constructor calls through shared invoke/index emitters"
  [(l/emit-as :php ['(. $obj (method $arg))])
   (l/emit-as :php ['($ MyClass staticMethod $arg)])
   (l/emit-as :php ['(new MyClass $arg)])]
  => ["$obj->method($arg)"
      "MyClass::staticMethod($arg)"
      "new MyClass($arg)"])

^{:refer hara.model.annex.spec-php/+grammar+ :added "4.1"}
(fact "emits raw PHP script bodies"
  (impl/emit-script '(do (var $a 1)
                         (echo $a))
                    {:lang :php})
  => "$a = 1;\necho $a;")
