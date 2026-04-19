(ns code.doc.theme.foundation-test
  (:require [code.doc.theme.foundation :refer :all])
  (:use code.test))

^{:refer code.doc.theme.foundation/site-pages :added "4.1"}
(fact "returns non-index pages for the current site"
  (let [lookup (with-meta (fn [_] {:name "index" :ns 'foundation.code})
                 {:foundation.code {:pages {'index {:title "Home"}
                                            'guides {:title "Guides" :subtitle "Patterns"}}}})]
    (site-pages 'foundation.code/index lookup))
  => [['guides {:title "Guides" :subtitle "Patterns"}]])

^{:refer code.doc.theme.foundation/render-top-level :added "4.1"}
(fact "renders the sidebar links for the foundation theme"
  (let [lookup (with-meta (fn [_] {:name "guides" :ns 'foundation.code})
                 {:foundation.code {:pages {'index {:title "Home"}
                                            'guides {:title "Guides" :subtitle "Patterns"}}}})]
    (render-top-level 'foundation.code/guides {} lookup))
  => #"sidebar-link active")

^{:refer code.doc.theme.foundation/render-page-meta :added "4.1"}
(fact "renders metadata chips for the current page"
  (let [lookup (constantly {:name "guides" :title "Guides" :subtitle "Patterns"})]
    (render-page-meta 'foundation.code/guides {:project {:url "https://example.com/repo"
                                                         :version "1.0.0"}} lookup))
  => #"Repository")

^{:refer code.doc.theme.foundation/render-site-links :added "4.1"}
(fact "renders page links for the home page"
  (let [lookup (with-meta (fn [_] {:name "index" :ns 'foundation.code})
                 {:foundation.code {:pages {'index {:title "Home"}
                                            'guides {:title "Guides" :subtitle "Patterns"}}}})]
    (render-site-links 'foundation.code/index {} lookup))
  => #"site-link-card")

^{:refer code.doc.theme.foundation/render-article :added "4.1"}
(fact "renders the individual page for the foundation theme"
  (let [lookup (constantly {:name "guides"})]
    (render-article 'foundation.code/guides {:articles {"guides" {:elements [{:type :html :src "<div>ok</div>"}]}}} lookup))
  => "<div>ok</div>")

^{:refer code.doc.theme.foundation/render-outline :added "4.1"}
(fact "renders the page outline for the foundation theme"
  (let [lookup (constantly {:name "guides"})]
    (render-outline 'foundation.code/guides {:articles {"guides" {:elements [{:type :chapter :tag "intro" :number 1 :title "Intro"}]}}} lookup))
  => #"Intro")
