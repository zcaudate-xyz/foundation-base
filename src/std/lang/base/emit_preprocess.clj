(ns std.lang.base.emit-preprocess
  (:require [std.lang.base.preprocess-input :as input]
            [std.lang.base.preprocess-base :as base]
            [std.lang.base.preprocess-assign :as assign]
            [std.lang.base.preprocess-staging :as staging]
            [std.lang.base.preprocess-resolve :as resolve]
            [std.lang.base.preprocess-value :as value]
            [std.lib.foundation :as f]))

(f/intern-in std.lang.base.emit-preprocess
  base/macro-opts
  base/macro-grammar
  base/with:macro-opts
  input/to-input-form
  input/to-input
  value/value-template-args
  value/value-standalone
  resolve/process-namespaced-resolve
  resolve/process-namespaced-symbol
  resolve/get-fragment
  resolve/process-standard-symbol
  resolve/find-natives
  
  assign/process-inline-assignment
  assign/protect-reserved-head
  staging/to-staging-form
  staging/to-staging
  staging/to-resolve)
