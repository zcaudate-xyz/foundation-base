tahto/typed_test.clj:1:(ns tahto.typed-test
  (:refer-clojure :exclude [load-file])
  (:use code.test)
tahto/typed_test.clj:4:  (:require [tahto.typed :refer :all]
tahto/typed_test.clj:5:            [tahto.typed.xtalk-common :as types]))

tahto/typed_test.clj:7:^{:refer tahto.typed/namespace-aliases :added "4.1"}
(fact "returns alias to namespace name map"
tahto/typed_test.clj:9:  (namespace-aliases (find-ns 'tahto.typed-test))
tahto/typed_test.clj:10:  => '{types tahto.typed.xtalk-common})

tahto/typed_test.clj:12:^{:refer tahto.typed/register-spec-form! :added "4.1"}
(fact "registers a spec declaration from a form"
  (do
    (types/clear-registry!)
    (register-spec-form! 'sample.route/Role :xt/str {} {})
    (:name (spec-def (load-registry) 'sample.route/Role)))
  => "Role")

tahto/typed_test.clj:20:^{:refer tahto.typed/defspec.xt :added "4.1"}
(fact "defines a spec via macro"
  (do
    (types/clear-registry!)
    (defspec.xt Item :xt/int)
tahto/typed_test.clj:25:    (types/type->data (:type (spec-def (load-registry) 'tahto.typed-test/Item))))
  => '{:kind :primitive :name :xt/int})

tahto/typed_test.clj:28:^{:refer tahto.typed/load-file :added "4.1"}
(fact "creates an xtalk typed context from a source file"
tahto/typed_test.clj:30:  (let [ctx (load-file "test/tahto/model/spec_xtalk_typed_fixture.clj")]
    [(:domain ctx)
tahto/typed_test.clj:32:     (some? (entry ctx 'tahto.model.spec-xtalk-typed-fixture/find-user))])
  => [:xtalk true])

tahto/typed_test.clj:35:^{:refer tahto.typed/load-ns :added "4.1"}
(fact "creates an xtalk typed context from a namespace"
tahto/typed_test.clj:37:  (let [ctx (load-ns 'tahto.model.spec-xtalk-typed-fixture)]
    [(:domain ctx)
tahto/typed_test.clj:39:     (some? (entry ctx 'tahto.model.spec-xtalk-typed-fixture/find-user))
     (pos? (count (entries ctx)))])
  => [:xtalk true true])

tahto/typed_test.clj:43:^{:refer tahto.typed/spec-def :added "4.1"}
(fact "returns declarations from an xtalk context"
tahto/typed_test.clj:45:  (let [ctx (load-ns 'tahto.model.spec-xtalk-typed-fixture)]
tahto/typed_test.clj:46:    [(:name (spec-def ctx 'tahto.model.spec-xtalk-typed-fixture/User))
tahto/typed_test.clj:47:     (:name (function-def ctx 'tahto.model.spec-xtalk-typed-fixture/find-user))
tahto/typed_test.clj:48:     (nil? (macro-def ctx 'tahto.model.spec-xtalk-typed-fixture/find-user))
tahto/typed_test.clj:49:     (nil? (value-def ctx 'tahto.model.spec-xtalk-typed-fixture/find-user))])
  => ["User" "find-user" true true])

tahto/typed_test.clj:52:^{:refer tahto.typed/declaration :added "4.1"}
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

tahto/typed_test.clj:66:^{:refer tahto.typed/function-report :added "4.1"}
(fact "checks a function through an xtalk typed context"
tahto/typed_test.clj:68:  (let [report (function-report (load-ns 'tahto.model.spec-xtalk-typed-fixture)
tahto/typed_test.clj:69:                                'tahto.model.spec-xtalk-typed-fixture/find-user)]
    [(:function report) (:errors report)])
tahto/typed_test.clj:71:  => '[tahto.model.spec-xtalk-typed-fixture/find-user []])

tahto/typed_test.clj:73:^{:refer tahto.typed/function-input :added "4.1"}
(fact "returns xtalk function input and output types from context"
tahto/typed_test.clj:75:  (let [ctx (load-ns 'tahto.model.spec-xtalk-typed-fixture)]
tahto/typed_test.clj:76:    [(function-input ctx 'tahto.model.spec-xtalk-typed-fixture/find-user 'id)
tahto/typed_test.clj:77:     (function-output ctx 'tahto.model.spec-xtalk-typed-fixture/find-user)])
  => '[{:kind :primitive :name :xt/str}
tahto/typed_test.clj:79:       {:kind :maybe :item {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}}])

tahto/typed_test.clj:81:^{:refer tahto.typed/namespace-report :added "4.1"}
(fact "checks every function in an xtalk context namespace"
tahto/typed_test.clj:83:  (let [report (namespace-report (load-ns 'tahto.model.spec-xtalk-typed-fixture))]
    [(:namespace report)
tahto/typed_test.clj:85:     (some #(= 'tahto.model.spec-xtalk-typed-fixture/find-user (:function %))
           (:functions report))])
tahto/typed_test.clj:87:  => '[tahto.model.spec-xtalk-typed-fixture true])


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

tahto/typed_test.clj:109:^{:refer tahto.typed/load-analysis :added "4.1"}
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

tahto/typed_test.clj:128:^{:refer tahto.typed/load-registry :added "4.1"}
(fact "wraps an explicit registry without changing its identity"
  (let [registry {'sample/value :entry}
        ctx (load-registry registry)]
    [(:domain ctx) (identical? registry (:registry ctx))])
  => [:xtalk true])

tahto/typed_test.clj:135:^{:refer tahto.typed/with-context-registry :added "4.1"}
(fact "exposes a context registry only for the callback and restores the prior registry"
  (with-redefs [types/*type-registry* (atom {:outside true})]
    [(with-context-registry {:registry {:inside true}}
       #(deref types/*type-registry*))
     @types/*type-registry*])
  => [{:inside true} {:outside true}])

tahto/typed_test.clj:143:^{:refer tahto.typed/entries :added "4.1"}
(fact "returns every registry entry in a context"
  (set (map :symbol (entries (sample-context))))
  => '#{sample/lookup sample/expand sample/Count})

tahto/typed_test.clj:148:^{:refer tahto.typed/entry :added "4.1"}
(fact "returns a registry entry by fully-qualified symbol"
  (let [ctx (sample-context)]
    [(:symbol (entry ctx 'sample/lookup))
     (entry ctx 'sample/missing)])
  => '[sample/lookup nil])

tahto/typed_test.clj:155:^{:refer tahto.typed/macro-def :added "4.1"}
(fact "selects the macro declaration from a combined registry entry"
  (types/declaration-kind (macro-def (sample-context) 'sample/expand))
  => :macro)

tahto/typed_test.clj:160:^{:refer tahto.typed/value-def :added "4.1"}
(fact "selects the value declaration from a combined registry entry"
  (let [value (value-def (sample-context) 'sample/Count)]
    [(:name value) (:raw-value value)
     (types/type->data (:type value))])
  => ["Count" 3 {:kind :primitive :name :xt/int}])

tahto/typed_test.clj:167:^{:refer tahto.typed/missing-function! :added "4.1"}
(fact "throws a structured missing-function error"
  (missing-function! 'sample/missing)
  => (throws-info {:type :typed/missing-function
                   :fn 'sample/missing}))

tahto/typed_test.clj:173:^{:refer tahto.typed/missing-argument! :added "4.1"}
(fact "throws a structured missing-argument error"
  (missing-argument! 'sample/lookup 'missing)
  => (throws-info {:type :typed/missing-argument
                   :fn 'sample/lookup
                   :arg 'missing}))

tahto/typed_test.clj:180:^{:refer tahto.typed/function-def :added "4.1"}
(fact "resolves functions by symbol and accepts an existing function definition"
  (let [ctx (sample-context)
        resolved (function-def ctx 'sample/lookup)]
    [(identical? resolved (function-def ctx resolved))
     (:name resolved)])
  => [true "lookup"]
  (function-def (sample-context) 'sample/missing)
  => (throws-info {:type :typed/missing-function
                   :fn 'sample/missing}))

tahto/typed_test.clj:191:^{:refer tahto.typed/function-output :added "4.1"}
(fact "returns the declared output type as portable data"
  (function-output (sample-context) 'sample/lookup)
  => {:kind :primitive :name :xt/int})
