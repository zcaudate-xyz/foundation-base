(ns code.doc.engine.winterfell
  (:require [clojure.string]
            [code.doc.engine.plugin.api :as api]
            [code.doc.render.util :as util]))

(defmulti page-element
  "seed function for rendering a page element"
  {:added "3.0"}
  :type)

(defmethod page-element :html
  ([{:keys [src]}]
   src))

(defmethod page-element :block
  ([elem]
   (page-element (assoc elem :type :code :origin :block))))

(defmethod page-element :ns
  ([elem]
   (page-element (assoc elem :type :code :origin :ns))))

(defmethod page-element :test
  ([elem]
   (page-element (assoc elem :type :code :origin :test))))

(defmethod page-element :reference
  ([elem]
   (page-element (assoc elem :type :code :origin :reference))))

(defmethod page-element :chapter
  ([{:keys [tag number title]}]
   [:div
    (if tag [:span {:id tag}])
    [:h2 [:b (str number " &nbsp;&nbsp; " title)]]]))

(defmethod page-element :section
  ([{:keys [tag number title]}]
   [:div
    (if tag [:span {:id tag}])
    [:h3 (str number " &nbsp;&nbsp; " title)]]))

(defmethod page-element :subsection
  ([{:keys [tag number title]}]
   [:div
    (if tag [:span {:id tag}])
    [:h3 [:i (str number " &nbsp;&nbsp; " title)]]]))

(defmethod page-element :subsubsection
  ([{:keys [tag number title]}]
   [:div
    (if tag [:span {:id tag}])
    [:h4 [:i (str number " &nbsp;&nbsp; " title)]]]))

(defmethod page-element :paragraph
  ([{:keys [text]}]
    [:div (util/basic-html-unescape (util/markup text))]))

(defmethod page-element :hero
  ([{:keys [title subtitle lead actions badges]}]
   [:section {:class "hero"}
    (when (seq badges)
      [:div {:class "hero-badges"}
       (mapv (fn [badge]
               [:span {:class "badge badge-hero"} badge])
             badges)])
    [:div {:class "hero-copy"}
     [:h1 title]
     (when subtitle [:p {:class "hero-subtitle"} subtitle])
     (when lead [:div {:class "hero-lead"}
                 (util/basic-html-unescape (util/markup lead))])]
    (when (seq actions)
      [:div {:class "hero-actions"}
       (mapv (fn [{:keys [href label variant]}]
               [:a {:class (str "hero-action"
                                (when variant
                                  (str " hero-action-" (name variant))))
                    :href href}
                label])
             actions)])]))

(defmethod page-element :callout
  ([{:keys [tone title content]}]
   [:aside {:class (str "callout"
                        (when tone
                          (str " callout-" (name tone))))}
    (when title [:h4 title])
    (when content
      [:div (util/basic-html-unescape (util/markup content))])]))

(defmethod page-element :card-grid
  ([{:keys [title lead items]}]
   [:section {:class "card-grid-shell"}
    (when title [:div {:class "card-grid-heading"} [:h3 title]])
    (when lead [:div {:class "card-grid-lead"}
                (util/basic-html-unescape (util/markup lead))])
    [:div {:class "card-grid"}
     (mapv (fn [{:keys [title text href meta]}]
             [:article {:class "card"}
              (when meta [:span {:class "card-meta"} meta])
              [:h4 title]
              [:div {:class "card-text"}
               (util/basic-html-unescape (util/markup text))]
              (when href
                [:a {:href href} "Learn more"])])
           items)]]))

(defmethod page-element :quote
  ([{:keys [text author source]}]
   [:blockquote {:class "doc-quote"}
    [:p text]
    (when (or author source)
      [:footer
       (str author
            (when (and author source) " · ")
            source)])]))

(defmethod page-element :badge
  ([{:keys [label tone]}]
   [:span {:class (str "badge"
                       (when tone
                         (str " badge-" (name tone))))}
    label]))

(defmethod page-element :demo
  ([{:keys [title content code lang]}]
   [:section {:class "demo-block"}
    (when title [:h4 title])
    (when content
      [:div {:class "demo-copy"}
       (util/basic-html-unescape (util/markup content))])
    (when code
      [:pre
       [:code {:class (or lang "clojure")}
        (-> code
            util/basic-html-escape
            clojure.string/trim)]])]))

(defmethod page-element :image
  ([{:keys [tag number title] :as elem}]
   [:div {:class "figure"}
    (if tag [:a {:id tag}])
    (if number
      [:h4 [:i (str "fig."
                    number
                    (if title (str "  &nbsp;-&nbsp; " title)))]])
    [:div {:class "img"}
     [:img (dissoc elem :number :type :tag)]]
    [:p]]))

(defmethod page-element :code
  ([{:keys [tag number title code lang indentation failed path] :as elem}]
   [:div {:class "code"}
    (if tag [:a {:id tag}])
    (if number
      [:h4 [:i (str "e."
                    number
                    (if title (str "  &nbsp;-&nbsp; " title)))]])
    [:pre
     [:code {:class (or lang "clojure")}
      (-> code
          (util/join-string)
          (util/basic-html-escape)
          (util/adjust-indent indentation)
          (clojure.string/trim))]]
    (if failed
      (apply vector
             :div
             {:class "failed"}
             [:h4
              (str "FAILED: " (count (:output failed)))
              (str "&nbsp;&nbsp;&nbsp;FILE: " path)
              (str "&nbsp;&nbsp;&nbsp;LINE: " (:line failed))]
             [:hr]
             (map (fn [{:keys [data form check code]}]
                    [:div
                     [:h5 "&nbsp;"]
                     [:h5 "Line: " (:line code)]
                     [:h5 "Expression: " (str form)]
                     [:h5 "Expected: " (str check)]
                     [:h5 "Actual: " (str data)]])
                  (:output failed))))]))

(defmethod page-element :api
  ([elem]
   (api/api-element elem)))

(defmethod page-element :default
  ([{:keys [line] :as elem}]
   (throw (Exception. (str "Cannot process element:" elem)))))

(defn render-chapter
  "seed function for rendering a chapter element"
  {:added "3.0"}
  ([{:keys [tag title number elements
            link table only exclude] :as elem}]
   (apply vector
          :li
          [:a {:class "chapter"
               :data-scroll ""
               :href (str "#" tag)}
           [:h4 (str number " &nbsp; " title)]]
          (cond (and link table)
                (let [entries (api/select-entries elem)]
                  (mapv (fn [entry]
                          [:a {:class "section"
                               :data-scroll ""
                               :href (str "#" (api/entry-tag link entry))}
                           [:h5 [:i (str entry)]]])
                        entries))

                :else
                (mapv (fn [{:keys [tag title number] :as elem}]
                        [:a {:class "section"
                             :data-scroll ""
                             :href (str "#" tag)}
                         [:h5 [:i (str number " &nbsp; " title)]]])
                      elements)))))

(defmulti nav-element
  "seed function for rendering a navigation element"
  {:added "3.0"}
  :type)

(defmethod nav-element :chapter
  ([{:keys [tag number title]}]
   [:h4
    [:a {:href (str "#" tag)} (str number " &nbsp; " title)]]))

(defmethod nav-element :section
  ([{:keys [tag number title]}]
   [:h5 "&nbsp;&nbsp;"
    [:i [:a {:href (str "#" tag)} (str number " &nbsp; " title)]]]))

(defmethod nav-element :subsection
  ([{:keys [tag number title]}]
   [:h5 "&nbsp;&nbsp;&nbsp;&nbsp;"
    [:i [:a {:href (str "#" tag)} (str number " &nbsp; " title)]]]))
