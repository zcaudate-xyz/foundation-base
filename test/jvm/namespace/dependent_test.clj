(ns jvm.namespace.dependent-test
  (:use code.test)
  (:require [jvm.namespace.dependent :refer :all]))

^{:refer jvm.namespace.dependent/ns-select :added "3.0"}
(fact "selects a bunch of namespaces"

  (ns-select [(.getName *ns*)])
  => '[jvm.namespace.dependent-test])

^{:refer jvm.namespace.dependent/ns-has-deps :added "3.0"}
(fact "checks if current namespace depends on `test`"

  (ns-has-deps 'jvm.namespace.dependent
               'std.lib)
  => true)

^{:refer jvm.namespace.dependent/ns-dependents :added "3.0"}
(fact "finds dependent namespaces"
  (ns-dependents 'std.lib
                 '#{jvm.namespace.dependent})
  => '#{jvm.namespace.dependent})

^{:refer jvm.namespace.dependent/ns-level-dependents :added "3.0"}
(fact "arranges dependent namespaces in map"

  (ns-level-dependents '#{std.lib}
                       '#{jvm.namespace.dependent})
  => '{std.lib #{jvm.namespace.dependent}})

^{:refer jvm.namespace.dependent/ns-all-dependents :added "3.0"}
(fact "gets all dependencies"

  (ns-all-dependents 'std.lib.sort '[hara])
  => map?)

^{:refer jvm.namespace.dependent/reeval :added "3.0"}
(fact "reevaluates all files dependent on current"

  (->> (reeval '#{jvm.namespace.dependent})
       (map str))
  => ["jvm.namespace.dependent-test"])
