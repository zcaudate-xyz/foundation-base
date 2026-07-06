(ns hara.typed-test
  (:refer-clojure :exclude [load-file])
  (:use code.test)
  (:require [hara.typed :refer :all]
            [hara.typed.xtalk-common :as types]))

^{:refer hara.typed/namespace-aliases :added "4.1"}
(fact "returns alias to namespace name map"
  (namespace-aliases (find-ns 'hara.typed-test))
  => '{types hara.typed.xtalk-common})

^{:refer hara.typed/register-spec-form! :added "4.1"}
(fact "registers a spec declaration from a form"
  (do
    (types/clear-registry!)
    (register-spec-form! 'sample.route/Role :xt/str {} {})
    (:name (spec-def (load-registry) 'sample.route/Role)))
  => "Role")

^{:refer hara.typed/defspec.xt :added "4.1"}
(fact "defines a spec via macro"
  (do
    (types/clear-registry!)
    (defspec.xt Item :xt/int)
    (types/type->data (:type (spec-def (load-registry) 'hara.typed-test/Item))))
  => '{:kind :primitive :name :xt/int})

^{:refer hara.typed/load-file :added "4.1"}
(fact "creates an xtalk typed context from a source file"
  (let [ctx (load-file "test/hara/model/spec_xtalk_typed_fixture.clj")]
    [(:domain ctx)
     (some? (entry ctx 'hara.model.spec-xtalk-typed-fixture/find-user))])
  => [:xtalk true])

^{:refer hara.typed/load-ns :added "4.1"}
(fact "creates an xtalk typed context from a namespace"
  (let [ctx (load-ns 'hara.model.spec-xtalk-typed-fixture)]
    [(:domain ctx)
     (some? (entry ctx 'hara.model.spec-xtalk-typed-fixture/find-user))
     (pos? (count (entries ctx)))])
  => [:xtalk true true])

^{:refer hara.typed/spec-def :added "4.1"}
(fact "returns declarations from an xtalk context"
  (let [ctx (load-ns 'hara.model.spec-xtalk-typed-fixture)]
    [(:name (spec-def ctx 'hara.model.spec-xtalk-typed-fixture/User))
     (:name (function-def ctx 'hara.model.spec-xtalk-typed-fixture/find-user))
     (nil? (macro-def ctx 'hara.model.spec-xtalk-typed-fixture/find-user))
     (nil? (value-def ctx 'hara.model.spec-xtalk-typed-fixture/find-user))])
  => ["User" "find-user" true true])

^{:refer hara.typed/declaration :added "4.1"}
(fact "distinguishes spec macro and value declarations"
  (let [ctx (load-analysis
             {:ns 'combined
              :specs (:specs (:analysis (load-ns 'xt.lang.spec-base)))
              :functions (:functions (:analysis (load-ns 'xt.lang.spec-base)))
              :macros (:macros (:analysis (load-ns 'xt.lang.spec-base)))
              :values (:values (:analysis (load-ns 'xt.db.text.base-scope)))})]
    [(-> (entry ctx 'xt.lang.spec-base/x:add) types/entry-kinds set)
     (types/declaration-kind (macro-def ctx 'xt.lang.spec-base/x:add))
     (types/declaration-kind (value-def ctx 'xt.db.text.base-scope/Scopes))
     (some? (declaration ctx 'xt.db.text.base-scope/Scopes :value))])
  => '[#{:macro :spec} :macro :value true])

^{:refer hara.typed/function-report :added "4.1"}
(fact "checks a function through an xtalk typed context"
  (let [report (function-report (load-ns 'hara.model.spec-xtalk-typed-fixture)
                                'hara.model.spec-xtalk-typed-fixture/find-user)]
    [(:function report) (:errors report)])
  => '[hara.model.spec-xtalk-typed-fixture/find-user []])

^{:refer hara.typed/function-input :added "4.1"}
(fact "returns xtalk function input and output types from context"
  (let [ctx (load-ns 'hara.model.spec-xtalk-typed-fixture)]
    [(function-input ctx 'hara.model.spec-xtalk-typed-fixture/find-user 'id)
     (function-output ctx 'hara.model.spec-xtalk-typed-fixture/find-user)])
  => '[{:kind :primitive :name :xt/str}
       {:kind :maybe :item {:kind :named :name hara.model.spec-xtalk-typed-fixture/User}}])

^{:refer hara.typed/namespace-report :added "4.1"}
(fact "checks every function in an xtalk context namespace"
  (let [report (namespace-report (load-ns 'hara.model.spec-xtalk-typed-fixture))]
    [(:namespace report)
     (some #(= 'hara.model.spec-xtalk-typed-fixture/find-user (:function %))
           (:functions report))])
  => '[hara.model.spec-xtalk-typed-fixture true])
