(ns lib.aether.result-test
  (:use code.test)
  (:require [lib.aether.result :refer :all]
            [std.string :as str]
            [jvm.artifact :as artifact])
  (:import (org.eclipse.aether.graph DependencyNode DefaultDependencyNode)
           (org.eclipse.aether.artifact DefaultArtifact)))

^{:refer lib.aether.result/clojure-core? :added "3.0"}
(fact "checks if artifact represents clojure.core"

  (clojure-core? '[org.clojure/clojure "1.2.0"])
  => true)

^{:refer lib.aether.result/prioritise :added "3.0"}
(fact "gives the higher version library more priority"

  (prioritise '[[a "3.0"]
                [a "1.2"]
                [a "1.1"]]
              :coord)
  => '[[a/a "3.0"]])

^{:refer lib.aether.result/print-tree :added "3.0"}
(fact "prints a tree structure"

  (-> (print-tree '[[a "1.1"]
                    [[b "1.1"]
                     [[c "1.1"]
                      [d "1.1"]]]])
      (with-out-str)) ^:hidden
  => string?)

^{:refer lib.aether.result/dependency-graph :added "3.0"}
(fact "creates a dependency graph for the results"
  (let [node (doto (DefaultDependencyNode. (DefaultArtifact. "g:a:1.0"))
               (.setChildren [(DefaultDependencyNode. (DefaultArtifact. "g:b:1.0"))]))]
    (dependency-graph node :coord))
  => ['[g/a "1.0"] ['[g/b "1.0"]]])

^{:refer lib.aether.result/flatten-tree :added "3.0"}
(fact "converts a tree structure into a vector"

  (flatten-tree '[[a "1.1"]
                  [[b "1.1"]
                   [[c "1.1"]
                    [d "1.1"]]]])
  => '[[a "1.1"] [b "1.1"] [c "1.1"] [d "1.1"]])

^{:refer lib.aether.result/summary :added "3.0"}
(fact "creates a summary for the different types of results"
  (summary [] {}) => [])

^{:refer lib.aether.result/return :added "3.0"}
(fact "returns a summary of install and deploy results"
  (return [] [] {:return :default}) => [])

^{:refer lib.aether.result/return-deps :added "3.0"}
(fact "returns a summary of resolve and collect results"
  (return-deps [] [] {:return :resolved}) => [])
