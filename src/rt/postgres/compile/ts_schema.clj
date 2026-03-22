(ns rt.postgres.compile.ts-schema
  (:require [rt.postgres.compile.common :as compile.common])
  "Canonical TypeScript schema compile target.")

(def shape->ts-interface compile.common/shape->ts-interface)
(def generate-ts-schema compile.common/generate-typescript)

;; Compatibility alias while older callers still use typescript naming.
(def generate-typescript generate-ts-schema)
