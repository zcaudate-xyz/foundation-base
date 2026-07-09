(ns documentation.foundation-code-architecture
  (:require [code.doc.collect.base :as collect.base]
            [code.doc.executive :as executive]
            [code.doc.link.anchor :as link.anchor]
            [code.doc.link.number :as link.number]
            [code.doc.link.tag :as link.tag]
            [code.doc.parse :as parse]
            [std.block.navigate :as nav])
  (:use code.test))

[[:chapter {:title "Architecture overview"}]]

"The revived site keeps the existing compiler shape: parse files into elements, collect and link metadata, then let a theme render the final site."

[[:card-grid {:items [{:meta "Parse"
                       :title "`code.doc.parse`"
                       :text "Reads Clojure and markdown sources into a shared document model, now with frontmatter, heading ids, fenced code blocks, and embedded directives."}
                      {:meta "Collect"
                       :title "`code.doc.collect.*`"
                       :text "Pulls referenced namespaces, API metadata, and citations into an interim bundle."}
                      {:meta "Link"
                       :title "`code.doc.link.*`"
                       :text "Assigns numbering, tags, namespace references, cross-links, and generated API tables."}
                      {:meta "Render"
                       :title "`code.doc.executive` + theme"
                       :text "Merges theme render functions, page metadata, and templates to emit static HTML pages."}]}]]

[[:chapter {:title "Extension points"}]]

[[:callout {:tone :info
            :title "Where to extend"
            :content "Prefer extending **directives**, **themes**, and **API rendering** before touching the core parse/collect/link pipeline. Those are the highest-leverage places to improve the site without destabilizing it."}]]

[[:demo {:title "A theme contract in practice"
          :lang "clojure"
          :code "{:render {:article \"render-article\"\n          :outline \"render-outline\"\n          :top-level \"render-top-level\"\n          :page-meta \"render-page-meta\"\n          :site-links \"render-site-links\"}}"}]]

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Parsing source forms into elements"}]]

"`code.doc.parse` turns Clojure and markdown sources into a sequence of elements. Each element is a map with a `:type` key and the data needed for later stages."

(fact "parse a namespace form and a directive"
  (-> (nav/parse-string "(ns example.core)")
      (parse/parse-ns-form))
  => {:type :ns-form
      :indentation 0
      :meta {}
      :ns 'example.core
      :code "(ns example.core)"}

  (-> (nav/parse-string "[[:chapter {:title \"hello world\"}]]")
      (parse/parse-directive))
  => {:type :chapter :title "hello world"})

(fact "parse paragraphs and fact blocks"
  (-> (nav/parse-string "\"this is a paragraph\"")
      (parse/parse-paragraph))
  => {:type :paragraph :text "this is a paragraph"}

  (-> (nav/parse-string "(fact \"hello world\" (+ 1 1) \n => 2)")
      (parse/parse-fact-form))
  => {:type :test :indentation 2 :caption "hello world" :code "(+ 1 1) \n => 2"})

[[:section {:title "Parsing markdown content"}]]

"Markdown files are first-class sources. `parse-frontmatter` extracts EDN frontmatter, `parse-header` recognises heading ids, and `parse-markdown` weaves together directives, fenced code blocks, and paragraphs."

(fact "extract frontmatter and heading ids"
  (parse/parse-frontmatter ["---" "{:title \"Hello\"}" "---" "# Heading"])
  => [{:type :article}
      {:type :article :title "Hello"}
      ["# Heading"]]

  (parse/parse-header "## Section {#section-id}")
  => {:type :section :title "Section" :tag "section-id"})

(fact "parse embedded directives and fenced code blocks"
  (parse/parse-markdown-directive "[[:callout {:tone :info :title \"Hello\" :content \"World\"}]]")
  => {:type :callout :tone :info :title "Hello" :content "World"}

  (parse/parse-markdown (str "---\n{:title \"Hello\"}\n---\n"
                             "# Heading {#heading}\n"
                             "[[:callout {:tone :success :title \"Nice\" :content \"Yep\"}]]\n"
                             "```clojure\n(+ 1 1)\n```"))
  => [{:type :article :title "Hello"}
      {:type :chapter :title "Heading" :tag "heading"}
      {:type :callout :tone :success :title "Nice" :content "Yep"}
      {:type :block :indentation 0 :lang "clojure" :code "(+ 1 1)"}])

[[:section {:title "Collecting article metadata"}]]

"The collect layer moves special directives out of the element stream and into lookup tables. This makes namespaces, tags, citations, and article/global options available to later link and render stages."

(fact "collect namespaces and article metadata"
  (collect.base/collect-namespaces
   {:articles {"example"
               {:elements [{:type :ns-form
                            :ns    'clojure.core}]}}}
   "example")
  => '{:articles {"example" {:elements () :meta {}}}
       :namespaces {clojure.core {:type :ns-form :ns clojure.core}}}

  (collect.base/collect-article-metas
   {:articles {"example" {:elements [{:type :article
                                      :options {:color :light}}]}}}
   "example")
  => '{:articles {"example" {:elements []
                             :meta {:options {:color :light}}}}})

(fact "collect global options and tags"
  (collect.base/collect-global-metas
   {:articles {"example" {:elements [{:type :global
                                      :options {:color :light}}]}}}
   "example")
  => {:articles {"example" {:elements ()}}
      :global {:options {:color :light}}}

  (get-in (collect.base/collect-tags
           {:articles {"example"
                       {:elements [{:type :chapter :tag "hello"}
                                   {:type :chapter :tag "world"}]}}}
           "example")
          [:articles "example" :tags])
  => #{"hello" "world"})

[[:section {:title "Linking structure"}]]

"The link layer turns raw elements into a connected document. Titles become url-friendly tags, headings get numbers, and anchors are indexed for cross-page navigation."

(fact "normalise titles into url-friendly tags"
  (link.tag/tag-string "hello2World/this.Rocks")
  => "hello2-world--this-rocks"

  (link.tag/create-tag {:title "hello"} (atom #{"hello" "hello-0" "hello-1"}))
  => {:title "hello" :tag "hello-2"})

(fact "number chapters and sections"
  (link.number/increment 1)
  => "A"

  (link.number/increment "1")
  => "2"

  (->> (link.number/link-numbers-loop
        [{:type :chapter}
         {:type :section}
         {:type :code :numbered true :title "Code"}]
        #{:code})
       (mapv :number))
  => ["1" "1.1" "1.1"])

(fact "build anchor lookups by tag"
  (-> (link.anchor/link-anchors
       {:anchors-lu {"example" {:by-tag {:a 1 :b 2}}}}
       "example")
      :anchors)
  => {"example" {:a 1 :b 2}})

[[:section {:title "Loading a theme"}]]

"Finally, `code.doc.executive` loads the theme map that the renderer consumes. A theme exposes render functions for each page region."

(fact "load the bolton theme"
  (let [theme (executive/load-theme "bolton")]
    (keys theme))
  => [:engine :resource :copy :render :manifest]

  (let [render-fn (-> (executive/load-theme "bolton")
                      (get-in [:render :article]))]
    [(first render-fn) (var? (second render-fn))])
  => [:fn true])
