(ns code.query.traverse-test
  (:use code.test)
  (:require [code.query.block :as nav]
            [code.query.traverse :refer :all]))

#_(defn source [pos]
    (-> pos :source nav/value))

^{:refer code.query.traverse/pattern-zip :added "3.0"}
(fact "creates a clojure.zip pattern")

^{:refer code.query.traverse/wrap-meta :added "3.0"}
(fact "provides a wrapper for traversing and manipulating metadata tags within code structures"))

^{:refer code.query.traverse/wrap-delete-next :added "3.0"}
(fact "provides a wrapper function to delete the element immediately following the current position in a zipper"))

^{:refer code.query.traverse/traverse-delete-form :added "3.0"}
(fact "traversing deletion form")

^{:refer code.query.traverse/traverse-delete-node :added "3.0"}
(fact "traversing deletion node")

^{:refer code.query.traverse/traverse-delete-level :added "3.0"}
(fact "traversing deletion level")

^{:refer code.query.traverse/prep-insert-pattern :added "3.0"}
(fact "prepares a pattern for insertion operations during code traversal"))

^{:refer code.query.traverse/wrap-insert-next :added "3.0"}
(fact "provides a wrapper function to insert an element immediately following the current position in a zipper"))

^{:refer code.query.traverse/traverse-insert-form :added "3.0"}
(fact "traversing insertion form")

^{:refer code.query.traverse/traverse-insert-node :added "3.0"}
(fact "traversing insertion node")

^{:refer code.query.traverse/traverse-insert-level :added "3.0"}
(fact "traversing insertion level")

^{:refer code.query.traverse/wrap-cursor-next :added "3.0"}
(fact "provides a wrapper function to locate the cursor at the next element during code traversal"))

^{:refer code.query.traverse/traverse-cursor-form :added "3.0"}
(fact "traversing cursor form")

^{:refer code.query.traverse/traverse-cursor-level :added "3.0"}
(fact "traversing cursor level")

^{:refer code.query.traverse/count-elements :added "3.0"}
(fact "counts the number of elements in a given code structure"))

^{:refer code.query.traverse/traverse :added "3.0"}
(fact "basic traverse functions"
  (source
   (traverse (nav/parse-string "^:a (+ () 2 3)")
             '(+ () 2 3)))
  => '(+ () 2 3)

  (source
   (traverse (nav/parse-string "^:a (hello)")
             '(hello)))
  => '(hello)
  ^:hidden
  (source
   (traverse (nav/parse-string "^:a (hello)")
             '(^:- hello)))
  => ()

  (source
   (traverse (nav/parse-string "(hello)")
             '(^:- hello)))
  => ()

  (source
   (traverse (nav/parse-string "((hello))")
             '((^:- hello))))
  => '(())

  ;; Insertions
  (source
   (traverse (nav/parse-string "()")
             '(^:+ hello)))
  => '(hello)

  (source
   (traverse (nav/parse-string "(())")
             '((^:+ hello))))
  => '((hello)))

^{:refer code.query.traverse/source :added "3.0"}
(fact "retrives the source of a traverse"

  (source
   (traverse (nav/parse-string "()")
             '(^:+ hello)))
  => '(hello))

(fact "more advanced traverse functions"
  (source
   (traverse (nav/parse-string "(defn hello)")
             '(defn ^{:? true :% true} symbol? ^:+ [])))
  => '(defn hello [])

  (source
   (traverse (nav/parse-string "(defn hello)")
             '(defn ^{:? true :% true :- true} symbol? ^:+ [])))
  => '(defn [])

  (source
   (traverse (nav/parse-string "(defn hello)")
             '(defn ^{:? true :% true :- true} symbol? | ^:+ [])))
  => []

  (source
   (traverse (nav/parse-string "(defn hello \"world\" {:a 1} [])")
             '(defn ^:% symbol?
                ^{:? true :% true :- true} string?
                ^{:? true :% true :- true} map?
                ^:% vector? & _)))
  => '(defn hello [])

  (source
   (traverse (nav/parse-string "(defn hello [] (+ 1 1))")
             '(defn _ _ (+ | 1 & _))))
  => 1

  (source
   (traverse (nav/parse-string "(defn hello [] (+ 1 1))")
             '(#{defn} | & _)))
  => 'hello

  (source
   (traverse (nav/parse-string "(fact \"hello world\")")
             '(fact | & _)))
  => "hello world")
