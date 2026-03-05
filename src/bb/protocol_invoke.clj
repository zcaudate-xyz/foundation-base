(ns bb.protocol-invoke)

(defmulti -invoke-intern
  "extendable function for loading invoke form constructors"
  {:added "3.0"}
  (fn [label & _] label))

(defmulti -invoke-package
  "extendable function for loading invoke-intern types"
  {:added "3.0"}
  identity)

(defmulti -fn-body
  "multimethod for defining anonymous function body"
  {:added "3.0"}
  (fn [label body] label))

(defmulti -fn-package
  "extendable function for loading fn-body types"
  {:added "3.0"}
  identity)