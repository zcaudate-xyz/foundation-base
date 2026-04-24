(ns std.lang.base.preprocess-base
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
