(ns xtbench.elisp.lang.common-math-test
  (:use code.test)
  (:require [std.lang :as l]))

(l/script- :elisp
  {:runtime :basic
   :require [[xt.lang.common-math :as xtm]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-math/abs :added "4.1"}
(fact "returns the absolute value"

  (!.elisp
   [(xtm/abs -5)
    (xtm/abs 5)])
  => [5 5])

^{:refer xt.lang.common-math/acos :added "4.1"}
(fact "returns the arccosine"

  (!.elisp
    [(xtm/acos 1)
     (xtm/acos -1)])
  => (contains [(approx 0) (approx 3.14159)]))

^{:refer xt.lang.common-math/asin :added "4.1"}
(fact "returns the arcsine"

  (!.elisp
    [(xtm/asin 0)
     (xtm/asin 1)])
  => (contains [(approx 0) (approx 1.57079)]))

^{:refer xt.lang.common-math/atan :added "4.1"}
(fact "returns the arctangent"

  (!.elisp
    [(xtm/atan 0)
     (xtm/atan 1)])
  => (contains [(approx 0) (approx 0.78539)]))

^{:refer xt.lang.common-math/ceil :added "4.1"}
(fact "rounds up to the nearest integer"

  (!.elisp
   [(xtm/ceil 1.2)
    (xtm/ceil -1.2)])
  => [2 -1])

^{:refer xt.lang.common-math/cos :added "4.1"}
(fact "returns the cosine"

  (!.elisp
   [(xtm/cos 0)
    (xtm/cos 3.1415926535898)])
  => (contains [(approx 1) (approx -1)]))

^{:refer xt.lang.common-math/cosh :added "4.1"}
(fact "returns the hyperbolic cosine"

  (!.elisp
    [(xtm/cosh 0)
     (xtm/cosh 1)])
  => (contains [(approx 1) (approx 1.54308)]))

^{:refer xt.lang.common-math/exp :added "4.1"}
(fact "returns e raised to a power"

  (!.elisp
    [(xtm/exp 0)
     (xtm/exp 1)])
  => (contains [(approx 1) (approx 2.71828)]))

^{:refer xt.lang.common-math/floor :added "4.1"}
(fact "rounds down to the nearest integer"

  (!.elisp
   [(xtm/floor 1.8)
    (xtm/floor -1.2)])
  => [1 -2])

^{:refer xt.lang.common-math/loge :added "4.1"}
(fact "returns the natural logarithm"

  (!.elisp
   (xtm/loge 1))
  => (approx 0))

^{:refer xt.lang.common-math/log10 :added "4.1"}
(fact "returns the base-10 logarithm"

  (!.elisp
   [(xtm/log10 1)
    (xtm/log10 1000)])
  => (contains [(approx 0) (approx 3)]))

^{:refer xt.lang.common-math/max :added "4.1"}
(fact "returns the maximum value"

  (!.elisp
   [(xtm/max 3 7)
    (xtm/max 3 7 5 1)])
  => [7 7])

^{:refer xt.lang.common-math/mod :added "4.1"}
(fact "returns the modulo"

  (!.elisp
    [(xtm/mod 10 3)
     (xtm/mod -1 5)])
  => [1 4])

^{:refer xt.lang.common-math/min :added "4.1"}
(fact "returns the minimum value"

  (!.elisp
   [(xtm/min 3 7)
    (xtm/min 3 7 5 1)])
  => [3 1])

^{:refer xt.lang.common-math/pow :added "4.1"}
(fact "raises a number to a power"

  (!.elisp
   [(xtm/pow 2 5)
    (xtm/pow 9 0.5)])
  => [32 3])

^{:refer xt.lang.common-math/quot :added "4.1"}
(fact "returns floored division"

  (!.elisp
   [(xtm/quot 7 3)
    (xtm/quot 9 3)])
  => [2 3])

^{:refer xt.lang.common-math/sin :added "4.1"}
(fact "returns the sine"

  (!.elisp
   [(xtm/sin 0)
    (xtm/sin 1.5707963267949)])
  => (contains [(approx 0) (approx 1)]))

^{:refer xt.lang.common-math/sinh :added "4.1"}
(fact "returns the hyperbolic sine"

  (!.elisp
    [(xtm/sinh 0)
     (xtm/sinh 1)])
  => (contains [(approx 0) (approx 1.1752)]))

^{:refer xt.lang.common-math/sqrt :added "4.1"}
(fact "returns the square root"

  (!.elisp
   [(xtm/sqrt 4)
    (xtm/sqrt 0)])
  => (contains [(approx 2) (approx 0)]))

^{:refer xt.lang.common-math/tan :added "4.1"}
(fact "returns the tangent"

  (!.elisp
    [(xtm/tan 0)
     (xtm/tan 0.78539816339745)])
  => (contains [(approx 0) (approx 1)]))

^{:refer xt.lang.common-math/tanh :added "4.1"}
(fact "returns the hyperbolic tangent"

  (!.elisp
    [(xtm/tanh 0)
     (xtm/tanh 1)])
  => (contains [(approx 0) (approx 0.76159)]))

^{:refer xt.lang.common-math/mod-pos :added "4.1"}
(fact "returns a positive modulo"

  (!.elisp
   [(xtm/mod-pos -1 5)
    (xtm/mod-pos 6 5)])
  => [4 1])

^{:refer xt.lang.common-math/mod-offset :added "4.1"}
(fact "returns the closest modular offset"

  (!.elisp
   [(xtm/mod-offset 9 1 10)
    (xtm/mod-offset 1 9 10)
    (xtm/mod-offset 2 4 10)])
  => [2 -2 2])

^{:refer xt.lang.common-math/gcd :added "4.1"}
(fact "returns the greatest common divisor"

  (!.elisp
   [(xtm/gcd 54 24)
    (xtm/gcd 10 0)])
  => [6 10])

^{:refer xt.lang.common-math/lcm :added "4.1"}
(fact "returns the least common multiple"

  (!.elisp
   [(xtm/lcm 4 6)
    (xtm/lcm 3 7)])
  => [12 21])

^{:refer xt.lang.common-math/mix :added "4.1"}
(fact "interpolates between two values"

  (!.elisp
   [(xtm/mix 0 10 0.5)
    (xtm/mix 10 20 0.25)])
  => [5 12.5])

^{:refer xt.lang.common-math/sign :added "4.1"}
(fact "returns the sign of a number"

  (!.elisp
   [(xtm/sign -10)
    (xtm/sign 0)
    (xtm/sign 10)])
  => [-1 0 1])

^{:refer xt.lang.common-math/round :added "4.1"}
(fact "rounds to the nearest integer"

  (!.elisp
   [(xtm/round 1.2)
    (xtm/round 1.7)
    (xtm/round 2.5)])
  => [1 2 3])

^{:refer xt.lang.common-math/clamp :added "4.1"}
(fact "clamps a value between bounds"

  (!.elisp
   [(xtm/clamp 0 10 -3)
    (xtm/clamp 0 10 7)
    (xtm/clamp 0 10 12)])
  => [0 7 10])

(comment
  (s/seedgen-benchadd 'xt.lang.common-math {:lang [:r] :write true})
  (s/seedgen-langadd 'xt.lang.common-math {:lang [:lua :python] :write true})
  (s/seedgen-langadd 'xt.lang.common-math {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-math {:lang [:lua :python] :write true}))
