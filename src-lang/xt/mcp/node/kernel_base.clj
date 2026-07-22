(ns xt.mcp.node.kernel-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]
             [xt.substrate.base-request :as node-request]
             [xt.mcp.base :as base]]})

(def.xt DEFAULT_SERVICE "mcp/default")
(def.xt MESSAGE_ACTION "@xt.mcp/message")

(defn.xt create-service
  "creates MCP registry and session state"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (return {"::" "mcp.service"
           "protocol_version" base/PROTOCOL_VERSION
           "server_info" (or (. opts ["server_info"])
                             {"name" "xt.mcp" "version" "0.1.0"})
           "instructions" (. opts ["instructions"])
           "authorize_fn" (. opts ["authorize_fn"])
           "tools" {}
           "sessions" {}
           "meta" (or (. opts ["meta"]) {})}))

(defn.xt install-service
  "installs a named MCP service on a substrate node"
  {:added "4.1"}
  [node service-id opts]
  (var sid (or service-id -/DEFAULT_SERVICE))
  (var service (-/create-service opts))
  (substrate/set-service node sid service)
  (return service))

(defn.xt get-service
  "gets a named MCP service"
  {:added "4.1"}
  [node service-id]
  (return (substrate/get-service node (or service-id -/DEFAULT_SERVICE))))

(defn.xt ensure-service
  "gets or creates a named MCP service"
  {:added "4.1"}
  [node service-id opts]
  (var sid (or service-id -/DEFAULT_SERVICE))
  (var service (-/get-service node sid))
  (when (xt/x:nil? service)
    (:= service (-/install-service node sid opts)))
  (return service))

(defn.xt register-tool
  "combines generic MCP data with an executable handler"
  {:added "4.1"}
  [node service-id tool handler meta]
  (when (not (base/tool-valid? tool))
    (xt/x:err "invalid MCP tool descriptor"))
  (when (not (xt/x:is-function? handler))
    (xt/x:err "MCP tool handler must be a function"))
  (var service (-/ensure-service node service-id nil))
  (var tools (. service ["tools"]))
  (var name (. tool ["name"]))
  (when (xt/x:not-nil? (xt/x:get-key tools name))
    (xt/x:err (xt/x:cat "duplicate MCP tool - " name)))
  (var entry {:tool tool :handler handler :meta (or meta {})})
  (xt/x:set-key tools name entry)
  (return entry))

(defn.xt unregister-tool
  "removes an MCP tool registration"
  {:added "4.1"}
  [node service-id tool-name]
  (var service (-/ensure-service node service-id nil))
  (var tools (. service ["tools"]))
  (var entry (xt/x:get-key tools tool-name))
  (xt/x:del-key tools tool-name)
  (return entry))

(defn.xt get-tool
  "gets an MCP tool registry entry"
  {:added "4.1"}
  [node service-id tool-name]
  (var service (-/ensure-service node service-id nil))
  (return (xt/x:get-key (. service ["tools"]) tool-name)))

(defn.xt list-tools
  "lists registered tools in deterministic name order"
  {:added "4.1"}
  [node service-id]
  (var service (-/ensure-service node service-id nil))
  (var names (xtd/arr-sort (xt/x:obj-keys (. service ["tools"]))
                           (fn [x] (return x))
                           xt/x:str-lt))
  (return
   (xt/x:arr-map names
                 (fn [name]
                   (return
                    (base/tool-wire
                     (. (xt/x:get-key (. service ["tools"]) name) ["tool"])))))))

(defn.xt session-id
  "resolves the logical MCP session id from call context"
  {:added "4.1"}
  [context]
  (return (or (xtd/get-in context ["session_id"]) "__DEFAULT__")))

(defn.xt session-get
  [service context]
  (return (xt/x:get-key (. service ["sessions"])
                        (-/session-id context))))

(defn.xt session-initialized?
  [service context]
  (return (== true (xtd/get-in (-/session-get service context)
                               ["initialized"]))))

(defn.xt session-mark-initialized
  [service context client-info]
  (var session (or (-/session-get service context) {}))
  (xt/x:set-key session "initialized" true)
  (when (xt/x:not-nil? client-info)
    (xt/x:set-key session "client_info" client-info))
  (xt/x:set-key (. service ["sessions"])
                (-/session-id context)
                session)
  (return session))

