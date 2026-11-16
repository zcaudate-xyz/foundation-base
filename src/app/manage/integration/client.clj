(ns app.manage.integration.client
  "Test-only Node PostgreSQL interop. Domain SQL and assertions remain in the DSL."
  (:require [lang.core :as l]))
(l/script :js
  {:import [["pg" :as [* postgres]]] :static {:export [open call close]}})

(defn.js ^{:- [:async]} open
  [config registry]
  (var client (new postgres.Client config))
  (await (. client (connect)))
  (return {"client" client "registry" registry}))

(defn.js ^{:- [:async]} call
  [handle name args]
  (var spec (. handle.registry [name]))
  (when (not spec)
    (throw (new Error (+ "Undeclared test RPC: " name))))
  (var identifier (new RegExp "^[A-Za-z_][A-Za-z0-9_]*$"))
  (var schema spec.schema)
  (var functionName (. spec.id (replaceAll "-" "_")))
  (when (or (not (. identifier (test schema))) (not (. identifier (test functionName))))
    (throw (new Error "Invalid RPC identifier")))
  (var inputs (or spec.input []))
  (when (or (not (Array.isArray args)) (not= args.length inputs.length))
    (throw (new Error "RPC argument count does not match the declaration")))
  (var parameters
       (. inputs
          (map (fn [input index]
                 (when (not (. (new RegExp "^[a-z][a-z0-9_]*$") (test input.type)))
                   (throw (new Error "Unsupported RPC argument type")))
                 (return (+ "$" (+ index 1) "::" input.type))))))
  (var values
       (. args
          (map (fn [value index]
                 (return (:? (and (== (. inputs [index] ["type"]) "jsonb")
                                  (not= value nil)
                                  (== (typeof value) "object"))
                             (JSON.stringify value)
                             value))))))
  (try (var
        result
        (await
         (.
          handle.client
          (query
           (+ "SELECT \"" schema "\".\"" functionName "\"(" (. parameters (join ",")) ") AS value")
           values))))
       (return (. result.rows [0] ["value"]))
       (catch error (when (. (new RegExp "^(22|23)|^P0001$") (test (or error.code "")))
                      (:= error.status 400))
         (throw error))))

(defn.js ^{:- [:async]} close [handle] (await (. handle.client (end))) (return true))
