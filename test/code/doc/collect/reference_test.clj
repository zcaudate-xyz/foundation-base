(ns code.doc.collect.reference-test
  (:require [code.doc.collect.reference :refer :all]
            [code.doc.parse :as parse]
            [code.project :as project])
  (:use code.test))

^{:refer code.doc.collect.reference/ns-form-aliases :added "4.1"}
(fact "returns the alias -> namespace map for a namespace form"

  (ns-form-aliases '(ns example (:require [std.block.base :as base]
                                          [std.string [case :as c]]
                                          [std.fs])))
  => '{base std.block.base, c std.string.case})

^{:refer code.doc.collect.reference/aggregate-imports :added "4.1"}
(fact "finds vars imported into an aggregate namespace"

  (-> (aggregate-imports (project/file-lookup (project/project)) 'std.block)
      :namespaces
      (contains? 'std.block.base))
  => true

  (-> (aggregate-imports (project/file-lookup (project/project)) 'std.block)
      :vars
      (get 'std.block.base)
      (set)
      (contains? 'block?))
  => true

  (-> (aggregate-imports (project/file-lookup (project/project)) 'std.block.heal)
      :vars
      (get 'std.block.heal.core))
  => '[[heal heal-content]]

  (-> (aggregate-imports (project/file-lookup (project/project)) 'std.lib)
      :vars
      (get 'std.lib.collection))
  => :all)

^{:refer code.doc.collect.reference/find-import-namespaces :added "3.0"}
(fact "finds namespaces imported via `module/include`, `intern-in` and `intern-all`"

  (find-import-namespaces (project/file-lookup (project/project))
                          'code.test)
  => '(code.test.base.context
       code.test.checker.collection
       code.test.checker.common
       code.test.checker.logic
       code.test.compile
       code.test.manage
       code.test.task))

^{:refer code.doc.collect.reference/reference-namespaces :added "3.0"}
(fact "finds the referenced vars in the namespace"

  (-> (reference-namespaces {}
                            (project/file-lookup (project/project))
                            '[jvm.artifact.common])
      (get 'jvm.artifact.common)
      keys
      sort)
  => '(*java-class-path* *java-home* *java-runtime-jar* *local-repo* *sep*
       resource-entry resource-entry-symbol))

^{:refer code.doc.collect.reference/reference-namespaces :id reference-def-forms :added "4.1"}
(fact "indexes `def` and `defimpl` forms for documentation"

  (-> (reference-namespaces {}
                            (project/file-lookup (project/project))
                            '[lib.postgres])
      (get 'lib.postgres)
      (contains? 'start-pg))
  => true

  (-> (reference-namespaces {}
                            (project/file-lookup (project/project))
                            '[std.task])
      (get 'std.task)
      (contains? 'map->Task))
  => true)

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
