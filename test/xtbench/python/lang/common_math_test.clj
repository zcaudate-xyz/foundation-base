(ns
 xtbench.python.lang.common-math-test
 (:use code.test)
 (:require [std.lang :as l]))

(l/script-
 :python
 {:runtime :basic, :require [[xt.lang.common-math :as xtm]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-math/abs, :added "4.1"}
(fact
 "returns the absolute value"
 (!.py [(xtm/abs -5) (xtm/abs 5)])
 =>
 [5 5])

^{:refer xt.lang.common-math/acos, :added "4.1"}
(fact
 "returns the arccosine"
 (!.py [(xtm/acos 1) (xtm/acos -1)])
 =>
 (contains [(approx 0) (approx 3.1415926535898)]))

^{:refer xt.lang.common-math/asin, :added "4.1"}
(fact
 "returns the arcsine"
 (!.py [(xtm/asin 0) (xtm/asin 1)])
 =>
 (contains [(approx 0) (approx 1.5707963267949)]))

^{:refer xt.lang.common-math/atan, :added "4.1"}
(fact
 "returns the arctangent"
 (!.py [(xtm/atan 0) (xtm/atan 1)])
 =>
 (contains [(approx 0) (approx 0.78539816339745)]))

^{:refer xt.lang.common-math/ceil, :added "4.1"}
(fact
 "rounds up to the nearest integer"
 (!.py [(xtm/ceil 1.2) (xtm/ceil -1.2)])
 =>
 [2 -1])

^{:refer xt.lang.common-math/cos, :added "4.1"}
(fact
 "returns the cosine"
 (!.py [(xtm/cos 0) (xtm/cos 3.1415926535898)])
 =>
 (contains [(approx 1) (approx -1)]))

^{:refer xt.lang.common-math/cosh, :added "4.1"}
(fact
 "returns the hyperbolic cosine"
 (!.py [(xtm/cosh 0) (xtm/cosh 1)])
 =>
 (contains [(approx 1) (approx 1.5430806348152)]))

^{:refer xt.lang.common-math/exp, :added "4.1"}
(fact
 "returns e raised to a power"
 (!.py [(xtm/exp 0) (xtm/exp 1)])
 =>
 (contains [(approx 1) (approx 2.718281828459)]))

^{:refer xt.lang.common-math/floor, :added "4.1"}
(fact
 "rounds down to the nearest integer"
 (!.py [(xtm/floor 1.8) (xtm/floor -1.2)])
 =>
 [1 -2])

^{:refer xt.lang.common-math/loge, :added "4.1"}
(fact "returns the natural logarithm" (!.py (xtm/loge 1)) => (approx 0))

^{:refer xt.lang.common-math/log10, :added "4.1"}
(fact
 "returns the base-10 logarithm"
 (!.py [(xtm/log10 1) (xtm/log10 1000)])
 =>
 (contains [(approx 0) (approx 3)]))

^{:refer xt.lang.common-math/max, :added "4.1"}
(fact
 "returns the maximum value"
 (!.py [(xtm/max 3 7) (xtm/max 3 7 5 1)])
 =>
 [7 7])

^{:refer xt.lang.common-math/mod, :added "4.1"}
(fact
 "returns the modulo"
 (!.py [(xtm/mod 10 3) (xtm/mod -1 5)])
 =>
 [1 4])

^{:refer xt.lang.common-math/min, :added "4.1"}
(fact
 "returns the minimum value"
 (!.py [(xtm/min 3 7) (xtm/min 3 7 5 1)])
 =>
 [3 1])

^{:refer xt.lang.common-math/pow, :added "4.1"}
(fact
 "raises a number to a power"
 (!.py [(xtm/pow 2 5) (xtm/pow 9 0.5)])
 =>
 [32 3])

^{:refer xt.lang.common-math/quot, :added "4.1"}
(fact
 "returns floored division"
 (!.py [(xtm/quot 7 3) (xtm/quot 9 3)])
 =>
 [2 3])

^{:refer xt.lang.common-math/sin, :added "4.1"}
(fact
 "returns the sine"
 (!.py [(xtm/sin 0) (xtm/sin 1.5707963267949)])
 =>
 (contains [(approx 0) (approx 1)]))

^{:refer xt.lang.common-math/sinh, :added "4.1"}
(fact
 "returns the hyperbolic sine"
 (!.py [(xtm/sinh 0) (xtm/sinh 1)])
 =>
 (contains [(approx 0) (approx 1.1752011936438)]))

^{:refer xt.lang.common-math/sqrt, :added "4.1"}
(fact
 "returns the square root"
 (!.py [(xtm/sqrt 4) (xtm/sqrt 0)])
 =>
 (contains [(approx 2) (approx 0)]))

^{:refer xt.lang.common-math/tan, :added "4.1"}
(fact
 "returns the tangent"
 (!.py [(xtm/tan 0) (xtm/tan 0.78539816339745)])
 =>
 (contains [(approx 0) (approx 1)]))

^{:refer xt.lang.common-math/tanh, :added "4.1"}
(fact
 "returns the hyperbolic tangent"
 (!.py [(xtm/tanh 0) (xtm/tanh 1)])
 =>
 (contains [(approx 0) (approx 0.76159415595576)]))

^{:refer xt.lang.common-math/mod-pos, :added "4.1"}
(fact
 "returns a positive modulo"
 (!.py [(xtm/mod-pos -1 5) (xtm/mod-pos 6 5)])
 =>
 [4 1])

^{:refer xt.lang.common-math/mod-offset, :added "4.1"}
(fact
 "returns the closest modular offset"
 (!.py
  [(xtm/mod-offset 9 1 10)
   (xtm/mod-offset 1 9 10)
   (xtm/mod-offset 2 4 10)])
 =>
 [2 -2 2])

^{:refer xt.lang.common-math/gcd, :added "4.1"}
(fact
 "returns the greatest common divisor"
 (!.py [(xtm/gcd 54 24) (xtm/gcd 10 0)])
 =>
 [6 10])

^{:refer xt.lang.common-math/lcm, :added "4.1"}
(fact
 "returns the least common multiple"
 (!.py [(xtm/lcm 4 6) (xtm/lcm 3 7)])
 =>
 [12 21])

^{:refer xt.lang.common-math/mix, :added "4.1"}
(fact
 "interpolates between two values"
 (!.py [(xtm/mix 0 10 0.5) (xtm/mix 10 20 0.25)])
 =>
 [5 12.5])

^{:refer xt.lang.common-math/sign, :added "4.1"}
(fact
 "returns the sign of a number"
 (!.py [(xtm/sign -10) (xtm/sign 0) (xtm/sign 10)])
 =>
 [-1 0 1])

^{:refer xt.lang.common-math/round, :added "4.1"}
(fact
 "rounds to the nearest integer"
 (!.py [(xtm/round 1.2) (xtm/round 1.7) (xtm/round 2.5)])
 =>
 [1 2 3])

^{:refer xt.lang.common-math/clamp, :added "4.1"}
(fact
 "clamps a value between bounds"
 (!.py [(xtm/clamp 0 10 -3) (xtm/clamp 0 10 7) (xtm/clamp 0 10 12)])
 =>
 [0 7 10])
