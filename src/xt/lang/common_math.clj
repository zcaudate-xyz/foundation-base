(ns xt.lang.common-math
  (:require [std.lang :as l :refer [defspec.xt]])
  (:refer-clojure :exclude [mod round abs]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]]})

(defspec.xt abs [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  abs
  ([value] (list (quote x:m-abs) value)))

(defspec.xt acos [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  acos
  ([value] (list (quote x:m-acos) value)))

(defspec.xt asin [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  asin
  ([value] (list (quote x:m-asin) value)))

(defspec.xt atan [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  atan
  ([value] (list (quote x:m-atan) value)))

(defspec.xt ceil [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  ceil
  ([value] (list (quote x:m-ceil) value)))

(defspec.xt cos [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  cos
  ([value] (list (quote x:m-cos) value)))

(defspec.xt cosh [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  cosh
  ([value] (list (quote x:m-cosh) value)))

(defspec.xt exp [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  exp
  ([value] (list (quote x:m-exp) value)))

(defspec.xt floor [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  floor
  ([value] (list (quote x:m-floor) value)))

(defspec.xt loge [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  loge
  ([value] (list (quote x:m-loge) value)))

(defspec.xt log10 [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  log10
  ([value] (list (quote x:m-log10) value)))

(defspec.xt max [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  max
  ([x y] (list (quote x:m-max) x y)))

(defspec.xt mod [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  mod
  ([x y] (list (quote x:m-mod) x y)))

(defspec.xt min [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  min
  ([x y] (list (quote x:m-min) x y)))

(defspec.xt pow [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  pow
  ([x y] (list (quote x:m-pow) x y)))

(defspec.xt quot [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  quot
  ([x y] (list (quote x:m-quot) x y)))

(defspec.xt sin [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  sin
  ([value] (list (quote x:m-sin) value)))

(defspec.xt sinh [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  sinh
  ([value] (list (quote x:m-sinh) value)))

(defspec.xt sqrt [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  sqrt
  ([value] (list (quote x:m-sqrt) value)))

(defspec.xt tan [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  tan
  ([value] (list (quote x:m-tan) value)))

(defspec.xt tanh [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true} 
  tanh
  ([value] (list (quote x:m-tanh) value)))


(defspec.xt mod-pos
  [:fn [:xt/num :xt/num] :xt/num])

(defn.xt mod-pos
  "gets the positive mod"
  {:added "4.1"}
  [val modulo]
  (var out (xt/x:m-mod val modulo))
  (return
   (:? (< out 0)
       (+ out modulo)
       out)))

(defspec.xt mod-offset
  [:fn [:xt/num :xt/num :xt/num] :xt/num])

(defn.xt mod-offset
  "calculates the closest offset"
  {:added "4.1"}
  [pval nval modulo]
  (var offset (xt/x:m-mod (- nval pval) modulo))
  (cond (> (xt/x:m-abs offset)
           (/ modulo 2))
        (cond (> offset 0)
              (return (- offset modulo))
              :else
              (return (+ offset modulo)))
        :else
        (return offset)))

(defspec.xt gcd
  [:fn [:xt/num :xt/num] :xt/num])

(defn.xt gcd
  "greatest common denominator"
  {:added "4.1"}
  [a b]
  (return (:? (== 0 b)
              a
              (-/gcd b (mod a b)))))

(defspec.xt lcm
  [:fn [:xt/num :xt/num] :xt/num])

(defn.xt lcm
  "lowest common multiple"
  {:added "4.1"}
  [a b]
  (return (/ (* a b)
             (-/gcd a b))))

(defspec.xt mix
  [:fn [:xt/num :xt/num :xt/num] :xt/num])

(defn.xt mix
  "mixes two values with a fraction"
  {:added "4.1"}
  [x0 x1 v]
  (return (+ x0 (* (- x1 x0)
                   v))))

(defspec.xt sign
  [:fn [:xt/num] :xt/num])

(defn.xt sign
  "gets the sign"
  {:added "4.1"}
  [x]
  (cond (== x 0) (return 0)
        (< x 0)  (return -1)
        :else    (return 1)))

(defspec.xt round
  [:fn [:xt/num] :xt/num])

(defn.xt round
  "rounds to the nearest integer"
  {:added "4.1"}
  [x]
  (return (xt/x:m-floor (+ x 0.5))))

(defspec.xt clamp
  [:fn [:xt/num :xt/num :xt/num] :xt/num])

(defn.xt clamp
  "clamps a value between min and max"
  {:added "4.1"}
  [min max v]
  (cond (< v min)
        (return min)
        (< max v)
        (return max)
        :else
        (return v)))
