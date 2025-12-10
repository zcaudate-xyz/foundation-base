(ns code.tool.build
  (:require [code.framework.link :as link]
            [code.project :as project]
            [jvm.deps :as deps]
            [std.fs :as fs]
            [std.lib :as h]
            [std.make :as make]
            [std.string :as str]
            [clojure.pprint :as pprint]
            [std.fs.watch :as watch]
            [std.make.project :as make.project]))

(defn project-form
  "constructs the `project.clj` form"
  {:added "4.0"}
  ([manifest main]
   (let [deps (apply vector
                     ['org.clojure/clojure
                      (jvm.deps/current-version
                       'org.clojure/clojure)]
                     (mapcat :dependencies (vals manifest)))
         proj (h/-> (str main)
                    (str/split #"\.")
                    (butlast)
                    (str/join "." %)
                    symbol)]
     (h/$ (defproject ~proj "LATEST"
            :dependencies ~deps
            :profiles {:uberjar {:aot [~main]
                                 :main ~main
                                 :jar-exclusions [#"\.*\.clj"]}})))))

(defn build-deps
  "gets dependencies for a given file"
  {:added "4.0"}
  ([links ns]
   (let [{:keys [imports]} (link/file-linkage (h/sys:ns-file ns))]
     (keep (fn [[id {:keys [exports]}]]
             (if-not (empty? (h/intersection exports imports))
               id))
           links))))

(defn build-prep
  "prepares the build
 
   (build-prep 'std.lang)
   => vector?"
  {:added "4.0"}
  ([ns]
   (let [proj      (assoc (link/make-project)
                          :tag :all)
         links     (link/all-linkages proj)
         deps      (build-deps links ns)
         manifest  (link/select-manifest links deps)
         project   (project-form manifest ns)]
     [manifest project deps])))

(defn build-copy
  "copies deps to the build directory"
  {:added "4.0"}
  ([[manifest project] {:keys [ns root build main]
                        :or {main ns}}]
   (let [out-dir (str root "/" build)
         _    (fs/create-directory out-dir)
         ;; Copy Project
         _    (spit (fs/path out-dir "project.clj")
                    (with-out-str
                      (pprint/pprint project)))
         src-dir (fs/path out-dir "/src")
         ;; Copy File
         src-path (h/sys:ns-file ns)
         dst-path (h/-> (fs/relativize (fs/path ".")
                                       (h/sys:ns-file ns))
                        (fs/subpath 1 (inc (count (str/split (name ns) #"\."))))
                        (fs/path src-dir %))
         _    (fs/copy-single src-path dst-path {:options #{:replace-existing}})
         ;; Copy Deps
         src-ns (h/sys:ns-dir)
         _   (doseq [[k m] manifest]
               (doseq [[from to] (:files m)]
                 (fs/copy-single from (fs/path src-dir to)
                                 {:options #{:replace-existing}})))]
     true)))

(defn build-output
  "outputs all files to the build directory"
  {:added "4.0"}
  ([]
   (build-output (.getName *ns*)))
  ([ns-or-m]
   (let [{:keys [ns root build]
          :as conf} (if (symbol? ns-or-m)
                      @@(resolve ns-or-m)
                      ns-or-m)
         prep (build-prep ns)
         _    (build-copy prep conf)]
     (fs/list (str root "/" build)))))

(defn file-watcher-handler
  "handler for file changes"
  [path _]
  (let [path-str (str path)]
    (when (and (or (str/ends-with? path-str ".clj")
                   (str/ends-with? path-str ".cljc"))
               (not (str/includes? path-str ".#"))) ;; Emacs lock files
      (let [ns (fs/file-namespace path-str)]
        (when ns
          (try
            (println "Detected change in:" ns)
            (make.project/build-triggered ns)
            (catch Throwable t
              (println "Build failed for:" ns)
              (println (.getMessage t)))))))))

(defn watch
  "starts watching a directory"
  ([path]
   (println "Starting build watcher on:" path)
   (let [cb (fn [type file]
              (when (= type :modify)
                (file-watcher-handler (.getPath file) nil)))]
     (watch/start-watcher (watch/watcher path cb {:recursive true
                                                  :types :all})))))


(comment
  (build-output {:ns 'std.lang
                 :root ".build"
                 :build "std.lang"})
  (build-output 'std.lang)
  (build-clean 'app.jvm.resp-repl.main)
  
  (build-output 'app.jvm.resp-repl.main)
  (def -links- (link/all-linkages (link/make-project)))
  (:imports (code.framework.link/file-linkage (.getFile (h/sys:ns-url))))
  
 
  
  
  (h/make:config)
  
  
  (keys (build-service {:deps '[foundation/net.resp]}))
  (xyz.zcaudate/std.json foundation/net.resp xyz.zcaudate/std.lib)
  
  (def -out- '{:main  demo.resp-repl
               :extra [["src-play/demo/resp_repl.clj" "demo/resp_repl.clj"]]
               :package "demo"
               :deps [hara/net.resp]})
  )


(comment
  
  
  _  (clean-files manifest)
  _  (h/prn "COPY FILES")
  _  (doseq [[source dest] extra]
       (fs/copy-single (fs/path source)
                       (fs/path root (str main) "src" dest)))
  _  (copy-files manifest info)
  _  (h/prn "UBERJARRING")
  (h/sh "lein" "uberjar" {:root (str (fs/path root (str main)))
                          :inherit true}))
