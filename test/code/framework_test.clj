(ns code.framework-test
  (:use code.test)
  (:require [code.framework :refer :all]
            [code.edit :as nav]
            [code.framework.common :as common]
            [code.framework.docstring :as docstring]
            [code.project :as project]))

^{:refer code.framework/import-selector :added "3.0"}
(fact "creates an import selector"

  (import-selector '-hello-)
  ;;[(#{<options>} | -hello- string? map? & _)]
  => vector?)

^{:refer code.framework/toplevel-selector :added "3.0"}
(fact "creates a selector for querying toplevel forms"

  (toplevel-selector '-hello-)
  ;;[(#{<def forms>} | -hello- & _)]
  => vector?)

^{:refer code.framework/analyse-source-function :added "3.0"}
(fact "analyzes a single function definition within a source file, used as a helper for `analyse-source-code`"
  (binding [common/*path* "path"]
    (analyse-source-function 'foo (nav/parse-string "foo")))
  => (contains ['foo (contains {:var 'foo
                                :ns 'foo})]))

^{:refer code.framework/analyse-source-code :added "3.0"}
(fact "analyses a source file for namespace and function definitions"

  (-> (analyse-source-code (slurp "test-data/code.manage/src/example/core.clj"))
      (get-in '[example.core -foo-]))
  => (contains '{:ns example.core,
                 :var -foo-,
                 :source {:code "(defn -foo-\n  [x]\n  (println x \"Hello, World!\"))",
                          :line {:row 3, :col 1, :end-row 6, :end-col 31},
                          :path nil}}))

^{:refer code.framework/find-test-frameworks :added "3.0"}
(fact "find test frameworks given a namespace form"
  (find-test-frameworks '(ns ...
                           (:use code.test)))
  => #{:fact}

  (find-test-frameworks '(ns ...
                           (:use clojure.test)))
  => #{:clojure})

^{:refer code.framework/analyse-test-code :added "3.0"}
(fact "analyses a test file for docstring forms"

  (-> (analyse-test-code (slurp "test-data/code.manage/test/example/core_test.clj"))
      (get-in '[example.core -foo-]))
  => (contains {:ns 'example.core
                :var '-foo-
                :test map?
                :meta {:added "3.0"}
                :intro ""}))

^{:refer code.framework/analyse-file :added "3.0"}
(fact "analyzes a source or test file for namespace and function definitions, used as a helper for `analyse`"

  (analyse-file [:source "src/code/framework.clj"])
  => (contains {:source any}))

^{:refer code.framework/analyse :added "3.0"}
(fact "seed analyse function for the `code.manage/analyse` task"

  (->> (project/in-context (analyse 'code.framework-test))
       (common/display-entry))
  => (contains-in {:test {'code.framework
                          (contains '[analyse
                                      analyse-file
                                      analyse-source-code])}})

  (->> (project/in-context (analyse 'code.framework))
       (common/display-entry))
  => (contains-in {:source {'code.framework
                            (contains '[analyse
                                        analyse-file
                                        analyse-source-code])}}))

^{:refer code.framework/var-function :added "3.0"}
(fact "constructs a var, with or without namespace"

  ((var-function true) {:ns 'hello :var 'world})
  => 'hello/world

  ((var-function false) {:ns 'hello :var 'world})
  => 'world)

^{:refer code.framework/vars :added "3.0"}
(fact "returns all vars in a given namespace"

  (project/in-context (vars {:sorted true}))
  => (contains '[analyse
                 analyse-file
                 analyse-source-code
                 analyse-source-function]))

^{:refer code.framework/read-ns-form :added "4.0"}
(fact "memoised version of fs/read-ns"
  (read-ns-form "src/code/framework.clj")
  => (contains '(ns code.framework)))

^{:refer code.framework/no-test :added "4.0"}
(fact "checks that a namespace does not require test"
  (project/in-context (no-test 'code.framework)) => nil)

^{:refer code.framework/docstrings :added "3.0"}
(fact "returns all docstrings in a given namespace with given keys"

  (->> (project/in-context (docstrings))
       (map first)
       sort)
  => (project/in-context (vars {:sorted true})))

^{:refer code.framework/transform-code :added "3.0"}
(fact "transforms the code and performs a diff to see what has changed"

  ;; options include :skip, :full and :write
  (project/in-context (transform-code 'code.framework {:transform identity}))
  => (contains {:changed []
                :updated false
                :path any}))

^{:refer code.framework/locate-code :added "3.0"}
(fact "finds code base upon a query"
  
  (project/in-context (locate-code {:query '[docstrings]
                                    :print {:function true}}))
  ;;[{:row 93, :col 28, :end-row 93, :end-col 40}]
  => vector?)

^{:refer code.framework/compile-regex :added "3.0"}
(fact "compiles a regex string from an input string"

  (compile-regex "(:require")
  => "\\\\(:require")

^{:refer code.framework/search-regex :added "3.0"}
(fact "constructs a search regex (for line numbers)"

  (search-regex "hello")
  => #"^(.*?)(hello)")

^{:refer code.framework/grep-search :added "3.0"}
(fact "finds code based on string query"

  (project/in-context (grep-search {:query '[docstrings]
                                    :print {:function true}}))
  => seq?)

^{:refer code.framework/grep-replace :added "3.0"}
(fact "replaces code based on string query"

  (project/in-context (grep-replace {:query "docstrings"
                                     :replace "[DOCSTRINGS]"
                                     :print {:function true}})))

^{:refer code.framework/refactor-code :added "3.0"}
(fact "takes in a series of edits and performs them on the code"

  (project/in-context (refactor-code {:edits []}))
  => {:changed [], :updated false, :verified true, :path "test/code/framework_test.clj"})

^{:refer code.framework/extract :added "4.0"}
(fact "returns all vars in a given namespace"
  (extract 'code.framework {:process identity} (project/file-lookup (project/project)) (project/project))
  => string?)
