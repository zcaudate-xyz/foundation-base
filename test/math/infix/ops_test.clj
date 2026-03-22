(ns math.infix.ops-test
  (:require [math.infix.ops :as ops])
  (:use code.test))

^{:refer math.infix.ops/defunary :added "3.0"}
(fact "helper for `java.lang.Math` unary functions")

^{:refer math.infix.ops/defbinary :added "3.0"}
(fact "helper for `java.lang.Math` binary functions")

^{:refer math.infix.ops/divide :added "3.0"}
(fact "computes the divisor with infinity")

^{:refer math.infix.ops/root :added "3.0"}
(fact "computes the nth root")

^{:refer math.infix.ops/gcd :added "3.0"}
(fact "computes the greatest common denominator")

^{:refer math.infix.ops/lcm :added "3.0"}
(fact "computes the lowest common multiple")

^{:refer math.infix.ops/fact :added "3.0"}
(fact "computes the factorial")

^{:refer math.infix.ops/sec :added "3.0"}
(fact "compute secant, given the angle in radians")

^{:refer math.infix.ops/csc :added "3.0"}
(fact "compute cosecant, given the angle in radians")

^{:refer math.infix.ops/cot :added "3.0"}
(fact "compute cotangent, given the angle in radians")

^{:refer math.infix.ops/asec :added "3.0"}
(fact "compute arcsecant, given the number, returns the arcsecant in radians")

^{:refer math.infix.ops/acsc :added "3.0"}
(fact "compute arccosecant, given the number, returns the arccosecant in radians")

^{:refer math.infix.ops/acot :added "3.0"}
(fact "compute arccotangent, given the number, returns the arccotangent in radians")
