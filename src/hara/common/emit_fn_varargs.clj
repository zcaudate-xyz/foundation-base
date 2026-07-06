(ns hara.common.emit-fn-varargs
  (:require [hara.common.emit-helper :as helper]
            [std.lib.foundation :as f]))

(defn- hook-namespaces
  [lang]
  [(symbol (str "hara.model.spec-" (name lang) "-varargs"))
   (symbol (str "hara.model.spec-" (name lang) ".rest"))])

(defn resolve-hook
  [mopts hook]
  (when-let [lang (:lang mopts)]
    (some (fn [ns-sym]
            (try
              (requiring-resolve (symbol (str ns-sym) (name hook)))
              (catch java.io.FileNotFoundException _
                nil)))
          (hook-namespaces lang))))

(defn emit-input
  [arg grammar mopts]
  (let [emit-rest (or (get-in grammar [:default :function :args :rest])
                      (resolve-hook mopts 'emit-input-rest))]
    (if emit-rest
      (emit-rest arg grammar mopts)
      (f/error "Rest argument emitter not configured"
               {:lang (:lang mopts)
                :symbol (:symbol arg)}))))

(defn prepare-body
  [args body grammar mopts]
  (if (some helper/rest-arg-symbol args)
    (if-let [prepare (resolve-hook mopts 'prepare-body)]
      (prepare args body grammar mopts)
      body)
    body))
