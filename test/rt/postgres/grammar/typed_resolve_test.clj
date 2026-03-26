(ns rt.postgres.grammar.typed-resolve-test
  (:use code.test)
  (:require [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-resolve :as typed-resolve]))

^{:refer rt.postgres.grammar.typed-resolve/app-name-from-static :added "4.1"}
(fact "app-name-from-static normalizes static application values"
  (typed-resolve/app-name-from-static ["demo"]) => "demo"
  (typed-resolve/app-name-from-static "demo") => "demo"
  (typed-resolve/app-name-from-static nil) => nil)

^{:refer rt.postgres.grammar.typed-resolve/fn-ref->fn-sym :added "4.1"}
(fact "fn-ref->fn-sym resolves vars and symbols"
  (typed-resolve/fn-ref->fn-sym #'clojure.string/blank?) => 'clojure.string/blank?
  (typed-resolve/fn-ref->fn-sym 'blank?) => 'rt.postgres.grammar.typed-resolve-test/blank?)

^{:refer rt.postgres.grammar.typed-resolve/fn-ref->app-name :added "4.1"}
(fact "fn-ref->app-name reads application names from fn metadata"
  (typed-resolve/fn-ref->app-name
   nil
   {:body-meta {:static/application ["demo"]}})
  => "demo")

^{:refer rt.postgres.grammar.typed-resolve/resolve-called-fn :added "4.1"}
(fact "resolve-called-fn applies aliases before registry lookup"
  (types/clear-registry!)
  (let [fn-def (types/make-fn-def "demo" "inner" [] [:jsonb] {} nil)]
    (types/register-type! 'demo/inner fn-def)
    (typed-resolve/resolve-called-fn 'x/inner {'x 'demo})
    => ['demo/inner fn-def]))

^{:refer rt.postgres.grammar.typed-resolve/resolve-called-fn :added "4.1"}
(fact "resolve-called-fn keeps qualified lookups namespace-strict"
  (types/clear-registry!)
  (let [rpc-fn (types/make-fn-def "demo.rpc" "user-set-handle" [] [:jsonb] {} nil)]
    (types/register-type! 'demo.rpc/user-set-handle rpc-fn)
    (typed-resolve/resolve-called-fn 'fuc/user-set-handle {})
    => ['fuc/user-set-handle nil]))

^{:refer rt.postgres.grammar.typed-resolve/resolve-function-def :added "4.1"}
(fact "resolve-function-def returns registered function definitions"
  (let [fn-def (types/make-fn-def "demo" "inner" [] [:jsonb] {} nil)]
    (types/register-type! 'demo/inner fn-def)
    (typed-resolve/resolve-function-def fn-def) => fn-def))
