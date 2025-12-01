(ns std.lang.model.spec-c-test
  (:use code.test)
  (:require [std.lang.model.spec-c :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

^{:refer std.lang.model.spec-c/tf-define :added "4.0"}
(fact "not sure if this is needed (due to defmacro) but may be good for source to source"
  ^:hidden
  
  (tf-define '(define A 1))
  => '(:- "#define" A 1)

  (tf-define '(define A [a b] (+ a b)))
  => '(:- "#define" (:% A (quote (a b))) (+ a b)))

(fact "emit basic C"
  (l/emit-script
   ['(define A 1)]
   {:lang :c})
  => "{#define A 1}")

(fact "emit pointer ops"
  (l/emit-script
   ['(:* x)]
   {:lang :c})
  => "{*x}")

(fact "emit math"
  (l/emit-script
   ['(+ 1 2)]
   {:lang :c})
  => "{1 + 2}")

(fact "emit control"
  (l/emit-script
   ['(if true
       (return 1)
       (return 0))]
   {:lang :c})
  => "{\n  if(true){\n  return 1;\n}\nelse{\n  return 0;\n}\n}")

(fact "emit struct"
  (l/emit-script
   ['(struct Point
       [:int x
        :int y])]
   {:lang :c})
  => "{\n  struct Point { \n  int x;\n  int y; \n};\n}")

(fact "emit enum"
  (l/emit-script
   ['(enum Color [RED GREEN BLUE])]
   {:lang :c})
  => "{enum Color { RED,GREEN,BLUE };}")

(fact "emit typedef"
  (l/emit-script
   ['(typedef :int :my_int)]
   {:lang :c})
  => "{typedef int my_int;}")

(fact "emit function with types"
  (l/emit-script
   ['(defn main [[:int argc] [:char** argv]]
       (return 0))]
   {:lang :c})
  => "{\n  void main (int argc, char** argv) { \n  return 0; \n}\n}")

(fact "emit arrow and sizeof"
  (l/emit-script
   ['(arrow ptr field)]
   {:lang :c})
  => "{ptr -> field}")

(fact "emit sizeof"
  (l/emit-script
   ['(sizeof x)]
   {:lang :c})
  => "{sizeof x}")


^{:refer std.lang.model.spec-c/tf-struct :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-c/tf-enum :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-c/tf-typedef :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-c/c-fn-args :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-c/tf-defn :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-c/tf-arrow :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-c/tf-sizeof :added "4.1"}
(fact "TODO")