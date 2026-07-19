(ns kmi.lang.type-hashmap-node-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.type-hashmap-node :as node]
             [xt.lang.spec-base :as xt]
             [kmi.lang.common-util :as ic]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.type-hashmap-node/impl-mask :added "4.1"}
(fact "masks the hash at the given shift"

  (!.js
   [(node/impl-mask 33 0)
    (node/impl-mask 33 5)
    (node/impl-mask 32 0)])
  => [1 1 0])

^{:refer kmi.lang.type-hashmap-node/impl-bitpos :added "4.1"}
(fact "computes the bit position for a hash fragment"

  (!.js
   [(node/impl-bitpos 33 0)
    (node/impl-bitpos 33 5)
    (node/impl-bitpos 32 0)])
  => [2 2 1])

^{:refer kmi.lang.type-hashmap-node/impl-index :added "4.1"}
(fact "counts the set bits before a bit position"

  (!.js
   (var bitmap (xt/x:bit-or 2 8))
   [(node/impl-index bitmap 2)
    (node/impl-index bitmap 8)
    (node/impl-index (xt/x:bit-or bitmap 16) 16)])
  => [0 1 2])

^{:refer kmi.lang.type-hashmap-node/impl-edit-allowed :added "4.1"}
(fact "checks if a node can be edited under the current edit id"

  (!.js
   [(node/impl-edit-allowed 1 1)
    (node/impl-edit-allowed nil 1)
    (node/impl-edit-allowed 1 2)])
  => [true false false])

^{:refer kmi.lang.type-hashmap-node/node-create :added "4.1"}
(fact "creates a bitmap indexed node"

  (!.js
   (node/node-create 1 3 ["a" "b"]))
  => {"edit_id" 1
      "bitmap" 3
      "children" ["a" "b"]
      "::" "hashmap.node"})

^{:refer kmi.lang.type-hashmap-node/leaf-create :added "4.1"}
(fact "creates a normalised leaf"

  (!.js
   (var leaf (node/leaf-create 11 "a" nil))
   [(xt/x:get-key leaf "::")
    (xt/x:get-key leaf "_hash")
    (xt/x:get-key leaf "_key")
    (== nil (node/leaf-value leaf))])
  => ["hashmap.leaf" 11 "a" true])

^{:refer kmi.lang.type-hashmap-node/collision-create :added "4.1"}
(fact "creates a collision node"

  (!.js
   (var out (node/collision-create 7 11 [(node/leaf-create 11 "a" 1)
                                         (node/leaf-create 11 "b" 2)]))
   [(xt/x:get-key out "::")
    (xt/x:get-key out "_hash")
    (xt/x:get-key out "edit_id")
    (xt/x:len (xt/x:get-key out "children"))])
  => ["hashmap.collision" 11 7 2])

^{:refer kmi.lang.type-hashmap-node/leaf-value :added "4.1"}
(fact "retrieves denormalised values from leaves"

  (!.js
   [(node/leaf-value (node/leaf-create 11 "a" 1))
    (== nil (node/leaf-value (node/leaf-create 11 "b" nil)))])
  => [1 true])

^{:refer kmi.lang.type-hashmap-node/node-clone :added "4.1"}
(fact "clones node and collision children arrays"

  (!.js
   (var node-0 (node/node-create nil 1 ["a"]))
   (var node-1 (node/node-clone node-0))
   (xt/x:arr-push (xt/x:get-key node-1 "children") "b")
   (var col-0 (node/collision-create nil 11 [(node/leaf-create 11 "a" 1)]))
   (var col-1 (node/node-clone col-0))
   (xt/x:arr-push (xt/x:get-key col-1 "children") (node/leaf-create 11 "b" 2))
   [(xt/x:len (xt/x:get-key node-0 "children"))
    (xt/x:len (xt/x:get-key node-1 "children"))
    (xt/x:len (xt/x:get-key col-0 "children"))
    (xt/x:len (xt/x:get-key col-1 "children"))])
  => [1 2 1 2])

