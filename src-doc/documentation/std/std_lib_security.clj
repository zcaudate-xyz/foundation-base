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

;; BEGIN merged documentation: plans/slop/summary/std_lib_security_summary.md
;; sha256: 1ce1d31be4b96f013fb2ba6dd85feef5e76090333cf8be5ef50d47c36c34ae12
[[:chapter {:title "std.lib.security: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-security-summary-md"}]]

"The `std.lib.security` module provides a comprehensive set of utilities for cryptographic operations, key management, and security provider introspection in Clojure. It wraps Java's Cryptography Architecture (JCA) and Java Cryptography Extension (JCE) to offer functionalities for encryption/decryption, key generation, digital signing, message digesting, and HMAC (Hash-based Message Authentication Code) generation. This module is crucial for applications requiring secure data handling, authentication, and integrity verification within the `foundation-base` project."

"The module is organized into several sub-namespaces:"

[[:section {:title "std.lib.security.provider" :link "merged-plans-slop-summary-std-lib-security-summary-md-std-lib-security-provider"}]]

"This namespace focuses on introspecting and accessing available cryptographic service providers (CSPs) and their services."

"*   **`list-providers []`**: Lists the names of all installed security providers.\n*   **`sort-services [type services]`**: Helper function to filter and sort services by type.\n*   **`list-services [& [type provider]]`**: Lists all available cryptographic services, optionally filtered by `type` (e.g., \"Cipher\", \"KeyGenerator\") and `provider`.\n*   **`cipher [& [name provider]]`**: Lists available `Cipher` implementations or returns a `javax.crypto.Cipher` instance for a given `name` and optional `provider`.\n*   **`key-factory [& [name provider]]`**: Lists available `KeyFactory` implementations or returns a `java.security.KeyFactory` instance.\n*   **`key-generator [& [name provider]]`**: Lists available `KeyGenerator` implementations or returns a `javax.crypto.KeyGenerator` instance.\n*   **`key-pair-generator [& [name provider]]`**: Lists available `KeyPairGenerator` implementations or returns a `java.security.KeyPairGenerator` instance.\n*   **`key-store [& [name provider]]`**: Lists available `KeyStore` implementations or returns a `java.security.KeyStore` instance.\n*   **`mac [& [name provider]]`**: Lists available `Mac` (Message Authentication Code) implementations or returns a `javax.crypto.Mac` instance.\n*   **`message-digest [& [name provider]]`**: Lists available `MessageDigest` implementations or returns a `java.security.MessageDigest` instance.\n*   **`signature [& [name provider]]`**: Lists available `Signature` implementations or returns a `java.security.Signature` instance."

[[:section {:title "std.lib.security.key" :link "merged-plans-slop-summary-std-lib-security-summary-md-std-lib-security-key"}]]

"This namespace provides utilities for generating, managing, and converting cryptographic keys."

"*   **`init-key-generator [gen opts]`**: Initializes a `KeyGenerator` with options like `length`, `params`, and `random`.\n*   **`generate-key [& [algo opts]]`**: Generates a symmetric `javax.crypto.SecretKey` for a given `algo` and `opts`.\n*   **`init-key-pair-generator [gen opts]`**: Initializes a `KeyPairGenerator` with options.\n*   **`generate-key-pair [& [type opts]]`**: Generates an asymmetric `java.security.KeyPair` (public and private keys) for a given `type` and `opts`.\n*   **`key-mode [k]`**: Returns the mode of a key (`:public`, `:private`, or `:secret`).\n*   **`key-type [k]`**: Returns the algorithm type of a key (e.g., \"AES\", \"RSA\").\n*   **`key->map [k]`**: Converts a `java.security.Key` object into a map representation, including its type, mode, format, and encoded bytes (Base64).\n*   **`to-bytes [input]`**: Converts input (Base64 string or byte array) to a byte array.\n*   **`map->key [m]`**: A multimethod that converts a map representation of a key back into a `java.security.Key` object (e.g., `SecretKeySpec`, `PublicKey`, `PrivateKey`), dispatching on the `:mode` of the key.\n*   **`->key [k]`**: An idempotent function that ensures its input `k` is a `java.security.Key` object, converting from a map if necessary.\n*   **`print-method Key/PublicKey/PrivateKey`**: Extends `print-method` for `Key` types to display them as `#key` followed by their map representation."

