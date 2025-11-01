### `std.block.parse` Tutorial

**Module:** `std.block.parse`
**Source File:** `src/std/block/parse.clj`
**Test File:** `test/std/block/parse_test.clj`

The `std.block.parse` module is responsible for parsing Clojure code strings into `std.block` AST nodes. It acts as the core parser, dispatching to various parsing methods based on the initial character of a form. This module leverages `std.block.reader` for character-level input and `std.block.construct` for building the AST nodes.

#### Core Concepts

*   **`*end-delimiter*`:** A dynamic var used to track the expected closing delimiter during parsing of collections.
*   **`*symbol-allowed*`:** A dynamic var defining characters allowed within symbols.
*   **`*dispatch-options*`:** A map that dispatches parsing logic based on the first character encountered (e.g., `(` for lists, `#` for hash forms).
*   **`-parse` multimethod:** The central parsing function, extended for different dispatch keys (e.g., `:void`, `:token`, `:list`, `:hash`).
*   **`*hash-options*` and `*hash-dispatch*`:** Maps defining how different hash-prefixed forms (e.g., `#{`, `#_`, `#?`) are parsed.

#### Functions

##### `read-dispatch`

`^{:refer std.block.parse/read-dispatch :added "3.0"}`

Dispatches parsing logic based on the first character of a form. It returns a keyword indicating the type of form to be parsed.

```clojure
(read-dispatch \tab)
;; => :void

(read-dispatch (first "#"))
;; => :hash
```

##### `-parse`

`^{:refer std.block.parse/-parse :added "3.0"}`

The extendable parsing multimethod. It takes a `reader` and returns a `std.block` AST node. This function is the core of the parsing process.

```clojure
(base/block-info (-parse (reader/create ":a")))
;; => {:type :token, :tag :keyword, :string ":a", :height 0, :width 2}

(base/block-info (-parse (reader/create "\"\\n\"")))
;; => {:type :token, :tag :string, :string "\"\\n\"", :height 1, :width 1}
```

##### `parse-void`

`^{:refer std.block.parse/parse-void :added "3.0"}`

Reads a void block (e.g., space, newline, tab) from the reader.

```clojure
(->> (reader/read-repeatedly (reader/create " \t\n\f")
                             parse-void
                             eof-block?)
     (take 5)
     (map str))
;; => ["\u202F" "\t" "\n" "\f"]
```

##### `parse-comment`

`^{:refer std.block.parse/parse-comment :added "3.0"}`

Reads a comment block from the reader.

```clojure
(-> (reader/create ";this is a comment")
    parse-comment
    (base/block-info))
;; => {:type :comment, :tag :comment, :string ";this is a comment", :height 0, :width 18}
```

##### `parse-token`

`^{:refer std.block.parse/parse-token :added "3.0"}`

Reads a token block (e.g., symbol, number, string) from the reader.

```clojure
(-> (reader/create "abc")
    (parse-token)
    (base/block-value))
;; => 'abc

(-> (reader/create "3/5")
    (parse-token)
    (base/block-value))
;; => 3/5
```

##### `parse-keyword`

`^{:refer std.block.parse/parse-keyword :added "3.0"}`

Reads a keyword block from the reader, handling both simple and namespaced keywords.

```clojure
(-> (reader/create ":a/b")
    (parse-keyword)
    (base/block-value))
;; => :a/b

(-> (reader/create "::hello")
    (parse-keyword)
    (base/block-value))
;; => (keyword ":hello")
```

##### `parse-reader`

`^{:refer std.block.parse/parse-reader :added "3.0"}`

Reads a character literal (e.g., `\c`) from the reader.

```clojure
(-> (reader/create "\\c")
    (parse-reader)
    (base/block-info))
;; => (contains {:type :token, :tag :char, :string "\\c"})
```

##### `read-string-data`

`^{:refer std.block.parse/read-string-data :added "3.0"}`

Reads the content of a string literal from the reader, handling escape sequences and newlines.

```clojure
(read-string-data (reader/create "\"hello\""))
;; => "hello"
```

##### `eof-block?`

`^{:refer std.block.parse/eof-block? :added "3.0"}`

Checks if a block represents the end-of-file.

```clojure
(eof-block? (-parse (reader/create "")))
;; => true
```

##### `delimiter-block?`

`^{:refer std.block.parse/delimiter-block? :added "3.0"}`

Checks if a block represents a closing delimiter.

```clojure
(delimiter-block?
 (binding [*end-delimiter* (first ")")]
   (-parse (reader/create ")"))))
;; => true
```

##### `read-whitespace`

`^{:refer std.block.parse/read-whitespace :added "3.0"}`

Reads a sequence of whitespace characters from the reader and returns them as a vector of void blocks.

```clojure
(count (read-whitespace (reader/create "   ")))
;; => 3
```

##### `parse-non-expressions`

`^{:refer std.block.parse/parse-non-expressions :added "3.0"}`

Parses whitespace and non-expression blocks until the next expression block is found.

```clojure
(str (parse-non-expressions (reader/create " \na")))
;; => "[(\u202F \n) a]"
```

##### `read-start`

`^{:refer std.block.parse/read-start :added "3.0"}`

Helper function to consume and verify starting characters of a form (e.g., `(` for a list, `~@` for unquote-splicing).

```clojure
(read-start (reader/create "~@") "~#")
;; => (throws)
```

##### `read-collection`

`^{:refer std.block.parse/read-collection :added "3.0"}`

Reads all child blocks within a collection, respecting the start and end delimiters.

```clojure
(->> (read-collection (reader/create "(1 2 3 4 5)") "(" (first ")"))
     (apply str))
;; => "1\u202F2\u202F3\u202F4\u202F5"
```

