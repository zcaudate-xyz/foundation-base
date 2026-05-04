(ns hara.typed.xtalk-test
  (:use code.test)
  (:require [hara.typed.xtalk :refer :all]
            [hara.typed.xtalk-common :as types]))

(defn fixture-register! []
  (clear-registry!)
  (analyze-and-register! 'hara.model.spec-xtalk-typed-fixture))

^{:refer hara.typed.xtalk/namespace-aliases :added "4.1"}
(fact "extracts namespace alias maps"
  (contains? (namespace-aliases *ns*) 'types)
  => true)

^{:refer hara.typed.xtalk/register-spec-form! :added "4.1"}
(fact "registers spec forms directly"
  (do
    (clear-registry!)
    (:name (register-spec-form! 'hara.typed.xtalk-test/LocalId :xt/str {})))
  => "LocalId")

^{:refer hara.typed.xtalk/defspec.xt :added "4.1"}
(fact "defspec macro registers specs in the current ns"
  (do
    (clear-registry!)
    (eval '(hara.typed.xtalk/defspec.xt MacroSpec :xt/int))
    (-> (get-spec 'hara.typed.xtalk-test/MacroSpec) :type types/type->data))
  => '{:kind :primitive :name :xt/int})

^{:refer hara.typed.xtalk/clear-registry! :added "4.1"}
(fact "clears registry through facade"
  (do
    (fixture-register!)
    (clear-registry!)
    (count (list-entries)))
  => 0)

^{:refer hara.typed.xtalk/register-type! :added "4.1"}
(fact "register-type delegates to common registry"
  (do
    (clear-registry!)
    (register-type! 'hara.typed.xtalk-test/ManualType
                    (types/make-spec-def 'hara.typed.xtalk-test 'ManualType types/+bool-type+ {}))
    (-> (get-spec 'hara.typed.xtalk-test/ManualType) :type types/type->data))
  => '{:kind :primitive :name :xt/bool})

^{:refer hara.typed.xtalk/get-type :added "4.1"}
(fact "get-type returns primary declarations"
  (do
    (fixture-register!)
    (-> (get-type 'hara.model.spec-xtalk-typed-fixture/User) :name))
  => "User")

^{:refer hara.typed.xtalk/get-entry :added "4.1"}
(fact "get-entry returns registry entries"
  (do
    (fixture-register!)
    (some? (get-entry 'hara.model.spec-xtalk-typed-fixture/find-user)))
  => true)

^{:refer hara.typed.xtalk/get-declaration :added "4.1"}
(fact "get-declaration returns specific declarations"
  (do
    (fixture-register!)
    (-> (get-declaration 'hara.model.spec-xtalk-typed-fixture/find-user :fn) :name))
  => "find-user")

^{:refer hara.typed.xtalk/get-spec :added "4.1"}
(fact "get-spec returns spec defs"
  (do
    (fixture-register!)
    (-> (get-spec 'hara.model.spec-xtalk-typed-fixture/UserMap) :name))
  => "UserMap")

^{:refer hara.typed.xtalk/get-function :added "4.1"}
(fact "get-function returns parsed fn defs"
  (do
    (fixture-register!)
    (-> (get-function 'hara.model.spec-xtalk-typed-fixture/find-user) :name))
  => "find-user")

^{:refer hara.typed.xtalk/get-macro :added "4.1"}
(fact "get-macro returns macro defs when present"
  (do
    (clear-registry!)
    (analyze-and-register! 'xt.lang.spec-base)
    (true? (get-in (get-macro 'xt.lang.spec-base/x:add) [:body-meta :macro])))
  => true)

^{:refer hara.typed.xtalk/get-value :added "4.1"}
(fact "get-value returns value defs when present"
  (do
    (clear-registry!)
    (analyze-and-register! 'xt.db.text.base-scope)
    (-> (get-value 'xt.db.text.base-scope/Scopes) :name))
  => "Scopes")

^{:refer hara.typed.xtalk/list-specs :added "4.1"}
(fact "lists specs through facade"
  (do (fixture-register!) (pos? (count (list-specs))))
  => true)

^{:refer hara.typed.xtalk/list-entries :added "4.1"}
(fact "lists entries through facade"
  (do (fixture-register!) (pos? (count (list-entries))))
  => true)

^{:refer hara.typed.xtalk/list-functions :added "4.1"}
(fact "lists functions through facade"
  (do (fixture-register!) (pos? (count (list-functions))))
  => true)

^{:refer hara.typed.xtalk/list-macros :added "4.1"}
(fact "lists macros through facade"
  (do
    (clear-registry!)
    (analyze-and-register! 'xt.lang.spec-base)
    (pos? (count (list-macros))))
  => true)

^{:refer hara.typed.xtalk/list-values :added "4.1"}
(fact "lists values through facade"
  (do
    (clear-registry!)
    (analyze-and-register! 'xt.db.text.base-scope)
    (pos? (count (list-values))))
  => true)

^{:refer hara.typed.xtalk/analyze-file :added "4.1"}
(fact "analyzes files through facade"
  (count (:specs (analyze-file "test/hara.lang/model/spec_xtalk_typed_fixture.clj")))
  => 3)

^{:refer hara.typed.xtalk/analyze-file-raw :added "4.1"}
(fact "delegates file-raw analysis to xtalk-parse"
  (let [result (analyze-file-raw "test/hara.lang/model/spec_xtalk_typed_fixture.clj")]
    [(map? result)
     (contains? result :specs)
     (contains? result :functions)])
  => [true true true])

^{:refer hara.typed.xtalk/analyze-namespace :added "4.1"}
(fact "analyzes namespaces through facade"
  (:ns (analyze-namespace 'hara.model.spec-xtalk-typed-fixture))
  => 'hara.model.spec-xtalk-typed-fixture)

^{:refer hara.typed.xtalk/analyze-namespace-raw :added "4.1"}
(fact "exposes raw namespace analysis through facade"
  (->> (analyze-namespace-raw 'hara.model.spec-xtalk-typed-fixture)
       :functions
       (some #(when (= "find-user" (:name %)) %))
       :output
       :name)
  => :xt/unknown)

^{:refer hara.typed.xtalk/analyze-and-register! :added "4.1"}
(fact "registers namespaces through facade"
  (do
    (clear-registry!)
    (some? (analyze-and-register! 'hara.model.spec-xtalk-typed-fixture)))
  => true)

^{:refer hara.typed.xtalk/check-function :added "4.1"}
(fact "checks functions through facade"
  (do
    (clear-registry!)
    (-> (check-function 'hara.model.spec-xtalk-typed-fixture/find-user) :errors))
  => [])

^{:refer hara.typed.xtalk/check-namespace :added "4.1"}
(fact "checks namespaces through facade"
  (do
    (clear-registry!)
    (:namespace (check-namespace 'xt.event.base-route)))
  => 'xt.event.base-route)
