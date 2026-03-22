(ns std.block.reader-test
  (:require [std.block.check :as check]
            [std.block.reader :as reader :refer :all])
  (:use code.test)
  (:refer-clojure :exclude [slurp]))

^{:refer std.block.reader/create :added "3.0"}
(fact "create reader from string"

  (type (create "hello world"))
  => clojure.tools.reader.reader_types.IndexingPushbackReader)

^{:refer std.block.reader/reader-position :added "3.0"}
(fact "returns the position of the reader"

  (-> (create "abc")
      step-char
      step-char
      reader-position)
  => [1 3])

^{:refer std.block.reader/throw-reader :added "3.0"}
(fact "throws a reader message"

  (throw-reader (create "abc")
                "Message"
                {:data true})
  => (throws))

^{:refer std.block.reader/step-char :added "3.0"}
(fact "moves reader one char forward"

  (-> (create "abc")
      step-char
      read-char
      str)
  => "b")

^{:refer std.block.reader/read-char :added "3.0"}
(fact "reads single char and move forward"

  (->> read-char
       (read-repeatedly (create "abc"))
       (take 3)
       (apply str))
  => "abc")

^{:refer std.block.reader/ignore-char :added "3.0"}
(fact "returns nil and moves reader one char forward"

  (->> ignore-char
       (read-repeatedly (create "abc"))
       (take 3)
       (apply str))
  => "")

^{:refer std.block.reader/unread-char :added "3.0"}
(fact "move reader one char back, along with char"

  (-> (create "abc")
      (step-char)
      (unread-char \A)
      (reader/slurp))
  => "Abc")

^{:refer std.block.reader/peek-char :added "3.0"}
(fact "returns the current reader char with moving"

  (->> (read-times (create "abc")
                   peek-char
                   3)
       (apply str))
  => "aaa")

^{:refer std.block.reader/read-while :added "3.0"}
(fact "reads input while the predicate is true"

  (read-while (create "abcde")
              (fn [ch]
                (not= (str ch) "d")))
  => "abc")

^{:refer std.block.reader/read-until :added "3.0"}
(fact "reads inputs until the predicate is reached"

  (read-until (create "abcde")
              (fn [ch]
                (= (str ch) "d")))
  => "abc")

^{:refer std.block.reader/read-times :added "3.0"}
(fact "reads input repeatedly"

  (->> (read-times (create "abcdefg")
                   #(str (read-char %) (read-char %))
                   2))
  => ["ab" "cd"])

^{:refer std.block.reader/read-repeatedly :added "3.0"}
(fact "reads input repeatedly"

  (->> (read-repeatedly (create "abcdefg")
                        #(str (read-char %) (read-char %))
                        empty?)
       (take 5))
  => ["ab" "cd" "ef" "g"])

^{:refer std.block.reader/read-include :added "3.0"}
(fact "reads up to a given predicate"

  (read-include (create "  a")
                read-char (complement check/voidspace?))
  => [[\space \space] \a])

^{:refer std.block.reader/slurp :added "3.0"}
(fact "reads rest of input from reader"

  (reader/slurp (reader/step-char (create "abc efg")))
  => "bc efg")

^{:refer std.block.reader/read-to-boundary :added "3.0"}
(fact "reads to an input boundary"

  (read-to-boundary (create "abc efg"))
  => "abc")
