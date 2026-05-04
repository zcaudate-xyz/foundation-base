(ns hara.common.emit-preprocess
  (:require [hara.common.preprocess-input :as input]
            [hara.common.preprocess-base :as base]
            [hara.common.preprocess-assign :as assign]
            [hara.common.preprocess-staging :as staging]
            [hara.common.preprocess-resolve :as resolve]
            [hara.common.preprocess-value :as value]
            [std.lib.foundation :as f]))

(f/intern-in hara.common.emit-preprocess
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
