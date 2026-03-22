(ns std.math.markov-test
  (:require [std.math.markov :refer :all])
  (:use code.test))

^{:refer std.math.markov/cumulative :added "3.0"}
(fact "Reassemble the map of probabilities in cumulative order")

^{:refer std.math.markov/select :added "3.0"}
(fact "Given a map of probabilities, select one at random.")

^{:refer std.math.markov/generate :added "3.0"}
(fact "generate takes a probability matrix, and produces an infinite stream of 
 tokens, taking into consideration any remembered state"

  (->> (collate (seq "AEAEAAAAAEAAAAAAEEAEEAEAEEEAAAEAAAA") 1)
       (generate)
       (take 10)
       (apply str))
  ;; "EAAAEAAAAE"
  => string?)

^{:refer std.math.markov/tally :added "3.0"}
(fact "helper function for collate")

^{:refer std.math.markov/collate :added "3.0"}
(fact "generates an output probability map given a sequence"

  (collate (seq "AEAEAAAAAEAAAAAAEEAEEAEAEEEAAAEAAAA") 3)
  => '{(\A \E \A) {\E 2, \A 3},
       (\E \A \E) {\A 2, \E 2},
       (\E \A \A) {\A 4},
       (\A \A \A) {\A 6, \E 3},
       (\A \A \E) {\A 2, \E 1},
       (\A \E \E) {\A 2, \E 1},
       (\E \E \A) {\E 2, \A 1},
       (\E \E \E) {\A 1}})
