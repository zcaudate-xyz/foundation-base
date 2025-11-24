(ns code.doc.link.reference-test
  (:use code.test)
  (:require [code.doc.link.reference :refer :all]))

^{:refer code.doc.link.reference/link-references :added "3.0"}
(fact "links references when working with specific source and test code"

  (-> (link-references {:references {'code.core {'foo {:source {:code "(defn foo [])"}}}}
                        :articles {"doc" {:elements [{:type :reference :refer "code.core/foo"}]}}}
                       "doc")
      (get-in [:articles "doc" :elements]))
  => [{:type :reference, :refer "code.core/foo", :indentation 0, :code "(defn foo [])", :mode :source, :title "source of <i>code.core/foo</i>"}])
