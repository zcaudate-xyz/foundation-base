(ns std.lib.zip-test
  (:use code.test)
  (:require [std.lib.zip :refer :all])
  (:refer-clojure :exclude [find get]))

^{:refer std.lib.zip/register-type :added "4.0"}
(fact "registers a zip type")

^{:refer std.lib.zip/unregister-type :added "4.0"}
(fact "unregisters a zip type")

^{:refer std.lib.zip/check-context :added "3.0"}
(fact "checks that the zipper contains valid functions"
  ^:hidden
  
  (check-context {})
  => (throws))

^{:refer std.lib.zip/check-optional :added "3.0"}
(fact "checks that the meta contains valid functions"
  ^:hidden

  (check-optional {})
  => (throws))

^{:refer std.lib.zip/zipper? :added "3.0" :class [:zip/general]}
(fact "checks to see if an object is a zipper"
  ^:hidden

  (zipper? 1)
  => false)

^{:refer std.lib.zip/zipper :added "3.0"}
(fact "constructs a zipper")

^{:refer std.lib.zip/unwrap-element :added "4.0"}
(fact "unwraps an element")

^{:refer std.lib.zip/left-element :added "3.0" :class [:zip/element]}
(fact "element directly left of current position"
  ^:hidden
  
  (-> (from-status '[1 2 3 | 4])
      (left-element))
  => 3

  (-> (from-status '[1 [2 [| 3]]])
      (left-element))
  => nil

  (-> (from-status '[1 [2 [nil | 3]]])
      (left-element))
  => nil)

^{:refer std.lib.zip/right-element :added "3.0" :class [:zip/element]}
(fact "element directly right of current position"
  ^:hidden

  (-> (from-status '[1 2 3 | 4])
      (right-element))
  => 4)

^{:refer std.lib.zip/left-elements :added "3.0" :class [:zip/element]}
(fact "all elements left of current position"
  ^:hidden

  (-> (from-status '[1 2 | 3 4])
      (left-elements))
  => '(1 2))

^{:refer std.lib.zip/right-elements :added "3.0" :class [:zip/element]}
(fact "all elements right of current position"
  ^:hidden

  (-> (from-status '[1 2 | 3 4])
      (right-elements))
  => '(3 4))

^{:refer std.lib.zip/current-elements :added "3.0" :class [:zip/element]}
(fact "all elements left and right of current position"
  ^:hidden

  (-> (from-status '[1 2 | 3 4])
      (current-elements))
  => '(1 2 3 4)

  (-> (from-status '[1 [2 | 3] 4])
      (current-elements))
  => '(2 3))

^{:refer std.lib.zip/is :added "3.0" :class [:zip/general]}
(fact "checks zip given a predicate"
  ^:hidden

  (-> (vector-zip [0 1 2 3 4])
      (step-inside)
      (is zero?))
  => true)

^{:refer std.lib.zip/get :added "3.0" :class [:zip/general]}
(fact "gets the value of the zipper"
  ^:hidden

  (-> (vector-zip [0 1 2 3 4])
      (step-inside)
      (get))
  => 0)

^{:refer std.lib.zip/is-container? :added "3.0" :class [:zip/general]}
(fact "checks if node on either side is a container"

  (-> (vector-zip [1 2 3])
      (is-container? :right))
  => true

  (-> (vector-zip [1 2 3])
      (is-container? :left))
  => false)

^{:refer std.lib.zip/is-empty-container? :added "3.0" :class [:zip/general]}
(fact "check if current container is empty"

  (-> (vector-zip [])
      (is-empty-container?))
  => true)

^{:refer std.lib.zip/at-left-most? :added "3.0" :class [:zip/move]}
(fact "check if at left-most point of a container"
  ^:hidden

  (-> (from-status [1 2 ['| 3 4] 5 6])
      
      (at-left-most?))
  => true)

^{:refer std.lib.zip/at-right-most? :added "3.0" :class [:zip/move]}
(fact "check if at right-most point of a container"
  ^:hidden

  (-> (from-status '[1 2 [3 4 |]])
      (at-right-most?))
  => true)

^{:refer std.lib.zip/at-inside-most? :added "3.0" :class [:zip/move]}
(fact "check if at inside-most point of a container"
  ^:hidden

  (-> (from-status '[1 2 [3 4 |]])
      (at-inside-most?))
  => true)

^{:refer std.lib.zip/at-inside-most-left? :added "3.0" :class [:zip/move]}
(fact "check if at inside-most left point of a container"
  ^:hidden

  (-> (from-status '[1 2 [| 1 2]])
      (at-inside-most-left?))
  => true)

^{:refer std.lib.zip/at-outside-most? :added "3.0" :class [:zip/move]}
(fact "check if at outside-most point of the tree"
  ^:hidden

  (-> (from-status [1 2 [3 4 '|]])
      (at-outside-most?))
  => false

  (-> (from-status '[1 2 [3 4 |]])
      (step-outside)
      (step-outside)
      (at-outside-most?))
  => true)

^{:refer std.lib.zip/seq-zip :added "3.0" :class [:zip/general]}
(fact "constructs a sequence zipper"
  ^:hidden

  (seq-zip '(1 2 3 4 5))
  => (contains {:left (),
                :right '((1 2 3 4 5))}))

^{:refer std.lib.zip/vector-zip :added "3.0" :class [:zip/general]}
(fact "constructs a vector based zipper"
  ^:hidden

  (vector-zip [1 2 3 4 5])
  => (contains {:left (),
                :right '([1 2 3 4 5])}))

^{:refer std.lib.zip/list-child-elements :added "3.0" :class [:zip/element]}
(fact "lists elements of a container "
  ^:hidden

  (-> (vector-zip [1 2 3])
      (list-child-elements :right))
  => '(1 2 3)

  (-> (vector-zip 1)
      (list-child-elements :right))
  => (throws))

^{:refer std.lib.zip/update-child-elements :added "3.0" :class [:zip/element]}
(fact "updates elements of a container"
  ^:hidden

  (-> (vector-zip [1 2])
      (update-child-elements [1 2 3 4] :right)
      (right-element))
  => [1 2 3 4])

^{:refer std.lib.zip/can-step-left? :added "3.0" :class [:zip/move]}
(fact "check if can step left from current status"

  (-> (from-status '[1 2 [3 | 4]])
      (can-step-left?))
  => true

  (-> (from-status '[1 2 [| 3 4]])
      (can-step-left?))
  => false)

^{:refer std.lib.zip/can-step-right? :added "3.0" :class [:zip/move]}
(fact "check if can step right from current status"

  (-> (from-status '[1 2 [3 | 4]])
      (can-step-right?))
  => true

  (-> (from-status '[1 2 [3 4 |]])
      (can-step-right?))
  => false)

^{:refer std.lib.zip/can-step-inside? :added "3.0" :class [:zip/move]}
(fact "check if can step down from current status"
  ^:hidden

  (-> (from-status '[1 2 [3 4 |]])
      (can-step-inside?))
  => false

  (-> (from-status '[1 2 | [3 4]])
      (can-step-inside?))
  => true)

^{:refer std.lib.zip/can-step-inside-left? :added "3.0" :class [:zip/move]}
(fact "check if can step left inside a container"
  ^:hidden

  (-> (from-status '[[3 4] |])
      (can-step-inside-left?))
  => true)

^{:refer std.lib.zip/can-step-outside? :added "3.0" :class [:zip/move]}
(fact "check if can step up from current status"
  ^:hidden

  (-> (from-status '[1 2 [3 4 |]])
      (can-step-outside?))
  => true)

^{:refer std.lib.zip/step-left :added "3.0" :class [:zip/move]}
(fact "step left from current status"
  ^:hidden

  (-> (from-status '[1 2 [3 4 |]])
      (step-left)
      (status))
  => '([1 2 [3 | 4]]))

^{:refer std.lib.zip/step-right :added "3.0" :class [:zip/move]}
(fact "step right from current status"
  ^:hidden

  (-> (from-status '[1 2 [| 3 4]])
      (step-right)
      (status))
  => '([1 2 [3 | 4]]))

^{:refer std.lib.zip/step-inside :added "3.0" :class [:zip/move]}
(fact "step down from current status"
  ^:hidden

  (-> (from-status '[1 2 | [3 4]])
      (step-inside)
      (status))
  => '([1 2 [| 3 4]]))

^{:refer std.lib.zip/step-inside-left :added "3.0" :class [:zip/move]}
(fact "steps into the form on the left side"
  ^:hidden

  (-> (from-status '[[1 2] |])
      (step-inside-left)
      (status))
  => '([[1 2 |]]))

^{:refer std.lib.zip/step-outside :added "3.0" :class [:zip/move]}
(fact "step out to the current container"
  ^:hidden

  (-> (from-status '[1 2 [| 3 4]])
      (step-outside)
      (status))
  => '([1 2 | [3 4]]))

^{:refer std.lib.zip/step-outside-right :added "3.0" :class [:zip/move]}
(fact "the right of the current container"
  ^:hidden

  (-> (from-status '[1 2 [| 3 4]])
      (step-outside-right)
      (status))
  => '([1 2 [3 4] |]))

^{:refer std.lib.zip/step-left-most :added "3.0" :class [:zip/move]}
(fact "step to left-most point of current container"
  ^:hidden

  (-> (from-status '[1 2 [3 4 |]])
      (step-left-most)
      (status))
  => '([1 2 [| 3 4]]))

^{:refer std.lib.zip/step-right-most :added "3.0" :class [:zip/move]}
(fact "step to right-most point of current container"
  ^:hidden

  (-> (from-status '[1 2 [| 3 4]])
      (step-right-most)
      (status))
  => '([1 2 [3 4 |]]))

^{:refer std.lib.zip/step-inside-most :added "3.0" :class [:zip/move]}
(fact "step to at-inside-most point of current container"
  ^:hidden

  (-> (from-status '[1 2 | [[3] 4]])
      (step-inside-most)
      (status))
  => '([1 2 [[| 3] 4]]))

^{:refer std.lib.zip/step-inside-most-left :added "3.0" :class [:zip/move]}
(fact "steps all the way inside to the left side"
  ^:hidden

  (-> (from-status '[[1 [2]] | 3 4])
      (step-inside-most-left)
      (status))
  => '([[1 [2 |]] 3 4]))

^{:refer std.lib.zip/step-outside-most :added "3.0" :class [:zip/move]}
(fact "step to outside-most point of the tree"
  ^:hidden

  (-> (from-status '[1 2 [| 3 4]])
      (step-outside-most)
      (status))
  => '(| [1 2 [3 4]]))

^{:refer std.lib.zip/step-outside-most-right :added "3.0" :class [:zip/move]}
(fact "step to outside-most point of the tree to the right"
  ^:hidden

  (-> (from-status '[1 2 [| 3 4]])
      (step-outside-most-right)
      (status))
  => '([1 2 [3 4]] |))

^{:refer std.lib.zip/step-end :added "3.0" :class [:zip/move]}
(fact "steps status to container directly at end"
  ^:hidden

  (->> (from-status '[1 | [[]]])
       (step-end)
       (status))
  => '([1 [[|]]]))

^{:refer std.lib.zip/insert-left :added "3.0" :class [:zip/edit]}
(fact "insert element/s left of the current status"
  ^:hidden

  (-> (from-status '[1 2  [[| 3] 4]])
      (insert-left 1 2 3)
      (status))
  => '([1 2 [[1 2 3 | 3] 4]]))

^{:refer std.lib.zip/insert-right :added "3.0" :class [:zip/edit]}
(fact "insert element/s right of the current status"
  ^:hidden

  (-> (from-status '[| 1 2 3])
      (insert-right 1 2 3)
      (status))
  => '([| 3 2 1 1 2 3]))

^{:refer std.lib.zip/delete-left :added "3.0" :class [:zip/edit]}
(fact "delete element/s left of the current status"
  ^:hidden

  (-> (from-status '[1 2 | 3])
      (delete-left)
      (status))
  => '([1 | 3]))

^{:refer std.lib.zip/delete-right :added "3.0" :class [:zip/edit]}
(fact "delete element/s right of the current status"
  ^:hidden

  (-> (from-status '[1 2 | 3])
      (delete-right)
      (status))
  => '([1 2 |]))

^{:refer std.lib.zip/replace-left :added "3.0" :class [:zip/edit]}
(fact "replace element left of the current status"
  ^:hidden

  (-> (from-status '[1 2 | 3])
      (replace-left "10")
      (status))
  => '([1 "10" | 3]))

^{:refer std.lib.zip/replace-right :added "3.0" :class [:zip/edit]}
(fact "replace element right of the current status"
  ^:hidden

  (-> (from-status '[1 2 | 3])
      (replace-right "10")
      (status))
  => '([1 2 | "10"]))

^{:refer std.lib.zip/hierarchy :added "3.0" :class [:zip/general]}
(fact "replace element right of the current status"
  ^:hidden

  (->> (from-status '[1 [[|]]])
       (hierarchy)
       (map right-element))
  => [[] [[]] [1 [[]]]])

^{:refer std.lib.zip/at-end? :added "3.0" :class [:zip/move]}
(fact "replace element right of the current status"
  ^:hidden
  
  (->> (from-status '[1 [[|]]])
       (at-end?))
  => true

  (->> (from-status '[1 [[[2 |]] 3]])
       (at-end?))
  => false

  (->> (from-status '[1 [[[2 |]] 3]])
       (at-end?)))

^{:refer std.lib.zip/surround :added "3.0" :class [:zip/edit]}
(fact "nests elements in current block within another container"
  ^:hidden

  (-> (vector-zip 3)
      (insert-left 1 2)
      (surround)
      (status))
  => '(| [1 2 3])

  (->> (from-status '[1 [1 2 | 3 4]])
       (surround)
       (status))
  => '([1 [| [1 2 3 4]]]))

^{:refer std.lib.zip/root-element :added "3.0" :class [:zip/general]}
(fact "accesses the top level node"
  ^:hidden

  (-> (vector-zip [[[3] 2] 1])
      (step-inside-most)
      (root-element))
  => [[[3] 2] 1])

^{:refer std.lib.zip/status :added "3.0" :class [:zip/general]}
(fact "returns the form with the status showing"
  ^:hidden

  (-> (vector-zip [1 [[2] 3]])
      (step-inside)
      (step-right)
      (step-inside)
      (step-inside)
      (status))
  => '([1 [[| 2] 3]]))

^{:refer std.lib.zip/status-string :added "3.0" :class [:zip/general]}
(fact "returns the string form of the status"
  ^:hidden

  (-> (vector-zip [1 [[2] 3]])
      (step-inside)
      (step-right)
      (status-string))
  => "[1 | [[2] 3]]")

^{:refer std.lib.zip/display-zipper :added "4.0"}
(fact "displays the zipper")

^{:refer std.lib.zip/step-next :added "3.0" :class [:zip/move]}
(fact "step status through the tree in depth first order"
  ^:hidden

  (->> (from-status '[| 1 [2 [6 7] 3] [4 5]])
       (iterate step-next)
       (take-while identity)
       (map right-element))
  => '(1 [2 [6 7] 3] 2 [6 7] 6 7 3 [4 5] 4 5))

^{:refer std.lib.zip/step-prev :added "3.0" :class [:zip/move]}
(fact "step status in reverse through the tree in depth first order"
  ^:hidden

  (->> (from-status '[1 [2 [6 7] 3] [4 | 5]])
       (iterate step-prev)
       (take 10)
       (map right-element))
  => '(5 4 [4 5] 3 7 6 [6 7] 2 [2 [6 7] 3] 1))

^{:refer std.lib.zip/find :added "3.0"}
(fact "helper function for the rest of the `find` series")

^{:refer std.lib.zip/find-left :added "3.0" :class [:zip/move]}
(fact "steps status left to search predicate"
  ^:hidden

  (-> (from-status '[0 1 [2 3] [4 5] 6 |])
      (find-left odd?)
      (status))
  => '([0 | 1 [2 3] [4 5] 6])

  (-> (from-status '[0 1 [2 3] [4 5] 6 |])
      (find-left keyword?))
  => nil)

^{:refer std.lib.zip/find-right :added "3.0" :class [:zip/move]}
(fact "steps status right to search for predicate"
  ^:hidden

  (-> (from-status '[0 | 1 [2 3] [4 5] 6])
      (find-right even?)
      (status))
  => '([0 1 [2 3] [4 5] | 6]))

^{:refer std.lib.zip/find-next :added "3.0" :class [:zip/move]}
(fact "step status through the tree in depth first order to the first matching element"
  ^:hidden

  (-> (vector-zip [1 [2 [6 7] 3] [4 5]])
      (find-next #{7})
      (status))
  => '([1 [2 [6 | 7] 3] [4 5]])

  (-> (vector-zip [1 [2 [6 7] 3] [4 5]])
      (find-next #{3})
      (status))
  
  (-> (vector-zip [1 [2 [6 7] 3] [4 5]])
      (find-next keyword))
  => nil)

^{:refer std.lib.zip/find-prev :added "3.0" :class [:zip/move]}
(fact "step status through the tree in reverse order to the last matching element"
  ^:hidden

  (-> (from-status '[1 [2 [6 | 7] 3] [4 5]])
      (find-prev even?)
      (status))
  => '([1 [2 [| 6 7] 3] [4 5]]))

^{:refer std.lib.zip/from-status :added "3.0" :class [:zip/general]}
(fact "returns a zipper given a data structure with | as the status"
  ^:hidden

  (from-status '[1 2 3 | 4])
  => (contains {:left '(3 2 1),
                :right '(4)}))

^{:refer std.lib.zip/form-zip-wrap :added "4.0"}
(fact "wraps nils for the zipper")

^{:refer std.lib.zip/form-zip-unwrap :added "4.0"}
(fact "unwraps nils for the zipper")

^{:refer std.lib.zip/form-zip :added "4.0"}
(fact "creates a form zip"
  ^:hidden
  
  (form-zip :hello)
  
  (-> (form-zip '([x] 1))
      (step-next)
      (status))
  => '((| [x] 1))  
  
  (-> (form-zip '([x] 1))
      (step-next)
      (step-next)
      (step-next)
      (status))
  => '(([x] | 1))

  (-> (form-zip '([nil] 1))
      (step-next)
      (step-next)
      (step-next)
      (status))
  
  (-> (form-zip '([nil] 1))
      (step-next)
      (step-next)
      (step-next)
      (status))
  => '(([nil] | 1))
  
  (-> (form-zip '(1 nil 2 3))
      (step-next)
      (step-next)
      (step-next)
      (status))
  => '((1 nil | 2 3))
  
  (-> (form-zip '(defn hello [x]
                   (+ 1 2 3)))
      (find-next #{3})
      (status))
  => '((defn hello [x] (+ 1 2 | 3)))
  
    
  (-> (form-zip {:b 2 :a 1 })
      (step-inside)
      (insert-left [:c 4])
      (step-outside)
      (status))
  => '(| {:c 4, :b 2, :a 1})
  
  (-> (form-zip #{:b :c :a})
      (step-inside)
      (insert-left :e)
      (step-outside)
      (status))
  => '(| #{:e :c :b :a}))

^{:refer std.lib.zip/prewalk :added "3.0" :class [:zip/edit]}
(fact "emulates std.lib.walk/prewalk behavior with zipper"
  ^:hidden

  (-> (vector-zip [[1 2] [3 4]])
      (prewalk (fn [v] (if (vector? v)
                         (conj v 100)
                         (+ v 100))))
      (root-element))
  => [[101 102 200] [103 104 200] 200])

^{:refer std.lib.zip/postwalk :added "3.0" :class [:zip/edit]}
(fact "emulates std.lib.walk/postwalk behavior with zipper"
  ^:hidden

  (-> (vector-zip [[1 2] [3 4]])
      (postwalk (fn [v] (if (vector? v)
                          (conj v 100)
                          (+ v 100))))
      (root-element))
  => [[101 102 100] [103 104 100] 100])

^{:refer std.lib.zip/matchwalk :added "3.0" :class [:zip/edit]}
(fact "performs a match at each level"
  ^:hidden

  (-> (matchwalk (vector-zip [1 [2 [3 [4]]]])
                 [(fn [zip]
                    (= 2 (first (right-element zip))))
                  (fn [zip]
                    (= 4 (first (right-element zip))))]
                 delete-left)
      (root-element))
  => [1 [2 [[4]]]])

^{:refer std.lib.zip/levelwalk :added "3.0" :class [:zip/edit]}
(fact "performs a match at the same level"
  ^:hidden

  (-> (vector-zip [1 2 3 4])
      (step-inside)
      (levelwalk [(fn [zip]
                    (odd? (right-element zip)))]
                 delete-right)
      (root-element))
  => [2 4])
