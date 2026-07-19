(ns kmi.lang.runtime.eval-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.runtime.eval :as eval]
             [kmi.lang.runtime :as rt]
             [kmi.lang.runtime.env :as env]
             [kmi.lang.parser :as parser]
             [kmi.lang.type-symbol :as sym]
             [kmi.lang.type-keyword :as kw]
             [kmi.lang.type-list :as list]
             [kmi.lang.type-vector :as vec]
             [kmi.lang.type-hashmap :as hm]
             [kmi.lang.type-hashset :as hs]
             [kmi.lang.type-syntax :as syn]
             [kmi.lang.protocol-base :as p]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.runtime.eval/make-result :added "4.1"}
(fact "creates a successful result map"

  (!.js
   (eval/make-result {} 42))
  => {"runtime" {} "value" 42})

^{:refer kmi.lang.runtime.eval/make-error :added "4.1"}
(fact "creates an error result map"

  (!.js
   (eval/make-error {} "oops"))
  => {"runtime" {} "error" "oops"})

^{:refer kmi.lang.runtime.eval/errorp :added "4.1"}
(fact "checks whether a result is an error"

  (!.js
   [(eval/errorp (eval/make-result {} 1))
    (eval/errorp (eval/make-error {} "e"))])
  => [false true])

^{:refer kmi.lang.runtime.eval/get-value :added "4.1"}
(fact "extracts the value from a result"

  (!.js
   (eval/get-value (eval/make-result {} 99)))
  => 99)

^{:refer kmi.lang.runtime.eval/get-runtime :added "4.1"}
(fact "extracts the runtime from a result"

  (!.js
   (eval/get-runtime (eval/make-result {"ns" "user"} 1)))
  => {"ns" "user"})

^{:refer kmi.lang.runtime.eval/recur-marker :added "4.1"}
(fact "creates a recur control value"

  (!.js
   (eval/recur-marker [1 2]))
  => {"::" "recur" "values" [1 2]})

^{:refer kmi.lang.runtime.eval/recur? :added "4.1"}
(fact "checks if a value is a recur marker"

  (!.js
   [(eval/recur? (eval/recur-marker []))
    (eval/recur? {"::" "recur"})
    (eval/recur? {})])
  => [true true false])

^{:refer kmi.lang.runtime.eval/recur-values :added "4.1"}
(fact "returns the values carried by a recur marker"

  (!.js
   (eval/recur-values (eval/recur-marker [3 4])))
  => [3 4])

^{:refer kmi.lang.runtime.eval/class-of :added "4.1"}
(fact "returns native class tags for managed and native values"

  (!.js
   [(eval/class-of (sym/symbol nil "x"))
    (eval/class-of (kw/keyword nil "x"))
    (eval/class-of (list/list [ 1 2]))
    (eval/class-of (vec/vector [ 1 2]))
    (eval/class-of (hm/hashmap [ (kw/keyword nil "a") 1]))
    (eval/class-of (hs/hashset [ 1 2]))
    (eval/class-of (syn/syntax [1] {}))
    (eval/class-of "hello")
    (eval/class-of 42)
    (eval/class-of nil)])
  => ["symbol" "keyword" "list" "vector" "hashmap" "hashset"
      "syntax" "string" "number" "nil"])

^{:refer kmi.lang.runtime.eval/symbol? :added "4.1"}
(fact "checks if a value is a kmi symbol"

  (!.js
   [(eval/symbol? (sym/symbol nil "x"))
    (eval/symbol? (kw/keyword nil "x"))
    (eval/symbol? 42)])
  => [true false false])

^{:refer kmi.lang.runtime.eval/keyword? :added "4.1"}
(fact "checks if a value is a kmi keyword"

  (!.js
   [(eval/keyword? (kw/keyword nil "x"))
    (eval/keyword? (sym/symbol nil "x"))
    (eval/keyword? (kw/keyword nil "x"))])
  => [true false true])

^{:refer kmi.lang.runtime.eval/list? :added "4.1"}
(fact "checks if a value is a kmi list"

  (!.js
   [(eval/list? (list/list [ 1 2]))
    (eval/list? (vec/vector [ 1 2]))
    (eval/list? [1 2])])
  => [true false false])

^{:refer kmi.lang.runtime.eval/vector? :added "4.1"}
(fact "checks if a value is a kmi vector"

  (!.js
   [(eval/vector? (vec/vector [ 1 2]))
    (eval/vector? (list/list [ 1 2]))
    (eval/vector? [1 2])])
  => [true false false])

