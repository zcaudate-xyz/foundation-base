(ns code.doc.engine.winterfell-test
  (:use code.test)
  (:require [code.doc.engine.winterfell :refer :all]))

^{:refer code.doc.engine.winterfell/page-element
  :added "3.0"}
(fact "seed function for rendering a page element"

  (page-element {:type :html :src "<div></div>"})
  => "<div></div>"

  (page-element {:type :chapter :tag "chk" :number 1 :title "Introduction"})
  => [:div [:span {:id "chk"}] [:h2 [:b "1 &nbsp;&nbsp; Introduction"]]])

^{:refer code.doc.engine.winterfell/render-chapter :added "3.0"}
(fact "seed function for rendering a chapter element"

  (render-chapter {:tag "chk" :number 1 :title "Introduction"
                   :elements [{:tag "sec1" :number "1.1" :title "Section 1"}]})
  => [:li
      [:a {:class "chapter", :data-scroll "", :href "#chk"} [:h4 "1 &nbsp; Introduction"]]
      [:a {:class "section", :data-scroll "", :href "#sec1"} [:h5 [:i "1.1 &nbsp; Section 1"]]]])

^{:refer code.doc.engine.winterfell/nav-element :added "3.0"}
(fact "seed function for rendering a navigation element"

  (nav-element {:type :chapter :tag "chk" :number 1 :title "Introduction"})
  => [:h4 [:a {:href "#chk"} "1 &nbsp; Introduction"]])
