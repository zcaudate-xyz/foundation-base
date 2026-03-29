(ns xt.lang.common-math
  (:require [std.lang :as l :refer [defspec.xt]])
  (:refer-clojure :exclude []))

(l/script :xtalk)

(defspec.xt mod-pos
  [:fn [:xt/num :xt/num] :xt/num])

(defspec.xt mod-offset
  [:fn [:xt/num :xt/num :xt/num] :xt/num])

(defspec.xt gcd
  [:fn [:xt/num :xt/num] :xt/num])

(defspec.xt lcm
  [:fn [:xt/num :xt/num] :xt/num])

(defspec.xt mix
  [:fn [:xt/num :xt/num :xt/num [:xt/maybe [:fn [:xt/num] :xt/num]]] :xt/num])

(defspec.xt sign
  [:fn [:xt/num] :xt/num])

(defspec.xt round
  [:fn [:xt/num] :xt/num])

(defspec.xt clamp
  [:fn [:xt/num :xt/num :xt/num] :xt/num])

(defspec.xt bit-count
  [:fn [:xt/num] :xt/num])

(def$.xt sin x:m-sin)

(defn.xt mod-pos
  "gets the positive mod"
  {:added "4.1"}
  [val modulo]
  (var out (mod val modulo))
  (return
   (:? (< out 0)
       (+ out modulo)
       out)))

(defn.xt mod-offset
  "calculates the closest offset"
  {:added "4.1"}
  [pval nval modulo]
  (var offset (mod (- nval pval) modulo))
  (cond (> (x:m-abs offset)
           (/ modulo 2))
        (cond (> offset 0)
              (return (- offset modulo))
              :else
              (return (+ offset modulo)))
        :else
        (return offset)))

(defn.xt gcd
  "greatest common denominator"
  {:added "4.1"}
  [a b]
  (return (:? (== 0 b)
              a
              (-/gcd b (mod a b)))))

(defn.xt lcm
  "lowest common multiple"
  {:added "4.1"}
  [a b]
  (return (/ (* a b)
             (-/gcd a b))))

(defn.xt mix
  "mixes two values with a fraction"
  {:added "4.1"}
  [x0 x1 v f]
  (when (x:nil? f)
    (:= f (fn:> [x] x)))
  (return (+ x0 (* (- x1 x0)
                   (f v)))))

(defn.xt sign
  "gets the sign"
  {:added "4.1"}
  [x]
  (cond (== x 0) (return 0)
        (< x 0)  (return -1)
        :else    (return 1)))

(defn.xt round
  "rounds to the nearest integer"
  {:added "4.1"}
  [x]
  (return (x:m-floor (+ x 0.5))))

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
