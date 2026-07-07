(ns documentation.code-query
  (:require [code.query :as q]
            [std.block.navigate :as nav])
  (:use code.test))

[[:hero {:title "code.query"
         :subtitle "Structural code queries and transformations."
         :lead "`code.query` provides selectors and traversal helpers over parsed Clojure source. It lets tools ask for forms by structure instead of relying on regular expressions."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Many maintenance tasks need to identify forms such as namespace requires, facts, defs, macros, or specific nested calls. `code.query` is the layer that turns parsed `std.block` trees into searchable and editable source structures."

[[:chapter {:title "How to use it" :link "usage"}]]

"Use match predicates for local checks, traversal functions to walk a tree, and compile helpers to turn query data into reusable selectors. `code.manage` builds on this layer for locate and refactor tasks."

[[:chapter {:title "Internal usage" :link "internal"}]]

"`code.manage.var/find-usages` uses `code.query` to discover namespace require aliases before searching for candidate symbols. Refactor and formatting tasks use query traversal to modify parsed blocks while preserving source shape."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Matching a single form"}]]

"`match` checks whether a parsed navigator satisfies a pattern. Meta annotations like `^:%` mark predicates, `^:%-` allow preceding whitespace, and `^:%+` insert matching nodes."

^{:refer code.query/match :added "3.0"}
(fact "match with predicates and anchors"
  (q/match (nav/parse-string "(+ 1 1)") '(symbol? _ _))
  => false

  (q/match (nav/parse-string "(+ 1 1)") '(^:% symbol? _ _))
  => true

  (q/match (nav/parse-string "(+ 1 1)") '(^:%- symbol? _ | _))
  => true)

[[:section {:title "Selecting and modifying forms"}]]

"`$` is the main query macro. Give it a string or navigator, a selector, and an optional transform function. Selectors can capture values with `|`, insert nodes with `^:%+`, and target nested paths."

^{:refer code.query/$ :added "3.0"}
(fact "select matching forms from source"
  (q/$ {:string "(defn hello1) (defn hello2)"}
       [(defn _ ^:%+ (keyword "tag"))])
  => '[(defn hello1 :tag) (defn hello2 :tag)])

^{:refer code.query/$ :added "3.0"}
(fact "capture values with the pipe marker"
  (q/$ {:string "(defn hello1) (defn hello2)"}
       [(defn _ | ^:%+ (keyword "tag"))])
  => '[:tag :tag])

^{:refer code.query/modify :added "3.0"}
(fact "modify matched forms with a function"
  (nav/string
   (q/modify (nav/parse-root "(defn hello3) (defn hello)")
             ['(defn | _)]
             (fn [zloc]
               (nav/insert-token-to-left zloc :hello))))
  => "(defn :hello hello3) (defn :hello hello)")

[[:section {:title "Traversing a tree"}]]

"`traverse` walks to the first match and returns a navigator, while `select` collects every match. Both work with nested patterns like `[defn if try]`."

^{:refer code.query/traverse :added "3.0"}
(fact "traverse to a nested form"
  (nav/value
   (q/traverse (nav/parse-string "(defn hello [] (if (try)))")
               '[defn if try]))
  => '(defn hello [] (if (try))))

^{:refer code.query/select :added "3.0"}
(fact "select all matching nested forms"
  (map nav/value
       (q/select (nav/parse-root "(defn a [] (if (try))) (defn b [] (if (try)))")
                 '[defn if try]))
  => '((defn a  [] (if (try)))
       (defn b [] (if (try)))))

[[:section {:title "End-to-end: add a tag to every defn"}]]

"Combining `$`, a capture selector, and a replace function makes a tiny refactoring: tag every top-level `defn` with metadata."

^{:refer code.query/$ :added "3.0"}
(fact "tag every defn in a snippet"
  (->> (q/$ {:string "(defn a []) (defn b [])"}
             [(defn _ _)]
             (fn [zloc]
               (nav/insert-token-to-right zloc :tagged)))
       (map nav/root-string)
       vec)
  => ["(defn a :tagged [])" "(defn b :tagged [])"])

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "code.query"}]]
[[:api {:namespace "code.query.match"}]]
[[:api {:namespace "code.query.traverse"}]]
[[:api {:namespace "code.query.walk"}]]
[[:api {:namespace "code.query.compile"}]]

;; BEGIN merged documentation: guides/code.query.md
;; sha256: 1b3f76ccfee7dab5138c26d1b1da5a286093daf5f1b8e7704a0fd4aff8c4c314
[[:chapter {:title "code.query Guide" :link "merged-guides-code-query-md"}]]
"# `code.query` Guide\n\n`code.query` enables advanced searching and modification of Clojure source code using a pattern-matching DSL over zippers. It allows you to find code structures that match a specific shape and transform them.\n\n## Core Concepts\n\n- **ZLoc**: A zipper location representing a node in the syntax tree.\n- **Selector**: A vector or list pattern describing the structure to match.\n- **Directives**: Special symbols (e.g., `^:%`, `|`, `^:?`) in patterns that control matching behavior (capturing, optionality, cursor placement).\n\n## Usage\n\n### The `$` Macro\n\nThe `$` macro is the main interface. It takes a context (string, file, zipper), a selector path, and optional arguments (transformation function, options).\n\n```clojure\n(require '[code.query :as query])\n```\n\n### Scenarios\n\n#### 1. Finding Specific Forms\n\n**Scenario: Find all `defn` names in a string.**\n\nWe match `(defn name ...)` and capture the name.\n\n```clojure\n(def code \"(defn foo [x] x) (defn bar [y] y)\")\n\n(query/$ code\n         ;; Pattern: List starting with defn, capture the second element (symbol)\n         '[(defn ^:% symbol? & _)])\n;; => (foo bar)\n```\n\n**Scenario: Find all maps containing a specific key.**\n\n```clojure\n(def code \"{:a 1 :b 2} {:a 3}\")\n\n(query/$ code\n         ;; Pattern: Map containing key :a\n         '[{:a _}])\n;; => ({:a 1 :b 2} {:a 3})\n```\n\n#### 2. Structural Editing\n\n**Scenario: Add a docstring to a function if it's missing.**\n\nWe need to match `defn` forms that *don't* have a string as the third element.\n\n```clojure\n(def code \"(defn my-fn [x] x)\")\n\n(query/$ code\n         ;; Match defn where 3rd element is a vector (args), not a string\n         '[(defn symbol? ^:% vector? & _)]\n\n         ;; Transformation function: insert docstring before args\n         (fn [zloc]\n           (std.block.navigate/insert-left zloc \"Added docstring\")))\n;; => \"(defn my-fn \\\"Added docstring\\\" [x] x)\"\n```\n\n#### 3. Context-Aware Modification\n\n**Scenario: Rename a variable only within a specific binding scope.**\n\nThis often requires a multi-step approach or using `|` to position the cursor exactly where the modification should happen.\n\n```clojure\n(def code \"(let [x 1] (+ x 1))\")\n\n(query/$ code\n         ;; Match `let` block, then find `x` inside the vector, position cursor (|) on it\n         '[(let [| x _] & _)]\n         (fn [zloc]\n           (std.block.navigate/set-value zloc 'y)))\n;; => \"(let [y 1] (+ x 1))\"\n;; Note: This only changed the binding name. A full refactor would need to traverse the body too.\n```\n\n#### 4. Advanced Pattern Matching Directives\n\n- `^:%` **Capture**: Returns the matched element.\n- `^:?` **Optional**: The element may or may not exist.\n- `^:+` **One or more**: Repeat match.\n- `^:*` **Zero or more**: Repeat match.\n- `|` **Cursor**: Sets the \"focus\" of the match. The transformation function applies to *this* node, not the whole pattern root.\n- `& _` **Rest**: Matches the rest of the collection (like `& args` in functions).\n\n**Scenario: Extracting dependencies from `ns` form.**\n\n```clojure\n(def ns-form \"(ns my.ns (:require [a.b :as ab] [c.d :refer [x]]))\")\n\n(query/$ ns-form\n         ;; Find :require, then inside it, match vectors\n         '[(ns _ (:require ^:%+ vector?))])\n;; => ([a.b :as ab] [c.d :refer [x]])\n```\n"
;; END merged documentation: guides/code.query.md

;; BEGIN merged documentation: plans/slop/summary/code_query_block_tutorial.md
;; sha256: fbc1af1a5d8dfc598f502a03081f8eb79296f49131c3336b585f40aad1865fe3
[[:chapter {:title "code.query.block Tutorial" :link "merged-plans-slop-summary-code-query-block-tutorial-md"}]]

"**Module:** `code.query.block`\n**Source File:** `src/code/query/block.clj`\n**Test File:** `test/code/query/block_test.clj`"

"The `code.query.block` module provides a powerful navigation and manipulation API for `std.block` ASTs, built on top of `std.lib.zip` (zippers). It allows for cursor-based traversal, inspection, and modification of Clojure code represented as blocks, making it ideal for structural editing, refactoring tools, and code analysis."

[[:section {:title "Core Concepts" :link "merged-plans-slop-summary-code-query-block-tutorial-md-core-concepts"}]]

"*   **Navigator:** The central data structure, which is a `std.lib.zip/Zipper` specifically configured for `std.block`s. It maintains a current position within the AST and provides functions to move around and modify the tree.\n*   **Cursor (`#|`):** A special block (`construct/cursor`) used to denote the current position within the code string representation of a navigator.\n*   **Position Tracking:** The navigator tracks the `[line column]` position of the cursor within the code.\n*   **Expression vs. Element:** Functions often distinguish between \"expressions\" (blocks with a Clojure value) and \"elements\" (any block, including whitespace and comments)."

[[:section {:title "Functions" :link "merged-plans-slop-summary-code-query-block-tutorial-md-functions"}]]

[[:subsection {:title "nav-template" :link "merged-plans-slop-summary-code-query-block-tutorial-md-nav-template"}]]

"`^{:refer code.query.block/nav-template :added \"3.0\"}`"

"A helper macro for generating navigation function definitions. It takes a symbol and a block tag function, and creates a function that can be used to query properties of the current block in a navigator."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-1 :added "4.0"}
(fact "nav-template example"
  (nav-template '-tag- #'std.block.base/block-tag)
  => '(clojure.core/defn -tag-
           ([zip] (-tag- zip :right))
           ([zip step]
            (clojure.core/if-let [elem (std.lib.zip/get zip)]
              (std.block.base/block-tag elem))))
)

[[:subsection {:title "left-anchor" :link "merged-plans-slop-summary-code-query-block-tutorial-md-left-anchor"}]]

"`^{:refer code.query.block/left-anchor :added \"3.0\" :class [:nav/primitive]}`"

"Calculates the length from the start of the current line to the current cursor position, considering newlines."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-2 :added "4.0"}
(fact "left-anchor example"
  (left-anchor (-> (navigator nil)
                     (zip/step-right)))
  => 3
)

[[:subsection {:title "update-step-left" :link "merged-plans-slop-summary-code-query-block-tutorial-md-update-step-left"}]]

"`^{:refer code.query.block/update-step-left :added \"3.0\" :class [:nav/primitive]}`"

"Updates the navigator's position when moving left, adjusting line and column numbers based on the block's dimensions."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-3 :added "4.0"}
(fact "update-step-left example"
  (-> {:position [0 7]}
        (update-step-left (construct/block [1 2 3])))
  => {:position [0 0]}
)

[[:subsection {:title "update-step-right" :link "merged-plans-slop-summary-code-query-block-tutorial-md-update-step-right"}]]

"`^{:refer code.query.block/update-step-right :added \"3.0\" :class [:nav/primitive]}`"

"Updates the navigator's position when moving right, adjusting line and column numbers."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-4 :added "4.0"}
(fact "update-step-right example"
  (-> {:position [0 0]}
        (update-step-right (construct/block [1 2 3])))
  => {:position [0 7]}
)

[[:subsection {:title "update-step-inside" :link "merged-plans-slop-summary-code-query-block-tutorial-md-update-step-inside"}]]

"`^{:refer code.query.block/update-step-inside :added \"3.0\" :class [:nav/primitive]}`"

"Updates the navigator's position when stepping inside a container block, placing the cursor after the opening delimiter."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-5 :added "4.0"}
(fact "update-step-inside example"
  (-> {:position [0 0]}
        (update-step-inside (construct/block #{})))
  => {:position [0 2]}
)

[[:subsection {:title "update-step-inside-left" :link "merged-plans-slop-summary-code-query-block-tutorial-md-update-step-inside-left"}]]

"`^{:refer code.query.block/update-step-inside-left :added \"3.0\" :class [:nav/primitive]}`"

"Updates the navigator's position when stepping inside a container block from the right, placing the cursor before the closing delimiter."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-6 :added "4.0"}
(fact "update-step-inside-left example"
  (-> {:position [0 3]}
        (update-step-inside-left (construct/block #{})))
  => {:position [0 2]}
)

[[:subsection {:title "update-step-outside" :link "merged-plans-slop-summary-code-query-block-tutorial-md-update-step-outside"}]]

"`^{:refer code.query.block/update-step-outside :added \"3.0\" :class [:nav/primitive]}`"

"Updates the navigator's position when stepping outside a container block."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-7 :added "4.0"}
(fact "update-step-outside example"
  (let [left-elems [(construct/block [1 2 3]) (construct/newline)]]
      (-> {:position [1 0]
           :left left-elems}
          (update-step-outside left-elems)
          :position))
  => [0 7]
)

[[:subsection {:title "display-navigator" :link "merged-plans-slop-summary-code-query-block-tutorial-md-display-navigator"}]]

"`^{:refer code.query.block/display-navigator :added \"3.0\" :class [:nav/primitive]}`"

"Returns a string representation of the navigator, including its position and the current block."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-8 :added "4.0"}
(fact "display-navigator example"
  (-> (navigator [1 2 3 4])
        (display-navigator))
  => "<0,0> |[1 2 3 4]"
)

[[:subsection {:title "navigator" :link "merged-plans-slop-summary-code-query-block-tutorial-md-navigator"}]]

"`^{:refer code.query.block/navigator :added \"3.0\" :class [:nav/general]}`"

"Creates a new `std.block` navigator from a block or Clojure data. This is the primary way to start navigating an AST."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-9 :added "4.0"}
(fact "navigator example"
  (str (navigator [1 2 3 4]))
  => "<0,0> |[1 2 3 4]"
)

[[:subsection {:title "navigator?" :link "merged-plans-slop-summary-code-query-block-tutorial-md-navigator-2"}]]

"`^{:refer code.query.block/navigator? :added \"3.0\" :class [:nav/general]}`"

"Checks if an object is a `std.block` navigator."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-10 :added "4.0"}
(fact "navigator? example"
  (navigator? (navigator [1 2 3 4]))
  => true
)

[[:subsection {:title "from-status" :link "merged-plans-slop-summary-code-query-block-tutorial-md-from-status"}]]

"`^{:refer code.query.block/from-status :added \"3.0\" :class [:nav/general]}`"

"Constructs a navigator from a given status (a block with a cursor)."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-11 :added "4.0"}
(fact "from-status example"
  (str (from-status (construct/block [1 2 3 (construct/cursor) 4])))
  => "<0,7> [1 2 3 |4]"
)

[[:subsection {:title "parse-string" :link "merged-plans-slop-summary-code-query-block-tutorial-md-parse-string"}]]

"`^{:refer code.query.block/parse-string :added \"3.0\" :class [:nav/general]}`"

"Parses a string into a navigator, automatically placing the cursor at the beginning or at a `#|` marker if present."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-12 :added "4.0"}
(fact "parse-string example"
  (str (parse-string "(2   #|   3  )"))
  => "<0,5> (2   |   3  )"
)

[[:subsection {:title "parse-root" :link "merged-plans-slop-summary-code-query-block-tutorial-md-parse-root"}]]

"`^{:refer code.query.block/parse-root :added \"3.0\" :class [:nav/general]}`"

"Parses a root string into a navigator."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-13 :added "4.0"}
(fact "parse-root example"
  (str (parse-root "a b c"))
  => "<0,0> |a b c"
)

[[:subsection {:title "parse-root-status" :link "merged-plans-slop-summary-code-query-block-tutorial-md-parse-root-status"}]]

"`^{:refer code.query.block/parse-root-status :added \"3.0\" :class [:nav/general]}`"

"Parses a string and creates a navigator from its status, similar to `from-status` but for root strings."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-14 :added "4.0"}
(fact "parse-root-status example"
  (str (parse-root-status "a b #|c"))
  => "<0,6> a b |c"
)

[[:subsection {:title "root-string" :link "merged-plans-slop-summary-code-query-block-tutorial-md-root-string"}]]

"`^{:refer code.query.block/root-string :added \"3.0\" :class [:nav/general]}`"

"Returns the string representation of the entire root block of the navigator."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-15 :added "4.0"}
(fact "root-string example"
  (root-string (navigator [1 2 3 4]))
  => "[1 2 3 4]"
)

