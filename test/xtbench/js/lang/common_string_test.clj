(ns xtbench.js.lang.common-string-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.string.prose :as prose]))

(l/script- :js
 {:runtime :basic,
  :require [[xt.lang.common-string :as xts]
            [xt.lang.common-spec :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-string/get-char :added "4.1"}
(fact "gets a character code from string"
  
  (!.js (xts/get-char "abc" (xt/x:offset 0)))
  => 97)

^{:refer xt.lang.common-string/split :added "4.1"}
(fact "splits a string using a token"
  
  (!.js (xts/split "hello/world" "/"))
  => ["hello" "world"])

^{:refer xt.lang.common-string/join :added "4.1"}
(fact "joins an array using a separator"
  
  (!.js (xts/join "/" ["hello" "world"]))
  => "hello/world")

^{:refer xt.lang.common-string/replace :added "4.1"}
(fact "replaces a string token with another"
  
  (!.js (xts/replace "hello/world" "/" "_"))
  => "hello_world")

^{:refer xt.lang.common-string/index-of :added "4.1"}
(fact "returns index of character in string"
  
  (!.js (xts/index-of "hello/world" "/"))
  => 5)

^{:refer xt.lang.common-string/substring :added "4.1"}
(fact "gets the substring"
  
  (!.js
    [(xts/substring "hello/world" 3)
     (xts/substring "hello/world" 3 8)])
  => ["lo/world" "lo/wo"])

^{:refer xt.lang.common-string/to-uppercase :added "4.1"}
(fact "converts string to uppercase"
  
  (!.js (xts/to-uppercase "hello"))
  => "HELLO")

^{:refer xt.lang.common-string/to-lowercase :added "4.1"}
(fact "converts string to lowercase"
  
  (!.js (xts/to-lowercase "HELLO"))
  => "hello")

^{:refer xt.lang.common-string/to-fixed :added "4.1"}
(fact "formats decimal places"
  
  (!.js (xts/to-fixed 1.2 3))
  => "1.200")

^{:refer xt.lang.common-string/trim :added "4.1"}
(fact "trims a string"
  
  (!.js (xts/trim " \n  hello \n  "))
  => "hello")

^{:refer xt.lang.common-string/trim-left :added "4.1"}
(fact "trims a string on left"
  
  (!.js (xts/trim-left " \n  hello \n  "))
  => "hello \n  ")

^{:refer xt.lang.common-string/trim-right :added "4.1"}
(fact "trims a string on right"
  
  (!.js (xts/trim-right " \n  hello \n  "))
  => " \n  hello")

^{:refer xt.lang.common-string/sym-full :added "4.1"}
(fact "creates a symbol path"
  
  (!.js (xts/sym-full "hello" "world"))
  => "hello/world"

  (!.js (xts/sym-full nil "world"))
  => "world")

^{:refer xt.lang.common-string/sym-name :added "4.1"}
(fact "gets the name part of the symbol"
  
  (!.js (xts/sym-name "hello/world"))
  => "world")

^{:refer xt.lang.common-string/sym-ns :added "4.1"}
(fact "gets the namespace part of the symbol"
  
  (!.js (xts/sym-ns "hello/world"))
  => "hello"

  (!.js (xt/x:nil? (xts/sym-ns "hello")))
  => true)

^{:refer xt.lang.common-string/sym-pair :added "4.1"}
(fact "gets the namespace and name pair"
  
  (!.js (xts/sym-pair "hello/world"))
  => ["hello" "world"])

^{:refer xt.lang.common-string/starts-with? :added "4.1"}
(fact "checks for starts with"
  
  (!.js (xts/starts-with? "Foo Bar" "Foo"))
  => true)

^{:refer xt.lang.common-string/ends-with? :added "4.1"}
(fact "checks for ends with"
  
  (!.js (xts/ends-with? "Foo Bar" "Bar"))
  => true)

^{:refer xt.lang.common-string/capitalize :added "4.1"}
(fact "uppercases the first letter"
  
  (!.js (xts/capitalize "hello"))
  => "Hello")

^{:refer xt.lang.common-string/decapitalize :added "4.1"}
(fact "lowercases the first letter"
  
  (!.js (xts/decapitalize "HELLO"))
  => "hELLO")

^{:refer xt.lang.common-string/pad-left :added "4.1"}
(fact "pads string with n chars on left"
  
  (!.js (xts/pad-left "000" 5 "-"))
  => "--000")

^{:refer xt.lang.common-string/pad-right :added "4.1"}
(fact "pads string with n chars on right"
  
  (!.js (xts/pad-right "000" 5 "-"))
  => "000--")

^{:refer xt.lang.common-string/pad-lines :added "4.1"}
(fact "pads lines with leading spaces"
  
  (!.js (xts/pad-lines (xts/join "\n" ["hello" "world"]) 2 " "))
  => (prose/| "  hello" "  world"))

^{:refer xt.lang.common-string/split-long
  :added "4.1"
  :setup [(def +s+ (apply str (repeat 5 "1234567890")))
          (def +out+ ["1234567890" "1234567890" "1234567890" "1234567890" "1234567890"])]}
(fact "splits a long string"
  
  (!.js (xts/split-long nil 10))
  => (any [] {})

  (!.js (xts/split-long "" 10))
  => (any [] {})

  (!.js (xts/split-long (@! +s+) 10))
  => +out+)

^{:refer xt.lang.common-string/str-rand :added "4.1"}
(fact "creates a random alpha-numeric string"
  
  (!.js
   [(xt/x:is-string? (xts/str-rand 8))
    (xt/x:str-len (xts/str-rand 8))])
  => [true 8])

^{:refer xt.lang.common-string/tag-string :added "4.1"}
(fact "gets the string description for a given tag"
  
  (!.js (xts/tag-string "user.account/login"))
  => "account login")
