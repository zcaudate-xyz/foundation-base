(ns std.lib.security-test
  (:require [std.lib.security :refer :all])
  (:use code.test))

^{:refer std.lib.security/sha1 :added "3.0"}
(fact "function for sha1 hash"

  (sha1 "123")
  => "40bd001563085fc35165329ea1ff5c5ecbdbbeef")

^{:refer std.lib.security/md5 :added "4.0"}
(fact "function for md5 hash"

  (md5 "123")
  => "202cb962ac59075b964b07152d234b70")