[[:subsection {:title "left-expression" :link "merged-plans-slop-summary-code-query-block-tutorial-md-left-expression"}]]

"`^{:refer code.query.block/left-expression :added \"3.0\" :class [:nav/general]}`"

"Returns the first expression block to the left of the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-16 :added "4.0"}
(fact "left-expression example"
  (-> {:left [(construct/newline)
              (construct/block [1 2 3])]}
        (left-expression)
        (base/block-value))
  => [1 2 3]
)

[[:subsection {:title "left-expressions" :link "merged-plans-slop-summary-code-query-block-tutorial-md-left-expressions"}]]

"`^{:refer code.query.block/left-expressions :added \"3.0\" :class [:nav/general]}`"

"Returns all expression blocks to the left of the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-17 :added "4.0"}
(fact "left-expressions example"
  (->> {:left [(construct/newline)
               (construct/block :b)
               (construct/space)
               (construct/space)
               (construct/block :a)]}
         (left-expressions)
         (mapv base/block-value))
  => [:a :b]
)

[[:subsection {:title "right-expression" :link "merged-plans-slop-summary-code-query-block-tutorial-md-right-expression"}]]

"`^{:refer code.query.block/right-expression :added \"3.0\" :class [:nav/general]}`"

"Returns the first expression block to the right of the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-18 :added "4.0"}
(fact "right-expression example"
  (-> {:right [(construct/newline)
                (construct/block [1 2 3])]}
        (right-expression)
        (base/block-value))
  => [1 2 3]
)

[[:subsection {:title "right-expressions" :link "merged-plans-slop-summary-code-query-block-tutorial-md-right-expressions"}]]

"`^{:refer code.query.block/right-expressions :added \"3.0\" :class [:nav/general]}`"

"Returns all expression blocks to the right of the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-19 :added "4.0"}
(fact "right-expressions example"
  (->> {:right [(construct/newline)
                 (construct/block :b)
                 (construct/space)
                 (construct/space)
                 (construct/block :a)]}
          (right-expressions)
          (mapv base/block-value))
  => [:b :a]
)

[[:subsection {:title "left" :link "merged-plans-slop-summary-code-query-block-tutorial-md-left"}]]

"`^{:refer code.query.block/left :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the next expression block on the left."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-20 :added "4.0"}
(fact "left example"
  (-> (parse-string "(1  [1 2 3]    #|)")
        (left)
        str)
  => "<0,4> (1  |[1 2 3]    )"
)

[[:subsection {:title "left-most" :link "merged-plans-slop-summary-code-query-block-tutorial-md-left-most"}]]

"`^{:refer code.query.block/left-most :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the leftmost expression block in the current level."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-21 :added "4.0"}
(fact "left-most example"
  (-> (parse-string "(1  [1 2 3]  3 4   #|)")
        (left-most)
        str)
  => "<0,1> (|1  [1 2 3]  3 4   )"
)

[[:subsection {:title "left-most?" :link "merged-plans-slop-summary-code-query-block-tutorial-md-left-most-2"}]]

"`^{:refer code.query.block/left-most? :added \"3.0\" :class [:nav/move]}`"

"Checks if the navigator's cursor is at the leftmost expression block."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-22 :added "4.0"}
(fact "left-most? example"
  (-> (from-status [1 [(construct/cursor) 2 3]])
        (left-most?))
  => true
)

[[:subsection {:title "right" :link "merged-plans-slop-summary-code-query-block-tutorial-md-right"}]]

"`^{:refer code.query.block/right :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the next expression block on the right."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-23 :added "4.0"}
(fact "right example"
  (-> (parse-string "(#|[1 2 3]  3 4  ) ")
        (right)
        str)
  => "<0,10> ([1 2 3]  |3 4  )"
)

[[:subsection {:title "right-most" :link "merged-plans-slop-summary-code-query-block-tutorial-md-right-most"}]]

"`^{:refer code.query.block/right-most :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the rightmost expression block in the current level."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-24 :added "4.0"}
(fact "right-most example"
  (-> (parse-string "(#|[1 2 3]  3 4  ) ")
        (right-most)
        str)
  => "<0,12> ([1 2 3]  3 |4  )"
)

[[:subsection {:title "right-most?" :link "merged-plans-slop-summary-code-query-block-tutorial-md-right-most-2"}]]

"`^{:refer code.query.block/right-most? :added \"3.0\" :class [:nav/move]}`"

"Checks if the navigator's cursor is at the rightmost expression block."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-25 :added "4.0"}
(fact "right-most? example"
  (-> (from-status [1 [2 3 (construct/cursor)]])
        (right-most?))
  => true
)

[[:subsection {:title "up" :link "merged-plans-slop-summary-code-query-block-tutorial-md-up"}]]

"`^{:refer code.query.block/up :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor up to the parent container."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-26 :added "4.0"}
(fact "up example"
  (str (up (from-status [1 [2 (construct/cursor) 3]])))
  => "<0,3> [1 |[2 3]]"
)

[[:subsection {:title "down" :link "merged-plans-slop-summary-code-query-block-tutorial-md-down"}]]

"`^{:refer code.query.block/down :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor down into the first child expression of the current container."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-27 :added "4.0"}
(fact "down example"
  (str (down (from-status [1 (construct/cursor) [2 3]])))
  => "<0,4> [1 [|2 3]]"
)

[[:subsection {:title "right" :link "merged-plans-slop-summary-code-query-block-tutorial-md-right-2"}]]

"`^{:refer code.query.block/right* :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the next element (including whitespace) on the right."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-28 :added "4.0"}
(fact "right example"
  (str (right* (from-status [(construct/cursor) 1 2])))
  => "<0,2> [1| 2]"
)

