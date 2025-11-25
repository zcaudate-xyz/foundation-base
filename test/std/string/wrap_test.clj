(ns std.string.wrap-test
  (:use code.test)
  (:require [std.string.wrap :refer :all]
            [std.string.coerce :as coerce]))

^{:refer std.string.wrap/wrap-fn :added "3.0"}
(fact "multimethod for extending wrap"

  (let [f #(str '+ % '+)]
    (((wrap-fn f) f)
     :hello))
  => :+hello+)

^{:refer std.string.wrap/join :added "3.0"}
(fact "extends common/join to all string-like types"

  (join "." [:a :b :c])
  => :a.b.c)

^{:refer std.string.wrap/wrap :added "3.0"}
(fact "enables string-like ops for more types"

  ((wrap #(str '+ % '+)) 'hello)
  => '+hello+)
