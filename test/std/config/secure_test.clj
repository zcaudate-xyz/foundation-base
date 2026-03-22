(ns std.config.secure-test
  (:require [std.config.secure :refer :all]
            [std.lib.security :as security])
  (:use code.test))

^{:refer std.config.secure/resolve-key :added "3.0"}
(fact "resolves the key for decryption"

  (def -key- (resolve-key "3YO19A4QAlRLDc8qmhLD7A=="))
  => #'-key-)

^{:refer std.config.secure/decrypt-text :added "3.0"}
(fact "decrypts text based on key"

  (decrypt-text "2OJZ7B2JpH7Pa5p9XpDc6w==" -key-)
  => "hello"

  (decrypt-text "d8e259ec1d89a47ecf6b9a7d5e90dceb"
                -key-
                {:format :hex})
  => "hello")

^{:refer std.config.secure/encrypt-text :added "3.0"}
(fact "encrypts text based on key"

  (encrypt-text "hello" -key-)
  => "2OJZ7B2JpH7Pa5p9XpDc6w=="

  (encrypt-text "hello" -key- {:format :hex})
  => "d8e259ec1d89a47ecf6b9a7d5e90dceb")
