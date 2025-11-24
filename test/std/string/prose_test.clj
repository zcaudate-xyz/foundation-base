(ns std.string.prose-test
  (:use code.test)
  (:require [std.string.prose :refer :all]
            [std.string.common :as common]))

^{:refer std.string.prose/has-quotes? :added "3.0"}
(fact "checks if a string has quotes"

  (has-quotes? "\"hello\"")
  => true)

^{:refer std.string.prose/strip-quotes :added "3.0"}
(fact "gets rid of quotes in a string"

  (strip-quotes "\"hello\"")
  => "hello")

^{:refer std.string.prose/whitespace? :added "3.0"}
(fact "checks if the string is all whitespace"

  (whitespace? "        ")
  => true)

^{:refer std.string.prose/escape-dollars :added "3.0"}
(fact "for regex purposes, escape dollar signs in strings"

  (escape-dollars "$")
  => string?)

^{:refer std.string.prose/escape-newlines :added "3.0"}
(fact "makes sure that newlines are printable"

  (escape-newlines "\\n")
  => "\\n")

^{:refer std.string.prose/escape-escapes :added "3.0"}
(fact "makes sure that newlines are printable"

  (escape-escapes "\\n")
  => "\\\\n")

^{:refer std.string.prose/escape-quotes :added "3.0"}
(fact "makes sure that quotes are printable in string form"

  (escape-quotes "\"hello\"")
  => "\\\"hello\\\"")

^{:refer std.string.prose/filter-empty-lines :added "3.0"}
(fact "filter empty line"

  (filter-empty-lines (common/join "\n" ["a" "  " "   " "b"]))
  => "a\nb")

^{:refer std.string.prose/single-line :added "3.0"}
(fact "replace newlines with spaces")

^{:refer std.string.prose/join-lines :added "4.0"}
(fact "join non empty elements in array"

  (join-lines ["hello" "world"])
  => "hello\nworld")

^{:refer std.string.prose/spaces :added "4.0"}
(fact "create `n` spaces"

  (spaces 4)
  => "    ")

^{:refer std.string.prose/write-line :added "4.0"}
(fact "writes a line based on data structure"
  ^:hidden
  
  (write-line [:a :b ["hello" "world"]])
  => "a b hello world")

^{:refer std.string.prose/write-lines :added "4.0"}
(fact "writes a block of string"
  ^:hidden
  
  (write-lines ["a" "b" "c"])
  => "a\nb\nc")

^{:refer std.string.prose/indent :added "4.0"}
(fact "indents a block of string"
  
  (indent (write-lines ["a" "b" "c"]) 2)
  => "  a\n  b\n  c")

^{:refer std.string.prose/indent-rest :added "4.0"}
(fact "indents the rest of the boiy"

  (indent-rest (write-lines ["a" "b" "c"]) 2)
  => "a\n  b\n  c")

^{:refer std.string.prose/multi-line? :added "4.0"}
(fact "check that a string has newlines")

^{:refer std.string.prose/single-line? :added "4.0"}
(fact "check that a string does not have newlines")

^{:refer std.string.prose/layout-lines :added "4.0"}
(fact "layout tokens in lines depending on max length"

  (layout-lines ["hello" "world" "again" "a" "b"]
                8)
  => "hello\nworld\nagain a\nb")
