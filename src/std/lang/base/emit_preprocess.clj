(ns std.lang.base.emit-preprocess
  (:require [std.lang.base.preprocess :as preprocess]
            [std.lib.foundation :as f]))

(f/intern-in preprocess/macro-form
             preprocess/macro-opts
             preprocess/macro-grammar
             preprocess/with:macro-opts
             preprocess/to-input-form
             preprocess/to-input
             preprocess/get-fragment
             preprocess/value-template-args
             preprocess/value-standalone
             preprocess/process-namespaced-resolve
             preprocess/process-namespaced-symbol
             preprocess/process-inline-assignment
             preprocess/protect-reserved-head
             preprocess/to-staging-form
             preprocess/process-standard-symbol
             preprocess/to-staging
             preprocess/to-resolve
             preprocess/find-natives)
