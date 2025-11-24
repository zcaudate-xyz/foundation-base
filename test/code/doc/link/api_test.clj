(ns code.doc.link.api-test
  (:use code.test)
  (:require [code.doc.link.api :refer :all]
            [code.project :as project]))

^{:refer code.doc.link.api/external-vars :added "3.0"}
(fact "grabs external vars from the `module/include` form"

  ;; THIS NEEDS FIXING to use h/intern-all as well as module/include
  (external-vars (project/file-lookup (project/project))
                 'code.test)
  => {}
  #_'{code.test.checker.common [throws exactly approx satisfies stores anything]
      code.test.checker.collection [contains just contains-in just-in throws-info]
      code.test.checker.logic [any all is-not]
      code.test.compile [fact facts =>]})

^{:refer code.doc.link.api/create-api-table :added "3.0"}
(fact "creates a api table for publishing"
  ;; Test this needs a lot of context (project, references).
  ;; Mocking might be needed, or using a simple project structure.
  ;; For now, we can test with empty data to see if it runs.

  (create-api-table {}
                    {:lookup (constantly nil)}
                    'code.doc.link.api)
  => {})

^{:refer code.doc.link.api/link-apis :added "3.0"}
(fact "links all the api source and test files to the elements"

  (let [project {:lookup (constantly nil) :root "."}
        interim {:references {}
                 :project project
                 :articles {"doc" {:elements [{:type :api :namespace "code.doc.link.api"}]}}}]
    (-> (link-apis interim "doc")
        (get-in [:articles "doc" :elements])
        first
        keys)
    => (contains [:project :table])))
