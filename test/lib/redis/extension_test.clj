(ns lib.redis.extension-test
  (:require [lib.redis.extension :refer :all])
  (:use code.test))

^{:refer lib.redis.extension/optional:set :added "4.0"}
(fact "optional parameters for `set` command"

  (optional:set {:expiry 10 :unit :ms :mode :exists} nil)
  => ["XX" "PX" 10])