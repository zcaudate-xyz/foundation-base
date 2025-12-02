(ns lib.redis.impl.reference-test
  (:use code.test)
  (:require [lib.redis.impl.reference :refer :all]
            [std.lib :as h]))

^{:refer lib.redis.impl.reference/command-doc :added "3.0"}
(fact "converts an entry to the redis doc format"
  (command-doc {:id :set :prefix ["SET"] :arguments [{:name "key"} {:name "value"} {:name "EX" :optional true}]})
  => "SET key value [EX]")

^{:refer lib.redis.impl.reference/parse-main :added "3.0"}
(fact "parses file for main reference"
  (parse-main "{}") => map?)

^{:refer lib.redis.impl.reference/parse-supplements :added "3.0"}
(fact "parses file for supplement reference"
  (parse-supplements "{}") => map?)

^{:refer lib.redis.impl.reference/parse-commands :added "3.0"}
(fact "returns all commands"
  (with-redefs [parse-main (constantly {:set {:id :set :group :string}})
                parse-supplements (constantly {})
                h/sys:resource-content (constantly "{}")]
    (parse-commands))
  => map?)

^{:refer lib.redis.impl.reference/command-list :added "3.0"}
(fact "returns all commands"
  (with-redefs [parse-commands (constantly {:set {:group :string} :hset {:group :hash}})]
    (command-list) => (just [:hset :set])
    (command-list :hash) => (just [:hset])))

^{:refer lib.redis.impl.reference/command :added "3.0"}
(fact "gets the command info"
  (with-redefs [parse-commands (constantly {:hset {:id :hset}})]
    (command :hset))
  => {:id :hset})

^{:refer lib.redis.impl.reference/command-groups :added "3.0"}
(fact "lists all command group types"
  (with-redefs [parse-commands (constantly {:set {:group :string} :hset {:group :hash}})]
    (command-groups))
  => (just [:hash :string]))
