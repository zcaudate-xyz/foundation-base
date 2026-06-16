(ns documentation.std-block
  (:require [std.block :as block]
            [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.parse :as parse])
  (:use code.test))

[[:hero {:title "std.block"
         :subtitle "Code representation, traversal, and manipulation."
         :lead "std.block turns Clojure source into a tree of typed blocks: tokens, containers, whitespace, comments, and modifiers."
         :actions [{:label "Back to std" :href "index.html" :variant :primary}]}]]

[[:chapter {:title "What is a block?"}]]

"A block is a structural unit of source code. The simplest blocks are:

- **:token** — literals and symbols like `1`, `:a`, strings
- **:container** — collections like lists, vectors, maps, sets
- **:void** — whitespace, newlines, tabs, EOF
- **:comment** — `;` comments
- **:modifier** — reader macros like `#_`

Every block knows its type, tag, string representation, width, and height."

[[:chapter {:title "Parsing source" :link "std.block"}]]

"`parse-string` turns a Clojure string into a block tree. `parse-first` returns the first top-level form."

(fact "parse a simple vector"
  (-> (parse/parse-string "[1 2 3]")
      str)
  => "[1 2 3]")

(fact "parse-first returns the first form"
  (-> (parse/parse-first "1 2 3")
      base/block-info)
  => (contains {:type :token
                :tag :long
                :string "1"}))

"Whitespace and comments are preserved as void and comment blocks, so round-tripping source keeps formatting."

(fact "whitespace is preserved"
  (-> (parse/parse-string "[1   2]")
      str)
  => "[1   2]")

[[:chapter {:title "Inspecting blocks" :link "std.block"}]]

"Use the predicate and accessor functions to understand a block."

(fact "check block predicates"
  (base/block? (parse/parse-string "[1 2]"))
  => true)

(fact "containers have children"
  (-> (parse/parse-string "[1 2]")
      base/container?)
  => true)

(fact "tokens have values"
  (-> (parse/parse-string "1.2")
      base/block-value)
  => 1.2)

(fact "block-info summarises a block"
  (-> (parse/parse-string "true")
      base/block-info)
  => (contains {:type :token
                :tag :boolean
                :string "true"}))

[[:chapter {:title "Constructing blocks" :link "std.block"}]]

"You can build blocks from Clojure data with `construct/block`. Collections become containers, primitives become tokens."

(fact "construct a block from data"
  (-> (construct/block [1 2 3])
      str)
  => "[1 2 3]")

(fact "nested collections work"
  (-> (construct/block {:a [1 2]})
      str)
  => "{:a [1 2]}")

"Use `construct/void`, `construct/space`, `construct/newline`, and `construct/comment` to create formatting blocks explicitly."

(fact "construct whitespace"
  (str (construct/space))
  => "␣")

(fact "construct a newline"
  (str (construct/newline))
  => "\n")

[[:chapter {:title "Modifying blocks" :link "std.block"}]]

"Blocks are immutable. To change a container, replace its children with `base/replace-children` or add a single child with `construct/add-child`."

(fact "add a child to a container"
  (-> (construct/block [])
      (construct/add-child 1)
      (construct/add-child 2)
      str)
  => "[12]")

"Notice that `add-child` does not add spacing automatically. For formatted output, interleave spaces and newlines."

(fact "build a formatted list"
  (-> (construct/block [1])
      (construct/add-child (construct/space))
      (construct/add-child 2)
      str)
  => "[1 2]")

[[:chapter {:title "Reference" :link "std.block"}]]

[[:section {:title "Predicates and accessors"}]]

[[:api {:namespace "std.block.base"
        :only [block? expression? container? modifier?
               block-type block-tag block-string block-value
               block-length block-width block-height
               block-children block-info]}]]

[[:section {:title "Construction"}]]

[[:api {:namespace "std.block.construct"
        :only [block token container root contents
               void space newline comment
               add-child]}]]

[[:section {:title "Parsing"}]]

[[:api {:namespace "std.block.parse"
        :only [parse-string parse-first parse-root]}]]
