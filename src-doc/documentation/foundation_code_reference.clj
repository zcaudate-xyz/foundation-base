(ns documentation.foundation-code-reference)

[[:chapter {:title "Reference"}]]

"These are the namespaces that matter most when extending `code.doc` for foundation-code."

[[:chapter {:title "Parsing" :link "code.doc.parse"}]]
[[:api {:namespace "code.doc.parse"
        :only [parse-file parse-markdown parse-header parse-frontmatter]}]]

[[:chapter {:title "Publishing" :link "code.doc.executive"}]]
[[:api {:namespace "code.doc.executive"
        :only [all-pages load-theme render init-template deploy-template]}]]

[[:chapter {:title "Rendering" :link "code.doc.engine.winterfell"}]]
[[:api {:namespace "code.doc.engine.winterfell"
        :only [page-element render-chapter nav-element]}]]
