(ns postgres.typed.typed-resolve-test
  (:use code.test)
  (:require [postgres.typed.typed-common :as types]
            [postgres.typed.typed-resolve :as typed-resolve]))

^{:refer postgres.typed.typed-resolve/canonical-fn-sym :added "4.1"}
(fact "TODO")

^{:refer postgres.typed.typed-resolve/app-name-from-static :added "4.1"}
(fact "app-name-from-static normalizes static application values"
  (typed-resolve/app-name-from-static ["demo"]) => "demo"
  (typed-resolve/app-name-from-static "demo") => "demo"
  (typed-resolve/app-name-from-static nil) => nil)

^{:refer postgres.typed.typed-resolve/fn-ref->fn-sym :added "4.1"}
(fact "fn-ref->fn-sym resolves vars and symbols"
  (typed-resolve/fn-ref->fn-sym #'clojure.string/blank?) => 'clojure.string/blank?
  (typed-resolve/fn-ref->fn-sym 'blank?) => 'postgres.typed.typed-resolve-test/blank?)

^{:refer postgres.typed.typed-resolve/fn-ref->app-name :added "4.1"}
(fact "fn-ref->app-name reads application names from fn metadata"
  (typed-resolve/fn-ref->app-name
   nil
   {:body-meta {:static/application ["demo"]}})
  => "demo")

^{:refer postgres.typed.typed-resolve/resolve-called-fn :added "4.1"}
(fact "resolve-called-fn keeps qualified lookups namespace-strict"
  (types/clear-registry!)
  (let [rpc-fn (types/make-fn-def "demo.rpc" "user-set-handle" [] [:jsonb] {} nil)]
    (types/register-type! 'demo.rpc/user-set-handle rpc-fn)
    (typed-resolve/resolve-called-fn 'fuc/user-set-handle {})
    => ['fuc/user-set-handle nil]))

^{:refer postgres.typed.typed-resolve/resolve-function-def :added "4.1"}
(fact "resolve-function-def returns registered function definitions"
  (let [fn-def (types/make-fn-def "demo" "inner" [] [:jsonb] {} nil)]
    (types/register-type! 'demo/inner fn-def)
    (typed-resolve/resolve-function-def fn-def) => fn-def))