(ns code.doc.theme.mormont
  (:require [code.doc.engine.winterfell :as engine]
            [std.html :as html]
            [std.string.common :as common]))

(def settings
  {:engine    "winterfell"
   :resource  "theme/mormont"
   :copy      ["assets"]
   :render    {:article       "render-article"
               :navigation    "render-navigation"
               :top-level     "render-top-level"}
   :defaults  {:template         "article.html"
               :icon             "favicon"
               :tracking-enabled "false"}
   :manifest  ["article.html"
               "assets/favicon.ico"
               "assets/js/highlight.min.js"
               "assets/js/gumshoe.min.js"
               "assets/js/smooth-scroll.min.js"
               "assets/css/api.css"
               "assets/css/code.css"
               "assets/css/highlight.css"
               "assets/css/page.css"]})

(defn render-top-level
  "renders the top-level (cover page) for the mormont theme"
  {:added "3.0"}
  ([interim name]
   (let [files (-> interim
                   :project
                   :publish
                   :files
                   (dissoc "index")
                   (sort))]
     (->> files
          (map (fn [[key {title :title}]]
                 [:li [:a {:href (str key ".html")} title]]))
          (concat [:ul [:li [:a {:href "index.html"} "home"]]])
          vec
          html/html))))

(defn render-article
  "renders the individual page for the mormont theme"
  {:added "3.0"}
  ([interim name]
   (->> (get-in interim [:articles name :elements])
        (map engine/page-element)
        (map (fn [ele] (html/html ele)))
        (common/joinl))))

(defn render-navigation
  "renders the navigation outline for the mormont theme"
  {:added "3.0"}
  ([interim name]
   (let [elems (get-in interim [:articles name :elements])
         telems (filter #(-> % :type #{:chapter :section :subsection}) elems)]
     (->> telems
          (map engine/nav-element)
          (map (fn [ele] (html/html ele)))
          common/joinl))))
