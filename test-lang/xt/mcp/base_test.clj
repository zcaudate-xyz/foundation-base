(ns xt.mcp.base-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.mcp.base :as base]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.mcp.base :as base]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.mcp.base :as base]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.mcp.base :as base]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.mcp.base/schema-wire :added "4.1"}
(fact "camel-cases MCP schema vocabulary without changing property names"

  (!.js
    (base/schema-wire
     {"type" "object"
      "properties" {"snake_case" {"type" "string"}}
      "additional_properties" false}))
  => {"type" "object"
      "properties" {"snake_case" {"type" "string"}}
      "additionalProperties" false}

  (!.lua
    (base/schema-wire
     {"type" "object"
      "properties" {"snake_case" {"type" "string"}}
      "additional_properties" false}))
  => {"type" "object"
      "properties" {"snake_case" {"type" "string"}}
      "additionalProperties" false}

  (!.py
    (base/schema-wire
     {"type" "object"
      "properties" {"snake_case" {"type" "string"}}
      "additional_properties" false}))
  => {"type" "object"
      "properties" {"snake_case" {"type" "string"}}
      "additionalProperties" false}

  (!.dt
    (base/schema-wire
     {"type" "object"
      "properties" {"snake_case" {"type" "string"}}
      "additional_properties" false}))
  => {"type" "object"
      "properties" {"snake_case" {"type" "string"}}
      "additionalProperties" false})

^{:refer xt.mcp.base/tool-wire :added "4.1"}
(fact "projects portable snake_case descriptors onto the MCP wire shape"

  (!.js
    (base/tool-wire
     {"name" "sample_echo"
      "description" "Echoes a value."
      "input_schema" {"type" "object" "properties" {}}
      "annotations" {"read_only_hint" true}}))
  => {"name" "sample_echo"
      "description" "Echoes a value."
      "inputSchema" {"type" "object" "properties" {}}
      "annotations" {"readOnlyHint" true}}

  (!.lua
    (base/tool-wire
     {"name" "sample_echo"
      "description" "Echoes a value."
      "input_schema" {"type" "object" "properties" {}}
      "annotations" {"read_only_hint" true}}))
  => {"name" "sample_echo"
      "description" "Echoes a value."
      "inputSchema" {"type" "object" "properties" {}}
      "annotations" {"readOnlyHint" true}}

  (!.py
    (base/tool-wire
     {"name" "sample_echo"
      "description" "Echoes a value."
      "input_schema" {"type" "object" "properties" {}}
      "annotations" {"read_only_hint" true}}))
  => {"name" "sample_echo"
      "description" "Echoes a value."
      "inputSchema" {"type" "object" "properties" {}}
      "annotations" {"readOnlyHint" true}}

  (!.dt
    (base/tool-wire
     {"name" "sample_echo"
      "description" "Echoes a value."
      "input_schema" {"type" "object" "properties" {}}
      "annotations" {"read_only_hint" true}}))
  => {"name" "sample_echo"
      "description" "Echoes a value."
      "inputSchema" {"type" "object" "properties" {}}
      "annotations" {"readOnlyHint" true}})

^{:refer xt.mcp.base/schema-error :added "4.1"}
(fact "validates required fields, types, and additional properties"

  (!.js
    [(base/schema-error
      {"type" "object"
       "properties" {"snake_case" {"type" "string"}}
       "required" ["snake_case"]
       "additional_properties" false}
      {"snake_case" "ok"} "$" )
     (base/schema-error
      {"type" "object"
       "properties" {"snake_case" {"type" "string"}}
       "required" ["snake_case"]}
      {} "$" )])
  => [nil "$.snake_case is required"]

  (!.lua
    [(base/schema-error
      {"type" "object"
       "properties" {"snake_case" {"type" "string"}}
       "required" ["snake_case"]
       "additional_properties" false}
      {"snake_case" "ok"} "$" )
     (base/schema-error
      {"type" "object"
       "properties" {"snake_case" {"type" "string"}}
       "required" ["snake_case"]}
      {} "$" )])
  => [nil "$.snake_case is required"]

  (!.py
    [(base/schema-error
      {"type" "object"
       "properties" {"snake_case" {"type" "string"}}
       "required" ["snake_case"]
       "additional_properties" false}
      {"snake_case" "ok"} "$" )
     (base/schema-error
      {"type" "object"
       "properties" {"snake_case" {"type" "string"}}
       "required" ["snake_case"]}
      {} "$" )])
  => [nil "$.snake_case is required"]

  (!.dt
    [(base/schema-error
      {"type" "object"
       "properties" {"snake_case" {"type" "string"}}
       "required" ["snake_case"]
       "additional_properties" false}
      {"snake_case" "ok"} "$" )
     (base/schema-error
      {"type" "object"
       "properties" {"snake_case" {"type" "string"}}
       "required" ["snake_case"]}
      {} "$" )])
  => [nil "$.snake_case is required"])

^{:refer xt.mcp.base/tool-result :added "4.1"}
(fact "normalizes generic handler output into an MCP tool result"

  (!.js
    (base/tool-result {"ok" true}))
  => {"content" [{"type" "text" "text" "{\"ok\":true}"}]
      "structuredContent" {"ok" true}
      "isError" false}

  (!.lua
    (base/tool-result {"ok" true}))
  => {"content" [{"type" "text" "text" "{\"ok\":true}"}]
      "structuredContent" {"ok" true}
      "isError" false}

  (!.py
    (base/tool-result {"ok" true}))
  => {"content" [{"type" "text" "text" "{\"ok\":true}"}]
      "structuredContent" {"ok" true}
      "isError" false}

  (!.dt
    (base/tool-result {"ok" true}))
  => {"content" [{"type" "text" "text" "{\"ok\":true}"}]
      "structuredContent" {"ok" true}
      "isError" false})
