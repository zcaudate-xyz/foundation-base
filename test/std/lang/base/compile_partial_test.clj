(ns std.lang.base.compile-partial-test
  (:use code.test)
  (:require [std.lang.base.compile :refer :all]
            [std.make.compile :as compile]
            [std.fs :as fs]))

(fact "compile-module-directory-partial"
  (with-redefs [fs/select (constantly ["src/xt/lang/base_lib.clj" "src/xt/lang/base_iter.clj"])
                fs/file-namespace (fn [p] (if (.contains p "base_lib") 'xt.lang.base-lib 'xt.lang.base-iter))]
    (compile/with:mock-compile
      (compile-module-directory-partial
       'xt.lang.base-lib
       {:lang :lua
        :root ".build"
        :target "src"
        :main 'xt.lang})))
  => (contains {:files 1}))

(fact "compile-module-directory-selected-partial"
  (compile/with:mock-compile
    (compile-module-directory-selected-partial
     'xt.lang.base-lib
     :directory
     ['xt.lang.base-lib 'xt.lang.base-iter]
     {:lang :lua :main 'xt.lang.base-lib :root ".build" :target "src"}))
  => (contains {:files 1}))
