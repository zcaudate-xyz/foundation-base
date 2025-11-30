(ns std.block.heal.edit-test
  (:use code.test)
  (:require [std.block.heal.edit :as core]
            [std.block.heal.parse :as parse]
            [std.block.heal.print :as print]
            [std.lib :as h]))

^{:refer std.block.heal.edit/update-content :added "4.0"}
(fact "performs the necessary edits to a string"
  ^:hidden

  (core/update-content
   "("
   '({:action :insert, :line 1, :col 1, :new-char ")"}))
  => "()"

  
  (core/update-content
   "
(defn my-func [x]
  (if (> x 0)
    (println :positive)
    (println :done})"
   '({:action :replace, :line 5, :col 19, :new-char ")"}))
  =>
  "
(defn my-func [x]
  (if (> x 0)
    (println :positive)
    (println :done))")

^{:refer std.block.heal.edit/create-mismatch-edits :added "4.0"}
(fact "find the actions required to edit the content"
  ^:hidden
  
  (core/create-mismatch-edits
   (parse/pair-delimiters
    (parse/parse-delimiters
     "
(defn my-func [x]
  (if (> x 0)
    (println :positive)
    (println :done})")))
  => '({:action :replace, :line 5, :col 19, :new-char ")"}))

^{:refer std.block.heal.edit/check-append-fn :added "4.0"}
(fact "check for open unpaired"
  (core/check-append-fn {:type :open :pair-id nil}) => true
  (core/check-append-fn {:type :open :pair-id 1}) => false
  (core/check-append-fn {:type :close :pair-id nil}) => false)

^{:refer std.block.heal.edit/check-append-edits :added "4.0"}
(fact "checks that append edits are value"
  (core/check-append-edits
    [{:type :open :pair-id nil}
     {:type :open :pair-id nil}])
  => true

  (core/check-append-edits
    [{:type :open :pair-id nil}
     {:type :close :pair-id 1}
     {:type :open :pair-id nil}])
  => false)

^{:refer std.block.heal.edit/create-append-edits :added "4.0"}
(fact "creates the append edits"
  ^:hidden
  
  (core/create-append-edits
   (parse/parse-delimiters" ( "))
  => [{:action :insert, :line 1, :col 2, :new-char ")"}])

^{:refer std.block.heal.edit/check-remove-fn :added "4.0"}
(fact "check for close unpaired"
  (core/check-remove-fn {:type :close :pair-id nil}) => true
  (core/check-remove-fn {:type :open :pair-id nil}) => false)

^{:refer std.block.heal.edit/create-remove-edits :added "4.0"}
(fact "creates removes edits"
  (core/create-remove-edits
   (parse/parse-delimiters ")"))
  => [{:action :remove, :line 1, :col 1}])
