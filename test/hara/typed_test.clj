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


(defn- sample-context
  []
  (let [fn-def (types/make-fn-def
                'sample 'lookup
                [(types/make-arg 'id types/+str-type+ [])]
                types/+int-type+ {} [] nil)
        macro-def (types/make-fn-def
                   'sample 'expand [] types/+unknown-type+
                   {:macro true} [] nil)
        value-def (types/make-value-def
                   'sample 'Count types/+int-type+ {} 3 nil)]
    (load-registry
     {'sample/lookup (assoc (types/make-registry-entry 'sample/lookup)
                            :fn fn-def)
      'sample/expand (assoc (types/make-registry-entry 'sample/expand)
                            :macro macro-def)
      'sample/Count (assoc (types/make-registry-entry 'sample/Count)
                           :value value-def)})))

^{:refer hara.typed/load-analysis :added "4.1"}
(fact "builds an isolated registry from parsed analysis and preserves the live registry"
  (let [spec (types/make-spec-def 'sample 'Role types/+str-type+ {})
        analysis {:ns 'sample :specs [spec]
                  :functions [] :macros [] :values []}]
    (with-redefs [types/*type-registry*
                  (atom {'outside/value :sentinel})]
      (let [ctx (load-analysis analysis)]
        [(:domain ctx)
         (:analysis ctx)
         (-> (entry ctx 'sample/Role) :spec :name)
         @types/*type-registry*])))
  => [:xtalk
      {:ns 'sample
       :specs [(types/make-spec-def 'sample 'Role types/+str-type+ {})]
       :functions [] :macros [] :values []}
      "Role"
      {'outside/value :sentinel}])

^{:refer hara.typed/load-registry :added "4.1"}
(fact "wraps an explicit registry without changing its identity"
  (let [registry {'sample/value :entry}
        ctx (load-registry registry)]
    [(:domain ctx) (identical? registry (:registry ctx))])
  => [:xtalk true])

^{:refer hara.typed/with-context-registry :added "4.1"}
(fact "exposes a context registry only for the callback and restores the prior registry"
  (with-redefs [types/*type-registry* (atom {:outside true})]
    [(with-context-registry {:registry {:inside true}}
       #(deref types/*type-registry*))
     @types/*type-registry*])
  => [{:inside true} {:outside true}])

^{:refer hara.typed/entries :added "4.1"}
(fact "returns every registry entry in a context"
  (set (map :symbol (entries (sample-context))))
  => '#{sample/lookup sample/expand sample/Count})

^{:refer hara.typed/entry :added "4.1"}
(fact "returns a registry entry by fully-qualified symbol"
  (let [ctx (sample-context)]
    [(:symbol (entry ctx 'sample/lookup))
     (entry ctx 'sample/missing)])
  => '[sample/lookup nil])

^{:refer hara.typed/macro-def :added "4.1"}
(fact "selects the macro declaration from a combined registry entry"
  (types/declaration-kind (macro-def (sample-context) 'sample/expand))
  => :macro)

^{:refer hara.typed/value-def :added "4.1"}
(fact "selects the value declaration from a combined registry entry"
  (let [value (value-def (sample-context) 'sample/Count)]
    [(:name value) (:raw-value value)
     (types/type->data (:type value))])
  => ["Count" 3 {:kind :primitive :name :xt/int}])

^{:refer hara.typed/missing-function! :added "4.1"}
(fact "throws a structured missing-function error"
  (missing-function! 'sample/missing)
  => (throws-info {:type :typed/missing-function
                   :fn 'sample/missing}))

^{:refer hara.typed/missing-argument! :added "4.1"}
(fact "throws a structured missing-argument error"
  (missing-argument! 'sample/lookup 'missing)
  => (throws-info {:type :typed/missing-argument
                   :fn 'sample/lookup
                   :arg 'missing}))

^{:refer hara.typed/function-def :added "4.1"}
(fact "resolves functions by symbol and accepts an existing function definition"
  (let [ctx (sample-context)
        resolved (function-def ctx 'sample/lookup)]
    [(identical? resolved (function-def ctx resolved))
     (:name resolved)])
  => [true "lookup"]
  (function-def (sample-context) 'sample/missing)
  => (throws-info {:type :typed/missing-function
                   :fn 'sample/missing}))

^{:refer hara.typed/function-output :added "4.1"}
(fact "returns the declared output type as portable data"
  (function-output (sample-context) 'sample/lookup)
  => {:kind :primitive :name :xt/int})
