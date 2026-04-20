(ns
 xtbench.lua.lang.common-sort-topo-test
 (:require [std.lang :as l])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic, :require [[xt.lang.common-sort-topo :as topo]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-sort-topo/sort-edges-build, :added "4.1"}
(fact
 "builds an edge with links"
 (!.lua (var out {}) (topo/sort-edges-build out ["a" "b"]) out)
 =>
 {"a" {"id" "a", "links" ["b"]}, "b" {"id" "b", "links" {}}})

^{:refer xt.lang.common-sort-topo/sort-edges-visit, :added "4.1"}
(fact
 "visits nodes and pushes the sorted ids"
 (!.lua
  (var
   nodes
   {"a" {"id" "a", "links" ["b"]},
    "b" {"id" "b", "links" ["c"]},
    "c" {"id" "c", "links" []}})
  (var visited {})
  (var sorted [])
  (topo/sort-edges-visit nodes visited sorted "a" nil)
  [visited sorted])
 =>
 [{"a" true, "b" true, "c" true} ["a" "b" "c"]])

^{:refer xt.lang.common-sort-topo/sort-edges, :added "4.1"}
(fact
 "sorts edges given a list"
 (!.lua (topo/sort-edges [["a" "b"] ["b" "c"] ["c" "d"] ["d" "e"]]))
 =>
 ["a" "b" "c" "d" "e"])

^{:refer xt.lang.common-sort-topo/sort-topo, :added "4.1"}
(fact
 "sorts in topological order"
 (!.lua (topo/sort-topo [["a" ["b" "c"]] ["c" ["b"]]]))
 =>
 ["b" "c" "a"])
