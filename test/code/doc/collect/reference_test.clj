(ns code.doc.collect.reference-test
  (:require [code.doc.collect.reference :refer :all]
            [code.doc.parse :as parse]
            [code.project :as project])
  (:use code.test))

^{:refer code.doc.collect.reference/find-import-namespaces :added "3.0"}
(fact "finds forms with `(module/include ...)`"

  ;; THIS NEEDS FIXING to use h/intern-all as well as module/include
  (find-import-namespaces (project/file-lookup (project/project))
                          'code.test)
  => () #_'(code.test.checker.common
            code.test.checker.collection
            code.test.checker.logic
            code.test.compile))

^{:refer code.doc.collect.reference/reference-namespaces :added "3.0"}
(fact "finds the referenced vars in the namespace"

  (-> (reference-namespaces {}
                            (project/file-lookup (project/project))
                            '[jvm.artifact.common])
      (get 'jvm.artifact.common)
      keys
      sort)
  => '(resource-entry resource-entry-symbol))

^{:refer code.doc.collect.reference/collect-references :added "3.0"}
(fact "collects all `:reference` tags of within an article"

  (let [project (project/project)
        project (assoc project :lookup (project/file-lookup project))
        elems   (parse/parse-file "src-doc/documentation/code_doc.clj" project)
        bundle  {:articles {"code.doc" {:elements elems}}
                 :references {}
                 :project project}]
    (-> (collect-references bundle "code.doc")
        :references
        keys))
  => '(code.doc code.doc.manage))
