(ns documentation.std-block
  (:require [std.block :as block]
            [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.heal :as heal]
            [std.block.layout :as layout]
            [std.block.navigate :as nav]
            [std.block.parse :as parse]
            [std.block.template :as template])
  (:use code.test))

[[:hero {:title "std.block"
         :subtitle "Code representation, traversal, and manipulation."
         :lead "std.block turns Clojure source into a tree of typed blocks that preserve whitespace, comments, reader macros, and exact token strings. It is the foundation for parsing, layout, healing, templating, and source-level refactoring in foundation-base."
         :actions [{:label "Back to std" :href "index.html" :variant :primary}]}]]

[[:chapter {:title "The Block Model" :link "std.block"}]]

[[:section {:title "Why std.block exists"}]]

"Most Clojure readers discard formatting when they turn source into data. `read-string` gives you values, but it loses comments, extra spaces, and reader macros. `std.block` keeps those details as first-class nodes so code can be analyzed, transformed, and printed back out without destroying the programmer's formatting."

(fact "read-string loses whitespace; std.block keeps it"
  
  (-> (parse/parse-string "[1   2]")
      str)
  => "[1   2]")

(fact "comments survive a parse/string round trip"

  (-> (parse/parse-root ";; hello\n[1 2]")
      str)
  => ";; hello\n[1 2]")

[[:section {:title "The five block types"}]]

"Every block has a `:type`. The five types are:

- **:token** — literals, symbols, keywords, strings, numbers
- **:container** — lists, vectors, maps, sets, and cons forms
- **:void** — whitespace, newlines, tabs, EOF
- **:comment** — `;` line comments
- **:modifier** — uneval (`#_`) and cursor (`#|`) markers"

(fact "a keyword is a token"
  (-> (parse/parse-string ":hello")
      base/block-type)
  => :token)

(fact "a vector is a collection container"
  (-> (parse/parse-string "[1 2]")
      base/block-type)
  => :collection)

(fact "a line comment is its own block type"
  (-> (parse/parse-string ";comment")
      base/block-type)
  => :comment)

[[:section {:title "Block tags refine the type"}]]

"A `:tag` narrows the type. Tokens can be `:long`, `:double`, `:boolean`, `:string`, `:keyword`, `:symbol`, and so on. Containers carry tags like `:list`, `:vector`, `:map`, `:set`, `:quote`, `:deref`, `:meta`, `:fn`, etc."

(fact "token tags distinguish values"
  (-> (parse/parse-string "true")
      base/block-info)
  => (contains {:type :token
                :tag  :boolean
                :string "true"}))

(fact "container tags distinguish collection kinds"
  (-> (parse/parse-string "#{1}")
      base/block-info)
  => (contains {:type :collection
                :tag  :set}))

[[:section {:title "Block info summarises a node"}]]

"`base/block-info` returns the type, tag, string, width, and height of any block. It is the quickest way to inspect a node without reaching into implementation details."

(fact "block-info on a void block"
  (-> (parse/parse-string "\t")
      base/block-info)
  => (contains {:type :void
                :tag  :linetab
                :string "\t"}))

(fact "block-info on a container includes dimensions"
  (-> (parse/parse-string "[1 2 3]")
      base/block-info)
  => (contains {:type :collection
                :tag  :vector
                :width 7
                :height 0}))

[[:section {:title "Tokens carry values"}]]

"Expression blocks (tokens and containers) can produce a Clojure value with `base/block-value`. Tokens that are not expressions, such as whitespace, have no value."

(fact "numeric token value"
  (-> (parse/parse-string "3/5")
      base/block-value)
  => 3/5)

