(ns code.doc.executive-test
  (:require [code.doc :as publish]
            [code.doc.executive :refer :all]
            [std.config :as config])
  (:use code.test))

^{:refer code.doc.executive/all-pages :added "3.0"}
(fact "finds and creates entries for all documents"

  (-> (all-pages {:publish (config/load "config/code.doc.edn")})
      keys
      sort
      vec)
  ;;[hara/hara-code hara/hara-code-query ... spirit/spirit-net-mail]
  => sequential?)

^{:refer code.doc.executive/load-var :added "3.0"}
(fact "loads a var, automatically requires and loads the given namespace"

  (load-var "clojure.core" "apply")
  => fn?)

^{:refer code.doc.executive/load-theme :added "3.0"}
(fact "loads a theme to provide the renderer"

  (load-theme "bolton")
  => (contains {:engine "winterfell",
                :resource "assets/code.doc/theme/bolton",
                :copy ["assets"],
                :render map?,
                :manifest sequential?}))

^{:refer code.doc.executive/render :added "3.0"}
(comment "renders .clj file to html"

  (let [project (publish/make-project)
        lookup  (all-pages project)]
    (render 'hara/index {:write false} lookup project))
  => (contains {:path string?
                :updated boolean?
                :time number?}))

^{:refer code.doc.executive/deploy-template :added "3.0"}
(comment "copies all assets in the template folder to output"

  (deploy-template "hara"
                   {:print {:function true}}
                   (all-sites {:root "."})
                   {:root "."}))

^{:refer code.doc.executive/init-template :added "3.0"}
(comment "initialises template for customisation and deployment"

  (init-template "spirit"
                 {:write true}
                 (all-sites {:root "."})
                 {:root "."}))

(comment

  (in-context (render 'hara/hara-code {}))

  (in-context (render 'hara/index {:write true}))
  (-> (all-sites {:root "."})
      (get "hara")
      :files
      (get "index"))
  (get (all-documents {:root "."})
       'hara/index))
