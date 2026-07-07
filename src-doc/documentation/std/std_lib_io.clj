(ns documentation.std-lib-io
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.io` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "API" :link "std.lib.io"}]]

[[:api {:namespace "std.lib.io"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_io_summary.md
;; sha256: 9c44c42998bcbae1328c0952105c171d07650066fa130bfded59d54af3f55d2d
[[:chapter {:title "std.lib.io: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-io-summary-md"}]]

"The `std.lib.io` namespace provides fundamental utilities for working with I/O operations in Clojure, primarily focusing on character sets and type predicates for various I/O stream and reader/writer types. It offers functions to query and manipulate character sets and to check the type of I/O-related objects."

"**Key Features and Concepts:**"

"1.  **Character Set Utilities:**\n    *   `charset:default`: Returns the default character set of the system (e.g., \"UTF-8\").\n    *   `charset:list`: Provides a list of all available character set names on the system.\n    *   `charset`: Constructs a `java.nio.charset.Charset` object from a given string name.\n\n2.  **I/O Type Predicates:**\n    *   `input-stream?`: Checks if an object is an instance of `java.io.InputStream`.\n    *   `output-stream?`: Checks if an object is an instance of `java.io.OutputStream`.\n    *   `reader?`: Checks if an object is an instance of `java.io.Reader`.\n    *   `writer?`: Checks if an object is an instance of `java.io.Writer`."

"**Usage and Importance:**"

"The `std.lib.io` module is a foundational component for any Clojure application that deals with file operations, network communication, or any form of data input/output. It provides basic tools for ensuring correct character encoding and for type-checking I/O objects, which can be crucial for robust and error-free I/O handling. While simple, these utilities are essential for building more complex I/O functionalities within the `foundation-base` project."
;; END merged documentation: plans/slop/summary/std_lib_io_summary.md
