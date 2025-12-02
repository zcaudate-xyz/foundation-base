(ns code.manage.var-test
  (:use code.test)
  (:require [code.manage.var :refer :all]
            [code.edit :as nav]
            [code.framework :as base]))

^{:refer code.manage.var/create-candidates :added "3.0"}
(fact "creates candidates for search"
  (create-candidates '[a :as b] 'a/c 'c)
  => #{'a/c 'b/c})

^{:refer code.manage.var/find-candidates :added "3.0"}
(fact "finds candidates in current namespace"
  (find-candidates (nav/parse-string "(ns a (:require [b :as c]))") 'b/d)
  => [#{'b/d 'c/d} 'b 'd])

^{:refer code.manage.var/find-usages :added "3.0"}
(fact "top-level find-usage query"
  (with-redefs [base/locate-code (constantly [])]
    (find-usages 'code.manage.var {:var 'code.manage.var/create-candidates}
                 {'code.manage.var "src/code/manage/var.clj"} nil))
  => vector?)