^{:refer kmi.lang.runtime.eval/hashmap? :added "4.1"}
(fact "checks if a value is a kmi hashmap"

  (!.js
   [(eval/hashmap? (hm/hashmap [ (kw/keyword nil "a") 1]))
    (eval/hashmap? (hs/hashset [ 1]))
    (eval/hashmap? {})])
  => [true false false])

^{:refer kmi.lang.runtime.eval/hashset? :added "4.1"}
(fact "checks if a value is a kmi hashset"

  (!.js
   [(eval/hashset? (hs/hashset [ 1 2]))
    (eval/hashset? (hm/hashmap [ (kw/keyword nil "a") 1]))
    (eval/hashset? [1 2])])
  => [true false false])

^{:refer kmi.lang.runtime.eval/syntax? :added "4.1"}
(fact "checks if a value is a syntax wrapper"

  (!.js
   [(eval/syntax? (syn/syntax [1] {}))
    (eval/syntax? [1])
    (eval/syntax? (list/list [ 1 2]))])
  => [true false false])

^{:refer kmi.lang.runtime.eval/self-evaluating? :added "4.1"}
(fact "checks if a value evaluates to itself"

  (!.js
   [(eval/self-evaluating? nil)
    (eval/self-evaluating? "s")
    (eval/self-evaluating? 42)
    (eval/self-evaluating? true)
    (eval/self-evaluating? (kw/keyword nil "k"))
    (eval/self-evaluating? (hs/hashset [ 1]))
    (eval/self-evaluating? (sym/symbol nil "x"))
    (eval/self-evaluating? (list/list [ 1]))])
  => [true true true true true true false false])

^{:refer kmi.lang.runtime.eval/tagged-list? :added "4.1"}
(fact "checks if a list starts with a symbol of the given name"

  (!.js
   [(eval/tagged-list? (parser/read-string "(if true 1)") "if")
    (eval/tagged-list? (parser/read-string "(when true 1)") "if")
    (eval/tagged-list? (vec/vector [ 1 2]) "if")
    (eval/tagged-list? (parser/read-string "(1 2 3)") "if")])
  => [true false false false])

^{:refer kmi.lang.runtime.eval/read-many :added "4.1"}
(fact "reads all forms from a string"

  (!.js
   [(xt/x:len (eval/read-many "1 2 3"))
    (xt/x:get-idx (eval/read-many "(+ 1 2)") (xt/x:offset 0))])
  => [3 (parser/read-string "(+ 1 2)")])

^{:refer kmi.lang.runtime.eval/eval-symbol :added "4.1"}
(fact "resolves bound symbols and reports unbound ones"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   [(eval/errorp (eval/eval-symbol runtime env (sym/symbol nil "+")))
    (eval/errorp (eval/eval-symbol runtime env (sym/symbol nil "unbound-symbol-xyz")))])
  => [false true])

^{:refer kmi.lang.runtime.eval/eval-do-array :added "4.1"}
(fact "evaluates a sequence of forms and returns the last value"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   [(eval/get-value (eval/eval-do-array eval/eval-form runtime env [1 2 3]))
    (== nil (eval/get-value (eval/eval-do-array eval/eval-form runtime env [])))])
  => [3 true])

^{:refer kmi.lang.runtime.eval/eval-if :added "4.1"}
(fact "branches on the condition value"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   [(eval/get-value (eval/eval-if eval/eval-form runtime env (p/to-array (parser/read-string "(if true 1 2)"))))
    (eval/get-value (eval/eval-if eval/eval-form runtime env (p/to-array (parser/read-string "(if false 1 2)"))))
    (== nil (eval/get-value (eval/eval-if eval/eval-form runtime env (p/to-array (parser/read-string "(if nil 1)")))))])
  => [1 2 true])

^{:refer kmi.lang.runtime.eval/bind-pattern :added "4.1"}
(fact "binds symbols, vectors and hashmaps to values"

  (!.js
   (var runtime (rt/empty-runtime))

   (var out-sym (eval/bind-pattern eval/eval-form runtime (env/empty-env) (sym/symbol nil "x") 42))
   (var env-sym (eval/get-value out-sym))

   (var out-vec (eval/bind-pattern eval/eval-form runtime (env/empty-env) (parser/read-string "[a b]") (vec/vector [ 1 2])))
   (var env-vec (eval/get-value out-vec))

   (var out-map (eval/bind-pattern eval/eval-form runtime (env/empty-env) (parser/read-string "{:keys [a b] :or {b 99}}") (hm/hashmap [ (kw/keyword nil "a") 1])))
   (var env-map (eval/get-value out-map))

   [(env/env-lookup env-sym (sym/symbol nil "x"))
    (env/env-lookup env-vec (sym/symbol nil "a"))
    (env/env-lookup env-vec (sym/symbol nil "b"))
    (env/env-lookup env-map (sym/symbol nil "a"))
    (env/env-lookup env-map (sym/symbol nil "b"))])
  => [42 1 2 1 99])

