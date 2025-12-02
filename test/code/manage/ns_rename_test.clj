(ns code.manage.ns-rename-test
  (:use code.test)
  (:require [code.manage.ns-rename :refer :all]))

^{:refer code.manage.ns-rename/move-list :added "3.0"}
(fact "compiles a list of file movements"
  (with-redefs [std.fs/select (constantly [])
                std.fs/move (constantly nil)]
    (move-list ['old 'new] {} nil {:source-paths ["src"]}))
  => [])

^{:refer code.manage.ns-rename/change-list :added "3.0"}
(fact "compiles a list of code changes"
  (with-redefs [code.framework/transform-code (constantly {:deltas []})]
    (change-list ['old 'new] {} {'old "path"} nil))
  => (any seq? nil?))

^{:refer code.manage.ns-rename/ns-rename :added "3.0"}
(fact "top-level ns rename function"
  (with-redefs [change-list (constantly [])
                move-list (constantly [])]
    (ns-rename ['old 'new] {} nil nil))
  => {:changes [], :moves []})
