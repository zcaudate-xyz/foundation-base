(ns xt.lang.common-string
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]])
  (:refer-clojure :exclude [abs bit-and bit-or bit-xor type get-in identity inc
                            dec zero? pos? neg? even? odd? max min mod quot
                            cat eval apply print nil? fn? first second nth
                            replace last sort sort-by throw]))

(l/script :xtalk
  {:require [[xt.lang.base-macro :as k]]})

(l/intern-macros :xtalk 'xt.lang.base-macro)

(defspec.xt StringPair
  [:xt/tuple [:xt/maybe :xt/str] :xt/str])

(defspec.xt sym-full
  [:fn [[:xt/maybe :xt/str] :xt/str] :xt/str])

(defspec.xt sym-name
  [:fn [:xt/str] :xt/str])

(defspec.xt sym-ns
  [:fn [:xt/str] [:xt/maybe :xt/str]])

(defspec.xt sym-pair
  [:fn [:xt/str] StringPair])

(defspec.xt starts-with?
  [:fn [:xt/str :xt/str] :xt/bool])

(defspec.xt ends-with?
  [:fn [:xt/str :xt/str] :xt/bool])

(defspec.xt capitalize
  [:fn [:xt/str] :xt/str])

(defspec.xt decapitalize
  [:fn [:xt/str] :xt/str])

(defspec.xt pad-left
  [:fn [:xt/str :xt/num :xt/str] :xt/str])

(defspec.xt pad-right
  [:fn [:xt/str :xt/num :xt/str] :xt/str])

(defspec.xt pad-lines
  [:fn [:xt/str :xt/num] :xt/str])

(defspec.xt split-long
  [:fn [:xt/str :xt/num] [:xt/array :xt/str]])

(defspec.xt str-rand
  [:fn [:xt/num] :xt/str])

(defn.xt sym-full
  "creates a sym"
  {:added "4.1"}
  [ns name]
  (if (x:nil? ns)
    (return name)
    (return (x:cat ns "/" name))))

(defn.xt sym-name
  "gets the name part of the sym"
  {:added "4.1"}
  [sym]
  (var idx (x:str-index-of sym "/"))
  (return (x:str-substring sym
                           (x:offset (- idx (x:offset-len))))))

(defn.xt sym-ns
  "gets the namespace part of the sym"
  {:added "4.1"}
  [sym]
  (var idx (x:str-index-of sym "/"))
  (if (< 0 idx)
    (return (x:str-substring sym 0 (- idx (x:offset))))
    (return nil)))

(defn.xt sym-pair
  "gets the sym pair"
  {:added "4.1"}
  [sym]
  (return [(-/sym-ns sym)
           (-/sym-name sym)]))

(defn.xt starts-with?
  "check for starts with"
  {:added "4.1"}
  [s match]
  (return (== (x:str-substring s
                               (x:offset)
                               (x:str-len match))
              match)))

(defn.xt ends-with?
  "check for ends with"
  {:added "4.1"}
  [s match]
  (return (== match
              (x:str-substring
               s
               (x:offset (- (x:str-len s)
                            (x:str-len match)))
               (x:str-len s)))))

(defn.xt capitalize
  "uppercases the first letter"
  {:added "4.1"}
  [s]
  (return (x:cat (x:str-to-upper
                   (x:str-substring s
                                    (x:offset 0)
                                    1))
                  (x:str-substring s
                                   (x:offset 1)))))

(defn.xt decapitalize
  "lowercases the first letter"
  {:added "4.1"}
  [s]
  (return (x:cat (x:str-to-lower
                   (x:str-substring s
                                    (x:offset 0)
                                    1))
                  (x:str-substring s
                                   (x:offset 1)))))

(defn.xt pad-left
  "pads string with n chars on left"
  {:added "4.1"}
  [s n ch]
  (var l := (- n (x:offset (x:str-len s))))
  (var out := s)
  (for:index [i [0 l]]
    (:= out (x:cat ch out)))
  (return out))

(defn.xt pad-right
  "pads string with n chars on right"
  {:added "4.1"}
  [s n ch]
  (var l := (- n (x:offset (x:str-len s))))
  (var out := s)
  (for:index [i [0 l]]
    (:= out (x:cat out ch)))
  (return out))

(defn.xt pad-lines
  "pad lines with starting chars"
  {:added "4.1"}
  [s n ch]
  (var lines (x:str-split s "\n"))
  (var out := "")
  (for:array [line lines]
    (when (< 0 (x:len out))
      (:= out (x:cat out "\n")))
    (:= out (x:cat out (-/pad-left "" n " ") line)))
  (return out))

(defn.xt split-long
  "splits a long string"
  {:added "4.1"}
  [s line-len]
  (when (or (x:nil? s)
            (== 0 (x:str-len s)))
    (return []))
  (:= line-len (or line-len 50))
  (var total (x:str-len s))
  (var lines (x:m-ceil (/ total line-len)))
  (var out [])
  (for:index [i [0 lines 1]]
    (var line (x:str-substring s
                               (* i line-len)
                               (* (+ i 1) line-len)))
    (when (< 0 (x:str-len line))
      (x:arr-push out line)))
  (return out))

(defn.xt str-rand
  "creates a random alpha-numeric string"
  {:added "4.1"}
  [n]
  (var choices ["A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L" "M" "N"
                "O" "P" "Q" "R" "S" "T" "U" "V" "W" "X" "Y" "Z"
                "a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n"
                "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"
                "0" "1" "2" "3" "4" "5" "6" "7" "8"])
  (var out "")
  (for:index [i [0 n]]
    (var idx (x:m-floor (* (x:random) (x:len choices))))
    (:= out (x:cat out (x:get-idx choices (x:offset idx)))))
  (return out))