^{:refer kmi.lang.runtime.eval/eval-let :added "4.1"}
(fact "evaluates a let form in a local scope"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (eval/get-value (eval/eval-let eval/eval-form runtime env (p/to-array (parser/read-string "(let [x 10 y 20] (+ x y))")))))
  => 30)

^{:refer kmi.lang.runtime.eval/parse-params :added "4.1"}
(fact "splits a parameter vector into required and rest symbols"

  (!.js
   (var params (p/to-array (parser/read-string "[a b & rest]")))
   (var out (eval/parse-params params))
   [(xt/x:len (xt/x:get-key out "req"))
    (env/sym-name (xt/x:get-idx (xt/x:get-key out "req") (xt/x:offset 0)))
    (env/sym-name (xt/x:get-key out "rest"))])
  => [2 "a" "rest"])

^{:refer kmi.lang.runtime.eval/make-closure :added "4.1"}
(fact "creates a closure descriptor from a fn form"

  (!.js
   (var closure (eval/make-closure (env/empty-env) (p/to-array (parser/read-string "(fn [x] (+ x 1))"))))
   [(xt/x:get-key closure "type")
    (xt/x:len (xt/x:get-key closure "req"))
    (xt/x:get-key closure "macro")])
  => ["kmi.fn" 1 false])

^{:refer kmi.lang.runtime.eval/eval-def :added "4.1"}
(fact "evaluates a def form and stores the var in the current namespace"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var out (eval/eval-def eval/eval-form runtime env (p/to-array (parser/read-string "(def x 42)"))))
   (env/ns-lookup (eval/get-runtime out) (sym/symbol nil "x")))
  => 42)

^{:refer kmi.lang.runtime.eval/eval-syntax-quote :added "4.1"}
(fact "evaluates syntax-quote, unquote and unquote-splicing"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var sq-form (parser/read-string "`(1 ~(+ 1 2) ~@[4 5])"))
   (var inner (xt/x:get-idx (p/to-array sq-form) (xt/x:offset 1)))
   (var out (eval/eval-syntax-quote eval/eval-form runtime env inner))
   (var value (eval/get-value out))
   [(eval/list? value)
    (p/to-array value)])
  => [true [1 3 4 5]])

^{:refer kmi.lang.runtime.eval/eval-defmacro :added "4.1"}
(fact "evaluates a defmacro form and registers a macro transformer"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var out (eval/eval-defmacro eval/eval-form runtime env
                                (p/to-array (parser/read-string "(defmacro when [test & body] (list (quote if) test (cons (quote do) body)))"))))
   (var new-rt (eval/get-runtime out))
   [(eval/errorp out)
    (xt/x:not-nil? (env/macro-lookup new-rt (sym/symbol nil "when")))])
  => [false true])

^{:refer kmi.lang.runtime.eval/eval-deref :added "4.1"}
(fact "evaluates deref on a var object"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var rt-with-x (env/ns-assoc runtime (sym/symbol nil "x") 42))
   (var x-var (eval/get-value (eval/eval-var rt-with-x env (p/to-array (parser/read-string "(var x)")))))
   (var rt-with-v (env/ns-assoc rt-with-x (sym/symbol nil "v") x-var))
   (var deref-form (p/to-array (list/list [ (sym/symbol nil "deref") (sym/symbol nil "v")])))
   (eval/get-value (eval/eval-deref eval/eval-form rt-with-v env deref-form)))
  => 42)

^{:refer kmi.lang.runtime.eval/eval-var :added "4.1"}
(fact "returns a var descriptor for a var-quote form"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (eval/get-value (eval/eval-var runtime env (p/to-array (parser/read-string "(var kmi.core/+)")))))
  => {"::" "var" "ns" "kmi.core" "name" "+"})

^{:refer kmi.lang.runtime.eval/eval-in-ns :added "4.1"}
(fact "switches the current namespace"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var out (eval/eval-in-ns eval/eval-form runtime env (p/to-array (parser/read-string "(in-ns (quote foo))"))))
   (env/current-ns-name (eval/get-runtime out)))
  => "foo")

