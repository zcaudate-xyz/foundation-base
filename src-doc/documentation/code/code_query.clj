(ns documentation.code-query
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

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "code.query"}]]
[[:api {:namespace "code.query.match"}]]
[[:api {:namespace "code.query.traverse"}]]
[[:api {:namespace "code.query.walk"}]]
[[:api {:namespace "code.query.compile"}]]
