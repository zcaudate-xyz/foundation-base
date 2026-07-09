(ns documentation.code-reference
  (:require [code.doc.parse :as parse]
            [code.doc.executive :as exec]
            [code.doc.engine.winterfell :as winterfell]
            [std.block.navigate :as nav])
  (:use code.test))

[[:chapter {:title "Reference"}]]

"These are the namespaces that matter most when extending `code.doc` for code."

[[:chapter {:title "Parsing" :link "code.doc.parse"}]]
[[:api {:namespace "code.doc.parse"
        :only [parse-file parse-markdown parse-header parse-frontmatter]}]]

[[:chapter {:title "Publishing" :link "code.doc.executive"}]]
[[:api {:namespace "code.doc.executive"
        :only [all-pages load-theme render init-template deploy-template]}]]

[[:chapter {:title "Rendering" :link "code.doc.engine.winterfell"}]]
[[:api {:namespace "code.doc.engine.winterfell"
        :only [page-element render-chapter nav-element]}]]

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

"This walkthrough follows a single page through the `code.doc` pipeline: parsing a Clojure or Markdown source file, looking up publishing resources, and rendering the resulting elements as Hiccup HTML."

[[:section {:title "Parsing directives and facts"}]]

"`code.doc.parse` turns raw source into a sequence of elements. Directives such as `[[:chapter ...]]` become element maps, and `fact` forms become test blocks that can be executed during publication."

(fact "parse a chapter directive"
  (-> (nav/parse-string "[[:chapter {:title \"Hello\"}]]")
      (parse/parse-directive))
  => {:type :chapter :title "Hello"})

(fact "parse a fact form into a test element"
  (-> (nav/parse-string "(fact \"basic addition\" (+ 1 1) \n => 2)")
      (parse/parse-fact-form))
  => {:type :test :indentation 2 :caption "basic addition" :code "(+ 1 1) \n => 2"})

(fact "parse a comment block into a code block element"
  (-> (nav/parse-string "(comment (+ 1 1) \n => 2)")
      (parse/parse-comment-form))
  => {:type :block :indentation 2 :code "(+ 1 1) \n => 2"})

[[:section {:title "Parsing Markdown"}]]

"Markdown files are first-class documentation sources. The parser extracts front matter, headings, embedded directives, and fenced code blocks."

(fact "parse a markdown heading with an id"
  (parse/parse-header "## Section {#section-id}")
  => {:type :section :title "Section" :tag "section-id"})

(fact "parse a complete markdown snippet"
  (parse/parse-markdown (str "---\n{:title \"Hello\"}\n---\n"
                             "# Heading {#heading}\n"
                             "[[:callout {:tone :success :title \"Nice\" :content \"Yep\"}]]\n"
                             "```clojure\n(+ 1 1)\n```"))
  => [{:type :article :title "Hello"}
      {:type :chapter :title "Heading" :tag "heading"}
      {:type :callout :tone :success :title "Nice" :content "Yep"}
      {:type :block :indentation 0 :lang "clojure" :code "(+ 1 1)"}])

[[:section {:title "Loading themes and vars"}]]

"Before rendering, the executive resolves external resources. `load-var` pulls in a function from any namespace, and `load-theme` loads a theme definition so its render functions can be used during page generation."

(fact "load a var by namespace and name"
  (exec/load-var "clojure.core" "apply")
  => fn?)

(fact "load the bolton theme"
  (exec/load-theme "bolton")
  => (contains {:engine "winterfell"
                :resource string?
                :render map?}))

[[:section {:title "Rendering elements"}]]

"`code.doc.engine.winterfell` converts parsed elements into Hiccup data structures. Chapter and section tags become anchors so the table of contents can link directly to them."

(fact "render a chapter heading"
  (winterfell/page-element {:type :chapter :tag "intro" :number 1 :title "Introduction"})
  => [:div [:span {:id "intro"}] [:h2 [:b "1 &nbsp;&nbsp; Introduction"]]])

(fact "render a navigation section"
  (winterfell/nav-element {:type :section :tag "sec1" :number "1.1" :title "First Steps"})
  => [:h5 "&nbsp;&nbsp;"
      [:i [:a {:href "#sec1"} "1.1 &nbsp; First Steps"]]])

(fact "render a chapter entry for the table of contents"
  (winterfell/render-chapter {:tag "intro" :number 1 :title "Introduction"
                              :elements [{:tag "sec1" :number "1.1" :title "First Steps"}]})
  => [:li
      [:a {:class "chapter", :data-scroll "", :href "#intro"} [:h4 "1 &nbsp; Introduction"]]
      [:a {:class "section", :data-scroll "", :href "#sec1"} [:h5 [:i "1.1 &nbsp; First Steps"]]]])
