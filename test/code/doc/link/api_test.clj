(ns code.doc.link.api-test
  (:require [code.doc.link.api :refer :all]
            [code.project :as project])
  (:use code.test))

^{:refer code.doc.link.api/external-vars :added "3.0"}
(fact "grabs external vars from aggregate forms (`module/include`, `intern-in`, `intern-all`)"

  (-> (external-vars (project/file-lookup (project/project))
                     'code.test)
      (get 'code.test.checker.common))
  => '[throws exactly approx satisfies stores anything capture]

  (-> (external-vars (project/file-lookup (project/project))
                     'code.test)
      (get 'code.test.checker.collection))
  => '[contains just contains-in just-in throws-info]

  (-> (external-vars (project/file-lookup (project/project))
                     'std.lib)
      (get 'std.lib.collection))
  => :all)

^{:refer code.doc.link.api/external-vars :id external-vars-aliased :added "4.1"}
(fact "resolves `[dst src]` alias entries in `intern-in` forms"

  (-> (external-vars (project/file-lookup (project/project))
                     'std.block)
      (get 'std.block.base)
      (->> (filter vector?)))
  => '[[type block-type]
       [tag block-tag]
       [string block-string]
       [length block-length]
       [width block-width]
       [height block-height]
       [prefixed block-prefixed]
       [suffixed block-suffixed]
       [verify block-verify]
       [value block-value]
       [value-string block-value-string]
       [children block-children]
       [info block-info]]

  (-> (external-vars (project/file-lookup (project/project))
                     'std.block.heal)
      (get 'std.block.heal.core))
  => '[[heal heal-content]])

^{:refer code.doc.link.api/create-api-table :added "3.0"}
(fact "creates a api table for publishing"
  ;; Test this needs a lot of context (project, references).
  ;; Mocking might be needed, or using a simple project structure.
  ;; For now, we can test with empty data to see if it runs.

  (create-api-table {}
                    {:lookup (constantly nil)}
                    'code.doc.link.api)
  => {})

^{:refer code.doc.link.api/create-api-table :id create-api-table-generated :added "4.1"}
(fact "marks runtime-generated vars with live metadata"

  (let [table (create-api-table {}
                                {:lookup (project/file-lookup (project/project))
                                 :root "."}
                                'std.lib.bin)
        entry (get table 'double-buffer)]
    [(-> entry :source :generated)
     (boolean (seq (:arglists entry)))])
  => [true true])

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
