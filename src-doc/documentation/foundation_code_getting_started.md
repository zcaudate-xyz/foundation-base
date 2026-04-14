---
{:title "Getting Started"
 :subtitle "bootstrap a site, a page, and a publish flow"
 :summary "Use markdown frontmatter for page metadata and embed code.doc directives directly in prose."}
---

# Bootstrap a site {#bootstrap}

[[:callout {:tone :success
            :title "Start from the theme"
            :content "Initialize the site template once, deploy the assets, then render the pages you want to publish."}]]

```clojure
(require '[code.doc :as doc])

(doc/init-template :foundation.code {:write true})
(doc/deploy-template :foundation.code {:write true})
(doc/publish '[foundation.code/index] {:write true})
```

## Author a page {#author-a-page}

A page can be a Clojure source file with directives, a markdown file with frontmatter, or a mix of both patterns in one site.

[[:demo {:title "Embedded markdown directive"
         :content "Because markdown now supports embedded directives, you can drop in callouts or demos without leaving the page."
         :lang "clojure"
         :code "[[:callout {:tone :info :title \"Hello\" :content \"This came from markdown.\"}]]"}]]

## Track gaps {#track-gaps}

Use the new coverage task to find namespaces that still are not represented in `code.doc`.

```clojure
(require '[code.doc :as doc])

(doc/missing)
```
