(ns std.lib.bin.type-test
  (:use code.test)
  (:require [std.lib.bin.type :refer :all :as binary]
            [std.fs :as fs])
  (:refer-clojure :exclude [bytes]))

^{:refer std.lib.bin.type/bitstr-to-bitseq :added "3.0"}
(fact "creates a binary sequence from a bitstr"

  (bitstr-to-bitseq "100011")
  => [1 0 0 0 1 1])

^{:refer std.lib.bin.type/bitseq-to-bitstr :added "3.0"}
(fact "creates a bitstr from a binary sequence"

  (bitseq-to-bitstr [1 0 0 0 1 1])
  => "100011")

^{:refer std.lib.bin.type/bitseq-to-number :added "3.0"}
(fact "creates a number from a binary sequence"

  (bitseq-to-number [1 0 0 0 1 1])
  => 49

  (bitseq-to-number (repeat 10 1))
  => 1023
  ^:hidden
  (- (bitseq-to-number (repeat 64 1))
     (bitseq-to-number (repeat 63 1)))
  => 9223372036854775808N)

^{:refer std.lib.bin.type/bitseq-to-bitset :added "3.0"}
(fact "creates a bitset from a binary sequence"

  (bitseq-to-bitset [1 0 0 0 1 1])
  ;; => #bs[1 0 0 0 1 1]
  => java.util.BitSet
  => any?
  => any?)

^{:refer std.lib.bin.type/bitset-to-bitseq :added "3.0"}
(fact "creates a binary sequence from a bitset"
  (-> (bitseq-to-bitset [1 0 0 0 1 1])
      (bitset-to-bitseq))
  => [1 0 0 0 1 1])

^{:refer std.lib.bin.type/bitset-to-bytes :added "3.0"}
(fact "creates a byte array from a bitset"

  (-> (bitseq-to-bitset [1 0 0 0 1 1])
      (bitset-to-bytes)
      seq)
  => [49])

^{:refer std.lib.bin.type/bytes-to-bitset :added "3.0"}
(fact "creates a bitset from bytes"

  (-> (byte-array [49])
      (bytes-to-bitset)
      (bitset-to-bitseq 8))
  => [1 0 0 0 1 1 0 0])

^{:refer std.lib.bin.type/long-to-bitseq :added "3.0"}
(fact "creates a binary sequence from a long"

  (long-to-bitseq 1023)
  => [1 1 1 1 1 1 1 1 1 1]

  (long-to-bitseq 49 8)
  => [1 0 0 0 1 1 0 0])

^{:refer std.lib.bin.type/bigint-to-bitset :added "3.0"}
(fact "creates a bitset from a bigint"

  (-> (bigint-to-bitset 9223372036854775808N)
      (bitset-to-bitseq))
  => (conj (vec (repeat 63 0))
           1))

^{:refer std.lib.bin.type/bitstr? :added "3.0"}
(fact "checks if a string consists of only 1s and 0s"

  (bitstr? "0101010101")
  => true

  (bitstr? "hello")
  => false)

^{:refer std.lib.bin.type/input-stream :added "3.0"}
(fact "creates an inputstream from various representations"

  (input-stream 9223372036854775808N)
  => java.io.ByteArrayInputStream)

^{:refer std.lib.bin.type/output-stream :added "3.0"}
(comment "creates an inputstream from various representations"

  (output-stream (fs/file "project.clj"))
  => java.io.FileOutputStream)

^{:refer std.lib.bin.type/channel :added "3.0"}
(comment "creates a channel from various representations"

  (channel (fs/file "project.clj"))
  => java.nio.channels.Channel)

^{:refer std.lib.bin.type/bitstr :added "3.0"}
(fact "converts to a bitstr binary representation"

  (binary/bitstr (byte-array [49]))
  => "100011")

^{:refer std.lib.bin.type/bitseq :added "3.0"}
(fact "converts to a bitseq binary representation"

  (binary/bitseq (byte-array [49]))
  => [1 0 0 0 1 1])

^{:refer std.lib.bin.type/bitset :added "3.0"}
(fact "converts to a bitset binary representation"

  (-> (binary/bitset "100011")
      (bitset-to-bitseq))
  => [1 0 0 0 1 1])

^{:refer std.lib.bin.type/bytes :added "3.0"}
(fact "converts to a byte array binary representation"

  (-> (binary/bytes "100011")
      (seq))
  => [49])

^{:refer std.lib.bin.type/number :added "3.0"}
(fact "converts to a number binary representation"

  (binary/number "100011")
  => 49
  ^:hidden
  (-> (binary/number (byte-array [78 45 34 -12 45 34 56]))
      (binary/bitstr)
      (binary/bitset)
      (binary/bytes)
      (seq))
  => [78 45 34 -12 45 34 56])
