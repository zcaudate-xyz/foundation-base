(ns code.doc.link.tag-test
  (:use code.test)
  (:require [code.doc.link.tag :refer :all]))

^{:refer code.doc.link.tag/inc-candidate :added "3.0"}
(fact "creates additional candidates"

  (inc-candidate "hello")
  => "hello-0"

  (inc-candidate "hello-10")
  => "hello-11")

^{:refer code.doc.link.tag/tag-string :added "3.0"}
(fact "creates a tag based on string"

  (tag-string "hello2World/this.Rocks")
  => "hello2-world--this-rocks")

^{:refer code.doc.link.tag/create-candidate :added "3.0"}
(fact "creates a candidate based on element"

  (create-candidate {:origin :ns
                     :ns "code.doc"})
  => "ns-code-doc")

^{:refer code.doc.link.tag/create-tag :added "3.0"}
(fact "creates a tag based on element and existing tags"

  (create-tag {:title "hello"}
              (atom #{"hello" "hello-0" "hello-1" "hello-2"}))
  => {:title "hello", :tag "hello-3"})

^{:refer code.doc.link.tag/link-tags :added "3.0"}
(fact "link tags to all elements for a particular article"

  (-> (link-tags {:articles {"doc" {:elements [{:type :chapter :title "Intro"}]}}}
                 "doc")
      (get-in [:articles "doc" :elements]))
  => [{:type :chapter :title "Intro" :tag "intro"}])
