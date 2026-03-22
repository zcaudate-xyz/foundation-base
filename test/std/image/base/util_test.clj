
(ns std.image.base.util-test
  (:require [std.image.base.util :refer :all])
  (:use code.test))

^{:refer std.image.base.util/int->bytes :added "3.0"}
(fact "returns bytes value given an integer"

  (int->bytes 4278854200)
  => [255 10 34 56])

^{:refer std.image.base.util/bytes->int :added "3.0"}
(fact "returns the integer value for bytes"

  (bytes->int [255 10 34 56])
  => 4278854200)

^{:refer std.image.base.util/array-fn :added "3.0"}
(fact "returns the appropriate array function for the given inputs"

  (array-fn [5 5 5])
  => short-array

  (array-fn [8 8 8 8])
  => int-array)

^{:refer std.image.base.util/mask :added "3.0"}
(fact "returns the number representation of the binary bit mask of `n` digits"

  (mask 5) => 31
  (mask 7) => 127)

^{:refer std.image.base.util/form-params :added "3.0"}
(fact "returns the mask and start values given a list of elements"

  (form-params [5 5 5])
  => '[(31 31 31) [0 5 10]]

  (form-params [8 8 8 8])
  => '[(255 255 255 255) [0 8 16 24]])

^{:refer std.image.base.util/<<form :added "3.0"}
(fact "returns the form for evaluating a set of elements"

  (<<form [5 5 5])
  => '(clojure.core/fn [[i2 i1 i0]]
        (clojure.core/bit-or (clojure.core/bit-shift-left
                              (clojure.core/bit-and i2 31) 10)
                             (clojure.core/bit-shift-left
                              (clojure.core/bit-and i1 31) 5)
                             (clojure.core/bit-shift-left
                              (clojure.core/bit-and i0 31) 0))))

^{:refer std.image.base.util/<<fn :added "3.0"}
(fact "returns the function that will output a value given a vector of inputs"

  ((<<fn [5 5 5]) [10 20 30])
  => 10910)

^{:refer std.image.base.util/<< :added "3.0"}
(fact "returns the low-level representation of data"

  (-> (<< [[10 20 30]
           [10 20 30]] [5 5 5])
      vec)
  => [10910 10910])

^{:refer std.image.base.util/>>form :added "3.0"}
(fact "returns the form for turning an integer into representations"

  (>>form [5 5 5])
  => '(clojure.core/fn [v]
        [(clojure.core/bit-and (clojure.core/bit-shift-right v 10) 31)
         (clojure.core/bit-and (clojure.core/bit-shift-right v 5) 31)
         (clojure.core/bit-and (clojure.core/bit-shift-right v 0) 31)]))

^{:refer std.image.base.util/>>fn :added "3.0"}
(fact "returns a vector representation given an input value"

  ((>>fn [5 5 5]) 10910)
  => [10 20 30])

^{:refer std.image.base.util/>> :added "3.0"}
(fact "returns human readable version of the raw bytes"

  (-> (short-array [10910 10910])
      (>> [5 5 5]))
  => [[10 20 30] [10 20 30]])

^{:refer std.image.base.util/byte-argb->byte-gray :added "3.0"}
(fact "converts a byte-argb array to byte-gray"

  (-> (byte-array [255 10 10 10 255 20 20 20])
      (byte-argb->byte-gray)
      (vec))
  => [10 20])

^{:refer std.image.base.util/int-argb->byte-gray :added "3.0"}
(fact "converts a int-argb array to byte-gray"

  (->> [[255 10 10 10] [255 20 20 20]]
       (map bytes->int)
       (int-array)
       (int-argb->byte-gray)
       (vec))
  => [10 20])

^{:refer std.image.base.util/byte-gray->int-argb :added "3.0"}
(fact "converts a byte-gray array to int-argb"

  (->> (byte-array [10 20])
       (byte-gray->int-argb)
       (mapv int->bytes))
  => [[255 10 10 10] [255 20 20 20]])

(comment
  (./import)
  (./scaffold))
