(ns
 xtbench.python.lang.common-string-test
 (:use code.test)
 (:require [std.lang :as l] [std.string.prose :as prose]))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.lang.common-string :as xts] [xt.lang.common-spec :as xt]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-string/get-char, :added "4.1"}
(fact
 "gets a character code from string"
 (!.py (xts/get-char "abc" (xt/x:offset 0)))
 =>
 97)

^{:refer xt.lang.common-string/split, :added "4.1"}
(fact
 "splits a string using a token"
 (!.py (xts/split "hello/world" "/"))
 =>
 ["hello" "world"])

^{:refer xt.lang.common-string/join, :added "4.1"}
(fact
 "joins an array using a separator"
 (!.py (xts/join "/" ["hello" "world"]))
 =>
 "hello/world")

^{:refer xt.lang.common-string/replace, :added "4.1"}
(fact
 "replaces a string token with another"
 (!.py (xts/replace "hello/world" "/" "_"))
 =>
 "hello_world")

^{:refer xt.lang.common-string/index-of, :added "4.1"}
(fact
 "returns index of character in string"
 (!.py (xts/index-of "hello/world" "/"))
 =>
 5)

^{:refer xt.lang.common-string/substring, :added "4.1"}
(fact
 "gets the substring"
 (!.py
  [(xts/substring "hello/world" 3) (xts/substring "hello/world" 3 8)])
 =>
 ["lo/world" "lo/wo"])

^{:refer xt.lang.common-string/to-uppercase, :added "4.1"}
(fact
 "converts string to uppercase"
 (!.py (xts/to-uppercase "hello"))
 =>
 "HELLO")

^{:refer xt.lang.common-string/to-lowercase, :added "4.1"}
(fact
 "converts string to lowercase"
 (!.py (xts/to-lowercase "HELLO"))
 =>
 "hello")

^{:refer xt.lang.common-string/to-fixed, :added "4.1"}
(fact "formats decimal places" (!.py (xts/to-fixed 1.2 3)) => "1.200")

^{:refer xt.lang.common-string/trim, :added "4.1"}
(fact "trims a string" (!.py (xts/trim " \n  hello \n  ")) => "hello")

^{:refer xt.lang.common-string/trim-left, :added "4.1"}
(fact
 "trims a string on left"
 (!.py (xts/trim-left " \n  hello \n  "))
 =>
 "hello \n  ")

^{:refer xt.lang.common-string/trim-right, :added "4.1"}
(fact
 "trims a string on right"
 (!.py (xts/trim-right " \n  hello \n  "))
 =>
 " \n  hello")

^{:refer xt.lang.common-string/sym-full, :added "4.1"}
(fact
 "creates a symbol path"
 (!.py (xts/sym-full "hello" "world"))
 =>
 "hello/world"
 (!.py (xts/sym-full nil "world"))
 =>
 "world")

^{:refer xt.lang.common-string/sym-name, :added "4.1"}
(fact
 "gets the name part of the symbol"
 (!.py (xts/sym-name "hello/world"))
 =>
 "world")

^{:refer xt.lang.common-string/sym-ns, :added "4.1"}
(fact
 "gets the namespace part of the symbol"
 (!.py (xts/sym-ns "hello/world"))
 =>
 "hello"
 (!.py (xt/x:nil? (xts/sym-ns "hello")))
 =>
 true)

^{:refer xt.lang.common-string/sym-pair, :added "4.1"}
(fact
 "gets the namespace and name pair"
 (!.py (xts/sym-pair "hello/world"))
 =>
 ["hello" "world"])

^{:refer xt.lang.common-string/starts-with?, :added "4.1"}
(fact
 "checks for starts with"
 (!.py (xts/starts-with? "Foo Bar" "Foo"))
 =>
 true)

^{:refer xt.lang.common-string/ends-with?, :added "4.1"}
(fact
 "checks for ends with"
 (!.py (xts/ends-with? "Foo Bar" "Bar"))
 =>
 true)

^{:refer xt.lang.common-string/capitalize, :added "4.1"}
(fact
 "uppercases the first letter"
 (!.py (xts/capitalize "hello"))
 =>
 "Hello")

^{:refer xt.lang.common-string/decapitalize, :added "4.1"}
(fact
 "lowercases the first letter"
 (!.py (xts/decapitalize "HELLO"))
 =>
 "hELLO")

^{:refer xt.lang.common-string/pad-left, :added "4.1"}
(fact
 "pads string with n chars on left"
 (!.py (xts/pad-left "000" 5 "-"))
 =>
 "--000")

^{:refer xt.lang.common-string/pad-right, :added "4.1"}
(fact
 "pads string with n chars on right"
 (!.py (xts/pad-right "000" 5 "-"))
 =>
 "000--")

^{:refer xt.lang.common-string/pad-lines, :added "4.1"}
(fact
 "pads lines with leading spaces"
 (!.py (xts/pad-lines (xts/join "\n" ["hello" "world"]) 2 " "))
 =>
 (prose/| "  hello" "  world"))

^{:refer xt.lang.common-string/split-long,
  :added "4.1",
  :setup
  [(def +s+ (apply str (repeat 5 "1234567890")))
   (def
    +out+
    ["1234567890"
     "1234567890"
     "1234567890"
     "1234567890"
     "1234567890"])]}
(fact
 "splits a long string"
 (!.py (xts/split-long nil 10))
 =>
 (any [] {})
 (!.py (xts/split-long "" 10))
 =>
 (any [] {})
 (!.py (xts/split-long (@! +s+) 10))
 =>
 +out+)

^{:refer xt.lang.common-string/str-rand, :added "4.1"}
(fact
 "creates a random alpha-numeric string"
 (!.py
  [(xt/x:is-string? (xts/str-rand 8)) (xt/x:str-len (xts/str-rand 8))])
 =>
 [true 8])

^{:refer xt.lang.common-string/tag-string, :added "4.1"}
(fact
 "gets the string description for a given tag"
 (!.py (xts/tag-string "user.account/login"))
 =>
 "account login")
