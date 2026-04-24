(ns std.lang.base.preprocess
  (:require [std.lib.foundation :as f]))

(def ^:dynamic *macro-form* nil)

(def ^:dynamic *macro-grammar* nil)

(def ^:dynamic *macro-opts* nil)

(def ^:dynamic *macro-splice* nil)

(def ^:dynamic *macro-skip-deps* nil)

(defn macro-form
  "gets the current macro form"
  {:added "4.0"}
  []
  *macro-form*)

(defn macro-opts
  "gets current macro-opts"
  {:added "4.0"}
  []
  *macro-opts*)

(defn macro-grammar
  "gets the current grammar"
  {:added "4.0"}
  []
  *macro-grammar*)

(defmacro ^{:style/indent 1}
  with:macro-opts
  "bind macro opts"
  {:added "4.0"}
  [[mopts] & body]
  `(binding [*macro-opts* ~mopts]
     ~@body))

(require '[std.lang.base.preprocess-input :as input]
         '[std.lang.base.preprocess-assign :as assign]
         '[std.lang.base.preprocess-staging :as staging])

(f/intern-in input/to-input-form
             input/to-input
             staging/get-fragment
             staging/value-template-args
             staging/value-standalone
             staging/process-namespaced-resolve
             staging/process-namespaced-symbol
             assign/process-inline-assignment
             assign/protect-reserved-head
             staging/to-staging-form
             staging/process-standard-symbol
             staging/to-staging
             staging/to-resolve
             staging/find-natives)
