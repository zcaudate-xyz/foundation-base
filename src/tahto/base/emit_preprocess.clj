(ns tahto.base.emit-preprocess
  (:require [tahto.base.preprocess-input :as input]
            [tahto.base.preprocess-base :as base]
            [tahto.base.preprocess-assign :as assign]
            [tahto.base.preprocess-staging :as staging]
            [tahto.base.preprocess-resolve :as resolve]
            [tahto.base.preprocess-value :as value]
            [std.lib.foundation :as f]))

(f/intern-in tahto.base.emit-preprocess
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
