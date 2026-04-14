(ns xt.runtime.type-hashmap-node-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-hashmap-node :as node]
             [xt.lang.common-spec :as xt]
             [xt.runtime.interface-common :as ic]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.type-hashmap-node :as node]
             [xt.lang.common-spec :as xt]
             [xt.runtime.interface-common :as ic]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-hashmap-node/node-create :added "4.1"}
(fact "creates a bitmap indexed node"
  ^:hidden

  (!.js
   (node/node-create 1 3 ["a" "b"]))
  => {"edit_id" 1
      "bitmap" 3
      "children" ["a" "b"]
      "::" "hashmap.node"}

  (!.lua
   (node/node-create 1 3 ["a" "b"]))
  => {"edit_id" 1
      "bitmap" 3
      "children" ["a" "b"]
      "::" "hashmap.node"})

^{:refer xt.runtime.type-hashmap-node/node-assoc :added "4.1"}
(fact "associates and replaces leaf entries"
  ^:hidden

  (!.js
   (var added-1 (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 (ic/hash "a") "a" 1))
   (var added-2 (node/node-assoc (. added-1 node) nil 0 (ic/hash "a") "a" 2))
   [(. added-1 added)
    (. added-2 added)
    (node/node-lookup (. added-2 node) 0 (ic/hash "a") "a" "missing")])
  => [true false 2]

  (!.lua
   (var added-1 (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 (ic/hash "a") "a" 1))
   (var added-2 (node/node-assoc (. added-1 node) nil 0 (ic/hash "a") "a" 2))
   [(. added-1 added)
    (. added-2 added)
    (node/node-lookup (. added-2 node) 0 (ic/hash "a") "a" "missing")])
  => [true false 2])

^{:refer xt.runtime.type-hashmap-node/branch-create :added "4.1"}
(fact "creates nested bitmap nodes and hash collisions"
  ^:hidden

  (!.js
   (var branch-a (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 1 "a" 1))
   (var branch-b (node/node-assoc (. branch-a node) nil 0 33 "b" 2))
   (var collide-a (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 11 "x" 10))
   (var collide-b (node/node-assoc (. collide-a node) nil 0 11 "y" 20))
   [(xt/x:get-key (xt/x:get-idx (. (. branch-b node) children) (xt/x:offset 0)) "::")
    (node/node-lookup (. branch-b node) 0 1 "a" "missing")
    (node/node-lookup (. branch-b node) 0 33 "b" "missing")
    (xt/x:get-key (xt/x:get-idx (. (. collide-b node) children) (xt/x:offset 0)) "::")
    (node/node-lookup (. collide-b node) 0 11 "x" "missing")
    (node/node-lookup (. collide-b node) 0 11 "y" "missing")])
  => ["hashmap.node" 1 2 "hashmap.collision" 10 20]

  (!.lua
   (var branch-a (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 1 "a" 1))
   (var branch-b (node/node-assoc (. branch-a node) nil 0 33 "b" 2))
   (var collide-a (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 11 "x" 10))
   (var collide-b (node/node-assoc (. collide-a node) nil 0 11 "y" 20))
   [(xt/x:get-key (xt/x:get-idx (. (. branch-b node) children) (xt/x:offset 0)) "::")
    (node/node-lookup (. branch-b node) 0 1 "a" "missing")
    (node/node-lookup (. branch-b node) 0 33 "b" "missing")
    (xt/x:get-key (xt/x:get-idx (. (. collide-b node) children) (xt/x:offset 0)) "::")
    (node/node-lookup (. collide-b node) 0 11 "x" "missing")
    (node/node-lookup (. collide-b node) 0 11 "y" "missing")])
  => ["hashmap.node" 1 2 "hashmap.collision" 10 20])

^{:refer xt.runtime.type-hashmap-node/node-dissoc :added "4.1"}
(fact "dissociates keys from leaf and collision nodes"
  ^:hidden

  (!.js
   (var collide-a (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 11 "x" 10))
   (var collide-b (node/node-assoc (. collide-a node) nil 0 11 "y" 20))
   (var removed-1 (node/node-dissoc (. collide-b node) nil 0 11 "x"))
   (var removed-2 (node/node-dissoc (. removed-1 node) nil 0 11 "y"))
   [(. removed-1 removed)
    (node/node-lookup (. removed-1 node) 0 11 "x" "missing")
    (node/node-lookup (. removed-1 node) 0 11 "y" "missing")
    (. removed-2 removed)])
  => [true "missing" 20 true]

  (!.lua
   (var collide-a (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 11 "x" 10))
   (var collide-b (node/node-assoc (. collide-a node) nil 0 11 "y" 20))
   (var removed-1 (node/node-dissoc (. collide-b node) nil 0 11 "x"))
   (var removed-2 (node/node-dissoc (. removed-1 node) nil 0 11 "y"))
   [(. removed-1 removed)
    (node/node-lookup (. removed-1 node) 0 11 "x" "missing")
    (node/node-lookup (. removed-1 node) 0 11 "y" "missing")
    (. removed-2 removed)])
  => [true "missing" 20 true])


^{:refer xt.runtime.type-hashmap-node/impl-mask :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/impl-bitpos :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/impl-index :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/impl-edit-allowed :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/leaf-create :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/collision-create :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/leaf-value :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/node-clone :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/node-editable :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/node-editable-root :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/ensure-editable :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/ensure-persistent :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/collision-find-leaf :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/collision-assoc :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/collision-dissoc :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/node-lookup :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashmap-node/node-find-leaf :added "4.1"}
(fact "TODO")