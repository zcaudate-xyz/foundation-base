(ns lang.base.emit-preprocess
  (:require [lang.base.preprocess-input :as input]
            [lang.base.preprocess-base :as base]
            [lang.base.preprocess-assign :as assign]
            [lang.base.preprocess-staging :as staging]
            [lang.base.preprocess-resolve :as resolve]
            [lang.base.preprocess-value :as value]
            [std.lib.foundation :as f]))

(f/intern-in lang.base.emit-preprocess
  base/macro-opts
  base/macro-grammar
  base/with:macro-opts
  input/to-input-form
  input/to-input
  input/eval-template-forms
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
