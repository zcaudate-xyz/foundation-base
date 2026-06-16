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

[[:chapter {:title "1. The Block Model" :link "std.block"}]]

[[:section {:title "1.1 Why std.block exists"}]]

"Most Clojure readers discard formatting when they turn source into data. `read-string` gives you values, but it loses comments, extra spaces, and reader macros. `std.block` keeps those details as first-class nodes so code can be analyzed, transformed, and printed back out without destroying the programmer's formatting."

(fact "read-string loses whitespace; std.block keeps it"
  (-> (parse/parse-string "[1   2]")
      str)
  => "[1   2]")

(fact "comments survive a parse/string round trip"
  (-> (parse/parse-root ";; hello\n[1 2]")
      str)
  => ";; hello\n[1 2]")

[[:section {:title "1.2 The five block types"}]]

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

[[:section {:title "1.3 Block tags refine the type"}]]

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

[[:section {:title "1.4 Block info summarises a node"}]]

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

[[:section {:title "1.5 Tokens carry values"}]]

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

[[:section {:title "1.6 Containers have children"}]]

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

[[:section {:title "1.7 Void blocks model whitespace"}]]

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

[[:section {:title "1.8 Comments are first-class blocks"}]]

"A semicolon comment becomes a single `:comment` block. Because it is a node in the tree, comments can be preserved, moved, or deleted during transformations."

(fact "construct a comment block"
  (str (construct/comment ";note"))
  => ";note")

