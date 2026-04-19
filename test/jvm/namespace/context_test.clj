(ns jvm.namespace.context-test
  (:require [jvm.namespace.context :refer :all]
            [std.lib.deps :as deps])
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
(fact "gets the namespace context"
  (ns-context)
  => jvm.namespace.context.NamespaceContext)

^{:refer jvm.namespace.context/reeval :added "3.0"}
(fact "reevals all dependents of a namespace"
  (with-redefs [deps/dependents-refresh (fn [_ ns] [:refreshed ns])]
    (reeval 'jvm.namespace.context))
  => [:refreshed 'jvm.namespace.context])
