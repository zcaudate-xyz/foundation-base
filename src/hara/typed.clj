(ns hara.typed
  (:require [hara.typed.xtalk-analysis :as analysis]
            [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-parse :as parse]
            [std.lib.foundation :as f]))

(defn namespace-aliases
  [ns-obj]
  (into {}
        (map (fn [[alias target-ns]]
               [alias (ns-name target-ns)]))
        (ns-aliases ns-obj)))

(defn register-spec-form!
  ([sym type-form spec-meta]
   (register-spec-form! sym type-form spec-meta {}))
  ([sym type-form spec-meta aliases]
   (let [ns-sym (some-> sym namespace symbol)
         spec-sym (symbol (name sym))
         spec (parse/parse-spec-decl ns-sym spec-sym type-form spec-meta aliases
                                     {:file (or *file* "UNKNOWN")})]
     (types/register-spec! sym spec)
     spec)))

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
        aliases (namespace-aliases *ns*)
        spec-meta (cond-> (merge (meta spec-sym) attr-map)
                    docstring (assoc :docstring docstring))]
    `(do
       (hara.typed/register-spec-form!
         '~full-sym
         '~type-form
         '~spec-meta
         '~aliases)
        nil)))

(f/intern-in types/clear-registry!
             [register-type! types/register-spec!]
             types/get-type
             types/get-entry
             types/get-declaration
             types/get-spec
             types/get-function
             types/get-macro
             types/get-value
             types/list-specs
             types/list-entries
             types/list-functions
             types/list-macros
             types/list-values
             analysis/analyze-file
             analysis/analyze-file-raw
             analysis/analyze-namespace
             analysis/analyze-namespace-raw
             analysis/analyze-and-register!
             [check-function analysis/get-function-report]
             analysis/check-namespace)
