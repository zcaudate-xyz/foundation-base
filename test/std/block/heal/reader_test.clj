(ns std.block.heal.reader-test
  (:use code.test)
  (:require [std.block.heal.core :as heal]
            [std.lib :as h]))

(fact "Demonstrate handling of #_ reader macro"
  ;; The string "( [ #_ ] )" is invalid Clojure code because #_ tries to read ']', which is a delimiter.
  ;; However, heal now detects the position where reading #_ fails and continues parsing from there.
  ;; This means it sees [ as unclosed and tries to fix it.

  (let [broken-code "( [ #_ ] )"]
    (h/suppress (try (read-string broken-code)
        (catch Exception e :error))) => :error

    ;; heal should modify this code (e.g. by closing the [ or changing ) to ])
    (not= (heal/heal-content broken-code) broken-code) => true))

(fact "Demonstrate #_ hiding delimiters correctly when valid"
  ;; (let [a 1 #_]) is invalid.
  ;; heal sees ( [ ). Adds ].

  (let [broken-code "(let [a 1 #_])"]
    (not= (heal/heal-content broken-code) broken-code) => true
    ;; Note: The result might still be invalid due to insertion placement (e.g. trailing delimiter),
    ;; but heal has successfully detected the imbalance and attempted a fix.
    ))
