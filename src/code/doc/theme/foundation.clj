(ns code.doc.theme.foundation
  (:require [code.doc.engine.winterfell :as engine]
            [code.doc.render.structure :as structure]
            [std.html :as html]
            [std.string.common :as common]))

(def settings
  {:engine   "winterfell"
   :resource "assets/code.doc/theme/foundation"
   :copy     ["assets"]
   :render   {:article   "render-article"
              :outline   "render-outline"
              :top-level "render-top-level"
              :page-meta "render-page-meta"
              :site-links "render-site-links"
              :volume-links "render-volume-links"}
   :manifest ["article.html"
              "home.html"
              "assets/css/foundation.css"
              "assets/css/foundation-highlight.css"
              "assets/css/widgets/xtbench.css"
              "assets/js/highlight.min.js"
              "assets/js/widgets/xtbench.js"
              "deploy.edn"
              "include.edn"]})

(defn site-pages
  [key lookup]
  (let [{:keys [name ns]} (lookup key)]
    (-> (meta lookup)
        (get (keyword ns))
        :pages
        (dissoc 'index)
        sort)))

(defn render-top-level
  "renders the sidebar links for the foundation theme"
  {:added "4.1"}
  [key _ lookup]
  (let [{:keys [name]} (lookup key)]
    (->> (site-pages key lookup)
         (map (fn [[page-key {title :title}]]
                (html/html
                 [:a {:class (str "sidebar-link"
                                   (when (= name (clojure.core/name page-key)) " active"))
                      :href (str (clojure.core/name page-key) ".html")}
                   title])))
         common/joinl)))

(defn render-site-links
  "renders page links for the home page"
  {:added "4.1"}
  [key _ lookup]
  (->> (site-pages key lookup)
        (map (fn [[page-key {:keys [title subtitle]}]]
               (html/html
                [:a {:class "site-link-card"
                    :href (str (clojure.core/name page-key) ".html")}
                 [:span {:class "site-link-title"} title]
                 [:span {:class "site-link-subtitle"} subtitle]])))
       common/joinl))

(defn- public-relative-path
  [output]
  (let [prefix "public/"]
    (cond
      (= output "public") ""
      (.startsWith ^String output prefix) (subs output (count prefix))
      :else output)))

(defn- page-prefix
  [output]
  (let [rel (public-relative-path output)]
    (if (empty? rel)
      ""
      (apply str (repeat (count (remove empty? (common/split rel #"/"))) "../")))))

(defn render-volume-links
  "renders links between configured documentation volumes"
  {:added "4.1"}
  [key _ lookup]
  (let [{current-ns :ns current-output :output} (lookup key)
        prefix (page-prefix current-output)]
    (->> (meta lookup)
         (filter (fn [[site {:keys [pages]}]]
                   (and (contains? #{:core :std :hara :code :xt} site)
                        (get pages 'index))))
         (sort-by (comp {:core 0 :std 1 :hara 2 :code 3 :xt 4} first))
         (map (fn [[site {:keys [output pages]}]]
                (let [rel   (public-relative-path output)
                      href  (str prefix (when-not (empty? rel) (str rel "/")) "index.html")
                      title (get-in pages ['index :title])]
                  (html/html
                   [:a {:class (str "sidebar-link volume-link"
                                    (when (= current-ns (name site)) " active"))
                        :href href}
                    title]))))
         common/joinl)))

(defn render-page-meta
  "renders metadata chips for the current page"
  {:added "4.1"}
  [key interim lookup]
  (let [{:keys [name title subtitle]} (lookup key)
        {:keys [project]} interim]
    (html/html
     [:div {:class "page-meta"}
      [:span {:class "meta-chip"} title]
      (when subtitle [:span {:class "meta-chip"} subtitle])
      (when-let [version (:version project)]
        [:span {:class "meta-chip"} (str "v" version)])
      (when-let [url (:url project)]
        [:a {:class "meta-chip meta-chip-link"
             :href url
             :target "_blank"}
         "Repository"])
      (when (= name "index")
        [:span {:class "meta-chip"} "Landing page"])])))

(defn render-article
  "renders the individual page for the foundation theme"
  {:added "4.1"}
  [key interim lookup]
  (let [{:keys [name]} (lookup key)]
    (->> (get-in interim [:articles name :elements])
         (map engine/page-element)
         (map html/html)
         common/joinl)))

(defn render-outline
  "renders the page outline for the foundation theme"
  {:added "4.1"}
  [key interim lookup]
  (let [{:keys [name]} (lookup key)]
    (->> (get-in interim [:articles name :elements])
         (filter #(-> % :type #{:chapter :section :subsection :subsubsection}))
         structure/structure
         :elements
         (map engine/render-chapter)
         (map html/html)
         common/joinl)))
