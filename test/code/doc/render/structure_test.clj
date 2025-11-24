(ns code.doc.render.structure-test
  (:use code.test)
  (:require [code.doc.render.structure :refer :all]))

^{:refer code.doc.render.structure/inclusive :added "3.0"}
(fact "checks is a section is within another"

  (inclusive :article :chapter)
  => true

  (inclusive :chapter :section)
  => true

  (inclusive :section :chapter)
  => false)

^{:refer code.doc.render.structure/separate :added "3.0"}
(fact "separates elements into various structures"

  (separate even? [1 2 3 4 5])
  => [[1] [2 3] [4 5]])

^{:refer code.doc.render.structure/containify :added "3.0"}
(fact "puts a flat element structure into containers"

  (containify [{:type :chapter :title "C1"}
               {:type :paragraph :text "p1"}
               {:type :section :title "S1"}
               {:type :paragraph :text "p2"}])
  => [{:type :article}
      [{:type :chapter :title "C1"}
       [{:type :paragraph :text "p1"}]
       [{:type :section :title "S1"}
        [{:type :paragraph :text "p2"}]]]])

^{:refer code.doc.render.structure/mapify-unit :added "3.0"}
(fact "helper class for mapify")

^{:refer code.doc.render.structure/mapify :added "3.0"}
(fact "creates the hierarchical structure for a flat list of elements"

  (mapify [{:type :chapter :title "C1"}
           [{:type :paragraph :text "p1"}]])
  => {:type :chapter, :title "C1", :elements [{:type :paragraph, :text "p1"}]})

^{:refer code.doc.render.structure/structure :added "3.0"}
(fact "creates a structure for the article and its elements"

  (structure [{:type :chapter :title "C1"}
              {:type :paragraph :text "p1"}])
  => {:type :article,
      :elements [{:type :chapter,
                  :title "C1",
                  :elements [{:type :paragraph, :text "p1"}]}]})
