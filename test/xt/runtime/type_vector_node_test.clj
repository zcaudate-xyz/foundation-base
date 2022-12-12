(ns xt.runtime.type-vector-node-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-vector-node :as node]
             [xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.lang.base-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.type-vector-node :as node]
             [xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-vector-node/impl-mask :added "4.0"}
(fact "masks an integer value"
  ^:hidden
  
  (!.js
   [(node/impl-mask -1)
    (node/impl-mask 5)
    (node/impl-mask 32)])
  => [31 5 0]

  (!.lua
   [(node/impl-mask -1)
    (node/impl-mask 5)
    (node/impl-mask 32)])
  => [31 5 0])

^{:refer xt.runtime.type-vector-node/impl-offset :added "4.0"}
(fact "gets the tail off"
  ^:hidden
  
  (!.js
   [(node/impl-offset 0)
    (node/impl-offset 3)
    (node/impl-offset 31)
    (node/impl-offset 33)
    (node/impl-offset 156)])
  => [0 0 0 32 128]

  (!.lua
   [(node/impl-offset 0)
    (node/impl-offset 3)
    (node/impl-offset 31)
    (node/impl-offset 33)
    (node/impl-offset 156)])
  => [0 0 0 32 128])

^{:refer xt.runtime.type-vector-node/node-create :added "4.0"}
(fact "creates a new node"
  ^:hidden
  
  (!.js
   (node/node-create 1 [1 2 3 4]))
  => {"edit_id" 1 "children" [1 2 3 4] "::" "vector.node"}

  (!.lua
   (node/node-create 1 [1 2 3 4]))
  => {"edit_id" 1 "children" [1 2 3 4] "::" "vector.node"})

^{:refer xt.runtime.type-vector-node/node-clone :added "4.0"}
(fact "clones the node"
  ^:hidden
  
  (!.js
   (node/node-clone
    (node/node-create 1 [1 2 3 4])))
  => {"edit_id" 1 "children" [1 2 3 4] "::" "vector.node"}
  
  (!.lua
   (node/node-clone
    (node/node-create 1 [1 2 3 4])))
  => {"edit_id" 1 "children" [1 2 3 4] "::" "vector.node"})

^{:refer xt.runtime.type-vector-node/node-editable-root :added "4.0"}
(fact "creates an editable root"
  ^:hidden
  
  (!.js
   (node/node-editable-root
    (node/node-create 1 [1 2 3 4])))
  => (contains-in
      {"edit_id" number?
       "children" [1 2 3 4]
       "::" "vector.node"}))

^{:refer xt.runtime.type-vector-node/node-editable :added "4.0"}
(fact  "creates an editable node"
  ^:hidden
  
  (!.js
   (var node (node/node-create 1 [1 2 3 4]))
   (== node (node/node-editable
             node
             1)))
  => true

  (!.lua
   (var node (node/node-create 1 [1 2 3 4]))
   (== node (node/node-editable
             node
             1)))
  => true)

^{:refer xt.runtime.type-vector-node/ensure-editable :added "4.0"}
(fact "ensures that the node is editable"
  ^:hidden
  
  (!.js
   (node/ensure-editable
    (node/node-create nil [1 2 3])))
  => (throws)

  (!.lua
   (node/ensure-editable
    (node/node-create nil [1 2 3])))
  => (throws))

^{:refer xt.runtime.type-vector-node/ensure-persistent :added "4.0"}
(fact "ensures that the node is not editable"
  ^:hidden
  
  (!.js
   (node/ensure-persistent (node/node-create 1 [])))
  => {"children" [], "::" "vector.node"}

  (!.lua
   (node/ensure-persistent (node/node-create 1 [])))
  => {"children" {}, "::" "vector.node"})

^{:refer xt.runtime.type-vector-node/node-array-for :added "4.0"}
(fact "gets the node array")

^{:refer xt.runtime.type-vector-node/node-new-path :added "4.0"}
(fact "new path"
  ^:hidden
  
  (!.js
   (node/node-new-path nil 5 (node/node-create nil [1 2 3])))
  => {"children"
      [{
        "children" [1 2 3]
        "::" "vector.node"}]
      "::" "vector.node"}

  (!.lua
   (node/node-new-path nil 5 (node/node-create nil [1 2 3])))
  => {"children"
      [{
        "children" [1 2 3]
        "::" "vector.node"}]
      "::" "vector.node"})

^{:refer xt.runtime.type-vector-node/node-push-tail :added "4.0"}
(fact "pushes an element onto node")

^{:refer xt.runtime.type-vector-node/node-pop-tail :added "4.0"}
(fact "pops the last element off node")

^{:refer xt.runtime.type-vector-node/node-assoc :added "4.0"}
(fact "associates a given node")

