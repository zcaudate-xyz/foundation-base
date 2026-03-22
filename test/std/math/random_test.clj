(ns std.math.random-test
  (:require [std.math.random :refer :all])
  (:use code.test)
  (:refer-clojure :exclude [rand rand-nth rand-int]))

^{:refer std.math.random/rand-gen :added "3.0"}
(fact "creates a random number generator"

  (rand-gen)
  => org.apache.commons.math3.random.MersenneTwister)

^{:refer std.math.random/rand-seed! :added "3.0"}
(fact "sets the seed of a given random number generator"

  (-> (rand-gen)
      (rand-seed! 10)
      (rand))
  => (any 0.77132064549269
          0.5711645232847797))

^{:refer std.math.random/rand :added "3.0"}
(fact "returns a random double between 0 and 1"

  (rand)
  ;;0.19755427425784822
  => number?

  (rand (rand-gen))
  ;;0.8479218396605446
  => number?)

^{:refer std.math.random/rand-int :added "3.0"}
(fact "returns a random integer less than `n`"

  (rand-int 100)
  ;; 16
  => integer?)

^{:refer std.math.random/rand-nth :added "3.0"}
(fact "returns a random element in an array"

  (rand-nth [:a :b :c])
  => #{:a :b :c})

^{:refer std.math.random/rand-normal :added "3.0"}
(fact "returns a random number corresponding to the normal distribution"

  (rand-normal)
  ;;-0.6591021470679017
  => number?)

^{:refer std.math.random/rand-digits :added "3.0"}
(fact "constructs a n digit string"

  (rand-digits 10)
  ;; "9417985847"
  => string?)

^{:refer std.math.random/rand-sample :added "3.0"}
(fact "takes from the collection given specified proportions"

  (rand-sample [:a :b :c] [1 2 3])
  => keyword?)
