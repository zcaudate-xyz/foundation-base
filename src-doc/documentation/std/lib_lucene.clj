(ns documentation.lib-lucene
  (:use code.test))

[[:hero {:title "lib.lucene"
         :subtitle "component-managed Lucene indexing and search"
         :lead "Create memory or disk-backed search engines, define field templates, and query indexed documents through a shared protocol."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"`LuceneSearch` implements the search-engine and component protocols. Configuration selects a store and describes document fields and analyzers."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Create an engine"}]]

(comment
  (require '[lib.lucene :as lucene])

  (def engine
    (lucene/lucene
     {:store :memory
      :template
      {:article
       {:analyzer {:type :standard}
        :type {:id {:stored true}
               :title {:stored true}
               :body {:stored true}}}}})))

[[:section {:title "Use the engine protocol"}]]

"The engine protocol provides document addition, replacement, removal, and search. Stop the component when the engine is no longer required so readers, writers, analyzers, and storage are released."

(comment
  (require '[lib.lucene.protocol :as search])
  (require '[std.lib.component :as component])

  (search/index-add engine :article
                    {:id "1" :title "Foundation"}
                    {})
  (search/search engine :article {:title "Foundation"} {})
  (component/stop engine))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "lib.lucene"}]]
[[:api {:namespace "lib.lucene.protocol"}]]
[[:api {:namespace "lib.lucene.impl"}]]
