;; ## Stemmers

;; A top-level interface to the stemmers, handling tokenising of
;; sentences and phrases, as well as removing extra-short and -long
;; words, and excluding common noisy words (see `*excluded-words*`).

(ns std.text.index.stemmer
  (:require [clojure.string]
            [std.string.common :as str]
            [std.text.index.porter :as porter]))

;; For later (internal) use.
(def default-stemmer ^{:private true} porter/stem)

;; Set of specific words to exclude from stemming.
(def ^:dynamic *excluded-words*
  #{"the" "and" "was" "are" "not" "you" "were" "that" "this" "did"
    "etc" "there" "they" "our" "their"})

;; Ignore words shorter than this.
(def ^:dynamic *min-word-length* 3)

;; Ignore words longer than this.
(def ^:dynamic *max-word-length* 30)

(defn excluded-word?
  "checks if word is excluded from indexing
 
   (excluded-word? \"and\")
   => true"
  {:added "3.0"}
  ([word]
   (or (not (<= *min-word-length* (count word) *max-word-length*))
       (boolean (*excluded-words* word)))))

(defn remove-excluded-words
  "remove excluded words"
  {:added "3.0"}
  ([word-seq]
   (filter (complement excluded-word?)
           word-seq)))

(defn expand-hyphenated-words
  "split hyphenated words
 
   (expand-hyphenated-words [\"hello-world\"])
   => [\"hello-world\" \"hello\" \"world\"]"
  {:added "3.0"}
  ([word-seq]
   (mapcat (fn [^String w]
             (if (.contains w "-") (conj (seq (.split w "-")) w) [w]))
           word-seq)))

;; ## Top-level interface

(defn tokenise
  "makes tokens from text
 
   (tokenise \"The lazy brown fox jumped over\")
   => [\"lazy\" \"brown\" \"fox\" \"jumped\" \"over\"]"
  {:added "3.0"}
  ([^String txt]
   (-> (clojure.string/replace txt #"[^-\d\w]+" " ")
       (.toLowerCase)
       (clojure.string/split #"\s+")
       expand-hyphenated-words
       remove-excluded-words)))

(defn stems
  "classifies text into word stems
 
   (stems \"The lazy brown fox jumped over\")
   => [\"lazi\" \"brown\" \"fox\" \"jump\" \"over\"]"
  {:added "3.0"}
  ([phrase] (stems phrase default-stemmer))
  ([phrase stemmer-func]
   (map stemmer-func (tokenise phrase))))
