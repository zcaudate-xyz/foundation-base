(ns code.doc.link.namespace-test
  (:use code.test)
  (:require [code.doc.link.namespace :refer :all]))

^{:refer code.doc.link.namespace/link-namespaces :added "3.0"}
(fact "links the current namespace to the elements"

  (-> (link-namespaces {:namespaces {'code.core {:code "(ns code.core)"}}
                        :articles {"doc" {:elements [{:type :ns :ns 'code.core}]}}}
                       "doc")
      (get-in [:articles "doc" :elements]))
  => [{:type :ns, :ns 'code.core, :indentation 0, :code "(ns code.core)"}])
