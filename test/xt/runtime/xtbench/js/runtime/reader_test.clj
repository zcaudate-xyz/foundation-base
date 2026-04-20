(ns
 xtbench.js.runtime.reader-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :js
 {:runtime :basic,
  :require
  [[xt.lang.common-spec :as xt]
   [xt.runtime.reader :as rdr]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.runtime.reader/reader?, :added "4.1"}
(fact
 "creates runtime reader values"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "abc"))
  [(rdr/reader? reader)
   (. reader ["::"])
   (rdr/reader-position reader)])
 =>
 [true "reader" [1 1]])

^{:refer xt.runtime.reader/create, :added "4.1"}
(fact
 "creates readers with empty buffers and zero offset"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "abc"))
  [(. reader idx)
   (. reader line)
   (. reader col)
   (== 0 (xt/x:len (. reader pushback)))
   (== 0 (xt/x:len (. reader history)))])
 =>
 [0 1 1 true true])

^{:refer xt.runtime.reader/impl-char-at, :added "4.1"}
(fact
 "extracts one-character slices from strings"
 ^{:hidden true}
 (!.js
  [(rdr/impl-char-at "abc" 0)
   (rdr/impl-char-at "abc" 1)
   (== nil (rdr/impl-char-at "abc" 3))])
 =>
 ["a" "b" true])

^{:refer xt.runtime.reader/reader-position, :added "4.1"}
(fact
 "tracks line and column positions"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "ab"))
  (rdr/read-char reader)
  (rdr/reader-position reader))
 =>
 [1 2])

^{:refer xt.runtime.reader/read-char, :added "4.1"}
(fact
 "reads chars sequentially"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "ab"))
  [(rdr/read-char reader)
   (rdr/read-char reader)
   (== nil (rdr/read-char reader))])
 =>
 ["a" "b" true])

^{:refer xt.runtime.reader/peek-char, :added "4.1"}
(fact
 "peeks and reads chars without losing position"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "abc"))
  [(rdr/peek-char reader)
   (rdr/read-char reader)
   (rdr/peek-char reader)
   (rdr/read-char reader)
   (rdr/read-char reader)
   (== nil (rdr/read-char reader))
   (rdr/reader-position reader)])
 =>
 ["a" "a" "b" "b" "c" true [1 4]])

^{:refer xt.runtime.reader/unread-char, :added "4.1"}
(fact
 "unreads chars and restores the exact position"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "a\nb"))
  (var a (rdr/read-char reader))
  (var nl (rdr/read-char reader))
  (var after-newline (rdr/reader-position reader))
  (rdr/unread-char reader nl)
  [(rdr/reader-position reader)
   after-newline
   (rdr/read-char reader)
   (rdr/reader-position reader)
   (rdr/read-char reader)])
 =>
 [[1 2] [2 1] "\n" [2 1] "b"])

(fact
 "throws when unreading before any char has been consumed"
 ^{:hidden true}
 (!.js (rdr/unread-char (rdr/create "a") "a"))
 =>
 (throws))

^{:refer xt.runtime.reader/throw-reader, :added "4.1"}
(fact
 "throws reader errors with position context"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "ab"))
  (rdr/read-char reader)
  (rdr/throw-reader reader "boom"))
 =>
 (throws))

^{:refer xt.runtime.reader/read-while, :added "4.1"}
(fact
 "scans while predicates match"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "abc 123"))
  [(rdr/read-while
    reader
    (fn:> [ch] (and (not= ch nil) (not= ch " ")))
    true)
   (rdr/peek-char reader)])
 =>
 ["abc" " "])

^{:refer xt.runtime.reader/read-until, :added "4.1"}
(fact
 "scans until predicates match"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "abc 123"))
  [(rdr/read-until reader (fn:> [ch] (== ch "1")) true)
   (rdr/peek-char reader)])
 =>
 ["abc " "1"])

(fact
 "reports unexpected eof when requested"
 ^{:hidden true}
 (!.js
  (rdr/read-while (rdr/create "abc") (fn:> [ch] (not= ch nil)) false))
 =>
 (throws))

^{:refer xt.runtime.reader/ignore-char, :added "4.1"}
(fact
 "ignores chars while advancing the reader"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "ab"))
  [(== nil (rdr/ignore-char reader)) (rdr/read-char reader)])
 =>
 [true "b"])

^{:refer xt.runtime.reader/slurp, :added "4.1"}
(fact
 "slurps the remaining input"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "abcd"))
  (rdr/read-char reader)
  (rdr/slurp reader))
 =>
 "bcd")

^{:refer xt.runtime.reader/step-char, :added "4.1"}
(fact
 "supports stepping ignoring and slurping the remaining input"
 ^{:hidden true}
 (!.js
  (var reader (rdr/create "abcd"))
  (rdr/step-char reader)
  (rdr/ignore-char reader)
  [(rdr/reader-position reader)
   (rdr/slurp reader)
   (== nil (rdr/peek-char reader))])
 =>
 [[1 3] "cd" true])
