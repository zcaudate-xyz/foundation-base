(ns code.doc.prepare-test
  (:use code.test)
  (:require [code.doc.prepare :refer :all]
            [code.doc.executive :as executive]
            [code.doc :as publish]))

^{:refer code.doc.prepare/prepare :added "3.0"}
(fact "processes a single meta to generate an interim structure"
  (let [project (publish/make-project)
        lookup  (executive/all-pages project)]
    (sort (keys (prepare 'hara/index {} lookup project))))
  => '(:anchors :anchors-lu :articles :global :namespaces :project :references))

(comment

  (:anchors (executive/in-context (prepare 'hara/index))))
