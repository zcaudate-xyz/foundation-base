(ns std.concurrent.request-apply-test
  (:require [std.concurrent.request-apply :refer :all])
  (:use [code.test :exclude [run]]))

^{:refer std.concurrent.request-apply/req-call :added "3.0"}
(fact "extensible function for a request applicative")

^{:refer std.concurrent.request-apply/req-apply-in :added "3.0"}
(fact "runs a request applicative")

^{:refer std.concurrent.request-apply/req:applicative :added "3.0"}
(fact "constructs a request applicative")
