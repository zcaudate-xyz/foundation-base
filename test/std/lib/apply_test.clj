(ns std.lib.apply-test
  (:use code.test)
  (:require [std.lib.apply :refer :all]))

^{:refer std.lib.apply/apply-in :added "3.0"}
(fact "runs the applicative within a context"

  (apply-in (host-applicative {:form '+})
            nil
            [1 2 3 4 5])
  => 15)

^{:refer std.lib.apply/apply-as :added "3.0"}
(fact "allows the applicative to auto-resolve its context"

  (apply-as (host-applicative {:form '+})
            [1 2 3 4 5])
  => 15)

^{:refer std.lib.apply/invoke-as :added "3.0"}
(fact "invokes the applicative to args"

  (invoke-as (host-applicative {:form '+})
             1 2 3 4 5)
  => 15)

^{:refer std.lib.apply/host-apply-in :added "3.0"}
(fact "helper function for the `host` applicative"

  (host-apply-in (host-applicative {:form '+})
                 nil
                 [1 2 3 4 5])
  => 15)

^{:refer std.lib.apply/host-applicative :added "3.0"}
(fact "constructs an applicative that does not need a context"

  @((host-applicative {:form '+ :async true}) 1 2 3 4 5)
  => 15)
