(ns hara.model.spec-lua.c-ffi-test
  (:require [hara.model.spec-lua.c-ffi :as c])
  (:use code.test))

^{:refer hara.model.spec-lua.c-ffi/c-ffi-type :added "4.1"}
(fact "coerces a C type annotation to a string"
  (c/c-ffi-type :int)         => "int"
  (c/c-ffi-type 'foo)         => "foo"
  (c/c-ffi-type [:const :char '*]) => "const char *"
  (c/c-ffi-type "custom")     => "custom"
  (c/c-ffi-type 5)            => "5")

^{:refer hara.model.spec-lua.c-ffi/c-ffi-sym :added "4.1"}
(fact "sanitizes a symbol for use in C"
  (c/c-ffi-sym 'my-fn)   => "my_fn"
  (c/c-ffi-sym 'a.b)     => "a_b"
  (c/c-ffi-sym 'a/b)     => "a____b"
  (c/c-ffi-sym "foo-bar") => "foo_bar")

^{:refer hara.model.spec-lua.c-ffi/c-ffi-form? :added "4.1"}
(fact "identifies C FFI blocks"
  (c/c-ffi-form? '(%.c (fn add [x])))                     => true
  (c/c-ffi-form? '(clojure.core/deref .c (fn add [x])))   => true
  (c/c-ffi-form? '(!:lang {:lang :c}))                    => true
  (c/c-ffi-form? '(!:lang {:lang :lua}))                  => false
  (c/c-ffi-form? '(fn add [x]))                           => false
  (c/c-ffi-form? :not-a-form)                             => false)

^{:refer hara.model.spec-lua.c-ffi/c-ffi-fn-form? :added "4.1"}
(fact "checks if a form is a C FFI function declaration"
  (c/c-ffi-fn-form? '(fn [x] x))       => true
  (c/c-ffi-fn-form? '(fn:> [x] x))     => true
  (c/c-ffi-fn-form? '(defn add [x]))   => false
  (c/c-ffi-fn-form? '(%.c 1 2))        => false)

^{:refer hara.model.spec-lua.c-ffi/c-ffi-forms :added "4.1"}
(fact "collects fn/fn:> forms, descending into do blocks"
  (c/c-ffi-forms '[(fn a []) (fn:> b [])])
  => '((fn a []) (fn:> b []))

  (c/c-ffi-forms '[(do (fn a []) (fn b []))])
  => '((fn a []) (fn b []))

  (c/c-ffi-forms '[(fn a []) (other 1 2) (do (fn b []))])
  => '((fn a []) (fn b [])))

^{:refer hara.model.spec-lua.c-ffi/c-ffi-args :added "4.1"}
(fact "emits C argument list from a fn args vector"
  (c/c-ffi-args '[:int x :char* y])
  => ["int x" "char* y"]

  (c/c-ffi-args '[^{:tag :int} x])
  => ["int x"]

  (c/c-ffi-args '[^{:- :double} x])
  => ["double x"]

  (c/c-ffi-args '[x])
  => ["void* x"]

  (c/c-ffi-args '[])
  => [])

^{:refer hara.model.spec-lua.c-ffi/c-ffi-decl :added "4.1"}
(fact "emits a single C forward declaration from a fn/fn:> form"
  (c/c-ffi-decl '(fn ^{:tag :int} add [^{:tag :int} x]))
  => "int add(int x);"

  (c/c-ffi-decl '(fn sub [:int x :double y]))
  => "void sub(int x, double y);"

  (c/c-ffi-decl '(fn nop []))
  => "void nop();")

^{:refer hara.model.spec-lua.c-ffi/c-ffi-body->string :added "4.1"}
(fact "emits C forward declarations from a collection of fn/fn:> forms"
  (c/c-ffi-body->string '[(fn a [x]) (fn b [y])])
  => "void a(void* x);\nvoid b(void* y);")
