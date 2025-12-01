(ns std.lang.model.spec-php-test
  (:require [std.lang.model.spec-php :as php]
            [std.lang :as l]
            [std.lib :as h]
            [code.test :as t]))

(l/script :php)

(t/fact "test php emission"
  (l/emit-script
   '(do
      (var a 10)
      (var b 20)
      (if (> a b)
        (return a)
        (return b)))
   {:lang :php})
  => "$a = 10;\n$b = 20;\nif($a > $b){\n  return $a;\n}\nelse{\n  return $b;\n}")

(t/fact "test php functions"
  (l/emit-script
   '(defn add [a b]
      (return (+ a b)))
   {:lang :php})
  => "function add($a, $b) {\nreturn $a + $b;\n}")

(t/fact "test php arrays"
  (l/emit-script
   '(var arr [1 2 3])
   {:lang :php})
  => "$arr = [1, 2, 3]")

(t/fact "test php maps"
  (l/emit-script
   '(var m {"a" 1 "b" 2})
   {:lang :php})
  => "$m = ['a' => 1, 'b' => 2]")

(t/fact "test php invoke"
  (l/emit-script
   '(do
      (x-print "Hello")
      (add 1 2))
   {:lang :php})
  => "x_print('Hello');\nadd(1, 2);")

(t/fact "test php dot access"
  (l/emit-script
   '(. obj (method arg))
   {:lang :php})
  => "$obj->method($arg)")

(t/fact "test php static access"
  (l/emit-script
   '(. MyClass staticMethod arg)
   {:lang :php})
  => "MyClass::staticMethod->arg")

(t/fact "test php new"
  (l/emit-script
   '(new MyClass arg1 arg2)
   {:lang :php})
  => "new MyClass($arg1, $arg2)")

(t/fact "test php x-len"
  (l/emit-script
   '(x-len arr)
   {:lang :php})
  => "x_len($arr)")
