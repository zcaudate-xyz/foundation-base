(ns indigo.server.pages
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.html :as html]
            [std.string :as str]
            [script.css :as css]
            [indigo.client.page-demo :as page-demo]
            [indigo.client.page-index :as page-index]
            [indigo.client.page-tasks :as page-tasks]
            [std.lang.base.runtime :as default]))




(defn make-page
  [{:keys [title
           body]}]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:http-equiv "Content-Security-Policy"
            :content    "script-src self https:  http: 'unsafe-eval' 'unsafe-inline';"}]
    
    [:title title]

    ;; babel
    #_[:script {:src "https://unpkg.com/@babel/standalone/babel.min.js"}]
    
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
}"]
    
    ]
   

   [:body
    [:div {:id "root"}]
    [:script
     (or body "")]]])

(comment
  (css/generate-css
   [[".CodeMirror" {:line-height "100%"
                    :font-size "10px"
                    
                    #_#_:height "100%"
                    :top 0
                    :bottom 0
                    #_#_#_#_:right 0
                    :left 0}]])
  )

(defn index-page
  []
  (html/html
   (make-page
    {:title "Dev Interface"
     :body (emit-main 'indigo.client.page-index)})))

(defn tasks-page
  []
  (html/html
   (make-page
    {:title "Tasks Interface"
     :body (emit-main 'indigo.client.page-tasks)})))

(defn demo-page
  []
  (html/html
   (make-page
    {:title "Dev Demo Page HELLO WORLD"
     :body (emit-main 'indigo.client.page-demo)})))



(comment
;; clojure-mode
    [:script {:src "/js/clojure-mode.umd.js"}]

    ;; puck
    [:script {:type "module"}
     "import * as Puck from 'https://cdn.jsdelivr.net/npm/@measured/puck@0.20.2/+esm';\n
      window.Puck = Puck"]
    #_[:script {:src "/js/puck.umd.js"}]

    ;; radix
    [:script {:type "module"}
     "import * as RadixMain from 'https://cdn.jsdelivr.net/npm/@radix-ui/themes@3.2.1/+esm';\n
      console.log(RadixMain)
      window.RadixMain = RadixMain"]
    #_[:script {:src "/js/radix-main.umd.js"}]

    ;; react-live
    [:script {:type "module"}
     "import ReactLive from 'https://cdn.jsdelivr.net/npm/react-live@4.1.8/+esm';\n
      window.ReactLive = ReactLive"]
    
    ;; recharts
    [:script {:src "/js/recharts.umd.js"}]
  )
