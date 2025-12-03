(ns code.manage.ns-rename-test
  (:use code.test)
  (:require [code.manage.ns-rename :refer :all]))

^{:refer code.manage.ns-rename/move-list :added "3.0"}
(fact "compiles a list of file movements"
  ^:hidden

  (with-redefs [std.fs/select (constantly [])
                std.fs/move (constantly nil)]
    (move-list ['old 'new] {} nil {:source-paths ["src"]}))
  => [])

^{:refer code.manage.ns-rename/change-list :added "3.0"}
(fact "compiles a list of code changes"
  ^:hidden

  (with-redefs [code.framework/transform-code (constantly {:deltas []})]
    (vec (change-list ['old 'new] {} {'old "path"} nil)))
  => (just []))

^{:refer code.manage.ns-rename/ns-rename :added "3.0"}
(fact "top-level ns rename function"
  ^:hidden

  (with-redefs [change-list (constantly [])
                move-list (constantly [])]
    (ns-rename ['old 'new] {} nil nil))
  => {:changes [], :moves []})
