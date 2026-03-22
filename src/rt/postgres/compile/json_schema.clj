(ns rt.postgres.compile.json-schema
  (:require [rt.postgres.compile.common :as compile.common])
  "Canonical JSON Schema compile target.")

(def shape->json-schema compile.common/shape->jschema)
(def generate-json-schema compile.common/generate-jschema)

;; Compatibility aliases while older callers still use jschema naming.
(def shape->jschema shape->json-schema)
(def generate-jschema generate-json-schema)
