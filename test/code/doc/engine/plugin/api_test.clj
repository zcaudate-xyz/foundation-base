(ns code.doc.engine.plugin.api-test
  (:use code.test)
  (:require [code.doc.engine.plugin.api :refer :all]))

^{:refer code.doc.engine.plugin.api/entry-tag :added "3.0"}
(fact "helper for formating vars"

  (entry-tag 'code.doc 'output-path)
  => "entry__code_doc__output_path")

^{:refer code.doc.engine.plugin.api/lower-first :added "3.0"}
(fact "converts the first letter to lowercase"

  (lower-first "Hello")
  => "hello")

^{:refer code.doc.engine.plugin.api/api-entry-example :added "3.0"}
(fact "helper function to convert a test entry into a html tree form"

  (api-entry-example {:test {:code "(+ 1 1)"
                             :path "src/code/core.clj"
                             :line {:row 10 :end-row 12}}}
                     {:url "http://github.com/code/core"})
  => (contains [:pre [:h6 [:i [:a {:href "http://github.com/code/core/blob/master/src/code/core.clj#L10-L12", :target "_blank"} "link"]]]]))

^{:refer code.doc.engine.plugin.api/api-entry-source :added "3.0"}
(fact "helper function to convert a source entry into a html tree form"

  (api-entry-source {:source {:code "(defn foo [] 1)"
                              :path "src/code/core.clj"
                              :line {:row 10 :end-row 12}}
                     :meta {:added "1.0"}}
                    {:url "http://github.com/code/core"}
                    'foo
                    'code.core)
  => (contains [:div {:class "entry-option"}
                [:h6 [:a {:href "http://github.com/code/core/blob/master/src/code/core.clj#L10-L12", :target "_blank"} "v&nbsp;" "1.0"]]]))

^{:refer code.doc.engine.plugin.api/api-entry :added "3.0"}
(fact "formats a `ns/var` pair tag into an html element"

  (api-entry ['foo {:intro "My Function"}]
             {:project {:url "http://github.com/code/core"}
              :namespace 'code.core})
  => (contains [:div {:class "entry"}
                [:span {:id "entry__code_core__foo"}]]))

^{:refer code.doc.engine.plugin.api/select-entries :added "3.0"}
(fact "selects api entries based on filters"

  (select-entries {:table {'foo {:ns 'code.core}
                           'bar {:ns 'code.core}
                           'baz {:ns 'code.other}}
                   :module #{"code.core"}})
  => ['bar 'foo])

^{:refer code.doc.engine.plugin.api/api-element :added "3.0"}
(fact "displays the entire `:api` namespace"

  (let [res (api-element {:title "API"
                          :table {'foo {:ns 'code.core :intro "Foo"}
                                  'bar {:ns 'code.core :intro "Bar"}}
                          :namespace 'code.core
                          :project {:url "http://github.com/code/core"}})]
    (take 4 res) => [:div {:class "api"}
                     [:span {:id "entry__code_core__"}]
                     [:div [:h2 "API"]]]))
