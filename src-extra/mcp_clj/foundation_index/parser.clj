(ns mcp-clj.foundation-index.parser
  "Clojure code parsing using clj-kondo analysis"
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clj-kondo.core :as kondo])
  (:import
    [java.io File]
    [java.security MessageDigest]))

;; ============================================================================
;; File Discovery
;; ============================================================================

(defn- clojure-file? [^File file]
  (let [name (.getName file)]
    (and (.isFile file)
         (or (str/ends-with? name ".clj")
             (str/ends-with? name ".cljc")
             (str/ends-with? name ".cljs"))
         (not (str/starts-with? name "."))
         (not (str/includes? name ".#")))))  ; Emacs temp files

(defn- should-skip-dir? [^File dir]
  (let [name (.getName dir)]
    (#{"target" ".git" "node_modules" ".clj-kondo" ".cpcache" "classes" "checkouts"}
     name)))

(defn find-clojure-files
  "Find all Clojure files under root path"
  [root-path & {:keys [exclude] :or {exclude []}}]
  (let [files (atom [])
        root (io/file root-path)]
    (when (.exists root)
      (letfn [(walk [^File f]
                (when (instance? File f)
                  (cond
                    (.isDirectory f)
                    (when-not (should-skip-dir? f)
                      (doseq [child (.listFiles f)]
                        (walk child)))
                    
                    (clojure-file? f)
                    (let [path (.getPath f)]
                      (when-not (some #(str/includes? path %) exclude)
                        (swap! files conj path))))))]
        (walk root)))
    (sort @files)))

;; ============================================================================
;; Hash Calculation
;; ============================================================================

(defn file-hash [file-path]
  (let [content (slurp file-path)
        digest (MessageDigest/getInstance "SHA-256")]
    (.update digest (.getBytes content "UTF-8"))
    (str/join "" (map #(format "%02x" %) (.digest digest)))))

;; ============================================================================
;; clj-kondo Analysis
;; ============================================================================

(defn- run-clj-kondo-analysis
  "Run clj-kondo analysis on a file"
  [file-path]
  (try
    (let [result (kondo/run! {:lint [file-path]
                              :config {:analysis {:arglists true
                                                 :locals false
                                                 :keywords false
                                                 :protocol-impls true
                                                 :var-definitions {:shallow false}
                                                 :var-usages {:shallow false}}}})]
      (:analysis result))
    (catch Exception e
      (println "clj-kondo analysis failed for" file-path ":" (.getMessage e))
      nil)))

(defn- kind-from-defined-by
  "Map clj-kondo defined-by to our kind categories.
   Note: clj-kondo returns symbols, not strings!"
  [defined-by]
  (let [db-str (str defined-by)]  ; Convert symbol to string
    (case db-str
      "clojure.core/defn" "function"
      "clojure.core/defn-" "function"
      "clojure.core/def" "var"
      "clojure.core/defmacro" "macro"
      "clojure.core/deftype" "type"
      "clojure.core/defrecord" "type"
      "clojure.core/defprotocol" "protocol"
      "clojure.core/defmulti" "multimethod"
      "clojure.core/defmethod" "method"
      "var")))  ; default

;; ============================================================================
;; Simple Fallback Parser (for when clj-kondo fails)
;; ============================================================================

(defn- extract-ns-from-content
  "Extract namespace declaration from file content"
  [content]
  (or (second (re-find #"\(ns\s+([a-zA-Z0-9._-]+)" content))
      "unknown"))

(defn- parse-arglists
  "Extract arglist vectors from defn form"
  [content]
  (when-let [match (re-find #"\(defn\.?-?\s+(?:\^\{[^}]+\}\s+)?[a-zA-Z0-9*-]+\s+(?:\"[^\"]*\"\s+)?(?:\{[^}]+\}\s+)?(\[.*?\])" content)]
    (let [args-str (second match)]
      (try
        [(read-string args-str)]
        (catch Exception _
          [args-str])))))

(defn- extract-docstring
  "Extract docstring from content near line"
  [content]
  (when-let [match (re-find #"\"([^\"]{10,500})\"" content)]
    (second match)))

(defn- simple-parse-file
  "Fallback parser using regex for when clj-kondo is unavailable"
  [file-path]
  (let [content (slurp file-path)
        ns-name (extract-ns-from-content content)
        lines (str/split-lines content)
        symbols (atom [])]
    
    (doseq [[line-num line] (map-indexed vector lines)]
      (let [line-num (inc line-num)]
        ;; defn / defn-
        (when-let [match (re-find #"\(defn\.?-?\s+(?:\^\{[^}]*\}\s*)?([a-zA-Z0-9*-]+)" line)]
          (let [name (second match)
                is-private (or (str/includes? line "defn-")
                              (re-find #":private\s+true" line))]
            (swap! symbols conj
              {:name name
               :kind "function"
               :namespace ns-name
               :qualified-name (str ns-name "/" name)
               :file file-path
               :line line-num
               :private is-private
               :docstring (extract-docstring line)
               :arglists (parse-arglists line)})))
        
        ;; def (vars)
        (when-let [match (re-find #"\(def\s+(?!n\s|macro\s|multi\s|method\s|record\s|type\s)([a-zA-Z0-9*-]+)" line)]
          (let [name (second match)]
            (swap! symbols conj
              {:name name
               :kind "var"
               :namespace ns-name
               :qualified-name (str ns-name "/" name)
               :file file-path
               :line line-num})))
        
        ;; defmacro
        (when-let [match (re-find #"\(defmacro\s+([a-zA-Z0-9*-]+)" line)]
          (let [name (second match)]
            (swap! symbols conj
              {:name name
               :kind "macro"
               :namespace ns-name
               :qualified-name (str ns-name "/" name)
               :file file-path
               :line line-num})))
        
        ;; deftype / defrecord
        (when-let [match (re-find #"\(def(?:type|record)\.?\s+(?:\^\{[^}]*\}\s*)?([a-zA-Z0-9]+)" line)]
          (let [name (second match)]
            (swap! symbols conj
              {:name name
               :kind "type"
               :namespace ns-name
               :qualified-name (str ns-name "/" name)
               :file file-path
               :line line-num})))
        
        ;; defprotocol
        (when-let [match (re-find #"\(defprotocol\s+([a-zA-Z0-9]+)" line)]
          (let [name (second match)]
            (swap! symbols conj
              {:name name
               :kind "protocol"
               :namespace ns-name
               :qualified-name (str ns-name "/" name)
               :file file-path
               :line line-num})))
        
        ;; defmulti
        (when-let [match (re-find #"\(defmulti\s+([a-zA-Z0-9*-]+)" line)]
          (let [name (second match)]
            (swap! symbols conj
              {:name name
               :kind "multimethod"
               :namespace ns-name
               :qualified-name (str ns-name "/" name)
               :file file-path
               :line line-num})))))
    
    {:namespace ns-name
     :file file-path
     :hash (file-hash file-path)
     :symbols @symbols
     :symbol-count (count @symbols)}))

;; ============================================================================
;; Main Parser Interface
;; ============================================================================

(defn parse-file
  "Parse a Clojure file and extract symbols using clj-kondo"
  [file-path]
  (try
    (if-let [analysis (run-clj-kondo-analysis file-path)]
      ;; Use clj-kondo results
      (let [var-defs (:var-definitions analysis)
            ns-defs (:namespace-definitions analysis)
            ns-name (or (:name (first ns-defs))
                       (extract-ns-from-content (slurp file-path)))
            symbols (mapv (fn [v]
                           {:name (:name v)
                            :kind (kind-from-defined-by (:defined-by v))
                            :namespace (or (:ns v) ns-name)
                            :qualified-name (str (or (:ns v) ns-name) "/" (:name v))
                            :file (:filename v)
                            :line (:row v)
                            :column (:col v)
                            :end-line (:end-row v)
                            :end-column (:end-col v)
                            :private (:private v)
                            :docstring (:doc v)
                            :arglists (:arglist-strs v)
                            :deprecated (:deprecated v)
                            :meta (when-let [m (:meta v)] (str m))})
                         var-defs)]
        {:namespace ns-name
         :file file-path
         :hash (file-hash file-path)
         :symbols symbols
         :symbol-count (count symbols)})
      ;; Fallback to simple parser
      (simple-parse-file file-path))
    (catch Exception e
      (println "Error parsing" file-path ":" (.getMessage e))
      {:namespace "error"
       :file file-path
       :hash ""
       :symbols []
       :symbol-count 0
       :error (.getMessage e)})))

(defn parse-files
  "Parse multiple files, returns lazy sequence"
  [file-paths & {:keys [progress-fn]}]
  (let [total (count file-paths)]
    (map-indexed
      (fn [idx file-path]
        (when progress-fn
          (progress-fn (inc idx) total file-path))
        (parse-file file-path))
      file-paths)))
