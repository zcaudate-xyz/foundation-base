(ns std.image.protocol-test
  (:require [std.image.protocol :refer :all])
  (:use code.test))

^{:refer std.image.protocol/-image-meta :added "3.0"}
(comment "additional information about the image")

^{:refer std.image.protocol/-image :added "3.0"}
(comment "creates an image based on inputs")

^{:refer std.image.protocol/-blank :added "3.0"}
(comment "creates an empty image")

^{:refer std.image.protocol/-read :added "3.0"}
(comment "reads an image from file")

^{:refer std.image.protocol/-display :added "3.0"}
(comment "displays an image")

^{:refer std.image.protocol/-display-class :added "3.0"}
(comment "types that are able to be displayed")

(comment
  (./import))
