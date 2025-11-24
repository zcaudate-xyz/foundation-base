(ns code.doc.link.stencil-test
  (:use code.test)
  (:require [code.doc.link.stencil :refer :all]))

^{:refer code.doc.link.stencil/transform-stencil :added "3.0"}
(fact "creates a link to the given tags"

  (transform-stencil "See {{foo/bar}}" "foo" {"foo" {"bar" {"number" "1"}}})
  => "See 1"

  (transform-stencil "See {{bar}}" "foo" {"foo" {"bar" {"number" "1"}}})
  => "See 1")

^{:refer code.doc.link.stencil/link-stencil :added "3.0"}
(fact "creates links to all the other documents in the project"

  (-> (link-stencil {:anchors {"foo" {"bar" {"number" "1"}}}
                     :articles {"doc" {:meta {}
                                       :elements [{:type :paragraph :text "See {{foo/bar}}"}]}}}
                    "doc")
      (get-in [:articles "doc" :elements]))
  => [{:type :paragraph, :text "See 1"}])
