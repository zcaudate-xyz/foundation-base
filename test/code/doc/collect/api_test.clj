(ns code.doc.collect.api-test
  (:require [code.doc.collect.api :refer :all]
            [code.doc.collect.reference :as reference]
            [code.doc.parse :as parse]
            [code.project :as project])
  (:use code.test))

^{:refer code.doc.collect.api/collect-apis :added "3.0"}
(fact "gets all the `api` tags in the document"

  (let [project (project/project)
        project (assoc project :lookup (project/file-lookup project))
        elems   (parse/parse-file "src-doc/documentation/code_doc.clj" project)
        bundle  {:articles {"code-doc" {:elements elems}}
                 :references {}
                 :project project}]
    (with-redefs [reference/reference-namespaces (fn [_ _ namespaces] namespaces)]
      (-> (collect-apis bundle "code-doc")
          :references)))
  => '[code.doc code.doc.manage])

(comment
  (code.manage/import))
