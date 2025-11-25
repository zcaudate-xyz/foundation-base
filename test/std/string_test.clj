(ns std.string-test
  (:use code.test)
  (:require [std.string :refer :all]
            [std.lib :as h])
  (:refer-clojure :exclude [reverse replace]))

^{:refer std.string/split :added "3.0" :adopt true}
(fact "splits a string given a regex"

  ((wrap split) "a b" #" ") => ["a" "b"]
  ((wrap split) " " #" ") => ["" ""])

^{:refer std.string/split-lines :added "3.0" :adopt true}
(fact "splits a string given newlines"

  ((wrap split-lines) "a\nb") => ["a" "b"]

  ((wrap split-lines) "\n") => ["" ""])

^{:refer clojure.core/= :added "3.0" :adopt true}
(fact "compares two string-like things"

  ((wrap =) :a 'a)
  => true

  ((wrap =) *ns* :std.string-test)
  => true)

^{:refer clojure.core/subs :added "3.0" :adopt true}
(fact "compares two string-like things"

  ((wrap subs) :hello-world  3 8)
  => :lo-wo

  ((wrap format) :hello%d-world  100)
  => :hello100-world)

^{:refer std.string/| :added "3.0"}
(fact "shortcut for join lines"

  (| "abc" "def")
  => "abc\ndef")

^{:refer std.string/lines :added "3.0"}
(fact "transforms string to seperated newlines"

  (lines "abc\ndef")
  => '(std.string/| "abc" "def"))

(fact "replace-at"
  (replace-at "abcde" 2 "X")
  => "abXde")

(fact "insert-at"
  (insert-at "abcde" 2 "X")
  => "abXcde")

(fact "single-line"
  (std.string.prose/single-line "a\nb")
  => "a b")

(fact "multi-line?"
  (std.string.prose/multi-line? "a\nb")
  => true)

(fact "single-line?"
  (std.string.prose/single-line? "a")
  => true)

(fact "join"
  (std.string.wrap/join "|" ["a" "b"])
  => "a|b")
