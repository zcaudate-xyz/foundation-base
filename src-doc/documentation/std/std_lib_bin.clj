(ns documentation.std-lib-bin
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.bin` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "API" :link "std.lib.bin"}]]

[[:api {:namespace "std.lib.bin"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_bin_summary.md
;; sha256: cf77b820c8a669e9f567227c6ec5c3c61147479de09875f9b4bf51f75e0def82
[[:chapter {:title "std.lib.bin: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-bin-summary-md"}]]

"The `std.lib.bin` namespace, along with its sub-namespaces `std.lib.bin.buffer` and `std.lib.bin.type`, provides a comprehensive set of utilities for working with binary data in Clojure. This module focuses on efficient manipulation of byte buffers, conversion between various binary representations (bit strings, bit sequences, bit sets, numbers, byte arrays), and handling I/O streams and channels for binary data."

[[:section {:title "std.lib.bin (Main Namespace)" :link "merged-plans-slop-summary-std-lib-bin-summary-md-std-lib-bin-main-namespace"}]]

"This namespace primarily acts as an aggregator, re-exporting key functions from its sub-namespaces `std.lib.bin.buffer` and `std.lib.bin.type`. It simplifies access to the binary manipulation capabilities by providing a unified interface."

"**Key Re-exported Functions:**"

"*   From `std.lib.bin.buffer`: `buffer`, `buffer-read`, `buffer-write`, and type-specific buffer creation functions like `byte-buffer`, `char-buffer`, `short-buffer`, `int-buffer`, `long-buffer`, `float-buffer`, `double-buffer`.\n*   From `std.lib.bin.type`: `input-stream`, `output-stream`, `bitseq`, `bitstr`, `bitset`, `bytes`, `number`."

[[:section {:title "std.lib.bin.buffer (NIO Buffer Manipulation)" :link "merged-plans-slop-summary-std-lib-bin-summary-md-std-lib-bin-buffer-nio-buffer-manipulation"}]]

"This sub-namespace provides tools for creating, manipulating, and converting Java NIO `ByteBuffer` and its type-specific views (e.g., `IntBuffer`, `DoubleBuffer`). It emphasizes direct memory manipulation and control over byte order (endianness)."

"**Core Concepts:**"

"*   **NIO Buffers:** Leverages Java's `java.nio` package for efficient, direct memory access to binary data.\n*   **Endianness:** Explicit control over `ByteOrder` (big-endian or little-endian) for multi-byte data types.\n*   **Type-Specific Buffers:** Supports creating and working with buffers for various primitive types (byte, char, short, int, long, float, double)."

"**Key Functions:**"

"*   **`byte-order`**: Converts a keyword (`:little`, `:big`) to a `java.nio.ByteOrder` object.\n*   **`buffer`**: The primary function for creating or wrapping NIO buffers. It allows specifying the buffer type (`:byte`, `:int`, etc.), whether it's direct or heap-allocated, endianness, and if it should convert to a type-specific view.\n    *   **Type-specific buffer creation functions (e.g., `byte-buffer`, `double-buffer`)**: Convenience wrappers around `buffer` for specific primitive types.\n*   **`buffer-convert`**: Converts a `ByteBuffer` to a type-specific buffer (e.g., `DoubleBuffer`).\n*   **`buffer-type`**: Returns metadata (e.g., `:type`, `:array-fn`) about the primitive type associated with a given buffer class.\n*   **`buffer-primitive`**: Similar to `buffer-type`, but works with a buffer instance.\n*   **`buffer-put`**: Writes a primitive array into a buffer.\n*   **`buffer-get`**: Reads data from a buffer into a primitive array.\n*   **`buffer-write`**: Writes a primitive array into a `ByteBuffer`, handling type conversion if necessary.\n*   **`buffer-read`**: Reads data from a buffer into a primitive array, handling type conversion."

[[:section {:title "std.lib.bin.type (Binary Data Conversion and I/O)" :link "merged-plans-slop-summary-std-lib-bin-summary-md-std-lib-bin-type-binary-data-conversion-and-i-o"}]]

"This sub-namespace focuses on converting data between various binary representations and providing unified interfaces for binary I/O (streams and channels). It extends the `std.protocol.binary/IBinary` and `std.protocol.binary/IByteSource`/`IByteSink`/`IByteChannel` protocols to many common Java types."

"**Core Concepts:**"

"*   **`IBinary` Protocol:** Defines a set of functions for converting an object to different binary forms: `bitstr` (binary string), `bitseq` (sequence of 0s and 1s), `bitset` (`java.util.BitSet`), `bytes` (byte array), and `number` (numeric representation).\n*   **`IByteSource` / `IByteSink` / `IByteChannel` Protocols:** Provide a unified way to get `InputStream`, `OutputStream`, or `ByteChannel` from various data sources (e.g., `File`, `URL`, `ByteBuffer`, `byte[]`).\n*   **Supported Formats:** The module explicitly supports conversion for: bit strings, bit sequences, bit sets, numbers (Long, BigInt), byte arrays, `InputStream`, `ByteBuffer`, `File`, `Path`, `Channel`, `URL`, `URI`."

"**Key Functions:**"

"*   **`bitstr-to-bitseq`, `bitseq-to-bitstr`**: Convert between binary strings and sequences.\n*   **`bitseq-to-number`, `long-to-bitseq`**: Convert between binary sequences and numbers (handling large numbers with `BigInt`).\n*   **`bitseq-to-bitset`, `bitset-to-bitseq`**: Convert between binary sequences and `BitSet`.\n*   **`bitset-to-bytes`, `bytes-to-bitset`**: Convert between `BitSet` and byte arrays.\n*   **`bitstr?`**: Checks if a string is a valid binary string (contains only '0' and '1').\n*   **`input-stream`**: Creates an `InputStream` from various binary representations.\n*   **`output-stream`**: Creates an `OutputStream` from various binary representations.\n*   **`channel`**: Creates a `ByteChannel` from various binary representations.\n*   **`bitstr`, `bitseq`, `bitset`, `bytes`, `number` (definvoke functions)**: These are the primary entry points for converting any supported object into its respective binary representation, leveraging the `IBinary` protocol."

[[:section {:title "Usage Pattern:" :link "merged-plans-slop-summary-std-lib-bin-summary-md-usage-pattern"}]]

"This module is essential for any application requiring low-level binary data handling, network communication, file I/O, or cryptographic operations where precise control over data representation and efficient processing are critical. It abstracts away much of the boilerplate Java interop for NIO buffers and binary conversions, providing a more idiomatic Clojure interface."

[[:code {:lang "clojure"} ";; Example: Creating and writing to a ByteBuffer\n(require '[std.lib.bin :as bin])\n\n(def my-buffer (bin/int-buffer 4)) ; Creates an IntBuffer with capacity 4\n(bin/buffer-write my-buffer (int-array [10 20 30 40]))\n\n;; Example: Converting a number to a bit sequence and then to bytes\n(require '[std.lib.bin.type :as btype])\n\n(def num-val 49)\n(def bit-seq (btype/bitseq num-val)) ; => [1 0 0 0 1 1]\n(def byte-arr (btype/bytes num-val)) ; => byte-array with [49]"]]
;; END merged documentation: plans/slop/summary/std_lib_bin_summary.md
