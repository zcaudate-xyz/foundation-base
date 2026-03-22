(ns std.lib.transform.coerce-test
  (:require [std.lib.transform.coerce :refer :all])
  (:use code.test))

^{:refer std.lib.transform.coerce/assoc-set :added "3.0"}
(fact "associates a set as keys to a map"
  (assoc-set {} #{:a :b :c} 1)
  => {:a 1, :b 1, :c 1})

^{:refer std.lib.transform.coerce/hash-mapset :added "3.0"}
(fact "constructs a hashmap from a set of keys"

  (hash-mapset #{:a :b :c} 1 #{:d :e} 2)
  => {:c 1, :b 1, :a 1, :e 2, :d 2})

^{:refer std.lib.transform.coerce/read-enum :added "3.0"}
(fact "helper function for enums"

  (read-enum ":enum") => :enum

  (read-enum "12334") => 12334

  (read-enum "{}") => (throws))

^{:refer std.lib.transform.coerce/read-ref :added "3.0"}
(fact "helper function for refs"

  (read-ref "{:a 1}") => {:a 1}

  (read-ref "_") => (throws))

^{:refer std.lib.transform.coerce/parse-date :added "3.0"}
(fact "converts string to date"

  (parse-date "2017-05-25T17:29:46.000Z")
  ;;#inst "2017-05-25T09:29:46.000-00:00"
  => java.util.Date)

^{:refer std.lib.transform.coerce/coerce :added "3.0"}
(fact "associates a set as keys to a map"
  (coerce 1 :string)
  => "1"

  (coerce  "2017-05-25T17:29:46.000Z" :instant)
  => (java.util.Date. 117 4 25 17 29 46)

  (coerce "2017-05-25T17:29:46Z" :instant)
  => (java.util.Date. 117 4 25 17 29 46)

  (coerce "oeuoe" :keyword)
  => :oeuoe)

(comment
  (./import)
  (./scaffold))
