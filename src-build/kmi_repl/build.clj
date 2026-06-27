(ns kmi-repl.build
  (:require [clojure.java.io :as io]
            [clojure.string]
            [std.make :as make :refer [def.make]]
            [hara.lang :as l]
            [hara.lang.compile]))

(def +gitignore+
  ["node_modules"])

(def +makefile+
  [[:.PHONY {:- ["run"]}]
   [:run
    ["@node main.js"]]])

(def +package+
  {"name" "kmi-repl"
   "version" "0.0.1"
   "private" true
   "main" "main.js"
   "bin" {"kmi-repl" "main.js"}})

(def.make PROJECT
  {:github   {:repo "greenways/kmi-repl"
              :description "Interactive KMI REPL for Node.js"}
   :orgfile  "Main.org"
   :triggers '#{kmi-repl.main}
   :build    ".build/kmi-repl"
   :sections {:setup [{:type :gitignore
                       :main +gitignore+}
                      {:type :makefile
                       :main +makefile+}
                      {:type :package.json
                       :main +package+}]}
   :default  [{:type :module.directory
               :lang :js
               :search ["src-lang/kmi/lang"
                        "src-lang/xt/lang"
                        "src-build/kmi_repl"]
               :main 'kmi-repl.main
               :target ""
               :emit {:code {:link {:path-suffix ".js"
                                    :root-prefix "."}}
                      :lang/format :commonjs}}]})

(defn fix-same-dir-requires
  "the linker emits require(\"/file.js\") for same-directory imports;
   rewrite them to require(\"./file.js\") so Node resolves them correctly"
  [dir]
  (doseq [f (file-seq (io/file dir))
          :when (and (.isFile f)
                     (.endsWith (.getName f) ".js"))]
    (let [content (slurp f)
          fixed (clojure.string/replace content
                                        #"require\(\"\//"
                                        "require(\"./")]
      (when (not= content fixed)
        (spit f fixed)))))

(defn relativize-require
  "rewrites a root-relative require path so it resolves from the requiring file"
  [base-path file-dir require-path]
  (when (clojure.string/starts-with? require-path "./")
    (let [target-rel (subs require-path 2)
          target     (.resolve base-path target-rel)
          dir        (.resolve base-path file-dir)]
      (when (java.nio.file.Files/exists target (into-array java.nio.file.LinkOption []))
        (let [rel (str (.relativize dir target))]
          (if (clojure.string/starts-with? rel ".")
            rel
            (str "./" rel)))))))

(defn fix-relative-requires
  "module.directory emits root-relative requires; make them file-relative"
  [dir]
  (let [base      (io/file dir)
        base-path (.toPath base)]
    (doseq [f (file-seq base)
            :when (and (.isFile f)
                       (.endsWith (.getName f) ".js"))
            :let [rel      (str (.relativize base-path (.toPath f)))
                  file-dir (if-let [i (clojure.string/last-index-of rel "/")]
                             (subs rel 0 i)
                             "")]]
      (let [content (slurp f)
            fixed (clojure.string/replace content
                                          #"require\(\"([^\"]+)\"\)"
                                          (fn [[_ path]]
                                            (if-let [new (relativize-require base-path file-dir path)]
                                              (str "require(\"" new "\")")
                                              (str "require(\"" path "\")"))))]
        (when (not= content fixed)
          (spit f fixed))))))

(defn -main
  []
  (l/with:cache-none
    (make/build-all PROJECT))
  (fix-same-dir-requires ".build/kmi-repl")
  (fix-relative-requires ".build/kmi-repl"))
