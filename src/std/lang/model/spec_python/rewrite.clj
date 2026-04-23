(ns std.lang.model.spec-python.rewrite
  (:require [std.lang.rewrite.hoist :as hoist]))

(def ^:private +python-rewriter+
  (hoist/create-rewriter
   {:fn-tags #{'fn 'fn.inner}
    :symbol-prefix "py_callback__"}))

(def python-rewrite-expression
  (:rewrite-expression +python-rewriter+))

(def python-rewrite-statement
  (:rewrite-statement +python-rewriter+))

(def python-rewrite-statements
  (:rewrite-statements +python-rewriter+))

(def python-rewrite-stage
  (:rewrite-stage +python-rewriter+))
