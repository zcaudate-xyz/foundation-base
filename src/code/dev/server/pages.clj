(ns code.dev.server.pages
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.html :as html]
            [std.string :as str]
            [code.dev.client.page-demo :as demo]
            [code.dev.client.page-index :as index]
            [std.lang.base.runtime :as default]))

(defn emit-main
  [ns]
  (l/emit-script
   '(-/main)
   {:lang :js
    :library (l/default-library)
    :module  (l/get-module (l/default-library)
                           :js
                           ns)
    :emit {:native {:suppress true}
           :lang/jsx false}
    :layout :flat}))
(defn make-page
  [{:keys [title
           body]}]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:title title]
    
    ;; codemirror
    [:link {:href "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.15/codemirror.min.css"
            :rel "stylesheet" :type "text/css"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.15/codemirror.min.js"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.15/mode/javascript/javascript.min.js"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.15/mode/python/python.min.js"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.15/mode/xml/xml.min.js"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.15/mode/css/css.min.js"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.15/mode/htmlmixed/htmlmixed.min.js"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.15/mode/sql/sql.min.js"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.15/mode/clojure/clojure.min.js"}]    
    
    ;; daisyui
    [:link {:href "https://cdn.jsdelivr.net/npm/daisyui@5"
            :rel "stylesheet" :type "text/css"}]
    [:link {:href "https://cdn.jsdelivr.net/npm/daisyui@5/themes.css"
            :rel "stylesheet" :type "text/css"}]    
    
    ;; tailwind
    [:script {:src "https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"}]

    ;; lucide
    [:script {:src "https://unpkg.com/lucide/dist/umd/lucide.min.js"}]
    
    ;; react
    [:script {:src "https://unpkg.com/react@18/umd/react.development.js", :crossorigin ""}]
    [:script {:src "https://unpkg.com/react-dom@18/umd/react-dom.development.js" :crossorigin ""}]

    ;; react hook form
    [:script {:src "https://unpkg.com/react-hook-form@7/dist/index.umd.js"}]
    [:script {:src "https://cdn.jsdelivr.net/npm/zod@3/lib/index.umd.js"}]
    [:script {:src "https://cdn.jsdelivr.net/npm/@hookform/resolvers/zod/dist/zod.umd.js"}]
    
    ;; react query
    [:script {:src "https://unpkg.com/react-query@3/dist/react-query.production.min.js"}]

    [:style
     ".CodeMirror {
  font-size: 10px; /* Your desired font size */
  
  /* IMPORTANT: With CodeMirror 5, you usually need to 
    adjust the line-height along with the font-size 
    to keep the cursor and line numbers aligned.
  */
  line-height: 1.0; 
}"]
    #_#_#_ ;; flowbite
    [:link   {:href "https://cdnjs.cloudflare.com/ajax/libs/flowbite/2.3.0/flowbite.min.css"
              :rel "stylesheet"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/flowbite/2.3.0/flowbite.min.js"}]
    [:script {:src "https://unpkg.com/flowbite-react@0.9.0/dist/flowbite-react.umd.js"}]
    
    #_#_ ;; babel
    [:script {:src "https://unpkg.com/@babel/standalone/babel.min.js"}]
    [:script {:type "text/babel"}]]
   [:body
    [:div {:id "root"}]
    [:script
     {:type "text/javascript"}
     (or body "")]]])

(defn index-page
  []
  (html/html
   (make-page
    {:title "Dev Interface"
     :body (emit-main 'code.dev.client.page-index)})))

(defn demo-page
  []
  (html/html
   (make-page
    {:title "Dev Demo Page HELLO WORLD"
     :body (emit-main 'code.dev.client.page-demo)})))


