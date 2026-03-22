(ns rt.postgres.compile.json-openapi
  (:require [rt.postgres.compile.common :as compile.common])
  "Canonical OpenAPI compile target.")

(def shape->openapi compile.common/shape->openapi)
(def fn->openapi compile.common/fn->openapi)
(def generate-openapi compile.common/generate-openapi)
