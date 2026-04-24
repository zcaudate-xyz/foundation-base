(ns std.lang.base.emit-preprocess
  (:require [std.lang.base.preprocess-input :as input]
            [std.lang.base.preprocess-base :as base]
            [std.lang.base.preprocess-assign :as assign]
            [std.lang.base.preprocess-staging :as staging]
            [std.lib.foundation :as f]))

(f/intern-in std.lang.base.emit-preprocess
  base/macro-opts
  base/macro-grammar
  base/with:macro-opts
  input/to-input-form
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
