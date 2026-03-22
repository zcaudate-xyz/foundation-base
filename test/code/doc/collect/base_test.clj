(ns code.doc.collect.base-test
  (:require [code.doc.collect.base :refer :all])
  (:use code.test))

^{:refer code.doc.collect.base/collect-namespaces :added "3.0"}
(fact "combines `:ns-form` directives into a namespace map for easy referral"

  (collect-namespaces
   {:articles
    {"example"
     {:elements [{:type :ns-form
                  :ns    'clojure.core}]}}}
   "example")
  => '{:articles {"example" {:elements () :meta {}}}
       :namespaces {clojure.core {:type :ns-form :ns clojure.core}}})

^{:refer code.doc.collect.base/collect-article-metas :added "3.0"}
(fact "shunts `:article` directives into a separate `:meta` section"

  (collect-article-metas
   {:articles {"example" {:elements [{:type :article
                                      :options {:color :light}}]}}}
   "example")
  => '{:articles {"example" {:elements []
                             :meta {:options {:color :light}}}}})

^{:refer code.doc.collect.base/collect-global-metas :added "3.0"}
(fact "shunts `:global` directives into a globally available `:meta` section"

  (collect-global-metas
   {:articles {"example" {:elements [{:type :global
                                      :options {:color :light}}]}}}
   "example")
  => {:articles {"example" {:elements ()}}
      :global {:options {:color :light}}})

^{:refer code.doc.collect.base/collect-tags :added "3.0"}
(fact "puts any element with `:tag` attribute into a separate `:tag` set"

  (collect-tags
   {:articles {"example" {:elements [{:type :chapter :tag  "hello"}
                                     {:type :chapter :tag  "world"}]}}}
   "example")
  => {:articles {"example" {:elements [{:type :chapter :tag "hello"}
                                       {:type :chapter :tag "world"}]
                            :tags #{"hello" "world"}}}})

^{:refer code.doc.collect.base/collect-citations :added "3.0"}
(fact "shunts `:citation` directives into a separate `:citation` section"

  (collect-citations
   {:articles {"example" {:elements [{:type :citation :author "Chris"}]}}}
   "example")
  => {:articles {"example" {:elements [],
                            :citations [{:type :citation, :author "Chris"}]}}})

(comment
  (code.manage/import))