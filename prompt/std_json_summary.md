## std.json: A Comprehensive Summary

The `std.json` namespace provides a robust and flexible API for serializing and deserializing Clojure data to and from JSON format. It leverages the Jackson JSON processor, offering extensive customization options for handling various Clojure data types, including keywords, symbols, ratios, vars, and persistent collections. The module aims to provide a seamless and efficient way to work with JSON data in Clojure applications.

### Core Concepts:

*   **Jackson Integration:** Built on top of `com.fasterxml.jackson.databind.ObjectMapper`, providing high-performance JSON processing.
*   **Clojure Data Type Support:** Custom serializers and deserializers are provided for common Clojure data structures and types, ensuring correct round-tripping between Clojure and JSON.
*   **Customization:** Offers various options for configuring the `ObjectMapper`, such as pretty printing, handling `BigDecimal`s, escaping non-ASCII characters, and custom key encoding/decoding.
*   **Protocol-based I/O:** Uses protocols (`ReadValue`, `WriteValue`) to abstract over different input/output sources (strings, files, streams).

### Key Functions:

*   **`clojure-module`**:
    *   **Purpose:** Creates a `com.fasterxml.jackson.databind.module.SimpleModule` that registers custom serializers and deserializers for Clojure types.
    *   **Options:** Supports `encode-key-fn`, `decode-key-fn` for custom key transformations, `encoders` for additional custom serializers, and `date-format`.
*   **`object-mapper`**:
    *   **Purpose:** Creates and configures a `com.fasterxml.jackson.databind.ObjectMapper` instance with the `clojure-module` and other specified options.
    *   **Options:** `pretty` (for pretty printing), `bigdecimals`, `escape-non-ascii`, and `modules` for registering additional Jackson modules.
*   **`+default-mapper+`**: A default `ObjectMapper` instance with basic Clojure serialization.
*   **`+keyword-mapper+`**: An `ObjectMapper` that deserializes JSON keys to Clojure keywords.
*   **`+keyword-case-mapper+`**: An `ObjectMapper` that deserializes JSON keys to Clojure keywords, converting them to spear-case.
*   **`+keyword-js-mapper+`**: An `ObjectMapper` that deserializes JSON keys, replacing underscores with hyphens and converting to keywords.
*   **`+keyword-spear-mapper+`**: An `ObjectMapper` that deserializes JSON keys, replacing spaces with hyphens and converting to keywords.
*   **`ReadValue` Protocol**:
    *   **Purpose:** Defines the `-read-value` method for reading JSON from various sources (File, URL, String, Reader, InputStream).
*   **`WriteValue` Protocol**:
    *   **Purpose:** Defines the `-write-value` method for writing JSON to various sinks (File, OutputStream, DataOutput, Writer).
*   **`read`**:
    *   **Purpose:** Reads JSON content from an object (string, file, stream) and deserializes it into Clojure data.
    *   **Usage:** `(read "{\"a\":1,\"b\":2}")` => `{"a" 1, "b" 2}`
*   **`write`**:
    *   **Purpose:** Serializes Clojure data into a JSON string.
    *   **Usage:** `(write {:a 1 :b 2})` => `"{\"a\":1,\"b\":2}"`
*   **`write-pp`**:
    *   **Purpose:** Serializes Clojure data into a pretty-printed JSON string.
    *   **Usage:** `(write-pp {:a 1 :b 2})`
*   **`write-bytes`**:
    *   **Purpose:** Serializes Clojure data into a JSON byte array.
    *   **Usage:** `(String. (write-bytes {:a 1 :b 2}))`
*   **`write-to`**:
    *   **Purpose:** Writes Clojure data as JSON to a specified sink (file, output stream).
    *   **Usage:** `(write-to my-output-stream {:a 1 :b 2})`
*   **`sys:resource-json`**:
    *   **Purpose:** Reads and caches JSON content from a classpath resource, deserializing keys to spear-case keywords.

### Usage Pattern:

This namespace is crucial for any Clojure application that needs to:
*   **Interact with Web Services/APIs:** Send and receive JSON data.
*   **Store Configuration:** Use JSON as a configuration file format.
*   **Data Exchange:** Exchange data with other systems that use JSON.
*   **Logging:** Log structured data in JSON format.

By providing a powerful, customizable, and efficient JSON serialization/deserialization library, `std.json` simplifies working with JSON data in Clojure applications.
