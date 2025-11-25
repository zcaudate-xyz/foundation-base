(ns code.query.match-test
  (:use code.test)
  (:require [code.query.match :refer :all]
            [code.edit :as nav]))

^{:refer code.query.match/matcher :added "3.0"}
(fact "creates a predicate function that can be used to match against values"

  ((matcher string?) "hello")
  => true)

^{:refer code.query.match/matcher? :added "3.0"}
(fact "checks if element is a matcher"

  (matcher? (matcher string?))
  => true)

^{:refer code.query.match/p-fn :added "3.0"}
(fact "takes a predicate function to check the state of the zipper"
  ((p-fn (fn [nav]
           (-> nav (nav/tag) (= :symbol))))
   (nav/parse-string "defn"))
  => true)

^{:refer code.query.match/p-not :added "3.0"}
(fact "checks if node is the inverse of the given matcher"

  ((p-not (p-is 'if)) (nav/parse-string "defn"))
  => true

  ((p-not (p-is 'if)) (nav/parse-string "if"))
  => false)

^{:refer code.query.match/p-is :added "3.0"}
(fact "checks if node is equivalent, does not meta into account"
  ((p-is 'defn) (nav/parse-string "defn"))
  => true

  ((p-is '^{:a 1} defn) (nav/parse-string "defn"))
  => true

  ((p-is 'defn) (nav/parse-string "is"))
  => false

  ((p-is '(defn & _)) (nav/parse-string "(defn x [])"))
  => false)

^{:refer code.query.match/p-equal-loop :added "3.0"}
(fact "helper function for `p-equal`"

  ((p-equal [1 2 3]) (nav/parse-string "[1 2 3]"))
  => true

  ((p-equal (list 'defn)) (nav/parse-string "(defn)"))
  => true

  ((p-equal '(defn)) (nav/parse-string "(defn)"))
  => true)

^{:refer code.query.match/p-equal :added "3.0"}
(fact "checks if the node is equivalent, takes meta into account"
  ((p-equal '^{:a 1} defn) (nav/parse-string "defn"))
  => false

  ((p-equal '^{:a 1} defn) (nav/parse-string "^{:a 1} defn"))
  => true

  ((p-equal '^{:a 1} defn) (nav/parse-string "^{:a 2} defn"))
  => false)

^{:refer code.query.match/p-meta :added "3.0"}
(fact "checks if meta is the same"
  ((p-meta {:a 1}) (nav/down (nav/parse-string "^{:a 1} defn")))
  => true

  ((p-meta {:a 1}) (nav/down (nav/parse-string "^{:a 2} defn")))
  => false)

^{:refer code.query.match/p-type :added "3.0"}
(fact "check on the type of element"
  ((p-type :symbol) (nav/parse-string "defn"))
  => true

  ((p-type :symbol) (-> (nav/parse-string "^{:a 1} defn") nav/down nav/right))
  => true)

^{:refer code.query.match/p-form :added "3.0"}
(fact "checks if it is a form with the symbol as the first element"
  ((p-form 'defn) (nav/parse-string "(defn x [])"))
  => true
  ((p-form 'let) (nav/parse-string "(let [])"))
  => true)

^{:refer code.query.match/p-pattern :added "3.0"}
(fact "checks if the form matches a particular pattern"
  ((p-pattern '(defn ^:% symbol? & _)) (nav/parse-string "(defn ^{:a 1} x [])"))
  => true

  ((p-pattern '(defn ^:% symbol? ^{:% true :? true} string? []))
   (nav/parse-string "(defn ^{:a 1} x [])"))
  => true)

^{:refer code.query.match/p-code :added "3.0"}
(fact "checks if the form matches a string in the form of a regex expression"
  ((p-code #"defn") (nav/parse-string "(defn ^{:a 1} x [])"))
  => true)

^{:refer code.query.match/p-and :added "3.0"}
(fact "takes multiple predicates and ensures that all are correct"
  ((p-and (p-code #"defn")
          (p-type :token)) (nav/parse-string "(defn ^{:a 1} x [])"))
  => false

  ((p-and (p-code #"defn")
          (p-type :list)) (nav/parse-string "(defn ^{:a 1} x [])"))
  => true)

^{:refer code.query.match/p-or :added "3.0"}
(fact "takes multiple predicates and ensures that at least one is correct"
  ((p-or (p-code #"defn")
         (p-type :token)) (nav/parse-string "(defn ^{:a 1} x [])"))
  => true

  ((p-or (p-code #"defn")
         (p-type :list)) (nav/parse-string "(defn ^{:a 1} x [])"))
  => true)

^{:refer code.query.match/compile-matcher :added "3.0"}
(fact "creates a matcher given a datastructure declaring the actual template"

  ((compile-matcher {:is 'hello}) (nav/parse-string "hello"))
  => true)

^{:refer code.query.match/p-parent :added "3.0"}
(fact "checks that the parent of the element contains a certain characteristic"
  ((p-parent 'defn) (-> (nav/parse-string "(defn x [])") nav/next nav/next))
  => true

  ((p-parent {:parent 'if}) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => true

  ((p-parent {:parent 'if}) (-> (nav/parse-string "(if (= x y))") nav/down))
  => false)

^{:refer code.query.match/p-child :added "3.0"}
(fact "checks that there is a child of a container that has a certain characteristic"
  ((p-child {:form '=}) (nav/parse-string "(if (= x y))"))
  => true

  ((p-child '=) (nav/parse-string "(if (= x y))"))
  => false)

^{:refer code.query.match/p-first :added "3.0"}
(fact "checks that the first element of the container has a certain characteristic"
  ((p-first 'defn) (-> (nav/parse-string "(defn x [])")))
  => true

  ((p-first 'x) (-> (nav/parse-string "[x y z]")))
  => true

  ((p-first 'x) (-> (nav/parse-string "[y z]")))
  => false)

^{:refer code.query.match/p-last :added "3.0"}
(fact "checks that the last element of the container has a certain characteristic"
  ((p-last 1) (-> (nav/parse-string "(defn [] 1)")))
  => true

  ((p-last 'z) (-> (nav/parse-string "[x y z]")))
  => true

  ((p-last 'x) (-> (nav/parse-string "[y z]")))
  => false)

^{:refer code.query.match/p-nth :added "3.0"}
(fact "checks that the element at a specific Nth index within the container has a certain characteristic"
  ((p-nth [0 'defn]) (-> (nav/parse-string "(defn [] 1)")))
  => true

  ((p-nth [2 'z]) (-> (nav/parse-string "[x y z]")))
  => true

  ((p-nth [2 'x]) (-> (nav/parse-string "[y z]")))
  => false)

^{:refer code.query.match/p-nth-left :added "3.0"}
(fact "checks that the element at a specific Nth index to the left of the current position has a certain characteristic"
  ((p-nth-left [0 'defn]) (-> (nav/parse-string "(defn [] 1)") nav/down))
  => true

  ((p-nth-left [1 ^:& vector?]) (-> (nav/parse-string "(defn [] 1)") nav/down nav/right-most))
  => true)

^{:refer code.query.match/p-nth-right :added "3.0"}
(fact "checks that the element at a specific Nth index to the right of the current position has a certain characteristic"
  ((p-nth-right [0 'defn]) (-> (nav/parse-string "(defn [] 1)") nav/down))
  => true

  ((p-nth-right [1 vector?]) (-> (nav/parse-string "(defn [] 1)") nav/down))
  => true)

^{:refer code.query.match/p-nth-ancestor :added "3.0"}
(fact "search for match n-levels up"

  ((p-nth-ancestor [2 {:contains 3}])
   (-> (nav/parse-string "(* (- (+ 1 2) 3) 4)")
       nav/down nav/right nav/down nav/right nav/down))
  => true)

^{:refer code.query.match/tree-search :added "3.0"}
(fact "recursively searches a tree structure for elements matching a predicate, used as a helper for `p-contains`"
  (let [m-fn (fn [nav] (= 'x (nav/value nav)))]
    (tree-search (nav/parse-string "((a b) (c (d x)))") m-fn nav/down nav/right))
  => true)

^{:refer code.query.match/p-contains :added "3.0"}
(fact "checks that any element (deeply nested also) of the container matches"
  ((p-contains '=) (nav/parse-string "(if (= x y))"))
  => true

  ((p-contains 'x) (nav/parse-string "(if (= x y))"))
  => true)

^{:refer code.query.match/tree-depth-search :added "3.0"}
(fact "performs a depth-first search for a match N levels down in a tree structure, used as a helper for `p-nth-contains`"
  (let [m-fn (fn [nav] (= 'x (nav/value nav)))]
    (tree-depth-search (nav/parse-string "((a b) (c (d x)))") m-fn 3 nav/down nav/right))
  => true)

^{:refer code.query.match/p-nth-contains :added "3.0"}
(fact "search for match n-levels down"

  ((p-nth-contains [2 {:contains 1}])
   (nav/parse-string "(* (- (+ 1 2) 3) 4)"))
  => true)

^{:refer code.query.match/p-ancestor :added "3.0"}
(fact "checks that any parent container matches"
  ((p-ancestor {:form 'if}) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => true
  ((p-ancestor 'if) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => true)

^{:refer code.query.match/p-sibling :added "3.0"}
(fact "checks that any element on the same level has a certain characteristic"
  ((p-sibling '=) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => false

  ((p-sibling 'x) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => true)

^{:refer code.query.match/p-left :added "3.0"}
(fact "checks that the element on the left has a certain characteristic"
  ((p-left '=) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next nav/next))
  => true

  ((p-left 'if) (-> (nav/parse-string "(if (= x y))") nav/down nav/next))
  => true)

^{:refer code.query.match/p-right :added "3.0"}
(fact "checks that the element on the right has a certain characteristic"
  ((p-right 'x) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
  => true

  ((p-right {:form '=}) (-> (nav/parse-string "(if (= x y))") nav/down))
  => true)

^{:refer code.query.match/p-left-of :added "3.0"}
(fact "checks that any element on the left has a certain characteristic"
  ((p-left-of '=) (-> (nav/parse-string "(= x y)") nav/down nav/next))
  => true

  ((p-left-of '=) (-> (nav/parse-string "(= x y)") nav/down nav/next nav/next))
  => true)

^{:refer code.query.match/p-right-of :added "3.0"}
(fact "checks that any element on the right has a certain characteristic"
  ((p-right-of 'x) (-> (nav/parse-string "(= x y)") nav/down))
  => true

  ((p-right-of 'y) (-> (nav/parse-string "(= x y)") nav/down))
  => true

  ((p-right-of 'z) (-> (nav/parse-string "(= x y)") nav/down))
  => false)

^{:refer code.query.match/p-left-most :added "3.0"}
(fact "checks if the navigator is at the left-most expression within its current level"
  ((p-left-most true) (-> (nav/parse-string "(= x y)") nav/down))
  => true

  ((p-left-most true) (-> (nav/parse-string "(= x y)") nav/down nav/next))
  => false)

^{:refer code.query.match/p-right-most :added "3.0"}
(fact "checks if the navigator is at the right-most expression within its current level"
  ((p-right-most true) (-> (nav/parse-string "(= x y)") nav/down nav/next))
  => false

  ((p-right-most true) (-> (nav/parse-string "(= x y)") nav/down nav/next nav/next))
  => true)
