(ns jvm.namespace.context-test
  (:require [jvm.namespace.context :refer :all])
  (:use code.test))

^{:refer jvm.namespace.context/resolve-ns :added "3.0"}
(fact "resolves the namespace or else returns nil if it does not exist"

  (resolve-ns 'clojure.core) => 'clojure.core

  (resolve-ns 'clojure.core/some) => 'clojure.core

  (resolve-ns 'clojure.hello) => nil)

^{:refer jvm.namespace.context/ns-vars :added "3.0"}
(fact "lists the vars in a particular namespace"

  (ns-vars 'jvm.namespace.context)
  => '[*ns-context* ->NamespaceContext map->NamespaceContext
       ns-context ns-vars reeval resolve-ns])

^{:refer jvm.namespace.context/ns-context :added "3.0"}
(fact "gets the namespace context")

^{:refer jvm.namespace.context/reeval :added "3.0"}
(fact "reevals all dependents of a namespace")
