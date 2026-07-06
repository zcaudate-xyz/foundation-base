(ns documentation.code-framework
  (:require [code.framework :as fw]
            [code.project :as project])
  (:use code.test))

[[:hero {:title "code.framework"
         :subtitle "Source and test analysis foundation."
         :lead "`code.framework` extracts namespace, function, docstring, line, and test metadata from source files. It is the analysis substrate below code management and documentation generation."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Documentation and maintenance both need an index of what exists. `code.framework` creates that index by analysing source and test files and by connecting implementation forms with facts and docstrings."

[[:chapter {:title "How it is used" :link "usage"}]]

"Use `analyse` for namespace-level source/test metadata, `extract` for function-level entries, and the link/cache helpers when repeated project-wide scans would be too expensive."

[[:chapter {:title "Internal usage" :link "internal"}]]

"`code.doc` relies on framework-style analysis to build API tables. `code.manage` uses the same metadata to find missing tests, incomplete examples, orphaned references, and importable fact bodies."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Parsing source and test files"}]]

"`code.framework` turns raw source text into structured entries. `analyse-source-code` extracts namespace and function metadata from a source string, while `analyse-test-code` extracts fact metadata from a test string."

^{:refer code.framework/analyse-source-code :added "3.0"}
(fact "analyse a source snippet"
  (-> (fw/analyse-source-code "(ns example.core)\n\n(defn hello []\n  1)")
      (get-in '[example.core hello]))
  => (contains {:ns 'example.core
                :var 'hello
                :source (contains {:code string?})}))

^{:refer code.framework/toplevel-selector :added "3.0"}
(fact "create a selector for top-level forms"
  (fw/toplevel-selector 'hello)
  => vector?)

^{:refer code.framework/find-test-frameworks :added "3.0"}
(fact "detect the test framework in a namespace form"
  (fw/find-test-frameworks '(ns example
                              (:use code.test)))
  => #{:fact})

[[:section {:title "Analysing the current project"}]]

"`analyse` and `vars` operate within the project context. They use the file lookup built by `code.project` to find source and test files."

^{:refer code.framework/analyse :added "3.0"}
(fact "analyse a namespace in the current project"
  (->> (project/in-context (fw/analyse 'code.framework))
       (get-in [:source 'code.framework]))
  => map?)

^{:refer code.framework/vars :added "3.0"}
(fact "list vars in a project namespace"
  (project/in-context (fw/vars {:sorted true}))
  => (contains '[analyse
                 analyse-file
                 analyse-source-code]))

[[:section {:title "End-to-end: locate code by query"}]]

"`locate-code` parses a file and returns line information for every match of a structural query."

^{:refer code.framework/locate-code :added "3.0"}
(fact "locate a structural pattern in a project file"
  (project/in-context (fw/locate-code 'code.framework
                                       {:query '[defn]
                                        :print {:result false :summary false}}))
  => vector?)

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "code.framework"}]]
[[:api {:namespace "code.framework.common"}]]
[[:api {:namespace "code.framework.link"}]]
[[:api {:namespace "code.framework.cache"}]]
[[:api {:namespace "code.framework.docstring"}]]
