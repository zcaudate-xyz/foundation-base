(ns std.lib.encode-test
  (:use code.test)
  (:require [std.lib.encode :refer :all]))

^{:refer std.lib.encode/hex-chars :added "3.0"}
(fact "turns a byte into two chars"

  (hex-chars 255)
  => [\f \f]

  (hex-chars 42)
  => [\2 \a])

^{:refer std.lib.encode/to-hex-chars :added "3.0"}
(fact "turns a byte array into a hex char array")

^{:refer std.lib.encode/to-hex :added "3.0"}
(fact "turns a byte array into hex string"

  (to-hex (.getBytes "hello"))
  => "68656c6c6f")

^{:refer std.lib.encode/from-hex-chars :added "3.0"}
(fact "turns two hex characters into a byte value"

  (byte (from-hex-chars \2 \a))
  => 42)

^{:refer std.lib.encode/from-hex :added "3.0"}
(fact "turns a hex string into a sequence of bytes"

  (String. (from-hex "68656c6c6f"))
  => "hello")

^{:refer std.lib.encode/to-base64-bytes :added "3.0"}
(fact "turns a byte array into a base64 encoding"

  (-> (.getBytes "hello")
      (to-base64-bytes)
      (String.))
  => "aGVsbG8=")

^{:refer std.lib.encode/to-base64 :added "3.0"}
(fact "turns a byte array into a base64 encoded string"

  (-> (.getBytes "hello")
      (to-base64))
  => "aGVsbG8=")

^{:refer std.lib.encode/from-base64 :added "3.0"}
(fact "turns a base64 encoded string into a byte array"

  (-> (from-base64 "aGVsbG8=")
      (String.))
  => "hello")