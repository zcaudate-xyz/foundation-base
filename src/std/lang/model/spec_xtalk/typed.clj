(ns std.lang.model.spec-xtalk.typed
  (:require [std.lang.model.spec-xtalk.typed-analysis :as analysis]
            [std.lang.model.spec-xtalk.typed-check :as check]
            [std.lang.model.spec-xtalk.typed-common :as types]
            [std.lang.model.spec-xtalk.typed-parse :as parse]))

(defn register-spec-form!
  [sym type-form spec-meta]
  (let [ns-sym (some-> sym namespace symbol)
        spec-sym (symbol (name sym))
        spec (parse/parse-spec-decl ns-sym spec-sym type-form spec-meta {})]
    (types/register-spec! sym spec)
    spec))

(defmacro defspec.xt
  [spec-sym & body]
  (let [[docstring body] (if (string? (first body))
                           [(first body) (rest body)]
                           [nil body])
        [attr-map body] (if (map? (first body))
                          [(first body) (rest body)]
                          [nil body])
        type-form (first body)
        full-sym (symbol (str (ns-name *ns*)) (name spec-sym))
        spec-meta (cond-> (merge (meta spec-sym) attr-map)
                    docstring (assoc :docstring docstring))]
    `(do
       (std.lang.model.spec-xtalk.typed/register-spec-form!
        '~full-sym
        '~type-form
        '~spec-meta)
       nil)))

(defn clear-registry!
  []
  (types/clear-registry!))

(defn register-type!
  [sym type-def]
  (types/register-spec! sym type-def))

(defn get-type
  [sym]
  (types/get-type sym))

(defn get-spec
  [sym]
  (types/get-spec sym))

(defn get-function
  [sym]
  (types/get-function sym))

(defn list-specs
  []
  (types/list-specs))

(defn list-functions
  []
  (types/list-functions))

(defn analyze-file
  [file-path]
  (analysis/analyze-file file-path))

(defn analyze-namespace
  [ns-sym]
  (analysis/analyze-namespace ns-sym))

(defn analyze-and-register!
  [ns-sym]
  (analysis/analyze-and-register! ns-sym))

(defn check-function
  [fn-ref]
  (or (analysis/get-function-report fn-ref)
      (check/check-function fn-ref)))

(defn check-namespace
  [ns-sym]
  (analysis/check-namespace ns-sym))
