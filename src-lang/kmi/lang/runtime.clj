(ns kmi.lang.runtime
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [eval read read-string load]))

(l/script :xtalk
  {:require [[kmi.lang.parser :as parser]
             [kmi.lang.runtime.env :as env]
             [kmi.lang.runtime.primitive :as prim]
             [kmi.lang.runtime.eval :as eval]
             [xt.lang.spec-base :as xt]]})

;;
;; PUBLIC RUNTIME API
;;

(defn.xt empty-runtime
  "returns an empty runtime seeded with primitives"
  {:added "4.1"}
  []
  (return (prim/init-runtime (env/runtime-create))))

(defn.xt read-string
  "reads a single form from a string"
  {:added "4.1"}
  [source]
  (return (parser/read-string source)))

(defn.xt read-many
  "reads all forms from a string"
  {:added "4.1"}
  [source]
  (return (eval/read-many source)))

(defn.xt eval-form
  "evaluates a single form in a fresh environment"
  {:added "4.1"}
  [runtime form]
  (return (eval/eval-form runtime (env/empty-env) form)))

(defn.xt eval-string
  "evaluates a string expression and returns a result map"
  {:added "4.1"}
  [runtime source]
  (return (eval/eval-string runtime source)))

(defn.xt eval-string-many
  "evaluates all forms in a string"
  {:added "4.1"}
  [runtime source]
  (return (eval/eval-string-many runtime source)))