##### `read-cons`

`^{:refer std.block.parse/read-cons :added "3.0"}`

Helper method for reading "cons" forms (e.g., `@x`, `'x`, `^x`).

```clojure
(->> (read-cons (reader/create "@hello") "@")
     (map base/block-string))
;; => '("hello")

(->> (read-cons (reader/create "^hello {}") "^" 2)
     (map base/block-string))
;; => '("hello" " " "{}")
```

##### `parse-collection`

`^{:refer std.block.parse/parse-collection :added "3.0"}`

Parses a collection block (list, vector, map, set, fn, root) from the reader.

```clojure
(-> (parse-collection (reader/create "#(+ 1 2 3 4)") :fn)
    (base/block-value))
;; => '(fn* [] (+ 1 2 3 4))

(-> (parse-collection (reader/create "(1 2 3 4)") :list)
    (base/block-value))
;; => '(1 2 3 4)

(-> (parse-collection (reader/create "[1 2 3 4]") :vector)
    (base/block-value))
;; => [1 2 3 4]

(-> (parse-collection (reader/create "{1 2 3 4}") :map)
    (base/block-value))
;; => {1 2, 3 4}

(-> (parse-collection (reader/create "#{1 2 3 4}") :set)
    (base/block-value))
;; => #{1 4 3 2}
```

##### `parse-cons`

`^{:refer std.block.parse/parse-cons :added "3.0"}`

Parses a "cons" block (deref, meta, quote, syntax, unquote, unquote-splice, select, select-splice, var, hash-keyword, hash-meta, hash-eval).

```clojure
(-> (parse-cons (reader/create "~hello") :unquote)
    (base/block-value))
;; => '(unquote hello)

(-> (parse-cons (reader/create "~@hello") :unquote-splice)
    (base/block-value))
;; => '(unquote-splicing hello)

(-> (parse-cons (reader/create "^tag {:a 1}") :meta)
    (base/block-value)
    ((juxt meta identity)))
;; => [{:tag 'tag} {:a 1}]

(-> (parse-cons (reader/create "@hello") :deref)
    (base/block-value))
;; => '(deref hello)

(-> (parse-cons (reader/create "`hello") :syntax)
    (base/block-value))
;; => '(quote std.block.parse-test/hello)
```

##### `parse-unquote`

`^{:refer std.block.parse/parse-unquote :added "3.0"}`

Parses a block starting with `~` (unquote or unquote-splice).

```clojure
(-> (parse-unquote (reader/create "~hello"))
    (base/block-value))
;; => '(unquote hello)

(-> (parse-unquote (reader/create "~@hello"))
    (base/block-value))
;; => '(unquote-splicing hello)
```

##### `parse-select`

`^{:refer std.block.parse/parse-select :added "3.0"}`

Parses a block starting with `#?` (reader conditional or reader conditional splicing).

```clojure
(-> (parse-select (reader/create "#?(:cljs a)"))
    (base/block-value))
;; => '(? {:cljs a})

(-> (parse-select (reader/create "#?@(:cljs a)"))
    (base/block-value))
;; => '(?-splicing {:cljs a})
```

##### `parse-hash-uneval`

`^{:refer std.block.parse/parse-hash-uneval :added "3.0"}`

Parses a hash-uneval block (`#_`).

```clojure
(str (parse-hash-uneval (reader/create "#_")))
;; => "#_"
```

##### `parse-hash-cursor`

`^{:refer std.block.parse/parse-hash-cursor :added "3.0"}`

Parses a hash-cursor block (`#|`).

```clojure
(str (parse-hash-cursor (reader/create "#|")))
;; => "|"
```

##### `parse-hash`

`^{:refer std.block.parse/parse-hash :added "3.0"}`

Parses a block starting with `#` (hash forms like sets, fn literals, regex, metadata, etc.).

```clojure
(-> (parse-hash (reader/create "#{1 2 3}"))
    (base/block-value))
;; => #{1 2 3}

(-> (parse-hash (reader/create "#(+ 1 2)"))
    (base/block-value))
;; => '(fn* [] (+ 1 2))

(-> (parse-hash (reader/create "#\"hello\""))
    (base/block-value))
;; => #"hello"

(-> (parse-hash (reader/create "#^hello {}"))
    (base/block-value))
;; => (with-meta {} {:tag 'hello})

(-> (parse-hash (reader/create "#\'hello"))
    (base/block-value))
;; => '(var hello)

(-> (parse-hash (reader/create "#=(list 1 2 3)"))
    (base/block-value))
;; => '(1 2 3)

(-> (parse-hash (reader/create "#?(:clj true)"))
    (base/block-value))
;; => '(? {:clj true})

(-> (parse-hash (reader/create "#?@(:clj [1 2 3])"))
    (base/block-value))
;; => '(?-splicing {:clj [1 2 3]})

(-> (parse-hash (reader/create "#:hello {:a 1 :b 2}"))
    (base/block-value))
;; => #:hello{:b 2, :a 1}

(-> (parse-hash (reader/create "#inst \"2018-08-06T06:01:40.682-00:00\""))
    (base/block-value))
;; => #inst "2018-08-06T06:01:40.682-00:00"
```

##### `parse-string`

`^{:refer std.block.parse/parse-string :added "3.0"}`

Parses a single block from a string input. This is a convenient entry point for parsing.

```clojure
(-> (parse-string "#(:b {:b 1})")
    (base/block-value))
;; => '(fn* [] ((keyword "b") {(keyword "b") 1}))
```

##### `parse-root`

`^{:refer std.block.parse/parse-root :added "3.0"}`

Parses a string into a root block, which can contain multiple top-level forms.

```clojure
(str (parse-root "a b c"))
;; => "a b c"
```
