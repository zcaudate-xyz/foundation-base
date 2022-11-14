(ns std.object.framework.vector-like-test
  (:use code.test)
  (:require [std.object.framework.vector-like :refer :all]))

^{:refer std.object.framework.vector-like/extend-vector-like :added "3.0"}
(comment "sets the fields of an object with keyword"

  (extend-vector-like test.Cat {:read (fn [x] (seq (.getName x)))
                                :write (fn [arr] (test.Cat. (apply str arr)))})

  (test.Cat. "spike")
  ;=> #test.Cat(\s \p \i \k \e)
  )

(comment
  (code.manage/import))