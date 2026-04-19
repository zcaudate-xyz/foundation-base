(ns code.mcp.base.wrap
  (:require [std.json :as json])
  (:import [io.modelcontextprotocol.server McpServerFeatures$SyncToolSpecification]
           [io.modelcontextprotocol.spec McpSchema$CallToolResult McpSchema$Content McpSchema$TextContent McpSchema$Tool]
           [java.util.function BiFunction]))

(defn text-content
  [text]
  (McpSchema$TextContent. (str text)))

(defn- normalize-content-item
  [item]
  (cond
    (instance? McpSchema$Content item)
    item

    (string? item)
    (text-content item)

    (map? item)
    (case (or (:type item) (get item "type"))
      "text" (text-content (or (:text item) (get item "text")))
      (text-content (json/write item)))

    :else
    (text-content (pr-str item))))

(defn call-tool-result
  [{:keys [content isError structuredContent meta]}]
  (McpSchema$CallToolResult.
   (mapv normalize-content-item (or content []))
   (boolean isError)
   structuredContent
   meta))

(defn error-result
  [text]
  (call-tool-result {:content [{:type "text" :text text}]
                     :isError true}))

(defn tool->sdk-tool
  [{:keys [name title description inputSchema outputSchema annotations meta]}]
  (cond-> (doto (McpSchema$Tool/builder)
            (.name name)
            (.title (or title name))
            (.description (or description name)))
    inputSchema (.inputSchema (json/write inputSchema))
    outputSchema (.outputSchema outputSchema)
    annotations (.annotations annotations)
    meta (.meta meta)
    :always (.build)))

(defn tool-handler
  [{:keys [implementation] :as tool}]
  (let [f (cond
            (var? implementation) @implementation
            (fn? implementation) implementation
            :else (throw (ex-info "Tool implementation must be invokable"
                                  {:tool (:name tool)})))]
    (reify BiFunction
      (apply [_ exchange args]
        (try
          (let [result (f exchange
                          (reduce (fn [output entry]
                                    (let [key (.getKey ^java.util.Map$Entry entry)
                                          value (.getValue ^java.util.Map$Entry entry)]
                                      (cond-> (assoc output key value)
                                        (string? key) (assoc (keyword key) value))))
                                  {}
                                  (.entrySet ^java.util.Map args)))]
            (if (instance? McpSchema$CallToolResult result)
              result
              (call-tool-result result)))
          (catch Throwable t
            (error-result (or (.getMessage t)
                              (str t)))))))))

(defn clj-tool->spec
  [tool]
  (McpServerFeatures$SyncToolSpecification.
   (tool->sdk-tool tool)
   (tool-handler tool)))
