(ns tahto.uberjar.main
  (:require [clojure.edn :as edn]
            [clojure.string :as str])
  (:gen-class))

(def language-namespaces
  {:xtalk  'tahto.model.spec-xtalk
   :bash   'tahto.model.spec-bash
   :c      'tahto.model.spec-c
   :dart   'tahto.model.spec-dart
   :glsl   'tahto.model.spec-glsl
   :js     'tahto.model.spec-js
   :lua    'tahto.model.spec-lua
   :elisp  'tahto.model.spec-elisp
   :scheme 'tahto.model.spec-scheme
   :python 'tahto.model.spec-python
   :sql    'tahto.model.spec-sql
   :oracle 'tahto.model.sql.spec-oracle})

(def usage
  (str "Usage:\n"
       "  java -jar tahto-standalone.jar emit <language> '<edn-forms>'\n"
       "  java -jar tahto-standalone.jar emit <language> -\n"
       "  java -jar tahto-standalone.jar languages\n\n"
       "Example:\n"
       "  java -jar tahto-standalone.jar emit js '[(+ 1 2 3)]'"))

(defn emit-source
  "emits an EDN collection of forms using a lazily loaded language spec"
  {:added "4.1"}
  [language source]
  (let [lang    (keyword (str/lower-case language))
        spec-ns (get language-namespaces lang)]
    (when-not spec-ns
      (throw (ex-info (str "Unsupported language: " language)
                      {:language language})))
    (let [forms (edn/read-string source)]
      (when-not (sequential? forms)
        (throw (ex-info "Input must be an EDN sequential collection of forms."
                        {:input forms})))
      (require spec-ns)
      ((requiring-resolve 'tahto.core.impl/emit-as) lang forms))))

(defn run
  "runs the Tahto command and returns an exit/out/err result map"
  {:added "4.1"}
  ([args]
   (run args (constantly "")))
  ([args read-stdin]
   (try
     (cond
       (or (empty? args)
           (= ["help"] args)
           (= ["--help"] args)
           (= ["-h"] args))
       {:exit 0 :out usage}

       (= ["languages"] args)
       {:exit 0
        :out (->> language-namespaces keys sort (map name) (str/join "\n"))}

       (and (= "emit" (first args))
            (= 3 (count args)))
       (let [[_ language source] args]
         {:exit 0
          :out (emit-source language (if (= "-" source)
                                      (read-stdin)
                                      source))})

       :else
       {:exit 2
        :err (str "Invalid arguments.\n\n" usage)})
     (catch Exception e
       {:exit 2
        :err (str "Error: " (.getMessage e))}))))

(defn -main
  "entry point for the standalone Tahto uberjar"
  {:added "4.1"}
  [& args]
  (let [{:keys [exit out err]} (run args #(slurp *in*))]
    (when out
      (println out))
    (when err
      (binding [*out* *err*]
        (println err)))
    (when-not (zero? exit)
      (System/exit exit))))
