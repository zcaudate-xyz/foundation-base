(ns code.dev.server.api-common
  (:require [code.heal :as heal]
            [std.json :as json]
            [std.block :as block]
            [std.lib :as h]
            [std.lang :as l]
            [std.html :as html]
            [std.lib.walk :as walk]
            [std.string :as str]
            [std.fs :as fs]
            [script.css :as css]))

(comment
  (fn [req]
    (json/write
     {:data (from-html (:body req))})))

(defn wrap-api-log
  [f]
  (fn [req]
    ))


(defn wrap-api-call
  [f])



(defn make-page-script
  [main]
  (l/emit-script
   (list (l/sym-full main))
   {:lang :js
    :library (l/default-library)
    :module  (l/get-module (l/default-library)
                           :js
                           (:module main))
    :emit {:native {:suppress true}
           :lang/jsx false}
    :layout :full}))

(defn make-page-template
  [{:keys [title
           body
           main]}]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:http-equiv "Content-Security-Policy"
            :content    "script-src self https:  http: 'unsafe-eval' 'unsafe-inline';"}]
    
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
    [:script {:src "https://unpkg.com/@xtalk/clojure-mode@0.3.7/dist/clojure-mode.umd.js"}]    

    
    ;; daisyui
    [:link {:href "https://cdn.jsdelivr.net/npm/daisyui@5"
            :rel "stylesheet" :type "text/css"}]
    [:link {:href "https://cdn.jsdelivr.net/npm/daisyui@5/themes.css"
            :rel "stylesheet" :type "text/css"}]    

    ;; tailwind
    [:script {:src "https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"}]
    
    ;; lucide
    [:script {:src "https://unpkg.com/lucide/dist/umd/lucide.min.js"}]
    [:script
     "window.Lucide = window.lucide;"]
    
    ;; react
    [:script {:src "https://unpkg.com/react@18/umd/react.development.js", :crossorigin ""}]
    [:script {:src "https://unpkg.com/react-dom@18/umd/react-dom.development.js" :crossorigin ""}]

    ;; react hook form
    [:script {:src "https://unpkg.com/react-hook-form@7/dist/index.umd.js"}]
    [:script {:src "https://cdn.jsdelivr.net/npm/zod@3/lib/index.umd.js"}]
    [:script {:src "https://cdn.jsdelivr.net/npm/@hookform/resolvers/zod/dist/zod.umd.js"}]    
    
    ;; react query
    [:script {:src "https://unpkg.com/react-query@3/dist/react-query.production.min.js"}]

    ;; diff
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/jsdiff/5.1.0/diff.min.js"}]
    [:link {:href "https://cdn.jsdelivr.net/npm/@radix-ui/themes@latest/styles.css"
            :rel "stylesheet" :type "text/css"}]
    
    
    
    [:style
     (css/generate-css
      [[".CodeMirror"
        {:font-size "10px"
         :line-height "1.0"
         :min-height "300px"}]

       [".DiffViewer pre"
        {:font-size "9px !important"}]
       
       [".DiffViewer table td:first-of-type"
        {:font-size "9px !important"}]])]]
   
   [:body
    [:div {:id "root"}]
    [:script
     (if main
       (make-page-script main)
       (or body ""))]]])

(defn page
  [title main]
  (html/html
   (make-page-template {:title title
                        :main main})))

(defn page-handler
  [title main]
  (fn [req]
    {:status 200
     :headers {"Content-Type" "text/html"
               "Content-Security-Policy" "script-src self https:  http: 'unsafe-eval' 'unsafe-inline';"}
     :body   (page title main)}))

(defn prompt-handler
  [f op]
  (fn [{:keys [body]}]
    (let [id (h/sid)
          time-start   (h/time-ms)
          output       (f body)
          time-end     (h/time-ms)
          res          {:id id
                        :op op
                        :time-start time-start
                        :time-end   time-end
                        :input body
                        :output output}
          log-dir      (str ".prompts/log/" op)]
      (fs/create-directory log-dir)
      (spit (str log-dir "/" id ".edn")
            (str (block/layout
                  res)))
      {:status  200
       :headers {"Content-Type" "application/json"}
       :body    (json/write res)})))

(defn create-routes
  [prefix routes]
  (h/map-keys (fn [k]
                (str prefix k))
              routes))

(defn create-prompt-routes
  [prefix routes]
  (h/map-entries
   (fn [[op f]]
     [(str prefix op)
      (prompt-handler f op)])
   routes))





(comment


  ".CodeMirror {
  font-size: 10px; /* Your desired font size */
  
  /* IMPORTANT: With CodeMirror 5, you usually need to 
    adjust the line-height along with the font-size 
    to keep the cursor and line numbers aligned.
  */
  line-height: 1.0;
  min-height: 300px;
}

  /* This targets the diff viewer's preformatted text blocks */
.DiffViewer pre {
  font-size: 9px !important;
}

/* You may also need to target the line numbers */
.DiffViewer table td:first-of-type {
  font-size: 9px !important;
}")