[[:subsection {:title "left" :link "merged-plans-slop-summary-code-query-block-tutorial-md-left-2"}]]

"`^{:refer code.query.block/left* :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the next element (including whitespace) on the left."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-29 :added "4.0"}
(fact "left example"
  (str (left* (from-status [1 (construct/cursor) 2])))
  => "<0,2> [1| 2]"
)

[[:subsection {:title "block" :link "merged-plans-slop-summary-code-query-block-tutorial-md-block"}]]

"`^{:refer code.query.block/block :added \"3.0\" :class [:nav/general]}`"

"Returns the `std.block` AST node at the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-30 :added "4.0"}
(fact "block example"
  (block (from-status [1 [2 (construct/cursor) 3]]))
  => (construct/block 3)
)

[[:subsection {:title "prev" :link "merged-plans-slop-summary-code-query-block-tutorial-md-prev"}]]

"`^{:refer code.query.block/prev :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the previous expression block in a depth-first traversal."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-31 :added "4.0"}
(fact "prev example"
  (-> (parse-string "([1 2 [3]] #|)")
        (prev)
        str)
  => "<0,7> ([1 2 [|3]] )"
)

[[:subsection {:title "next" :link "merged-plans-slop-summary-code-query-block-tutorial-md-next"}]]

"`^{:refer code.query.block/next :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the next expression block in a depth-first traversal."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-32 :added "4.0"}
(fact "next example"
  (-> (parse-string "(#|  [[3]]  )")
        (next)
        (next)
        (next)
        str)
  => "<0,5> (  [[|3]]  )"
)

[[:subsection {:title "find-next-token" :link "merged-plans-slop-summary-code-query-block-tutorial-md-find-next-token"}]]

"`^{:refer code.query.block/find-next-token :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the next token block whose value matches the given data."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-33 :added "4.0"}
(fact "find-next-token example"
  (-> (parse-string "(#|  [[3 2]]  )")
        (find-next-token 2)
        str)
  => "<0,7> (  [[3 |2]]  )"
)

[[:subsection {:title "prev-anchor" :link "merged-plans-slop-summary-code-query-block-tutorial-md-prev-anchor"}]]

"`^{:refer code.query.block/prev-anchor :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the previous newline or the beginning of the current line."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-34 :added "4.0"}
(fact "prev-anchor example"
  (-> (parse-string "( \n \n [[3 \n]] #|  )")
        (prev-anchor)
        (:position))
  => [3 0]

  (-> (parse-string "( #| )")
        (prev-anchor)
        (:position))
  => [0 0]
)

[[:subsection {:title "next-anchor" :link "merged-plans-slop-summary-code-query-block-tutorial-md-next-anchor"}]]

"`^{:refer code.query.block/next-anchor :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the next newline."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-35 :added "4.0"}
(fact "next-anchor example"
  (-> (parse-string "( \n \n#| [[3 \n]]  )")
        (next-anchor)
        (:position))
  => [3 0]
)

[[:subsection {:title "left-token" :link "merged-plans-slop-summary-code-query-block-tutorial-md-left-token"}]]

"`^{:refer code.query.block/left-token :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the next token block on the left."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-36 :added "4.0"}
(fact "left-token example"
  (-> (parse-string "(1  {}  #|2 3 4)")
        (left-token)
        str)
  => "<0,1> (|1  {}  2 3 4)"
)

[[:subsection {:title "left-most-token" :link "merged-plans-slop-summary-code-query-block-tutorial-md-left-most-token"}]]

"`^{:refer code.query.block/left-most-token :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the leftmost token block in the current level."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-37 :added "4.0"}
(fact "left-most-token example"
  (-> (parse-string "(1  {}  2 3 #|4)")
        (left-most-token)
        str)
  => "<0,10> (1  {}  2 |3 4)"
)

[[:subsection {:title "right-token" :link "merged-plans-slop-summary-code-query-block-tutorial-md-right-token"}]]

"`^{:refer code.query.block/right-token :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the next token block on the right."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-38 :added "4.0"}
(fact "right-token example"
  (-> (parse-string "(#|1  {}  2 3 4)")
        (right-token)
        str)
  => "<0,8> (1  {}  |2 3 4)"
)

[[:subsection {:title "right-most-token" :link "merged-plans-slop-summary-code-query-block-tutorial-md-right-most-token"}]]

"`^{:refer code.query.block/right-most-token :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the rightmost token block in the current level."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-39 :added "4.0"}
(fact "right-most-token example"
  (-> (parse-string "(#|1  {}  2 3 [4])")
        (right-most-token)
        str)
  => "<0,10> (1  {}  2 |3 [4])"
)

[[:subsection {:title "prev-token" :link "merged-plans-slop-summary-code-query-block-tutorial-md-prev-token"}]]

"`^{:refer code.query.block/prev-token :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the previous token block in a depth-first traversal."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-40 :added "4.0"}
(fact "prev-token example"
  (-> (parse-string "(1 (2 3 [4])#|)")
        (prev-token)
        str)
  => "<0,9> (1 (2 3 [|4]))"
)

[[:subsection {:title "next-token" :link "merged-plans-slop-summary-code-query-block-tutorial-md-next-token"}]]

"`^{:refer code.query.block/next-token :added \"3.0\" :class [:nav/move]}`"

"Moves the navigator's cursor to the next token block in a depth-first traversal."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-41 :added "4.0"}
(fact "next-token example"
  (-> (parse-string "(#|[[1 2 3 4]])")
        (next-token)
        str)
  => "<0,3> ([[|1 2 3 4]])"
)

[[:subsection {:title "position-left" :link "merged-plans-slop-summary-code-query-block-tutorial-md-position-left"}]]

"`^{:refer code.query.block/position-left :added \"3.0\" :class [:nav/move]}`"

"Moves the cursor to the left expression, skipping whitespace."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-42 :added "4.0"}
(fact "position-left example"
  (-> (parse-string "( 2   #|   3  )")
        (position-left)
        str)
  => "<0,2> ( |2      3  )"

  (-> (parse-string "(   #|   3  )")
        (position-left)
        str)
  => "<0,1> (|      3  )"
)

[[:subsection {:title "position-right" :link "merged-plans-slop-summary-code-query-block-tutorial-md-position-right"}]]

"`^{:refer code.query.block/position-right :added \"3.0\" :class [:nav/move]}`"

"Moves the cursor to the right expression, skipping whitespace."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-43 :added "4.0"}
(fact "position-right example"
  (-> (parse-string "(2   #|    3  )")
        (position-right)
        str)
  => "<0,9> (2       |3  )"

  (-> (parse-string "(2   #|     )")
        (position-right)
        str)
  => "<0,10> (2        |)"
)

[[:subsection {:title "tighten-left" :link "merged-plans-slop-summary-code-query-block-tutorial-md-tighten-left"}]]

"`^{:refer code.query.block/tighten-left :added \"3.0\" :class [:nav/edit]}`"

"Removes extra spaces on the left of the current expression."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-44 :added "4.0"}
(fact "tighten-left example"
  (-> (parse-string "(1 2 3   #|4)")
        (tighten-left)
        str)
  => "<0,7> (1 2 3 |4)"

  (-> (parse-string "(1 2 3   #|    4)")
        (tighten-left)
        str)
  => "<0,7> (1 2 3 |4)"

  (-> (parse-string "(    #|     )")
        (tighten-left)
        str)
  => "<0,1> (|)"
)

[[:subsection {:title "tighten-right" :link "merged-plans-slop-summary-code-query-block-tutorial-md-tighten-right"}]]

"`^{:refer code.query.block/tighten-right :added \"3.0\" :class [:nav/edit]}`"

"Removes extra spaces on the right of the current expression."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-45 :added "4.0"}
(fact "tighten-right example"
  (-> (parse-string "(1 2 #|3       4)")
        (tighten-right)
        str)
  => "<0,5> (1 2 |3 4)"

  (-> (parse-string "(1 2 3   #|    4)")
        (tighten-right)
        str)
  => "<0,5> (1 2 |3 4)"

  (-> (parse-string "(    #|     )")
        (tighten-right)
        str)
  => "<0,1> (|)"
)

[[:subsection {:title "tighten" :link "merged-plans-slop-summary-code-query-block-tutorial-md-tighten"}]]

"`^{:refer code.query.block/tighten :added \"3.0\" :class [:nav/edit]}`"

"Removes extra spaces on both the left and right of the current expression."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-46 :added "4.0"}
(fact "tighten example"
  (-> (parse-string "(1 2      #|3       4)")
        (tighten)
        str)
  => "<0,5> (1 2 |3 4)"
)

[[:subsection {:title "level-empty?" :link "merged-plans-slop-summary-code-query-block-tutorial-md-level-empty"}]]

"`^{:refer code.query.block/level-empty? :added \"3.0\" :class [:nav/edit]}`"

"Checks if the current container has no expression children."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-47 :added "4.0"}
(fact "level-empty? example"
  (-> (parse-string "( #| )")
        (level-empty?))
  => true
)

[[:subsection {:title "insert-empty" :link "merged-plans-slop-summary-code-query-block-tutorial-md-insert-empty"}]]

"`^{:refer code.query.block/insert-empty :added \"3.0\" :class [:nav/edit]}`"

"Inserts an element into an empty container."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-48 :added "4.0"}
(fact "insert-empty example"
  (-> (parse-string "( #| )")
        (insert-empty 1)
        str)
  => "<0,1> (|1  )"
)

[[:subsection {:title "insert-right" :link "merged-plans-slop-summary-code-query-block-tutorial-md-insert-right"}]]

"`^{:refer code.query.block/insert-right :added \"3.0\" :class [:nav/edit]}`"

"Inserts an element to the right of the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-49 :added "4.0"}
(fact "insert-right example"
  (-> (parse-string "(#|0)")
        (insert-right 1)
        str)
  => "<0,1> (|0 1)"

  (-> (parse-string "(#|)")
        (insert-right 1)
        str)
  => "<0,1> (|1)"

  (-> (parse-string "( #| )")
        (insert-right 1)
        str)
  => "<0,1> (|1  )"
)

[[:subsection {:title "insert-token-to-left" :link "merged-plans-slop-summary-code-query-block-tutorial-md-insert-token-to-left"}]]

"`^{:refer code.query.block/insert-token-to-left :added \"3.0\" :class [:nav/edit]}`"

"Inserts an element to the left of the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-50 :added "4.0"}
(fact "insert-token-to-left example"
  (-> (parse-string "(#|0)")
        (insert-token-to-left 1)
        str)
  => "<0,3> (1 |0)"

  (-> (parse-string "(#|)")
        (insert-token-to-left 1)
        str)
  => "<0,1> (|1)"

  (-> (parse-string "( #| )")
        (insert-token-to-left 1)
        str)
  => "<0,1> (|1  )"
)

[[:subsection {:title "insert" :link "merged-plans-slop-summary-code-query-block-tutorial-md-insert"}]]

"`^{:refer code.query.block/insert :added \"3.0\" :class [:nav/edit]}`"

"Inserts an element at the current cursor position and moves the cursor past the inserted element."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-51 :added "4.0"}
(fact "insert example"
  (-> (parse-string "(#|0)")
        (insert 1)
        str)
  => "<0,3> (0 |1)"

  (-> (parse-string "(#|)")
        (insert-right 1)
        str)
  => "<0,1> (|1)"

  (-> (parse-string "( #| )")
        (insert-right 1)
        str)

  => "<0,1> (|1  )"
)

