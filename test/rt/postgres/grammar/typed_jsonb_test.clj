(ns rt.postgres.grammar.typed-jsonb-test
  (:require [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-jsonb :as jsonb])
  (:use code.test))

^{:refer rt.postgres.grammar.typed-jsonb/infer-jsonb-arg-access-shape :added "4.1"}
(fact "infer-jsonb-arg-access-shape infers keys from js-select"
  (let [fn-def {:body-meta
                {:raw-body '((fu/js-select m (js ["a" "b" "c"])))}} ; close maps
        shape (jsonb/infer-jsonb-arg-access-shape 'm fn-def)]
    (types/jsonb-shape? shape) => true
    (contains? (:fields shape) :a) => true
    (contains? (:fields shape) :b) => true
    (contains? (:fields shape) :c) => true))


^{:refer rt.postgres.grammar.typed-jsonb/symbol->field-key :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/field-info :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/typed-binding-form? :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/accessor-expr? :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/append-path :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/access-descriptors :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/access-descriptor :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/expr-jsonb-path :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/set-binding-descriptors :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/binding-descriptors :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/descriptor-shape :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/update-root-shape :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/apply-descriptor :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/apply-descriptors :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/js-keys-form->keywords :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/js-select-descriptors :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/analyze-binding :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-jsonb/scan-form :added "4.1"}
(fact "TODO")