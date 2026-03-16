(ns rt.postgres.analyze.parse
  "Parses Clojure source files and extracts top-level pg forms.
   
   Uses clojure.core/read-string with custom readers to handle
   reader macros like #' (var), #{} (sets), and ^{} (metadata)."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

;;
;; Reader setup
;;

(defn safe-read-string
  "Reads a Clojure form from a string, handling unknown reader macros
   by wrapping them as tagged literals."
  [s]
  (binding [*read-eval* false]
    (read-string {:read-cond :allow
                  :features #{:clj}
                  :readers (fn [tag]
                             (fn [val] {:tagged-literal tag :value val}))}
                 s)))

(defn read-all-forms
  "Reads all top-level forms from a string of Clojure source code.
   Returns a vector of forms. Skips reader errors gracefully."
  [source]
  (let [rdr (java.io.PushbackReader. (java.io.StringReader. source))]
    (loop [forms []]
      (let [form (try
                   (binding [*read-eval* false]
                     (read {:read-cond :allow
                            :features #{:clj}
                            :eof ::eof
                            :readers (fn [_tag]
                                       (fn [val] val))}
                           rdr))
                   (catch Exception _e ::skip))]
        (cond
          (= form ::eof) forms
          (= form ::skip)
          (do
            ;; try to skip past the problematic form
            (try (.read rdr) (catch Exception _ nil))
            (recur forms))
          :else (recur (conj forms form)))))))

;;
;; Form classification
;;

(def pg-form-types
  "Set of top-level pg form symbols to recognize."
  #{'deftype.pg 'defn.pg 'defenum.pg 'defrun.pg 'defconst.pg})

(defn pg-form?
  "Returns true if the form is a pg DSL form (deftype.pg, defn.pg, etc.)."
  [form]
  (and (list? form)
       (symbol? (first form))
       (contains? pg-form-types (first form))))

(defn form-type
  "Returns the type keyword for a pg form, e.g. :deftype, :defn, :defenum."
  [form]
  (when (pg-form? form)
    (let [op (first form)]
      (cond
        (= op 'deftype.pg) :deftype
        (= op 'defn.pg)    :defn
        (= op 'defenum.pg) :defenum
        (= op 'defrun.pg)  :defrun
        (= op 'defconst.pg) :defconst
        :else nil))))

(defn ns-form?
  "Returns true if the form is an (ns ...) declaration."
  [form]
  (and (list? form) (= 'ns (first form))))

(defn script-form?
  "Returns true if the form is an (l/script ...) declaration."
  [form]
  (and (list? form)
       (symbol? (first form))
       (let [s (name (first form))]
         (= s "script"))))

;;
;; Metadata extraction
;;

(defn extract-metadata
  "Extracts metadata from a symbol, handling ^{...} reader macro forms.
   Returns the metadata map or nil."
  [sym]
  (when (instance? clojure.lang.IMeta sym)
    (meta sym)))

;;
;; Namespace extraction
;;

(defn extract-ns-info
  "Extracts namespace information from an (ns ...) form.
   Returns {:name symbol :requires [{:ns symbol :as symbol}...]}."
  [form]
  (when (ns-form? form)
    (let [ns-name (second form)
          requires (->> form
                        (filter #(and (list? %) (= :require (first %))))
                        first
                        rest
                        (mapv (fn [req]
                                (if (vector? req)
                                  (let [pairs (partition 2 (rest req))]
                                    {:ns (first req)
                                     :as (second (first (filter #(= :as (first %)) pairs)))})
                                  {:ns req}))))]
      {:name ns-name
       :requires requires})))

(defn extract-script-info
  "Extracts script configuration from an (l/script :postgres {...}) form.
   Returns the config map."
  [form]
  (when (script-form? form)
    (let [[_ lang config] form]
      {:lang lang
       :config config})))

;;
;; File parsing
;;

(defn parse-source
  "Parses a Clojure source string and returns a map of:
   - :ns       - namespace info
   - :script   - script config
   - :forms    - all top-level pg forms with their types
   - :raw      - all raw forms"
  [source]
  (let [forms (read-all-forms source)
        ns-form (first (filter ns-form? forms))
        script-forms (filter script-form? forms)
        pg-forms (filter pg-form? forms)]
    {:ns (when ns-form (extract-ns-info ns-form))
     :script (when (seq script-forms)
               (extract-script-info (first script-forms)))
     :forms (mapv (fn [f]
                    {:type (form-type f)
                     :form f})
                  pg-forms)
     :raw forms}))

(defn parse-file
  "Parses a Clojure source file and returns the same structure as parse-source."
  [path]
  (let [source (slurp (io/file path))]
    (assoc (parse-source source)
           :file path)))

(defn find-clj-files
  "Recursively finds all .clj files under the given directory path."
  [dir-path]
  (->> (file-seq (io/file dir-path))
       (filter #(and (.isFile %)
                     (str/ends-with? (.getName %) ".clj")))
       (mapv #(.getPath %))))

(defn parse-directory
  "Parses all .clj files under a directory. Returns a vector of parse results."
  [dir-path]
  (->> (find-clj-files dir-path)
       (mapv parse-file)))