[[:subsection {:title "insert-all" :link "merged-plans-slop-summary-code-query-block-tutorial-md-insert-all"}]]

"`^{:refer code.query.block/insert-all :added \"3.0\"}`"

"Inserts all expressions from a collection into the block at the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-52 :added "4.0"}
(fact "insert-all example"
  ;; No direct test example, but it would involve:
  (-> (parse-string "(#|)")
      (insert-all [1 2 3])
      str)
  => "<0,7> (1 2 3|)"
)

[[:subsection {:title "insert-newline" :link "merged-plans-slop-summary-code-query-block-tutorial-md-insert-newline"}]]

"`^{:refer code.query.block/insert-newline :added \"3.0\"}`"

"Inserts one or more newline blocks at the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-53 :added "4.0"}
(fact "insert-newline example"
  ;; No direct test example, but it would involve:
  (-> (parse-string "(#|)")
      (insert-newline)
      str)
  => "<0,1> (|\n)"
)

[[:subsection {:title "insert-space" :link "merged-plans-slop-summary-code-query-block-tutorial-md-insert-space"}]]

"`^{:refer code.query.block/insert-space :added \"3.0\"}`"

"Inserts one or more space blocks at the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-54 :added "4.0"}
(fact "insert-space example"
  ;; No direct test example, but it would involve:
  (-> (parse-string "(#|)")
      (insert-space)
      str)
  => "<0,1> (| \t)"
)

[[:subsection {:title "delete-left" :link "merged-plans-slop-summary-code-query-block-tutorial-md-delete-left"}]]

"`^{:refer code.query.block/delete-left :added \"3.0\" :class [:nav/edit]}`"

"Deletes the element to the left of the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-55 :added "4.0"}
(fact "delete-left example"
  (-> (parse-string "(1 2   #|3)")
        (delete-left)
        str)
  => "<0,3> (1 |3)"

  (-> (parse-string "(  #|1 2 3)")
        (delete-left)
        str)
  => "<0,1> (|1 2 3)"

  (-> (parse-string "( #| )")
        (delete-left)
        str)
  => "<0,1> (|)"
)

[[:subsection {:title "delete-right" :link "merged-plans-slop-summary-code-query-block-tutorial-md-delete-right"}]]

"`^{:refer code.query.block/delete-right :added \"3.0\" :class [:nav/edit]}`"

"Deletes the element to the right of the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-56 :added "4.0"}
(fact "delete-right example"
  (-> (parse-string "(  #|1 2 3)")
        (delete-right)
        str)
  => "<0,3> (  |1 3)"

  (-> (parse-string "(1 2   #|3)")
        (delete-right)
        str)
  => "<0,7> (1 2   |3)"

  (-> (parse-string "( #| )")
        (delete-right)
        str)
  => "<0,1> (|)"
)

[[:subsection {:title "delete" :link "merged-plans-slop-summary-code-query-block-tutorial-md-delete"}]]

"`^{:refer code.query.block/delete :added \"3.0\" :class [:nav/edit]}`"

"Deletes the element at the current cursor position."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-57 :added "4.0"}
(fact "delete example"
  (-> (parse-string "(  #|1   2 3)")
        (delete)
        str)
  => "<0,3> (  |2 3)"

  (-> (parse-string "(1 2   #|3)")
        (delete)
        str)
  => "<0,7> (1 2   |)"

  (-> (parse-string "(  #|    )")
        (delete)
        str)
  => "<0,1> (|)"
)

[[:subsection {:title "backspace" :link "merged-plans-slop-summary-code-query-block-tutorial-md-backspace"}]]

"`^{:refer code.query.block/backspace :added \"3.0\" :class [:nav/edit]}`"

"Performs a \"backspace\" operation, deleting the element to the left of the cursor and moving the cursor."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-58 :added "4.0"}
(fact "backspace example"
  (-> (parse-string "(0  #|1   2 3)")
        (backspace)
        str)
  => "<0,1> (|0 2 3)"

  (-> (parse-string "(  #|1   2 3)")
        (backspace)
        str)
  => "<0,1> (|2 3)"
)

[[:subsection {:title "replace" :link "merged-plans-slop-summary-code-query-block-tutorial-md-replace"}]]

"`^{:refer code.query.block/replace :added \"3.0\" :class [:nav/edit]}`"

"Replaces the element at the current cursor position with new data."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-59 :added "4.0"}
(fact "replace example"
  (-> (parse-string "(0  #|1   2 3)")
        (position-right)
        (replace :a)
        str)
  => "<0,4> (0  |:a   2 3)"
)

[[:subsection {:title "swap" :link "merged-plans-slop-summary-code-query-block-tutorial-md-swap"}]]

"`^{:refer code.query.block/swap :added \"3.0\" :class [:nav/edit]}`"

"Applies a function to the element at the current cursor position, replacing it with the result."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-60 :added "4.0"}
(fact "swap example"
  (-> (parse-string "(0  #|1   2 3)")
        (position-right)
        (swap inc)
        str)
  => "<0,4> (0  |2   2 3)"
)

[[:subsection {:title "update-children" :link "merged-plans-slop-summary-code-query-block-tutorial-md-update-children"}]]

"`^{:refer code.query.block/update-children :added \"3.0\" :class [:nav/edit]}`"

"Replaces all children of the current container block with a new sequence of children."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-61 :added "4.0"}
(fact "update-children example"
  (-> (update-children (parse-string "[1 2 3]")
                       [(construct/block 4)
                        (construct/space)
                        (construct/block 5)])
      str)
  => "<0,0> |[4 5]"
)

[[:subsection {:title "line-info" :link "merged-plans-slop-summary-code-query-block-tutorial-md-line-info"}]]

"`^{:refer code.query.block/line-info :added \"3.0\" :class [:nav/general]}`"

"Returns a map containing line and column information for the current block."

^{:id merged-plans-slop-summary-code-query-block-tutorial-md-example-62 :added "4.0"}
(fact "line-info example"
  (line-info (parse-string "[1 \n  2 3]"))
  => {:row 1, :col 1, :end-row 2, :end-col 7}
)
;; END merged documentation: plans/slop/summary/code_query_block_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/code_query_common_tutorial.md
;; sha256: 2babc7e5761c507383f6779a654f8da772074795b2f10ba0b2f1f11068f6a226
[[:chapter {:title "code.query.common Tutorial" :link "merged-plans-slop-summary-code-query-common-tutorial-md"}]]

"**Module:** `code.query.common`\n**Source File:** `src/code/query/common.clj`\n**Test File:** `test/code/query/common_test.clj`"

"The `code.query.common` module provides a set of utility functions for working with Clojure forms, particularly in the context of code querying, transformation, and diffing. It includes predicates for special symbols (like cursor markers), functions for manipulating metadata-driven flags (insertion, deletion), and tools for walking and cleaning up forms."

[[:section {:title "Core Concepts" :link "merged-plans-slop-summary-code-query-common-tutorial-md-core-concepts"}]]

"*   **Metadata Flags:** Uses metadata (`^:+`, `^:-`, `^:?`) to mark forms for insertion, deletion, or optional presence during code transformation.\n*   **Cursor Marker:** The `|` symbol is used as a placeholder for a cursor position within a form.\n*   **Form Walking:** Leverages `std.lib.walk` for recursive traversal and transformation of Clojure forms."

[[:section {:title "Functions" :link "merged-plans-slop-summary-code-query-common-tutorial-md-functions"}]]

[[:subsection {:title "any" :link "merged-plans-slop-summary-code-query-common-tutorial-md-any"}]]

"`^{:refer code.query.common/any :added \"3.0\"}`"

"A predicate that always returns `true` for any input. Useful as a wildcard or default matcher."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-1 :added "4.0"}
(fact "any example"
  (any nil)
  => true

  (any '_) ; Note: The test uses '_ as a symbol, which is fine.
  => true
)

[[:subsection {:title "none" :link "merged-plans-slop-summary-code-query-common-tutorial-md-none"}]]

"`^{:refer code.query.common/none :added \"3.0\"}`"

"A predicate that always returns `false` for any input."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-2 :added "4.0"}
(fact "none example"
  (none nil)
  => false

  (none '_) ; Note: The test uses '_ as a symbol, which is fine.
  => false
)

[[:subsection {:title "expand-meta" :link "merged-plans-slop-summary-code-query-common-tutorial-md-expand-meta"}]]

"`^{:refer code.query.common/expand-meta :added \"3.0\"}`"

"Takes a form and expands its metadata keywords (e.g., `:?`, `:+`, `:%`) into a map of boolean flags."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-3 :added "4.0"}
(fact "expand-meta example"
  (meta (expand-meta ^:? ()))
  => {:? true}

  (meta (expand-meta ^:+%? ()))
  => {:? true, :% true, :+ true}
)

[[:subsection {:title "cursor?" :link "merged-plans-slop-summary-code-query-common-tutorial-md-cursor"}]]

"`^{:refer code.query.common/cursor? :added \"3.0\"}`"