^{:refer kmi.lang.runtime.eval/spec-ns-name :added "4.1"}
(fact "extracts a namespace name from a require/use spec"

  (!.js
   [(eval/spec-ns-name (sym/symbol nil "foo"))
    (eval/spec-ns-name (parser/read-string "[foo :as f]"))
    (eval/spec-ns-name (parser/read-string "(foo :as f)"))
    (== nil (eval/spec-ns-name 42))])
  => ["foo" "foo" "foo" true])

^{:refer kmi.lang.runtime.eval/eval-require-spec :added "4.1"}
(fact "processes a single require spec"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var out (eval/eval-require-spec eval/eval-form runtime env (parser/read-string "[kmi.core :as c]")))
   [(eval/errorp out)
    (env/ns-alias (eval/get-runtime out) "c")])
  => [false "kmi.core"])

^{:refer kmi.lang.runtime.eval/eval-require :added "4.1"}
(fact "evaluates a require form and updates the runtime"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var form (p/to-array (parser/read-string "(require (quote [kmi.core :refer [+]]))")))
   (var out (eval/eval-require eval/eval-form runtime env form))
   (var new-rt (eval/get-runtime out))
   [(eval/errorp out)
    (xt/x:not-nil? (env/var-lookup new-rt (env/empty-env) (sym/symbol nil "+")))])
  => [false true])

^{:refer kmi.lang.runtime.eval/eval-use :added "4.1"}
(fact "refers all vars from a namespace"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var form (p/to-array (parser/read-string "(use (quote kmi.core))")))
   (var out (eval/eval-use eval/eval-form runtime env form))
   (var new-rt (eval/get-runtime out))
   [(eval/errorp out)
    (xt/x:not-nil? (env/var-lookup new-rt (env/empty-env) (sym/symbol nil "+")))])
  => [false true])

^{:refer kmi.lang.runtime.eval/eval-host-interop :added "4.1"}
(fact "evaluates host interop calls and property access"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var plus (eval/get-value (eval/eval-symbol runtime env (sym/symbol nil "+"))))
   (var target {"add" plus "name" "world"})
   (var call-form (p/to-array (list/list [ (sym/symbol nil "host") target "add" 1 2])))
   (var prop-form (p/to-array (list/list [ (sym/symbol nil "host") target "name"])))
   [(eval/get-value (eval/eval-host-interop eval/eval-form runtime env call-form))
    (eval/get-value (eval/eval-host-interop eval/eval-form runtime env prop-form))])
  => [3 "world"])

^{:refer kmi.lang.runtime.eval/eval-throw :added "4.1"}
(fact "returns an error result with the evaluated message"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var form (p/to-array (list/list [ (sym/symbol nil "throw") "boom"])))
   (var out (eval/eval-throw eval/eval-form runtime env form))
   [(eval/errorp out) (xt/x:get-key out "error")])
  => [true "boom"])

^{:refer kmi.lang.runtime.eval/env-loop-find :added "4.1"}
(fact "finds the nearest loop environment in the lexical chain"

  (!.js
   (var loop-env (env/env-create (env/empty-env)))
   (xt/x:set-key loop-env "loop-bindings" [])
   [(== nil (eval/env-loop-find (env/empty-env)))
    (xt/x:has-key? (eval/env-loop-find loop-env) "loop-bindings")])
  => [true true])

^{:refer kmi.lang.runtime.eval/eval-loop :added "4.1"}
(fact "evaluates a loop form with tail recursion"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var form (p/to-array (parser/read-string "(loop [n 3 acc 1] (if (== n 0) acc (recur (- n 1) (* acc n))))")))
   (eval/get-value (eval/eval-loop eval/eval-form runtime env form)))
  => 6)

^{:refer kmi.lang.runtime.eval/eval-recur :added "4.1"}
(fact "returns a recur marker inside a loop or an error outside"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var loop-env (env/env-create env))
   (xt/x:set-key loop-env "loop-bindings" [])
   (var form (p/to-array (list/list [ (sym/symbol nil "recur") 1 2])))
   (var out (eval/eval-recur eval/eval-form runtime loop-env form))
   [(eval/recur? (eval/get-value out))
    (eval/recur-values (eval/get-value out))
    (eval/errorp (eval/eval-recur eval/eval-form runtime env form))])
  => [true [1 2] true])

^{:refer kmi.lang.runtime.eval/apply-fn :added "4.1"}
(fact "applies closures to arguments and reports arity mismatches"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var closure (eval/make-closure env (p/to-array (parser/read-string "(fn [x] (+ x 1))"))))
   (var bad-closure (eval/make-closure env (p/to-array (parser/read-string "(fn [x] x)"))))
   [(eval/get-value (eval/apply-fn eval/eval-form runtime closure [5]))
    (eval/errorp (eval/apply-fn eval/eval-form runtime bad-closure []))])
  => [6 true])

