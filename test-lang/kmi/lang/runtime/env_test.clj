(ns kmi.lang.runtime.env-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.runtime.env :as env]
             [kmi.lang.type-symbol :as sym]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.runtime.env/sym-name :added "4.1"}
(fact "returns the name string of a symbol"

  (!.js
   [(env/sym-name (sym/symbol nil "foo"))
    (env/sym-name (sym/symbol "ns" "foo"))])
  => ["foo" "foo"])

^{:refer kmi.lang.runtime.env/sym-ns :added "4.1"}
(fact "returns the namespace string of a symbol"

  (!.js
   [(env/sym-ns (sym/symbol nil "foo"))
    (env/sym-ns (sym/symbol "ns" "foo"))])
  => [nil "ns"])

^{:refer kmi.lang.runtime.env/env-create :added "4.1"}
(fact "creates a lexical frame with an optional parent"

  (!.js
   (var e (env/env-create nil))
   [(== (xt/x:get-key e "parent") nil)
    (not (xt/x:has-key? (xt/x:get-key e "bindings") "x"))])
  => [true true])

^{:refer kmi.lang.runtime.env/empty-env :added "4.1"}
(fact "returns an empty top-level environment"

  (!.js
   (var e (env/empty-env))
   [(== (xt/x:get-key e "parent") nil)
    (not (xt/x:has-key? (xt/x:get-key e "bindings") "x"))])
  => [true true])

^{:refer kmi.lang.runtime.env/env-lookup :added "4.1"}
(fact "looks up a symbol in the lexical environment chain"

  (!.js
   (var parent (env/env-extend nil {"a" 1}))
   (var child (env/env-extend parent {"b" 2}))
   [(env/env-lookup parent (sym/symbol nil "a"))
    (env/env-lookup child (sym/symbol nil "b"))
    (env/env-lookup child (sym/symbol nil "a"))
    (env/env-lookup child (sym/symbol nil "c"))])
  => [1 2 1 nil])

^{:refer kmi.lang.runtime.env/env-has? :added "4.1"}
(fact "checks if a symbol is bound in the lexical environment chain"

  (!.js
   (var parent (env/env-extend nil {"a" 1}))
   (var child (env/env-extend parent {"b" 2}))
   [(env/env-has? parent (sym/symbol nil "a"))
    (env/env-has? child (sym/symbol nil "b"))
    (env/env-has? child (sym/symbol nil "a"))
    (env/env-has? child (sym/symbol nil "c"))])
  => [true true true false])

^{:refer kmi.lang.runtime.env/env-extend :added "4.1"}
(fact "creates a child frame with the given bindings"

  (!.js
   (var parent (env/env-create nil))
   (var child (env/env-extend parent {"x" 10}))
   [(== (xt/x:get-key child "parent") parent)
    (env/env-lookup child (sym/symbol nil "x"))])
  => [true 10])

^{:refer kmi.lang.runtime.env/runtime-create :added "4.1"}
(fact "creates an empty runtime seeded with default namespaces"

  (!.js
   (var rt (env/runtime-create))
   [(xt/x:get-key rt "ns")
    (xt/x:has-key? (xt/x:get-key rt "namespaces") "user")
    (xt/x:has-key? (xt/x:get-key rt "namespaces") "kmi.core")])
  => ["user" true true])

^{:refer kmi.lang.runtime.env/current-ns-name :added "4.1"}
(fact "returns the current namespace name"

  (!.js
   (env/current-ns-name (env/runtime-create)))
  => "user")

^{:refer kmi.lang.runtime.env/current-ns :added "4.1"}
(fact "returns the current namespace map"

  (!.js
   (var rt (env/runtime-create))
   (var ns (env/current-ns rt))
   [(not (xt/x:has-key? (xt/x:get-key ns "vars") "x"))
    (not (xt/x:has-key? (xt/x:get-key ns "macros") "x"))
    (not (xt/x:has-key? (xt/x:get-key ns "aliases") "x"))
    (not (xt/x:has-key? (xt/x:get-key ns "refs") "x"))])
  => [true true true true])

^{:refer kmi.lang.runtime.env/ns-lookup :added "4.1"}
(fact "looks up a symbol in the current namespace vars"

  (!.js
   (var rt (env/runtime-create))
   [(env/ns-lookup rt (sym/symbol nil "missing"))
    (env/ns-lookup (env/ns-assoc rt (sym/symbol nil "x") 42) (sym/symbol nil "x"))])
  => [nil 42])

^{:refer kmi.lang.runtime.env/ns-lookup-in :added "4.1"}
(fact "looks up a symbol in a specific namespace"

  (!.js
   (var rt0 (env/runtime-create))
   (var rt1 (env/ns-ensure rt0 "other"))
   (var rt2 (env/runtime-set-ns rt1 "other"))
   (var rt3 (env/ns-assoc rt2 (sym/symbol nil "y") 99))
   [(env/ns-lookup-in rt3 "other" (sym/symbol nil "y"))
    (env/ns-lookup-in rt3 "user" (sym/symbol nil "y"))])
  => [99 nil])

^{:refer kmi.lang.runtime.env/ns-alias :added "4.1"}
(fact "returns the namespace name for an alias in the current namespace"

  (!.js
   (var rt (env/runtime-create))
   [(env/ns-alias rt "c")
    (env/ns-alias (env/ns-set-alias rt "c" "kmi.core") "c")])
  => [nil "kmi.core"])

