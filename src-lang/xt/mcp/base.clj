(ns xt.mcp.base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(def.xt PROTOCOL_VERSION "2025-11-25")

(defn.xt schema-wire
  "converts the portable snake_case schema keys used by xt.mcp to MCP wire keys"
  {:added "4.1"}
  [schema]
  (when (xt/x:nil? schema)
    (return nil))
  (var out {})
  (xt/for:object [[k v] schema]
    (cond (== k "properties")
          (do (var properties {})
              (xt/for:object [[property-name property-schema] v]
                (xt/x:set-key properties property-name (-/schema-wire property-schema)))
              (xt/x:set-key out "properties" properties))

          (== k "items")
          (xt/x:set-key out "items" (-/schema-wire v))

          (== k "additional_properties")
          (xt/x:set-key out "additionalProperties" v)

          :else
          (xt/x:set-key out k v)))
  (return out))

(defn.xt annotations-wire
  "converts portable annotation keys to MCP annotation keys"
  {:added "4.1"}
  [annotations]
  (when (xt/x:nil? annotations)
    (return nil))
  (var out {})
  (xt/for:object [[k v] annotations]
    (cond (== k "read_only_hint")
          (xt/x:set-key out "readOnlyHint" v)

          (== k "destructive_hint")
          (xt/x:set-key out "destructiveHint" v)

          (== k "idempotent_hint")
          (xt/x:set-key out "idempotentHint" v)

          (== k "open_world_hint")
          (xt/x:set-key out "openWorldHint" v)

          :else
          (xt/x:set-key out k v)))
  (return out))

(defn.xt tool-valid?
  "checks the required portable MCP tool fields"
  {:added "4.1"}
  [tool]
  (return
   (and (xt/x:is-object? tool)
        (xt/x:is-string? (. tool ["name"]))
        (< 0 (xt/x:len (. tool ["name"])))
        (xt/x:is-string? (. tool ["description"]))
        (xt/x:is-object? (. tool ["input_schema"])))))

(defn.xt tool-wire
  "projects a portable MCP tool descriptor onto the MCP wire shape"
  {:added "4.1"}
  [tool]
  (when (not (-/tool-valid? tool))
    (xt/x:err "invalid MCP tool descriptor"))
  (var out {:name (. tool ["name"])
            :description (. tool ["description"])
            :inputSchema (-/schema-wire (. tool ["input_schema"]))})
  (when (xt/x:not-nil? (. tool ["title"]))
    (xt/x:set-key out "title" (. tool ["title"])))
  (when (xt/x:not-nil? (. tool ["output_schema"]))
    (xt/x:set-key out "outputSchema"
                  (-/schema-wire (. tool ["output_schema"]))))
  (when (xt/x:not-nil? (. tool ["annotations"]))
    (xt/x:set-key out "annotations"
                  (-/annotations-wire (. tool ["annotations"]))))
  (return out))

(defn.xt schema-type-valid?
  "checks a value against the portable JSON Schema type subset"
  {:added "4.1"}
  [type value]
  (cond (xt/x:nil? type) (return true)
        (== type "object") (return (xt/x:is-object? value))
        (== type "array") (return (xt/x:is-array? value))
        (== type "string") (return (xt/x:is-string? value))
        (== type "number") (return (xt/x:is-number? value))
        (== type "integer") (return (xt/x:is-integer? value))
        (== type "boolean") (return (xt/x:is-boolean? value))
        (== type "null") (return (xt/x:nil? value))
        :else (return false)))

(defn.xt schema-error
  "returns nil for valid input or an actionable validation error string"
  {:added "4.1"}
  [schema value path]
  (:= path (or path "$"))
  (when (not (-/schema-type-valid? (. schema ["type"]) value))
    (return (xt/x:cat path " must be " (. schema ["type"]))))
  (var enum-values (. schema ["enum"]))
  (when (and (xt/x:is-array? enum-values)
             (not (xtd/arr-some enum-values
                                (fn [candidate]
                                  (return (== candidate value))))))
    (return (xt/x:cat path " must be one of the declared enum values")))
  (when (and (== "object" (. schema ["type"]))
             (xt/x:is-object? value))
    (var properties (or (. schema ["properties"]) {}))
    (var required (or (. schema ["required"]) []))
    (var required-error nil)
    (xt/for:array [required-key required]
      (when (and (xt/x:nil? required-error)
                 (not (xt/x:has-key? value required-key)))
        (:= required-error (xt/x:cat path "." required-key " is required"))))
    (when (xt/x:not-nil? required-error)
      (return required-error))
    (var property-error nil)
    (xt/for:object [[property-name property-value] value]
      (when (xt/x:nil? property-error)
        (var property-schema (xt/x:get-key properties property-name))
        (cond (xt/x:not-nil? property-schema)
              (:= property-error
                  (-/schema-error property-schema
                                  property-value
                                  (xt/x:cat path "." property-name)))

              (== false (. schema ["additional_properties"]))
              (:= property-error
                  (xt/x:cat path "." property-name " is not allowed")))))
    (when (xt/x:not-nil? property-error)
      (return property-error)))
  (when (and (== "array" (. schema ["type"]))
             (xt/x:is-array? value)
             (xt/x:not-nil? (. schema ["items"])))
    (var item-error nil)
    (var index 0)
    (xt/for:array [item value]
      (when (xt/x:nil? item-error)
        (:= item-error
            (-/schema-error (. schema ["items"])
                            item
                            (xt/x:cat path "[" index "]"))))
      (:= index (+ index 1)))
    (when (xt/x:not-nil? item-error)
      (return item-error)))
  (return nil))

(defn.xt response
  "constructs a JSON-RPC success response"
  {:added "4.1"}
  [id result]
  (return {:jsonrpc "2.0" :id id :result result}))

(defn.xt error-response
  "constructs a JSON-RPC error response"
  {:added "4.1"}
  [id code message data]
  (var error {:code code :message message})
  (when (xt/x:not-nil? data)
    (xt/x:set-key error "data" data))
  (return {:jsonrpc "2.0" :id id :error error}))

(defn.xt tool-result
  "normalizes a handler value into an MCP tool result"
  {:added "4.1"}
  [value]
  (var text nil)
  (try
    (:= text (xt/x:json-encode value))
    (catch err
      (:= text (xt/x:to-string value))))
  (var result {:content [{:type "text" :text text}]
               :isError false})
  (when (xt/x:is-object? value)
    (xt/x:set-key result "structuredContent" value))
  (return result))

(defn.xt tool-error-result
  "constructs an MCP tool execution error"
  {:added "4.1"}
  [error]
  (var message (or (xt/x:ex-message error)
                   (xt/x:to-string error)
                   "Tool execution failed"))
  (return {:content [{:type "text" :text message}]
           :isError true}))
