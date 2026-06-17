(ns r.core
  (:require [hara.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [library]))

(l/script :r
  {})

(def.R add
  "adds two numbers"
  {:added "4.1"}
  (fn [a b]
    (+ a b)))

(def.R mean-of
  "computes the mean of a vector"
  {:added "4.1"}
  (fn [v]
    (mean v)))

(def.R linear-model
  "fits a simple linear model"
  {:added "4.1"}
  (fn [x y]
    (lm (formula y x) :data (df {:x x :y y}))))

(comment
  (l/!.R (add 1 2))
  (l/!.R (mean-of [1 2 3 4]))
  (l/!.R (|> [1 2 3 4] (mean)))
  (l/!.R (%in% 2 [1 2 3])))
