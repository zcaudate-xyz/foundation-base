(ns std.lang.typed.xtalk-analysis
  (:require [std.json :as json]
            [std.lang.typed.xtalk-check :as check]
            [std.lang.typed.xtalk-common :as types]
            [std.lang.typed.xtalk-parse :as parse]))

(defn analyze-file
  [file-path]
  (parse/analyze-file file-path))

(defn analyze-file-raw
  [file-path]
  (parse/analyze-file-raw file-path))

(defn analyze-namespace
  [ns-sym]
  (parse/analyze-namespace ns-sym))

(defn analyze-namespace-raw
  [ns-sym]
  (parse/analyze-namespace-raw ns-sym))

(defn analyze-and-register!
  [ns-sym]
  (-> ns-sym
      parse/analyze-namespace
      parse/register-types!))

(defn resolve-function-def
  [fn-ref]
  (cond
    (instance? std.lang.typed.xtalk_common.XtFnDef fn-ref)
    fn-ref

    (symbol? fn-ref)
    (or (types/get-function fn-ref)
        (when-let [ns-sym (some-> fn-ref namespace symbol)]
          (do (analyze-and-register! ns-sym)
              (types/get-function fn-ref))))

    :else
    nil))

(defn get-function-report
  [fn-ref]
  (some-> fn-ref
          resolve-function-def
          check/check-fn-def))

(defn get-function-input-type
  [fn-ref arg-sym]
  (some->> (resolve-function-def fn-ref)
           :inputs
           (some (fn [arg]
                   (when (= arg-sym (:name arg))
                     (:type arg))))
           types/type->data))

(defn get-function-output-type
  [fn-ref]
  (some-> (resolve-function-def fn-ref)
          :output
          types/type->data))

(defn check-namespace
  [ns-sym]
  (let [analysis (analyze-and-register! ns-sym)]
    {:namespace ns-sym
     :functions (mapv check/check-fn-def (:functions analysis))}))

(defn report-json
  ([report]
   (json/write report))
  ([report pretty?]
   (if pretty?
     (json/write-pp report)
     (json/write report))))
