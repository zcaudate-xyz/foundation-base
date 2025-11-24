(ns code.doc.executive
  (:require [clojure.java.io :as io]
            [std.config :as config]
            [code.doc.prepare :as prepare]
            [std.text.diff :as text.diff]
            [std.fs :as fs]
            [code.project :as project]
            [std.task :as task]
            [std.print :as print]
            [std.print.ansi :as ansi]
            [std.string :as str]
            [std.lib :as h])
  (:import (java.io InputStream)))

(def ^:dynamic *template-path* "template")

(def ^:dynamic *default-base* "article.html")

(def ^:dynamic *default-deploy* "deploy.edn")

(def ^:dynamic *default-include* "include.edn")

(defn all-pages
  "finds and creates entries for all documents
 
   (-> (all-pages {:publish (config/load \"config/code.doc.edn\")})
       keys
       sort
      vec)"
  {:added "3.0"}
  ([{:keys [publish]}]
   (let [sites (:sites publish)]
     (with-meta (reduce-kv (fn [out k v]
                             (let [settings (dissoc v :pages)
                                   files (:pages v)
                                   k (name k)]
                               (create-ns (symbol k))
                               (reduce-kv (fn [out fk fmeta]
                                            (let [fk (name fk)
                                                  id (symbol k fk)
                                                  fv (-> fmeta
                                                         (merge settings)
                                                         (assoc :ns k)
                                                         (assoc :name fk)
                                                         (assoc :id id))]
                                              (intern (symbol k) (symbol fk) fv)
                                              (assoc out id fv)))
                                          out
                                          files)))
                           {}
                           sites)
       sites))))

(defn load-var
  "loads a var, automatically requires and loads the given namespace
 
   (load-var \"clojure.core\" \"apply\")
   => fn?"
  {:added "3.0"}
  ([ns var]
   (-> (symbol (str ns "/" var))
       resolve
       deref)))

(defn load-theme
  "loads a theme to provide the renderer
 
   (load-theme \"bolton\")
   => (contains {:engine \"winterfell\",
                 :resource \"theme/bolton\",
                 :copy [\"assets\"],
                 :render map?,
                 :manifest sequential?})"
  {:added "3.0"}
  ([theme]
   (let [ns (cond (string? theme)
                  (symbol (str "code.doc.theme." theme))

                  (symbol? theme) theme

                  :else (throw (ex-info "Cannot load theme" {:theme theme})))
         theme (do (require ns)
                   (load-var ns "settings"))]
     (update-in theme [:render]
                (fn [m]
                  (h/map-vals (fn [v] [:fn (load-var ns v)]) m))))))

(defn render
  "renders .clj file to html
 
   (let [project (publish/make-project)
         lookup  (all-pages project)]
     (render 'hara/index {:write false} lookup project))
   => (contains {:path string?
                 :updated boolean?
                 :time number?})"
  {:added "3.0"}
  ([key {:keys [write full skip] :as params} lookup project]
   (let [params  (task/single-function-print params)
         interim (prepare/prepare key params lookup project)
         {:keys [ns theme name] :as entry} (lookup key)
         theme   (load-theme theme)
         start   (System/currentTimeMillis)
         current (let [now (java.util.Date. start)]
                   {:date (-> (java.text.SimpleDateFormat. "dd MMMM yyyy")
                              (.format now))
                    :time (-> (java.text.SimpleDateFormat. "HH mm")
                              (.format now))})
         include-path  (fs/path *template-path* ns *default-include*)
         template-path (fs/path *template-path* ns (or (:base entry)
                                                       (:base theme)
                                                       *default-base*))
         output-path   (fs/path (:output entry) (str name ".html"))
         include   (-> (read-string (slurp include-path))
                       (merge (:template interim)
                              (:render theme)
                              current
                              (dissoc entry :template)
                              (get-in project [:publish :template])
                              (select-keys project [:url :version])))
         output    (slurp template-path)
         output    (reduce-kv (fn [^String html k v]
                                (let [value (cond (string? v) v

                                                  (vector? v)
                                                  (case (first v)
                                                    :fn  ((second v) key interim lookup)))]
                                  (.replaceAll html
                                               (str "<@=" (clojure.core/name k) ">")
                                               (str/escape-dollars (str value)))))
                              output
                              include)
         original  (if (fs/exists? output-path) (slurp output-path) "")
         deltas    (text.diff/diff original output)
         _         (if (-> params :print :function)
                     (print/print (text.diff/->string deltas)))
         updated   (when (and write (seq deltas))
                     (spit output-path output)
                     true)
         end       (System/currentTimeMillis)]
     (cond-> (text.diff/summary deltas)
       full  (assoc :deltas deltas)
       :them (assoc :path (str (fs/relativize (:root project) output-path))
                    :updated (boolean updated)
                    :time (- end start))))))

(defn deploy-template
  "copies all assets in the template folder to output
 
   (deploy-template \"hara\"
                    {:print {:function true}}
                   (all-sites {:root \".\"})
                    {:root \".\"})"
  {:added "3.0"}
  ([site params lookup {:keys [root]}]
   (let [{:keys [write print]} (task/single-function-print params)
         {:keys [theme]} (lookup site)
         {:keys [theme output] :as out} (lookup site)
         output-path (fs/path root output)
         template-path (fs/path root *template-path* (str/to-string site))]
     (cond (fs/exists? template-path)
           (let [{:keys [copy]} (read-string (slurp (fs/path template-path *default-deploy*)))]
             (mapcat (fn [entry]
                       (let [entry-path   (fs/path template-path entry)
                             entry-files  (filter fs/file? (fs/select entry-path))
                             output-files (map (fn [entry-file]
                                                 (fs/path output-path
                                                          (str (fs/relativize entry-path
                                                                              entry-file))))
                                               entry-files)]
                         (mapv (fn [i in out]
                                 (if write (fs/create-directory (fs/parent out)))
                                 (if (:function print)
                                   (print/println
                                    (ansi/blue (format "%4s" (inc i)))
                                    (ansi/bold (str (fs/relativize root out)))
                                    '<=
                                    (str (fs/relativize root in))))
                                 (fs/copy-single in out {:options [:replace-existing :copy-attributes]
                                                         :simulate (not write)})
                                 [(str in) (str out)])
                               (range (count entry-files))
                               entry-files
                               output-files)))
                     copy))

           :else
           (throw (ex-info (str "Template for " site " not initialiseed") {:path (str template-path)}))))))

(defn init-template
  "initialises template for customisation and deployment
 
   (init-template \"spirit\"
                  {:write true}
                 (all-sites {:root \".\"})
                  {:root \".\"})"
  {:added "3.0"}
  ([site params lookup {:keys [root]}]
   (let [{:keys [write print]} (task/single-function-print params)
         {:keys [theme]} (lookup site)
         {:keys [resource manifest]} (load-theme theme)
         template-path (fs/path root *template-path* (str/to-string site))]
     (cond (fs/exists? template-path)
           (throw (ex-info (str "Template for " site " already exists") {:path (str template-path)}))

           :else
           (let [move-list   (mapv (juxt (fn [path]
                                           (-> (str resource "/" path)
                                               (io/resource)))
                                         (fn [path]
                                           (fs/path template-path path)))
                                   manifest)]
             (mapv (fn [i [resource out]]
                     (try
                       (when write
                         (fs/create-directory (fs/parent out))
                         (with-open [input (.openStream ^java.net.URL resource)]
                           (fs/write-into out input {:options [:replace-existing]})))
                       (if (:function print)
                         (print/println
                          (ansi/blue (format "%4s" (inc i)))
                          (ansi/bold (str (fs/relativize root out)))
                          '<=
                          (str resource)))
                       (catch Exception e
                         (print/println "Cannot initialise filename:" out)))
                     out)
                   (range (count move-list))
                   move-list))))))