^{:refer kmi.lang.runtime.eval/eval-apply :added "4.1"}
(fact "evaluates an apply form"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var form (p/to-array (parser/read-string "(apply + [1 2 3])")))
   (eval/get-value (eval/eval-apply eval/eval-form runtime env form)))
  => 6)

^{:refer kmi.lang.runtime.eval/macroexpand-one :added "4.1"}
(fact "expands a form once when its head is a macro"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var macro-form (p/to-array (parser/read-string "(defmacro when [test then] (list (quote if) test then))")))
   (var rt-with-macro (eval/get-runtime (eval/eval-defmacro eval/eval-form runtime env macro-form)))
   (var form (parser/read-string "(when true 1)"))
   (var expanded (eval/macroexpand-one eval/eval-form rt-with-macro env form))
   [(eval/list? expanded)
    (== "if" (env/sym-name (xt/x:first (p/to-array expanded))))])
  => [true true])

^{:refer kmi.lang.runtime.eval/macroexpand-all :added "4.1"}
(fact "returns stable non-list forms without expansion"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   [(eval/macroexpand-all eval/eval-form runtime env 42)
    (eval/macroexpand-all eval/eval-form runtime env (sym/symbol nil "x"))
    (eval/macroexpand-all eval/eval-form runtime env "hello")])
  => [42 (sym/symbol nil "x") "hello"])

^{:refer kmi.lang.runtime.eval/eval-call :added "4.1"}
(fact "evaluates a function call"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var form (parser/read-string "(+ 1 2 3)"))
   (eval/get-value (eval/eval-call eval/eval-form runtime env (p/to-array form))))
  => 6)

^{:refer kmi.lang.runtime.eval/eval-list :added "4.1"}
(fact "evaluates special forms and function calls in lists"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   [(eval/get-value (eval/eval-list eval/eval-form runtime env (parser/read-string "(+ 1 2)")))
    (eval/get-value (eval/eval-list eval/eval-form runtime env (parser/read-string "(if true 1 2)")))])
  => [3 1])

^{:refer kmi.lang.runtime.eval/eval-vector :added "4.1"}
(fact "evaluates a vector literal"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var out (eval/eval-vector eval/eval-form runtime env (parser/read-string "[1 2 (+ 1 2)]")))
   (var value (eval/get-value out))
   [(eval/vector? value) (p/to-array value)])
  => [true [1 2 3]])

^{:refer kmi.lang.runtime.eval/eval-hashmap :added "4.1"}
(fact "evaluates a hash-map literal"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var form (parser/read-string "{:a 1 :b (+ 1 1)}"))
   (var out (eval/eval-hashmap eval/eval-form runtime env form))
   (var value (eval/get-value out))
   [(eval/hashmap? value)
    (hm/hashmap-lookup-key value (kw/keyword nil "a") nil)
    (hm/hashmap-lookup-key value (kw/keyword nil "b") nil)])
  => [true 1 2])

^{:refer kmi.lang.runtime.eval/eval-set :added "4.1"}
(fact "evaluates a set literal"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (var form (parser/read-string "#{1 2 (+ 1 2)}"))
   (var out (eval/eval-set eval/eval-form runtime env form))
   (eval/hashset? (eval/get-value out)))
  => true)

^{:refer kmi.lang.runtime.eval/eval-form :added "4.1"}
(fact "evaluates a single form"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   [(eval/get-value (eval/eval-form runtime env 42))
    (eval/get-value (eval/eval-form runtime env "hello"))
    (eval/errorp (eval/eval-form runtime env (sym/symbol nil "unbound")))])
  => [42 "hello" true])

^{:refer kmi.lang.runtime.eval/eval-forms :added "4.1"}
(fact "evaluates an array of forms sequentially"

  (!.js
   (var runtime (rt/empty-runtime))
   (var env (env/empty-env))
   (eval/get-value (eval/eval-forms runtime env [1 2 (+ 1 2)])))
  => 3)

^{:refer kmi.lang.runtime.eval/eval-string :added "4.1"}
(fact "evaluates a single string expression"

  (!.js
   (var out (eval/eval-string (rt/empty-runtime) "(+ 1 2)"))
   [(eval/errorp out) (eval/get-value out)])
  => [false 3])

^{:refer kmi.lang.runtime.eval/eval-string-many :added "4.1"}
(fact "evaluates all forms in a string"

  (!.js
   (var out (eval/eval-string-many (rt/empty-runtime) "(def x 1) x"))
   [(eval/errorp out) (eval/get-value out)])
  => [false 1])
