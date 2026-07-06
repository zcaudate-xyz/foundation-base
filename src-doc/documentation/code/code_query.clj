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
