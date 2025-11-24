(ns code.doc.theme.mormont-test
  (:use code.test)
  (:require [code.doc.theme.mormont :refer :all]))

^{:refer code.doc.theme.mormont/render-top-level :added "3.0"}
(fact "renders the top-level (cover page) for the mormont theme"

  (render-top-level {:project {:publish {:files {"doc" {:title "Doc"} "index" {}}}}} "index")
  => "<ul>\n  <li><a href=\"index.html\">home</a></li>\n  <li><a href=\"doc.html\">Doc</a></li>\n</ul>")

^{:refer code.doc.theme.mormont/render-article :added "3.0"}
(fact "renders the individual page for the mormont theme"

  (render-article {:articles {"doc" {:elements [{:type :html :src "<div></div>"}]}}} "doc")
  => "<div></div>")

^{:refer code.doc.theme.mormont/render-navigation :added "3.0"}
(fact "renders the navigation outline for the mormont theme"

  (render-navigation {:articles {"doc" {:elements [{:type :chapter :tag "1" :number 1 :title "Intro"}]}}} "doc")
  => "<h4><a href=\"#1\">1 &nbsp; Intro</a></h4>")
