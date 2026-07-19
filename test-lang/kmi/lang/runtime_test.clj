(ns kmi.lang.runtime-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
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

^{:refer kmi.lang.runtime/eval-string :id kmi-extra-1}
(fact "evaluates arithmetic and comparison"

  (!.js
   [(xt/x:get-key (rt/eval-string (rt/empty-runtime) "(+ 1 2)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(- 10 3)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(* 4 5)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(/ 10 2)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(< 1 2)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(== 1 1)") "value")])
  => [3 7 20 5 true true])

^{:refer kmi.lang.runtime/eval-string :id kmi-extra-2}
(fact "quote returns the form unevaluated"

  (!.js
   (var out (rt/eval-string (rt/empty-runtime) "(quote (+ 1 2))"))
   (var value (xt/x:get-key out "value"))
   [(rev/list? value)
    (rev/symbol? (xt/x:first (proto/to-array value)))])
  => [true true])

^{:refer kmi.lang.runtime/eval-string :id kmi-extra-3}
(fact "if branches on truthiness"

  (!.js
   [(xt/x:get-key (rt/eval-string (rt/empty-runtime) "(if true 1 2)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(if false 1 2)") "value")
    (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(if nil 1 2)") "value")])
  => [1 2 2])

^{:refer kmi.lang.runtime/eval-string :id kmi-extra-4}
(fact "do evaluates in sequence"

  (!.js
   (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(do 1 2 3)") "value"))
  => 3)

^{:refer kmi.lang.runtime/eval-string :id kmi-extra-5}
(fact "let binds values in a local scope"

  (!.js
   (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(let [x 10 y 20] (+ x y))") "value"))
  => 30)

^{:refer kmi.lang.runtime/eval-string :id kmi-extra-6}
(fact "fn creates applicable closures"

  (!.js
   (xt/x:get-key (rt/eval-string (rt/empty-runtime) "((fn [x] (+ x 1)) 5)") "value"))
  => 6)

^{:refer kmi.lang.runtime/eval-string :id kmi-extra-7}
(fact "closures capture lexical bindings"

  (!.js
   (xt/x:get-key (rt/eval-string (rt/empty-runtime)
                                 "(let [x 10] ((fn [y] (+ x y)) 5))")
                 "value"))
  => 15)

^{:refer kmi.lang.runtime/eval-string-many :id kmi-extra-8}
(fact "def stores vars across evaluations"

  (!.js
   (var runtime (rt/empty-runtime))
   (var out1 (rt/eval-string runtime "(def x 7)"))
   (var out2 (rt/eval-string (xt/x:get-key out1 "runtime") "x"))
   [(xt/x:get-key out1 "value")
    (xt/x:get-key out2 "value")])
  => [7 7])

^{:refer kmi.lang.runtime/eval-string-many :id kmi-extra-9}
(fact "recursion via def computes factorial"

  (!.js
   (var runtime (rt/empty-runtime))
   (var out (rt/eval-string runtime
                            "(do (def fact (fn [n] (if (== n 0) 1 (* n (fact (- n 1)))))) (fact 5))"))
   (xt/x:get-key out "value"))
  => 120)

^{:refer kmi.lang.runtime/eval-string :id kmi-extra-10}
(fact "collection literals evaluate their elements"

  (!.js
   (var out (rt/eval-string (rt/empty-runtime) "[1 2 (+ 1 2)]"))
   (var value (xt/x:get-key out "value"))
   [(rev/vector? value)
    (proto/to-array value)])
  => [true [1 2 3]])

^{:refer kmi.lang.runtime/eval-string :id kmi-extra-11}
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
  => ["@/echo"
      "@/get-service"
      "@/list-handlers"
      "@/list-spaces"
      "@/list-transports"
      "@/list-triggers"
      "@/node-info"
      "@/ping"
      "@kmi.lang/describe"
      "@kmi.lang/eval"
      "@kmi.lang/load"
      "@kmi.lang/read"])

^{:refer kmi.lang.runtime/create-node :id kmi-extra-12}
(fact "substrate eval request returns the value"

  (notify/wait-on :js
    (var node (rt/create-node {}))
    (-> (substrate/request node "kmi.session" "@kmi.lang/eval" ["(+ 1 2)"] {})
        (promise/x:promise-then
         (fn [res]
           (repl/notify res)))))
  => {"value" 3})

^{:refer kmi.lang.runtime/create-node :id kmi-extra-13}
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

^{:refer kmi.lang.runtime/create-node :id kmi-extra-14}
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
(fact "reads primitive and managed forms"

  (!.js
   [(rt/read-string "42")
    (rt/read-string "\"hello\"")
    (rt/read-string "true")
    (rt/read-string "nil")
    (rev/symbol? (rt/read-string "+"))
    (rev/keyword? (rt/read-string ":key"))
    (rev/list? (rt/read-string "(+ 1 2)"))
    (rev/vector? (rt/read-string "[1 2]"))
    (xt/x:get-key (rt/read-string ":key") "_name")
    (xt/x:len (proto/to-array (rt/read-string "(+ 1 2)")))])
  => [42 "hello" true nil true true true true "key" 3])

^{:refer kmi.lang.runtime/read-string :id kmi-extra-15}
(fact "reads only the first form"

  (!.js
   (rt/read-string "1 2 3"))
  => 1)

^{:refer kmi.lang.runtime/read-many :added "4.1"}
(fact "reads all forms from a string"

  (!.js
   (var forms (rt/read-many "1 2 (+ 1 2)"))
   [(xt/x:len forms)
    (xt/x:get-idx forms 0)
    (xt/x:get-idx forms 1)
    (rev/list? (xt/x:get-idx forms 2))])
  => [3 1 2 true])

^{:refer kmi.lang.runtime/read-many :id kmi-extra-16}
(fact "returns an empty array for an empty string"

  (!.js
   (rt/read-many ""))
  => [])

^{:refer kmi.lang.runtime/eval-form :added "4.1"}
(fact "evaluates a self-evaluating form"

  (!.js
   (var out (rt/eval-form (rt/empty-runtime) 42))
   [(xt/x:get-key out "value")
    (xt/x:get-key (xt/x:get-key out "runtime") "ns")])
  => [42 "user"])

^{:refer kmi.lang.runtime/eval-form :id kmi-extra-17}
(fact "evaluates a function call form"

  (!.js
   (var out (rt/eval-form (rt/empty-runtime) (rt/read-string "(+ 1 2)")))
   (xt/x:get-key out "value"))
  => 3)

^{:refer kmi.lang.runtime/eval-form :id kmi-extra-18}
(fact "returns an error for unbound symbols"

  (!.js
   (var out (rt/eval-form (rt/empty-runtime) (rt/read-string "x")))
   (xt/x:has-key? out "error"))
  => true)

^{:refer kmi.lang.runtime/eval-string-many :added "4.1"}
(fact "evaluates multiple forms and returns the last value"

  (!.js
   (var out (rt/eval-string-many (rt/empty-runtime) "1 2 3"))
   (xt/x:get-key out "value"))
  => 3)

^{:refer kmi.lang.runtime/eval-string-many :id kmi-extra-19}
(fact "threads runtime state across forms"

  (!.js
   (var out (rt/eval-string-many (rt/empty-runtime) "(def x 5) (+ x 1)"))
   [(xt/x:get-key out "value")
    (xt/x:get-key (xt/x:get-key (xt/x:get-key (xt/x:get-key (xt/x:get-key out "runtime") "namespaces") "user") "vars") "x")])
  => [6 5])

^{:refer kmi.lang.runtime/handler-read :added "4.1"}
(fact "substrate read request returns the parsed form"

  (notify/wait-on :js
    (var node (rt/create-node {}))
    (-> (substrate/request node "kmi.session" "@kmi.lang/read" ["42"] {})
        (promise/x:promise-then
         (fn [res]
           (repl/notify res)))))
  => {"form" 42})

^{:refer kmi.lang.runtime/handler-eval :added "4.1"}
(fact "substrate eval request returns errors for invalid input"

  (notify/wait-on :js
    (var node (rt/create-node {}))
    (-> (substrate/request node "kmi.session" "@kmi.lang/eval" ["x"] {})
        (promise/x:promise-then
         (fn [res]
           (repl/notify {"has-error" (xt/x:is-string? (xt/x:get-key res "error"))})))))
  => {"has-error" true})

^{:refer kmi.lang.runtime/handler-load :added "4.1"}
(fact "substrate load request evaluates many forms and threads state"

  (notify/wait-on :js
    (var node (rt/create-node {}))
    (-> (substrate/request node "kmi.session" "@kmi.lang/load" ["(def z 8) (+ z 2)"] {})
        (promise/x:promise-then
         (fn [_]
           (return
            (substrate/request node "kmi.session" "@kmi.lang/eval" ["z"] {}))))
        (promise/x:promise-then
         (fn [res]
           (repl/notify res)))))
  => {"value" 8})

^{:refer kmi.lang.runtime/handler-describe :added "4.1"}
(fact "substrate describe request returns metadata for a managed form"

  (notify/wait-on :js
    (var node (rt/create-node {}))
    (-> (substrate/request node "kmi.session" "@kmi.lang/describe" ["[1 2 3]"] {})
        (promise/x:promise-then
         (fn [res]
           (repl/notify res)))))
  => {"tag" "vector" "type" "object" "size" 3 "string" "[1, 2, 3]"})

^{:refer kmi.lang.runtime/create-node :id kmi-extra-20}
(fact "substrate describe request returns metadata for primitives"

  (notify/wait-on :js
    (var node (rt/create-node {}))
    (-> (substrate/request node "kmi.session" "@kmi.lang/describe" ["42"] {})
        (promise/x:promise-then
         (fn [res]
           (repl/notify res)))))
  => {"tag" "number" "type" "number" "size" nil "string" "42"})

^{:refer kmi.lang.runtime/stop :added "4.1"}
(fact "placeholder stop returns true"

  (!.js
   (rt/stop (rt/create-node {})))
  => true)
