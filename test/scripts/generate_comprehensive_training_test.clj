(ns scripts.generate-comprehensive-training-test
  (:use code.test)
  (:require [scripts.generate-comprehensive-training :refer :all]))

^{:refer scripts.generate-comprehensive-training/generate-1000-comprehensive-pairs :added "4.1"}
(fact "TODO")

^{:refer scripts.generate-comprehensive-training/pair->jsonl :added "4.1"}
(fact "TODO")

^{:refer scripts.generate-comprehensive-training/pairs->jsonl :added "4.1"}
(fact "TODO")

^{:refer scripts.generate-comprehensive-training/format-pair-console :added "4.1"}
(fact "TODO")

^{:refer scripts.generate-comprehensive-training/-main :added "4.1"}
(fact "TODO")