^{:refer kmi.lang.runtime.env/var-lookup :added "4.1"}
(fact "looks up a symbol through env, current ns, refs and kmi.core"

  (!.js
   (var rt0 (env/runtime-create))
   (var lex (env/env-extend nil {"local" 7}))
   (var rt1 (env/ns-assoc rt0 (sym/symbol nil "v") 11))
   (var rt2 (env/ns-ensure rt1 "other"))
   (var rt3 (env/runtime-set-ns rt2 "other"))
   (var rt4 (env/ns-assoc rt3 (sym/symbol nil "w") 22))
   (var rt5 (env/runtime-set-ns rt4 "user"))
   (var rt6 (env/ns-refer rt5 "other" (sym/symbol nil "w")))
   [(env/var-lookup rt6 lex (sym/symbol nil "local"))
    (env/var-lookup rt6 lex (sym/symbol nil "v"))
    (env/var-lookup rt6 lex (sym/symbol nil "w"))
    (env/var-lookup rt6 lex (sym/symbol nil "missing"))])
  => [7 11 22 nil])

^{:refer kmi.lang.runtime.env/ns-assoc :added "4.1"}
(fact "binds a symbol to a value in the current namespace"

  (!.js
   (var rt (env/ns-assoc (env/runtime-create) (sym/symbol nil "x") 100))
   (env/ns-lookup rt (sym/symbol nil "x")))
  => 100)

^{:refer kmi.lang.runtime.env/ns-assoc-macro :added "4.1"}
(fact "binds a symbol to a macro in the current namespace"

  (!.js
   (var rt (env/ns-assoc-macro (env/runtime-create) (sym/symbol nil "m") "macro-fn"))
   (env/macro-lookup rt (sym/symbol nil "m")))
  => "macro-fn")

^{:refer kmi.lang.runtime.env/macro-lookup :added "4.1"}
(fact "looks up a macro in the current namespace or kmi.core"

  (!.js
   (var rt (env/ns-assoc-macro (env/runtime-create) (sym/symbol nil "m") "macro-fn"))
   [(env/macro-lookup rt (sym/symbol nil "m"))
    (env/macro-lookup (env/runtime-create) (sym/symbol nil "m"))])
  => ["macro-fn" nil])

^{:refer kmi.lang.runtime.env/macro? :added "4.1"}
(fact "checks whether a symbol names a macro"

  (!.js
   (var rt (env/ns-assoc-macro (env/runtime-create) (sym/symbol nil "m") "macro-fn"))
   [(env/macro? rt (sym/symbol nil "m"))
    (env/macro? (env/runtime-create) (sym/symbol nil "m"))])
  => [true false])

^{:refer kmi.lang.runtime.env/runtime-set-ns :added "4.1"}
(fact "changes the current namespace of a runtime"

  (!.js
   (var rt0 (env/runtime-create))
   (var rt1 (env/ns-ensure rt0 "other"))
   [(env/current-ns-name rt1)
    (env/current-ns-name (env/runtime-set-ns rt1 "other"))])
  => ["user" "other"])

^{:refer kmi.lang.runtime.env/ns-ensure :added "4.1"}
(fact "creates a namespace if it does not already exist"

  (!.js
   (var rt0 (env/runtime-create))
   (var rt1 (env/ns-ensure rt0 "other"))
   (var rt2 (env/ns-ensure rt1 "other"))
   [(xt/x:has-key? (xt/x:get-key rt1 "namespaces") "other")
    (== rt1 rt2)])
  => [true true])

^{:refer kmi.lang.runtime.env/ns-set-alias :added "4.1"}
(fact "sets an alias in the current namespace"

  (!.js
   (var rt (env/ns-set-alias (env/runtime-create) "c" "kmi.core"))
   (env/ns-alias rt "c"))
  => "kmi.core")

^{:refer kmi.lang.runtime.env/ns-refer :added "4.1"}
(fact "refers a single var from another namespace into the current one"

  (!.js
   (var rt0 (env/runtime-create))
   (var rt1 (env/ns-ensure rt0 "other"))
   (var rt2 (env/runtime-set-ns rt1 "other"))
   (var rt3 (env/ns-assoc rt2 (sym/symbol nil "z") 55))
   (var rt4 (env/runtime-set-ns rt3 "user"))
   (var rt5 (env/ns-refer rt4 "other" (sym/symbol nil "z")))
   [(env/var-lookup rt5 (env/empty-env) (sym/symbol nil "z"))
    (env/ns-lookup rt5 (sym/symbol nil "z"))])
  => [55 nil])

^{:refer kmi.lang.runtime.env/ns-refer-all :added "4.1"}
(fact "refers all vars from a namespace into the current one"

  (!.js
   (var rt0 (env/runtime-create))
   (var rt1 (env/ns-ensure rt0 "other"))
   (var rt2 (env/runtime-set-ns rt1 "other"))
   (var rt3 (env/ns-assoc rt2 (sym/symbol nil "a") 1))
   (var rt4 (env/ns-assoc rt3 (sym/symbol nil "b") 2))
   (var rt5 (env/runtime-set-ns rt4 "user"))
   (var rt6 (env/ns-refer-all rt5 "other"))
   [(env/var-lookup rt6 (env/empty-env) (sym/symbol nil "a"))
    (env/var-lookup rt6 (env/empty-env) (sym/symbol nil "b"))
    (env/ns-lookup rt6 (sym/symbol nil "a"))])
  => [1 2 nil])
