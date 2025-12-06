(ns code.manage.unit-test
  (:use code.test)
  (:require [code.manage.unit :refer :all]
            [code.project :as project]))

^{:refer code.manage.unit/import :added "3.0"}
(fact "imports unit tests as docstrings"

  (project/in-context (import {:print {:function true}}))
  => map?)

^{:refer code.manage.unit/purge :added "3.0"}
(fact "purge docstrings and meta from file"

  (project/in-context (purge {:print {:function true}}))
  => map?)

^{:refer code.manage.unit/missing :added "3.0"}
(fact "returns all functions missing unit tests"

  (project/in-context (missing))
  => (any vector? nil?))

^{:refer code.manage.unit/todos :added "3.0"}
(fact "returns all unit tests with TODOs"

  (project/in-context (todos))
  => (any vector? nil?))

^{:refer code.manage.unit/incomplete :added "3.0"}
(fact "returns functions with todos all missing tests"

  (project/in-context (incomplete))
  => (any vector? nil?))

^{:refer code.manage.unit/orphaned-meta :added "3.0"}
(fact "returns true if meta satisfies the orphaned criteria"

  (orphaned-meta {} {}) => false

  (orphaned-meta {:strict true}
                 {:meta {:adopt true} :ns 'clojure.core :var 'slurp})
  => true

  (orphaned-meta {:strict true}
                 {:meta {:adopt true} :ns 'clojure.core :var 'NONE})
  => false)

^{:refer code.manage.unit/orphaned :added "3.0"}
(fact "returns unit tests that do not have an associated function"

  (project/in-context (orphaned))
  => (any vector? nil?))

^{:refer code.manage.unit/mark-vars :added "3.0"}
(fact "captures changed vars in a set"

  (mark-vars '[a1 a2 a3 a4 a5]
             '[a1 a4 a3 a2 a5])
  => '[2 [a1 #{a2} #{a3} a4 a5]])

^{:refer code.manage.unit/in-order? :added "3.0"}
(fact "determines if the test code is in the same order as the source code"

  (project/in-context (in-order?))
  => (any vector? nil?))

^{:refer code.manage.unit/scaffold :added "3.0"}
(fact "creates a set of tests for a given source"

  (project/in-context (scaffold))
  => map?)

^{:refer code.manage.unit/arrange :added "3.0"}
(fact "arranges the test code to be in the same order as the source code"

  (project/in-context (arrange))
  => map?)

^{:refer code.manage.unit/create-tests :added "3.0"}
(fact "scaffolds and arranges the test file"

  (project/in-context (create-tests))
  => map?)

^{:refer code.manage.unit/unchecked :added "3.0"}
(fact "returns tests that does not contain a `=>`"

  (project/in-context (unchecked))
  => (any vector? nil?))

^{:refer code.manage.unit/commented :added "3.0"}
(fact "returns tests that are in a comment block"

  (project/in-context (commented))
  => (any vector? nil?))

^{:refer code.manage.unit/pedantic :added "3.0"}
(fact "returns all probable improvements on tests"

  (project/in-context (pedantic))
  => (any seq? nil?))

(comment
  (code.manage/import {:write true}))

(comment
  (project/in-context (import))
  (require '[std.block.navigate :as nav])
  (def nav (-> (nav/parse-root (slurp "src/hara/code/unit.clj"))
               (nav/down)))

  (def import-fn (fn [nsp refers]
                   (fn [zloc]
                     (docstring/insert-docstring zloc nsp refers))))

  (def refers (base/analyse-test-code (slurp "test/hara/code/unit_test.clj")))

  (get-in refers ['code.manage.unit 'missing :test :code])
  (code.query/modify nav
                     (code.framework/import-selector)
                     (fn [nav]
                       ((import-fn 'code.manage.unit
                                   refers)
                        nav))
                     {:walk :top})

  (./reset '[std.lib.zip])
  (./reset '[std.block])
  (./reset '[code.manage])
  (./run '[code.manage]))
