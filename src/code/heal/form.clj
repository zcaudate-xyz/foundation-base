(ns code.heal.form
  (:require [code.manage :as manage]
            [code.edit :as edit]
            [code.query :as query]
            [code.heal.level :as level]
            [code.heal.form-edits :as form-edits]
            [std.block :as b]))


(comment
  (code.project/project)
  {:root "../Szncampaigncenter/src-translated/"}
  

  (manage/transform-code
   :all
   {:transform level/heal-content
    :write true
    :print {:function true}
    :verify {:read b/parse-root}
    :no-analysis true}
   {:root "../Szncampaigncenter/"
    :source-paths ["src-translated"]})
  
  
  (b/value
   (s/layout
    ["\"hello\""]))

  (s/layout "\"hello\"")
  
  (b/string
   (s/layout "\"hello\""))
  
  (s/layout
   [["\"hello\""]])
  
  (manage/locate-code
   :all
   {:query [(fn [form]
              (and (symbol? form)
                   (string? (namespace form))
                   (.contains (name form) ".")))]}
   {:root "../Szncampaigncenter/"
    :source-paths ["src-translated"]})
  
  (b/value
   (b/parse-first "\"\\\"hello\\\"\""))
  (= (b/value
      (b/parse-first "\"\\\"hello\\\"\""))
     
     (b/value
      (s/layout "\"hello\"")))
  

  (edit/right-expression
   (edit/parse-first "\"\\\"hello\\\"\""))
  
  (query/modify
   (edit/parse-first "\"\\\"hello\\\"\""))
  
  (manage/refactor-code
   :all
   {:edits [form-edits/fix:namespaced-symbol-no-dot]}
   {:root "../Szncampaigncenter/"
    :source-paths ["src-translated"]})
  

  
  (code.project/all-files
   ["src"]
   {}
   {:root "."})
  
  (defn )

  (code.project/in-context)
  (code.heal/heal-code :all
                       {}
                       (code.project/all-files
                        ["src-translated"]
                        {}
                        {:root "../Szncampaigncenter/"})
                       {:root "../Szncampaigncenter/"})
  
  (manage/locate-code :all)
  
  (code.project/file-lookup
   (code.project/project))
  (code.manage/refactor-test
   :all
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (code.query/modify
               nav
               [(fn [form]
                  )]
               (fn [nav]
                 )))]}
   (code.project/all-files
    ["src-translated"]
    {}
    {:root "../Szncampaigncenter/"})
   {:root "../Szncampaigncenter/src-translated/"})

  -/mockMarket.options)
