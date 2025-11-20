(ns std.string.prose
  (:require [std.string.common :as common]
            [std.lib.foundation :as h]))

(defn has-quotes?
  "checks if a string has quotes
 
   (has-quotes? \"\\\"hello\\\"\")
   => true"
  {:added "3.0"}
  ([^String s]
   (and (.startsWith s "\"")
        (.endsWith s "\""))))

(defn strip-quotes
  "gets rid of quotes in a string
 
   (strip-quotes \"\\\"hello\\\"\")
   => \"hello\""
  {:added "3.0"}
  ([s]
   (if (has-quotes? s)
     (subs s 1 (dec (count s)))
     s)))

(defn whitespace?
  "checks if the string is all whitespace
 
   (whitespace? \"        \")
   => true"
  {:added "3.0"}
  ([s]
   (boolean (or (= "" s) (re-find #"^[\s\t]+$" s)))))

(defn escape-dollars
  "for regex purposes, escape dollar signs in strings
 
   (escape-dollars \"$\")
   => string?"
  {:added "3.0"}
  ([^String s]
   (.replaceAll s "\\$" "\\\\\\$")))

(defn escape-newlines
  "makes sure that newlines are printable
 
   (escape-newlines \"\\\n\")
   => \"\\\n\""
  {:added "3.0"}
  ([^String s]
   (.replaceAll s "\\n" "\\\\n")))

(defn escape-escapes
  "makes sure that newlines are printable
 
   (escape-escapes \"\\\n\")
   => \"\\\\\n\""
  {:added "3.0"}
  ([^String s]
   (.replaceAll s "(\\\\)([A-Za-z])" "$1$1$2")))

(defn escape-quotes
  "makes sure that quotes are printable in string form
 
   (escape-quotes \"\\\"hello\\\"\")
   => \"\\\\\"hello\\\\\"\""
  {:added "3.0"}
  ([^String s]
   (.replaceAll s "(\\\\)?\"" "$1$1\\\\\\\"")))


(defn filter-empty-lines
  "filter empty line
 
   (filter-empty-lines (common/join \"\\n\" [\"a\" \"  \" \"   \" \"b\"]))
   => \"a\\nb\""
  {:added "3.0"}
  [s]
  (->> (common/split-lines s)
       (remove (fn [line] (re-find #"^\s*$" line)))
       (common/join "\n")))

(defn single-line
  "replace newlines with spaces"
  {:added "3.0"}
  [s]
  (->> (common/split-lines s)
       (common/join " ")))

(defn join-lines
  "join non empty elements in array
 
   (join-lines \"\" [\"hello\" \"world\"])
   => \"helloworld\""
  {:added "4.0"}
  [arr]
  (common/join "\n" arr))

(defn spaces
  "create `n` spaces
 
   (spaces 4)
   => \"\""
  {:added "4.0"}
  ([n]
   (apply str (repeat n " "))))

(defn write-line
  "writes a line based on data structure"
  {:added "4.0"}
  ([v]
   (if (vector? v)
     (let [tag  (first (keys (meta v)))
           body (common/join " " (map write-line v))
           body (if (#{"" :quote} tag)
                  (str "'" body "'")
                  body)]
       body)
     (h/strn v))))

(defn write-lines
  "writes a block of string"
  {:added "4.0"}
  [lines]
  (->> (map write-line lines)
       (common/join "\n")))

(defn indent
  "indents a block of string
   
   (indent (write-lines [\"a\" \"b\" \"c\"]) 2)
   => \"a\\n  b\\n  c\""
  {:added "4.0"}
  ([block n & [{:keys [custom] :or
                {custom ""}}]]
   (if (and (not (pos? n))
            (empty? custom))
     block
     (->> (common/split-lines block)
          (map (fn [line] (str (spaces n) custom line)))
          (common/join "\n")))))

(defn indent-rest
  "indents the rest of the boiy
 
   (indent-rest (write-lines [\"a\" \"b\" \"c\"]) 2)
   => \"a\\n  b\\n  c\""
  {:added "4.0"}
  ([block n & [{:keys [custom] :or
                {custom ""}}] ]
   (if (and (not (pos? n))
            (empty? custom))
     block
     (let [[head & rest] (common/split-lines block)]
       (cond (empty? rest)
             head

             :else
             (->> rest
                  (map (fn [line] (str (spaces n) custom line)))
                  (common/join "\n")
                  (str head "\n")))))))

(defn multi-line?
  "check that a string has newlines"
  {:added "4.0"}
  ([s]
   (and s (boolean (re-find #"\n" s)))))

(defn single-line?
  "check that a string does not have newlines"
  {:added "4.0"}
  ([s]
   (not (multi-line? s))))

(defn layout-lines
  "layout tokens in lines depending on max length
 
   (layout-lines [\"hello\" \"world\" \"again\" \"a\" \"b\"]
                 8)
   => \"hello\\nworld\\nagain a\\nb\""
  {:added "4.0"}
  ([tokens]
   (layout-lines tokens 80))
  ([tokens max]
   (->> (reduce (fn [[lines curr] tok]
                  (let [stok (str tok)]
                    (if (< (inc (+ (count curr)
                                   (count stok)))
                           max)
                      [lines (str curr " " stok)]
                      [(conj lines curr) stok])))
                [[] (str (first tokens))]
                (rest tokens))
        (apply conj)
        (common/join "\n"))))
