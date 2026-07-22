(ns hara.uberjar.build
  (:require [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as str]
            [code.tool.build :as build]
            [code.project :as project]
            [hara.lang.impl]
            [hara.uberjar.main :as cli]
            [std.fs :as fs]
            [std.lib.os :as os]))

(def +root+ ".build/hara-uberjar")

(def +version+ (:version (project/project)))

(def +jar+ (str +root+ "/target/hara-" +version+ "-standalone.jar"))

(def +config+
  {:ns 'hara.uberjar.build
   :main 'hara.uberjar.main
   :root ".build"
   :build "hara-uberjar"
   :version +version+
   :uberjar-name (str "hara-" +version+ "-standalone.jar")
   ;; Keep source fallbacks for dependency namespaces that are not AOT compiled.
   :jar-exclusions []})

(defn- manifest-namespaces
  [manifest]
  (->> manifest
       vals
       (mapcat :files)
       (keep (fn [[_ path]]
               (when (str/ends-with? path ".clj")
                 (-> path
                     (str/replace #"\.clj$" "")
                     (str/replace "/" ".")
                     (str/replace "_" "-")
                     symbol))))
       set))

(defn- lein-command
  []
  (or (some (fn [dir]
              (let [file (java.io.File. ^String dir "lein")]
                (when (.canExecute file)
                  (.getCanonicalPath file))))
            (concat (str/split (or (System/getenv "PATH") "")
                               (re-pattern (java.util.regex.Pattern/quote
                                            java.io.File/pathSeparator)))
                    [(str (System/getProperty "user.home") "/bin")
                     (str (System/getProperty "user.home") "/.local/bin")]))
      "lein"))

(defn- discover-runtime-namespaces
  []
  (let [specs (vec (vals cli/language-namespaces))
        expr  (pr-str
               `(do
                  (require '[clojure.set :as set])
                  (let [before# (loaded-libs)]
                    (doseq [spec# '~specs]
                      (require spec#))
                    (require 'hara.uberjar.main)
                    (prn (sort (set/difference (loaded-libs) before#))))))
        command (into-array String [(lein-command) "exec" "-ep" expr])
        builder (doto (ProcessBuilder. ^"[Ljava.lang.String;" command)
                  (.directory (java.io.File. (System/getProperty "user.dir")))
                  (.redirectErrorStream true))
        process (.start builder)
        output  (slurp (.getInputStream process))
        exit    (.waitFor process)]
    (when-not (zero? exit)
      (throw (ex-info "Unable to discover Hara runtime namespaces"
                      {:exit exit :output output})))
    (-> output str/split-lines last edn/read-string set)))

(defn- runtime-aot-namespaces
  [manifest runtime-namespaces]
  (-> (set/intersection (manifest-namespaces manifest)
                        runtime-namespaces)
      (conj (:main +config+))
      sort
      vec))

(defn build!
  "stages the current Hara sources and packages the standalone uberjar"
  {:added "4.1"}
  []
  (when (fs/exists? +root+)
    (fs/delete +root+ {:recursive true}))
  (let [[manifest _ deps] (build/build-prep (:ns +config+) +config+)
        runtime (discover-runtime-namespaces)
        aot     (runtime-aot-namespaces manifest runtime)
        project (build/project-form manifest
                                    (:main +config+)
                                    (assoc +config+ :aot aot))
        _       (println "AOT namespaces:" (count aot))
        _       (build/build-copy [manifest project deps] +config+)
        ^Process process (os/sh {:root +root+
                                 :args ["lein" "uberjar"]
                                 :inherit true
                                 :wait true})
        exit (.exitValue process)]
    (when-not (zero? exit)
      (throw (ex-info "Hara uberjar build failed" {:exit exit})))
    (println +jar+)
    +jar+))

(defn -main
  "builds the standalone Hara uberjar"
  {:added "4.1"}
  [& _]
  (try
    (build!)
    (flush)
    (System/exit 0)
    (catch Throwable t
      (.printStackTrace t)
      (flush)
      (System/exit 1))))
