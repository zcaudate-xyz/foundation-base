(ns std.lang.base.book-meta-test
  (:use code.test)
  (:require [std.lang.base.book-meta :refer :all]
            [std.lang.base.util :as ut]
            [std.lib :as h]))

^{:refer std.lang.base.book-meta/book-meta? :added "4.0"}
(fact "checks if object is a book meta"
  ^:hidden
  (book-meta? (book-meta {})) => true)

^{:refer std.lang.base.book-meta/book-meta :added "4.0"}
(fact "creates a book meta"

  (book-meta {:module-export  (fn [{:keys [as]} opts]
                                (h/$ (return ~as)))
              :module-import  (fn [name {:keys [as]} opts]  
                                (h/$ (var ~as := (require ~(str name)))))
              :has-ptr        (fn [ptr]
                                (list 'not= (ut/sym-full ptr) nil))
              :teardown-ptr   (fn [ptr]
                                (list := (ut/sym-full ptr) nil))})
  => book-meta?)
