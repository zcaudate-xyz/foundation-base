## std.lib.security: A Comprehensive Summary

The `std.lib.security` module provides a comprehensive set of utilities for cryptographic operations, key management, and security provider introspection in Clojure. It wraps Java's Cryptography Architecture (JCA) and Java Cryptography Extension (JCE) to offer functionalities for encryption/decryption, key generation, digital signing, message digesting, and HMAC (Hash-based Message Authentication Code) generation. This module is crucial for applications requiring secure data handling, authentication, and integrity verification within the `foundation-base` project.

The module is organized into several sub-namespaces:

### `std.lib.security.provider`

This namespace focuses on introspecting and accessing available cryptographic service providers (CSPs) and their services.

*   **`list-providers []`**: Lists the names of all installed security providers.
*   **`sort-services [type services]`**: Helper function to filter and sort services by type.
*   **`list-services [& [type provider]]`**: Lists all available cryptographic services, optionally filtered by `type` (e.g., "Cipher", "KeyGenerator") and `provider`.
*   **`cipher [& [name provider]]`**: Lists available `Cipher` implementations or returns a `javax.crypto.Cipher` instance for a given `name` and optional `provider`.
*   **`key-factory [& [name provider]]`**: Lists available `KeyFactory` implementations or returns a `java.security.KeyFactory` instance.
*   **`key-generator [& [name provider]]`**: Lists available `KeyGenerator` implementations or returns a `javax.crypto.KeyGenerator` instance.
*   **`key-pair-generator [& [name provider]]`**: Lists available `KeyPairGenerator` implementations or returns a `java.security.KeyPairGenerator` instance.
*   **`key-store [& [name provider]]`**: Lists available `KeyStore` implementations or returns a `java.security.KeyStore` instance.
*   **`mac [& [name provider]]`**: Lists available `Mac` (Message Authentication Code) implementations or returns a `javax.crypto.Mac` instance.
*   **`message-digest [& [name provider]]`**: Lists available `MessageDigest` implementations or returns a `java.security.MessageDigest` instance.
*   **`signature [& [name provider]]`**: Lists available `Signature` implementations or returns a `java.security.Signature` instance.

### `std.lib.security.key`

This namespace provides utilities for generating, managing, and converting cryptographic keys.

*   **`init-key-generator [gen opts]`**: Initializes a `KeyGenerator` with options like `length`, `params`, and `random`.
*   **`generate-key [& [algo opts]]`**: Generates a symmetric `javax.crypto.SecretKey` for a given `algo` and `opts`.
*   **`init-key-pair-generator [gen opts]`**: Initializes a `KeyPairGenerator` with options.
*   **`generate-key-pair [& [type opts]]`**: Generates an asymmetric `java.security.KeyPair` (public and private keys) for a given `type` and `opts`.
*   **`key-mode [k]`**: Returns the mode of a key (`:public`, `:private`, or `:secret`).
*   **`key-type [k]`**: Returns the algorithm type of a key (e.g., "AES", "RSA").
*   **`key->map [k]`**: Converts a `java.security.Key` object into a map representation, including its type, mode, format, and encoded bytes (Base64).
*   **`to-bytes [input]`**: Converts input (Base64 string or byte array) to a byte array.
*   **`map->key [m]`**: A multimethod that converts a map representation of a key back into a `java.security.Key` object (e.g., `SecretKeySpec`, `PublicKey`, `PrivateKey`), dispatching on the `:mode` of the key.
*   **`->key [k]`**: An idempotent function that ensures its input `k` is a `java.security.Key` object, converting from a map if necessary.
*   **`print-method Key/PublicKey/PrivateKey`**: Extends `print-method` for `Key` types to display them as `#key` followed by their map representation.

### `std.lib.security.cipher`

This namespace provides functions for symmetric encryption and decryption using various cipher algorithms.

*   **`init-cipher [cipher mode key opts]`**: Initializes a `javax.crypto.Cipher` object with a given `mode` (encrypt/decrypt), `key`, and options (e.g., `params`, `random`, `iv`).
*   **`operate [mode bytes key opts]`**: The base function for both encryption and decryption, performing the actual cipher operation.
*   **`encrypt [bytes key & [opts]]`**: Encrypts a byte array using a given `key` and optional `opts`.
*   **`decrypt [bytes key & [opts]]`**: Decrypts a byte array using a given `key` and optional `opts`.

### `std.lib.security.verify`

This namespace offers functionalities for generating and verifying cryptographic hashes, HMACs, and digital signatures.

*   **`digest [& [bytes algo opts]]`**: Generates a cryptographic hash (message digest) of a byte array using a specified `algo` (e.g., "SHA1", "MD5").
*   **`init-hmac [mac key opts]`**: Initializes a `javax.crypto.Mac` object with a `key` and optional `params`.
*   **`hmac [& [bytes key opts]]`**: Generates an HMAC (keyed-hash message authentication code) of a byte array using a `key` and optional `opts`.
*   **`sign [& [bytes key opts]]`**: Generates a digital signature of a byte array using a private `key` and optional `opts`.
*   **`verify [& [bytes signature key opts]]`**: Verifies a digital signature of a byte array using a public `key`, the `signature`, and optional `opts`.

### `std.lib.security` (Facade Namespace)

This namespace acts as a facade, re-exporting key functions from its sub-namespaces and providing convenient wrappers for common hashing algorithms.

*   **`h/intern-in`**: Interns `encrypt`, `decrypt`, `generate-key`, `generate-key-pair`, `->key`, `key->map`, `list-providers`, `list-services`, `cipher`, `key-generator`, `key-pair-generator`, `key-store`, `mac`, `message-digest`, `signature`, `digest`, `hmac`, `sign`, and `verify` into this namespace.
*   **`sha1 [body]`**: Computes the SHA1 hash of a string and returns it as a hexadecimal string.
*   **`md5 [body]`**: Computes the MD5 hash of a string and returns it as a hexadecimal string.

**Overall Importance:**

The `std.lib.security` module is essential for building secure and trustworthy applications within the `foundation-base` project. Its key contributions include:

*   **Comprehensive Cryptographic Support:** Provides a wide range of cryptographic primitives for data confidentiality, integrity, and authentication.
*   **Key Management:** Simplifies the generation, representation, and handling of various types of cryptographic keys.
*   **Provider Introspection:** Allows for dynamic discovery and selection of available security providers and algorithms.
*   **Data Integrity and Authentication:** Offers tools for hashing, HMACs, and digital signatures to ensure data has not been tampered with and originates from a trusted source.
*   **Extensibility:** Built upon Java's flexible JCA/JCE, allowing for easy integration of new algorithms and providers.

By offering these robust security capabilities, `std.lib.security` significantly enhances the `foundation-base` project's ability to handle sensitive data and operations securely, which is vital for its multi-language development ecosystem.
