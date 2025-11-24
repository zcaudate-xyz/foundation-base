(ns code.ai.base-summarise-test
  (:use code.test)
  (:require [code.ai.base-summarise :refer :all]))

^{:refer code.ai.base-summarise/create-tokenizer :added "4.0"}
(fact "creates an OpenNLP tokenizer")

^{:refer code.ai.base-summarise/create-pos-tagger :added "4.0"}
(fact "creates an OpenNLP POS tagger")

^{:refer code.ai.base-summarise/classify-text :added "4.0"}
(fact "classifies text")

^{:refer code.ai.base-summarise/find-best-phrase :added "4.0"}
(fact "Finds the most frequent keyphrase between min-words and max-words.")

^{:refer code.ai.base-summarise/-main :added "4.0"}
(fact "runs the example")