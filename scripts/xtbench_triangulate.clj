(ns xtbench-triangulate
  "Pulls a GitHub Actions xtbench run (or reads an already-downloaded log dir),
   extracts per-language failing namespaces, and triangulates whether the root
   cause is in the shared xt.* source test or in a language-specific spec/rewrite."
  (:refer-clojure :exclude [run!])
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.string :as str])
  (:import [java.io File]
           [java.nio.file Files Paths]
           [java.util.zip ZipInputStream]))

(def ^:private +repo+ "zcaudate-xyz/foundation-base")
(def ^:private +log-api-template+ "repos/%s/actions/runs/%s/logs")

(def ^:private +group-order+
  {"xt.lang"      1
   "xt.event"     2
   "xt.substrate" 3
   "xt.db"        4})

(defn- die
  [fmt & args]
  (binding [*out* *err*]
    (apply printf fmt args)
    (println))
  (System/exit 1))

(defn- parse-run-id
  "Extracts a numeric run id from a full GitHub Actions URL or a bare id."
  [s]
  (or (some-> (re-find #"/runs/(\d+)" s) second)
      (when (re-matches #"\d+" s) s)))

(defn- parse-args
  [args]
  (loop [opts {} [x & xs] args]
    (cond
      (nil? x)
      opts

      (= x "--log-dir")
      (if xs
        (recur (assoc opts :log-dir (first xs)) (rest xs))
        (die "Missing value for --log-dir"))

      (= x "--repo")
      (if xs
        (recur (assoc opts :repo (first xs)) (rest xs))
        (die "Missing value for --repo"))

      (str/starts-with? x "--")
      (die "Unknown option: %s" x)

      :else
      (if-let [run-id (parse-run-id x)]
        (recur (assoc opts :run-id run-id) xs)
        (recur (assoc opts :log-dir x) xs)))))

(defn- temp-dir
  []
  (str (Files/createTempDirectory
        "xtbench-logs-"
        (into-array java.nio.file.attribute.FileAttribute []))))

(defn- unzip!
  [zip-path dest-dir]
  (let [dest (Paths/get dest-dir (into-array String []))]
    (with-open [in (ZipInputStream. (java.io.FileInputStream. zip-path))]
      (loop [entry (.getNextEntry in)]
        (when entry
          (let [out-path (.resolve dest (.getName entry))]
            (if (.isDirectory entry)
              (Files/createDirectories out-path (into-array java.nio.file.attribute.FileAttribute []))
              (do (Files/createDirectories (.getParent out-path) (into-array java.nio.file.attribute.FileAttribute []))
                  (Files/copy in out-path (into-array java.nio.file.StandardCopyOption [])))))
          (.closeEntry in)
          (recur (.getNextEntry in)))))))

(defn- download-logs!
  [run-id repo]
  (let [dest-dir (temp-dir)
        zip-path (str dest-dir "/logs.zip")
        endpoint (format +log-api-template+ repo run-id)
        _ (println "[xtbench] downloading logs for run" run-id "...")
        result (shell/sh "gh" "api" endpoint
                         "--method" "GET"
                         "-H" "Accept: application/vnd.github+json"
                         :out-enc :bytes)]
    (when (pos? (:exit result))
      (die "gh api failed: %s" (:err result)))
    (Files/write (Paths/get zip-path (into-array String []))
                 (:out result)
                 (into-array java.nio.file.OpenOption []))
    (unzip! zip-path dest-dir)
    (println "[xtbench] logs extracted to" dest-dir)
    dest-dir))

(defn- top-level-log-files
  [log-dir]
  (->> (.listFiles (File. log-dir))
       (filter #(and (.isFile %)
                     (re-matches #"\d+_.* _ [a-z]+\.txt" (.getName %))))
       (sort-by #(.getName %))))

(defn- parse-job-file
  "Returns {:group :lang} parsed from a filename like '0_04 - xt.db _ dart.txt'."
  [filename]
  (when-let [[_ _ group lang] (re-matches #"\d+_(\d+ - )?([^_]+) _ ([a-z]+)\.txt" filename)]
    {:group (str/trim group)
     :lang (keyword lang)}))

(defn- strip-timestamp
  [line]
  (str/replace line #"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+Z\s*" ""))

(defn- try-read-edn
  "Attempts to parse a string as EDN, returning nil on failure."
  [s]
  (try
    (edn/read-string s)
    (catch Exception _ nil)))

(defn- parse-failing-summary
  "Extracts the seedgen failing-functions EDN map from a log string."
  [log-text]
  (when-let [start-idx (str/index-of log-text "[seedgen] per-language failing functions:")]
    (let [after-header (subs log-text (+ start-idx
                                          (count "[seedgen] per-language failing functions:")))
          lines (->> (str/split-lines after-header)
                     (map strip-timestamp)
                     (vec))
          content-lines (drop-while #(not (str/starts-with? (str/triml %) "{")) lines)]
      (loop [acc nil
             [line & rest] content-lines]
        (if-not line
          acc
          (let [new-acc (if (seq acc)
                          (str acc "\n" line)
                          line)]
            (or (try-read-edn new-acc)
                (recur new-acc rest))))))))

(defn- xtbench-source-ns
  "Maps a failure symbol or xtbench namespace back to the original xt.* source namespace."
  [sym lang]
  (let [s (str sym)
        lang-prefix (str "xtbench." (name lang) ".")]
    (cond
      (str/starts-with? s lang-prefix)
      (symbol (str "xt." (subs s (count lang-prefix))))

      (str/includes? s "/")
      (symbol (namespace sym))

      :else
      (symbol s))))

(defn- ns->file
  [root ns-sym suffix]
  (let [path (-> (str ns-sym)
                 (str/replace "-" "_")
                 (str/replace "." "/"))]
    (str root "/" path suffix)))

(defn- source-test-file
  "Returns the test file path for a source namespace.
   Namespaces that already end in -test are test namespaces themselves."
  [ns-sym]
  (let [s (str ns-sym)]
    (if (str/ends-with? s "-test")
      (ns->file "test-lang" ns-sym ".clj")
      (ns->file "test-lang" ns-sym "_test.clj"))))

(defn- source-impl-file
  "Returns the implementation file path. For a test namespace, strip -test."
  [ns-sym]
  (let [s (str ns-sym)]
    (if (str/ends-with? s "-test")
      (ns->file "src-lang" (symbol (subs s 0 (- (count s) 5))) ".clj")
      (ns->file "src-lang" ns-sym ".clj"))))

(defn- lang-spec-files
  [lang]
  (let [base (str "src/hara/model/spec_" (name lang))]
    [(str base ".clj")
     (str base "/rewrite.clj")]))

(defn- collect-failures
  "Returns a seq of {:lang :type :source-ns :sym} for one job's failing map."
  [lang failing-map]
  (mapcat (fn [type]
            (let [syms (get failing-map type [])]
              (map (fn [sym]
                     {:lang lang
                      :type type
                      :source-ns (xtbench-source-ns sym lang)
                      :sym sym})
                   syms)))
          [:errored :failed :throw :timeout]))

(defn- summarize-failures
  "Aggregates a list of failure maps into {:langs #{...} :types #{...} :syms [...]}."
  [failures]
  {:langs (set (map :lang failures))
   :types (set (map :type failures))
   :syms (vec (distinct (map :sym failures)))})

(defn- build-source-index
  [job-results]
  (reduce (fn [idx {:keys [group lang failing-map]}]
            (reduce (fn [idx' failure]
                      (update-in idx'
                                 [group (:source-ns failure)]
                                 (fnil conj [])
                                 failure))
                    idx
                    (collect-failures lang failing-map)))
          {}
          job-results))

(defn- source-index-summaries
  "Returns a sorted seq of [source-ns summary] for a group index.
   Sorted by descending language count, then source namespace name."
  [ns-index]
  (->> ns-index
       (map (fn [[source-ns failures]]
              [source-ns (summarize-failures failures)]))
       (sort-by (fn [[source-ns summary]]
                  [(- (count (:langs summary))) (str source-ns)]))))

(defn- group-key
  [group-name]
  (or (+group-order+ group-name) 99))

(defn- print-report
  [run-id job-results source-index]
  (println "# xtbench Triangulation Report")
  (println)
  (println (str "Run: " (or run-id "local logs")))
  (println)

  (println "## Failures by Group / Language")
  (println)
  (println "| Group | Lang | Errored | Failed | Throw | Timeout |")
  (println "|-------|------|---------|--------|-------|---------|")
  (doseq [{:keys [group lang failing-map]}
          (sort-by (juxt #(group-key (:group %)) #(name (:lang %))) job-results)]
    (printf "| %s | %s | %d | %d | %d | %d |\n"
            group
            (name lang)
            (count (:errored failing-map))
            (count (:failed failing-map))
            (count (:throw failing-map))
            (count (:timeout failing-map))))
  (println)

  (doseq [[group ns-index] (sort-by #(group-key (first %)) source-index)]
    (println (str "## Group: " group))
    (println)

    (let [summaries (source-index-summaries ns-index)
          shared (filter #(>= (count (:langs (second %))) 2) summaries)
          unique (filter #(< (count (:langs (second %))) 2) summaries)]

      (when (seq shared)
        (println "### Shared source namespaces (likely original xt.* test issue)")
        (println)
        (doseq [[source-ns {:keys [langs types]}] shared]
          (println (str "- **" source-ns "**"))
          (println (str "  - Languages: " (str/join ", " (map name (sort langs)))))
          (println (str "  - Failure types: " (str/join ", " (map name (sort types)))))
          (println (str "  - Source test: `" (source-test-file source-ns) "`"))
          (println (str "  - Source impl: `" (source-impl-file source-ns) "`")))
        (println))

      (when (seq unique)
        (println "### Language-specific namespaces")
        (println)
        (doseq [[lang entries] (->> unique
                                    (map (fn [[source-ns summary]]
                                           [(first (:langs summary)) source-ns summary]))
                                    (group-by first)
                                    (sort-by #(name (first %))))]
          (println (str "#### " (name lang)))
          (println)
          (doseq [[_ source-ns {:keys [types]}] (sort-by #(str (second %)) entries)]
            (println (str "- **" source-ns "**"))
            (println (str "  - Failure types: " (str/join ", " (map name (sort types)))))
            (println (str "  - Source test: `" (source-test-file source-ns) "`"))
            (println (str "  - Source impl: `" (source-impl-file source-ns) "`"))
            (println (str "  - Inspect lang spec/rewrite:"))
            (doseq [f (lang-spec-files lang)]
              (println (str "    - `" f "`"))))
          (println))))
    (println "---")
    (println))

  (println "## How to investigate")
  (println)
  (println "- A namespace failing in **2+ languages** usually means the problem is in the original")
  (println "  `test-lang/xt/...` test or the `src-lang/xt/...` implementation it exercises.")
  (println "- A namespace failing in **only one language** usually means the problem is in that")
  (println "  language's spec/rewrite (`src/hara/model/spec_<lang>.clj` and")
  (println "  `src/hara/model/spec_<lang>/rewrite.clj`).")
  (println))

(defn- run!
  [{:keys [run-id log-dir repo]}]
  (let [repo (or repo +repo+)
        log-dir (or log-dir
                    (when run-id (download-logs! run-id repo))
                    (die "Provide a run id or --log-dir"))
        files (top-level-log-files log-dir)]
    (when (empty? files)
      (die "No top-level xtbench log files found in %s" log-dir))
    (let [job-results
          (keep (fn [file]
                  (when-let [{:keys [group lang]} (parse-job-file (.getName file))]
                    (let [text (slurp file)
                          failing-map (get (parse-failing-summary text) lang {})]
                      {:group group
                       :lang lang
                       :failing-map failing-map})))
                files)
          source-index (build-source-index job-results)]
      (print-report run-id job-results source-index))))

(defn- script-args
  "lein-exec passes the script path as the first element of *command-line-args*;
   strip it so the parser sees only user-supplied arguments."
  []
  (let [args *command-line-args*
        first-arg (first args)]
    (if (and first-arg
             (= (.getName (File. ^String first-arg))
                (.getName (File. ^String *file*))))
      (rest args)
      args)))

(when (bound? #'*command-line-args*)
  (run! (parse-args (script-args))))
