(ns gdscript.tutorial.example-core-test
  (:require [std.lib.env :as env]
            [gdscript.tutorial.example-core])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "godot-4"))})

(defn- in-core
  "Evaluates form inside the example-core namespace so that the !.gd macro
   and the Godot runtime set up by that namespace are used."
  [form]
  (binding [*ns* (find-ns 'gdscript.tutorial.example-core)]
    (eval form)))

^{:refer gdscript.tutorial.example-core/add :added "4.1"}
(fact "adds two numbers"
  (in-core '(!.gd (gdscript.tutorial.example-core/add 2 3)))
  => 5)

^{:refer gdscript.tutorial.example-core/factorial :added "4.1"}
(fact "computes factorial"
  (in-core '(!.gd (gdscript.tutorial.example-core/factorial 5)))
  => 120)

^{:refer gdscript.tutorial.example-core/sum-array :added "4.1"}
(fact "sums an array"
  (in-core '(!.gd (gdscript.tutorial.example-core/sum-array [1 2 3 4 5])))
  => 15)

^{:refer gdscript.tutorial.example-core/build-vector :added "4.1"}
(fact "builds a vector"
  (in-core '(!.gd (gdscript.tutorial.example-core/build-vector 1 2 3)))
  => [1 2 3])