(fact "comments are children of containers"
  (->> (parse/parse-string "[1 ;note\n 2]")
       base/block-children
       (map base/block-type))
  => '(:token :void :comment :void :void :token))

[[:section {:title "1.9 Modifiers attach behaviour"}]]

"`:modifier` blocks currently implement `#_` (uneval) and `#|` (cursor). A modifier can modify an accumulator when evaluated. `#_` suppresses the next expression during value extraction."

(fact "uneval is a modifier"
  (-> (parse/parse-string "#_ignored")
      base/block-type)
  => :modifier)

(fact "uneval suppresses the next expression"
  (-> (parse/parse-string "[1 #_2 3]")
      base/block-value)
  => [1 3])

[[:section {:title "1.10 Dimensions: width, height, length"}]]

"Every block knows its on-screen width, its height in lines, and the length of its raw string. These dimensions power layout and navigation."

(fact "single-line block dimensions"
  (-> (parse/parse-string "hello")
      ((juxt base/block-width base/block-height base/block-length)))
  => [5 0 5])

(fact "multi-line string dimensions"
  (-> (parse/parse-string "\"a\nb\"")
      ((juxt base/block-width base/block-height)))
  => [2 1])

[[:chapter {:title "2. Parsing Source Code" :link "std.block.parse"}]]

[[:section {:title "2.1 parse-string reads one form"}]]

"`parse/parse-string` reads a single top-level form from a string. It is the simplest entry point for converting source text into a block tree."

(fact "parse a vector"
  (-> (parse/parse-string "[1 2 3]")
      str)
  => "[1 2 3]")

(fact "parse a nested map"
  (-> (parse/parse-string "{:a [1 2]}")
      str)
  => "{:a [1 2]}")

[[:section {:title "2.2 parse-root reads many forms"}]]

"`parse/parse-root` reads every form until EOF and wraps them in a `:root` container. Use it when you want to represent an entire file or snippet."

(fact "parse-root keeps multiple forms"
  (-> (parse/parse-root "a b c")
      str)
  => "a b c")

(fact "parse-root preserves inter-form whitespace"
  (-> (parse/parse-root "a\n\n  b")
      str)
  => "a\n\n  b")

[[:section {:title "2.3 parse-first returns the first expression"}]]

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

[[:section {:title "2.4 Reading collections"}]]

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

[[:section {:title "2.5 Reading cons forms"}]]

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

[[:section {:title "2.6 Reading hash dispatch"}]]

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

[[:section {:title "2.7 Preserving whitespace"}]]

"Spaces, tabs, newlines, commas, and EOF are all modelled as void blocks. This is why round-tripping source through `parse-root` followed by `str` gives back the exact original text."

(fact "round-trip keeps extra spaces"
  (-> (parse/parse-root "( 1   2 )")
      str)
  => "( 1   2 )")

(fact "commas become void blocks"
  (-> (parse/parse-string "[1,2]")
      str)
  => "[1,2]")

[[:section {:title "2.8 Preserving comments"}]]

"Comments are parsed as `:comment` blocks and kept inside containers. They participate in the child list just like whitespace."

(fact "comment inside a list"
  (-> (parse/parse-string "(1 ;two\n 3)")
      str)
  => "(1 ;two\n 3)")

(fact "comment at top level"
  (-> (parse/parse-root ";; header\n(+ 1 2)")
      str)
  => ";; header\n(+ 1 2)")

[[:section {:title "2.9 Reader conditionals"}]]

"`#?` and `#?@` are parsed as `:select` and `:select-splice` containers. Their values are represented as `(? ...)` and `(?-splicing ...)` forms."

(fact "reader conditional"
  (-> (parse/parse-string "#?(:cljs a :clj b)")
      base/block-value)
  => '(? {:cljs a :clj b}))

(fact "reader conditional splice"
  (-> (parse/parse-string "#?@(:cljs [a b])")
      base/block-value)
  => '(?-splicing {:cljs [a b]}))

[[:section {:title "2.10 Parsing errors"}]]

"Unmatched delimiters raise reader errors with the offending character. EOF inside a string or collection is also detected."

(fact "unmatched delimiter throws"
  (parse/parse-string "(]")
  => (throws))

(fact "unexpected EOF throws"
  (parse/parse-string "(1 2")
  => (throws))

[[:chapter {:title "3. Constructing and Modifying Blocks" :link "std.block.construct"}]]

[[:section {:title "3.1 The block helper"}]]

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

[[:section {:title "3.2 Constructing tokens"}]]

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

[[:section {:title "3.3 Constructing void blocks"}]]

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

[[:section {:title "3.4 Constructing comments"}]]

"`construct/comment` creates a comment block from a string. The string must begin with `;`."

(fact "valid comment"
  (str (construct/comment "; TODO: fix"))
  => "; TODO: fix")

(fact "invalid comment throws"
  (construct/comment "not a comment")
  => (throws))

[[:section {:title "3.5 Constructing containers"}]]

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

[[:section {:title "3.6 Adding children"}]]

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

[[:section {:title "3.7 Replacing children"}]]

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

[[:section {:title "3.8 Building root blocks"}]]

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

[[:section {:title "3.9 Reading contents back"}]]

"`construct/contents` converts a container's children back into Clojure forms. It is useful when you want the data inside a container without the container itself."

(fact "contents of a vector"
  (construct/contents (construct/block [1 2 3]))
  => '[1 ␣ 2 ␣ 3])

(fact "contents of a list"
  (construct/contents (construct/block '(+ 1 2)))
  => '(+ ␣ 1 ␣ 2))

[[:section {:title "3.10 Indentation helpers"}]]

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

[[:chapter {:title "4. Navigating and Transforming Blocks" :link "std.block.navigate"}]]

[[:section {:title "4.1 Creating navigators"}]]

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

[[:section {:title "4.2 Up, down, left, and right"}]]

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

[[:section {:title "4.3 Expression-aware navigation"}]]

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

[[:section {:title "4.4 Token navigation"}]]

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

[[:section {:title "4.5 Finding by predicate"}]]

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

[[:section {:title "4.6 Inserting elements"}]]

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

[[:section {:title "4.7 Deleting elements"}]]

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

[[:section {:title "4.8 Replacing and splicing"}]]

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

[[:section {:title "4.9 Tightening whitespace"}]]

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

[[:section {:title "4.10 Line information"}]]

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

[[:chapter {:title "5. Layout, Healing, Templating, and Real-World Usage" :link "std.block"}]]

[[:section {:title "5.1 Layout overview"}]]

"`block/layout` (alias for `std.block.layout/layout-main`) turns a Clojure form into a formatted block tree. It decides whether each collection fits on one line or needs multi-line formatting, then applies specialised rules for bindings, definitions, pairs, and hiccup-like vectors."

(fact "layout keeps a short form on one line"
  (-> (layout/layout-main '[1 2 3])
      str)
  => "[1 2 3]")

(fact "layout splits a long form across lines"
  (-> (layout/layout-main '[1 2 3 4 5 6 7 8 9 10 11 12 13 14 15])
      str)
  => "[1\n 2\n 3\n 4\n 5\n 6\n 7\n 8\n 9\n 10\n 11\n 12\n 13\n 14\n 15]")

[[:section {:title "5.2 Layout special forms"}]]

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

[[:section {:title "5.3 Healing unbalanced delimiters"}]]

"`block/heal` parses delimiter pairs and repairs mismatched or missing delimiters. It is used by file watchers and LLM output cleanup to keep source files readable."

(fact "heal removes extra closing delimiters"
  (block/heal "(1 2))")
  => "(1 2)")

(fact "heal removes mismatched delimiters"
  (block/heal "(1 2]}")
  => "(1 2)")

[[:section {:title "5.4 Rainbow output for debugging"}]]

"`heal/rainbow` prints delimiters in alternating colours so you can spot nesting errors. `heal/print-rainbow` writes the result to `*out*`."

(fact "rainbow wraps output in ANSI codes"
  (-> (heal/rainbow "(1 (2 (3)))")
      string?)
  => true)

(fact "rainbow preserves structure"
  (-> (heal/rainbow "(1 (2))")
      (clojure.string/replace #"\u001B\[[0-9;]*m" ""))
  => "(1 (2))")

[[:section {:title "5.5 Code templates"}]]

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

[[:section {:title "5.6 Real-world usage: code.query"}]]

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

[[:section {:title "5.7 Real-world usage: code.framework"}]]

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

[[:section {:title "5.8 Real-world usage: std.pretty"}]]

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

[[:section {:title "5.9 Real-world usage: healing and cleanup"}]]

"`std.make.project/file-watcher-heal` runs `block/heal` on changed source files. `indigo.server.api_prompt` runs LLM-generated DSL output through `block/heal` before using it. Both rely on the same simple contract: broken string in, balanced string out."

(fact "heal removes extra closing delimiters"
  (block/heal "(:? ()\n    (+ 1))) (+ 2)\n    nil {})")
  => "(:? ()\n    (+ 1) (+ 2)\n    nil {})")

(fact "heal cleans up mismatched brackets"
  (block/heal "(+ 1 2]}")
  => "(+ 1 2)")

[[:section {:title "5.10 Recipe: a tiny refactoring tool"}]]

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

[[:chapter {:title "6. Reference" :link "std.block"}]]

[[:section {:title "6.1 Public façade"}]]

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

[[:section {:title "6.2 Base predicates and accessors"}]]

"`std.block.base` defines the block taxonomy and the generic accessors used by every other namespace."

[[:api {:namespace "std.block.base"
        :only [block? expression? container? modifier?
               block-type block-tag block-string block-value
               block-length block-width block-height
               block-prefixed block-suffixed block-verify
               block-value-string block-children replace-children
               block-info]}]]

[[:section {:title "6.3 Construction"}]]

"`std.block.construct` builds blocks from Clojure data and from raw components."

[[:api {:namespace "std.block.construct"
        :only [block token token-from-string string-token
               container root contents
               void space spaces newline newlines tab tabs
               comment uneval cursor
               add-child replace-children
               max-width line-width indent-body]}]]

[[:section {:title "6.4 Parsing"}]]

"`std.block.parse` converts source strings into block trees."

[[:api {:namespace "std.block.parse"
        :only [parse-string parse-root parse-first
               read-dispatch -parse
               parse-token parse-keyword parse-comment
               parse-collection parse-cons parse-hash
               eof-block? delimiter-block?]}]]

[[:section {:title "6.5 Navigation"}]]

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

[[:section {:title "6.6 Layout and healing"}]]

"`std.block.layout` formats forms; `std.block.heal` repairs delimiters."

[[:api {:namespace "std.block.layout"
        :only [layout-main layout-default-fn layout-annotate
               layout-spec-fn layout-hiccup-like]}]]

[[:api {:namespace "std.block.heal"
        :only [heal rainbow print-rainbow]}]]

[[:section {:title "6.7 Templates"}]]

"`std.block.template` generates code from snippets with unquote holes."

[[:api {:namespace "std.block.template"
        :only [get-template get-template-params fill-template]}]]
