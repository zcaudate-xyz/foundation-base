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
  (l/emit-as :c '[(define A 1)])
  => "#define A 1")

(fact "emit pointer ops"
  (l/emit-as :c '[(:* x)])
  => "*x")

(fact "emit math"
  (l/emit-as :c '[(+ 1 2)])
  => "1 + 2")

(fact "emit control"
  (l/emit-as :c '[(if true
                    (return 1)
                    (return 0))])
  => "if(true){\n  return 1;\n}\nelse{\n  return 0;\n}")

(fact "emit struct"
  (l/emit-as :c '[(struct Point
                    [:int x
                     :int y])])
  => "struct Point { \n  int x;\n  int y; \n};")

(fact "emit enum"
  (l/emit-as :c '[(enum Color [RED GREEN BLUE])])
  => "enum Color { RED,GREEN,BLUE };")

(fact "emit typedef"
  (l/emit-as :c '[(typedef :int :my_int)])
  => "typedef int my_int;")

(fact "emit function with types"
  (l/emit-as :c '[(defn main [[:int argc] [:char** argv]]
                    (return 0))])
  => "void main (int argc, char** argv) { \n  return 0; \n}")

(fact "emit arrow"
  (l/emit-as :c '[(arrow ptr field)])
  => "ptr -> field")

(fact "emit sizeof"
  (l/emit-as :c '[(sizeof x)])
  => "sizeof x")


^{:refer std.lang.model.spec-c/tf-struct :added "4.1"}
(fact "transforms struct definition"
  (tf-struct '(struct Point [:int x :int y]))
  => (contains '(:- "struct" Point
                 (:- "{"
                  (\\ \\ (\| (do (:- :int "x;") (:- :int "y;"))))
                  (:- "\n};")))))

^{:refer std.lang.model.spec-c/tf-enum :added "4.1"}
(fact "transforms enum definition"
  (tf-enum '(enum Color [RED GREEN BLUE]))
  => '(:- "enum" Color (:- "{") (quote [RED GREEN BLUE]) (:- "};")))

^{:refer std.lang.model.spec-c/tf-typedef :added "4.1"}
(fact "transforms typedef"
  (tf-typedef '(typedef :int :my_int))
  => '(:- "typedef" :int "my_int;"))

^{:refer std.lang.model.spec-c/c-fn-args :added "4.1"}
(fact "custom C function arguments emission"
  (c-fn-args '(quote [[:int argc] [:char** argv]]) nil nil)
  => "(int argc, char** argv)")

^{:refer std.lang.model.spec-c/tf-defn :added "4.1"}
(fact "custom defn for C"
  (tf-defn '(defn main [[:int argc] [:char** argv]] (return 0)))
  => (contains '(:- "void" main (:c-args '([:int argc] [:char** argv])))))

^{:refer std.lang.model.spec-c/tf-arrow :added "4.1"}
(fact "transforms arrow ->"
  (tf-arrow '(arrow ptr field))
  => '(:% ptr (:- "->" field)))

^{:refer std.lang.model.spec-c/tf-sizeof :added "4.1"}
(fact "transforms sizeof"
  (tf-sizeof '(sizeof x))
  => '(:- "sizeof" x))
