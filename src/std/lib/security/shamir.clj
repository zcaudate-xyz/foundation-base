(ns std.lib.security.shamir
  (:import (hara.lib.security.shamir Shamir SecretShare) (java.security SecureRandom) (java.math BigInteger)))

(comment
  (def -r- (SecureRandom.))
  (def -CERTAINTY- 256)

  (def -secret- (BigInteger. "1231234213421342134213412341234214132421342"))
  (def -prime- (BigInteger. (inc (.bitLength -secret-))
                            -CERTAINTY-
                            -r-))

  (def -shares- (seq (Shamir/split -secret- 3, 5 -prime- -r-)))

  (Shamir/combine (into-array SecretShare
                              (take 3 (shuffle -shares-)))
                  -prime-))

