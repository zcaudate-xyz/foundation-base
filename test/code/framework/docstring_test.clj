(ns code.framework.docstring-test
  (:use code.test)
  (:require [code.framework.docstring :refer :all]
            [std.block :as block]
            [code.edit :as nav]
            [std.lib.zip :as zip]))

^{:refer code.framework.docstring/strip-quotes :added "3.0"}
(fact "utility that strips quotes when not the result of a fact"

  (strip-quotes ["\"hello\""])
  => ["hello"]

  (strip-quotes ["(str \"hello\")" " " "=>" " " "\"hello\""])
  => ["(str \"hello\")" " " "=>" " " "\"hello\""])

^{:refer code.framework.docstring/->refstring :added "3.0"}
(fact "creates a refstring for use in html blocks"

  (->> (nav/parse-root "\"hello\"\n  (+ 1 2)\n => 3")
       (iterate zip/step-right)
       (take-while zip/get)
       (map zip/get)
       (->refstring))
  => "\"hello\"\n  (+ 1 2)\n => 3")

^{:refer code.framework.docstring/->docstring-tag :added "3.0"}
(fact "converts a string representation of block"

  (->docstring-tag (block/block \c))
  => "'c'")

^{:refer code.framework.docstring/->docstring :added "3.0"}
(fact "converts nodes to a docstring"

  (->> (nav/parse-root "\n  (+ 1 2)\n => 3")
       (nav/down)
       (iterate zip/step-right)
       (take-while zip/get)
       (map zip/get)
       (->docstring))
  => "\n  (+ 1 2)\n => 3"

  (->docstring [(block/block \e)])
  => "'e'")

^{:refer code.framework.docstring/append-node :added "3.0"}
(fact "Adds node as well as whitespace and newline on right"

  (-> (nav/parse-string "(+)")
      (zip/step-inside)
      (append-node 2)
      (append-node 1)
      (nav/root-string))
  => "(+\n  1\n  2)")

^{:refer code.framework.docstring/->docstring-node :added "3.0"}
(fact "converts nodes to a docstring node"

  (->> (nav/navigator [\e \d \newline])
       (zip/step-inside)
       (iterate zip/step-right)
       (take-while zip/get)
       (map zip/get)
       (->docstring-node "")
       (block/value))
  => "'e' 'd' '\n '")

^{:refer code.framework.docstring/insert-docstring :added "3.0"}
(fact "inserts the meta information and docstring from tests"
  (insert-docstring (nav/parse-string "(defn foo [])")
                    'ns
                    {'ns {'foo {:intro "intro" :test {:code "code"}}}})
  => nav/navigator?)

(comment
  (code.manage/analyse))
