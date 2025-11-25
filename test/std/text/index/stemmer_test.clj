(ns std.text.index.stemmer-test
  (:use code.test)
  (:require [std.text.index.stemmer :refer :all]))

^{:refer std.text.index.stemmer/excluded-word? :added "3.0"}
(fact "checks if word is excluded from indexing"

  (excluded-word? "and")
  => true)

^{:refer std.text.index.stemmer/remove-excluded-words :added "3.0"}
(fact "remove excluded words"

  (remove-excluded-words ["the" "lazy" "fox"])
  => ["lazy" "fox"])

^{:refer std.text.index.stemmer/expand-hyphenated-words :added "3.0"}
(fact "split hyphenated words"

  (expand-hyphenated-words ["hello-world"])
  => ["hello-world" "hello" "world"])

^{:refer std.text.index.stemmer/tokenise :added "3.0"}
(fact "makes tokens from text"

  (tokenise "The lazy brown fox jumped over")
  => ["lazy" "brown" "fox" "jumped" "over"])

^{:refer std.text.index.stemmer/stems :added "3.0"}
(fact "classifies text into word stems"

  (stems "The lazy brown fox jumped over")
  => ["lazi" "brown" "fox" "jump" "over"])
