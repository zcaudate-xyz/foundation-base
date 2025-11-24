(ns code.doc.theme.stark-test
  (:use code.test)
  (:require [code.doc.theme.stark :refer :all]))

^{:refer code.doc.theme.stark/render-top-level :added "3.0"}
(fact "renders the top-level (cover page) for the stark theme"

  (let [lookup (with-meta (fn [k] {:name "doc" :ns 'code.doc})
                          {:code.doc {:pages {"doc" {:title "Doc"} "index" {}}}})]
    (render-top-level "doc" {} lookup))
  => "<a class=\"sidebar-nav-item active\" href=\"doc.html\">Doc</a><a class=\"sidebar-nav-item\" href=\"index.html\"></a>")

^{:refer code.doc.theme.stark/render-article :added "3.0"}
(fact "renders the individual page for the stark theme"

  (let [lookup (constantly {:name "doc"})]
    (render-article "doc" {:articles {"doc" {:elements [{:type :html :src "<div></div>"}]}}} lookup))
  => "<div></div>")

^{:refer code.doc.theme.stark/render-outline :added "3.0"}
(fact "renders the navigation outline for the stark theme"

  (let [lookup (constantly {:name "doc"})]
    (render-outline "doc" {:articles {"doc" {:elements [{:type :chapter :tag "1" :number 1 :title "Intro"}]}}} lookup))
  => "<li><a class=\"chapter\" data-scroll=\"\" href=\"#1\">\n    <h4>1 &nbsp; Intro</h4></a></li>")