(defn.xt session-start
  [service context params]
  (var session {"initialized" false
                "client_info" (. params ["clientInfo"])
                "protocol_version" (. params ["protocolVersion"])})
  (xt/x:set-key (. service ["sessions"])
                (-/session-id context)
                session)
  (return session))

(defn.xt call-tool
  "validates arguments and invokes a registered generic handler"
  {:added "4.1"}
  [node service-id tool-name tool-args request context]
  (var service (-/ensure-service node service-id nil))
  (var entry (-/get-tool node service-id tool-name))
  (when (xt/x:nil? entry)
    (xt/x:err (xt/x:cat "Unknown tool: " tool-name)))
  (var tool (. entry ["tool"]))
  (:= tool-args (or tool-args {}))
  (var validation-error
       (base/schema-error (. tool ["input_schema"]) tool-args "$"))
  (when (xt/x:not-nil? validation-error)
    (return (promise/x:promise-run
             (base/tool-error-result validation-error))))
  (var call-context
       {"node" node
        "service_id" (or service-id -/DEFAULT_SERVICE)
        "tool" tool
        "request" request
        "request_id" (. request ["id"])
        "session_id" (-/session-id context)
        "application" (. context ["application"])
        "meta" (or (. entry ["meta"]) {})})
  (var authorize-fn (. service ["authorize_fn"]))
  (try
    (return
     (-> (node-request/ensure-promise
          (:? (xt/x:is-function? authorize-fn)
              (authorize-fn tool tool-args call-context)
              true))
         (promise/x:promise-then
          (fn [allowed]
            (when (not allowed)
              (xt/x:err "MCP tool call was not authorized"))
            (return
             (node-request/ensure-promise
              ((. entry ["handler"]) tool-args call-context)))))
         (promise/x:promise-then base/tool-result)
         (promise/x:promise-catch
          (fn [error]
            (return (base/tool-error-result error))))))
    (catch error
      (return (promise/x:promise-run
               (base/tool-error-result error))))))

(defn.xt initialize-result
  [service]
  (var result
       {"protocolVersion" (. service ["protocol_version"])
        "capabilities" {"tools" {"listChanged" false}}
        "serverInfo" (. service ["server_info"])})
  (when (xt/x:not-nil? (. service ["instructions"]))
    (xt/x:set-key result "instructions" (. service ["instructions"])))
  (return result))

(defn.xt handle-message
  "handles one decoded MCP JSON-RPC message"
  {:added "4.1"}
  [node service-id message context]
  (:= context (or context {}))
  (var id (. message ["id"]))
  (var method (. message ["method"]))
  (var params (or (. message ["params"]) {}))
  (var service (-/ensure-service node service-id nil))
  (var tool-name nil)
  (when (not (== "2.0" (. message ["jsonrpc"])))
    (return (promise/x:promise-run
             (base/error-response id -32600 "Invalid Request" nil))))
  (cond (== method "initialize")
        (do (-/session-start service context params)
            (return
             (promise/x:promise-run
              (base/response id (-/initialize-result service)))))

        (== method "notifications/initialized")
        (do (-/session-mark-initialized service context nil)
            (return (promise/x:promise-run nil)))

        (not (-/session-initialized? service context))
        (return
         (promise/x:promise-run
          (base/error-response id -32002 "MCP session is not initialized" nil)))

        (== method "ping")
        (return (promise/x:promise-run (base/response id {})))

        (== method "tools/list")
        (return
         (promise/x:promise-run
          (base/response id {"tools" (-/list-tools node service-id)})))

        (== method "tools/call")
        (:= tool-name (. params ["name"]))

        :else
        (return
         (promise/x:promise-run
          (base/error-response id -32601 "Method not found" {"method" method}))))
  (when (xt/x:nil? (-/get-tool node service-id tool-name))
    (return
     (promise/x:promise-run
      (base/error-response id -32602
                           (xt/x:cat "Unknown tool: " tool-name)
                           nil))))
  (return
   (promise/x:promise-then
    (-/call-tool node
                 service-id
                 tool-name
                 (. params ["arguments"])
                 message
                 context)
    (fn [result]
      (return (base/response id result))))))

(defn.xt message-handler
  "substrate handler for decoded MCP messages"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var message (xt/x:second args))
  (var context (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return (-/handle-message node service-id message context)))

(defn.xt init-handlers
  "installs server-side MCP substrate handlers"
  {:added "4.1"}
  [node]
  (substrate/register-handler node -/MESSAGE_ACTION -/message-handler nil)
  (return node))
