(ns code.build-test
  (:use code.test)
  (:require [code.build :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

^{:refer code.build/project-form :added "4.0"}
(fact "constructs the `project.clj` form")

^{:refer code.build/build-deps :added "4.0"}
(fact "gets dependencies for a given file")

^{:refer code.build/build-prep :added "4.0"}
(fact "prepares the build environment or data structures for a given namespace, returning a vector of prepared items"
  
  (build-prep 'std.lang)
  => vector?)

^{:refer code.build/build-copy :added "4.0"}
(fact "copies deps to the build directory")

^{:refer code.build/build-output :added "4.0"}
(fact "outputs all files to the build directory")

(comment
  (s/run:interrupt))
