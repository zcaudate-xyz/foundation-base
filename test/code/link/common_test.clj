(ns code.link.common-test
  (:use code.test)
  (:require [code.link.common :refer :all]))

^{:refer code.link.common/-file-linkage :added "3.0"}
(fact "extendable function for `file-linkage`"
  (-file-linkage "foo.txt")
  => {:file "foo.txt", :exports #{}, :imports #{}})

^{:refer code.link.common/file-linkage-fn :added "3.0"}
(fact "memoized function for `file-linkage` based on time"
  (file-linkage-fn "foo.txt" 0)
  => {:file "foo.txt", :exports #{}, :imports #{}})
