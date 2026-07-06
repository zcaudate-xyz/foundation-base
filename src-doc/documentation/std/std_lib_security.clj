(ns documentation.std-lib-security
  (:require [std.lib.security :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.security` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Hashing"}]]

"`sha1` and `md5` produce hex-encoded message digests of a string. They are thin wrappers over `verify/digest`."

(fact "hash strings to hex"
  (sha1 "123")
  => "40bd001563085fc35165329ea1ff5c5ecbdbbeef"

  (md5 "123")
  => "202cb962ac59075b964b07152d234b70")

[[:section {:title "Digests and HMAC"}]]

"`digest` returns raw bytes; call `encode/to-hex` to display them. `hmac` computes a keyed hash. `list-providers` and `list-services` show what algorithms are available on the current JVM."

(fact "list security providers and services"
  (list-providers)
  => coll?

  (list-services "MessageDigest")
  => coll?)

(fact "compute a raw digest"
  (-> (digest (.getBytes "hello world") "SHA")
      (std.lib.encode/to-hex))
  => "2aae6c35c94fcfb415dbe95f408b9ce91ee846ed")

(fact "compute a keyed HMAC"
  (-> (hmac (.getBytes "hello world")
            {:type "HmacSHA1"
             :mode :secret
             :format "RAW"
             :encoded "wQ0lyydDSEFRKviwv/2BoWVQDpj8hbUiUXytuWj7Yv8="})
      (std.lib.encode/to-hex))
  => "a6f9e08fad62f63a35c6fd320f4249c9ad3079dc")

[[:section {:title "Keys"}]]

"Generate secret or asymmetric keys, convert them to and from maps, and inspect their algorithm and mode."

(fact "generate and inspect a secret key"
  (let [k (generate-key "AES" {:length 128})]
    (key-type k)
    => "AES"

    (key->map k)
    => (contains {:type "AES"
                  :mode :secret
                  :format "RAW"
                  :encoded string?})))

(fact "generate a key pair"
  (->> (generate-key-pair "RSA" {:length 512})
       (map key-mode))
  => [:public :private])

(fact "round-trip a key through its map representation"
  (let [k  (generate-key "AES" {:length 128})
        km (key->map k)]
    (-> km ->key key->map)
    => (contains km)))

[[:section {:title "Encryption and decryption"}]]

"`encrypt` and `decrypt` operate on byte arrays. The examples use a fixed AES key so the ciphertext is deterministic."

(fact "encrypt and decrypt with AES"
  (let [key   {:type "AES"
               :mode :secret
               :format "RAW"
               :encoded "euHlt5sHWhRpbKZHjrwrrQ=="}
        bytes (.getBytes "hello world")]
    (-> (decrypt (encrypt bytes key) key)
        (String.))
    => "hello world"))

[[:section {:title "End-to-end: generate a key, encrypt data, and verify it"}]]

"A complete workflow: list available ciphers, generate an AES key, encrypt a message, and decrypt it back to the original string."

(fact "generate a fresh key and round-trip a message"
  (let [key   (generate-key "AES" {:length 128})
        text  "round-trip secret message"
        bytes (.getBytes text)]
    (-> (encrypt bytes key)
        (decrypt key)
        (String.))
    => text))

[[:chapter {:title "API" :link "std.lib.security"}]]

[[:api {:namespace "std.lib.security"}]]
