(ns gdscript.core-test
  (:require [gdscript.core :as gd])
  (:use code.test))

^{:refer gdscript.core/+utility-functions+ :added "4.1"}
(fact "lists global utility functions"
  (count gd/+utility-functions+)
  => 114)

^{:refer gdscript.core/+builtin-classes+ :added "4.1"}
(fact "lists builtin value types"
  (count gd/+builtin-classes+)
  => 38)

^{:refer gdscript.core/+singletons+ :added "4.1"}
(fact "lists engine singletons"
  (count gd/+singletons+)
  => 39)

^{:refer gdscript.core/+classes-node3d+ :added "4.1"}
(fact "lists 3D node classes"
  (boolean (some #(= 'MeshInstance3D %) gd/+classes-node3d+))
  => true)

^{:refer gdscript.core/+classes-resource+ :added "4.1"}
(fact "lists resource classes"
  (boolean (some #(= 'BoxMesh %) gd/+classes-resource+))
  => true)