^{:refer kmi.lang.type-hashmap-node/node-editable :added "4.1"}
(fact "creates an editable node tree with the given edit id"

  (!.js
   (var base (xt/x:get-key (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 (ic/hash "a") "a" 1) "node"))
   (var out (node/node-editable base 99))
   [(xt/x:get-key out "edit_id")
    (node/node-lookup out 0 (ic/hash "a") "a" "missing")])
  => [99 1])

^{:refer kmi.lang.type-hashmap-node/node-editable-root :added "4.1"}
(fact "creates an editable root with a random edit id"

  (!.js
   (var base (xt/x:get-key (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 (ic/hash "a") "a" 1) "node"))
   (var out (node/node-editable-root base))
   [(xt/x:not-nil? (xt/x:get-key out "edit_id"))
    (node/node-lookup out 0 (ic/hash "a") "a" "missing")])
  => [true 1])

^{:refer kmi.lang.type-hashmap-node/ensure-editable :added "4.1"}
(fact "throws when the node is not editable"

  (!.js
   (node/ensure-editable
    node/EMPTY_HASHMAP_NODE))
  => (throws))

^{:refer kmi.lang.type-hashmap-node/ensure-persistent :added "4.1"}
(fact "removes edit ids from editable nodes"

  (!.js
   (var base (xt/x:get-key (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 (ic/hash "a") "a" 1) "node"))
   (var out (node/ensure-persistent (node/node-editable base 99)))
   [(xt/x:nil? (xt/x:get-key out "edit_id"))
    (node/node-lookup out 0 (ic/hash "a") "a" "missing")])
  => [true 1])

^{:refer kmi.lang.type-hashmap-node/collision-find-leaf :added "4.1"}
(fact "finds leaves within a collision node"

  (!.js
   (var col (node/collision-create nil 11 [(node/leaf-create 11 "a" 1)
                                           (node/leaf-create 11 "b" 2)]))
   (var leaf (node/collision-find-leaf col "b"))
   [(xt/x:get-key leaf "_key")
    (node/leaf-value leaf)
    (== nil (node/collision-find-leaf col "c"))])
  => ["b" 2 true])

^{:refer kmi.lang.type-hashmap-node/collision-assoc :added "4.1"}
(fact "adds or replaces leaves within a collision node"

  (!.js
   (var col-0 (node/collision-create nil 11 [(node/leaf-create 11 "a" 1)]))
   (var res-1 (node/collision-assoc col-0 nil (node/leaf-create 11 "b" 2)))
   (var res-2 (node/collision-assoc (xt/x:get-key res-1 "node") nil (node/leaf-create 11 "b" 3)))
   [(xt/x:get-key res-1 "added")
    (xt/x:get-key res-2 "added")
    (xt/x:len (xt/x:get-key (xt/x:get-key res-2 "node") "children"))
    (node/leaf-value (node/collision-find-leaf (xt/x:get-key res-2 "node") "b"))])
  => [true false 2 3])

^{:refer kmi.lang.type-hashmap-node/collision-dissoc :added "4.1"}
(fact "removes leaves from a collision node"

  (!.js
   (var col (node/collision-create nil 11 [(node/leaf-create 11 "a" 1)
                                           (node/leaf-create 11 "b" 2)]))
   (var res-0 (node/collision-dissoc col nil "c"))
   (var res-1 (node/collision-dissoc col nil "a"))
   [(xt/x:get-key res-0 "removed")
    (xt/x:get-key res-1 "removed")
    (xt/x:get-key (xt/x:get-key res-1 "node") "::")
    (xt/x:get-key (xt/x:get-key res-1 "node") "_key")])
  => [false true "hashmap.leaf" "b"])

