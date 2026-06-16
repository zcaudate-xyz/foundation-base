(ns documentation.std-lib-foundation
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.foundation` provides basic predicates, constructors, and helpers used across the foundation libraries."

[[:chapter {:title "Constants and Combinators" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [T F NIL U Z xor]}]]

[[:chapter {:title "Identifiers" :link "std.lib.foundation"}]]

"`sid`, `uuid`, and `flake` generate short and unique identifiers."

[[:api {:namespace "std.lib.foundation"
        :only [sid uuid uuid-nil flake]}]]

[[:chapter {:title "Constructors" :link "std.lib.foundation"}]]

"Construct common Java and Clojure values."

[[:api {:namespace "std.lib.foundation"
        :only [instant date uri url counter]}]]

[[:chapter {:title "Coercion" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [strn keyword string edn edn?]}]]

[[:chapter {:title "Type Predicates" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [byte? short? long? bigint? bigdec? regexp? iobj? iref? ideref? thread? url? atom? comparable? array?]}]]

[[:chapter {:title "Parsing" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [parse-long parse-double]}]]

[[:chapter {:title "Invocation" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [invoke call]}]]

[[:chapter {:title "Errors" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [error suppress with-ex with-thrown throwable?]}]]

[[:chapter {:title "Utilities" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [hash-id hash-code demunge aget var-sym unbound?]}]]
