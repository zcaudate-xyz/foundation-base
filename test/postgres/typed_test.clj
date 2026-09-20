(ns postgres.typed-test
  (:use code.test)
  (:require [lang.runtime.annex.postgres.base.application :as app]
            [postgres.typed :refer :all]
            [postgres.typed.typed-common :as types]
            [postgres.typed.typed-parse :as parse]))

^{:refer postgres.typed/load-full :added "4.1"}
(fact "loads declarations from every application module"
  (let [typed (types/empty-typed)
        analysis (fn [namespace]
                   {:ns namespace
                    :enums []
                    :tables []
                    :functions [(types/make-fn-def
                                 namespace
                                 'ping
                                 []
                                 types/+unknown-type+
                                 {}
                                 nil)]
                    :variants []})]
    (with-redefs [app/app-typed (fn [_] typed)
                  app/app-modules (fn [_] [{:id 'fixture.domain}
                                           {:id 'fixture.rpc}])
                  parse/analyze-namespace analysis]
      (let [ctx (load-full "fixture")]
        [(:app-name ctx)
         (:namespaces ctx)
         (some? (entry ctx 'fixture.domain/ping))
         (some? (entry ctx 'fixture.rpc/ping))])))
  => ["fixture"
      ['fixture.domain 'fixture.rpc]
      true
      true])
