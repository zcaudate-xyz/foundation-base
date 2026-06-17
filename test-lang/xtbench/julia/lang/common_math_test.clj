(ns xtbench.julia.lang.common-math-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :julia
  {:runtime :basic
   :require [[xt.lang.common-math :as xtm]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-math/abs :added "4.1"}
(fact "returns the absolute value"

  (!.julia
   [(xtm/abs -5)
    (xtm/abs 5)])
  => [5 5])

^{:refer xt.lang.common-math/acos :added "4.1"}
(fact "returns the arccosine"

  (!.julia
    [(xtm/acos 1)
     (xtm/acos -1)])
  => (contains [(approx 0) (approx 3.14159)]))

^{:refer xt.lang.common-math/asin :added "4.1"}
(fact "returns the arcsine"

  (!.julia
    [(xtm/asin 0)
     (xtm/asin 1)])
  => (contains [(approx 0) (approx 1.57079)]))

^{:refer xt.lang.common-math/atan :added "4.1"}
(fact "returns the arctangent"

  (!.julia
    [(xtm/atan 0)
     (xtm/atan 1)])
  => (contains [(approx 0) (approx 0.78539)]))

^{:refer xt.lang.common-math/ceil :added "4.1"}
(fact "rounds up to the nearest integer"

  (!.julia
   [(xtm/ceil 1.2)
    (xtm/ceil -1.2)])
  => [2 -1])

^{:refer xt.lang.common-math/cos :added "4.1"}
(fact "returns the cosine"

  (!.julia
   [(xtm/cos 0)
    (xtm/cos 3.1415926535898)])
  => (contains [(approx 1) (approx -1)]))

^{:refer xt.lang.common-math/cosh :added "4.1"}
(fact "returns the hyperbolic cosine"

  (!.julia
    [(xtm/cosh 0)
     (xtm/cosh 1)])
  => (contains [(approx 1) (approx 1.54308)]))

^{:refer xt.lang.common-math/exp :added "4.1"}
(fact "returns e raised to a power"

  (!.julia
    [(xtm/exp 0)
     (xtm/exp 1)])
  => (contains [(approx 1) (approx 2.71828)]))

^{:refer xt.lang.common-math/floor :added "4.1"}
(fact "rounds down to the nearest integer"

  (!.julia
   [(xtm/floor 1.8)
    (xtm/floor -1.2)])
  => [1 -2])

^{:refer xt.lang.common-math/loge :added "4.1"}
(fact "returns the natural logarithm"

  (!.julia
   (xtm/loge 1))
  => (approx 0))

^{:refer xt.lang.common-math/log10 :added "4.1"}
(fact "returns the base-10 logarithm"

  (!.julia
   [(xtm/log10 1)
    (xtm/log10 1000)])
  => (contains [(approx 0) (approx 3)]))

^{:refer xt.lang.common-math/max :added "4.1"}
(fact "returns the maximum value"

  (!.julia
   [(xtm/max 3 7)
    (xtm/max 3 7 5 1)])
  => [7 7])

^{:refer xt.lang.common-math/mod :added "4.1"}
(fact "returns the modulo"

  (!.julia
    [(xtm/mod 10 3)
     (xtm/mod -1 5)])
  => [1 4])

^{:refer xt.lang.common-math/min :added "4.1"}
(fact "returns the minimum value"

  (!.julia
   [(xtm/min 3 7)
    (xtm/min 3 7 5 1)])
  => [3 1])

^{:refer xt.lang.common-math/pow :added "4.1"}
(fact "raises a number to a power"

  (!.julia
   [(xtm/pow 2 5)
    (xtm/pow 9 0.5)])
  => [32 3])

^{:refer xt.lang.common-math/quot :added "4.1"}
(fact "returns floored division"

  (!.julia
   [(xtm/quot 7 3)
    (xtm/quot 9 3)])
  => [2 3])

^{:refer xt.lang.common-math/sin :added "4.1"}
(fact "returns the sine"

  (!.julia
   [(xtm/sin 0)
    (xtm/sin 1.5707963267949)])
  => (contains [(approx 0) (approx 1)]))

^{:refer xt.lang.common-math/sinh :added "4.1"}
(fact "returns the hyperbolic sine"

  (!.julia
    [(xtm/sinh 0)
     (xtm/sinh 1)])
  => (contains [(approx 0) (approx 1.1752)]))

^{:refer xt.lang.common-math/sqrt :added "4.1"}
(fact "returns the square root"

  (!.julia
   [(xtm/sqrt 4)
    (xtm/sqrt 0)])
  => (contains [(approx 2) (approx 0)]))

^{:refer xt.lang.common-math/tan :added "4.1"}
(fact "returns the tangent"

  (!.julia
    [(xtm/tan 0)
     (xtm/tan 0.78539816339745)])
  => (contains [(approx 0) (approx 1)]))

^{:refer xt.lang.common-math/tanh :added "4.1"}
(fact "returns the hyperbolic tangent"

  (!.julia
    [(xtm/tanh 0)
     (xtm/tanh 1)])
  => (contains [(approx 0) (approx 0.76159)]))

^{:refer xt.lang.common-math/mod-pos :added "4.1"}
(fact "returns a positive modulo"

  (!.julia
   [(xtm/mod-pos -1 5)
    (xtm/mod-pos 6 5)])
  => [4 1])

^{:refer xt.lang.common-math/mod-offset :added "4.1"}
(fact "returns the closest modular offset"

  (!.julia
   [(xtm/mod-offset 9 1 10)
    (xtm/mod-offset 1 9 10)
    (xtm/mod-offset 2 4 10)])
  => [2 -2 2])

^{:refer xt.lang.common-math/gcd :added "4.1"}
(fact "returns the greatest common divisor"

  (!.julia
   [(xtm/gcd 54 24)
    (xtm/gcd 10 0)])
  => [6 10])

^{:refer xt.lang.common-math/lcm :added "4.1"}
(fact "returns the least common multiple"

  (!.julia
   [(xtm/lcm 4 6)
    (xtm/lcm 3 7)])
  => [12 21])

^{:refer xt.lang.common-math/mix :added "4.1"}
(fact "interpolates between two values"

  (!.julia
   [(xtm/mix 0 10 0.5)
    (xtm/mix 10 20 0.25)])
  => [5 12.5])

^{:refer xt.lang.common-math/sign :added "4.1"}
(fact "returns the sign of a number"

  (!.julia
   [(xtm/sign -10)
    (xtm/sign 0)
    (xtm/sign 10)])
  => [-1 0 1])

^{:refer xt.lang.common-math/round :added "4.1"}
(fact "rounds to the nearest integer"

  (!.julia
   [(xtm/round 1.2)
    (xtm/round 1.7)
    (xtm/round 2.5)])
  => [1 2 3])

^{:refer xt.lang.common-math/clamp :added "4.1"}
(fact "clamps a value between bounds"

  (!.julia
   [(xtm/clamp 0 10 -3)
    (xtm/clamp 0 10 7)
    (xtm/clamp 0 10 12)])
  => [0 7 10])

(comment
  (s/seedgen-benchadd 'xt.lang.common-math {:lang [:r] :write true})
  (s/seedgen-langadd 'xt.lang.common-math {:lang [:lua :python] :write true})
  (s/seedgen-langadd 'xt.lang.common-math {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-math {:lang [:lua :python] :write true}))
