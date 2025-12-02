(ns std.lang.model.spec-php-test
  (:use [code.test])
  (:require [std.lang.model.spec-php :as spec-php]
            [std.lang :as l]
            [std.lib :as h]))

(l/script :php)

(fact "test php emission"
  (l/emit-as :php
   '[(do
      (var a 10)
      (var b 20)
      (if (> a b)
        (return a)
        (return b)))])
  => "$a = 10;\n$b = 20;\nif($a > $b){\n  return $a;\n}\nelse{\n  return $b;\n}")

(fact "test php functions"
  (l/emit-as :php
   '[(defn add [a b]
      (return (+ a b)))])
  => "function add($a, $b) {\nreturn $a + $b;\n}")

(fact "test php arrays"
  (l/emit-as :php
   '[(var arr [1 2 3])])
  => "$arr = [1, 2, 3]")

(fact "test php maps"
  (l/emit-as :php
   '[(var m {"a" 1 "b" 2})])
  => "$m = ['a' => 1, 'b' => 2]")

(fact "test php invoke"
  (l/emit-as :php
   '[(do
      (x-print "Hello")
      (add 1 2))])
  => "x_print('Hello');\nadd(1, 2);")

(fact "test php dot access"
  (l/emit-as :php
   '[(. obj (method arg))])
  => "$obj->method($arg)")

(fact "test php static access"
  (l/emit-as :php
   '[(. MyClass staticMethod arg)])
  => "MyClass::staticMethod->arg")

(fact "test php new"
  (l/emit-as :php
   '[(new MyClass arg1 arg2)])
  => "new MyClass($arg1, $arg2)")

(fact "test php x-len"
  (l/emit-as :php
   '[(x-len arr)])
  => "x_len($arr)")


^{:refer std.lang.model.spec-php/is-capitalized? :added "4.1"}
(fact "checks if string is capitalized"
  (spec-php/is-capitalized? "Abc") => true
  (spec-php/is-capitalized? "abc") => false)

^{:refer std.lang.model.spec-php/php-symbol :added "4.1"}
(fact "emit php symbol with $ prefix if it's a variable"
  (spec-php/php-symbol 'a spec-php/+grammar+ {})
  => "$a"
  (spec-php/php-symbol 'A spec-php/+grammar+ {})
  => "A"
  (spec-php/php-symbol 'a spec-php/+grammar+ {:php/func true})
  => "a")

^{:refer std.lang.model.spec-php/php-invoke-args :added "4.1"}
(fact "emit php invoke args"
  (spec-php/php-invoke-args '[1 2] spec-php/+grammar+ {})
  => "1, 2")

^{:refer std.lang.model.spec-php/php-invoke :added "4.1"}
(fact "emit php function call"
  (spec-php/php-invoke '(f 1 2) spec-php/+grammar+ {})
  => "f(1, 2)")

^{:refer std.lang.model.spec-php/php-var :added "4.1"}
(fact "emit php variable declaration"
  (spec-php/php-var '(var a 10))
  => '(:= a 10))

^{:refer std.lang.model.spec-php/php-defn :added "4.1"}
(fact "emit php function definition"
  (spec-php/php-defn '(defn add [a b] (return (+ a b))))
  => (list :- "function add(a, b) {\n(do (return (+ a b)))\n}"))

^{:refer std.lang.model.spec-php/php-defn- :added "4.1"}
(fact "emit php anonymous function"
  (spec-php/php-defn- '(fn [a b] (return (+ a b))))
  => (list :- "function (a, b) {\n(do (return (+ a b)))\n}"))

^{:refer std.lang.model.spec-php/php-array :added "4.1"}
(fact "emit php array"
  (spec-php/php-array [1 2 3] spec-php/+grammar+ {})
  => "[1, 2, 3]")

^{:refer std.lang.model.spec-php/php-map :added "4.1"}
(fact "emit php associative array"
  (spec-php/php-map {"a" 1 "b" 2} spec-php/+grammar+ {})
  => "[\"a\" => 1, \"b\" => 2]")

^{:refer std.lang.model.spec-php/php-dot-string :added "4.1"}
(fact "helper for php dot string"
  (spec-php/php-dot-string 'obj '(prop) spec-php/+grammar+ {})
  => "obj->prop")

^{:refer std.lang.model.spec-php/php-dot :added "4.1"}
(fact "emit php object access ->"
  (spec-php/php-dot '(. obj prop))
  => '(:- "obj->prop"))

^{:refer std.lang.model.spec-php/php-new :added "4.1"}
(fact "emit new Class()"
  (spec-php/php-new '(new MyClass arg1))
  => '(:- "new MyClass(arg1)"))
