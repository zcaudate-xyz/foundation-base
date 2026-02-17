(ns documentation.std-lib-collection
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.collection` provides extended collection utilities beyond clojure.core, including:

- Advanced map operations (map-keys, map-vals, filter-keys, filter-vals)
- Nested data structure manipulation (merge-nested, flatten-nested)
- Sequence utilities (queue, seqify, unlazy)
- Tree operations (tree-flatten, tree-nestify)
- Data diffing and patching (diff, diff:patch, diff:unpatch)
"

[[:chapter {:title "Type Predicates" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [hash-map? lazy-seq? cons? form?]}]]

[[:chapter {:title "Sequence Utilities" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [queue seqify unseqify unlazy]}]]

[[:chapter {:title "Map Operations" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [map-keys map-vals map-juxt pmap-vals map-entries pmap-entries]}]]

[[:chapter {:title "Map Filtering" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [filter-keys filter-vals keep-vals]}]]

[[:chapter {:title "Key Operations" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [qualified-keys unqualified-keys qualify-keys unqualify-keys rename-keys]}]]

[[:chapter {:title "Nested Operations" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [assoc-new merge-nested merge-nested-new dissoc-nested flatten-nested]}]]

[[:chapter {:title "Tree Operations" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [tree-flatten tree-nestify tree-nestify:all reshape find-templates transform]}]]

[[:chapter {:title "Diff Operations" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [diff diff:changes diff:new diff:changed diff:patch diff:unpatch]}]]

[[:chapter {:title "Collection Manipulation" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [index-at element-at insert-at remove-at split-by transpose deduped? unfold]}]]
