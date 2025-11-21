## std.lib.bin: A Comprehensive Summary

The `std.lib.bin` namespace, along with its sub-namespaces `std.lib.bin.buffer` and `std.lib.bin.type`, provides a comprehensive set of utilities for working with binary data in Clojure. This module focuses on efficient manipulation of byte buffers, conversion between various binary representations (bit strings, bit sequences, bit sets, numbers, byte arrays), and handling I/O streams and channels for binary data.

### `std.lib.bin` (Main Namespace)

This namespace primarily acts as an aggregator, re-exporting key functions from its sub-namespaces `std.lib.bin.buffer` and `std.lib.bin.type`. It simplifies access to the binary manipulation capabilities by providing a unified interface.

**Key Re-exported Functions:**

*   From `std.lib.bin.buffer`: `buffer`, `buffer-read`, `buffer-write`, and type-specific buffer creation functions like `byte-buffer`, `char-buffer`, `short-buffer`, `int-buffer`, `long-buffer`, `float-buffer`, `double-buffer`.
*   From `std.lib.bin.type`: `input-stream`, `output-stream`, `bitseq`, `bitstr`, `bitset`, `bytes`, `number`.

### `std.lib.bin.buffer` (NIO Buffer Manipulation)

This sub-namespace provides tools for creating, manipulating, and converting Java NIO `ByteBuffer` and its type-specific views (e.g., `IntBuffer`, `DoubleBuffer`). It emphasizes direct memory manipulation and control over byte order (endianness).

**Core Concepts:**

*   **NIO Buffers:** Leverages Java's `java.nio` package for efficient, direct memory access to binary data.
*   **Endianness:** Explicit control over `ByteOrder` (big-endian or little-endian) for multi-byte data types.
*   **Type-Specific Buffers:** Supports creating and working with buffers for various primitive types (byte, char, short, int, long, float, double).

**Key Functions:**

*   **`byte-order`**: Converts a keyword (`:little`, `:big`) to a `java.nio.ByteOrder` object.
*   **`buffer`**: The primary function for creating or wrapping NIO buffers. It allows specifying the buffer type (`:byte`, `:int`, etc.), whether it's direct or heap-allocated, endianness, and if it should convert to a type-specific view.
    *   **Type-specific buffer creation functions (e.g., `byte-buffer`, `double-buffer`)**: Convenience wrappers around `buffer` for specific primitive types.
*   **`buffer-convert`**: Converts a `ByteBuffer` to a type-specific buffer (e.g., `DoubleBuffer`).
*   **`buffer-type`**: Returns metadata (e.g., `:type`, `:array-fn`) about the primitive type associated with a given buffer class.
*   **`buffer-primitive`**: Similar to `buffer-type`, but works with a buffer instance.
*   **`buffer-put`**: Writes a primitive array into a buffer.
*   **`buffer-get`**: Reads data from a buffer into a primitive array.
*   **`buffer-write`**: Writes a primitive array into a `ByteBuffer`, handling type conversion if necessary.
*   **`buffer-read`**: Reads data from a buffer into a primitive array, handling type conversion.

### `std.lib.bin.type` (Binary Data Conversion and I/O)

This sub-namespace focuses on converting data between various binary representations and providing unified interfaces for binary I/O (streams and channels). It extends the `std.protocol.binary/IBinary` and `std.protocol.binary/IByteSource`/`IByteSink`/`IByteChannel` protocols to many common Java types.

**Core Concepts:**

*   **`IBinary` Protocol:** Defines a set of functions for converting an object to different binary forms: `bitstr` (binary string), `bitseq` (sequence of 0s and 1s), `bitset` (`java.util.BitSet`), `bytes` (byte array), and `number` (numeric representation).
*   **`IByteSource` / `IByteSink` / `IByteChannel` Protocols:** Provide a unified way to get `InputStream`, `OutputStream`, or `ByteChannel` from various data sources (e.g., `File`, `URL`, `ByteBuffer`, `byte[]`).
*   **Supported Formats:** The module explicitly supports conversion for: bit strings, bit sequences, bit sets, numbers (Long, BigInt), byte arrays, `InputStream`, `ByteBuffer`, `File`, `Path`, `Channel`, `URL`, `URI`.

**Key Functions:**

*   **`bitstr-to-bitseq`, `bitseq-to-bitstr`**: Convert between binary strings and sequences.
*   **`bitseq-to-number`, `long-to-bitseq`**: Convert between binary sequences and numbers (handling large numbers with `BigInt`).
*   **`bitseq-to-bitset`, `bitset-to-bitseq`**: Convert between binary sequences and `BitSet`.
*   **`bitset-to-bytes`, `bytes-to-bitset`**: Convert between `BitSet` and byte arrays.
*   **`bitstr?`**: Checks if a string is a valid binary string (contains only '0' and '1').
*   **`input-stream`**: Creates an `InputStream` from various binary representations.
*   **`output-stream`**: Creates an `OutputStream` from various binary representations.
*   **`channel`**: Creates a `ByteChannel` from various binary representations.
*   **`bitstr`, `bitseq`, `bitset`, `bytes`, `number` (definvoke functions)**: These are the primary entry points for converting any supported object into its respective binary representation, leveraging the `IBinary` protocol.

### Usage Pattern:

This module is essential for any application requiring low-level binary data handling, network communication, file I/O, or cryptographic operations where precise control over data representation and efficient processing are critical. It abstracts away much of the boilerplate Java interop for NIO buffers and binary conversions, providing a more idiomatic Clojure interface.

```clojure
;; Example: Creating and writing to a ByteBuffer
(require '[std.lib.bin :as bin])

(def my-buffer (bin/int-buffer 4)) ; Creates an IntBuffer with capacity 4
(bin/buffer-write my-buffer (int-array [10 20 30 40]))

;; Example: Converting a number to a bit sequence and then to bytes
(require '[std.lib.bin.type :as btype])

(def num-val 49)
(def bit-seq (btype/bitseq num-val)) ; => [1 0 0 0 1 1]
(def byte-arr (btype/bytes num-val)) ; => byte-array with [49]
```