(ns code.doc.theme.bolton
  (:require [code.doc.theme.stark :as stark]))

(def settings
  {:engine    "winterfell"
   :resource  "assets/code.doc/theme/bolton"
   :copy      ["assets"]
   :render    {:article       "render-article"
                :outline       "render-outline"
               :top-level     "render-top-level"}
   :manifest  ["article.html"
               "home.html"
               "assets/favicon.ico"
               "assets/js/gumshoe.min.js"
               "assets/js/highlight.min.js"
               "assets/js/smooth-scroll.min.js"
               "assets/css/bolton.css"
               "assets/css/bolton-api.css"
               "assets/css/bolton-highlight.css"
               "assets/css/lanyon.css"
               "assets/css/poole.css"
               "assets/css/syntax.css"
               "assets/img/logo.png"
               "assets/img/logo-white.png"
               "deploy.edn"
               "include.edn"]})

(def render-article #'stark/render-article)

(def render-outline #'stark/render-outline)

(def render-top-level #'stark/render-top-level)
