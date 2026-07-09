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

(doc/init-template :code {:write true})
(doc/deploy-template :code {:write true})
(doc/publish '[code/index] {:write true})
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

## Walkthrough {#walkthrough}

### Prepare the publish environment {#prepare-the-publish-environment}

Before rendering pages, assemble the project environment with `make-project`. It loads `config/publish.edn` and returns a map with `:lookup` and `:publish`.

```clojure
(require '[code.doc :as doc])

(doc/make-project)
;; => map?

(-> (doc/make-project) :publish :sites keys)
;; => (:core :hara :code :xt :std :test-site)
```

### Preview a page render {#preview-a-page-render}

Use `publish` with `:write false` to render a page in memory and preview the output path without writing files.

```clojure
(doc/publish '[code/code-test] {:write false})
;; => map with :path, :updated, and :time
```

### Initialise and deploy a theme {#initialise-and-deploy-a-theme}

Once the preview looks right, create the theme directory and copy assets. Both steps accept `:write true` to persist files.

```clojure
(doc/init-template :code {:write true})
(doc/deploy-template :code {:write true})
```

### Audit documentation coverage {#audit-documentation-coverage}

`missing` finds source namespaces that have not been referenced by any documentation page.

```clojure
(doc/missing '[code.doc]
             {:print {:result false :summary false}
              :return :all})
;; => sequence of uncovered namespaces (empty when fully covered)
```
