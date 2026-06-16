(ns hara.typed-test
  (:use code.test)
  (:require [hara.typed :refer :all]
            [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-parse :as parse]))

(defn fixture-register! []
  (clear-registry!)
  (parse/register-types! (parse/analyze-namespace 'hara.model.spec-xtalk-typed-fixture)))

^{:refer hara.typed/namespace-aliases :added "4.1"}
(fact "returns alias to namespace name map"
  (namespace-aliases (find-ns 'hara.typed-test))
  => '{types hara.typed.xtalk-common
       parse hara.typed.xtalk-parse})

^{:refer hara.typed/register-spec-form! :added "4.1"}
(fact "registers a spec declaration from a form"
  (do
    (clear-registry!)
    (register-spec-form! 'sample.route/Role :xt/str {} {})
    (:name (get-spec 'sample.route/Role)))
  => "Role")

^{:refer hara.typed/defspec.xt :added "4.1"}
(fact "defines a spec via macro"
  (do
    (clear-registry!)
    (defspec.xt Item :xt/int)
    (types/type->data (:type (get-spec 'hara.typed-test/Item))))
  => '{:kind :primitive :name :xt/int})

^{:refer hara.typed/clear-registry! :added "4.1"}
(fact "clears the type registry"
  (do
    (register-spec-form! 'sample.route/Role :xt/str {} {})
    (clear-registry!)
    (get-entry 'sample.route/Role))
  => nil)

^{:refer hara.typed/register-type! :added "4.1"}
(fact "registers a type definition directly"
  (do
    (clear-registry!)
    (register-type! 'sample.route/Direct {:kind :primitive :name :xt/str})
    (types/type->data (get-type 'sample.route/Direct)))
  => '{:kind :primitive :name :xt/str})

^{:refer hara.typed/get-type :added "4.1"}
(fact "retrieves the primary type declaration"
  (do
    (clear-registry!)
    (register-type! 'sample.route/Direct {:kind :primitive :name :xt/str}))
  => '{:kind :primitive :name :xt/str})

^{:refer hara.typed/get-entry :added "4.1"}
(fact "retrieves the registry entry for a symbol"
  (do
    (clear-registry!)
    (register-type! 'sample.route/Direct {:kind :primitive :name :xt/str})
    (:symbol (get-entry 'sample.route/Direct)))
  => 'sample.route/Direct)

^{:refer hara.typed/get-declaration :added "4.1"}
(fact "retrieves a declaration by kind"
  (do
    (clear-registry!)
    (register-type! 'sample.route/Direct {:kind :primitive :name :xt/str})
    (types/type->data (get-declaration 'sample.route/Direct :spec)))
  => '{:kind :primitive :name :xt/str})

^{:refer hara.typed/get-spec :added "4.1"}
(fact "retrieves the spec declaration"
  (do
    (fixture-register!)
    (:name (get-spec 'hara.model.spec-xtalk-typed-fixture/User)))
  => "User")

^{:refer hara.typed/get-function :added "4.1"}
(fact "retrieves the function declaration"
  (do
    (fixture-register!)
    (:name (get-function 'hara.model.spec-xtalk-typed-fixture/find-user)))
  => "find-user")

^{:refer hara.typed/get-macro :added "4.1"}
(fact "returns nil when no macro is registered"
  (do
    (fixture-register!)
    (get-macro 'hara.model.spec-xtalk-typed-fixture/find-user))
  => nil)

^{:refer hara.typed/get-value :added "4.1"}
(fact "returns nil when no value is registered"
  (do
    (fixture-register!)
    (get-value 'hara.model.spec-xtalk-typed-fixture/find-user))
  => nil)

^{:refer hara.typed/list-specs :added "4.1"}
(fact "lists all registered specs"
  (do
    (fixture-register!)
    (sort (map :name (filter #(= "hara.model.spec-xtalk-typed-fixture" (:ns %)) (list-specs)))))
  => ["User" "UserMap" "find-user"])

^{:refer hara.typed/list-entries :added "4.1"}
(fact "lists all registry entries"
  (do
    (fixture-register!)
    (count (filter #(= "hara.model.spec-xtalk-typed-fixture" (namespace (:symbol %))) (list-entries))))
  => 5)

^{:refer hara.typed/list-functions :added "4.1"}
(fact "lists all registered functions"
  (do
    (fixture-register!)
    (count (filter #(= "hara.model.spec-xtalk-typed-fixture" (:ns %)) (list-functions))))
  => 3)

^{:refer hara.typed/list-macros :added "4.1"}
(fact "lists all registered macros"
  (do
    (fixture-register!)
    (count (list-macros)))
  => 0)

^{:refer hara.typed/list-values :added "4.1"}
(fact "lists all registered values"
  (do
    (fixture-register!)
    (count (list-values)))
  => 0)

^{:refer hara.typed/analyze-file :added "4.1"}
(fact "analyzes a source file"
  (:ns (analyze-file "test/hara/model/spec_xtalk_typed_fixture.clj"))
  => 'hara.model.spec-xtalk-typed-fixture)

^{:refer hara.typed/analyze-file-raw :added "4.1"}
(fact "returns raw analysis for a source file"
  (let [result (analyze-file-raw "test/hara/model/spec_xtalk_typed_fixture.clj")]
    [(map? result)
     (:ns result)
     (contains? result :functions)])
  => [true 'hara.model.spec-xtalk-typed-fixture true])

^{:refer hara.typed/analyze-namespace :added "4.1"}
(fact "analyzes a namespace"
  (:ns (analyze-namespace 'hara.model.spec-xtalk-typed-fixture))
  => 'hara.model.spec-xtalk-typed-fixture)

^{:refer hara.typed/analyze-namespace-raw :added "4.1"}
(fact "returns raw analysis for a namespace"
  (let [result (analyze-namespace-raw 'hara.model.spec-xtalk-typed-fixture)]
    [(map? result)
     (:ns result)])
  => [true 'hara.model.spec-xtalk-typed-fixture])

^{:refer hara.typed/analyze-and-register! :added "4.1"}
(fact "analyzes a namespace and registers its declarations"
  (do
    (clear-registry!)
    (analyze-and-register! 'hara.model.spec-xtalk-typed-fixture)
    (some? (get-function 'hara.model.spec-xtalk-typed-fixture/find-user)))
  => true)

^{:refer hara.typed/check-function :added "4.1"}
(fact "checks a function for type errors"
  (do
    (clear-registry!)
    (analyze-and-register! 'hara.model.spec-xtalk-typed-fixture)
    (:function (check-function 'hara.model.spec-xtalk-typed-fixture/find-user)))
  => 'hara.model.spec-xtalk-typed-fixture/find-user)

^{:refer hara.typed/check-namespace :added "4.1"}
(fact "checks all functions in a namespace"
  (do
    (clear-registry!)
    (analyze-and-register! 'hara.model.spec-xtalk-typed-fixture)
    (:namespace (check-namespace 'hara.model.spec-xtalk-typed-fixture)))
  => 'hara.model.spec-xtalk-typed-fixture)
