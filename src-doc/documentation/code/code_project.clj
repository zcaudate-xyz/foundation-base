(ns documentation.code-project
  (:require [code.project :as project])
  (:use code.test))

[[:hero {:title "code.project"
         :subtitle "Project metadata and file lookup."
         :lead "`code.project` normalizes project files, source paths, lookup tables, and dependency metadata so tools can work across source, test, and language directories."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Most higher-level tools need to know where namespaces live. `code.project` builds that lookup from project metadata and abstracts over Leiningen and Shadow project shapes."

[[:chapter {:title "Internal usage" :link "internal"}]]

"`code.framework`, `code.manage`, `code.doc`, and build tooling all use project lookups to resolve namespaces to files. This is also what lets documentation pages reference APIs from `src`, `src-lang`, and tests consistently."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Reading project metadata"}]]

"`project` loads the current project map, and `project-name` returns the project symbol. These are the foundation for every path lookup and namespace scan."

^{:refer code.project/project :added "3.0"}
(fact "load the current project"
  (project/project)
  => (contains {:name symbol?
                :dependencies vector?}))

^{:refer code.project/project-name :added "3.0"}
(fact "get the project name"
  (project/project-name)
  => symbol?)

[[:section {:title "Resolving namespaces to files"}]]

"`file-lookup` builds a map of namespace to file path, `file-type` distinguishes source from test files, and `source-ns`/`test-ns` convert between the two conventions."

^{:refer code.project/file-lookup :added "3.0"}
(fact "look up namespace files"
  (-> (project/file-lookup (project/project))
      (get 'code.project))
  => #(.endsWith ^String % "/src/code/project.clj"))

^{:refer code.project/file-type :added "3.0"}
(fact "distinguish source and test files"
  (project/file-type "project.clj")
  => :source

  (project/file-type "test/code/project_test.clj")
  => :test)

^{:refer code.project/source-ns :added "3.0"}
(fact "convert between source and test namespaces"
  (project/source-ns 'a-test)
  => 'a

  (project/test-ns 'a)
  => 'a-test)

[[:section {:title "End-to-end: scan all test files"}]]

"`all-files` walks configured project roots and returns every Clojure file it finds, keyed by namespace."

^{:refer code.project/all-files :added "3.0"}
(fact "collect test files in the project"
  (-> (project/all-files ["test"])
      (get 'code.project-test))
  => #(.endsWith ^String % "/test/code/project_test.clj"))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "code.project"}]]
[[:api {:namespace "code.project.common"}]]
[[:api {:namespace "code.project.lein"}]]
[[:api {:namespace "code.project.shadow"}]]