"Checks if an element is the cursor marker (`|`)."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-4 :added "4.0"}
(fact "cursor? example"
  (cursor? '|)
  => true

  (cursor? '_) ; Note: The test uses '_ as a symbol, which is fine.
  => false
)

[[:subsection {:title "insertion?" :link "merged-plans-slop-summary-code-query-common-tutorial-md-insertion"}]]

"`^{:refer code.query.common/insertion? :added \"3.0\"}`"

"Checks if a form has the `^:+` metadata flag, indicating it's marked for insertion."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-5 :added "4.0"}
(fact "insertion? example"
  (insertion? '^:+ a)
  => true

  (insertion? 'a)
  => false
)

[[:subsection {:title "deletion?" :link "merged-plans-slop-summary-code-query-common-tutorial-md-deletion"}]]

"`^{:refer code.query.common/deletion? :added \"3.0\"}`"

"Checks if a form has the `^:-` metadata flag, indicating it's marked for deletion."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-6 :added "4.0"}
(fact "deletion? example"
  (deletion? '^:- a)
  => true

  (deletion? 'a)
  => false
)

[[:subsection {:title "prewalk" :link "merged-plans-slop-summary-code-query-common-tutorial-md-prewalk"}]]

"`^{:refer code.query.common/prewalk :added \"3.0\"}`"

"Applies a function to elements in a depth-first, pre-order traversal, eagerly modifying them. It preserves metadata."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-7 :added "4.0"}
(fact "prewalk example"
  Example from test code, but no direct assertion provided.
  (prewalk inc '(1 (2 3)))
  => '(2 (3 4))
)

[[:subsection {:title "remove-items" :link "merged-plans-slop-summary-code-query-common-tutorial-md-remove-items"}]]

"`^{:refer code.query.common/remove-items :added \"3.0\"}`"

"Recursively removes items from a form that match a given predicate."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-8 :added "4.0"}
(fact "remove-items example"
  (remove-items #{1} '(1 2 3 4))
  => '(2 3 4)

  (remove-items #{1} '(1 (1 (1 (1)))))
  => '(((())))
)

[[:subsection {:title "prepare-deletion" :link "merged-plans-slop-summary-code-query-common-tutorial-md-prepare-deletion"}]]

"`^{:refer code.query.common/prepare-deletion :added \"3.0\"}`"

"Prepares a form for a deletion walk by removing cursor markers and insertion-flagged elements."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-9 :added "4.0"}
(fact "prepare-deletion example"
  (prepare-deletion '(+ a 2))
  => '(+ a 2)

  (prepare-deletion '(+ ^:+ a | 2))
  => '(+ 2)
)

[[:subsection {:title "prepare-insertion" :link "merged-plans-slop-summary-code-query-common-tutorial-md-prepare-insertion"}]]

"`^{:refer code.query.common/prepare-insertion :added \"3.0\"}`"

"Prepares a form for an insertion operation by removing cursor markers and deletion-flagged elements."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-10 :added "4.0"}
(fact "prepare-insertion example"
  (prepare-insertion '(+ a 2))
  => '(+ a 2)

  (prepare-insertion '(+ ^:+ a | ^:- b 2))
  => '(+ a 2)
)

[[:subsection {:title "prepare-query" :link "merged-plans-slop-summary-code-query-common-tutorial-md-prepare-query"}]]

"`^{:refer code.query.common/prepare-query :added \"3.0\"}`"

"Prepares a form for a query walk by removing cursor markers, deletion-flagged, and insertion-flagged elements."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-11 :added "4.0"}
(fact "prepare-query example"
  (prepare-query '(+ ^:+ a | ^:- b 2))
  => '(+ 2)
)

[[:subsection {:title "find-index" :link "merged-plans-slop-summary-code-query-common-tutorial-md-find-index"}]]

"`^{:refer code.query.common/find-index :added \"3.0\"}`"

"Returns the index of the first occurrence of an element matching a predicate in a sequence."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-12 :added "4.0"}
(fact "find-index example"
  (find-index #{2} '(1 2 3 4))
  => 1
)

[[:subsection {:title "finto" :link "merged-plans-slop-summary-code-query-common-tutorial-md-finto"}]]

"`^{:refer code.query.common/finto :added \"3.0\"}`"

"A version of `into` that correctly handles lists by reversing the `from` collection before `into`ing."

^{:id merged-plans-slop-summary-code-query-common-tutorial-md-example-13 :added "4.0"}
(fact "finto example"
  (finto () '(1 2 3))
  => '(1 2 3)
)
;; END merged documentation: plans/slop/summary/code_query_common_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/code_query_compile_tutorial.md
;; sha256: 3e3e931b2dfd3e9f9078017a8c9435b9b747ad4de9ce267e98061cf9a4151689
[[:chapter {:title "code.query.compile Tutorial" :link "merged-plans-slop-summary-code-query-compile-tutorial-md"}]]

"**Module:** `code.query.compile`\n**Source File:** `src/code/query/compile.clj`\n**Test File:** `test/code/query/compile_test.clj`"

"The `code.query.compile` module is responsible for transforming a symbolic query path (a vector of Clojure forms and special keywords) into a structured map that can be used by the `code.query.match` module to find matching code structures. It handles cursor positions, metadata-driven flags, and special query operators like `:*` (multi-match) and `:n` (nth element)."

[[:section {:title "Core Concepts" :link "merged-plans-slop-summary-code-query-compile-tutorial-md-core-concepts"}]]

"*   **Query Path:** A vector of Clojure forms and special symbols that describe a desired code structure.\n*   **Cursor (`|`):** Marks the position of interest within the query path.\n*   **Metadata Flags:** `^:+`, `^:-`, `^:?` on forms in the query path indicate insertion, deletion, or optional elements.\n*   **Special Keywords:**\n    *   `_`: Matches any single element.\n    *   `:*`: Matches multiple elements (like `*` in regex).\n    *   `:n` (e.g., `:1`, `:5`): Matches the nth element.\n*   **Compiled Query Map:** The output of this module, a nested map describing the query for `code.query.match`."

[[:section {:title "Functions" :link "merged-plans-slop-summary-code-query-compile-tutorial-md-functions"}]]

[[:subsection {:title "cursor-info" :link "merged-plans-slop-summary-code-query-compile-tutorial-md-cursor-info"}]]

"`^{:refer code.query.compile/cursor-info :added \"3.0\"}`"

"Finds the information related to the cursor (`|`) within a sequence of selectors (query path). It returns a vector `[index type form]` where `index` is the position, `type` is `:cursor` or `:form`, and `form` is the element if `type` is `:form`."

^{:id merged-plans-slop-summary-code-query-compile-tutorial-md-example-1 :added "4.0"}
(fact "cursor-info example"
  (cursor-info '[(defn ^:?& _ | & _)])
  => '[0 :form (defn _ | & _)]

  (cursor-info (expand-all-metas '[(defn ^:?& _ | & _)]))
  => '[0 :form (defn _ | & _)]

  (cursor-info '[defn if])
  => [nil :cursor]

  (cursor-info '[defn | if])
  => [1 :cursor]
)

[[:subsection {:title "expand-all-metas" :link "merged-plans-slop-summary-code-query-compile-tutorial-md-expand-all-metas"}]]

"`^{:refer code.query.compile/expand-all-metas :added \"3.0\"}`"

"Converts shorthand metadata (e.g., `^:%?`) on forms within the query path into a map-based metadata (e.g., `{:? true, :% true}`)."

^{:id merged-plans-slop-summary-code-query-compile-tutorial-md-example-2 :added "4.0"}
(fact "expand-all-metas example"
  (meta (expand-all-metas '^:%? sym?))
  => {:? true, :% true}

  (-> (expand-all-metas '(^:%+ + 1 2))
      first meta)
  => {:+ true, :% true}
)

[[:subsection {:title "split-path" :link "merged-plans-slop-summary-code-query-compile-tutorial-md-split-path"}]]

"`^{:refer code.query.compile/split-path :added \"3.0\"}`"

"Splits the query path into two parts: `up` (elements before the cursor/form) and `down` (elements from the cursor/form onwards)."

^{:id merged-plans-slop-summary-code-query-compile-tutorial-md-example-3 :added "4.0"}
(fact "split-path example"
  (split-path '[defn | if try] [1 :cursor])
  => '{:up (defn), :down [if try]}

  (split-path '[defn if try] [nil :cursor])
  => '{:up [], :down [defn if try]}
)

[[:subsection {:title "process-special" :link "merged-plans-slop-summary-code-query-compile-tutorial-md-process-special"}]]

"`^{:refer code.query.compile/process-special :added \"3.0\"}`"

"Converts special keywords (`:*`, `:n`) in the query path into a map representation (e.g., `{:type :multi}`, `{:type :nth, :step 1}`)."

^{:id merged-plans-slop-summary-code-query-compile-tutorial-md-example-4 :added "4.0"}
(fact "process-special example"
  (process-special :*)
  => {:type :multi}

  (process-special :1)
  => {:type :nth, :step 1}

  (process-special :5)
  => {:type :nth, :step 5}
)

[[:subsection {:title "process-path" :link "merged-plans-slop-summary-code-query-compile-tutorial-md-process-path"}]]

"`^{:refer code.query.compile/process-path :added \"3.0\"}`"

"Converts a raw query path (vector of forms and special keywords) into a more structured sequence of maps, where each map describes an element or a special operation."

^{:id merged-plans-slop-summary-code-query-compile-tutorial-md-example-5 :added "4.0"}
(fact "process-path example"
  (process-path '[defn if try])
  => '[{:type :step, :element defn}
          {:type :step, :element if}
          {:type :step, :element try}]

  (process-path '[defn :* try :3 if])
  => '[{:type :step, :element defn}
          {:element try, :type :multi}
          {:element if, :type :nth, :step 3}]
)

[[:subsection {:title "compile-section-base" :link "merged-plans-slop-summary-code-query-compile-tutorial-md-compile-section-base"}]]

"`^{:refer code.query.compile/compile-section-base :added \"3.0\"}`"

"Compiles a single element section of the query path into its base matching criteria (e.g., `:form`, `:pattern`, `:is`)."

^{:id merged-plans-slop-summary-code-query-compile-tutorial-md-example-6 :added "4.0"}
(fact "compile-section-base example"
  (compile-section-base '{:element defn})
  => '{:form defn}

  (compile-section-base '{:element (if & _)})
  => '{:pattern (if & _)}

  (compile-section-base '{:element _})
  => {:is code.query.common/any}
)

[[:subsection {:title "compile-section" :link "merged-plans-slop-summary-code-query-compile-tutorial-md-compile-section"}]]

"`^{:refer code.query.compile/compile-section :added \"3.0\"}`"

"Compiles a query section based on the traversal direction (`:up` or `:down`), previous context, and element details. It translates special types (`:multi`, `:nth`) into corresponding matching strategies (`:contains`, `:nth-ancestor`)."

^{:id merged-plans-slop-summary-code-query-compile-tutorial-md-example-7 :added "4.0"}
(fact "compile-section example"
  (compile-section :up nil '{:element if, :type :nth, :step 3})
  => '{:nth-ancestor [3 {:form if}]}

  (compile-section :down nil '{:element if, :type :multi})
  => '{:contains {:form if}}
)

[[:subsection {:title "compile-submap" :link "merged-plans-slop-summary-code-query-compile-tutorial-md-compile-submap"}]]

"`^{:refer code.query.compile/compile-submap :added \"3.0\"}`"

"Compiles a nested sub-query map based on the traversal direction and a processed path. It builds up the `:child` or `:parent` relationships in the query map."

^{:id merged-plans-slop-summary-code-query-compile-tutorial-md-example-8 :added "4.0"}
(fact "compile-submap example"
  (compile-submap :down (process-path '[if try]))
  => '{:child {:child {:form if}, :form try}}

  (compile-submap :up (process-path '[defn if]))
  => '{:parent {:parent {:form defn}, :form if}}
)

[[:subsection {:title "prepare" :link "merged-plans-slop-summary-code-query-compile-tutorial-md-prepare"}]]

"`^{:refer code.query.compile/prepare :added \"3.0\"}`"

"The main function for compiling a query. It takes a raw query path, expands metadata, splits the path, processes special elements, and returns a compiled query map along with cursor information."

^{:id merged-plans-slop-summary-code-query-compile-tutorial-md-example-9 :added "4.0"}
(fact "prepare example"
  (prepare '[defn if])
  => '[{:child {:form if}, :form defn} [nil :cursor]]

  (prepare '[defn | if])
  => '[{:parent {:form defn}, :form if} [1 :cursor]]
)
;; END merged documentation: plans/slop/summary/code_query_compile_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/code_query_match_tutorial.md
;; sha256: 8edfeedaf04e7e83ebf50cac1b9f90a6207bd095f3b09b1c86d4077d6410e2ff
[[:chapter {:title "code.query.match Tutorial" :link "merged-plans-slop-summary-code-query-match-tutorial-md"}]]

"**Module:** `code.query.match`\n**Source File:** `src/code/query/match.clj`\n**Test File:** `test/code/query/match_test.clj`"

"The `code.query.match` module provides a powerful and flexible system for pattern matching against Clojure code represented as `std.block` navigators. It allows you to define complex matching rules using a declarative syntax, enabling tasks like code analysis, refactoring, and linting. The module is built around the concept of \"matchers\" – functions that take a navigator and return `true` if the current position matches a given pattern."

[[:section {:title "Core Concepts" :link "merged-plans-slop-summary-code-query-match-tutorial-md-core-concepts"}]]

"*   **Matcher:** A function (or an instance of `code.query.match/Matcher` record) that takes a `code.query.block` navigator as input and returns a boolean indicating whether the current block matches a defined pattern.\n*   **Predicate Functions (`p-*`):** A rich set of functions (prefixed with `p-`) that create matchers for various conditions, such as checking the value, type, metadata, or structural relationships (parent, child, sibling) of a block.\n*   **Query Language:** Matchers can be composed using logical operators (`p-and`, `p-or`, `p-not`) and can be built from a declarative data structure using `compile-matcher`.\n*   **Navigator (`nav/`):** Functions from `code.query.block` (aliased as `nav`) are used extensively to create and manipulate the AST context for matching."

[[:section {:title "Functions" :link "merged-plans-slop-summary-code-query-match-tutorial-md-functions"}]]

[[:subsection {:title "matcher" :link "merged-plans-slop-summary-code-query-match-tutorial-md-matcher"}]]

"`^{:refer code.query.match/matcher :added \"3.0\"}`"

"Creates a `Matcher` record from a predicate function. This allows any function that takes a navigator and returns a boolean to be used as a matcher."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-1 :added "4.0"}
(fact "matcher example"
  ((matcher string?) "hello")
  => true
)

[[:subsection {:title "matcher?" :link "merged-plans-slop-summary-code-query-match-tutorial-md-matcher-2"}]]

"`^{:refer code.query.match/matcher? :added \"3.0\"}`"

"Checks if an object is a `Matcher` instance."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-2 :added "4.0"}
(fact "matcher? example"
  (matcher? (matcher string?))
  => true
)

[[:subsection {:title "p-fn" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-fn"}]]

"`^{:refer code.query.match/p-fn :added \"3.0\"}`"

"Creates a matcher that applies a given predicate function directly to the navigator."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-3 :added "4.0"}
(fact "p-fn example"
  ((p-fn (fn [nav]
             (-> nav (nav/tag) (= :symbol))))
   (nav/parse-string "defn"))
  => true
)

[[:subsection {:title "p-not" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-not"}]]

"`^{:refer code.query.match/p-not :added \"3.0\"}`"

"Creates a matcher that negates the result of another matcher."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-4 :added "4.0"}
(fact "p-not example"
  ((p-not (p-is 'if)) (nav/parse-string "defn"))
  => true

  ((p-not (p-is 'if)) (nav/parse-string "if"))
  => false
)

[[:subsection {:title "p-is" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-is"}]]

"`^{:refer code.query.match/p-is :added \"3.0\"}`"

"Creates a matcher that checks if the current block's value is equivalent to a given template, ignoring metadata."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-5 :added "4.0"}
(fact "p-is example"
  ((p-is 'defn) (nav/parse-string "defn"))
  => true

  ((p-is '^{:a 1} defn) (nav/parse-string "defn"))
  => true

  ((p-is 'defn) (nav/parse-string "is"))
  => false

  ((p-is '(defn & _)) (nav/parse-string "(defn x [])"))
  => false
)

[[:subsection {:title "p-equal-loop" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-equal-loop"}]]

"`^{:refer code.query.match/p-equal-loop :added \"3.0\"}`"

"A helper function for `p-equal` that recursively compares two Clojure forms for deep equality, including collection contents."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-6 :added "4.0"}
(fact "p-equal-loop example"
  ((p-equal [1 2 3]) (nav/parse-string "[1 2 3]"))
  => true

  ((p-equal (list 'defn)) (nav/parse-string "(defn)"))
  => true

  ((p-equal '(defn)) (nav/parse-string "(defn)"))
  => true
)

[[:subsection {:title "p-equal" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-equal"}]]

"`^{:refer code.query.match/p-equal :added \"3.0\"}`"

"Creates a matcher that checks for deep equality between the current block's value and a template, including metadata."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-7 :added "4.0"}
(fact "p-equal example"
  ((p-equal '^{:a 1} defn) (nav/parse-string "defn"))
  => false

  ((p-equal '^{:a 1} defn) (nav/parse-string "^{:a 1} defn"))
  => true

  ((p-equal '^{:a 1} defn) (nav/parse-string "^{:a 2} defn"))
  => false
)

[[:subsection {:title "p-meta" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-meta"}]]

"`^{:refer code.query.match/p-meta :added \"3.0\"}`"

"Creates a matcher that checks if the metadata of the current block's parent (if it's a meta form) matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-8 :added "4.0"}
(fact "p-meta example"
  ((p-meta {:a 1}) (nav/down (nav/parse-string "^{:a 1} defn")))
  => true

  ((p-meta {:a 1}) (nav/down (nav/parse-string "^{:a 2} defn")))
  => false
)

[[:subsection {:title "p-type" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-type"}]]

"`^{:refer code.query.match/p-type :added \"3.0\"}`"

"Creates a matcher that checks if the `block-tag` of the current block matches a given type keyword (e.g., `:symbol`, `:list`)."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-9 :added "4.0"}
(fact "p-type example"
  ((p-type :symbol) (nav/parse-string "defn"))
  => true

  ((p-type :symbol) (-> (nav/parse-string "^{:a 1} defn") nav/down nav/right))
  => true
)

[[:subsection {:title "p-form" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-form"}]]

"`^{:refer code.query.match/p-form :added \"3.0\"}`"

"Creates a matcher that checks if the current block is a list form whose first element (the function/macro name) matches a given symbol."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-10 :added "4.0"}
(fact "p-form example"
  ((p-form 'defn) (nav/parse-string "(defn x [])"))
  => true
  ((p-form 'let) (nav/parse-string "(let [])"))
  => true
)

[[:subsection {:title "p-pattern" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-pattern"}]]

"`^{:refer code.query.match/p-pattern :added \"3.0\"}`"

"Creates a matcher that checks if the current block's value matches a complex pattern defined using Clojure forms and special query symbols (like `_`, `&`). This leverages `code.query.match.pattern`."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-11 :added "4.0"}
(fact "p-pattern example"
  ((p-pattern '(defn ^:% symbol? & _)) (nav/parse-string "(defn ^{:a 1} x [])"))
  => true

  ((p-pattern '(defn ^:% symbol? ^{:% true :? true} string? []))
   (nav/parse-string "(defn ^{:a 1} x [])"))
  => true
)

[[:subsection {:title "p-code" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-code"}]]

"`^{:refer code.query.match/p-code :added \"3.0\"}`"

"Creates a matcher that checks if the string representation of the current block matches a given regular expression."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-12 :added "4.0"}
(fact "p-code example"
  ((p-code #"defn") (nav/parse-string "(defn ^{:a 1} x [])"))
  => true
)

[[:subsection {:title "p-and" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-and"}]]

"`^{:refer code.query.match/p-and :added \"3.0\"}`"

"Combines multiple matchers, returning `true` only if all of them match."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-13 :added "4.0"}
(fact "p-and example"
  ((p-and (p-code #"defn")
            (p-type :token)) (nav/parse-string "(defn ^{:a 1} x [])"))
  => false

  ((p-and (p-code #"defn")
            (p-type :list)) (nav/parse-string "(defn ^{:a 1} x [])"))
  => true
)

[[:subsection {:title "p-or" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-or"}]]

"`^{:refer code.query.match/p-or :added \"3.0\"}`"

"Combines multiple matchers, returning `true` if at least one of them matches."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-14 :added "4.0"}
(fact "p-or example"
  ((p-or (p-code #"defn")
            (p-type :token)) (nav/parse-string "(defn ^{:a 1} x [])"))
  => true

  ((p-or (p-code #"defn")
            (p-type :list)) (nav/parse-string "(defn ^{:a 1} x [])"))
  => true
)

[[:subsection {:title "compile-matcher" :link "merged-plans-slop-summary-code-query-match-tutorial-md-compile-matcher"}]]

"`^{:refer code.query.match/compile-matcher :added \"3.0\"}`"

"The main entry point for creating complex matchers from a declarative data structure (a map, vector, symbol, or function). It recursively compiles the structure into a composite matcher."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-15 :added "4.0"}
(fact "compile-matcher example"
  ((compile-matcher {:is 'hello}) (nav/parse-string "hello"))
  => true
)

[[:subsection {:title "p-parent" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-parent"}]]

"`^{:refer code.query.match/p-parent :added \"3.0\"}`"

"Creates a matcher that checks if the parent of the current block matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-16 :added "4.0"}
(fact "p-parent example"
  ((p-parent 'defn) (-> (nav/parse-string "(defn x [])") nav/next nav/next))
  => true

  ((p-parent {:parent 'if}) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => true

  ((p-parent {:parent 'if}) (-> (nav/parse-string "(if (= x y))") nav/down))
  => false
)

[[:subsection {:title "p-child" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-child"}]]

"`^{:refer code.query.match/p-child :added \"3.0\"}`"

"Creates a matcher that checks if any child of the current container block matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-17 :added "4.0"}
(fact "p-child example"
  ((p-child {:form '=}) (nav/parse-string "(if (= x y))"))
  => true

  ((p-child '=) (nav/parse-string "(if (= x y))"))
  => false
)

[[:subsection {:title "p-first" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-first"}]]

"`^{:refer code.query.match/p-first :added \"3.0\"}`"

"Creates a matcher that checks if the first element of the current container block matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-18 :added "4.0"}
(fact "p-first example"
  ((p-first 'defn) (-> (nav/parse-string "(defn x [])")))
  => true

  ((p-first 'x) (-> (nav/parse-string "[x y z]")))
  => true

  ((p-first 'x) (-> (nav/parse-string "[y z]")))
  => false
)

[[:subsection {:title "p-last" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-last"}]]

"`^{:refer code.query.match/p-last :added \"3.0\"}`"

"Creates a matcher that checks if the last element of the current container block matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-19 :added "4.0"}
(fact "p-last example"
  ((p-last 1) (-> (nav/parse-string "(defn [] 1)")))
  => true

  ((p-last 'z) (-> (nav/parse-string "[x y z]")))
  => true

  ((p-last 'x) (-> (nav/parse-string "[y z]")))
  => false
)

[[:subsection {:title "p-nth" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-nth"}]]

"`^{:refer code.query.match/p-nth :added \"3.0\"}`"

"Creates a matcher that checks if the element at a specific Nth index within the current container block matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-20 :added "4.0"}
(fact "p-nth example"
  ((p-nth [0 'defn]) (-> (nav/parse-string "(defn [] 1)")))
  => true

  ((p-nth [2 'z]) (-> (nav/parse-string "[x y z]")))
  => true

  ((p-nth [2 'x]) (-> (nav/parse-string "[y z]")))
  => false
)

[[:subsection {:title "p-nth-left" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-nth-left"}]]

"`^{:refer code.query.match/p-nth-left :added \"3.0\"}`"

"Creates a matcher that checks if the element at a specific Nth index to the left of the current position has a certain characteristic."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-21 :added "4.0"}
(fact "p-nth-left example"
  ((p-nth-left [0 'defn]) (-> (nav/parse-string "(defn [] 1)") nav/down))
  => true

  ((p-nth-left [1 ^:& vector?]) (-> (nav/parse-string "(defn [] 1)") nav/down nav/right-most))
  => true
)

[[:subsection {:title "p-nth-right" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-nth-right"}]]

"`^{:refer code.query.match/p-nth-right :added \"3.0\"}`"

"Creates a matcher that checks if the element at a specific Nth index to the right of the current position has a certain characteristic."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-22 :added "4.0"}
(fact "p-nth-right example"
  ((p-nth-right [0 'defn]) (-> (nav/parse-string "(defn [] 1)") nav/down))
  => true

  ((p-nth-right [1 vector?]) (-> (nav/parse-string "(defn [] 1)") nav/down))
  => true
)

[[:subsection {:title "p-nth-ancestor" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-nth-ancestor"}]]

"`^{:refer code.query.match/p-nth-ancestor :added \"3.0\"}`"

"Creates a matcher that searches for a match `n` levels up in the ancestor chain."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-23 :added "4.0"}
(fact "p-nth-ancestor example"
  ((p-nth-ancestor [2 {:contains 3}])
   (-> (nav/parse-string "(* (- (+ 1 2) 3) 4)")
       nav/down nav/right nav/down nav/right nav/down))
  => true
)

[[:subsection {:title "tree-search" :link "merged-plans-slop-summary-code-query-match-tutorial-md-tree-search"}]]

"`^{:refer code.query.match/tree-search :added \"3.0\"}`"

"A helper function for `p-contains` that recursively searches a tree structure for elements matching a predicate."

"No direct test example, but used internally by p-contains."

[[:subsection {:title "p-contains" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-contains"}]]

"`^{:refer code.query.match/p-contains :added \"3.0\"}`"

"Creates a matcher that checks if any element (deeply nested) within the current container matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-25 :added "4.0"}
(fact "p-contains example"
  ((p-contains '=) (nav/parse-string "(if (= x y))"))
  => true

  ((p-contains 'x) (nav/parse-string "(if (= x y))"))
  => true
)

[[:subsection {:title "tree-depth-search" :link "merged-plans-slop-summary-code-query-match-tutorial-md-tree-depth-search"}]]

"`^{:refer code.query.match/tree-depth-search :added \"3.0\"}`"

"A helper function for `p-nth-contains` that performs a depth-first search for a match `n` levels down in a tree structure."

"No direct test example, but used internally by p-nth-contains."

[[:subsection {:title "p-nth-contains" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-nth-contains"}]]

"`^{:refer code.query.match/p-nth-contains :added \"3.0\"}`"

"Creates a matcher that searches for a match `n` levels down in the tree."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-27 :added "4.0"}
(fact "p-nth-contains example"
  ((p-nth-contains [2 {:contains 1}])
   (nav/parse-string "(* (- (+ 1 2) 3) 4)"))
  => true
)

[[:subsection {:title "p-ancestor" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-ancestor"}]]

"`^{:refer code.query.match/p-ancestor :added \"3.0\"}`"

"Creates a matcher that checks if any ancestor of the current block matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-28 :added "4.0"}
(fact "p-ancestor example"
  ((p-ancestor {:form 'if}) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => true

  ((p-ancestor 'if) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => true
)

[[:subsection {:title "p-sibling" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-sibling"}]]

"`^{:refer code.query.match/p-sibling :added \"3.0\"}`"

"Creates a matcher that checks if any element on the same level (sibling) as the current block matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-29 :added "4.0"}
(fact "p-sibling example"
  ((p-sibling '=) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => false

  ((p-sibling 'x) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => true
)

[[:subsection {:title "p-left" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-left"}]]

"`^{:refer code.query.match/p-left :added \"3.0\"}`"

"Creates a matcher that checks if the immediate left sibling of the current block matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-30 :added "4.0"}
(fact "p-left example"
  ((p-left '=) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next nav/next))
  => true

  ((p-left 'if) (-> (nav/parse-string "(if (= x y))") nav/down nav/next))
  => true
)

[[:subsection {:title "p-right" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-right"}]]

"`^{:refer code.query.match/p-right :added \"3.0\"}`"

"Creates a matcher that checks if the immediate right sibling of the current block matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-31 :added "4.0"}
(fact "p-right example"
  ((p-right 'x) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => true

  ((p-right {:form '=}) (-> (nav/parse-string "(if (= x y))") nav/down))
  => true
)

[[:subsection {:title "p-left-of" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-left-of"}]]

"`^{:refer code.query.match/p-left-of :added \"3.0\"}`"

"Creates a matcher that checks if any element to the left of the current block (on the same level) matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-32 :added "4.0"}
(fact "p-left-of example"
  ((p-left-of '=) (-> (nav/parse-string "(= x y)") nav/down nav/next))
  => true

  ((p-left-of '=) (-> (nav/parse-string "(= x y)") nav/down nav/next nav/next))
  => true
)

[[:subsection {:title "p-right-of" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-right-of"}]]

"`^{:refer code.query.match/p-right-of :added \"3.0\"}`"

"Creates a matcher that checks if any element to the right of the current block (on the same level) matches a given template."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-33 :added "4.0"}
(fact "p-right-of example"
  ((p-right-of 'x) (-> (nav/parse-string "(= x y)") nav/down))
  => true

  ((p-right-of 'y) (-> (nav/parse-string "(= x y)") nav/down))
  => true

  ((p-right-of 'z) (-> (nav/parse-string "(= x y)") nav/down))
  => false
)

[[:subsection {:title "p-left-most" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-left-most"}]]

"`^{:refer code.query.match/p-left-most :added \"3.0\"}`"

"Creates a matcher that checks if the current block is the leftmost expression within its current level."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-34 :added "4.0"}
(fact "p-left-most example"
  ((p-left-most true) (-> (nav/parse-string "(= x y)") nav/down))
  => true

  ((p-left-most true) (-> (nav/parse-string "(= x y)") nav/down nav/next))
  => false
)

[[:subsection {:title "p-right-most" :link "merged-plans-slop-summary-code-query-match-tutorial-md-p-right-most"}]]

"`^{:refer code.query.match/p-right-most :added \"3.0\"}`"

"Creates a matcher that checks if the current block is the rightmost expression within its current level."

^{:id merged-plans-slop-summary-code-query-match-tutorial-md-example-35 :added "4.0"}
(fact "p-right-most example"
  ((p-right-most true) (-> (nav/parse-string "(= x y)") nav/down nav/next))
  => false

  ((p-right-most true) (-> (nav/parse-string "(= x y)") nav/down nav/next nav/next))
  => true
)
;; END merged documentation: plans/slop/summary/code_query_match_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/code_query_summary.md
;; sha256: 2d1a4991d5132c4a5143deccc1c061908ff42a2b4c4ba2bba3b5466f555b1b81
[[:chapter {:title "code.query Summary" :link "merged-plans-slop-summary-code-query-summary-md"}]]

"`code.query` is a library for querying and manipulating code structures represented as `std.block` trees. It provides a powerful way to navigate, match, and transform code, making it a key component of the `foundation-base` transpiler and code analysis tools."

"**Core Concepts:**"

"*   **Navigator:** A zipper-like data structure (`code.query.block/navigator`) that allows for efficient traversal and manipulation of the `std.block` tree.\n*   **Matchers:** Predicate functions (`code.query.match`) that can be used to find specific nodes or patterns in the code tree.\n*   **Traversal:** Functions for walking the code tree and applying transformations (`code.query.walk` and `code.query.traverse`).\n*   **Compilation:** The `code.query.compile` namespace provides functions for compiling a query pattern into an efficient matcher."

"**Key Areas of Functionality (with Examples):**"

"*   **Navigation (`code.query.block`):**\n    *   **`navigator`**: Creates a navigator from a `std.block` tree.\n    *   **`up`, `down`, `left`, `right`**: Functions for moving the navigator around the tree.\n    *   **`node`**: Returns the current block at the navigator's focus.\n    *   **`root-string`**: Returns the string representation of the entire code tree.\n        ```clojure\n        (require '[code.query.block :as nav])\n\n        (def nav (nav/parse-string \"(+ 1 2)\"))\n        (-> nav nav/down nav/right nav/node base/block-value)\n        ;; => 1\n        ```\n\n*   **Matching (`code.query.match`):**\n    *   **`p-is`**: Matches a specific value.\n    *   **`p-form`**: Matches a form with a specific symbol as its first element.\n    *   **`p-pattern`**: Matches a code pattern with wildcards and predicates.\n    *   **`p-and`, `p-or`, `p-not`**: For combining matchers.\n    *   **`p-parent`, `p-child`, `p-ancestor`, `p-contains`**: For matching based on relationships between nodes.\n        ```clojure\n        (require '[code.query.match :as match])\n\n        (def nav (nav/parse-string \"(defn my-fn [x] (+ x 1))\"))\n        ((match/p-form 'defn) nav)\n        ;; => true\n        ((match/p-pattern '(defn _ [_] _)) nav)\n        ;; => true\n        ```\n\n*   **Walking and Traversal (`code.query.walk`, `code.query.traverse`):**\n    *   **`matchwalk`**: Traverses the code tree and applies a function to all nodes that match a given pattern.\n    *   **`levelwalk`**: Similar to `matchwalk`, but only applies the function to nodes at the top level of the tree.\n    *   **`traverse`**: A more powerful traversal function that can be used to perform complex transformations, including insertions and deletions.\n        ```clojure\n        (require '[code.query.walk :as walk])\n        (require '[code.query.match :as match])\n\n        (-> (walk/matchwalk (nav/parse-string \"(+ 1 (+ 2 3))\")\n                            [(match/p-is '+)]\n                            (fn [nav] (nav/replace nav '-)))\n            nav/root-string)\n        ;; => \"(- 1 (- 2 3))\"\n        ```\n\n*   **Compilation (`code.query.compile`):**\n    *   **`prepare`**: Compiles a query pattern into a more efficient internal representation that can be used by the matching and traversal functions. This is mostly used internally by the library."
;; END merged documentation: plans/slop/summary/code_query_summary.md

;; BEGIN merged documentation: plans/slop/summary/code_query_traverse_tutorial.md
;; sha256: b49eb2801fd957bd6038fb0ca515262d287eecce0db68c66e10a06e555c87fcf
[[:chapter {:title "code.query.traverse Tutorial" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md"}]]

"**Module:** `code.query.traverse`\n**Source File:** `src/code/query/traverse.clj`\n**Test File:** `test/code/query/traverse_test.clj`"

"The `code.query.traverse` module provides a powerful mechanism for traversing and transforming Clojure code ASTs (represented as `std.block` navigators) based on declarative patterns. It enables complex operations like inserting, deleting, and modifying code elements while maintaining structural integrity. This module is fundamental for building advanced code manipulation tools."

[[:section {:title "Core Concepts" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-core-concepts"}]]

"*   **`Position` Record:** A record that encapsulates the current state of a traversal, including the `source` navigator, the `pattern` navigator, and the `op` (operation) map.\n*   **Pattern-driven Traversal:** The traversal is guided by a `pattern` (a Clojure form with special metadata and symbols) that dictates how to navigate and what actions to perform.\n*   **Operations (`op` map):** A map of functions (`:delete-form`, `:insert-level`, `:cursor-node`, etc.) that define how to handle different parts of the pattern during traversal.\n*   **Metadata Flags:** `^:+` (insert), `^:-` (delete), `^:?` (optional) on forms within the pattern control the transformation behavior.\n*   **Cursor (`|`):** Marks a specific point of interest in the pattern, allowing for precise targeting of operations."

[[:section {:title "Functions" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-functions"}]]

[[:subsection {:title "pattern-zip" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-pattern-zip"}]]

"`^{:refer code.query.traverse/pattern-zip :added \"3.0\"}`"

"Creates a `clojure.zip` zipper specifically configured for traversing Clojure forms (lists and vectors) as patterns."

"No direct test example, but it's used internally to create pattern navigators."

[[:subsection {:title "wrap-meta" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-wrap-meta"}]]

"`^{:refer code.query.traverse/wrap-meta :added \"3.0\"}`"

"A higher-order function that wraps a traversal function to handle metadata tags. It ensures that metadata forms are correctly processed during traversal."

"No direct test example, but used internally by traversal functions."

[[:subsection {:title "wrap-delete-next" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-wrap-delete-next"}]]

"`^{:refer code.query.traverse/wrap-delete-next :added \"3.0\"}`"

"A wrapper function for deleting the element immediately following the current position in a zipper. Used internally by deletion traversal."

"No direct test example, but used internally by deletion traversal functions."

[[:subsection {:title "traverse-delete-form" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-traverse-delete-form"}]]

"`^{:refer code.query.traverse/traverse-delete-form :added \"3.0\"}`"

"Traverses a form marked for deletion, recursively applying deletion logic to its children."

"No direct test example, but used internally by deletion traversal functions."

[[:subsection {:title "traverse-delete-node" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-traverse-delete-node"}]]

"`^{:refer code.query.traverse/traverse-delete-node :added \"3.0\"}`"

"Handles the deletion of a single node during traversal."

"No direct test example, but used internally by deletion traversal functions."

[[:subsection {:title "traverse-delete-level" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-traverse-delete-level"}]]

"`^{:refer code.query.traverse/traverse-delete-level :added \"3.0\"}`"

"Traverses a level within the AST, applying deletion logic based on the pattern."

"No direct test example, but used internally by deletion traversal functions."

[[:subsection {:title "prep-insert-pattern" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-prep-insert-pattern"}]]

"`^{:refer code.query.traverse/prep-insert-pattern :added \"3.0\"}`"

"Prepares a pattern element for insertion by removing its metadata and evaluating it if marked with `:%`."

"No direct test example, but used internally by insertion traversal functions."

[[:subsection {:title "wrap-insert-next" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-wrap-insert-next"}]]

"`^{:refer code.query.traverse/wrap-insert-next :added \"3.0\"}`"

"A wrapper function for inserting an element immediately following the current position in a zipper. Used internally by insertion traversal."

"No direct test example, but used internally by insertion traversal functions."

[[:subsection {:title "traverse-insert-form" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-traverse-insert-form"}]]

"`^{:refer code.query.traverse/traverse-insert-form :added \"3.0\"}`"

"Traverses a form marked for insertion, recursively applying insertion logic to its children."

"No direct test example, but used internally by insertion traversal functions."

[[:subsection {:title "traverse-insert-node" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-traverse-insert-node"}]]

"`^{:refer code.query.traverse/traverse-insert-node :added \"3.0\"}`"

"Handles the insertion of a single node during traversal."

"No direct test example, but used internally by insertion traversal functions."

[[:subsection {:title "traverse-insert-level" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-traverse-insert-level"}]]

"`^{:refer code.query.traverse/traverse-insert-level :added \"3.0\"}`"

"Traverses a level within the AST, applying insertion logic based on the pattern."

"No direct test example, but used internally by insertion traversal functions."

[[:subsection {:title "wrap-cursor-next" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-wrap-cursor-next"}]]

"`^{:refer code.query.traverse/wrap-cursor-next :added \"3.0\"}`"

"A wrapper function for locating the cursor at the next element during code traversal."

"No direct test example, but used internally by cursor traversal functions."

[[:subsection {:title "traverse-cursor-form" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-traverse-cursor-form"}]]

"`^{:refer code.query.traverse/traverse-cursor-form :added \"3.0\"}`"

"Traverses a form to locate the cursor within it."

"No direct test example, but used internally by cursor traversal functions."

[[:subsection {:title "traverse-cursor-level" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-traverse-cursor-level"}]]

"`^{:refer code.query.traverse/traverse-cursor-level :added \"3.0\"}`"

"Traverses a level within the AST to locate the cursor."

"No direct test example, but used internally by cursor traversal functions."

[[:subsection {:title "count-elements" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-count-elements"}]]

"`^{:refer code.query.traverse/count-elements :added \"3.0\"}`"

"Counts the number of elements in a given code structure. Used internally for pattern matching."

"No direct test example, but used internally."

[[:subsection {:title "traverse" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-traverse"}]]

"`^{:refer code.query.traverse/traverse :added \"3.0\"}`"

"The main traversal function. It takes a source navigator and a pattern, and applies the transformations (insertions, deletions) defined by the pattern to the source."

^{:id merged-plans-slop-summary-code-query-traverse-tutorial-md-example-16 :added "4.0"}
(fact "traverse example"
  (source
   (traverse (nav/parse-string "^:a (+ () 2 3)")
             '(+ () 2 3)))
  => '(+ () 2 3)

  (source
   (traverse (nav/parse-string "^:a (hello)")
             '(hello)))
  => '(hello)

  ;; Deletions
  (source
   (traverse (nav/parse-string "^:a (hello)")
             '(^:- hello)))
  => ()

  (source
   (traverse (nav/parse-string "(hello)")
             '(^:- hello)))
  => ()

  (source
   (traverse (nav/parse-string "((hello))")
             '((^:- hello))))
  => '(())

  ;; Insertions
  (source
   (traverse (nav/parse-string "()")
             '(^:+ hello)))
  => '(hello)

  (source
   (traverse (nav/parse-string "(())")
             '((^:+ hello))))
  => '((hello))

  ;; More advanced transformations
  (source
   (traverse (nav/parse-string "(defn hello)")
             '(defn ^{:? true :% true} symbol? ^:+ [])))
  => '(defn hello [])

  (source
   (traverse (nav/parse-string "(defn hello)")
             '(defn ^{:? true :% true :- true} symbol? ^:+ [])))
  => '(defn [])

  (source
   (traverse (nav/parse-string "(defn hello)")
             '(defn ^{:? true :% true :- true} symbol? | ^:+ [])))
  => []

  (source
   (traverse (nav/parse-string "(defn hello "world" {:a 1} [])")
             '(defn ^:% symbol?
                ^{:? true :% true :- true} string?
                ^{:? true :% true :- true} map?
                ^:% vector? & _)))
  => '(defn hello [])

  (source
   (traverse (nav/parse-string "(defn hello [] (+ 1 1))")
             '(defn _ _ (+ | 1 & _))))
  => 1

  (source
   (traverse (nav/parse-string "(defn hello [] (+ 1 1))")
             '(#{defn} | & _)))
  => 'hello

  (source
   (traverse (nav/parse-string "(fact "hello world")")
             '(fact | & _)))
  => "hello world"
)

[[:subsection {:title "source" :link "merged-plans-slop-summary-code-query-traverse-tutorial-md-source"}]]

"`^{:refer code.query.traverse/source :added \"3.0\"}`"

"Retrieves the final source code (Clojure form) from a traversed `Position` record."

^{:id merged-plans-slop-summary-code-query-traverse-tutorial-md-example-17 :added "4.0"}
(fact "source example"
  (source
   (traverse (nav/parse-string "()")
             '(^:+ hello)))
  => '(hello)
)
;; END merged documentation: plans/slop/summary/code_query_traverse_tutorial.md

;; BEGIN merged documentation: plans/slop/summary/code_query_walk_tutorial.md
;; sha256: 464cc753f89247c9ede501841f4b42af36bddd231d9a4906a6e807ebeb25bf2e
[[:chapter {:title "code.query.walk Tutorial" :link "merged-plans-slop-summary-code-query-walk-tutorial-md"}]]

"**Module:** `code.query.walk`\n**Source File:** `src/code/query/walk.clj`\n**Test File:** `test/code/query/walk_test.clj`"

"The `code.query.walk` module provides functions for traversing and transforming `std.block` navigators (ASTs) based on patterns. It offers two primary walking strategies: `matchwalk` for deep, recursive traversal and `levelwalk` for top-level matching. These functions are essential for implementing automated code transformations and refactorings."

[[:section {:title "Core Concepts" :link "merged-plans-slop-summary-code-query-walk-tutorial-md-core-concepts"}]]

"*   **Navigator (`nav/`):** Functions from `code.query.block` (aliased as `nav`) are used to create and manipulate the AST context.\n*   **Matchers:** Predicates (created using `code.query.match` functions) that determine whether a block matches a specific pattern.\n*   **Transformation Function (`f`):** A function that takes a matching navigator and returns a transformed navigator.\n*   **Wrappers:** Functions like `wrap-meta` and `wrap-suppress` enhance the walking behavior by handling metadata and exceptions."

[[:section {:title "Functions" :link "merged-plans-slop-summary-code-query-walk-tutorial-md-functions"}]]

[[:subsection {:title "wrap-meta" :link "merged-plans-slop-summary-code-query-walk-tutorial-md-wrap-meta"}]]

"`^{:refer code.query.walk/wrap-meta :added \"3.0\"}`"

"A higher-order function that wraps a walk function to correctly handle metadata tags. It ensures that metadata forms are properly traversed and processed."

"No direct test example, but used internally by matchwalk and levelwalk."

[[:subsection {:title "wrap-suppress" :link "merged-plans-slop-summary-code-query-walk-tutorial-md-wrap-suppress"}]]

"`^{:refer code.query.walk/wrap-suppress :added \"3.0\"}`"

"A higher-order function that wraps a walk function to suppress exceptions during traversal, returning the original navigator in case of an error."

"No direct test example, but used internally by matchwalk and levelwalk."

[[:subsection {:title "matchwalk" :link "merged-plans-slop-summary-code-query-walk-tutorial-md-matchwalk"}]]

"`^{:refer code.query.walk/matchwalk :added \"3.0\"}`"

"Performs a deep, recursive traversal of the AST. It applies a transformation function `f` to every block that matches any of the provided `matchers`."

^{:id merged-plans-slop-summary-code-query-walk-tutorial-md-example-3 :added "4.0"}
(fact "matchwalk example"
  (-> (matchwalk (nav/parse-string "(+ (+ (+ 8 9)))")
                 [(match/compile-matcher '+)]
                 (fn [nav]
                   (-> nav nav/down (nav/replace '-) nav/up)))
      nav/value)
  => '(- (- (- 8 9)))
)

[[:subsection {:title "levelwalk" :link "merged-plans-slop-summary-code-query-walk-tutorial-md-levelwalk"}]]

"`^{:refer code.query.walk/levelwalk :added \"3.0\"}`"

"Performs a top-level traversal of the AST. It applies a transformation function `f` only to blocks at the current level that match the provided matcher."

^{:id merged-plans-slop-summary-code-query-walk-tutorial-md-example-4 :added "4.0"}
(fact "levelwalk example"
  (-> (levelwalk (nav/parse-string "(+ (+ (+ 8 9)))")
                 [(match/compile-matcher '+)]
                 (fn [nav]
                   (-> nav nav/down (nav/replace '-) nav/up)))
      nav/value)
  => '(- (+ (+ 8 9)))
)
;; END merged documentation: plans/slop/summary/code_query_walk_tutorial.md
