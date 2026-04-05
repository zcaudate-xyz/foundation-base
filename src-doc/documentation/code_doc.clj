(ns documentation.code-doc)

[[:hero {:title "code.doc"
         :subtitle "The publish pipeline, the theme layer, and the authoring model."
         :lead "This page is the compact reference for extending `code.doc` itself: what the pipeline does, where to plug in richer authoring blocks, and how the foundation-code site now uses it."
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

[[:chapter {:title "Source references"}]]

[[:reference {:refer "code.doc/publish" :title "Publish task"}]]
[[:reference {:refer "code.doc/missing" :title "Coverage task"}]]
[[:reference {:refer "code.doc.manage/documented-coverage" :title "Coverage collector"}]]
