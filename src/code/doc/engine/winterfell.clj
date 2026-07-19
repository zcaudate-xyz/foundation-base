(ns code.doc.engine.winterfell
  (:require [clojure.string]
            [code.doc.engine.plugin.api :as api]
            [code.doc.render.util :as util]
            [std.json :as json]))

(defmulti page-element
  "seed function for rendering a page element"
  {:added "3.0"}
  :type)

;;
;; SHADER DIRECTIVE
;;

(def ^:private +default-shader-size+ 256)

(defn- parse-refer
  "splits a 'namespace/var' refer string"
  {:added "4.0"}
  [s]
  (let [[ns-str var-str] (clojure.string/split s #"/" 2)]
    [(symbol ns-str) (symbol var-str)]))

(defn- js-string-literal
  "escapes a string for use as a single-quoted JS literal"
  {:added "4.0"}
  [s]
  (-> s
      (clojure.string/replace "\\" "\\\\")
      (clojure.string/replace "'" "\\'")
      (clojure.string/replace "\n" "\\n")
      (clojure.string/replace "\r" "\\r")
      (clojure.string/replace "\t" "\\t")))

(defn- emit-glsl-source
  "resolves a shader pointer and returns its emitted GLSL source"
  {:added "4.0"}
  [refer]
  (let [[ns-sym var-sym] (parse-refer refer)]
    (require ns-sym)
    (require 'hara.runtime.glsl)
    (when-let [display-fn (requiring-resolve 'hara.lang.workspace/ptr-display-str)]
      (when-let [v (find-var (symbol (str ns-sym) (str var-sym)))]
        (display-fn @v)))))

(defn- shader-preview-script
  "returns inline JS that renders the shader on a canvas"
  {:added "4.0"}
  [canvas-id frag-src]
  (str "(function(canvasId, fragSrc){\n"
       "  var canvas = document.getElementById(canvasId);\n"
       "  if(!canvas) return;\n"
       "  var gl = canvas.getContext('webgl');\n"
       "  if(!gl){ console.error('WebGL not supported'); return; }\n"
       "  function compile(type, src){\n"
       "    var s = gl.createShader(type);\n"
       "    gl.shaderSource(s, src);\n"
       "    gl.compileShader(s);\n"
       "    if(!gl.getShaderParameter(s, gl.COMPILE_STATUS)){\n"
       "      console.error(gl.getShaderInfoLog(s));\n"
       "    }\n"
       "    return s;\n"
       "  }\n"
       "  var vs = compile(gl.VERTEX_SHADER, 'attribute vec2 a_position;\\nvoid main(){ gl_Position = vec4(a_position, 0.0, 1.0); }');\n"
       "  var fs = compile(gl.FRAGMENT_SHADER, 'precision mediump float;\\n' + fragSrc.replace(/^#version[^\\n]*\\n?/gm, ''));\n"
       "  var prog = gl.createProgram();\n"
       "  gl.attachShader(prog, vs);\n"
       "  gl.attachShader(prog, fs);\n"
       "  gl.linkProgram(prog);\n"
       "  if(!gl.getProgramParameter(prog, gl.LINK_STATUS)){\n"
       "    console.error(gl.getProgramInfoLog(prog)); return;\n"
       "  }\n"
       "  gl.useProgram(prog);\n"
       "  var buf = gl.createBuffer();\n"
       "  gl.bindBuffer(gl.ARRAY_BUFFER, buf);\n"
       "  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1,-1,3,-1,-1,3]), gl.STATIC_DRAW);\n"
       "  var loc = gl.getAttribLocation(prog, 'a_position');\n"
       "  gl.enableVertexAttribArray(loc);\n"
       "  gl.vertexAttribPointer(loc, 2, gl.FLOAT, false, 0, 0);\n"
       "  var uRes = gl.getUniformLocation(prog, 'u_resolution');\n"
       "  var uTime = gl.getUniformLocation(prog, 'u_time');\n"
       "  function draw(t){\n"
       "    gl.viewport(0,0,canvas.width,canvas.height);\n"
       "    gl.clearColor(0,0,0,1);\n"
       "    gl.clear(gl.COLOR_BUFFER_BIT);\n"
       "    if(uRes) gl.uniform2f(uRes, canvas.width, canvas.height);\n"
       "    if(uTime) gl.uniform1f(uTime, (t || 0) * 0.001);\n"
       "    gl.drawArrays(gl.TRIANGLES, 0, 3);\n"
       "    requestAnimationFrame(draw);\n"
       "  }\n"
       "  requestAnimationFrame(draw);\n"
       "})('" canvas-id "', '" (js-string-literal frag-src) "');\n"))

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
   (into
    [:section {:class "hero"}]
    (concat
     (when (seq badges)
       [(into
         [:div {:class "hero-badges"}]
         (map (fn [badge]
                [:span {:class "badge badge-hero"} badge])
              badges))])
     [[:div {:class "hero-copy"}
       [:h1 title]
       (when subtitle [:p {:class "hero-subtitle"} subtitle])
       (when lead [:div {:class "hero-lead"}
                   (util/basic-html-unescape (util/markup lead))])]]
     (when (seq actions)
       [(into
         [:div {:class "hero-actions"}]
         (map (fn [{:keys [href label variant]}]
                [:a {:class (str "hero-action"
                                 (when variant
                                   (str " hero-action-" (name variant))))
                     :href href}
                 label])
              actions))])))))

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
   (into
    [:section {:class "card-grid-shell"}]
    (concat
     [(when title [:div {:class "card-grid-heading"} [:h3 title]])
      (when lead [:div {:class "card-grid-lead"}
                  (util/basic-html-unescape (util/markup lead))])]
     [(into
       [:div {:class "card-grid"}]
       (map (fn [{:keys [title text href meta]}]
              [:article {:class "card"}
               (when meta [:span {:class "card-meta"} meta])
               [:h4 title]
               [:div {:class "card-text"}
                (util/basic-html-unescape (util/markup (or text "")))]
               (when href
                 [:a {:href href} "Learn more"])])
            items))]))))

(defmethod page-element :related
  ([{:keys [title items error]}]
   (if error
     [:pre {:class "error"} error]
     [:section {:class "related-shell"}
      (when title
        [:div {:class "related-heading"} [:h3 title]])
      (into
       [:div {:class "related-table"}]
       (map (fn [{:keys [name href description comparison group]}]
              [:div {:class "related-row"}
               [:div {:class "related-name"}
                (if href
                  [:a {:href href} name]
                  name)
                (when group
                  [:span {:class "related-family"} (clojure.core/name group)])]
               [:div {:class "related-description"}
                (util/basic-html-unescape (util/markup (or description "")))]
               (when comparison
                 [:div {:class "related-comparison"}
                  (util/basic-html-unescape (util/markup comparison))])])
            items))])))

(defmethod page-element :links
  ([{:keys [items error]}]
   (if error
     [:pre {:class "error"} error]
     (into
      [:div {:class "links-list"}]
      (map (fn [{:keys [label href]}]
             [:a {:href href} label])
           items)))))

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

(defn widget-js-loader
  "returns a loader for a JavaScript widget mount function"
  {:added "4.1"}
  [src props]
  (let [src-json   (json/write src)
        props-json (-> (json/write (or props {}))
                       (clojure.string/replace "<" "\\u003c")
                       (clojure.string/replace ">" "\\u003e")
                       (clojure.string/replace "&" "\\u0026"))]
    (str "(function(root){"
         "root.dataset.widgetState='loading';"
         "import(" src-json ").then(function(module){"
         "if(typeof module.mount!=='function'){throw new Error('Widget module must export mount(element, props)');}"
         "return module.mount(root," props-json ");"
         "}).then(function(){root.dataset.widgetState='ready';})"
         ".catch(function(error){root.dataset.widgetState='error';"
         "root.textContent='Widget failed to load: '+error.message;console.error(error);});"
         "})(document.currentScript.previousElementSibling);")))

(defmethod page-element :widget/js
  ([{:keys [src props class fallback]}]
   (when-not (seq src)
     (throw (ex-info ":widget/js requires a non-empty :src" {:src src})))
   [:div
    [:div {:class (str "widget-js" (when (seq class) (str " " class)))
           :data-widget-state "pending"
           :aria-live "polite"}
     (or fallback "Loading widget…")]
    [:script {:type "text/javascript"}
     (widget-js-loader src props)]]))

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

(defmethod page-element :shader
  ([{:keys [refer live width height line] :as elem}]
   (let [source   (or (emit-glsl-source refer)
                      (str ";; could not emit shader: " refer))
         live?    (if (nil? live) true live)
         w        (or width +default-shader-size+)
         h        (or height +default-shader-size+)
         cid      (str "shader-canvas-" line)]
     (into
      [:div {:class "shader-block"}]
      (concat
       [[:pre
         [:code {:class "glsl"}
          (-> source
              clojure.string/trim
              util/basic-html-escape)]]]
       (when live?
         [[:canvas {:id cid :width w :height h}]
          [:script {:type "text/javascript"}
           (shader-preview-script cid source)]]))))))

(defmethod page-element :code
  ([{:keys [tag number title code lang indentation failed path caption] :as elem}]
   [:div {:class "code"}
    (if tag [:a {:id tag}])
    (if number
      [:h4 [:i (str "e."
                    number
                    (if title (str "  &nbsp;-&nbsp; " title)))]])
    (if caption
      [:p {:class "code-caption"}
       (util/basic-html-escape caption)])
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
   (let [sections (mapv (fn [{:keys [tag title number] :as elem}]
                          [:a {:class "section"
                               :data-scroll ""
                               :href (str "#" tag)}
                           [:h5 [:i (str number " &nbsp; " title)]]])
                        elements)
         api-entries (when (and (empty? sections) link table)
                       (let [entries (api/select-entries elem)]
                         (mapv (fn [entry]
                                 [:a {:class "section"
                                      :data-scroll ""
                                      :href (str "#" (api/entry-tag link entry))}
                                  [:h5 [:i (str entry)]]])
                               entries)))]
     (apply vector
            :li
            [:a {:class "chapter"
                 :data-scroll ""
                 :href (str "#" tag)}
             [:h4 (str number " &nbsp; " title)]]
            (concat sections api-entries)))))

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
