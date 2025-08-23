(ns std.make.compile
  (:require [std.lib :as h]
            [std.string :as str]
            [std.fs :as fs])
  (:refer-clojure :exclude [compile]))

(def ^:dynamic *mock-compile* false)

(defmacro with:mock-compile
  "sets the mock output flag"
  {:added "4.0"}
  [& body]
  `(binding [*mock-compile* true]
     ~@body))

(defn compile-fullbody
  "helper function for compile methods"
  {:added "4.0"}
  ([body {:keys [header footer]}]
   (->> (concat
         (if header [header])
         [body]
         (if footer [footer]))
        (str/join "\n\n"))))

(defn compile-out-path
  "creates the output path for file"
  {:added "4.0"}
  ([{:keys [root file target output]}]
   (or output
       (if root
         (cond-> root
           (not-empty target) (str "/" target)
           :then (str "/" file)))
       (if target
         (str target "/" file)))))

(defn compile-write
  "writes body to the output path"
  {:added "4.0"}
  ([file body]
   (if *mock-compile*
     [file body]
     (let [path (fs/path file)
           _    (if-not (fs/exists? (fs/parent path))
                  (fs/create-directory (fs/parent path)))]
       (if-not (fs/exists? file)
         (if (empty? body)
           [:unchanged file]
           [:written (doto file (spit body))])
         (let [orig (slurp file)]
           (cond (empty? body)
                 [:deleted  (doto file (fs/delete))]
                 
                 (= orig body)
                 [:unchanged   file]

                 :else
                 [:written (doto file (spit body))])))))))

(defn compile-summarise
  [files]
  (let [written (remove (comp #(= % :unchanged) first) files)]
    (merge {:files (count files)}
           (if (empty? written)
             {:status :unchanged}
             {:status :changed
              :written written}))))

;;;;
;;
;; FILES
;;

(defn compile-resource
  "copies resources to the build directory"
  {:added "4.0"}
  ([{:keys [main root] :as opts}]
   (mapv (fn [[input file]]
           (let [out-path (compile-out-path (assoc opts :file file))
                 in-path  (or (h/sys:resource input)
                              (h/error "Resource not found:" {:value input}))]
             (if *mock-compile*
               [in-path out-path]
               (if (not (fs/exists? out-path))
                 (fs/copy-single in-path out-path)))))
         main)))

(comment
  
  (fs/copy "resources/v1-assets"
           ".build/hello"
           {:include [fs/file?]
            :recursive true
            :options [:replace-existing]}))

(defn compile-directory
  "copies resources to the build directory"
  {:added "4.0"}
  ([{:keys [main root] :as opts}]
   (vec (mapcat (fn [[input output]]
                  (let [out-path (compile-out-path (assoc opts :file output))]
                    (cond *mock-compile*
                          (map #(str (fs/path out-path (fs/relativize input %))) 
                               (keys (fs/list input
                                              {:include [fs/file?]
                                               :recursive true})))

                          :else
                          (fs/copy input out-path
                                   {:include [fs/file?]
                                    :recursive true
                                    :options [:replace-existing]}))))
                main))))

;;;;
;;
;; CUSTOM
;;

(defn compile-custom
  "creates a custom"
  {:added "4.0"}
  ([{:keys [header footer root fn] :as opts}]
   (let [body  ((:fn opts) opts)
         full   (compile-fullbody body opts)
         output (compile-out-path opts)]
     (compile-write output full))))

(defonce +types+ (atom {:resource #'compile-resource
                        :directory #'compile-directory
                        :custom #'compile-custom}))

(defn types-list
  "lists all compilation types
 
   (set (types-list))
   => #{:script :module.graph :custom :module.single :resource}"
  {:added "4.0"}
  []
  (keys @+types+))

(defn types-add
  "adds a compilation type"
  {:added "4.0"}
  [k f]
  (swap! +types+ assoc k f))

(defn types-remove
  "removes a compilation type"
  {:added "4.0"}
  [k f]
  (swap! +types+ dissoc k f))

;;;;
;;
;; EXTENSIBLE
;;

(defmulti compile-ext-fn
  "creates various formats"
  {:added "4.0"}
  identity)

(defmethod compile-ext-fn :blank
  [_]
  {:fn (fn [_] "")})

(defmethod compile-ext-fn :raw
  [_]
  {:fn 'std.string/write-lines})

(defmethod compile-ext-fn :edn
  [_]
  {:fn 'clojure.core/pr-str})

(defmethod compile-ext-fn :json
  [_]
  {:fn 'std.json/write-pp})

(defmethod compile-ext-fn :yaml
  [_]
  {:fn 'script.yaml/write})

(defmethod compile-ext-fn :toml
  [_]
  {:fn 'script.toml/write})

(defmethod compile-ext-fn :html
  [_]
  {:fn 'std.html/html})

(defmethod compile-ext-fn :css
  [_]
  {:fn 'script.css/generate-css})

(defmethod compile-ext-fn :sql
  [_]
  {:fn 'script.sql/generate-sql})

(defmethod compile-ext-fn :redis
  [_]
  {:fn 'lib.redis/generate-script
   :lang    :lua
   :suffix  :lua})

(defmethod compile-ext-fn :vega
  [_]
  {:fn 'fx.vega/generate-schema
   :suffix :json})

(defmethod compile-ext-fn :gnuplot
  [_]
  {:fn 'fx.gnuplot/generate-script
   :suffix :gpl})

(defmethod compile-ext-fn :graphviz
  [_]
  {:fn 'fx.gnuplot/generate-script
   :suffix :dot})

;;;;
;;
;; PROJECT RELATED
;;

(defmethod compile-ext-fn :gitignore
  [_]
  {:fn 'std.string/write-lines
   :file ".gitignore"})

(defmethod compile-ext-fn :nginx.conf
  [_]
  {:fn 'rt.nginx.script/write
   :file "nginx.conf"})

(defmethod compile-ext-fn :dockerfile
  [_]
  {:fn 'std.string/write-lines
   :file "Dockerfile"})

(defmethod compile-ext-fn :readme.md
  [_]
  {:fn 'std.string/write-lines
   :file "README.md"})

(defmethod compile-ext-fn :makefile
  [_]
  {:fn 'std.make.makefile/write
   :file "Makefile"})

(defmethod compile-ext-fn :package.json
  [_]
  {:fn 'std.json/write-pp
   :file "package.json"})

(defn compile-resolve
  "resolves a symbol or pointer"
  {:added "4.0"}
  [x]
  (loop [x x]
    (cond (or (var? x)
              (h/pointer? x))
          (recur @x)
          
          (symbol? x)
          (recur @(resolve x))

          (fn? x)
          (recur (x))
          
          :else x)))

(defn compile-ext
  "compiles project files of different extensions"
  {:added "4.0"}
  ([{:keys [header footer main format root target name file wrapper] :as sopts
     :or {wrapper (fn [f sopts] f)}}]
   (let [{:keys [suffix lang] :as m} (compile-ext-fn format)
         main   (compile-resolve main)
         f      (:fn m)
         f      (if (symbol? f)
                  (do (h/require (symbol (namespace f)))
                      @(resolve f))
                  f)
         body   (f main)
         full   ((wrapper compile-fullbody sopts) body sopts)
         file   (or file
                    (if name (str name "." (h/strn (or suffix format))))
                    (:file m)
                    (h/error (str "Need a :file or :name entry for "
                                  (dissoc sopts :root :main))))
         output (compile-out-path (assoc sopts :file file))]
     (compile-write output full))))

(defn compile-single
  "compiles a single file"
  {:added "4.0"}
  [out-dir params {:keys [type format hook] :as sopts}]
  (let [sopts      (merge params {:root out-dir} sopts)
        compile-fn (get @+types+ type)
        result     (if compile-fn
                     (compile-fn sopts)
                     (compile-ext (assoc sopts :format (or format type))))
        _ (if hook (hook result))]
    result))

(defn compile-section
  "compiles section"
  {:added "4.0"}
  ([{:keys [root build params hooks] :as m} key section]
   (let [out-dir (if build
                   (str root "/" build)
                   root)
         {:keys [pre post]} (get hooks key)
         _ (if pre (pre key section))
         receipts (mapv (partial compile-single out-dir params) section)
         _ (if post (post key section receipts))
         summary  (filter map? receipts)
         files    (filter vector? receipts)
         written  (remove (comp #(= % :unchanged) first) files)]
     (->> (conj summary
                (merge {:files (count files)}
                       (if (empty? written)
                         {:status :unchanged}
                         {:status :changed
                          :written written})))
          (remove #(-> (:files %) (= 0)))))))

(defn compile-directive
  "compiles directive"
  {:added "4.0"}
  [m sections directive]
  (let [glob-fn (fn [patterns sopts]
                  (some (fn match-fn [pattern]
                          (cond (or (h/regexp? pattern)
                                    (string? pattern))
                                (match-fn {:name pattern})

                                (keyword? pattern)
                                (match-fn {:type pattern})

                                (map? pattern)
                                (every? (fn [[k comp]]
                                          
                                          (let [v (get sopts k)]
                                            (h/suppress (cond (h/regexp? comp) (re-find comp v)
                                                              (or (fn? comp)
                                                                  (var? comp)
                                                                  (set? comp)) (comp v)
                                                              :else (= comp v)))))
                                        pattern)

                                :else (h/error "Not valid" {:pattern pattern})))
                        patterns))]
    (cond (keyword? directive)
          (if-let [section (get sections directive)]
            (compile-section m directive section)
            :not-found)
          
          (vector? directive)
          (let [[key & patterns] directive]
            (if-let [section (get sections key)]
              (compile-section m key
                               (filter (partial glob-fn patterns) section))
              :not-found)))))

(defn compile
  "creates files based on entries"
  {:added "4.0"}
  [{:keys [instance] :as cfg} & directives]
  (let [{:keys [sections default] :as m}  @instance
        m          (dissoc m :sections :triggers)
        directives (or (not-empty directives)
                       [:default])]
    (mapv (fn [directive]
            [directive (compile-directive m (assoc sections
                                                   :default default)
                                          directive)])
          directives)))
