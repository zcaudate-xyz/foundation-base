(ns documentation.std-lib-encode
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.encode` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "API" :link "std.lib.encode"}]]

[[:api {:namespace "std.lib.encode"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_encode_summary.md
;; sha256: 54221bf383b1402e70a060810ad3ce2b5c2160e892d7bba7e5afb8f28145989a
[[:chapter {:title "std.lib.encode: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-encode-summary-md"}]]

"The `std.lib.encode` namespace provides essential utility functions for encoding and decoding binary data into common string representations, specifically hexadecimal (hex) and Base64. These functions are crucial for tasks involving data serialization, secure transmission, and interoperability with systems that expect these formats."

[[:section {:title "Core Concepts:" :link "merged-plans-slop-summary-std-lib-encode-summary-md-core-concepts"}]]

"*   **Hexadecimal Encoding:** Converts raw byte arrays into a human-readable hexadecimal string representation, where each byte is represented by two hexadecimal characters (0-9, A-F).\n*   **Base64 Encoding:** Converts raw byte arrays into an ASCII string representation, suitable for transmission over mediums that do not handle binary data directly. Base64 is commonly used in MIME, JSON, and XML."

[[:section {:title "Key Functions:" :link "merged-plans-slop-summary-std-lib-encode-summary-md-key-functions"}]]

"*   **`hex-chars`**:\n    *   **Purpose:** Converts a single byte into its two corresponding hexadecimal characters.\n    *   **Input:** A `Byte` value.\n    *   **Output:** A character array of two hexadecimal characters.\n    *   **Usage:** `(hex-chars 255)` => `[\\f \\f]`\n*   **`to-hex-chars`**:\n    *   **Purpose:** Converts a byte array into a character array of hexadecimal characters.\n    *   **Input:** A `byte[]` array.\n    *   **Output:** A `char[]` array.\n    *   **Usage:** `(to-hex-chars (.getBytes \"hello\"))`\n*   **`to-hex`**:\n    *   **Purpose:** Converts a byte array into a hexadecimal string.\n    *   **Input:** A `byte[]` array.\n    *   **Output:** A `String` representing the hexadecimal value.\n    *   **Usage:** `(to-hex (.getBytes \"hello\"))` => `\"68656c6c6f\"`\n*   **`from-hex-chars`**:\n    *   **Purpose:** Converts two hexadecimal characters back into a single byte value.\n    *   **Input:** Two `Character` values (hex digits).\n    *   **Output:** A `Byte` value.\n    *   **Usage:** `(byte (from-hex-chars \\2 \\a))` => `42`\n*   **`from-hex`**:\n    *   **Purpose:** Converts a hexadecimal string back into a byte array.\n    *   **Input:** A `String` representing hexadecimal data.\n    *   **Output:** A `byte[]` array.\n    *   **Usage:** `(String. (from-hex \"68656c6c6f\"))` => `\"hello\"`\n*   **`to-base64-bytes`**:\n    *   **Purpose:** Encodes a byte array into a Base64 byte array.\n    *   **Input:** A `byte[]` array.\n    *   **Output:** A `byte[]` array containing the Base64 encoded data.\n    *   **Usage:** `(to-base64-bytes (.getBytes \"hello\"))`\n*   **`to-base64`**:\n    *   **Purpose:** Encodes a byte array into a Base64 string.\n    *   **Input:** A `byte[]` array.\n    *   **Output:** A `String` representing the Base64 encoded data.\n    *   **Usage:** `(to-base64 (.getBytes \"hello\"))` => `\"aGVsbG8=\"`\n*   **`from-base64`**:\n    *   **Purpose:** Decodes a Base64 string back into a byte array.\n    *   **Input:** A `String` representing Base64 encoded data.\n    *   **Output:** A `byte[]` array.\n    *   **Usage:** `(String. (from-base64 \"aGVsbG8=\"))` => `\"hello\"`"

[[:section {:title "Usage Pattern:" :link "merged-plans-slop-summary-std-lib-encode-summary-md-usage-pattern"}]]

"This namespace is commonly used in scenarios such as:"

"*   **API Communication:** Encoding binary data (e.g., images, files) for inclusion in JSON or XML payloads.\n*   **Security:** Representing cryptographic keys, hashes, or signatures in a standard string format.\n*   **Data Storage:** Storing binary data in text-based databases or configuration files.\n*   **Debugging:** Inspecting raw byte data in a more readable hexadecimal format."

"By providing these straightforward encoding and decoding utilities, `std.lib.encode` simplifies the handling of binary-to-text conversions in Clojure applications."
;; END merged documentation: plans/slop/summary/std_lib_encode_summary.md
