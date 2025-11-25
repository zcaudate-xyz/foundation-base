(ns code.test.base.context
  (:require [std.lib :as h]))

(defrecord Context
  [eval-fact
   eval-mode
   eval-replace
   eval-meta
   eval-global
   eval-check
   eval-current-ns
   run-id
   registry
   accumulator
   errors
   settings
   root
   results])

(defonce ^:dynamic *context*
  (map->Context {:eval-fact false
                 :eval-mode true
                 :eval-replace nil
                 :eval-meta nil
                 :eval-global nil
                 :eval-check nil
                 :eval-current-ns nil
                 :run-id true
                 :registry (atom {})
                 :accumulator (atom nil)
                 :errors nil
                 :settings {:test-paths ["test"]}
                 :root "."
                 :results nil}))

(defmacro with-context
  "Runs code within a temporary context."
  {:added "4.0"}
  [m & body]
  `(binding [*context* (-> (h/merge-nested *context* ~m)
                           (map->Context))]
     ~@body))
