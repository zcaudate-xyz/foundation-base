(ns xt.runtime.type-vector-node-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-vector-node :as node]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-iter :as it]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.type-vector-node :as node]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-iter :as it]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-vector-node/impl-mask :added "4.0"}
(fact "masks an integer value"

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

  (!.js
   (node/node-create 1 [1 2 3 4]))
  => {"edit_id" 1 "children" [1 2 3 4] "::" "vector.node"}

  (!.lua
   (node/node-create 1 [1 2 3 4]))
  => {"edit_id" 1 "children" [1 2 3 4] "::" "vector.node"})

^{:refer xt.runtime.type-vector-node/node-clone :added "4.0"}
(fact "clones the node"

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

  (!.js
   (node/node-editable-root
    (node/node-create 1 [1 2 3 4])))
  => (contains-in
      {"edit_id" number?
       "children" [1 2 3 4]
       "::" "vector.node"}))

^{:refer xt.runtime.type-vector-node/node-editable :added "4.0"}
(fact  "creates an editable node"

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

  (!.js
   (node/ensure-persistent (node/node-create 1 [])))
  => {"children" [], "::" "vector.node"}

  (!.lua
   (node/ensure-persistent (node/node-create 1 [])))
  => {"children" {}, "::" "vector.node"})

^{:refer xt.runtime.type-vector-node/node-array-for :added "4.0"}
(fact "gets the node array"

  (!.js
   (var root (node/node-create nil [(node/node-create nil [1 2 3 4])]))
   [(node/node-array-for root 36 5 [32 33 34 35] 2 false)
    (node/node-array-for root 36 5 [32 33 34 35] 34 false)
    (== nil (node/node-array-for root 36 5 [32 33 34 35] 40 false))])
  => [[1 2 3 4] [32 33 34 35] true]

  (!.lua
   (var root (node/node-create nil [(node/node-create nil [1 2 3 4])]))
   [(node/node-array-for root 36 5 [32 33 34 35] 2 false)
    (node/node-array-for root 36 5 [32 33 34 35] 34 false)
    (== nil (node/node-array-for root 36 5 [32 33 34 35] 40 false))])
  => [[1 2 3 4] [32 33 34 35] true])

^{:refer xt.runtime.type-vector-node/node-new-path :added "4.0"}
(fact "new path"

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
(fact "pushes an element onto node"

  (!.js
   (node/node-push-tail nil 32 5 node/EMPTY_VECTOR_NODE (node/node-create nil [1 2 3]) false))
  => {"children"
      [{"children" [1 2 3]
        "::" "vector.node"}]
      "::" "vector.node"}

  (!.lua
   (node/node-push-tail nil 32 5 node/EMPTY_VECTOR_NODE (node/node-create nil [1 2 3]) false))
  => {"children"
      [{"children" [1 2 3]
        "::" "vector.node"}]
      "::" "vector.node"})

^{:refer xt.runtime.type-vector-node/node-pop-tail :added "4.0"}
(fact "pops the last element off node"

  (!.js
   (var out (node/node-pop-tail nil 33 5 (node/node-create nil [(node/node-create nil [1 2 3])]) false))
   [(. out ["::"])
    (xt/x:len (. out children))])
  => ["vector.node" 0]

  (!.lua
   (var out (node/node-pop-tail nil 33 5 (node/node-create nil [(node/node-create nil [1 2 3])]) false))
   [(. out ["::"])
    (xt/x:len (. out children))])
  => ["vector.node" 0])

^{:refer xt.runtime.type-vector-node/node-assoc :added "4.0"}
(fact "associates a given node without mutating the original"

  (!.js
   (var root (node/node-create nil [1 2 3]))
   (var out (node/node-assoc root 0 1 9))
   [(. root children)
    (. out children)])
  => [[1 2 3] [1 9 3]]

  (!.lua
   (var root (node/node-create nil [1 2 3]))
   (var out (node/node-assoc root 0 1 9))
   [(. root children)
    (. out children)])
  => [[1 2 3] [1 9 3]])