(fact "symbol token value"
  (-> (parse/parse-string "hello/world")
      base/block-value)
  => 'hello/world)

(fact "string token value"
  (-> (parse/parse-string "\"hello\"")
      base/block-value)
  => "hello")

[[:section {:title "Containers have children"}]]

"Containers store a vector of child blocks. The child list preserves every space, newline, and comment that appeared between the delimiters. Use `base/block-children` to access them and `base/replace-children` to create a new container."

(fact "children preserve spaces"
  (->> (parse/parse-string "[1   2]")
       base/block-children
       (map str))
  => '["1" "␣" "␣" "␣" "2"])

(fact "replace-children rebuilds a container"
  (-> (parse/parse-string "[1 2]")
      (base/replace-children [(construct/token 3)
                              (construct/space)
                              (construct/token 4)])
      str)
  => "[3 4]")

[[:section {:title "Void blocks model whitespace"}]]

"`:void` blocks represent spaces, tabs, newlines, carriage returns, form feeds, and EOF. Their string representation is the literal character, but `str` on a space void returns the visible placeholder `␣` to make whitespace observable in tests."

(fact "visible space representation"
  (str (construct/space))
  => "␣")

(fact "newline representation"
  (str (construct/newline))
  => "\\n")

(fact "tab representation"
  (str (construct/tab))
  => "\\t")

[[:section {:title "Comments are first-class blocks"}]]

"A semicolon comment becomes a single `:comment` block. Because it is a node in the tree, comments can be preserved, moved, or deleted during transformations."

(fact "construct a comment block"
  (str (construct/comment ";note"))
  => ";note")

(fact "comments are children of containers"
  (->> (parse/parse-string "[1 ;note\n 2]")
       base/block-children
       (map base/block-type))
  => '(:token :void :comment :void :void :token))

[[:section {:title "Modifiers attach behaviour"}]]

"`:modifier` blocks currently implement `#_` (uneval) and `#|` (cursor). A modifier can modify an accumulator when evaluated. `#_` suppresses the next expression during value extraction."

(fact "uneval is a modifier"
  (-> (parse/parse-string "#_ignored")
      base/block-type)
  => :modifier)

(fact "uneval suppresses the next expression"
  (-> (parse/parse-string "[1 #_2 3]")
      base/block-value)
  => [1 3])

[[:section {:title "Dimensions: width, height, length"}]]

"Every block knows its on-screen width, its height in lines, and the length of its raw string. These dimensions power layout and navigation."

(fact "single-line block dimensions"
  (-> (parse/parse-string "hello")
      ((juxt base/block-width base/block-height base/block-length)))
  => [5 0 5])

(fact "multi-line string dimensions"
  (-> (parse/parse-string "\"a\nb\"")
      ((juxt base/block-width base/block-height)))
  => [2 1])

[[:chapter {:title "Parsing Source Code" :link "std.block.parse"}]]

[[:section {:title "parse-string reads one form"}]]

"`parse/parse-string` reads a single top-level form from a string. It is the simplest entry point for converting source text into a block tree."

(fact "parse a vector"
  (-> (parse/parse-string "[1 2 3]")
      str)
  => "[1 2 3]")

(fact "parse a nested map"
  (-> (parse/parse-string "{:a [1 2]}")
      str)
  => "{:a [1 2]}")

[[:section {:title "parse-root reads many forms"}]]

"`parse/parse-root` reads every form until EOF and wraps them in a `:root` container. Use it when you want to represent an entire file or snippet."

(fact "parse-root keeps multiple forms"
  (-> (parse/parse-root "a b c")
      str)
  => "a b c")

(fact "parse-root preserves inter-form whitespace"
  (-> (parse/parse-root "a\n\n  b")
      str)
  => "a\n\n  b")

[[:section {:title "parse-first returns the first expression"}]]

"`parse/parse-first` skips leading whitespace and returns the first code expression. It is useful when you only care about the initial form."

(fact "parse-first ignores leading whitespace"
  (-> (parse/parse-first "  (+ 1 2)")
      str)
  => "(+ 1 2)")

(fact "parse-first returns a block info map"
  (-> (parse/parse-first "1 2 3")
      base/block-info)
  => (contains {:type :token
                :tag  :long
                :string "1"}))

[[:section {:title "Reading collections"}]]

"The parser recognises the four Clojure collection types: lists `()`, vectors `[]`, maps `{}`, and sets `#{}`. Each becomes a `:container` with the matching tag."

(fact "list value"
  (-> (parse/parse-string "(1 2 3)")
      base/block-value)
  => '(1 2 3))

(fact "vector value"
  (-> (parse/parse-string "[1 2 3]")
      base/block-value)
  => [1 2 3])

(fact "map value"
  (-> (parse/parse-string "{:a 1 :b 2}")
      base/block-value)
  => {:a 1 :b 2})

(fact "set value"
  (-> (parse/parse-string "#{1 2 3}")
      base/block-value)
  => #{1 2 3})

[[:section {:title "Reading cons forms"}]]

"Reader macros that prepend a single expression (`'`, `@`, `` ` ``, `~`, `~@`) and metadata (`^` and `#^`) become `:container` blocks tagged as `:quote`, `:deref`, `:syntax`, `:unquote`, `:unquote-splice`, or `:meta`."

(fact "quote becomes a cons container"
  (-> (parse/parse-string "'x")
      base/block-info)
  => (contains {:type :collection
                :tag  :quote
                :string "'x"}))

(fact "deref value"
  (-> (parse/parse-string "@atom")
      base/block-value)
  => '(deref atom))

(fact "metadata cons"
  (-> (parse/parse-string "^:tag [1]")
      base/block-value)
  => [1])

[[:section {:title "Reading hash dispatch"}]]

"The `#` reader dispatch recognises sets, anonymous functions, regexes, vars, keywords, metadata, eval, conditional read, uneval, and cursor markers."

(fact "anonymous function"
  (-> (parse/parse-string "#(+ 1 %)")
      base/block-value
      first)
  => 'fn*)

(fact "regex token"
  (-> (parse/parse-string "#\"a+b\"")
      base/block-value)
  => #(instance? java.util.regex.Pattern %))

(fact "var quote"
  (-> (parse/parse-string "#'inc")
      base/block-value)
  => '(var inc))

(fact "hash keyword"
  (-> (parse/parse-string "#:person{:name \"A\"}")
      base/block-value)
  => #:person{:name "A"})

[[:section {:title "Preserving whitespace"}]]

"Spaces, tabs, newlines, commas, and EOF are all modelled as void blocks. This is why round-tripping source through `parse-root` followed by `str` gives back the exact original text."

(fact "round-trip keeps extra spaces"
  (-> (parse/parse-root "( 1   2 )")
      str)
  => "( 1   2 )")

(fact "commas become void blocks"
  (-> (parse/parse-string "[1,2]")
      str)
  => "[1,2]")

[[:section {:title "Preserving comments"}]]

"Comments are parsed as `:comment` blocks and kept inside containers. They participate in the child list just like whitespace."

(fact "comment inside a list"
  (-> (parse/parse-string "(1 ;two\n 3)")
      str)
  => "(1 ;two\n 3)")

(fact "comment at top level"
  (-> (parse/parse-root ";; header\n(+ 1 2)")
      str)
  => ";; header\n(+ 1 2)")

[[:section {:title "Reader conditionals"}]]

"`#?` and `#?@` are parsed as `:select` and `:select-splice` containers. Their values are represented as `(? ...)` and `(?-splicing ...)` forms."

(fact "reader conditional"
  (-> (parse/parse-string "#?(:cljs a :clj b)")
      base/block-value)
  => '(? {:cljs a :clj b}))

(fact "reader conditional splice"
  (-> (parse/parse-string "#?@(:cljs [a b])")
      base/block-value)
  => '(?-splicing {:cljs [a b]}))

[[:section {:title "Parsing errors"}]]

"Unmatched delimiters raise reader errors with the offending character. EOF inside a string or collection is also detected."

(fact "unmatched delimiter throws"
  (parse/parse-string "(]")
  => (throws))

(fact "unexpected EOF throws"
  (parse/parse-string "(1 2")
  => (throws))

[[:chapter {:title "Constructing and Modifying Blocks" :link "std.block.construct"}]]

[[:section {:title "The block helper"}]]

"`construct/block` is the universal constructor. Pass it a primitive, a collection, or an existing block and it returns the appropriate block type."

(fact "block from a number"
  (-> (construct/block 42)
      base/block-info)
  => (contains {:type :token
                :tag  :long
                :string "42"}))

(fact "block from a vector"
  (-> (construct/block [1 2 3])
      str)
  => "[1 2 3]")

(fact "block from a map"
  (-> (construct/block {:a 1})
      str)
  => "{:a 1}")

(fact "block passes an existing block through"
  (let [b (construct/block 1)]
    (= b (construct/block b)))
  => true)

[[:section {:title "Constructing tokens"}]]

"`construct/token` builds a token from any Clojure value. `construct/token-from-string` builds a token from source text and infers the value. `construct/string-token` is used internally for strings."

(fact "token from a symbol"
  (str (construct/token 'foo))
  => "foo")

(fact "token from a ratio"
  (str (construct/token 3/4))
  => "3/4")

(fact "token from a string"
  (str (construct/token "hello"))
  => "\"hello\"")

(fact "token from string source"
  (-> (construct/token-from-string "1.5")
      base/block-value)
  => 1.5)

[[:section {:title "Constructing void blocks"}]]

"Use `construct/space`, `construct/newline`, `construct/tab`, and the general `construct/void` to create whitespace. Bulk constructors like `construct/spaces` and `construct/newlines` repeat a void block."

(fact "multiple spaces"
  (apply str (construct/spaces 3))
  => "␣␣␣")

(fact "multiple newlines"
  (apply str (construct/newlines 2))
  => "\\n\\n")

(fact "void from a character"
  (str (construct/void \newline))
  => "\\n")

[[:section {:title "Constructing comments"}]]

"`construct/comment` creates a comment block from a string. The string must begin with `;`."

(fact "valid comment"
  (str (construct/comment "; TODO: fix"))
  => "; TODO: fix")

(fact "invalid comment throws"
  (construct/comment "not a comment")
  => (throws))

[[:section {:title "Constructing containers"}]]

"`construct/container` builds a container from a tag and a vector of children. The tag must be one of the known container tags such as `:list`, `:vector`, `:map`, `:set`, `:quote`, `:meta`, etc."

(fact "empty list container"
  (str (construct/container :list []))
  => "()")

(fact "list with children"
  (-> (construct/container :list [(construct/token '+)
                                  (construct/space)
                                  (construct/token 1)])
      str)
  => "(+ 1)")

(fact "set container"
  (str (construct/container :set [(construct/token 1)]))
  => "#{1}")

[[:section {:title "Adding children"}]]

"`construct/add-child` appends a block to a container's children. It does not insert spacing automatically, so you usually interleave spaces or newlines yourself."

(fact "add-child without spacing"
  (-> (construct/block [])
      (construct/add-child 1)
      (construct/add-child 2)
      str)
  => "[12]")

(fact "add-child with explicit spacing"
  (-> (construct/block [])
      (construct/add-child 1)
      (construct/add-child (construct/space))
      (construct/add-child 2)
      str)
  => "[1 2]")

[[:section {:title "Replacing children"}]]

"`base/replace-children` swaps the entire child vector. This is the low-level primitive used by `add-child` and by navigational updates."

(fact "replace with formatted children"
  (-> (construct/block [1 2])
      (base/replace-children [(construct/token 3)
                              (construct/space)
                              (construct/token 4)])
      str)
  => "[3 4]")

(fact "replace with nested container"
  (-> (construct/block [1])
      (base/replace-children [(construct/block [2 3])])
      str)
  => "[[2 3]]")

[[:section {:title "Building root blocks"}]]

"`construct/root` wraps a sequence of children in a `:root` container. Use it when you need a top-level node that can hold multiple forms."

(fact "root with two forms"
  (-> (construct/root [(construct/token 'a)
                       (construct/space)
                       (construct/token 'b)])
      str)
  => "a b")

(fact "root round-trip"
  (-> (parse/parse-root "a b")
      str)
  => "a b")

[[:section {:title "Reading contents back"}]]

"`construct/contents` converts a container's children back into Clojure forms. It is useful when you want the data inside a container without the container itself."

(fact "contents of a vector"
  (construct/contents (construct/block [1 2 3]))
  => '[1 ␣ 2 ␣ 3])

(fact "contents of a list"
  (construct/contents (construct/block '(+ 1 2)))
  => '(+ ␣ 1 ␣ 2))

[[:section {:title "Indentation helpers"}]]

"`construct/indent-body` re-indents a multi-line container's children by a given offset. `construct/max-width` and `construct/line-width` measure block dimensions for layout tasks."

(fact "indent-body adds leading spaces"
  (-> (construct/block [(construct/token 'x)
                        (construct/newline)
                        (construct/token 'y)])
      (construct/indent-body 2)
      str)
  => "[x\n  y]")

(fact "max-width of a single-line block"
  (construct/max-width (construct/block [1 2 3]))
  => 7)

[[:chapter {:title "Navigating and Transforming Blocks" :link "std.block.navigate"}]]

[[:section {:title "Creating navigators"}]]

"`std.block.navigate` wraps a block tree in a zipper. `nav/navigator` turns a block into a navigator; `nav/parse-string`, `nav/parse-root`, and `nav/parse-first` parse source and return a navigator in one step."

(fact "navigator from a block"
  (-> (nav/navigator (construct/block [1 2 3]))
      nav/value)
  => [1 2 3])

(fact "parse-string returns a navigator"
  (-> (nav/parse-string "[1 2]")
      nav/value)
  => [1 2])

(fact "parse-root returns a navigator"
  (-> (nav/parse-root "a b")
      nav/root-string)
  => "a b")

[[:section {:title "Up, down, left, and right"}]]

"Navigators move through the tree. `down` enters the current container, `up` exits to the parent, and `left`/`right` move between expressions at the same level. The starred variants `left*` and `right*` include whitespace blocks."

(fact "down enters a container"
  (-> (nav/parse-string "(+ 1 2)")
      nav/down
      nav/value)
  => '+)

(fact "right moves to the next expression"
  (-> (nav/parse-string "(+ 1 2)")
      nav/down
      nav/right
      nav/value)
  => 1)

(fact "up returns to the parent"
  (-> (nav/parse-string "(+ 1 2)")
      nav/down
      nav/right
      nav/up
      nav/value)
  => '(+ 1 2))

[[:section {:title "Expression-aware navigation"}]]

"`left` and `right` skip whitespace and stop at the next expression. `left-most` and `right-most` jump to the ends of the current level."

(fact "right skips whitespace"
  (-> (nav/parse-string "(+ 1 2)")
      nav/down
      nav/right
      nav/right
      nav/value)
  => 2)

(fact "right-most reaches the last expression"
  (-> (nav/parse-string "(+ 1 2 3)")
      nav/down
      nav/right-most
      nav/value)
  => 3)

[[:section {:title "Token navigation"}]]

"`left-token`, `right-token`, `prev-token`, and `next-token` move specifically between `:token` blocks. This is handy when you are searching for a keyword or symbol."

(fact "down reaches the first keyword in a map"
  (-> (nav/parse-string "{:a 1 :b 2}")
      nav/down
      nav/value)
  => :a)

(fact "right-token skips to the next token"
  (-> (nav/parse-string "{:a 1 :b 2}")
      nav/down
      nav/right-token
      nav/value)
  => 1)

(fact "right-most-token stops at the last token"
  (-> (nav/parse-string "{:a 1 :b 2}")
      nav/down
      nav/right-most-token
      nav/value)
  => 2)

[[:section {:title "Finding by predicate"}]]

"`find-next`, `find-prev`, `find-next-token`, and `find-prev-token` search the zipper for blocks matching a predicate or token value. They are the backbone of refactoring recipes."

(fact "find-next by value"
  (-> (nav/parse-root "a b c")
      (nav/find-next-token 'b)
      nav/value)
  => 'b)

(fact "next moves to the next expression"
  (-> (nav/parse-string "[1 :key 3]")
      nav/down
      nav/next
      nav/value)
  => :key)

[[:section {:title "Inserting elements"}]]

"`insert-token-to-right`, `insert-token-to-left`, and `insert-token` add new expressions with appropriate spacing. `insert-empty` is used when the container has no expressions."

(fact "insert token to the right"
  (-> (nav/parse-string "[1]")
      nav/down
      (nav/insert-token-to-right 2)
      nav/root-string)
  => "[1 2]")

(fact "insert token to the left"
  (-> (nav/parse-string "[2]")
      nav/down
      (nav/insert-token-to-left 1)
      nav/root-string)
  => "[1 2]")

(fact "insert multiple tokens"
  (-> (nav/parse-string "[1]")
      nav/down
      (nav/insert-all [2 3])
      nav/root-string)
  => "[1 2 3]")

[[:section {:title "Deleting elements"}]]

"`delete` removes the current expression, `delete-left` removes the expression to the left, and `delete-right` removes the expression to the right. `backspace` combines tightening and deletion."

(fact "delete current expression"
  (-> (nav/parse-string "[1 2 3]")
      nav/down
      nav/right
      nav/delete
      nav/root-string)
  => "[1 3]")

(fact "delete left expression"
  (-> (nav/parse-string "[1 2 3]")
      nav/down
      nav/right
      nav/right
      nav/delete-left
      nav/root-string)
  => "[1 3]")

[[:section {:title "Replacing and splicing"}]]

"`replace` swaps the current expression for a single block. `replace-splice` swaps it for a sequence of blocks, expanding them in place. `swap` applies a function to the current value and replaces it."

(fact "replace a symbol"
  (-> (nav/parse-string "[inc 1]")
      nav/down
      (nav/replace (construct/token 'dec))
      nav/root-string)
  => "[dec 1]")

(fact "replace-splice expands a list"
  (-> (nav/parse-string "[1 placeholder 2]")
      nav/down
      nav/right
      (nav/replace-splice [(construct/token 'a)
                           (construct/space)
                           (construct/token 'b)])
      nav/root-string)
  => "[1 a   b 2]")

(fact "swap with inc"
  (-> (nav/parse-string "[1]")
      nav/down
      (nav/swap inc)
      nav/root-string)
  => "[2]")

[[:section {:title "Tightening whitespace"}]]

"`tighten-left`, `tighten-right`, and `tighten` normalise spacing around the cursor. They collapse multiple spaces to a single space and ensure comments are followed by newlines."

(fact "tighten removes extra spaces"
  (-> (nav/parse-string "[1    2]")
      nav/down
      nav/right
      nav/tighten
      nav/root-string)
  => "[1 2]")

(fact "tighten-right normalises trailing space"
  (-> (nav/parse-string "[1 2   ]")
      nav/down
      nav/right
      nav/tighten-right
      nav/root-string)
  => "[1 2]")

[[:section {:title "Line information"}]]

"`nav/line-info` returns the row and column of the current block based on its position in the source. This is used by `code.query` and other tools to report locations."

(fact "line-info for the first token"
  (-> (nav/parse-root "(+ 1 2)")
      nav/down
      nav/line-info)
  => (contains {:row 1 :col 1}))

(fact "line-info tracks newlines"
  (-> (nav/parse-string "(+\n  1)")
      nav/down
      nav/right
      nav/line-info)
  => (contains {:row 2 :col 3}))

[[:chapter {:title "Layout, Healing, Templating, and Real-World Usage" :link "std.block"}]]

[[:section {:title "Layout overview"}]]

"`block/layout` (alias for `std.block.layout/layout-main`) turns a Clojure form into a formatted block tree. It decides whether each collection fits on one line or needs multi-line formatting, then applies specialised rules for bindings, definitions, pairs, and hiccup-like vectors."

(fact "layout keeps a short form on one line"
  (-> (layout/layout-main '[1 2 3])
      str)
  => "[1 2 3]")

(fact "layout splits a long form across lines"
  (-> (layout/layout-main '[1 2 3 4 5 6 7 8 9 10 11 12 13 14 15])
      str)
  => "[1\n 2\n 3\n 4\n 5\n 6\n 7\n 8\n 9\n 10\n 11\n 12\n 13\n 14\n 15]")

[[:section {:title "Layout special forms"}]]

"The layout engine recognises `defn`, `let`, `cond`, `assoc`, `case`, and many `with-*` forms. It aligns bindings into columns and indents bodies consistently."

(fact "let bindings align in two columns"
  (-> (layout/layout-main '(let [a 1
                                 bbbbbbbbbbbbbbbbbbb 2
                                 c 3]
                             (+ a bbbbbbbbbbbbbbbbbbb c)))
      str)
  => "(let [a                   1\n      bbbbbbbbbbbbbbbbbbb 2\n      c                   3]\n  (+ a bbbbbbbbbbbbbbbbbbb c))")

(fact "defn indents the body"
  (-> (layout/layout-main '(defn hello [x]
                             (println x)
                             (+ x 1)))
      str)
  => "(defn hello\n  [x]\n  (println x)\n  (+ x 1))")

[[:section {:title "Healing unbalanced delimiters"}]]

"`block/heal` parses delimiter pairs and repairs mismatched or missing delimiters. It is used by file watchers and LLM output cleanup to keep source files readable."

(fact "heal removes extra closing delimiters"
  (block/heal "(1 2))")
  => "(1 2)")

(fact "heal removes mismatched delimiters"
  (block/heal "(1 2]}")
  => "(1 2)")

[[:section {:title "Rainbow output for debugging"}]]

"`heal/rainbow` prints delimiters in alternating colours so you can spot nesting errors. `heal/print-rainbow` writes the result to `*out*`."

(fact "rainbow wraps output in ANSI codes"
  (-> (heal/rainbow "(1 (2 (3)))")
      string?)
  => true)

(fact "rainbow preserves structure"
  (-> (heal/rainbow "(1 (2))")
      (clojure.string/replace #"\u001B\[[0-9;]*m" ""))
  => "(1 (2))")

[[:section {:title "Code templates"}]]

"`std.block.template` provides fill-in-the-blank code generation. `get-template` parses a snippet and finds `~param` and `~@param` holes. `fill-template` substitutes values for those holes."

(fact "get-template finds parameters"
  (-> (template/get-template "(defn ~name [~@args] ~body)")
      :params)
  => '[(unquote name) (unquote-splicing args) (unquote body)])

(fact "fill-template substitutes values"
  (let [t (template/get-template "(defn ~name [~@args] ~body)")]
    (template/fill-template t {'name 'greet
                               'args '[x y]
                               'body '(+ x y)}))
  => "(defn greet [x y] (+ x y))")

(fact "fill-template with splice"
  (let [t (template/get-template "[~@items]")]
    (template/fill-template t {'items [1 2 3]}))
  => "[1 2 3]")

[[:section {:title "Real-world usage: code.query"}]]

"`code.query` is the source-pattern matching layer used across foundation-base. It converts files and strings into `std.block.navigate` navigators, then matches, selects, and modifies forms with patterns. `code.query/context-zloc` is the entry point."

(fact "code.query selects matching forms"
  (require '[code.query :as q])
  (->> (q/$ {:string "(defn a []) (defn b [])"}
            (defn _ _))
       (map str)
       vec)
  => ["(defn a [])" "(defn b [])"])

(fact "code.query replaces matched forms"
  (require '[std.block.construct :as construct])
  (-> (q/$ {:string "(defn a [])"}
           (defn _ _)
           (fn [zloc]
             (nav/replace zloc (construct/block '(defn a [x])))))
      nav/root-string)
  => "(defn a [x])")

[[:section {:title "Real-world usage: code.framework"}]]

"`code.framework/analyse-source-code` parses an entire source file with `nav/parse-root`, then uses `code.query` to find top-level definitions and test facts. This is how the test runner and doc generator understand the codebase."

(fact "parse-root mirrors how code.framework loads a file"
  (-> (nav/parse-root "(ns example)\n\n(defn hello []\n  1)")
      nav/root-string)
  => "(ns example)\n\n(defn hello []\n  1)")

(fact "code.query finds top-level defns"
  (require '[code.query :as q])
  (->> (q/$ {:string "(defn a [])\n(defn b [x])"}
            (defn _ _))
       count)
  => 2)

[[:section {:title "Real-world usage: std.pretty"}]]

"`std.pretty` pretty-prints values by routing them through `block/layout` and then adding rainbow delimiters. This gives the REPL output consistent indentation and visually balanced parentheses."

(fact "layout produces pretty-printed output"
  (-> (layout/layout-main {:a [1 2 3]
                           :b [4 5 6]
                           :c [7 8 9]
                           :d [10 11 12]})
      str)
  => "{:a [1 2 3]\n :b [4 5 6]\n :c [7 8 9]\n :d [10 11 12]}")

(fact "rainbow can wrap layout output"
  (-> (layout/layout-main '(+ 1 (+ 2 (+ 3))))
      str
      heal/rainbow
      string?)
  => true)

[[:section {:title "Real-world usage: healing and cleanup"}]]

"`std.make.project/file-watcher-heal` runs `block/heal` on changed source files. `indigo.server.api_prompt` runs LLM-generated DSL output through `block/heal` before using it. Both rely on the same simple contract: broken string in, balanced string out."

(fact "heal removes extra closing delimiters"
  (block/heal "(:? ()\n    (+ 1))) (+ 2)\n    nil {})")
  => "(:? ()\n    (+ 1) (+ 2)\n    nil {})")

(fact "heal cleans up mismatched brackets"
  (block/heal "(+ 1 2]}")
  => "(+ 1 2)")

[[:section {:title "Recipe: a tiny refactoring tool"}]]

"Combining parsing, navigation, and replacement makes it easy to write small refactorings. The example below rewrites `(if (not x) then else)` into `(if-not x then else)`."

(fact "rewrite if-not"
  (let [source "(if (not (> x 0)) :small :big)"
        nav    (nav/parse-string source)]
    (-> (nav/replace nav
                     (let [form (nav/value nav)
                           [_ [_ pred] then & else] form]
                       (construct/block (apply list 'if-not pred then else))))
        nav/root-string))
  => "(if-not (> x 0) :small :big)")

(fact "insert a docstring placeholder"
  (-> (nav/parse-string "(defn hello [x])")
      nav/down
      nav/right
      (nav/insert-token-to-right "docstring")
      nav/root-string)
  => "(defn hello \"docstring\" [x])")

[[:chapter {:title "Reference" :link "std.block"}]]

[[:section {:title "Public façade"}]]

"`std.block` interns the most commonly used functions from the sub-namespaces. Most day-to-day work can be done through this single require."

[[:api {:namespace "std.block"
        :only [block? expression? container? modifier?
               type tag string length width height
               prefixed suffixed verify value value-string
               children info
               heal
               block void space spaces newline newlines tab tabs
               comment uneval cursor contents container root max-width
               parse-string parse-root parse-first
               layout
               void? code? space? linebreak? linespace? eof?
               comment? token? container? modifier?]}]]

[[:section {:title "Base predicates and accessors"}]]

"`std.block.base` defines the block taxonomy and the generic accessors used by every other namespace."

[[:api {:namespace "std.block.base"
        :only [block? expression? container? modifier?
               block-type block-tag block-string block-value
               block-length block-width block-height
               block-prefixed block-suffixed block-verify
               block-value-string block-children replace-children
               block-info]}]]

[[:section {:title "Construction"}]]

"`std.block.construct` builds blocks from Clojure data and from raw components."

[[:api {:namespace "std.block.construct"
        :only [block token token-from-string string-token
               container root contents
               void space spaces newline newlines tab tabs
               comment uneval cursor
               add-child replace-children
               max-width line-width indent-body]}]]

[[:section {:title "Parsing"}]]

"`std.block.parse` converts source strings into block trees."

[[:api {:namespace "std.block.parse"
        :only [parse-string parse-root parse-first
               read-dispatch -parse
               parse-token parse-keyword parse-comment
               parse-collection parse-cons parse-hash
               eof-block? delimiter-block?]}]]

[[:section {:title "Navigation"}]]

"`std.block.navigate` provides the zipper API for editing block trees."

[[:api {:namespace "std.block.navigate"
        :only [navigator navigator? parse-string parse-root parse-first
               root-string
               up down left right left* right*
               prev next find-prev find-next
               left-token right-token prev-token next-token
               left-most right-most left-most-token right-most-token
               insert-token insert-token-to-left insert-token-to-right
               insert-all insert-newline insert-space
               delete delete-left delete-right backspace
               replace replace-splice swap update-children
               tighten tighten-left tighten-right
               line-info]}]]

[[:section {:title "Layout and healing"}]]

"`std.block.layout` formats forms; `std.block.heal` repairs delimiters."

[[:api {:namespace "std.block.layout"
        :only [layout-main layout-default-fn layout-annotate
               layout-spec-fn layout-hiccup-like]}]]

[[:api {:namespace "std.block.heal"
        :only [heal rainbow print-rainbow]}]]

[[:section {:title "Templates"}]]

"`std.block.template` generates code from snippets with unquote holes."

[[:api {:namespace "std.block.template"
        :only [get-template get-template-params fill-template]}]]

;; BEGIN merged documentation: guides/std.block.md
;; sha256: 22c920588555fc7bde66da12f2a239bb31df7a66d5bb4b3bd2c16edde8822e2b
[[:chapter {:title "std.block Guide" :link "merged-guides-std-block-md"}]]

"`std.block` allows you to treat source code as data structure (\"blocks\") that preserves formatting, whitespace, and comments. This is essential for building formatters, linters, and refactoring tools."

[[:section {:title "Core Concepts" :link "merged-guides-std-block-md-core-concepts"}]]

"- **Block**: The atom of the system. Can be a token (symbol, keyword), a container (list, vector), or void (whitespace, comment).\n- **Construct**: Functions to build blocks programmatically.\n- **Layout**: Engine to render blocks back to string with specific formatting rules."

[[:section {:title "Usage" :link "merged-guides-std-block-md-usage"}]]

[[:subsection {:title "Scenarios" :link "merged-guides-std-block-md-scenarios"}]]

[[:subsubsection {:title "1. Programmatic Code Generation" :link "merged-guides-std-block-md-1-programmatic-code-generation"}]]

"Instead of building code with lists and then pretty-printing (which loses control over exact formatting), you can build blocks."

[[:code {:lang "clojure"} "(require '[std.block :as block])\n\n;; Create a `(+ 1 2)` block\n(def b (block/container :list\n         [(block/token '+)\n          (block/space)\n          (block/token 1)\n          (block/space)\n          (block/token 2)]))\n\n(block/string b) ;; => \"(+ 1 2)\""]]

[[:subsubsection {:title "2. Layout Customization" :link "merged-guides-std-block-md-2-layout-customization"}]]

"You can control how a block is printed by setting its layout spec."

[[:code {:lang "clojure"} ";; Create a vector that MUST be on one line\n(def v (block/container :vector\n         [(block/token 1) (block/space) (block/token 2)]))\n\n;; Layout usually decides based on width, but we can force it?\n;; Note: Layout logic is internal, but you can inspect the result of `layout`.\n\n(block/string (block/layout v))"]]

"To implement custom formatting rules (like `cond` or `let` binding alignment), you would typically extend the `std.block.layout` multimethods or manipulate the block structure before layout."

[[:subsubsection {:title "3. Parsing and Analysis" :link "merged-guides-std-block-md-3-parsing-and-analysis"}]]

"Distinguishing between code and \"void\" (whitespace/comments) is key for analysis tools."

[[:code {:lang "clojure"} "(def root (block/parse-string \"(+ 1 1) ;; comment\"))\n\n(def children (block/children root))\n\n;; Filter only meaningful code\n(filter block/code? children)\n;; => (#blk{:string \"(+ 1 1)\" ...})\n\n;; Find comments\n(filter block/comment? children)\n;; => (#blk{:string \";; comment\" ...})"]]

[[:subsubsection {:title "4. Manipulating the Block Tree" :link "merged-guides-std-block-md-4-manipulating-the-block-tree"}]]

"You can traverse and modify the block tree manually, although `code.query` provides a higher-level API for this."

[[:code {:lang "clojure"} ";; Replace all occurrences of 1 with 2 in a block\n(defn replace-one [blk]\n  (if (and (block/token? blk) (= (block/value blk) 1))\n    (block/token 2)\n    (if (block/container? blk)\n      (update blk :children #(mapv replace-one %))\n      blk)))"]]

[[:subsubsection {:title "5. Handling Uneval forms" :link "merged-guides-std-block-md-5-handling-uneval-forms"}]]

"`#_` (uneval) forms are tricky in standard Clojure readers as they disappear. `std.block` preserves them."

[[:code {:lang "clojure"} "(def b (block/parse-string \"#_(+ 1 2)\"))\n(block/type b) ;; => :uneval (or similar modifier type)"]]
;; END merged documentation: guides/std.block.md

;; BEGIN merged documentation: plans/slop/summary/std_block_base_tutorial.md
;; sha256: 6c3659def5b1c0fd569f96b3b967c67604575ada1e30cbb527c6f1c9e7ad5766
[[:chapter {:title "std.block.base Tutorial" :link "merged-plans-slop-summary-std-block-base-tutorial-md"}]]
"### `std.block.base` Tutorial\n\n**Module:** `std.block.base`\n**Source File:** `src/std/block/base.clj`\n**Test File:** `test/std/block/base_test.clj`\n\nThe `std.block.base` module defines the fundamental protocols and core functions for interacting with `std.block` AST nodes. It establishes the basic building blocks and properties that all block types share, such as type, tag, string representation, and dimensions.\n\n#### Core Concepts\n\n*   **`IBlock` Protocol:** The foundational protocol that all `std.block` nodes implement. It defines the basic interface for querying block properties.\n*   **`IBlockExpression` Protocol:** Extends `IBlock` for blocks that have an associated Clojure value.\n*   **`IBlockModifier` Protocol:** Extends `IBlock` for blocks that modify an accumulator (e.g., unevaluated forms).\n*   **`IBlockContainer` Protocol:** Extends `IBlock` for blocks that can contain other blocks (e.g., lists, vectors, maps).\n*   **`*block-types*`:** A dynamic var containing a set of all recognized block types (`:void`, `:token`, `:comment`, `:container`, `:modifier`).\n*   **`*container-tags*`:** A dynamic var mapping container types to their specific tags (e.g., `:collection` to `#{:list :map :set :vector}`).\n*   **`*block-tags*`:** A dynamic var merging `*container-tags*` with tags for other block types.\n*   **`*void-representations*`:** A dynamic var mapping special characters (like `\\space`, `\\newline`) to their void block tags.\n*   **`*container-limits*`:** A dynamic var defining the start and end delimiters for various container types (e.g., `{:list {:start \"(\" :end \")\"}}`).\n\n#### Functions\n\n##### `block?`\n\n`^{:refer std.block.base/block? :added \"3.0\"}`\n\nChecks whether an object is an `IBlock` instance.\n\n```clojure\n(block? (construct/void nil))\n;; => true\n\n(block? (construct/token \"hello\"))\n;; => true\n```\n\n##### `block-type`\n\n`^{:refer std.block.base/block-type :added \"3.0\"}`\n\nReturns the block's type as a keyword (e.g., `:void`, `:token`, `:container`).\n\n```clojure\n(block-type (construct/void nil))\n;; => :void\n\n(block-type (construct/token \"hello\"))\n;; => :token\n```\n\n##### `block-tag`\n\n`^{:refer std.block.base/block-tag :added \"3.0\"}`\n\nReturns the block's specific tag as a keyword (e.g., `:eof`, `:linespace`, `:symbol`).\n\n```clojure\n(block-tag (construct/void nil))\n;; => :eof\n\n(block-tag (construct/void \\space))\n;; => :linespace\n```\n\n##### `block-string`\n\n`^{:refer std.block.base/block-string :added \"3.0\"}`\n\nReturns the raw string representation of the block as it would appear in the source file.\n\n```clojure\n(block-string (construct/token 3/4))\n;; => \"3/4\"\n\n(block-string (construct/void \\space))\n;; => \" \"\n```\n\n##### `block-length`\n\n`^{:refer std.block.base/block-length :added \"3.0\"}`\n\nReturns the total character length of the block's string representation.\n\n```clojure\n(block-length (construct/void))\n;; => 1\n\n(block-length (construct/block [1 2 3 4]))\n;; => 9\n;; (e.g., \"[1 2 3 4]\")\n```\n\n##### `block-width`\n\n`^{:refer std.block.base/block-width :added \"3.0\"}`\n\nReturns the visual width of the block (number of characters on a single line).\n\n```clojure\n(block-width (construct/token 'hello))\n;; => 5\n```\n\n##### `block-height`\n\n`^{:refer std.block.base/block-height :added \"3.0\"}`\n\nReturns the height of the block (number of lines it spans).\n\n```clojure\n(block-height (construct/block\n               ^:list [(construct/newline)\n                       (construct/newline)]))\n;; => 2\n```\n\n##### `block-prefixed`\n\n`^{:refer std.block.base/block-prefixed :added \"3.0\"}`\n\nReturns the length of any starting characters (e.g., `(` for a list, `[` for a vector).\n\n```clojure\n(block-prefixed (construct/block #{}))\n;; => 2\n;; (e.g., for a set like #{})\n```\n\n##### `block-suffixed`\n\n`^{:refer std.block.base/block-suffixed :added \"3.0\"}`\n\nReturns the length of any ending characters (e.g., `)` for a list, `]` for a vector).\n\n```clojure\n(block-suffixed (construct/block #{}))\n;; => 1\n;; (e.g., for a set like #{})\n```\n\n##### `block-verify`\n\n`^{:refer std.block.base/block-verify :added \"3.0\"}`\n\nChecks that the block has correct internal data and structure.\n\n```clojure\n;; Example from test code, but no direct assertion provided.\n;; This function likely returns true for valid blocks.\n;; (block-verify (construct/token \"valid\"))\n;; => true\n```\n\n##### `expression?`\n\n`^{:refer std.block.base/expression? :added \"3.0\"}`\n\nChecks if the block has a Clojure value associated with it (i.e., implements `IBlockExpression`).\n\n```clojure\n(expression? (construct/token 1.2))\n;; => true\n```\n\n##### `block-value`\n\n`^{:refer std.block.base/block-value :added \"3.0\"}`\n\nReturns the actual Clojure value represented by an expression block.\n\n```clojure\n(block-value (construct/token 1.2))\n;; => 1.2\n```\n\n##### `block-value-string`\n\n`^{:refer std.block.base/block-value-string :added \"3.0\"}`\n\nReturns the string representation from which the block's value was generated. This can differ from `block-string` for special forms.\n\n```clojure\n(block-value-string (parse/parse-string \"#(+ 1 ::2)\"))\n;; => \"#(+ 1 (keyword \":2\"))\"\n```\n\n##### `modifier?`\n\n`^{:refer std.block.base/modifier? :added \"3.0\"}`\n\nChecks if the block is of type `IBlockModifier`.\n\n```clojure\n(modifier? (construct/uneval))\n;; => true\n```\n\n##### `block-modify`\n\n`^{:refer std.block.base/block-modify :added \"3.0\"}`\n\nAllows a modifier block to modify an accumulator. Used in parsing and transformation.\n\n```clojure\n(block-modify (construct/uneval) [1 2] 'ANYTHING)\n;; => [1 2]\n```\n\n##### `container?`\n\n`^{:refer std.block.base/container? :added \"3.0\"}`\n\nDetermines whether a block has children (i.e., implements `IBlockContainer`).\n\n```clojure\n(container? (parse/parse-string \"[1 2 3]\"))\n;; => true\n\n(container? (parse/parse-string \" \"))\n;; => false\n```\n\n##### `block-children`\n\n`^{:refer std.block.base/block-children :added \"3.0\"}`\n\nReturns a sequence of child blocks within a container block.\n\n```clojure\n(->> (block-children (parse/parse-string \"[1   2]\"))\n     (map block-string))\n;; => (\"1\" \"   \" \"2\")\n```\n\n##### `replace-children`\n\n`^{:refer std.block.base/replace-children :added \"3.0\"}`\n\nReplaces the children of a container block with a new sequence of children.\n\n```clojure\n(->> (replace-children (construct/block [])\n                       (conj (vec (block-children (construct/block [1 2]))) \n                             (construct/void \\space)\n                             (construct/block [3 4])))\n     str)\n;; => \"[1 2 [3 4]]\"\n```\n\n##### `block-info`\n\n`^{:refer std.block.base/block-info :added \"3.0\"}`\n\nReturns a map containing basic information about the block, including its type, tag, string, height, and width.\n\n```clojure\n(block-info (construct/token true))\n;; => {:type :token, :tag :boolean, :string \"true\", :height 0, :width 4}\n\n(block-info (construct/void \\tab))\n;; => {:type :void, :tag :linetab, :string \"\\t\", :height 0, :width 4}\n```\n"
;; END merged documentation: plans/slop/summary/std_block_base_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/std_block_check_tutorial.md
;; sha256: 3ae70dc884d67518045a9e08a85690e3f59a57da545b90ff1986db24fff57112
[[:chapter {:title "std.block.check Tutorial" :link "merged-plans-slop-summary-std-block-check-tutorial-md"}]]
"### `std.block.check` Tutorial\n\n**Module:** `std.block.check`\n**Source File:** `src/std/block/check.clj`\n**Test File:** `test/std/block/check_test.clj`\n\nThe `std.block.check` module provides utility functions for classifying characters and Clojure forms based on their syntactic role within `std.block`'s parsing and construction logic. It defines predicates for various types of characters (whitespace, delimiters, linebreaks) and forms (tokens, collections, void elements).\n\n#### Core Concepts\n\n*   **`*boundaries*`:** A dynamic var containing a set of characters that act as boundaries in Clojure syntax (e.g., space, colon, semicolon, parentheses, brackets, braces).\n*   **`*linebreaks*`:** A dynamic var containing a set of characters that represent line breaks (`\\newline`, `\\return`, `\\formfeed`).\n*   **`*delimiters*`:** A dynamic var containing a set of characters that act as delimiters for collections (`}`, `]`, `)`, `(`, `[`, `{`).\n*   **`*void-checks*`:** A dynamic var mapping void block tags (e.g., `:eof`, `:linetab`) to their respective predicate functions.\n*   **`*token-checks*`:** A dynamic var mapping token block tags (e.g., `:nil`, `:boolean`, `:symbol`) to their respective predicate functions.\n*   **`*collection-checks*`:** A dynamic var mapping collection block tags (e.g., `:list`, `:map`, `:set`, `:vector`) to their respective predicate functions.\n\n#### Functions\n\n##### `boundary?`\n\n`^{:refer std.block.check/boundary? :added \"3.0\"}`\n\nReturns `true` if a character is considered a boundary character in Clojure syntax.\n\n```clojure\n(boundary? (first \"[\"))\n;; => true\n\n(boundary? (first \"\"\"))\n;; => true\n```\n\n##### `whitespace?`\n\n`^{:refer std.block.check/whitespace? :added \"3.0\"}`\n\nReturns `true` if a character is a whitespace character (including spaces, tabs, newlines).\n\n```clojure\n(whitespace? \\space)\n;; => true\n```\n\n##### `comma?`\n\n`^{:refer std.block.check/comma? :added \"3.0\"}`\n\nReturns `true` if a character is a comma.\n\n```clojure\n(comma? (first \",\"))\n;; => true\n```\n\n##### `linebreak?`\n\n`^{:refer std.block.check/linebreak? :added \"3.0\"}`\n\nReturns `true` if a character is a linebreak character.\n\n```clojure\n(linebreak? \\newline)\n;; => true\n```\n\n##### `delimiter?`\n\n`^{:refer std.block.check/delimiter? :added \"3.0\"}`\n\nReturns `true` if a character is a collection delimiter (e.g., `(`, `)`, `[`, `]`, `{`, `}`).\n\n```clojure\n(delimiter? (first \")\"))\n;; => true\n```\n\n##### `voidspace?`\n\n`^{:refer std.block.check/voidspace? :added \"3.0\"}`\n\nDetermines if an input character represents a \"void space\" (whitespace or comma).\n\n```clojure\n(voidspace? \\newline)\n;; => true\n```\n\n##### `linetab?`\n\n`^{:refer std.block.check/linetab? :added \"3.0\"}`\n\nChecks if a character is a tab character.\n\n```clojure\n(linetab? (first \"\\t\"))\n;; => true\n```\n\n##### `linespace?`\n\n`^{:refer std.block.check/linespace? :added \"3.0\"}`\n\nReturns `true` if a character is a whitespace character that is *not* a linebreak or a tab.\n\n```clojure\n(linespace? \\space)\n;; => true\n```\n\n##### `voidspace-or-boundary?`\n\n`^{:refer std.block.check/voidspace-or-boundary? :added \"3.0\"}`\n\nChecks if a character is either a void space or a boundary character.\n\n```clojure\n(->> (map voidspace-or-boundary? (concat *boundaries*\n                                         *linebreaks*))\n     (every? true?))\n;; => true\n```\n\n##### `tag`\n\n`^{:refer std.block.check/tag :added \"3.0\"}`\n\nTakes a map of checks (predicate functions) and an input, returning the tag (key) of the first predicate that returns `true`.\n\n```clojure\n(tag *void-checks* \\space)\n;; => :linespace\n\n(tag *collection-checks* [])\n;; => :vector\n```\n\n##### `void-tag`\n\n`^{:refer std.block.check/void-tag :added \"3.0\"}`\n\nReturns the void tag associated with a character (e.g., `:linebreak` for `\\newline`).\n\n```clojure\n(void-tag \\newline)\n;; => :linebreak\n```\n\n##### `void?`\n\n`^{:refer std.block.check/void? :added \"3.0\"}`\n\nDetermines if a character corresponds to a void block type.\n\n```clojure\n(void? \\newline)\n;; => true\n```\n\n##### `token-tag`\n\n`^{:refer std.block.check/token-tag :added \"3.0\"}`\n\nReturns the token tag associated with a Clojure form (e.g., `:symbol` for a symbol, `:boolean` for `true`).\n\n```clojure\n(token-tag 'hello)\n;; => :symbol\n```\n\n##### `token?`\n\n`^{:refer std.block.check/token? :added \"3.0\"}`\n\nDetermines if a Clojure form is a token type.\n\n```clojure\n(token? 3/4)\n;; => true\n```\n\n##### `collection-tag`\n\n`^{:refer std.block.check/collection-tag :added \"3.0\"}`\n\nReturns the collection tag associated with a Clojure form (e.g., `:vector` for `[]`, `:map` for `{}`).\n\n```clojure\n(collection-tag [])\n;; => :vector\n```\n\n##### `collection?`\n\n`^{:refer std.block.check/collection? :added \"3.0\"}`\n\nDetermines if a Clojure form is a collection type.\n\n```clojure\n(collection? {})\n;; => true\n```\n\n##### `comment?`\n\n`^{:refer std.block.check/comment? :added \"3.0\"}`\n\nDetermines if a string is a comment (starts with `;`).\n\n```clojure\n(comment? \"hello\")\n;; => false\n\n(comment? \";hello\")\n;; => true\n```"
;; END merged documentation: plans/slop/summary/std_block_check_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/std_block_construct_tutorial.md
;; sha256: 3cc5b89f138c4794284180b4936c13109121e736f812b0f97eeb8620e38c2d5b
[[:chapter {:title "std.block.construct Tutorial" :link "merged-plans-slop-summary-std-block-construct-tutorial-md"}]]
"### `std.block.construct` Tutorial\n\n**Module:** `std.block.construct`\n**Source File:** `src/std/block/construct.clj`\n**Test File:** `test/std/block/construct_test.clj`\n\nThe `std.block.construct` module provides functions for programmatically creating `std.block` AST nodes. These functions are essential for building block structures from Clojure data, which can then be manipulated, transformed, and eventually rendered back into code strings. It offers constructors for various block types, including void blocks, tokens, comments, and containers.\n\n#### Core Concepts\n\n*   **`*tags*`:** A dynamic var consolidating tags for different block types (void, token, collection, comment, meta, cons, literal, macro, modifier).\n*   **`+space+`, `+newline+`, `+return+`, `+formfeed+`:** Pre-defined `std.block` instances for common void characters, optimizing their creation.\n*   **`void-lookup`:** A map for quickly retrieving pre-defined void blocks.\n\n#### Functions\n\n##### `void`\n\n`^{:refer std.block.construct/void :added \"3.0\"}`\n\nCreates a void block. Void blocks represent non-code elements like spaces, newlines, or comments.\n\n```clojure\n(str (void))\n;; => \"␣\"\n\n(str (void \\newline))\n;; => \"\\n\"\n```\n\n##### `space`\n\n`^{:refer std.block.construct/space :added \"3.0\"}`\n\nCreates a single space block.\n\n```clojure\n(str (space))\n;; => \"␣\"\n```\n\n##### `spaces`\n\n`^{:refer std.block.construct/spaces :added \"3.0\"}`\n\nCreates a sequence of `n` space blocks.\n\n```clojure\n(apply str (spaces 5))\n;; => \"␣␣␣␣␣\"\n```\n\n##### `tab`\n\n`^{:refer std.block.construct/tab :added \"3.0\"}`\n\nCreates a single tab block.\n\n```clojure\n(str (tab))\n;; => \"\\t\"\n```\n\n##### `tabs`\n\n`^{:refer std.block.construct/tabs :added \"3.0\"}`\n\nCreates a sequence of `n` tab blocks.\n\n```clojure\n(apply str (tabs 5))\n;; => \"\\t\\t\\t\\t\\t\"\n```\n\n##### `newline`\n\n`^{:refer std.block.construct/newline :added \"3.0\"}`\n\nCreates a single newline block.\n\n```clojure\n(str (newline))\n;; => \"\\n\"\n```\n\n##### `newlines`\n\n`^{:refer std.block.construct/newlines :added \"3.0\"}`\n\nCreates a sequence of `n` newline blocks.\n\n```clojure\n(apply str (newlines 5))\n;; => \"\\n\\n\\n\\n\\n\"\n```\n\n##### `comment`\n\n`^{:refer std.block.construct/comment :added \"3.0\"}`\n\nCreates a comment block from a string. The string must start with `;`.\n\n```clojure\n(str (comment \";hello\"))\n;; => \";hello\"\n\n;; Throws exception if string is not a valid comment\n;; (str (comment \"hello\"))\n;; => ExceptionInfo: \"Not a valid comment string.\"\n```\n\n##### `token-dimensions`\n\n`^{:refer std.block.construct/token-dimensions :added \"3.0\"}`\n\nReturns the `[width height]` of a token based on its tag and string representation.\n\n```clojure\n(token-dimensions :regexp \"#\\\"hello\\nworld\\\"\")\n;; => [6 1]\n\n(token-dimensions :regexp \"#\\\"hello\\nworld\\n\\\"\")\n;; => [15 0]\n```\n\n##### `string-token`\n\n`^{:refer std.block.construct/string-token :added \"3.0\"}`\n\nConstructs a token block specifically for Clojure string literals, including quotes and handling newlines within the string.\n\n```clojure\n(str (string-token \"hello\"))\n;; => \"\\\"hello\\\"\"\n\n(str (string-token \"hello\\nworld\"))\n;; => \"\\\"hello\\\\nworld\\\"\"\n```\n\n##### `token`\n\n`^{:refer std.block.construct/token :added \"3.0\"}`\n\nCreates a token block from a Clojure form (symbol, number, keyword, etc.). It automatically determines the correct tag and string representation.\n\n```clojure\n(str (token 'abc))\n;; => \"abc\"\n\n(str (token 123))\n;; => \"123\"\n```\n\n##### `token-from-string`\n\n`^{:refer std.block.construct/token-from-string :added \"3.0\"}`\n\nCreates a token block by reading a string input. This is useful for creating tokens from raw text.\n\n```clojure\n(str (token-from-string \"abc\"))\n;; => \"abc\"\n\n(str (token-from-string \"123\"))\n;; => \"123\"\n```\n\n##### `container-checks`\n\n`^{:refer std.block.construct/container-checks :added \"3.0\"}`\n\nPerforms validation checks for a container block based on its tag, children, and properties. This is an internal helper.\n\n```clojure\n;; No direct test example, but it ensures validity of container construction.\n;; (container-checks :list [(token 1)] {:cons 1})\n;; => true\n```\n\n##### `container`\n\n`^{:refer std.block.construct/container :added \"3.0\"}`\n\nCreates a container block (e.g., list, vector, map, set). It takes a tag and a sequence of child blocks.\n\n```clojure\n(str (container :list [(void) (void)]))\n;; => \"(  )\"\n\n(str (container :vector [(token 1) (space) (token 2)]))\n;; => \"[1 2]\"\n```\n\n##### `uneval`\n\n`^{:refer std.block.construct/uneval :added \"3.0\"}`\n\nCreates a hash-uneval block (`#_`), which is a modifier block used to comment out the next form.\n\n```clojure\n(str (uneval))\n;; => \"#_\"\n```\n\n##### `cursor`\n\n`^{:refer std.block.construct/cursor :added \"3.0\"}`\n\nCreates a cursor block (`|`), used for navigation or indicating a position.\n\n```clojure\n(str (cursor))\n;; => \"|\"\n```\n\n##### `construct-collection`\n\n`^{:refer std.block.construct/construct-collection :added \"3.0\"}`\n\nA multimethod for constructing collection blocks (`:list`, `:vector`, `:set`, `:map`) from Clojure data.\n\n```clojure\n(str (construct-collection [1 2 (void) (void) 3]))\n;; => \"[1 2  3]\"\n\n(str (construct-collection '(1 2 3)))\n;; => \"(1 2 3)\"\n```\n\n##### `construct-children`\n\n`^{:refer std.block.construct/construct-children :added \"3.0\"}`\n\nConstructs a sequence of child blocks from a raw Clojure data structure, automatically inserting spaces and handling different element types.\n\n```clojure\n(mapv str (construct-children [1 (newline) (void) 2]))\n;; => [\"1\" \"\\n\" \"␣\" \"2\"]\n```\n\n##### `block`\n\n`^{:refer std.block.construct/block :added \"3.0\"}`\n\nThe primary entry point for creating any type of `std.block` from a Clojure data element. It dispatches to `token`, `construct-collection`, or returns the element if it's already a block.\n\n```clojure\n(base/block-info (block 1))\n;; => {:type :token, :tag :long, :string \"1\", :height 0, :width 1}\n\n(str (block [1 (newline) (void) 2]))\n;; => \"[1\\n 2]\"\n```\n\n##### `add-child`\n\n`^{:refer std.block.construct/add-child :added \"3.0\"}`\n\nAdds a child element to an existing container block.\n\n```clojure\n(-> (block [])\n    (add-child 1)\n    (add-child 2)\n    (str))\n;; => \"[1 2]\"\n```\n\n##### `empty`\n\n`^{:refer std.block.construct/empty :added \"3.0\"}`\n\nConstructs an empty list block `()`.\n\n```clojure\n(str (empty))\n;; => \"()\"\n```\n\n##### `root`\n\n`^{:refer std.block.construct/root :added \"3.0\"}`\n\nConstructs a root block, which is a special container that typically represents the top-level of a parsed file.\n\n```clojure\n(str (root '[a b]))\n;; => \"a b\"\n```\n\n##### `contents`\n\n`^{:refer std.block.construct/contents :added \"3.0\"}`\n\nReads out the contents of a container block, returning a Clojure data structure.\n\n```clojure\n(contents (block [1 (space) 2 (space) 3]))\n;; => '[1 ␣ 2 ␣ 3]\n```"
;; END merged documentation: plans/slop/summary/std_block_construct_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/std_block_grid_tutorial.md
;; sha256: 4fa3c69200b4b298e20a1d1b45bebd2dc993ddb19674145e5dd41784562fec9f
[[:chapter {:title "std.block.grid Tutorial" :link "merged-plans-slop-summary-std-block-grid-tutorial-md"}]]
"### `std.block.grid` Tutorial\n\n**Module:** `std.block.grid`\n**Source File:** `src/std/block/grid.clj`\n**Test File:** `test/std/block/grid_test.clj`\n\nThe `std.block.grid` module provides advanced functionality for formatting and indenting `std.block` AST nodes. It's designed to take a raw block structure and apply a set of rules to produce a \"gridded\" or well-formatted code string, handling line breaks, indentation, and comment placement. This module is crucial for pretty-printing and code generation where consistent formatting is required.\n\n#### Core Concepts\n\n*   **`*bind-length*`:** A dynamic var controlling the length of binding indentation.\n*   **`*indent-length*`:** A dynamic var controlling the base indentation length.\n*   **Indentation Rules:** The `grid` function takes a map of rules that define how different forms (e.g., `if-let`, `do`, `let`) should be indented. These rules can specify `indent`, `bind`, and `scope`.\n    *   `indent`: The base indentation level.\n    *   `bind`: The number of binding forms to consider for special indentation.\n    *   `scope`: A vector or map defining how child scopes should be indented.\n\n#### Functions\n\n##### `trim-left`\n\n`^{:refer std.block.grid/trim-left :added \"3.0\"}`\n\nRemoves leading whitespace nodes from a sequence of blocks.\n\n```clojure\n(->> (trim-left [(construct/space)\n                 :a\n                 (construct/space)])\n     (mapv str))\n;; => [\":a\" \"␣\"]\n```\n\n##### `trim-right`\n\n`^{:refer std.block.grid/trim-right :added \"3.0\"}`\n\nRemoves trailing whitespace nodes from a sequence of blocks.\n\n```clojure\n(->> (trim-right [(construct/space)\n                  :a\n                  (construct/space)])\n     (mapv str))\n;; => [\"␣\" \":a\"]\n```\n\n##### `split-lines`\n\n`^{:refer std.block.grid/split-lines :added \"3.0\"}`\n\nSplits a sequence of blocks into sub-sequences, where each sub-sequence represents a line, retaining linebreak nodes.\n\n```clojure\n(split-lines [:a :b (construct/newline) :c :d])\n;; => [[:a :b]\n;;     [(construct/newline) :c :d]]\n```\n\n##### `remove-starting-spaces`\n\n`^{:refer std.block.grid/remove-starting-spaces :added \"3.0\"}`\n\nRemoves redundant spaces at the beginning of lines, especially after linebreaks.\n\n```clojure\n(remove-starting-spaces [[(construct/newline)\n                          (construct/space)\n                          (construct/space) :a :b]\n                           [(construct/newline) (construct/space) :c :d]])\n;; => [[(construct/newline) :a :b]\n;;     [(construct/newline) :c :d]]\n```\n\n##### `adjust-comments`\n\n`^{:refer std.block.grid/adjust-comments :added \"3.0\"}`\n\nAdds additional newlines after comments to ensure proper formatting and readability.\n\n```clojure\n(->> (adjust-comments [(construct/comment \";hello\") :a])\n     (mapv str))\n;; => [\";hello\" \"\\n\" \":a\"]\n```\n\n##### `remove-extra-linebreaks`\n\n`^{:refer std.block.grid/remove-extra-linebreaks :added \"3.0\"}`\n\nRemoves redundant or excessive linebreak nodes from a sequence of lines.\n\n```clojure\n(remove-extra-linebreaks [[:a]\n                            [(construct/newline)]\n                            [(construct/newline)]\n                            [(construct/newline)]\n                            [:b]])\n;; => [[:a]\n;;     [(construct/newline)]\n;;     [:b]]\n```\n\n##### `grid-scope`\n\n`^{:refer std.block.grid/grid-scope :added \"3.0\"}`\n\nCalculates the grid scope for child nodes based on the parent scope. This is an internal helper for indentation logic.\n\n```clojure\n(grid-scope [{0 1} 1])\n;; => [{0 1} 0]\n```\n\n##### `grid-rules`\n\n`^{:refer std.block.grid/grid-rules :added \"3.0\"}`\n\nCreates indentation rules for the current block based on its tag, symbol, parent scope, and a global rules map.\n\n```clojure\n(grid-rules :list nil nil nil)\n;; => {:indent 0, :bind 0, :scope []}\n\n(grid-rules :vector nil nil nil)\n;; => {:indent 0, :bind 0, :scope [0]}\n\n(grid-rules :list 'add [1] nil)\n;; => {:indent 1, :bind 0, :scope [0]}\n\n(grid-rules :list 'if nil '{if {:indent 1}})\n;; => {:indent 1, :bind 0, :scope []}\n```\n\n##### `indent-bind`\n\n`^{:refer std.block.grid/indent-bind :added \"3.0\"}`\n\nReturns the number of lines to indent for binding forms within a block, based on the `bind` rule.\n\n```clojure\n(indent-bind [[(construct/token 'if-let)]\n              [(construct/newline)]\n              [(construct/newline) (construct/block '[i (pos? 0)])]\n              [(construct/newline) (construct/block '(+ i 1))]]\n             1)\n;; => 2\n\n(indent-bind [[(construct/token 'if-let)]\n              [(construct/newline)]\n              [(construct/newline) (construct/block '[i (pos? 0)])]\n              [(construct/newline) (construct/block '(+ i 1))]]\n             0)\n;; => 0\n```\n\n##### `indent-lines`\n\n`^{:refer std.block.grid/indent-lines :added \"3.0\"}`\n\nIndents a sequence of lines based on a given anchor and indentation rule.\n\n```clojure\n(-> (indent-lines [[(construct/token 'if-let)]\n                   [(construct/newline)]\n                   [(construct/newline) (construct/block '[i (pos? 0)])]\n                   [(construct/newline) (construct/block '(+ i 1))]]\n                  1\n                  {:indent 1\n                   :bind 1})\n    (construct/contents))\n;; => '([if-let]\n;;      (\\n ␣ ␣ ␣ ␣)\n;;      (\\n ␣ ␣ ␣ ␣ [i (pos? 0)])\n;;      (\\n ␣ ␣ (+ i 1)))\n```\n\n##### `grid`\n\n`^{:refer std.block.grid/grid :added \"3.0\"}`\n\nThe main function for formatting a container block. It applies indentation rules and scope to produce a well-formatted block structure.\n\n```clojure\n(-> (construct/block ^:list ['if-let\n                             (construct/newline)\n                             (construct/newline) (construct/block '[i (pos? 0)])\n                             (construct/newline) (construct/block '(+ i 1))])\n    (grid 1 {:rules {'if-let {:indent 1\n                              :bind 1}}})\n    (construct/contents))\n;; => '(if-let\n;;      \\n ␣ ␣ ␣ ␣ ␣\n;;      \\n ␣ ␣ ␣ ␣ ␣ [i (pos? 0)]\n;;      \\n ␣ ␣ (+ i 1)))\n```"
;; END merged documentation: plans/slop/summary/std_block_grid_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/std_block_parse_tutorial.md
;; sha256: 254d7dd97105b381122d6768c8d9e1f0c16aba5bd972578c28624bd7449dfc8a
[[:chapter {:title "std.block.parse Tutorial" :link "merged-plans-slop-summary-std-block-parse-tutorial-md"}]]
"### `std.block.parse` Tutorial\n\n**Module:** `std.block.parse`\n**Source File:** `src/std/block/parse.clj`\n**Test File:** `test/std/block/parse_test.clj`\n\nThe `std.block.parse` module is responsible for parsing Clojure code strings into `std.block` AST nodes. It acts as the core parser, dispatching to various parsing methods based on the initial character of a form. This module leverages `std.block.reader` for character-level input and `std.block.construct` for building the AST nodes.\n\n#### Core Concepts\n\n*   **`*end-delimiter*`:** A dynamic var used to track the expected closing delimiter during parsing of collections.\n*   **`*symbol-allowed*`:** A dynamic var defining characters allowed within symbols.\n*   **`*dispatch-options*`:** A map that dispatches parsing logic based on the first character encountered (e.g., `(` for lists, `#` for hash forms).\n*   **`-parse` multimethod:** The central parsing function, extended for different dispatch keys (e.g., `:void`, `:token`, `:list`, `:hash`).\n*   **`*hash-options*` and `*hash-dispatch*`:** Maps defining how different hash-prefixed forms (e.g., `#{`, `#_`, `#?`) are parsed.\n\n#### Functions\n\n##### `read-dispatch`\n\n`^{:refer std.block.parse/read-dispatch :added \"3.0\"}`\n\nDispatches parsing logic based on the first character of a form. It returns a keyword indicating the type of form to be parsed.\n\n```clojure\n(read-dispatch \\tab)\n;; => :void\n\n(read-dispatch (first \"#\"))\n;; => :hash\n```\n\n##### `-parse`\n\n`^{:refer std.block.parse/-parse :added \"3.0\"}`\n\nThe extendable parsing multimethod. It takes a `reader` and returns a `std.block` AST node. This function is the core of the parsing process.\n\n```clojure\n(base/block-info (-parse (reader/create \":a\")))\n;; => {:type :token, :tag :keyword, :string \":a\", :height 0, :width 2}\n\n(base/block-info (-parse (reader/create \"\\\"\\\\n\\\"\")))\n;; => {:type :token, :tag :string, :string \"\\\"\\\\n\\\"\", :height 1, :width 1}\n```\n\n##### `parse-void`\n\n`^{:refer std.block.parse/parse-void :added \"3.0\"}`\n\nReads a void block (e.g., space, newline, tab) from the reader.\n\n```clojure\n(->> (reader/read-repeatedly (reader/create \" \\t\\n\\f\")\n                             parse-void\n                             eof-block?)\n     (take 5)\n     (map str))\n;; => [\"\\u202F\" \"\\t\" \"\\n\" \"\\f\"]\n```\n\n##### `parse-comment`\n\n`^{:refer std.block.parse/parse-comment :added \"3.0\"}`\n\nReads a comment block from the reader.\n\n```clojure\n(-> (reader/create \";this is a comment\")\n    parse-comment\n    (base/block-info))\n;; => {:type :comment, :tag :comment, :string \";this is a comment\", :height 0, :width 18}\n```\n\n##### `parse-token`\n\n`^{:refer std.block.parse/parse-token :added \"3.0\"}`\n\nReads a token block (e.g., symbol, number, string) from the reader.\n\n```clojure\n(-> (reader/create \"abc\")\n    (parse-token)\n    (base/block-value))\n;; => 'abc\n\n(-> (reader/create \"3/5\")\n    (parse-token)\n    (base/block-value))\n;; => 3/5\n```\n\n##### `parse-keyword`\n\n`^{:refer std.block.parse/parse-keyword :added \"3.0\"}`\n\nReads a keyword block from the reader, handling both simple and namespaced keywords.\n\n```clojure\n(-> (reader/create \":a/b\")\n    (parse-keyword)\n    (base/block-value))\n;; => :a/b\n\n(-> (reader/create \"::hello\")\n    (parse-keyword)\n    (base/block-value))\n;; => (keyword \":hello\")\n```\n\n##### `parse-reader`\n\n`^{:refer std.block.parse/parse-reader :added \"3.0\"}`\n\nReads a character literal (e.g., `\\c`) from the reader.\n\n```clojure\n(-> (reader/create \"\\\\c\")\n    (parse-reader)\n    (base/block-info))\n;; => (contains {:type :token, :tag :char, :string \"\\\\c\"})\n```\n\n##### `read-string-data`\n\n`^{:refer std.block.parse/read-string-data :added \"3.0\"}`\n\nReads the content of a string literal from the reader, handling escape sequences and newlines.\n\n```clojure\n(read-string-data (reader/create \"\\\"hello\\\"\"))\n;; => \"hello\"\n```\n\n##### `eof-block?`\n\n`^{:refer std.block.parse/eof-block? :added \"3.0\"}`\n\nChecks if a block represents the end-of-file.\n\n```clojure\n(eof-block? (-parse (reader/create \"\")))\n;; => true\n```\n\n##### `delimiter-block?`\n\n`^{:refer std.block.parse/delimiter-block? :added \"3.0\"}`\n\nChecks if a block represents a closing delimiter.\n\n```clojure\n(delimiter-block?\n (binding [*end-delimiter* (first \")\")]\n   (-parse (reader/create \")\"))))\n;; => true\n```\n\n##### `read-whitespace`\n\n`^{:refer std.block.parse/read-whitespace :added \"3.0\"}`\n\nReads a sequence of whitespace characters from the reader and returns them as a vector of void blocks.\n\n```clojure\n(count (read-whitespace (reader/create \"   \")))\n;; => 3\n```\n\n##### `parse-non-expressions`\n\n`^{:refer std.block.parse/parse-non-expressions :added \"3.0\"}`\n\nParses whitespace and non-expression blocks until the next expression block is found.\n\n```clojure\n(str (parse-non-expressions (reader/create \" \\na\")))\n;; => \"[(\\u202F \\n) a]\"\n```\n\n##### `read-start`\n\n`^{:refer std.block.parse/read-start :added \"3.0\"}`\n\nHelper function to consume and verify starting characters of a form (e.g., `(` for a list, `~@` for unquote-splicing).\n\n```clojure\n(read-start (reader/create \"~@\") \"~#\")\n;; => (throws)\n```\n\n##### `read-collection`\n\n`^{:refer std.block.parse/read-collection :added \"3.0\"}`\n\nReads all child blocks within a collection, respecting the start and end delimiters.\n\n```clojure\n(->> (read-collection (reader/create \"(1 2 3 4 5)\") \"(\" (first \")\"))\n     (apply str))\n;; => \"1\\u202F2\\u202F3\\u202F4\\u202F5\"\n```\n\n##### `read-cons`\n\n`^{:refer std.block.parse/read-cons :added \"3.0\"}`\n\nHelper method for reading \"cons\" forms (e.g., `@x`, `'x`, `^x`).\n\n```clojure\n(->> (read-cons (reader/create \"@hello\") \"@\")\n     (map base/block-string))\n;; => '(\"hello\")\n\n(->> (read-cons (reader/create \"^hello {}\") \"^\" 2)\n     (map base/block-string))\n;; => '(\"hello\" \" \" \"{}\")\n```\n\n##### `parse-collection`\n\n`^{:refer std.block.parse/parse-collection :added \"3.0\"}`\n\nParses a collection block (list, vector, map, set, fn, root) from the reader.\n\n```clojure\n(-> (parse-collection (reader/create \"#(+ 1 2 3 4)\") :fn)\n    (base/block-value))\n;; => '(fn* [] (+ 1 2 3 4))\n\n(-> (parse-collection (reader/create \"(1 2 3 4)\") :list)\n    (base/block-value))\n;; => '(1 2 3 4)\n\n(-> (parse-collection (reader/create \"[1 2 3 4]\") :vector)\n    (base/block-value))\n;; => [1 2 3 4]\n\n(-> (parse-collection (reader/create \"{1 2 3 4}\") :map)\n    (base/block-value))\n;; => {1 2, 3 4}\n\n(-> (parse-collection (reader/create \"#{1 2 3 4}\") :set)\n    (base/block-value))\n;; => #{1 4 3 2}\n```\n\n##### `parse-cons`\n\n`^{:refer std.block.parse/parse-cons :added \"3.0\"}`\n\nParses a \"cons\" block (deref, meta, quote, syntax, unquote, unquote-splice, select, select-splice, var, hash-keyword, hash-meta, hash-eval).\n\n```clojure\n(-> (parse-cons (reader/create \"~hello\") :unquote)\n    (base/block-value))\n;; => '(unquote hello)\n\n(-> (parse-cons (reader/create \"~@hello\") :unquote-splice)\n    (base/block-value))\n;; => '(unquote-splicing hello)\n\n(-> (parse-cons (reader/create \"^tag {:a 1}\") :meta)\n    (base/block-value)\n    ((juxt meta identity)))\n;; => [{:tag 'tag} {:a 1}]\n\n(-> (parse-cons (reader/create \"@hello\") :deref)\n    (base/block-value))\n;; => '(deref hello)\n\n(-> (parse-cons (reader/create \"`hello\") :syntax)\n    (base/block-value))\n;; => '(quote std.block.parse-test/hello)\n```\n\n##### `parse-unquote`\n\n`^{:refer std.block.parse/parse-unquote :added \"3.0\"}`\n\nParses a block starting with `~` (unquote or unquote-splice).\n\n```clojure\n(-> (parse-unquote (reader/create \"~hello\"))\n    (base/block-value))\n;; => '(unquote hello)\n\n(-> (parse-unquote (reader/create \"~@hello\"))\n    (base/block-value))\n;; => '(unquote-splicing hello)\n```\n\n##### `parse-select`\n\n`^{:refer std.block.parse/parse-select :added \"3.0\"}`\n\nParses a block starting with `#?` (reader conditional or reader conditional splicing).\n\n```clojure\n(-> (parse-select (reader/create \"#?(:cljs a)\"))\n    (base/block-value))\n;; => '(? {:cljs a})\n\n(-> (parse-select (reader/create \"#?@(:cljs a)\"))\n    (base/block-value))\n;; => '(?-splicing {:cljs a})\n```\n\n##### `parse-hash-uneval`\n\n`^{:refer std.block.parse/parse-hash-uneval :added \"3.0\"}`\n\nParses a hash-uneval block (`#_`).\n\n```clojure\n(str (parse-hash-uneval (reader/create \"#_\")))\n;; => \"#_\"\n```\n\n##### `parse-hash-cursor`\n\n`^{:refer std.block.parse/parse-hash-cursor :added \"3.0\"}`\n\nParses a hash-cursor block (`#|`).\n\n```clojure\n(str (parse-hash-cursor (reader/create \"#|\")))\n;; => \"|\"\n```\n\n##### `parse-hash`\n\n`^{:refer std.block.parse/parse-hash :added \"3.0\"}`\n\nParses a block starting with `#` (hash forms like sets, fn literals, regex, metadata, etc.).\n\n```clojure\n(-> (parse-hash (reader/create \"#{1 2 3}\"))\n    (base/block-value))\n;; => #{1 2 3}\n\n(-> (parse-hash (reader/create \"#(+ 1 2)\"))\n    (base/block-value))\n;; => '(fn* [] (+ 1 2))\n\n(-> (parse-hash (reader/create \"#\\\"hello\\\"\"))\n    (base/block-value))\n;; => #\"hello\"\n\n(-> (parse-hash (reader/create \"#^hello {}\"))\n    (base/block-value))\n;; => (with-meta {} {:tag 'hello})\n\n(-> (parse-hash (reader/create \"#\\'hello\"))\n    (base/block-value))\n;; => '(var hello)\n\n(-> (parse-hash (reader/create \"#=(list 1 2 3)\"))\n    (base/block-value))\n;; => '(1 2 3)\n\n(-> (parse-hash (reader/create \"#?(:clj true)\"))\n    (base/block-value))\n;; => '(? {:clj true})\n\n(-> (parse-hash (reader/create \"#?@(:clj [1 2 3])\"))\n    (base/block-value))\n;; => '(?-splicing {:clj [1 2 3]})\n\n(-> (parse-hash (reader/create \"#:hello {:a 1 :b 2}\"))\n    (base/block-value))\n;; => #:hello{:b 2, :a 1}\n\n(-> (parse-hash (reader/create \"#inst \\\"2018-08-06T06:01:40.682-00:00\\\"\"))\n    (base/block-value))\n;; => #inst \"2018-08-06T06:01:40.682-00:00\"\n```\n\n##### `parse-string`\n\n`^{:refer std.block.parse/parse-string :added \"3.0\"}`\n\nParses a single block from a string input. This is a convenient entry point for parsing.\n\n```clojure\n(-> (parse-string \"#(:b {:b 1})\")\n    (base/block-value))\n;; => '(fn* [] ((keyword \"b\") {(keyword \"b\") 1}))\n```\n\n##### `parse-root`\n\n`^{:refer std.block.parse/parse-root :added \"3.0\"}`\n\nParses a string into a root block, which can contain multiple top-level forms.\n\n```clojure\n(str (parse-root \"a b c\"))\n;; => \"a b c\"\n```\n"
;; END merged documentation: plans/slop/summary/std_block_parse_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/std_block_reader_tutorial.md
;; sha256: 1f61a8d44957116414f5ad2d2845ad7fad76d56b41540fc1e6de8a5ebbe14927
[[:chapter {:title "std.block.reader Tutorial" :link "merged-plans-slop-summary-std-block-reader-tutorial-md"}]]
"### `std.block.reader` Tutorial\n\n**Module:** `std.block.reader`\n**Source File:** `src/std/block/reader.clj`\n**Test File:** `test/std/block/reader_test.clj`\n\nThe `std.block.reader` module provides a set of low-level functions for character-by-character reading and manipulation of input streams, specifically designed for parsing Clojure code. It wraps `clojure.tools.reader.reader-types` to offer a more convenient and block-oriented interface for parsing.\n\n#### Core Concepts\n\n*   **Reader Abstraction:** Provides functions to create, step through, peek at, and unread characters from an input string, mimicking a traditional stream reader.\n*   **Position Tracking:** Integrates with `clojure.tools.reader`'s indexing reader to track line and column numbers.\n\n#### Functions\n\n##### `create`\n\n`^{:refer std.block.reader/create :added \"3.0\"}`\n\nCreates an `IndexingPushbackReader` from a string, suitable for character-by-character reading.\n\n```clojure\n(type (create \"hello world\"))\n;; => clojure.tools.reader.reader_types.IndexingPushbackReader\n```\n\n##### `reader-position`\n\n`^{:refer std.block.reader/reader-position :added \"3.0\"}`\n\nReturns the current `[line column]` position of the reader.\n\n```clojure\n(-> (create \"abc\")\n    step-char\n    step-char\n    reader-position)\n;; => [1 3]\n```\n\n##### `throw-reader`\n\n`^{:refer std.block.reader/throw-reader :added \"3.0\"}`\n\nThrows an `ExceptionInfo` with a message and the current reader position, useful for reporting parsing errors.\n\n```clojure\n(throw-reader (create \"abc\")\n              \"Message\"\n              {:data true})\n;; => (throws)\n```\n\n##### `step-char`\n\n`^{:refer std.block.reader/step-char :added \"3.0\"}`\n\nMoves the reader one character forward and returns the reader itself.\n\n```clojure\n(-> (create \"abc\")\n    step-char\n    read-char\n    str)\n;; => \"b\"\n```\n\n##### `read-char`\n\n`^{:refer std.block.reader/read-char :added \"3.0\"}`\n\nReads a single character from the reader and advances its position.\n\n```clojure\n(->> read-char\n     (read-repeatedly (create \"abc\"))\n     (take 3)\n     (apply str))\n;; => \"abc\"\n```\n\n##### `ignore-char`\n\n`^{:refer std.block.reader/ignore-char :added \"3.0\"}`\n\nReads a single character, ignores it (returns `nil`), and advances the reader's position.\n\n```clojure\n(->> ignore-char\n     (read-repeatedly (create \"abc\"))\n     (take 3)\n     (apply str))\n;; => \"\"\n```\n\n##### `unread-char`\n\n`^{:refer std.block.reader/unread-char :added \"3.0\"}`\n\nPushes a character back onto the reader, effectively moving the reader's position backward.\n\n```clojure\n(-> (create \"abc\")\n    (step-char)\n    (unread-char \\A)\n    (reader/slurp))\n;; => \"Abc\"\n```\n\n##### `peek-char`\n\n`^{:refer std.block.reader/peek-char :added \"3.0\"}`\n\nReturns the next character in the stream without advancing the reader's position.\n\n```clojure\n(->> (read-times (create \"abc\")\n                 peek-char\n                 3)\n     (apply str))\n;; => \"aaa\"\n```\n\n##### `read-while`\n\n`^{:refer std.block.reader/read-while :added \"3.0\"}`\n\nReads characters from the reader as long as a given predicate remains `true`.\n\n```clojure\n(read-while (create \"abcde\")\n            (fn [ch]\n              (not= (str ch) \"d\")))\n;; => \"abc\"\n```\n\n##### `read-until`\n\n`^{:refer std.block.reader/read-until :added \"3.0\"}`\n\nReads characters from the reader until a given predicate becomes `true`.\n\n```clojure\n(read-until (create \"abcde\")\n            (fn [ch]\n              (= (str ch) \"d\")))\n;; => \"abc\"\n```\n\n##### `read-times`\n\n`^{:refer std.block.reader/read-times :added \"3.0\"}`\n\nReads input a specified number of times using a provided reading function.\n\n```clojure\n(->> (read-times (create \"abcdefg\")\n                 #(str (read-char %) (read-char %))\n                 2))\n;; => [\"ab\" \"cd\"]\n```\n\n##### `read-repeatedly`\n\n`^{:refer std.block.reader/read-repeatedly :added \"3.0\"}`\n\nReads input repeatedly until a stop predicate is met.\n\n```clojure\n(->> (read-repeatedly (create \"abcdefg\")\n                      #(str (read-char %) (read-char %))\n                      empty?)\n     (take 5))\n;; => [\"ab\" \"cd\" \"ef\" \"g\"]\n```\n\n##### `read-include`\n\n`^{:refer std.block.reader/read-include :added \"3.0\"}`\n\nReads characters, including those that satisfy a predicate, and returns them along with the first character that *doesn't* satisfy the predicate.\n\n```clojure\n(read-include (create \"  a\")\n              read-char (complement check/voidspace?))\n;; => [[\" \" \" \"] \"a\"]\n```\n\n##### `slurp`\n\n`^{:refer std.block.reader/slurp :added \"3.0\"}`\n\nReads the rest of the input from the reader until EOF.\n\n```clojure\n(reader/slurp (reader/step-char (create \"abc efg\")))\n;; => \"bc efg\"\n```\n\n##### `read-to-boundary`\n\n`^{:refer std.block.reader/read-to-boundary :added \"3.0\"}`\n\nReads characters until a boundary character or a character not allowed by `allowed` is encountered.\n\n```clojure\n(read-to-boundary (create \"abc efg\"))\n;; => \"abc\"\n```"
;; END merged documentation: plans/slop/summary/std_block_reader_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/std_block_summary.md
;; sha256: a7675980544f94d757fc9865489362d3994b2d8e872aaf6a05ea26bc6d5c162c
[[:chapter {:title "std.block: A Comprehensive Summary (including submodules)" :link "merged-plans-slop-summary-std-block-summary-md"}]]
"## std.block: A Comprehensive Summary (including submodules)\n\nThe `std.block` module is a foundational component of the `foundation-base` ecosystem, providing an Abstract Syntax Tree (AST) or \"code-as-data\" abstraction for representing Clojure code. It allows for the parsing, manipulation, and structured representation of code as a hierarchical collection of \"blocks.\" This module is critical for metaprogramming, code analysis, and especially for transpilation processes where code needs to be understood and transformed systematically.\n\n### `std.block` (Main Namespace)\n\nThis namespace orchestrates the functionality of its submodules, providing a unified interface for working with code blocks. It re-exports key functions from its sub-namespaces, making it a convenient entry point for block manipulation.\n\n**Key Re-exported Functions:**\n\n*   From `std.block.base`: `block?`, `expression?`, `type`, `tag`, `string`, `length`, `width`, `height`, `prefixed`, `suffixed`, `verify`, `value`, `value-string`, `children`, `info`.\n*   From `std.block.construct`: `block`, `void`, `space`, `spaces`, `newline`, `newlines`, `tab`, `tabs`, `comment`, `uneval`, `cursor`, `contents`, `container`, `root`.\n*   From `std.block.parse`: `parse-string`, `parse-root`.\n*   From `std.block.type`: `void?`, `space?`, `linebreak?`, `linespace?`, `eof?`, `comment?`, `token?`, `container?`, `modifier?`.\n\n### `std.block.base` (Core Block Definitions and Protocols)\n\nThis sub-namespace defines the fundamental protocols and basic operations that all code blocks adhere to. It establishes the common interface for querying block properties.\n\n**Core Concepts:**\n\n*   **`IBlock` Protocol:** The base protocol for all code blocks, defining methods like `_type`, `_tag`, `_string`, `_length`, `_width`, `_height`, `_prefixed`, `_suffixed`, `_verify`.\n*   **`IBlockExpression` Protocol:** For blocks that have an associated Clojure value, defining `_value` and `_value_string`.\n*   **`IBlockModifier` Protocol:** For blocks that modify an accumulator (e.g., `#_` for unevaluated forms), defining `_modify`.\n*   **`IBlockContainer` Protocol:** For blocks that contain other blocks (e.g., lists, vectors), defining `_children` and `_replace_children`.\n*   **Block Types and Tags:** Blocks are categorized by `:type` (e.g., `:void`, `:token`, `:comment`, `:collection`, `:modifier`) and more specific `:tag` (e.g., `:eof`, `:symbol`, `:list`, `:meta`, `:hash-uneval`).\n*   **`*container-limits*`:** Defines the start and end delimiters and properties for various container types (e.g., `(`, `)`, `[`, `]`, `{`, `}`).\n\n**Key Functions:**\n\n*   **`block?`**: Checks if an object is an `IBlock`.\n*   **`block-*` functions**: Accessors for block properties (`block-type`, `block-tag`, `block-string`, `block-length`, `block-width`, `block-height`, `block-prefixed`, `block-suffixed`, `block-verify`).\n*   **`expression?`**: Checks if a block has a value.\n*   **`block-value`**: Returns the Clojure value of an expression block.\n*   **`block-value-string`**: Returns the `pr-str` representation of the block's value.\n*   **`modifier?`**: Checks if a block is a modifier.\n*   **`block-modify`**: Applies a modifier's logic.\n*   **`container?`**: Checks if a block is a container.\n*   **`block-children`**: Returns the child blocks of a container.\n*   **`replace-children`**: Replaces the children of a container.\n*   **`block-info`**: Returns a map of common block information.\n\n### `std.block.check` (Character and Token Classification)\n\nThis sub-namespace provides utilities for classifying characters and Clojure forms, which is essential for the parsing process.\n\n**Core Concepts:**\n\n*   **Character Properties:** Functions to determine if a character is a boundary, whitespace, comma, linebreak, or delimiter.\n*   **Form Tags:** Functions to categorize Clojure forms as void, token, or collection types.\n\n**Key Functions:**\n\n*   **`boundary?`, `whitespace?`, `comma?`, `linebreak?`, `delimiter?`, `voidspace?`, `linetab?`, `linespace?`, `voidspace-or-boundary?`**: Predicates for character classification.\n*   **`tag`**: Generic function to find a tag based on a set of checks.\n*   **`void-tag`, `void?`**: Classifies and checks for void forms (whitespace, newlines).\n*   **`token-tag`, `token?`**: Classifies and checks for token forms (numbers, symbols, keywords, strings).\n*   **`collection-tag`, `collection?`**: Classifies and checks for collection forms (lists, vectors, maps, sets).\n*   **`comment?`**: Checks if a string is a comment.\n\n### `std.block.construct` (Block Construction)\n\nThis sub-namespace provides functions for programmatically creating various types of code blocks.\n\n**Core Concepts:**\n\n*   **Direct Block Creation:** Functions to create instances of `VoidBlock`, `CommentBlock`, `TokenBlock`, and `ContainerBlock`.\n*   **Special Blocks:** `uneval` (for `#_`) and `cursor` (for `|`) as modifier blocks.\n\n**Key Functions:**\n\n*   **`void`, `space`, `spaces`, `tab`, `tabs`, `newline`, `newlines`**: Create void blocks (whitespace, newlines, tabs).\n*   **`comment`**: Creates a comment block.\n*   **`string-token`, `token`, `token-from-string`**: Create token blocks for various literal types.\n*   **`container`**: Creates a container block (list, vector, map, set, root), handling delimiters and children.\n*   **`uneval`**: Creates a modifier block for `#_` (unevaluated forms).\n*   **`cursor`**: Creates a modifier block for `|` (used as a cursor in editing contexts).\n*   **`construct-collection` (multimethod)**: A multimethod for constructing blocks from Clojure collections (lists, vectors, maps, sets).\n*   **`construct-children`**: Helper to convert a sequence of Clojure forms into a sequence of blocks, inserting correct spacing.\n*   **`block`**: A generic function to construct a block from a Clojure form (token or collection).\n*   **`add-child`**: Adds a child block to a container block.\n*   **`empty`**: Creates an empty list container block.\n*   **`root`**: Creates a special container block representing the root of a code structure.\n*   **`contents`**: Extracts the value representation of a container block's children, ignoring void blocks.\n\n### `std.block.grid` (Code Formatting and Layout)\n\nThis sub-namespace provides algorithms for formatting and arranging code blocks into a readable grid-like structure, handling indentation and line breaks.\n\n**Core Concepts:**\n\n*   **Line-based Formatting:** Operations for splitting blocks into lines, removing extra spaces/linebreaks, and adjusting comments.\n*   **Indentation Rules:** `grid-rules` defines how different container types and symbols should be indented.\n*   **Scope-aware Indentation:** Indentation can be adjusted based on the nesting level and specific rules for forms like `let` or `if`.\n\n**Key Functions:**\n\n*   **`trim-left`, `trim-right`**: Remove void blocks from the ends of sequences.\n*   **`split-lines`**: Splits a sequence of blocks into a sequence of lines based on linebreak blocks.\n*   **`remove-starting-spaces`**: Removes leading space blocks from lines.\n*   **`adjust-comments`**: Ensures comments are followed by newlines.\n*   **`remove-extra-linebreaks`**: Collapses multiple consecutive linebreak blocks into a single one.\n*   **`grid-scope`**: Calculates indentation scope for child nodes.\n*   **`grid-rules`**: Determines indentation and binding rules for a given block type and symbol.\n*   **`indent-bind`**: Calculates the number of lines to bind for indentation.\n*   **`indent-lines`**: Applies indentation to a sequence of lines based on rules.\n*   **`grid`**: The main function for formatting a container block into a grid, using the defined rules for indentation and line breaks.\n\n### `std.block.parse` (Code Parsing)\n\nThis sub-namespace defines the core logic for parsing raw input strings into a tree of `std.block` objects. It leverages `clojure.tools.reader` for low-level character reading.\n\n**Core Concepts:**\n\n*   **`tools.reader` Integration:** Uses `clojure.tools.reader.reader-types/IndexingPushbackReader` for character-by-character input processing, allowing for peeking, reading, and unreading characters.\n*   **Dispatch Mechanism:** Uses `read-dispatch` and a multimethod `-parse` to determine the type of block to parse based on the leading character.\n*   **Recursive Descent Parsing:** Parses complex structures like collections and forms recursively.\n*   **Error Handling:** Throws informative exceptions for parsing errors (e.g., unmatched delimiters, unexpected EOF).\n\n**Key Functions:**\n\n*   **`read-dispatch`**: Determines the block type (`:void`, `:hash`, `:list`, `:token`, etc.) based on the next character.\n*   **`-parse` (multimethod)**: The main dispatch function for parsing different block types.\n    *   **`parse-void`**: Parses whitespace, newlines, commas.\n    *   **`parse-comment`**: Parses a comment line.\n    *   **`parse-token`**: Parses numbers, symbols, booleans, ratios.\n    *   **`parse-keyword`**: Parses keywords (`:key`, `::key`).\n    *   **`parse-reader`**: Parses reader forms (e.g., `\\c`).\n    *   **`read-string-data`**: Parses string literals, handling newlines and escaped characters.\n    *   **`parse-collection`**: Parses collections (`()`, `[]`, `{}`, `#{}`).\n    *   **`parse-cons`**: Parses forms with prefixes (e.g., `'`, `~`, `@`, `#`).\n    *   **`parse-unquote`, `parse-select`, `parse-hash-uneval`, `parse-hash-cursor`, `parse-hash`**: Specific parsers for reader macros and prefixed forms.\n*   **`parse-string`**: Parses an entire string into a single block.\n*   **`parse-root`**: Parses a string into a root block, representing the entire input structure.\n*   **`eof-block?`, `delimiter-block?`**: Check for EOF or delimiter blocks.\n*   **`read-whitespace`**: Reads a sequence of whitespace blocks.\n*   **`parse-non-expressions`**: Parses blocks up to the next expression.\n*   **`read-start`**: Verifies starting delimiters for collections/forms.\n*   **`read-collection`**: Reads all child blocks within a collection, respecting delimiters.\n*   **`read-cons`**: Reads child blocks for cons-like forms (e.g., `'x`, `@y`).\n\n### `std.block.reader` (Low-Level Character Reading)\n\nThis sub-namespace provides an enhanced character-level reader built on top of `clojure.tools.reader`, offering utility functions for detailed input stream manipulation.\n\n**Core Concepts:**\n\n*   **`IndexingPushbackReader`:** Leverages `clojure.tools.reader.reader-types/IndexingPushbackReader` for precise control over the input stream, including tracking line and column numbers.\n*   **Character-level Operations:** Functions for peeking, reading, unreading, and skipping characters.\n*   **Predicate-based Reading:** Functions to read characters until a predicate is met or while a predicate is true.\n\n**Key Functions:**\n\n*   **`create`**: Creates an `IndexingPushbackReader` from a string.\n*   **`reader-position`**: Returns the current line and column of the reader.\n*   **`throw-reader`**: Throws an exception with detailed position information.\n*   **`step-char`**: Reads a character and advances the reader.\n*   **`read-char`**: Reads the next character.\n*   **`ignore-char`**: Skips the next character.\n*   **`unread-char`**: Pushes a character back onto the reader.\n*   **`peek-char`**: Returns the next character without advancing the reader.\n*   **`read-while`**: Reads characters as long as a predicate is true.\n*   **`read-until`**: Reads characters until a predicate is true.\n*   **`read-times`**: Reads characters a specified number of times.\n*   **`read-repeatedly`**: Reads characters repeatedly until a stop condition.\n*   **`read-include`**: Reads characters while accumulating non-expression blocks.\n*   **`slurp`**: Reads the rest of the reader's input as a string.\n*   **`read-to-boundary`**: Reads characters until a boundary character is encountered.\n\n### `std.block.type` (Concrete Block Implementations)\n\nThis sub-namespace defines the concrete `deftype` implementations for the various block types, adhering to the `std.protocol.block` protocols.\n\n**Core Concepts:**\n\n*   **`VoidBlock`**: Represents whitespace, newlines, tabs, and EOF.\n*   **`CommentBlock`**: Represents comments.\n*   **`TokenBlock`**: Represents atomic values like numbers, symbols, keywords, strings.\n*   **`ContainerBlock`**: Represents collections (lists, vectors, maps, sets, root) and forms with prefixes (e.g., `'x`).\n*   **`ModifierBlock`**: Represents reader macro modifiers (e.g., `#_`, `|`).\n*   **Dimensions:** Tracks `width` and `height` of blocks for formatting.\n\n**Key Functions:**\n\n*   **`block-compare`**: A utility for comparing two blocks.\n*   **`void-block?`, `void-block`**: Checks for and constructs `VoidBlock`.\n*   **`space-block?`, `linebreak-block?`, `linespace-block?`, `eof-block?`, `nil-void?`**: Specific predicates for types of void blocks.\n*   **`comment-block?`, `comment-block`**: Checks for and constructs `CommentBlock`.\n*   **`token-block?`, `token-block`**: Checks for and constructs `TokenBlock`.\n*   **`container-width`, `container-height`**: Calculates dimensions for `ContainerBlock`.\n*   **`container-string` (multimethod)**: Generates the string representation for different container types.\n*   **`container-value-string`**: Generates a string representation of the *value* of a container block (used for `block-value-string`).\n*   **`container-block?`, `container-block`**: Checks for and constructs `ContainerBlock`.\n*   **`modifier-block?`, `modifier-block`**: Checks for and constructs `ModifierBlock`.\n\n### `std.block.value` (Extracting Values from Blocks)\n\nThis sub-namespace focuses on extracting the underlying Clojure value from code blocks, applying any modifiers in the process.\n\n**Core Concepts:**\n\n*   **Value Extraction:** Functions to convert a block representation back into a runnable Clojure value.\n*   **Modifier Application:** Correctly applies the logic of modifier blocks (like `#_`) during value extraction.\n\n**Key Functions:**\n\n*   **`apply-modifiers`**: Applies `IBlockModifier` logic to a sequence of blocks.\n*   **`child-values`**: Extracts the Clojure values of child blocks within a container, applying modifiers.\n*   **`root-value`**, **`list-value`**, **`map-value`**, **`set-value`**, **`vector-value`**: Extract values from container blocks.\n*   **`deref-value`**, **`meta-value`**, **`quote-value`**, **`var-value`**, **`hash-keyword-value`**, **`select-value`**, **`select-splice-value`**, **`unquote-value`**, **`unquote-splice-value`**: Extract values from various prefixed forms.\n*   **`from-value-string`**: Uses `read-string` on the `block-value-string` to get the value.\n*   **`*container-values*`**: A dynamic map mapping container tags to the functions that extract their Clojure values.\n\n### Usage Pattern:\n\nThe `std.block` module and its sub-namespaces provide the backbone for:\n*   **Transpilers and Compilers:** Representing and manipulating source code during the conversion to other languages.\n*   **Code Editors and IDEs:** Providing structured editing, syntax highlighting, and formatting capabilities.\n*   **Static Analysis Tools:** Analyzing code structure and properties.\n*   **Metaprogramming and Code Generation:** Programmatically constructing and transforming Clojure code.\n\nBy offering a powerful and finely-grained abstraction over Clojure code structure, `std.block` enables sophisticated processing and transformation of code within the `foundation-base` ecosystem."
;; END merged documentation: plans/slop/summary/std_block_summary.md

;; BEGIN merged documentation: plans/slop/summary/std_block_type_tutorial.md
;; sha256: 04fcf602ceb356c14435c33b59d2ecbaeac2aad9c1b733698a7473c30216e7b8
[[:chapter {:title "std.block.type Tutorial" :link "merged-plans-slop-summary-std-block-type-tutorial-md"}]]
"### `std.block.type` Tutorial\n\n**Module:** `std.block.type`\n**Source File:** `src/std/block/type.clj`\n**Test File:** `test/std/block/type_test.clj`\n\nThe `std.block.type` module defines the various concrete implementations of `std.block` AST nodes (VoidBlock, CommentBlock, TokenBlock, ContainerBlock, ModifierBlock). It also provides predicate functions to check the type of a block and functions to construct new instances of these block types directly. This module is crucial for understanding the internal representation of blocks and for low-level block manipulation.\n\n#### Core Concepts\n\n*   **`*tab-width*`:** A dynamic var controlling the assumed width of a tab character for layout calculations.\n*   **`VoidBlock`:** Represents non-code elements like spaces, newlines, tabs, commas, or EOF signals.\n*   **`CommentBlock`:** Represents single-line comments starting with `;`.\n*   **`TokenBlock`:** Represents literal values, symbols, keywords, numbers, strings, etc.\n*   **`ContainerBlock`:** Represents collections like lists, vectors, maps, and sets, holding other blocks as children.\n*   **`ModifierBlock`:** Represents special forms that modify subsequent forms, like `#_` (uneval) or `|` (cursor).\n\n#### Functions\n\n##### `block-compare`\n\n`^{:refer std.block.type/block-compare :added \"3.0\"}`\n\nCompares two blocks for equality based on their tag and string representation. Returns 0 if equal, a negative number if the first is \"less\" than the second, and a positive number otherwise. This is used for `Comparable` implementation.\n\n```clojure\n(block-compare (construct/void \\space)\n                 (construct/void \\space))\n;; => 0\n```\n\n##### `void-block?`\n\n`^{:refer std.block.type/void-block? :added \"3.0\"}`\n\nChecks if a block is a `VoidBlock` instance.\n\n```clojure\n(void-block? (construct/void))\n;; => true\n```\n\n##### `void-block`\n\n`^{:refer std.block.type/void-block :added \"3.0\"}`\n\nConstructs a new `VoidBlock` instance directly.\n\n```clojure\n(-> (void-block :linespace \\tab 1 0)\n    (base/block-info))\n;; => {:type :void, :tag :linespace, :string \"\\t\", :height 0, :width 1}\n```\n\n##### `space-block?`\n\n`^{:refer std.block.type/space-block? :added \"3.0\"}`\n\nChecks if a block represents a space character.\n\n```clojure\n(space-block? (construct/space))\n;; => true\n```\n\n##### `linebreak-block?`\n\n`^{:refer std.block.type/linebreak-block? :added \"3.0\"}`\n\nChecks if a block represents a linebreak character.\n\n```clojure\n(linebreak-block? (construct/newline))\n;; => true\n```\n\n##### `linespace-block?`\n\n`^{:refer std.block.type/linespace-block? :added \"3.0\"}`\n\nChecks if a block represents a non-linebreak whitespace character (e.g., `\\space`, `\\tab`).\n\n```clojure\n(linespace-block? (construct/space))\n;; => true\n```\n\n##### `eof-block?`\n\n`^{:refer std.block.type/eof-block? :added \"3.0\"}`\n\nChecks if a block represents the end-of-file signal.\n\n```clojure\n(eof-block? (construct/void nil))\n;; => true\n```\n\n##### `nil-void?`\n\n`^{:refer std.block.type/nil-void? :added \"3.0\"}`\n\nChecks if a block is `nil` or a `VoidBlock`.\n\n```clojure\n(nil-void? nil)\n;; => true\n\n(nil-void? (construct/block nil))\n;; => false\n\n(nil-void? (construct/space))\n;; => true\n```\n\n##### `comment-block?`\n\n`^{:refer std.block.type/comment-block? :added \"3.0\"}`\n\nChecks if a block is a `CommentBlock` instance.\n\n```clojure\n(comment-block? (construct/comment \";;hello\"))\n;; => true\n```\n\n##### `comment-block`\n\n`^{:refer std.block.type/comment-block :added \"3.0\"}`\n\nConstructs a new `CommentBlock` instance directly.\n\n```clojure\n(-> (comment-block \";hello\")\n    (base/block-info))\n;; => {:type :comment, :tag :comment, :string \";hello\", :height 0, :width 6}\n```\n\n##### `token-block?`\n\n`^{:refer std.block.type/token-block? :added \"3.0\"}`\n\nChecks if a block is a `TokenBlock` instance.\n\n```clojure\n(token-block? (construct/token \"hello\"))\n;; => true\n```\n\n##### `token-block`\n\n`^{:refer std.block.type/token-block :added \"3.0\"}`\n\nConstructs a new `TokenBlock` instance directly.\n\n```clojure\n(base/block-info (token-block :symbol \"abc\" 'abc \"abc\" 3 0))\n;; => {:type :token, :tag :symbol, :string \"abc\", :height 0, :width 3}\n```\n\n##### `container-width`\n\n`^{:refer std.block.type/container-width :added \"3.0\"}`\n\nCalculates the visual width of a container block, considering its children and delimiters.\n\n```clojure\n(container-width (construct/block [1 2 3 4]))\n;; => 9\n```\n\n##### `container-height`\n\n`^{:refer std.block.type/container-height :added \"3.0\"}`\n\nCalculates the height (number of lines) of a container block.\n\n```clojure\n(container-height (construct/block [(construct/newline)\n                                      (construct/newline)]))\n;; => 2\n```\n\n##### `container-string`\n\n`^{:refer std.block.type/container-string :added \"3.0\"}`\n\nReturns the string representation of a container block, including its delimiters and children's string representations. This is a multimethod extending `base/block-tag`.\n\n```clojure\n(container-string (construct/block [1 2 3]))\n;; => \"[1 2 3]\"\n```\n\n##### `container-value-string`\n\n`^{:refer std.block.type/container-value-string :added \"3.0\"}`\n\nReturns the string representation used to generate the *value* of a container block, often used for debugging or internal representation.\n\n```clojure\n(container-value-string (construct/block [::a :b :c]))\n;; => \"[:std.block.type-test/a :b :c]\"\n\n(container-value-string (parse/parse-string \"[::a :b :c]\"))\n;; => \"[(keyword \":a\") (keyword \"b\") (keyword \"c\")]\"\n```\n\n##### `container-block?`\n\n`^{:refer std.block.type/container-block? :added \"3.0\"}`\n\nChecks if a block is a `ContainerBlock` instance.\n\n```clojure\n(container-block? (construct/block []))\n;; => true\n```\n\n##### `container-block`\n\n`^{:refer std.block.type/container-block :added \"3.0\"}`\n\nConstructs a new `ContainerBlock` instance directly.\n\n```clojure\n(-> (container-block :fn [(construct/token '+)\n                            (construct/void)\n                            (construct/token '1)]\n                       (construct/*container-props* :fn))\n    (base/block-value))\n;; => '(fn* [] (+ 1))\n```\n\n##### `modifier-block?`\n\n`^{:refer std.block.type/modifier-block? :added \"3.0\"}`\n\nChecks if a block is a `ModifierBlock` instance.\n\n```clojure\n(modifier-block? (construct/uneval))\n;; => true\n```\n\n##### `modifier-block`\n\n`^{:refer std.block.type/modifier-block :added \"3.0\"}`\n\nConstructs a new `ModifierBlock` instance directly.\n\n```clojure\n(modifier-block :hash-uneval \"#_\" (fn [acc _] acc))\n;; => #std.block.type.ModifierBlock{:tag :hash-uneval, :string \"#_\", :command #function[...]}\n```"
;; END merged documentation: plans/slop/summary/std_block_type_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/std_block_value_tutorial.md
;; sha256: 7fcc09f7e3349a7da79a55f643bcf621d87a6f8f2c52f7aeff11613d74f9522f
[[:chapter {:title "std.block.value Tutorial" :link "merged-plans-slop-summary-std-block-value-tutorial-md"}]]
"### `std.block.value` Tutorial\n\n**Module:** `std.block.value`\n**Source File:** `src/std/block/value.clj`\n**Test File:** `test/std/block/value_test.clj`\n\nThe `std.block.value` module is responsible for extracting the actual Clojure values from `std.block` AST nodes. It provides functions to convert various block types (tokens, collections, special forms) back into their corresponding Clojure data structures, handling modifiers like `#_` (uneval) in the process. This module is crucial for bridging the gap between the AST representation and executable Clojure code.\n\n#### Core Concepts\n\n*   **Value Extraction:** The primary goal is to get the Clojure data represented by a block.\n*   **Modifier Application:** Correctly applies the logic of modifier blocks (like `#_`) during value extraction.\n*   **`*container-values*`:** A dynamic var mapping container tags to functions that extract their Clojure value.\n\n#### Functions\n\n##### `apply-modifiers`\n\n`^{:refer std.block.value/apply-modifiers :added \"3.0\"}`\n\nApplies modifier blocks within a sequence of blocks to an accumulator. For example, `#_` modifiers will remove the subsequent block from the sequence.\n\n```clojure\n(apply-modifiers [(construct/uneval)\n                  (construct/uneval)\n                  1 2 3])\n;; => [3]\n```\n\n##### `child-values`\n\n`^{:refer std.block.value/child-values :added \"3.0\"}`\n\nReturns the Clojure values of the children within a container block, applying any modifiers present.\n\n```clojure\n(child-values (parse/parse-string \"[1 #_2 3]\"))\n;; => [1 3]\n```\n\n##### `root-value`\n\n`^{:refer std.block.value/root-value :added \"3.0\"}`\n\nReturns the Clojure value of a `:root` block, typically as a `(do ...)` form if it contains multiple top-level expressions.\n\n```clojure\n(root-value (parse/parse-string \"#[1 2 3]\"))\n;; => '(do 1 2 3)\n```\n\n##### `from-value-string`\n\n`^{:refer std.block.value/from-value-string :added \"3.0\"}`\n\nReads a Clojure value from the `block-value-string` of a block. This is useful for blocks where the string representation for value generation differs from the raw string.\n\n```clojure\n(from-value-string (parse/parse-string \"(+ 1 1)\"))\n;; => '(+ 1 1)\n```\n\n##### `list-value`\n\n`^{:refer std.block.value/list-value :added \"3.0\"}`\n\nReturns the Clojure `list` value of an `:list` block.\n\n```clojure\n(list-value (parse/parse-string \"(+ 1 1)\"))\n;; => '(+ 1 1)\n```\n\n##### `map-value`\n\n`^{:refer std.block.value/map-value :added \"3.0\"}`\n\nReturns the Clojure `map` value of an `:map` block.\n\n```clojure\n(map-value (parse/parse-string \"{1 2 3 4}\"))\n;; => {1 2, 3 4}\n\n(map-value (parse/parse-string \"{1 2 3}\"))\n;; => (throws)\n```\n\n##### `set-value`\n\n`^{:refer std.block.value/set-value :added \"3.0\"}`\n\nReturns the Clojure `set` value of an `:set` block.\n\n```clojure\n(set-value (parse/parse-string \"#{1 2 3 4}\"))\n;; => #{1 4 3 2}\n```\n\n##### `vector-value`\n\n`^{:refer std.block.value/vector-value :added \"3.0\"}`\n\nReturns the Clojure `vector` value of an `:vector` block.\n\n```clojure\n(vector-value (parse/parse-string \"[1 2 3 4]\"))\n;; => [1 2 3 4]\n```\n\n##### `deref-value`\n\n`^{:refer std.block.value/deref-value :added \"3.0\"}`\n\nReturns the Clojure value of a `:deref` block (e.g., `@atom`).\n\n```clojure\n(deref-value (parse/parse-string \"@hello\"))\n;; => '(deref hello)\n```\n\n##### `meta-value`\n\n`^{:refer std.block.value/meta-value :added \"3.0\"}`\n\nReturns the Clojure value of a `:meta` block (e.g., `^:dynamic x`).\n\n```clojure\n((juxt meta identity)\n (meta-value (parse/parse-string \"^:dynamic {:a 1}\")))\n;; => [{:dynamic true} {:a 1}]\n\n((juxt meta identity)\n (meta-value (parse/parse-string \"^String {:a 1}\")))\n;; => [{:tag 'String} {:a 1}]\n```\n\n##### `quote-value`\n\n`^{:refer std.block.value/quote-value :added \"3.0\"}`\n\nReturns the Clojure value of a `:quote` block (e.g., `'symbol`).\n\n```clojure\n(quote-value (parse/parse-string \"'hello\"))\n;; => '(quote hello)\n```\n\n##### `var-value`\n\n`^{:refer std.block.value/var-value :added \"3.0\"}`\n\nReturns the Clojure value of a `:var` block (e.g., `#'symbol`).\n\n```clojure\n(var-value (parse/parse-string \"#'hello\"))\n;; => '(var hello)\n```\n\n##### `hash-keyword-value`\n\n`^{:refer std.block.value/hash-keyword-value :added \"3.0\"}`\n\nReturns the Clojure value of a `:hash-keyword` block (e.g., `#:prefix{:key value}`).\n\n```clojure\n(hash-keyword-value (parse/parse-string \"#:hello{:a 1 :b 2}\"))\n;; => #:hello{:b 2, :a 1}\n```\n\n##### `select-value`\n\n`^{:refer std.block.value/select-value :added \"3.0\"}`\n\nReturns the Clojure value of a `:select` block (reader conditional, e.g., `#?(:clj x)`).\n\n```clojure\n(select-value (parse/parse-string \"#?(:clj hello)\"))\n;; => '(? {:clj hello})\n```\n\n##### `select-splice-value`\n\n`^{:refer std.block.value/select-splice-value :added \"3.0\"}`\n\nReturns the Clojure value of a `:select-splice` block (reader conditional splicing, e.g., `#?@(:clj x)`).\n\n```clojure\n(select-splice-value (parse/parse-string \"#?@(:clj hello)\"))\n;; => '(?-splicing {:clj hello})\n```\n\n##### `unquote-value`\n\n`^{:refer std.block.value/unquote-value :added \"3.0\"}`\n\nReturns the Clojure value of an `:unquote` block (e.g., `~x`).\n\n```clojure\n(unquote-value (parse/parse-string \"~hello\"))\n;; => '(unquote hello)\n```\n\n##### `unquote-splice-value`\n\n`^{:refer std.block.value/unquote-splice-value :added \"3.0\"}`\n\nReturns the Clojure value of an `:unquote-splice` block (e.g., `~@x`).\n\n```clojure\n(unquote-splice-value (parse/parse-string \"~@hello\"))\n;; => '(unquote-splicing hello)\n```"
;; END merged documentation: plans/slop/summary/std_block_value_tutorial.md
