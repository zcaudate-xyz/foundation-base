(ns std.string.plural-test
  (:require [std.string.plural :refer :all])
  (:use code.test))

^{:refer std.string.plural/uncountable? :added "3.0"}
(fact "checks if a word is uncountable"

  (uncountable? "gold")
  => true

  (uncountable? "moose")
  => true)

^{:refer std.string.plural/irregular? :added "3.0"}
(fact "checks if a work is irregular"

  (irregular? "geese")
  => true)

^{:refer std.string.plural/resolve-rules :added "3.0"}
(fact "goes through rules and checks if there is a matching pattern"

  (resolve-rules @*singular-rules*
                 "hello")
  => nil

  (resolve-rules @*singular-rules*
                 "hellos")
  => "hello")

^{:refer std.string.plural/singular :added "3.0"}
(fact "converts a word into its singular"

  (singular "rats")
  => "rat"

  (singular "geese")
  => "goose")

^{:refer std.string.plural/plural :added "3.0"}
(fact "converts a word into its plural"

  (plural "man")
  => "men"

  (plural "rate")
  => "rates"

  (plural "inventory")
  => "inventories"

  (plural "moose")
  => "moose")

(comment
  (singular "inventorys")
  (plural "rat")
  (singular "rat")
  (singular "rates")
  (plural "rates"))
