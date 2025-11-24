(ns code.doc.theme.stark
  (:require [std.string :as str]
            [std.html :as html]
            [code.doc.engine.winterfell :as engine]
            [code.doc.render.structure :as structure]))

(def settings
  {:engine    "winterfell"
   :resource  "assets/code.doc/theme/stark"
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
               "assets/css/stark.css"
               "assets/css/stark-api.css"
               "assets/css/stark-highlight.css"
               "assets/css/lanyon.css"
               "assets/css/poole.css"
               "assets/css/syntax.css"
               "assets/img/logo.png"
               "assets/img/logo-white.png"
               "deploy.edn"
               "include.edn"]})

(defn render-top-level
  "renders the top-level (cover page) for the stark theme"
  {:added "3.0"}
  ([key _ lookup]
   (let [{:keys [name ns]} (lookup key)
         files (-> (meta lookup)
                   (get (keyword ns))
                   :pages
                   (dissoc 'index)
                   (sort))]
     (->> files
          (map (fn [[key {title :title}]]
                 (html/html [:a {:class (str "sidebar-nav-item"
                                             (if (= name key)
                                               " active"))
                                 :href (str key ".html")}
                             title])))
          (str/joinl)))))

(defn render-article
  "renders the individual page for the stark theme"
  {:added "3.0"}
  ([key interim lookup]
   (let [{:keys [name]} (lookup key)]
     (->> (get-in interim [:articles name :elements])
          (map engine/page-element)
          (map (fn [ele] (html/html ele)))
          (str/joinl)))))

(defn render-outline
  "renders the navigation outline for the stark theme"
  {:added "3.0"}
  ([key interim lookup]
   (let [{:keys [name]} (lookup key)]
     (->> (get-in interim [:articles name :elements])
          (filter #(-> % :type #{:chapter :section}))
          structure/structure
          :elements
          (map engine/render-chapter)
          (map (fn [ele] (html/html ele)))
          str/joinl))))