[[:section {:title "std.lib.security.cipher" :link "merged-plans-slop-summary-std-lib-security-summary-md-std-lib-security-cipher"}]]

"This namespace provides functions for symmetric encryption and decryption using various cipher algorithms."

"*   **`init-cipher [cipher mode key opts]`**: Initializes a `javax.crypto.Cipher` object with a given `mode` (encrypt/decrypt), `key`, and options (e.g., `params`, `random`, `iv`).\n*   **`operate [mode bytes key opts]`**: The base function for both encryption and decryption, performing the actual cipher operation.\n*   **`encrypt [bytes key & [opts]]`**: Encrypts a byte array using a given `key` and optional `opts`.\n*   **`decrypt [bytes key & [opts]]`**: Decrypts a byte array using a given `key` and optional `opts`."

[[:section {:title "std.lib.security.verify" :link "merged-plans-slop-summary-std-lib-security-summary-md-std-lib-security-verify"}]]

"This namespace offers functionalities for generating and verifying cryptographic hashes, HMACs, and digital signatures."

"*   **`digest [& [bytes algo opts]]`**: Generates a cryptographic hash (message digest) of a byte array using a specified `algo` (e.g., \"SHA1\", \"MD5\").\n*   **`init-hmac [mac key opts]`**: Initializes a `javax.crypto.Mac` object with a `key` and optional `params`.\n*   **`hmac [& [bytes key opts]]`**: Generates an HMAC (keyed-hash message authentication code) of a byte array using a `key` and optional `opts`.\n*   **`sign [& [bytes key opts]]`**: Generates a digital signature of a byte array using a private `key` and optional `opts`.\n*   **`verify [& [bytes signature key opts]]`**: Verifies a digital signature of a byte array using a public `key`, the `signature`, and optional `opts`."

[[:section {:title "std.lib.security (Facade Namespace)" :link "merged-plans-slop-summary-std-lib-security-summary-md-std-lib-security-facade-namespace"}]]

"This namespace acts as a facade, re-exporting key functions from its sub-namespaces and providing convenient wrappers for common hashing algorithms."

"*   **`h/intern-in`**: Interns `encrypt`, `decrypt`, `generate-key`, `generate-key-pair`, `->key`, `key->map`, `list-providers`, `list-services`, `cipher`, `key-generator`, `key-pair-generator`, `key-store`, `mac`, `message-digest`, `signature`, `digest`, `hmac`, `sign`, and `verify` into this namespace.\n*   **`sha1 [body]`**: Computes the SHA1 hash of a string and returns it as a hexadecimal string.\n*   **`md5 [body]`**: Computes the MD5 hash of a string and returns it as a hexadecimal string."

"**Overall Importance:**"

"The `std.lib.security` module is essential for building secure and trustworthy applications within the `foundation-base` project. Its key contributions include:"

"*   **Comprehensive Cryptographic Support:** Provides a wide range of cryptographic primitives for data confidentiality, integrity, and authentication.\n*   **Key Management:** Simplifies the generation, representation, and handling of various types of cryptographic keys.\n*   **Provider Introspection:** Allows for dynamic discovery and selection of available security providers and algorithms.\n*   **Data Integrity and Authentication:** Offers tools for hashing, HMACs, and digital signatures to ensure data has not been tampered with and originates from a trusted source.\n*   **Extensibility:** Built upon Java's flexible JCA/JCE, allowing for easy integration of new algorithms and providers."

"By offering these robust security capabilities, `std.lib.security` significantly enhances the `foundation-base` project's ability to handle sensitive data and operations securely, which is vital for its multi-language development ecosystem."
;; END merged documentation: plans/slop/summary/std_lib_security_summary.md
