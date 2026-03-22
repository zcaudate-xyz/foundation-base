(ns code.doc.engine.plugin.api
  (:require [clojure.set]
            [clojure.string]
            [code.doc.render.util :as util]))

(defn entry-tag
  "helper for formating vars
 
   (entry-tag 'code.doc 'output-path)
   = \"entry__code.doc__output_path\""
  {:added "3.0"}
  ([ns var]
   (-> ^String (munge (str "entry__" ns "__" var))
       (.replaceAll  "\\." "_")
       (.replaceAll "\\$" "STAR"))))

(defn lower-first
  "converts the first letter to lowercase
 
   (lower-first \"Hello\")
   => \"hello\""
  {:added "3.0"}
  ([^String s]
   (if (empty? s)
     ""
     (str (-> s (.charAt 0) str (.toLowerCase))
          (subs s 1)))))

(defn api-entry-example
  "helper function to convert a test entry into a html tree form"
  {:added "3.0"}
  ([entry project]
   (let [code (-> (-> entry :test :code)
                  (or "")
                  (util/adjust-indent 2)
                  (clojure.string/trim))]
     (if (-> entry :test :code)
       [:pre
        [:h6 [:i [:a {:href (format "%s/blob/master/%s#L%d-L%d"
                                    (:url project)
                                    (-> entry :test :path)
                                    (-> entry :test :line :row)
                                    (-> entry :test :line :end-row))
                      :target "_blank"}
                  "link"]]]
        [:code {:class "clojure"} code]]
       [:pre {:class "error"}
        [:h6 "example not found"]
        [:code ""]]))))

(defn api-entry-source
  "helper function to convert a source entry into a html tree form"
  {:added "3.0"}
  ([entry project var namespace]
   (if (nil? entry)
     [:pre {:class "error"} [:h6 "source not found"] [:code ""]]
     [:div {:class "entry-option"}
      [:h6
       (if-let [version (-> entry :meta :added)]
         [:a {:href (format "%s/blob/master/%s#L%d-L%d"
                            (:url project)
                            (-> entry :source :path)
                            (-> entry :source :line :row)
                            (-> entry :source :line :end-row))
              :target "_blank"}
          "v&nbsp;" version]
         [:i {:class "error version"} "NONE"])]
      [:div
       [:input {:class "source-toggle"
                :type "checkbox"
                :id (entry-tag (str "pre-" namespace)  var)}]
       [:label {:class "source-toggle"
                :for (entry-tag (str "pre-" namespace) var)} ""]
       [:pre {:class "source"}
        [:code {:class "clojure"}
         (-> entry :source :code)]]]])))

(defn api-entry
  "formats a `ns/var` pair tag into an html element"
  {:added "3.0"}
  ([[var entry :as pair] {:keys [project namespace] :as elem}]
   [:div {:class "entry"}
    [:span {:id (entry-tag namespace var)}]
    [:div {:class "entry-description"}
     [:h4 [:b (str var) "&nbsp"
           [:a {:data-scroll ""
                :href (str "#" (entry-tag namespace ""))} "^"]]]
     [:p [:i (lower-first (:intro entry))]]]
    (api-entry-source entry project var namespace)
    (api-entry-example entry project)]))

(defn select-entries
  "selects api entries based on filters"
  {:added "3.0"}
  ([{:keys [table class only exclude module] :as elem}]
   (let [module  (if module (set module))
         entries (sort (keys table))
         entries (if module
                   (filter (fn [k]
                             (let [ns (get-in table [k :ns])]
                               (get module (str ns))))
                           entries)
                   entries)
         entries (if class
                   (filter (fn [k]
                             (when-let [kcls (get-in table [k :class])]
                               (seq (clojure.set/intersection (set kcls) (set class)))))
                           entries)
                   entries)
         entries (if only
                   (filter (set (map symbol only)) entries)
                   entries)
         entries (if exclude
                   (remove (set (map symbol exclude)) entries)
                   entries)]
     entries)))

(defn api-element
  "displays the entire `:api` namespace"
  {:added "3.0"}
  ([{:keys [table tag title namespace project display]
     :or {title ""
          display #{:tags :entries}}
     :as elem}]
   (let [entries (select-entries elem)]
     [:div {:class "api"}
      [:span {:id (entry-tag namespace "")}]
      (if (not= "" title)
        [:div [:h2 (or title namespace)]])
      [:hr]
      [:div
       (if (display :tags)
         (apply vector
                :ul
                (map (fn [v]
                       [:li [:a {:data-scroll ""
                                 :href (str "#" (entry-tag namespace v))}
                             (str v)]])
                     entries)))
       (if (= display #{:tags :entries})
         [:hr {:style "margin-bottom: 0"}])
       (if (display :entries)
         (apply vector
                :div
                (->> entries
                     (map (juxt identity table))
                     (map #(api-entry % elem)))))]])))
