(ns documentation.code-doc
  (:require [code.doc :as doc]
            [code.doc.executive :as executive]
            [code.project :as project]))

[[:hero {:title "code.doc"
         :subtitle "The publish pipeline, the theme layer, and the authoring model."
         :lead "This page is the compact reference for extending `code.doc` itself: what the pipeline does, where to plug in richer authoring blocks, and how the code site now uses it."
         :badges ["Compiler" "Themes" "Coverage audit"]
         :actions [{:label "Read the architecture" :href "architecture.html" :variant :primary}
                   {:label "Jump to reference" :href "reference.html"}]}]]

[[:chapter {:title "Pipeline"}]]

"`code.doc` already had the right backbone for a documentation compiler. The work in this refresh keeps that backbone and upgrades the pieces around it."

[[:card-grid {:items [{:meta "1"
                       :title "Parse"
                       :text "Clojure pages and markdown pages are parsed into a common element structure."}
                      {:meta "2"
                       :title "Collect + link"
                       :text "References, API entries, numbering, anchors, and stencil links are resolved into a renderable interim representation."}
                      {:meta "3"
                       :title "Render"
                       :text "A theme provides render functions and templates that turn the interim structure into static HTML."}]}]]

[[:callout {:tone :success
            :title "New in the revival"
            :content "The revived version adds a **foundation theme**, richer marketing-style directives, better markdown parsing, and a `code.doc/missing` audit so you can track namespaces that still need documentation coverage."}]]

[[:chapter {:title "Core entry points" :link "code.doc"}]]

[[:api {:namespace "code.doc"
        :only [make-project make-audit-project publish init-template deploy-template missing]}]]

[[:chapter {:title "Management + coverage" :link "code.doc.manage"}]]

[[:api {:namespace "code.doc.manage"
        :only [source-namespaces documented-coverage missing-namespaces]}]]

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Preparing the project"}]]

"`make-project` assembles the environment needed by the publish pipeline: the project map, namespace lookup, and publish config. `all-pages` then builds a lookup of every documented page."

^{:refer code.doc/make-project :added "3.0"}
(fact "create the publish environment"
  (doc/make-project)
  => map?)

^{:refer code.doc.executive/all-pages :added "3.0"}
(fact "list all documentation pages"
  (-> (doc/make-project)
      (executive/all-pages)
      keys)
  => seq?)

[[:section {:title "Rendering a page"}]]

"`publish` renders a page key to HTML. With `:write false` it previews the output and reports whether anything changed."

^{:refer code.doc/publish :added "3.0"}
(fact "render a single page without writing"
  (doc/publish '[code/code-test] {:write false})
  => (contains {:path string?
                :updated boolean?}))

[[:section {:title "Coverage audit"}]]

"`missing` reports source namespaces that are not referenced by any documentation page. Use it to find gaps in the docs."

^{:refer code.doc/missing :added "4.1"}
(fact "run a coverage audit"
  (doc/missing '[code.doc]
               {:print {:result false :summary false}
                :return :all})
  => any)

[[:section {:title "End-to-end: render a single page"}]]

"Combine `make-project` and `publish` to render a single page without writing. The result shows the output path and whether the HTML changed."

^{:refer code.doc/publish :added "3.0"}
(fact "render one documentation page"
  (doc/publish '[code/code-test] {:write false})
  => (contains {:path string?
                :updated boolean?}))

[[:chapter {:title "Source references"}]]

[[:reference {:refer "code.doc/publish" :title "Publish task"}]]
[[:reference {:refer "code.doc/missing" :title "Coverage task"}]]
[[:reference {:refer "code.doc.manage/documented-coverage" :title "Coverage collector"}]]
