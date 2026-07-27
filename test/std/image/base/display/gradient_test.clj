(ns std.image.base.display.gradient-test
  (:require [std.image.base.display.gradient :refer :all])
  (:use code.test))

(defn str-vals [m]
  (zipmap (keys m)
          (map (fn [c] (if (coll? c)
                         (apply str c)
                         (str c)))
               (vals m))))

^{:refer std.image.base.display.gradient/create-lookup :added "3.0"}
(fact "creates a lookup based on the range"

  (-> (create-lookup "01234" 5 5)
      (str-vals))
  => {5 "0", 6 "1", 7 "2", 8 "3", 9 "4"})

^{:refer std.image.base.display.gradient/create-single :added "3.0"}
(fact "creates a single gradient map"

  (-> (create-single)
      (select-keys [0 1 2 3])
      (str-vals))
  => {0 "Æ", 1 "Ñ", 2 "Ê", 3 "Œ"})

^{:refer std.image.base.display.gradient/create-multi :added "3.0"}
(fact "creates a multi gradient map"

  (-> (create-multi 3)
      (select-keys [0 3 6])
      (str-vals))
  => {0 "ÆÑÊ", 3 "ŒØM", 6 "ÉËÈ"})

^{:refer std.image.base.display.gradient/lookup-char :added "3.0"}
(fact "look up a character based on value"

  (str (lookup-char 0))
  => #{"Æ" "Ñ" "Ê"}

  (str (lookup-char 6))
  => #{"É" "Ë" "È"})

(comment
  (./import)
  (./scaffold))
