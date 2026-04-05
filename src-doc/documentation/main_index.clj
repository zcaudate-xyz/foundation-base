(ns documentation.main-index)

[[:hero {:title "foundation-code"
         :subtitle "A modern static documentation compiler for Clojure libraries."
         :lead "`code.doc` turns **Clojure source**, **markdown**, and **generated API metadata** into a polished multi-page site without giving up the code-first workflow that already fits this repository."
         :badges ["Static publishing" "API extraction" "Markdown + Clojure" "Themeable"]
         :actions [{:label "Get Started" :href "getting-started.html" :variant :primary}
                   {:label "See the architecture" :href "architecture.html"}
                   {:label "Review the reference" :href "reference.html"}]}]]

[[:callout {:tone :info
            :title "What changed"
            :content "The revived site focuses on **landing pages**, **guided docs**, and **generated API reference**. It keeps the existing parse → collect/link → render pipeline, but adds a sharper theme, richer authoring blocks, and better markdown ingestion."}]]

[[:card-grid {:title "Why this works for foundation-code"
              :lead "The core pipeline was already strong. The biggest lift was modernizing the authoring and presentation layers around it."
              :items [{:meta "Pipeline"
                       :title "Compiler-first docs"
                       :text "Keep documentation in versioned source files and let the compiler resolve numbering, links, references, and generated API tables."
                       :href "architecture.html"}
                      {:meta "Authoring"
                       :title "Narrative + generated reference"
                       :text "Use markdown and Clojure pages for high-level explanations, then drop in generated `:api` and `:reference` blocks where they add value."
                       :href "guides.html"}
                      {:meta "Coverage"
                       :title "Track what's still missing"
                       :text "Audit which source namespaces have not yet been surfaced in `code.doc`, just like the repository already audits tests and maintenance gaps."
                       :href "code-doc.html"}]}]]

[[:chapter {:title "Design goals"}]]

"The new `foundation-code` site is trying to do three things at once: present the library well, keep the authoring workflow close to the code, and make it easy to expand coverage over time."

[[:quote {:text "The fastest path is to treat `code.doc` as a static doc compiler with strong API/source linking, then modernize it through a new theme, better authoring blocks, and improved markdown ingestion rather than a full rewrite."
           :author "Revival plan"}]]

[[:demo {:title "A page stays close to the source"
          :content "The landing page can mix marketing-style blocks with generated technical material."
          :lang "clojure"
          :code "[[:hero {:title \"foundation-code\"\n         :actions [{:label \"Get Started\" :href \"getting-started.html\"}]}]]\n\n[[:card-grid {:items [{:title \"Compiler-first docs\"\n                       :text \"Author in source. Publish to a site.\"}]}]]"}]]
