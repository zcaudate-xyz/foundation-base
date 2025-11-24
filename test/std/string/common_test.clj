(ns std.string.common-test
  (:use code.test)
  (:require [std.string.common :refer :all]
            [std.string.wrap :refer [wrap]]
            [std.lib :as h])
  (:refer-clojure :exclude [reverse replace]))

^{:refer std.string.common/blank? :added "3.0"}
(fact "checks if string is empty or nil"

  (blank? nil)
  => true

  (blank? "")
  => true)

^{:refer std.string.common/split :added "3.0"}
(fact "splits the string into tokens"

  (split "a b c" #" ")
  => ["a" "b" "c"]

  ((wrap split) :a.b.c (re-pattern "\\."))
  => [:a :b :c])

^{:refer std.string.common/split-lines :added "3.0"}
(fact "splits the string into separate lines"

  (split-lines "a\nb\nc")
  => ["a" "b" "c"])

^{:refer std.string.common/joinl :added "3.0"}
(fact "joins an array using a separator"

  (joinl ["a" "b" "c"] ".")
  => "a.b.c"

  (joinl ["a" "b" "c"])
  => "abc"

  ((wrap joinl) [:a :b :c] "-")
  => :a-b-c)

^{:refer std.string.common/join :added "3.0"}
(fact "like `join` but used with `->>` opts"

  (join "." ["a" "b" "c"])
  => "a.b.c"

  ;;(join "." '[a b c])
  ;;=> 'a.b.c
  )

^{:refer std.string.common/upper-case :added "3.0"}
(fact "converts a string object to upper case"

  (upper-case "hello-world")
  => "HELLO-WORLD"

  ((wrap upper-case) :hello-world)
  => :HELLO-WORLD)

^{:refer std.string.common/lower-case :added "3.0"}
(fact "converts a string object to lower case"

  (lower-case "Hello.World")
  => "hello.world"

  ((wrap lower-case) 'Hello.World)
  => 'hello.world)

^{:refer std.string.common/capital-case :added "3.0"}
(fact "converts a string object to capital case"

  (capital-case "hello.World")
  => "Hello.world"

  ((wrap capital-case) 'hello.World)
  => 'Hello.world)

^{:refer std.string.common/reverse :added "3.0"}
(fact "reverses the string"

  (reverse "hello")
  => "olleh"

  ((wrap reverse) :hello)
  => :olleh)

^{:refer std.string.common/starts-with? :added "3.0"}
(fact "checks if string starts with another"

  (starts-with? "hello" "hel")
  => true

  ((wrap starts-with?) 'hello 'hel)
  => true)

^{:refer std.string.common/ends-with? :added "3.0"}
(fact "checks if string ends with another"

  (ends-with? "hello" "lo")
  => true

  ((wrap ends-with?) 'hello 'lo)
  => true)

^{:refer std.string.common/includes? :added "3.0"}
(fact "checks if first string contains the second"

  (includes? "hello" "ell")
  => true

  ((wrap includes?) 'hello 'ell)
  => true)

^{:refer std.string.common/trim :added "3.0"}
(fact "trims the string of whitespace"

  (trim "   hello   ")
  => "hello")

^{:refer std.string.common/trim-left :added "3.0"}
(fact "trims the string of whitespace on left"

  (trim-left "   hello   ")
  => "hello   ")

^{:refer std.string.common/trim-right :added "3.0"}
(fact "trims the string of whitespace on right"

  (trim-right "   hello   ")
  => "   hello")

^{:refer std.string.common/trim-newlines :added "3.0"}
(fact "removes newlines from right"

  (trim-newlines  "\n\n    hello   \n\n")
  => "\n\n    hello   ")

^{:refer std.string.common/escape :added "3.0"}
(fact "uses a map to replace chars" ^:hidden

  (escape "1 < 2 as HTML, & s."
          {\< "&lt;", \> "&gt;", \& "&amp;"})
  => "1 &lt; 2 as HTML, &amp; s.")

^{:refer std.string.common/replace :added "3.0"}
(fact "replace value in string with another"

  (replace "hello" "el" "AL")
  => "hALlo"

  ((wrap replace) :hello "el" "AL")
  => :hALlo)

^{:refer std.string.common/caseless= :added "3.0"}
(fact "compares two values ignoring case"

  (caseless= "heLLo" "HellO")
  => true

  ((wrap caseless=) 'heLLo :HellO)
  => true)

^{:refer std.string.common/truncate :added "3.0"}
(fact "truncates a word"

  (truncate "hello there" 5)
  => "hello")

^{:refer std.string.common/capitalize :added "4.0"}
(fact "capitalize the first letter"

  (capitalize "hello")
  => "Hello")

^{:refer std.string.common/decapitalize :added "4.0"}
(fact "lowercase the first letter"

  (decapitalize "HELLO")
  => "hELLO")

^{:refer std.string.common/replace-all :added "4.0"}
(fact "shortcut for ``.replaceAll``"

  (replace-all "hello" "l" "o")
  => "heooo")


^{:refer std.string.common/replace-at :added "4.0"}
(fact "TODO")

^{:refer std.string.common/insert-at :added "4.0"}
(fact "TODO")