(ns std.lang.typed.xtalk-test
  (:use code.test)
  (:require [std.lang.typed.xtalk :refer :all]
            [std.lang.typed.xtalk-common :as types]))

(defn fixture-register! []
  (clear-registry!)
  (analyze-and-register! 'std.lang.model.spec-xtalk-typed-fixture))

^{:refer std.lang.typed.xtalk/namespace-aliases :added "4.1"}
(fact "extracts namespace alias maps"
  (contains? (namespace-aliases *ns*) 'types)
  => true)

^{:refer std.lang.typed.xtalk/register-spec-form! :added "4.1"}
(fact "registers spec forms directly"
  (do
    (clear-registry!)
    (:name (register-spec-form! 'std.lang.typed.xtalk-test/LocalId :xt/str {})))
  => "LocalId")

^{:refer std.lang.typed.xtalk/defspec.xt :added "4.1"}
(fact "defspec macro registers specs in the current ns"
  (do
    (clear-registry!)
    (eval '(std.lang.typed.xtalk/defspec.xt MacroSpec :xt/int))
    (-> (get-spec 'std.lang.typed.xtalk-test/MacroSpec) :type types/type->data))
  => '{:kind :primitive :name :xt/int})

^{:refer std.lang.typed.xtalk/clear-registry! :added "4.1"}
(fact "clears registry through facade"
  (do
    (fixture-register!)
    (clear-registry!)
    (count (list-entries)))
  => 0)

^{:refer std.lang.typed.xtalk/register-type! :added "4.1"}
(fact "register-type delegates to common registry"
  (do
    (clear-registry!)
    (register-type! 'std.lang.typed.xtalk-test/ManualType
                    (types/make-spec-def 'std.lang.typed.xtalk-test 'ManualType types/+bool-type+ {}))
    (-> (get-spec 'std.lang.typed.xtalk-test/ManualType) :type types/type->data))
  => '{:kind :primitive :name :xt/bool})

^{:refer std.lang.typed.xtalk/get-type :added "4.1"}
(fact "get-type returns primary declarations"
  (do
    (fixture-register!)
    (-> (get-type 'std.lang.model.spec-xtalk-typed-fixture/User) :name))
  => "User")

^{:refer std.lang.typed.xtalk/get-entry :added "4.1"}
(fact "get-entry returns registry entries"
  (do
    (fixture-register!)
    (some? (get-entry 'std.lang.model.spec-xtalk-typed-fixture/find-user)))
  => true)

^{:refer std.lang.typed.xtalk/get-declaration :added "4.1"}
(fact "get-declaration returns specific declarations"
  (do
    (fixture-register!)
    (-> (get-declaration 'std.lang.model.spec-xtalk-typed-fixture/find-user :fn) :name))
  => "find-user")

^{:refer std.lang.typed.xtalk/get-spec :added "4.1"}
(fact "get-spec returns spec defs"
  (do
    (fixture-register!)
    (-> (get-spec 'std.lang.model.spec-xtalk-typed-fixture/UserMap) :name))
  => "UserMap")

^{:refer std.lang.typed.xtalk/get-function :added "4.1"}
(fact "get-function returns parsed fn defs"
  (do
    (fixture-register!)
    (-> (get-function 'std.lang.model.spec-xtalk-typed-fixture/find-user) :name))
  => "find-user")

^{:refer std.lang.typed.xtalk/get-macro :added "4.1"}
(fact "get-macro returns macro defs when present"
  (do
    (clear-registry!)
    (analyze-and-register! 'xt.lang.base-macro)
    (true? (get-in (get-macro 'xt.lang.base-macro/add) [:body-meta :macro])))
  => true)

^{:refer std.lang.typed.xtalk/get-value :added "4.1"}
(fact "get-value returns value defs when present"
  (do
    (clear-registry!)
    (analyze-and-register! 'xt.db.base-scope)
    (-> (get-value 'xt.db.base-scope/Scopes) :name))
  => "Scopes")

^{:refer std.lang.typed.xtalk/list-specs :added "4.1"}
(fact "lists specs through facade"
  (do (fixture-register!) (pos? (count (list-specs))))
  => true)

^{:refer std.lang.typed.xtalk/list-entries :added "4.1"}
(fact "lists entries through facade"
  (do (fixture-register!) (pos? (count (list-entries))))
  => true)

^{:refer std.lang.typed.xtalk/list-functions :added "4.1"}
(fact "lists functions through facade"
  (do (fixture-register!) (pos? (count (list-functions))))
  => true)

^{:refer std.lang.typed.xtalk/list-macros :added "4.1"}
(fact "lists macros through facade"
  (do
    (clear-registry!)
    (analyze-and-register! 'xt.lang.base-macro)
    (pos? (count (list-macros))))
  => true)

^{:refer std.lang.typed.xtalk/list-values :added "4.1"}
(fact "lists values through facade"
  (do
    (clear-registry!)
    (analyze-and-register! 'xt.db.base-scope)
    (pos? (count (list-values))))
  => true)

^{:refer std.lang.typed.xtalk/analyze-file :added "4.1"}
(fact "analyzes files through facade"
  (count (:specs (analyze-file "test/std/lang/model/spec_xtalk_typed_fixture.clj")))
  => 3)

^{:refer std.lang.typed.xtalk/analyze-namespace-raw :added "4.1"}
(fact "exposes raw namespace analysis through facade"
  (->> (analyze-namespace-raw 'std.lang.model.spec-xtalk-typed-fixture)
       :functions
       (some #(when (= "find-user" (:name %)) %))
       :output
       :name)
  => :xt/unknown)

^{:refer std.lang.typed.xtalk/analyze-namespace :added "4.1"}
(fact "analyzes namespaces through facade"
  (:ns (analyze-namespace 'std.lang.model.spec-xtalk-typed-fixture))
  => 'std.lang.model.spec-xtalk-typed-fixture)

^{:refer std.lang.typed.xtalk/analyze-and-register! :added "4.1"}
(fact "registers namespaces through facade"
  (do
    (clear-registry!)
    (some? (analyze-and-register! 'std.lang.model.spec-xtalk-typed-fixture)))
  => true)

^{:refer std.lang.typed.xtalk/check-function :added "4.1"}
(fact "checks functions through facade"
  (do
    (clear-registry!)
    (-> (check-function 'std.lang.model.spec-xtalk-typed-fixture/find-user) :errors))
  => [])

^{:refer std.lang.typed.xtalk/check-namespace :added "4.1"}
(fact "checks namespaces through facade"
  (do
    (clear-registry!)
    (:namespace (check-namespace 'xt.lang.event-route)))
  => 'xt.lang.event-route)


^{:refer std.lang.typed.xtalk/analyze-file-raw :added "4.1"}
(fact "delegates file-raw analysis to xtalk-parse"
  (let [result (analyze-file-raw "test/std/lang/model/spec_xtalk_typed_fixture.clj")]
    [(map? result)
     (contains? result :specs)
     (contains? result :functions)])
  => [true true true])