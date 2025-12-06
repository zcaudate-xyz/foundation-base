(ns std.lang.base.library-load
  (:require [std.lang.base.library :as lib]
            [std.lang.base.impl :as impl]
            [std.lang.base.script :as script]
            [std.lang :as l]
            [clojure.core :as core]
            [std.lib :as h]
            [clojure.tools.reader :as reader]
            [clojure.tools.reader.reader-types :as readers]))

(defn eval-in-library
  "Evaluates a form within the context of a specific library instance.
   Handles `l/script` specially to register modules in the provided library.
   Injects module metadata for other forms to ensure correct registration without relying on global runtime state."
  [form lib-instance ns-sym]
  (if (and (seq? form) (= 'l/script (first form)))
    (let [[_ lang config] form
           config (if (map? config) config {})
           module-id ns-sym]
      ;; l/script expands into script-fn, which calls script-fn-base.
      ;; script-fn-base sets up the module and runtime config in the library.
      (script/script-fn-base lang module-id config lib-instance))

    ;; For other forms (defn.js, etc.)
    (if (and (seq? form) (symbol? (first form))
             (or (.endsWith (name (first form)) ".js")
                 (.endsWith (name (first form)) ".lua")
                 (.endsWith (name (first form)) ".python")
                 ;; Add other extensions as needed
                 ))
      (let [module-id ns-sym
            ;; Inject module into metadata of the form
            form-with-meta (vary-meta form assoc :module module-id)]
        (impl/with:library [lib-instance]
          (binding [*ns* (the-ns ns-sym)]
            (eval form-with-meta))))

      ;; Fallback for other forms
      (impl/with:library [lib-instance]
        (binding [*ns* (the-ns ns-sym)]
          (eval form))))))

(defn load-string-into-library
  "Loads code from a string into a specific library instance.
   Tracks namespace changes via `ns` forms.

   Note: This function performs standard entry hydration (via `create-code-hydrate`)
   which resolves links and adds metadata as defined by the language grammar.
   However, it does NOT:
   1. Recursively load required files (file-level hydration).
   2. Initialize language runtimes (runtime hydration).
   3. Execute side effects outside of the library structure."
  [content lib-instance initial-ns-sym]
  ;; Create namespace if it doesn't exist
  (when-not (find-ns initial-ns-sym)
    (create-ns initial-ns-sym))

  (binding [*ns* *ns*] ;; Bind *ns* so it can be set! by `ns` forms or `in-ns`
    (let [reader (readers/push-back-reader (java.io.StringReader. content))
          eof    (Object.)]
      (loop [current-ns-sym initial-ns-sym]
        (let [form (reader/read reader false eof)]
          (when (not= form eof)
            (let [next-ns-sym (if (and (seq? form) (= 'ns (first form)))
                                  (second form)
                                  current-ns-sym)]
              (if (= 'ns (first form))
                ;; Evaluate ns form to ensure requires are processed
                (eval form)
                ;; Evaluate other forms in the library context
                (eval-in-library form lib-instance current-ns-sym))

              (recur next-ns-sym))))))))

(defn load-file-into-library
  "Loads a file into a specific library instance."
  [filepath lib-instance]
  (let [content (slurp filepath)]
    (load-string-into-library content lib-instance (h/ns-sym))))

(defn clone-and-load
  "Clones the default library and loads the given file into it.
   Useful for comparing versions."
  [filepath]
  (let [lib (impl/clone-default-library)]
    (load-file-into-library filepath lib)
    lib))

(defn analyze-string
  "Analyzes code string to find namespace and dependencies.
   Returns a map with :ns, :requires (Clojure), and :std-requires (std.lang)."
  [content]
  (let [reader (readers/push-back-reader (java.io.StringReader. content))
        eof    (Object.)
        info   (atom {:ns nil :requires #{} :std-requires #{}})]
    (loop []
      (let [form (try (reader/read reader false eof)
                      (catch Exception e nil))]
        (when (and form (not= form eof))
          (cond
            ;; Handle (ns ...)
            (and (seq? form) (= 'ns (first form)))
            (let [ns-name (second form)
                  deps    (->> (rest form)
                               (filter #(and (seq? %) (= :require (first %))))
                               (mapcat rest)
                               (map #(if (vector? %) (first %) %))
                               set)]
              (swap! info assoc :ns ns-name)
              (swap! info update :requires into deps))

            ;; Handle (l/script ...) or (std.lang/script ...)
            (and (seq? form) (or (= 'l/script (first form))
                                 (= 'std.lang/script (first form))))
            (let [[_ _ config] form
                  script-deps (if (map? config)
                                (->> (:require config)
                                     (map #(if (vector? %) (first %) %))
                                     set)
                                #{})]
              (swap! info update :std-requires into script-deps)))
          (recur))))
    @info))

(defn analyze-file
  "Analyzes a file to find namespace and dependencies."
  [filepath]
  (assoc (analyze-string (slurp filepath))
         :file filepath))

(defn create-dependency-graph
  "Creates a dependency graph from a list of files.
   Returns a map where keys are namespaces and values are sets of dependencies."
  [files]
  (reduce (fn [graph file]
            (let [{:keys [ns requires std-requires]} (analyze-file file)
                  all-deps (into requires std-requires)]
              (if ns
                (assoc graph ns all-deps)
                graph)))
          {}
          files))
