(ns documentation.xt-mcp)

[[:hero {:title "xt.mcp"
         :subtitle "A generic MCP addon for substrate nodes."
         :lead "`xt.mcp` keeps portable MCP descriptor data separate from execution. `xt.mcp.node` attaches descriptors to generic handlers and exposes decoded JSON-RPC through an existing `xt.substrate` node."}]]

[[:chapter {:title "Layering" :link "layering"}]]

"The package follows the `xt.db` split. `xt.mcp.base` owns protocol data, schema validation, and wire projection. `xt.mcp.node.kernel-base` owns server registry and dispatch. `client-base` is the stable caller API, `proxy-base` mirrors server action identifiers over a transport, and `runtime` installs either side onto a caller-owned substrate node. None of these namespaces imports `xt.db`."

[[:chapter {:title "Naming boundary" :link "naming"}]]

"Source descriptors use snake_case keys such as `input_schema`, `output_schema`, `additional_properties`, and `read_only_hint`. The wire projection emits MCP vocabulary such as `inputSchema`, `outputSchema`, `additionalProperties`, and `readOnlyHint`. Names inside a schema's `properties` map are application data and are preserved exactly, so XTalk and PostgreSQL arguments may remain snake_case."

[[:chapter {:title "PostgreSQL metadata" :link "postgres"}]]

"`postgres.gen.mcp` recognizes explicit `:api/meta {:mcp ...}` declarations and derives portable input/output schemas from bound PostgreSQL signatures. It emits descriptor data only. A database RPC handler, authentication policy, or other executable adapter must be composed separately with `xt.mcp.node/register-tool`."

[[:chapter {:title "Protocol scope" :link "scope"}]]

"The initial addon supports MCP protocol `2025-11-25` initialization, initialized notification, ping, tools/list, and tools/call. Transport framing, HTTP/stdio servers, resources, prompts, tasks, progress, and cancellation remain separate extensions."

[[:chapter {:title "Example" :link "example"}]]

[[:code {:lang "clojure"
         :title "Install a generic handler"
         :content "(runtime/init-server node \"mcp/app\" {})\n(kernel/register-tool\n node \"mcp/app\"\n {\"name\" \"sample_echo\"\n  \"description\" \"Echoes a value.\"\n  \"input_schema\" {\"type\" \"object\"\n                    \"properties\" {\"snake_case\" {\"type\" \"string\"}}}}\n (fn [args context]\n   (return {\"echo\" (. args [\"snake_case\"])}))\n {})"}]]
