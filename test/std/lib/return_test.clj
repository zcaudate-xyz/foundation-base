(ns std.lib.return-test
  (:require [std.lib.future :as f]
            [std.lib.return :refer :all])
  (:use code.test))

^{:refer std.lib.return/return-resolve :added "3.0"}
(fact "resolves encased futures"

  (return-resolve (f/future (f/future 1)))
  => 1)

^{:refer std.lib.return/return-chain :added "3.0"}
(fact "chains a function if a future or resolves if not"

  (return-chain 1 inc)
  => 2

  @(return-chain (f/future 1) inc)
  => 2)
