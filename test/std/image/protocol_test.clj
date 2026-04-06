(ns std.image.protocol-test
  (:require [std.image.protocol :refer :all])
  (:use code.test))

^{:refer std.image.protocol/-image-meta :added "3.0"}
(fact "additional information about the image"
  (do (defmethod -image-meta ::mock [_] {:meta true})
      (-image-meta ::mock))
  => {:meta true})

^{:refer std.image.protocol/-image :added "3.0"}
(fact "creates an image based on inputs"
  (do (defmethod -image ::mock [size model data _]
        {:size size :model model :data data})
      (-image [1 2] :gray [1] ::mock))
  => {:size [1 2] :model :gray :data [1]})

^{:refer std.image.protocol/-blank :added "3.0"}
(fact "creates an empty image"
  (do (defmethod -blank ::mock [size model _]
        {:size size :model model})
      (-blank [1 2] :gray ::mock))
  => {:size [1 2] :model :gray})

^{:refer std.image.protocol/-read :added "3.0"}
(fact "reads an image from file"
  (do (defmethod -read ::mock [source model _]
        {:source source :model model})
      (-read "hello.png" :gray ::mock))
  => {:source "hello.png" :model :gray})

^{:refer std.image.protocol/-display :added "3.0"}
(fact "displays an image"
  (do (defmethod -display ::mock [img opts _]
        [img opts])
      (-display :image {:a 1} ::mock))
  => [:image {:a 1}])

^{:refer std.image.protocol/-display-class :added "3.0"}
(fact "types that are able to be displayed"
  (do (defmethod -display-class ::mock [_]
        #{:image/mock})
      (-display-class ::mock))
  => #{:image/mock})

(comment
  (./import))
