(ns code.tool.build-test
  (:use code.test)
  (:require [code.tool.build :refer :all]
            [std.lang :as l]
            [std.lib :as h]
            [jvm.deps :as deps]))

^{:refer code.tool.build/project-form :added "4.0"}
(fact "constructs the `project.clj` form"
  (vec (project-form {:a {:dependencies [['a "1.0"]]}} 'my.main))
  => (contains ['defproject 'my "LATEST"
                :dependencies (contains [['org.clojure/clojure "1.12.0"]
                                         ['a "1.0"]])
                :profiles map?]))

^{:refer code.tool.build/build-deps :added "4.0"}
(fact "gets dependencies for a given file"
  (build-deps {} 'std.lang) => empty?)

^{:refer code.tool.build/build-prep :added "4.0"}
(fact "prepares the build environment or data structures for a given namespace, returning a vector of prepared items"
  
  (build-prep 'std.lang)
  => vector?)

^{:refer code.tool.build/build-copy :added "4.0"}
(fact "copies deps to the build directory"
  (with-redefs [std.fs/create-directory (constantly nil)
                clojure.core/spit (constantly nil)
                std.fs/copy-single (constantly nil)
                std.fs/list (constantly [])]
    (build-copy [{} {}] {:ns 'std.lang :root ".build" :build "test"}))
  => true)

^{:refer code.tool.build/build-output :added "4.0"}
(fact "outputs all files to the build directory"
  (with-redefs [build-prep (constantly [{} {} []])
                build-copy (constantly true)
                std.fs/list (constantly [])]
    (build-output {:ns 'std.lang :root ".build" :build "test"}))
  => vector?)

(comment
  (s/run:interrupt))
