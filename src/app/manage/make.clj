(ns app.manage.make
  "std.make adapters that return artifacts to app.manage instead of writing files."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [std.make.compile :as compile]
            [lang.core.compile]
            [lang.core :as l]))

(defn- receipts [result]
  (cond
    (and (vector? result) (= 2 (count result)) (every? string? result)) [result]
    (and (map? result) (:written result)) (receipts (:written result))
    (and (map? result) (zero? (:files result -1))) []
    (sequential? result) (mapcat receipts result)
    :else (throw (ex-info "Compiler did not return text artifacts in mock mode"
                          {:receipt-type (str (type result))}))))

(defn render-sections
  "Runs std.make sections in mock mode. Module compilers retain their normal emit options.
   Directory/resource copying uses different receipt semantics and is deliberately rejected."
  [{:keys [root]} {:keys [load sections]}]
  (doseq [module load] (require module))
  (reduce
   (fn [artifacts section]
     (when (#{:directory :resource} (:type section))
       (throw (ex-info "Copy sections need an explicit asset renderer" {:section section})))
     (let [section (cond-> section
                     (:search section) (update :search #(mapv (fn [path] (str (io/file root path))) %)))
           result (binding [compile/*mock-compile* true]
                    (compile/compile-single (str root) {} section))]
       (reduce (fn [out [path content]]
                 (let [absolute (.toPath (.getAbsoluteFile (io/file path)))
                       relative (str/replace (str (.relativize (.toPath root) absolute)) "\\" "/")]
                   (when (contains? out relative)
                     (throw (ex-info "Multiple sections emit the same path" {:path relative})))
                   (assoc out relative content)))
               artifacts (receipts result))))
   (sorted-map) sections))

(defn target
  "Constructs a management target from declarative std.make sections and :load namespaces."
  [{:keys [depends sources outputs] :as config}]
  {:depends (vec depends) :sources (vec sources) :outputs (vec outputs)
   :render (fn [context] (render-sections context config))})

(defn purge!
  "Purges XTalk registrations and authored Clojure mappings before namespace reload.
   Uses public namespace APIs, without JVM-private class-cache reflection."
  [namespace]
  (when (find-ns namespace)
    (when (find-ns 'lang.core)
      (l/rt:module-purge (l/rt:list namespace)))
    (doseq [alias (keys (ns-aliases namespace))] (ns-unalias namespace alias))
    (doseq [name (concat (keys (ns-interns namespace)) (keys (ns-refers namespace)))]
      (ns-unmap namespace name))))
