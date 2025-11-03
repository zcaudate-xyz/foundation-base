(ns code.project-test
  (:use code.test)
  (:require [code.project :refer :all]
            [std.fs :as fs]
            [std.lib :as h]))

^{:refer code.project/project-file :added "3.0"}
(fact "returns the current project file"

  (project-file)
  => "project.clj")

^{:refer code.project/project-map :added "3.0"}
(fact "returns the project map"

  (project-map (fs/path "project.clj")))

^{:refer code.project/project :added "3.0"}
(fact "returns project options as a map"

  (project)
  => (contains {:name symbol?
                :dependencies vector?}))

^{:refer code.project/project-name :added "3.0"}
(fact "returns the name, read from the project map"

  (project-name)
  => symbol?)

^{:refer code.project/exclude :added "3.0"}
(fact "helper function for excluding certain namespaces"

  (exclude '{lucid.legacy.analyzer :a
             lucid.legacy :a
             lib.aether :b}
           ["lucid.legacy"])
  => '{lib.aether :b})

^{:refer code.project/lookup-ns :added "3.0"}
(fact "fast lookup for all-files function"

  (first (lookup-ns (get-path (h/ns-sym))))
  => 'code.project-test)

^{:refer code.project/lookup-path :added "3.0"}
(fact "looks up the path given the `ns`"

  (lookup-path (h/ns-sym)))

^{:refer code.project/get-path :added "4.0"
  :setup [(reset! code.project.common/*lookup* {})]}
(fact "gets the path given the `ns`"
  ^:hidden
  
  (get-path (h/ns-sym))
  => "test/code/project_test.clj")

^{:refer code.project/all-files :added "3.0"}
(fact "returns all the clojure files in a directory"

  (count (all-files ["test"]))
  => number?

  (-> (all-files ["test"])
      (get 'code.project-test))
  => #(.endsWith ^String % "/test/code/project_test.clj"))

^{:refer code.project/file-lookup :added "3.0"}
(fact "creates a lookup of namespaces and files in the project"

  (-> (file-lookup (project))
      (get 'code.project))
  => #(.endsWith ^String % "/src/code/project.clj"))

^{:refer code.project/file-suffix :added "3.0"}
(fact "returns the file suffix for a given type"

  (file-suffix) => ".clj"

  (file-suffix :cljs) => ".cljs")

^{:refer code.project/test-suffix :added "3.0"}
(fact "returns the test suffix"

  (test-suffix) => "-test")

^{:refer code.project/file-type :added "3.0"}
(fact "returns the type of file according to the suffix"

  (file-type "project.clj")
  => :source

  (file-type "test/code/project_test.clj")
  => :test)

^{:refer code.project/sym-name :added "3.0"}
(fact "returns the symbol of the namespace"

  (sym-name *ns*)
  => 'code.project-test

  (sym-name 'a)
  => 'a)

^{:refer code.project/source-ns :added "3.0"}
(fact "returns the source namespace"

  (source-ns 'a) => 'a
  (source-ns 'a-test) => 'a)

^{:refer code.project/test-ns :added "3.0"}
(fact "returns the test namespace"

  (test-ns 'a) => 'a-test
  (test-ns 'a-test) => 'a-test)

^{:refer code.project/in-context :added "3.0"}
(fact "creates a local context for executing code functions"

  (in-context ((fn [current params _ project]
                 current)))
  => 'code.project-test)

^{:refer code.project/code-files :added "3.0"}
(fact "returns only the code files for the current project"

  (code-files))

^{:refer code.project/code-path :added "3.0"}
(fact "returns the path of the code"

  (str (code-path (h/ns-sym) true))
  => "test/code/project_test.clj")

(comment
  (./import))
