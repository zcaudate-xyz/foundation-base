(ns std.object.framework.string-like-test
  (:require [std.object :as object]
            [std.object.framework.string-like :refer :all])
  (:use code.test))

^{:refer std.object.framework.string-like/extend-string-like :added "3.0"}
(fact "creates an entry for string-like classes"

  (extend-string-like
   java.io.File
   {:tag "path"
    :read (fn [^java.io.File f] (.getPath f))
    :write (fn [^String path] (java.io.File. path))})

  (object/from-data "/home" java.io.File)

  (with-out-str
    (prn (java.io.File. "/home")))
  => "#path \"/home\"\n")
