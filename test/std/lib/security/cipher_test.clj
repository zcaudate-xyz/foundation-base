(ns std.lib.security.cipher-test
  (:require [std.lib.encode :as encode]
            [std.lib.security.cipher :refer :all])
  (:use code.test))

^{:refer std.lib.security.cipher/init-cipher :added "3.0"}
(comment "initializes cipher according to options")

^{:refer std.lib.security.cipher/operate :added "3.0"}
(comment "base function for encrypt and decrypt")

^{:refer std.lib.security.cipher/encrypt :added "3.0" :class [:security/general]}
(fact "encrypts a byte array using a key"

  (-> (encrypt (.getBytes "hello world")
               {:type "AES",
                :mode :secret,
                :format "RAW",
                :encoded "euHlt5sHWhRpbKZHjrwrrQ=="})
      (encode/to-hex))
  => "30491ab4427e45909f3d2f5d600b0f93")

^{:refer std.lib.security.cipher/decrypt :added "3.0" :class [:security/general]}
(fact "decrypts a byte array using a key"

  (-> (decrypt (encode/from-hex  "30491ab4427e45909f3d2f5d600b0f93")
               {:type "AES",
                :mode :secret,
                :format "RAW",
                :encoded "euHlt5sHWhRpbKZHjrwrrQ=="})
      (String.))
  => "hello world")
