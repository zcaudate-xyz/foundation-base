(ns postgres.gen.mcp-test
  (:use code.test)
  (:require [postgres.gen.mcp :as mcp]
            [postgres.sample.scratch-v0 :as scratch]))

^{:refer postgres.gen.mcp/pg-type-schema :added "4.1"}
(fact "maps PostgreSQL types to the portable JSON Schema subset"
  [(mcp/pg-type-schema "uuid")
   (mcp/pg-type-schema "numeric")
   (mcp/pg-type-schema "text")]
  => [{:type "string" :format "uuid"}
      {:type "number"}
      {:type "string"}])

^{:refer postgres.gen.mcp/input-schema :added "4.1"}
(fact "preserves source argument casing and uses snake_case descriptor keys"
  (mcp/input-schema [{:symbol "i_message" :type "text"}])
  => {:type "object"
      :properties {"i_message" {:type "string"}}
      :required ["i_message"]
      :additional_properties false})

^{:refer postgres.gen.mcp/tool-descriptor :added "4.1"}
(fact "extracts MCP metadata without attaching an executable database handler"
  (mcp/tool-descriptor scratch/log-append-public)
  => {:name "log_append_public"
      :title "Append public log"
      :description "Appends a log row for the current authenticated user."
      :input_schema {:type "object"
                     :properties {"i_message" {:type "string"}}
                     :required ["i_message"]
                     :additional_properties false}
      :output_schema {:type "object"}})

^{:refer postgres.gen.mcp/tool-entries :added "4.1"}
(fact "only includes explicitly opted-in PostgreSQL definitions"
  (mapv (comp :name second)
        (mcp/tool-entries ['postgres.sample.scratch-v0]))
  => ["log_append_public"])

^{:refer postgres.gen.mcp/render-module :added "4.1"}
(fact "renders MCP data through the generic module template"
  (mcp/render-module 'xt.mcp.sample ['postgres.sample.scratch-v0])
  => #(and (re-find #"\^\{:api/type :mcp\}" %)
           (re-find #":input_schema" %)
           (not (re-find #"xt.db" %))))