^{:refer kmi.lang.type-hashmap-node/branch-create :added "4.1"}
(fact "creates nested bitmap nodes and hash collisions"

  (!.js
   (var branch-a (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 1 "a" 1))
   (var branch-b (node/node-assoc (xt/x:get-key branch-a "node") nil 0 33 "b" 2))
   (var collide-a (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 11 "x" 10))
   (var collide-b (node/node-assoc (xt/x:get-key collide-a "node") nil 0 11 "y" 20))
   [(xt/x:get-key (xt/x:get-idx (xt/x:get-key (xt/x:get-key branch-b "node") "children") (xt/x:offset 0)) "::")
    (node/node-lookup (xt/x:get-key branch-b "node") 0 1 "a" "missing")
    (node/node-lookup (xt/x:get-key branch-b "node") 0 33 "b" "missing")
    (xt/x:get-key (xt/x:get-idx (xt/x:get-key (xt/x:get-key collide-b "node") "children") (xt/x:offset 0)) "::")
    (node/node-lookup (xt/x:get-key collide-b "node") 0 11 "x" "missing")
    (node/node-lookup (xt/x:get-key collide-b "node") 0 11 "y" "missing")])
  => ["hashmap.node" 1 2 "hashmap.collision" 10 20])

^{:refer kmi.lang.type-hashmap-node/node-lookup :added "4.1"}
(fact "looks up keys through branch and collision nodes"

  (!.js
   (var root-a (xt/x:get-key (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 1 "a" 1) "node"))
   (var root-b (xt/x:get-key (node/node-assoc root-a nil 0 33 "b" 2) "node"))
   [(node/node-lookup root-b 0 1 "a" "missing")
    (node/node-lookup root-b 0 33 "b" "missing")
    (node/node-lookup root-b 0 99 "z" "missing")])
  => [1 2 "missing"])

^{:refer kmi.lang.type-hashmap-node/node-find-leaf :added "4.1"}
(fact "finds leaves through branch and collision nodes"

  (!.js
   (var root-a (xt/x:get-key (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 11 "a" 1) "node"))
   (var root-b (xt/x:get-key (node/node-assoc root-a nil 0 11 "b" 2) "node"))
   (var leaf (node/node-find-leaf root-b 0 11 "b"))
   [(xt/x:get-key leaf "_key")
    (node/leaf-value leaf)
    (== nil (node/node-find-leaf root-b 0 11 "c"))])
  => ["b" 2 true])

^{:refer kmi.lang.type-hashmap-node/node-assoc :added "4.1"}
(fact "associates and replaces leaf entries"

  (!.js
   (var added-1 (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 (ic/hash "a") "a" 1))
   (var added-2 (node/node-assoc (xt/x:get-key added-1 "node") nil 0 (ic/hash "a") "a" 2))
   [(xt/x:get-key added-1 "added")
    (xt/x:get-key added-2 "added")
    (node/node-lookup (xt/x:get-key added-2 "node") 0 (ic/hash "a") "a" "missing")])
  => [true false 2])

^{:refer kmi.lang.type-hashmap-node/node-dissoc :added "4.1"}
(fact "dissociates keys from leaf and collision nodes"

  (!.js
   (var collide-a (node/node-assoc node/EMPTY_HASHMAP_NODE nil 0 11 "x" 10))
   (var collide-b (node/node-assoc (xt/x:get-key collide-a "node") nil 0 11 "y" 20))
   (var removed-1 (node/node-dissoc (xt/x:get-key collide-b "node") nil 0 11 "x"))
   (var removed-2 (node/node-dissoc (xt/x:get-key removed-1 "node") nil 0 11 "y"))
   [(xt/x:get-key removed-1 "removed")
    (node/node-lookup (xt/x:get-key removed-1 "node") 0 11 "x" "missing")
    (node/node-lookup (xt/x:get-key removed-1 "node") 0 11 "y" "missing")
    (xt/x:get-key removed-2 "removed")])
  => [true "missing" 20 true])
