(ns code.doc.manage-test
  (:require [code.doc.manage :refer :all]
            [code.doc.parse :as parse]
            [code.project :as project])
  (:use code.test))

^{:refer code.doc.manage/source-namespaces :added "4.1"}
(fact "returns all source namespaces for code.doc coverage"
  (source-namespaces (project/project))
  => (contains ['code.doc 'code.doc.executive] :gaps-ok))

^{:refer code.doc.manage/element-namespaces :added "4.1"}
(fact "extracts documented namespaces from a parsed element"
  (element-namespaces {:type :api :namespace "code.doc"})
  => #{'code.doc}

  (element-namespaces {:type :reference :refer "code.doc.executive/render"})
  => #{'code.doc.executive})

^{:refer code.doc.manage/documented-coverage :added "4.1"}
(fact "returns a namespace to pages coverage map"
  (with-redefs [parse/parse-file (fn [_ _]
                                   [{:type :api :namespace "code.doc"}
                                    {:type :reference :refer "code.doc.executive/render"}])]
    (documented-coverage {:root "."
                          :publish {:sites {:foundation.code {:pages {'index {:input "docs/index.clj"}}}}}}))
  => {'code.doc ['foundation.code/index]
      'code.doc.executive ['foundation.code/index]})

^{:refer code.doc.manage/missing-namespaces :added "4.1"}
(fact "returns a marker for namespaces that are not referenced by code.doc pages"
  (missing-namespaces 'code.doc {} nil {:code.doc/coverage {}})
  => [:missing-code-doc]

  (missing-namespaces 'code.doc {} nil {:code.doc/coverage {'code.doc ['foundation.code/index]}})
  => nil)
