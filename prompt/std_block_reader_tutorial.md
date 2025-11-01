### `std.block.reader` Tutorial

**Module:** `std.block.reader`
**Source File:** `src/std/block/reader.clj`
**Test File:** `test/std/block/reader_test.clj`

The `std.block.reader` module provides a set of low-level functions for character-by-character reading and manipulation of input streams, specifically designed for parsing Clojure code. It wraps `clojure.tools.reader.reader-types` to offer a more convenient and block-oriented interface for parsing.

#### Core Concepts

*   **Reader Abstraction:** Provides functions to create, step through, peek at, and unread characters from an input string, mimicking a traditional stream reader.
*   **Position Tracking:** Integrates with `clojure.tools.reader`'s indexing reader to track line and column numbers.

#### Functions

##### `create`

`^{:refer std.block.reader/create :added "3.0"}`

Creates an `IndexingPushbackReader` from a string, suitable for character-by-character reading.

```clojure
(type (create "hello world"))
;; => clojure.tools.reader.reader_types.IndexingPushbackReader
```

##### `reader-position`

`^{:refer std.block.reader/reader-position :added "3.0"}`

Returns the current `[line column]` position of the reader.

```clojure
(-> (create "abc")
    step-char
    step-char
    reader-position)
;; => [1 3]
```

##### `throw-reader`

`^{:refer std.block.reader/throw-reader :added "3.0"}`

Throws an `ExceptionInfo` with a message and the current reader position, useful for reporting parsing errors.

```clojure
(throw-reader (create "abc")
              "Message"
              {:data true})
;; => (throws)
```

##### `step-char`

`^{:refer std.block.reader/step-char :added "3.0"}`

Moves the reader one character forward and returns the reader itself.

```clojure
(-> (create "abc")
    step-char
    read-char
    str)
;; => "b"
```

##### `read-char`

`^{:refer std.block.reader/read-char :added "3.0"}`

Reads a single character from the reader and advances its position.

```clojure
(->> read-char
     (read-repeatedly (create "abc"))
     (take 3)
     (apply str))
;; => "abc"
```

##### `ignore-char`

`^{:refer std.block.reader/ignore-char :added "3.0"}`

Reads a single character, ignores it (returns `nil`), and advances the reader's position.

```clojure
(->> ignore-char
     (read-repeatedly (create "abc"))
     (take 3)
     (apply str))
;; => ""
```

##### `unread-char`

`^{:refer std.block.reader/unread-char :added "3.0"}`

Pushes a character back onto the reader, effectively moving the reader's position backward.

```clojure
(-> (create "abc")
    (step-char)
    (unread-char \A)
    (reader/slurp))
;; => "Abc"
```

##### `peek-char`

`^{:refer std.block.reader/peek-char :added "3.0"}`

Returns the next character in the stream without advancing the reader's position.

```clojure
(->> (read-times (create "abc")
                 peek-char
                 3)
     (apply str))
;; => "aaa"
```

##### `read-while`

`^{:refer std.block.reader/read-while :added "3.0"}`

Reads characters from the reader as long as a given predicate remains `true`.

```clojure
(read-while (create "abcde")
            (fn [ch]
              (not= (str ch) "d")))
;; => "abc"
```

##### `read-until`

`^{:refer std.block.reader/read-until :added "3.0"}`

Reads characters from the reader until a given predicate becomes `true`.

```clojure
(read-until (create "abcde")
            (fn [ch]
              (= (str ch) "d")))
;; => "abc"
```

##### `read-times`

`^{:refer std.block.reader/read-times :added "3.0"}`

Reads input a specified number of times using a provided reading function.

```clojure
(->> (read-times (create "abcdefg")
                 #(str (read-char %) (read-char %))
                 2))
;; => ["ab" "cd"]
```

##### `read-repeatedly`

`^{:refer std.block.reader/read-repeatedly :added "3.0"}`

Reads input repeatedly until a stop predicate is met.

```clojure
(->> (read-repeatedly (create "abcdefg")
                      #(str (read-char %) (read-char %))
                      empty?)
     (take 5))
;; => ["ab" "cd" "ef" "g"]
```

##### `read-include`

`^{:refer std.block.reader/read-include :added "3.0"}`

Reads characters, including those that satisfy a predicate, and returns them along with the first character that *doesn't* satisfy the predicate.

```clojure
(read-include (create "  a")
              read-char (complement check/voidspace?))
;; => [[" " " "] "a"]
```

##### `slurp`

`^{:refer std.block.reader/slurp :added "3.0"}`

Reads the rest of the input from the reader until EOF.

```clojure
(reader/slurp (reader/step-char (create "abc efg")))
;; => "bc efg"
```

##### `read-to-boundary`

`^{:refer std.block.reader/read-to-boundary :added "3.0"}`

Reads characters until a boundary character or a character not allowed by `allowed` is encountered.

```clojure
(read-to-boundary (create "abc efg"))
;; => "abc"
```