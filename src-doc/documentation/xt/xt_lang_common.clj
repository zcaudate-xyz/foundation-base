(ns documentation.xt-lang-common
  (:use code.test))

[[:hero {:title "xt.lang.common"
         :subtitle "Data, iteration, math, string, tree, and utility layers."
         :lead "The `xt.lang.common-*` namespaces are the everyday standard library for generated xtalk programs."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Portable programs need predictable helpers for maps, arrays, object traversal, string formatting, sorting, tracing, and tree operations. These namespaces smooth over target runtime differences."

[[:chapter {:title "Examples and usage" :link "usage"}]]

"Use common-data for object and array access, common-iter for loops, common-string for portable string behavior, common-sort-by/topo for ordering, and common-tree for nested structures. Pages in xt.db and xt.substrate build on these helpers heavily."

[[:chapter {:title "API" :link "api"}]]

