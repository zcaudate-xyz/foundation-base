(ns documentation.foundation-code-architecture)

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
