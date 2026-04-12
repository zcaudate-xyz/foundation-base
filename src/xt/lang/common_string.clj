(ns xt.lang.common-string
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]]})

;;
;; XLANG STRING
;;

(defn.xt get-char
  "gets a char from string"
  {:added "4.0"}
  [s i] (return (xt/x:str-char s i)))

(defn.xt split
  "splits a string using a token"
  {:added "4.0"}
  ([s tok] (return (xt/x:str-split s tok))))

(defn.xt join
  "joins an array using a seperator"
  {:added "4.0"}
  ([s arr] (return (xt/x:str-join s arr))))

(defn.xt replace
  "replaces a string with another"
  {:added "4.0"}
  ([s tok replacement]
   (return (xt/x:str-replace s tok replacement))))

(defn.xt index-of
  "returns index of character in string"
  {:added "4.0"}
  ([s tok]
   (return (- (xt/x:str-index-of s tok 0)
              (xt/x:offset 0)))))

(defn.xt substring
  "gets the substring"
  {:added "4.0"}
  ([s start finish]
   (return (xt/x:str-substring s
                               (xt/x:offset start)
                               finish))))

(defn.xt to-uppercase
  "converts string to uppercase"
  {:added "4.0"}
  ([s] (return (xt/x:str-to-upper s))))

(defn.xt to-lowercase
  "converts string to lowercase"
  {:added "4.0"}
  ([s] (return (xt/x:str-to-lower s))))

(defn.xt to-fixed
  "to fixed decimal places"
  {:added "4.0"}
  ([n digits] (return (xt/x:str-to-fixed n digits))))

(defn.xt trim
  "trims a string"
  {:added "4.0"}
  ([s] (return (xt/x:str-trim s))))

(defn.xt trim-left
  "trims a string on left"
  {:added "4.0"}
  ([s] (return (xt/x:str-trim-left s))))

(defn.xt trim-right
  "trims a string on right"
  {:added "4.0"}
  ([s] (return (xt/x:str-trim-right s))))

(defn.xt sym-full
  "creates a sym"
  {:added "4.1"}
  [ns name]
  (if (xt/x:nil? ns)
    (return name)
    (return (xt/x:cat ns "/" name))))

(defn.xt sym-name
  "gets the name part of the sym"
  {:added "4.1"}
  [sym]
  (var idx (xt/x:str-index-of sym "/"))
  (return  (xt/x:str-substring sym
                               (xt/x:offset (- idx (xt/x:offset-len))))))

(defn.xt sym-ns
  "gets the namespace part of the sym"
  {:added "4.1"}
  [sym]
  (var idx (xt/x:str-index-of sym "/"))
  (if (< 0 idx)
    (return (xt/x:str-substring sym 0 (- idx (xt/x:offset))))
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
  (return (== (xt/x:str-substring s
                                  (xt/x:offset)
                                  (xt/x:str-len match))
              match)))

(defn.xt ends-with?
  "check for ends with"
  {:added "4.1"}
  [s match]
  (return (== match
              (xt/x:str-substring
               s
               (xt/x:offset (- (xt/x:str-len s)
                               (xt/x:str-len match)))
               (xt/x:str-len s)))))

(defn.xt capitalize
  "uppercases the first letter"
  {:added "4.1"}
  [s]
  (return (xt/x:cat (xt/x:str-to-upper
                     (xt/x:str-substring s
                                         (xt/x:offset 0)
                                         1))
                    (xt/x:str-substring s (xt/x:offset 1)))))

(defn.xt decapitalize
  "lowercases the first letter"
  {:added "4.1"}
  [s]
  (return (xt/x:cat (xt/x:str-to-lower
                     (xt/x:str-substring s
                                         (xt/x:offset 0)
                                         1))
                    (xt/x:str-substring s
                                        (xt/x:offset 1)))))

(defn.xt pad-left
  "pads string with n chars on left"
  {:added "4.1"}
  [s n ch]
  (var l := (- n (xt/x:offset (xt/x:str-len s))))
  (var out := s)
  (xt/for:index [i [0 l]]
    (:= out (xt/x:cat ch out)))
  (return out))

(defn.xt pad-right
  "pads string with n chars on right"
  {:added "4.1"}
  [s n ch]
  (var l := (- n (xt/x:offset (xt/x:str-len s))))
  (var out := s)
  (xt/for:index [i [0 l]]
    (:= out (xt/x:cat out ch)))
  (return out))

(defn.xt pad-lines
  "pad lines with starting chars"
  {:added "4.1"}
  [s n ch]
  (var lines (xt/x:str-split s "\n"))
  (var out := "")
  (xt/for:array [line lines]
    (when (< 0 (xt/x:len out))
      (:= out (xt/x:cat out "\n")))
    (:= out (xt/x:cat out (-/pad-left "" n " ") line)))
  (return out))

(defn.xt split-long
  "splits a long string"
  {:added "4.1"}
  [s line-len]
  (when (or (xt/x:nil? s)
            (== 0 (xt/x:str-len s)))
    (return []))
  (:= line-len (or line-len 50))
  (var total (xt/x:str-len s))
  (var lines (xt/x:m-ceil (/ total line-len)))
  (var out [])
  (xt/for:index [i [0 lines 1]]
    (var line (-/substring s
                           (* i line-len)
                           (* (+ i 1) line-len)))
    (when (< 0 (xt/x:str-len line))
      (xt/x:arr-push out line)))
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
  (xt/for:index [i [0 (- n 1)]]
    (var idx (xt/x:m-floor (* (xt/x:random) (xt/x:len choices))))
    (:= out (xt/x:cat out (xt/x:get-idx choices (xt/x:offset idx)))))
  (return out))

(defspec.xt tag-string
  [:fn [:xt/str] :xt/str])

(defn.xt tag-string
  "gets the string description for a given tag"
  {:added "4.0"}
  [tag]
  (var [ns name] (-/sym-pair tag))
  (var parts (xt/x:str-split (or ns "") "."))
  (var part-count (xt/x:len parts))
  (var desc (:? ns
                (xt/x:cat (xt/x:get-idx parts (+ part-count
                                                 (xt/x:offset -1)))
                       " ")
                ""))
  (var clean-name (xt/x:str-replace (or name "") "_" " "))
  (:= clean-name  (xt/x:str-replace clean-name "-" " "))
  (:= clean-name  (xt/x:str-replace clean-name (xt/x:str-trim desc) ""))
  (return (xt/x:cat desc clean-name)))
