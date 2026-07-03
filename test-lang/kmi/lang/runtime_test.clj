(ns kmi.lang.runtime-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.transport-memory :as transport-memory]
             [kmi.lang.runtime :as rt]
             [kmi.lang.runtime.eval :as rev]
             [kmi.lang.protocol-base :as proto]
             [kmi.lang.type-keyword :as kw]
             [kmi.lang.type-symbol :as sym]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer kmi.lang.runtime/empty-runtime :added "4.1"}
(fact "creates an empty runtime seeded with primitives"

  (!.js
   (var runtime (rt/empty-runtime))
   [(xt/x:get-key runtime "ns")
    (xt/x:has-key? (xt/x:get-key (xt/x:get-key (xt/x:get-key runtime "namespaces") "kmi.core") "vars") "+")
    (xt/x:has-key? (xt/x:get-key (xt/x:get-key (xt/x:get-key runtime "namespaces") "kmi.core") "vars") "list")])
  => ["user" true true])

^{:refer kmi.lang.runtime/eval-string :added "4.1"}
(fact "evaluates self-evaluating literals"

  (!.js
   [(xt/x:get-key (rt/eval-string (rt/empty-runtime) "42") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "\"hello\"") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "true") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "nil") "value")
    (== nil (xt/x:get-key (rt/eval-string (rt/empty-runtime) "nil") "error"))])
  => [42 "hello" true nil true])

(fact "evaluates arithmetic and comparison"

  (!.js
   [(xt/x:get-key (rt/eval-string (rt/empty-runtime) "(+ 1 2)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(- 10 3)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(* 4 5)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(/ 10 2)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(< 1 2)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(== 1 1)") "value")])
  => [3 7 20 5 true true])

(fact "quote returns the form unevaluated"

  (!.js
   (var out (rt/eval-string (rt/empty-runtime) "(quote (+ 1 2))"))
   (var value (xt/x:get-key out "value"))
   [(rev/list? value)
    (rev/symbol? (xt/x:first (proto/to-array value)))])
  => [true true])

(fact "if branches on truthiness"

  (!.js
   [(xt/x:get-key (rt/eval-string (rt/empty-runtime) "(if true 1 2)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(if false 1 2)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(if nil 1 2)") "value")])
  => [1 2 2])

(fact "do evaluates in sequence"

  (!.js
   (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(do 1 2 3)") "value"))
  => 3)

(fact "let binds values in a local scope"

  (!.js
   (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(let [x 10 y 20] (+ x y))") "value"))
  => 30)

(fact "fn creates applicable closures"

  (!.js
   (xt/x:get-key (rt/eval-string (rt/empty-runtime) "((fn [x] (+ x 1)) 5)") "value"))
  => 6)

(fact "closures capture lexical bindings"

  (!.js
   (xt/x:get-key (rt/eval-string (rt/empty-runtime)
                                 "(let [x 10] ((fn [y] (+ x y)) 5))")
                 "value"))
  => 15)

(fact "def stores vars across evaluations"

  (!.js
   (var runtime (rt/empty-runtime))
   (var out1 (rt/eval-string runtime "(def x 7)"))
   (var out2 (rt/eval-string (xt/x:get-key out1 "runtime") "x"))
   [(xt/x:get-key out1 "value")
    (xt/x:get-key out2 "value")])
  => [7 7])

(fact "recursion via def computes factorial"

  (!.js
   (var runtime (rt/empty-runtime))
   (var out (rt/eval-string runtime
                            "(do (def fact (fn [n] (if (== n 0) 1 (* n (fact (- n 1)))))) (fact 5))"))
   (xt/x:get-key out "value"))
  => 120)

(fact "collection literals evaluate their elements"

  (!.js
   (var out (rt/eval-string (rt/empty-runtime) "[1 2 (+ 1 2)]"))
   (var value (xt/x:get-key out "value"))
   [(rev/vector? value)
    (proto/to-array value)])
  => [true [1 2 3]])

(fact "errors are returned without throwing"

  (!.js
   (var out (rt/eval-string (rt/empty-runtime) "x"))
   [(== nil (xt/x:get-key out "value"))
    (xt/x:is-string? (xt/x:get-key out "error"))])
  => [true true])

^{:refer kmi.lang.runtime/create-node :added "4.1"}
(fact "creates a substrate node with runtime handlers"

  (notify/wait-on :js
    (var node (rt/create-node {}))
    (repl/notify (substrate/list-handlers node)))
  => ["@kmi.lang/describe"
      "@kmi.lang/eval"
      "@kmi.lang/load"
      "@kmi.lang/read"])

(fact "substrate eval request returns the value"

  (notify/wait-on :js
    (var node (rt/create-node {}))
    (-> (substrate/request node "kmi.session" "@kmi.lang/eval" ["(+ 1 2)"] {})
        (promise/x:promise-then
         (fn [res]
           (repl/notify res)))))
  => {"value" 3})

(fact "substrate eval threads runtime state through session space"

  (notify/wait-on :js
    (var node (rt/create-node {}))
    (-> (substrate/request node "kmi.session" "@kmi.lang/eval" ["(def x 42)"] {})
        (promise/x:promise-then
         (fn [_]
           (return
            (substrate/request node "kmi.session" "@kmi.lang/eval" ["x"] {}))))
        (repl/notify)))
  => {"value" 42})

(fact "two nodes talk over a memory transport"

  (notify/wait-on :js
    (var server (rt/create-node {"id" "server"}))
    (var client (rt/create-node {"id" "client"}))
    (var wire (transport-memory/memory-pair {"left_id" "client"
                                             "right_id" "server"}))
    (-> (promise/x:promise-all
         [(substrate/attach-transport
           client
           "server"
           (transport-memory/text-endpoint (xt/x:get-key wire "left")))
          (substrate/attach-transport
           server
           "client"
           (transport-memory/text-endpoint (xt/x:get-key wire "right")))])
        (promise/x:promise-then
         (fn [_]
           (return
            (substrate/request
             client
             "kmi.session"
             "@kmi.lang/eval"
             ["(+ 3 4)"]
             nil))))
        (repl/notify)))
  => {"value" 7})


^{:refer kmi.lang.runtime/read-string :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.runtime/read-many :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.runtime/eval-form :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.runtime/eval-string-many :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.runtime/handler-read :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.runtime/handler-eval :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.runtime/handler-load :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.runtime/handler-describe :added "4.1"}
(fact "TODO")

^{:refer kmi.lang.runtime/stop :added "4.1"}
(fact "TODO")