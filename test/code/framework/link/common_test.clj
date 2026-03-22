(ns code.framework.link.common-test
  (:require [code.framework.link.common :refer :all])
  (:use code.test))

^{:refer code.framework.link.common/-file-linkage :added "3.0"}
(fact "extendable function for `file-linkage`"
  (-file-linkage "foo.txt")
  => {:file "foo.txt", :exports #{}, :imports #{}})

^{:refer code.framework.link.common/file-linkage-fn :added "3.0"}
(fact "memoized function for `file-linkage` based on time"
  (file-linkage-fn "foo.txt" 0)
  => {:file "foo.txt", :exports #{}, :imports #{}})
