(ns net.openapi.read-test
  (:require [net.openapi.read :as read])
  (:use code.test))

(def +supabase-spec-path+
  "resources/assets/lib.supabase/openapi.json")

(def +v2-spec-json+
  "{\"swagger\":\"2.0\",\"info\":{\"title\":\"Widget API\",\"version\":\"1.0.0\"},\"host\":\"example.com\",\"basePath\":\"/api\",\"schemes\":[\"https\"],\"consumes\":[\"application/json\"],\"produces\":[\"application/json\"],\"paths\":{\"/widgets\":{\"get\":{\"operationId\":\"list-widgets\",\"summary\":\"Lists widgets\",\"responses\":{\"200\":{\"description\":\"OK\"}}},\"post\":{\"operationId\":\"create-widget\",\"summary\":\"Creates a widget\",\"parameters\":[{\"name\":\"body\",\"in\":\"body\",\"required\":true,\"schema\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}}],\"responses\":{\"201\":{\"description\":\"Created\"}}}},\"/widgets/{widget_id}\":{\"parameters\":[{\"name\":\"widget_id\",\"in\":\"path\",\"required\":true,\"type\":\"string\"}],\"get\":{\"operationId\":\"get-widget\",\"summary\":\"Gets a widget\",\"security\":[{\"basic_auth\":[]}],\"responses\":{\"200\":{\"description\":\"OK\"}}},\"put\":{\"operationId\":\"update-widget\",\"summary\":\"Updates a widget\",\"security\":[{\"basic_auth\":[]}],\"parameters\":[{\"name\":\"body\",\"in\":\"body\",\"required\":false,\"schema\":{\"type\":\"object\"}}],\"responses\":{\"200\":{\"description\":\"OK\"}}},\"delete\":{\"operationId\":\"delete-widget\",\"summary\":\"Deletes a widget\",\"security\":[{\"basic_auth\":[]}],\"responses\":{\"204\":{\"description\":\"Deleted\"}}}}}}")

(def +v2-spec+
  (read/read-json-string +v2-spec-json+))

(def +v3-spec+
  {"openapi" "3.0.0"
   "info" {"title" "Widget API"
           "version" "2.0.0"}
   "servers" [{"url" "https://api.example.com"}]
   "security" [{"bearer_auth" []}]
   "components" {"schemas" {"Widget" {"type" "object"
                                     "description" "A widget"
                                     "x-go-name" "Widget"
                                     "properties" {"name" {"type" "string"
                                                           "description" "Widget name"}
                                                   "meta" {"type" "object"
                                                           "x-go-name" "WidgetMeta"
                                                           "properties" {"enabled" {"type" "boolean"}}}}}}
                 "parameters" {"WidgetId" {"name" "widget_id"
                                           "in" "path"
                                           "required" true
                                           "schema" {"type" "string"}}}
                 "requestBodies" {"WidgetBody" {"required" true
                                                "content" {"application/json" {"schema" {"$ref" "#/components/schemas/Widget"}}}}}
                 "responses" {"WidgetResponse" {"description" "OK"
                                                "content" {"application/json" {"schema" {"$ref" "#/components/schemas/Widget"}}}}}}
   "paths" {"/widgets/{widget_id}" {"parameters" [{"$ref" "#/components/parameters/WidgetId"}]
                                    "get" {"summary" "Gets a widget"
                                           "responses" {"200" {"$ref" "#/components/responses/WidgetResponse"}}}
                                    "patch" {"operationId" "updateWidget"
                                             "parameters" [{"name" "trace-id"
                                                            "in" "header"
                                                            "required" false
                                                            "schema" {"type" "string"}}
                                                           {"name" "include"
                                                            "in" "query"
                                                            "required" false
                                                            "schema" {"type" "string"}}]
                                             "requestBody" {"$ref" "#/components/requestBodies/WidgetBody"}
                                             "security" [{"service_key" []}]
                                             "responses" {"200" {"$ref" "#/components/responses/WidgetResponse"}
                                                          "204" {"description" "No Content"}}}}}})

(fact "reads an OpenAPI 3 JSON schema into a normalized operation map"
  (let [supabase (read/read-v3 +supabase-spec-path+)
       create-project (get supabase "v1-create-a-project")]
    [(:fn-name create-project)
     (:method create-project)
     (:path create-project)
     (-> create-project :body :required)
     (-> create-project :body :content-types)
     (:auth-names create-project)])
  => ["v1-create-a-project"
      :post
      "/v1/projects"
      true
      ["application/json"]
      ["bearer" "fga_permissions"]])

(fact "reads a Swagger 2 JSON schema into the same normalized model"
  (let [widgets (read/read-v2 +v2-spec-json+)
        update-widget (get widgets "update-widget")]
    [(:fn-name update-widget)
     (:method update-widget)
     (:path update-widget)
     (-> update-widget :path-params count)
     (-> update-widget :body :required)
     (-> update-widget :body :content-types)
     (:auth-names update-widget)])
  => ["update-widget"
      :put
      "/widgets/{widget_id}"
      1
      false
      ["application/json"]
      ["basic_auth"]])

(fact "auto-detects v2 and v3 specs and returns a function-keyed model"
  (let [supabase (read/read +supabase-spec-path+)
        widgets (read/read +v2-spec-json+)]
    [(-> supabase (get "v1-create-a-project") :path)
     (-> supabase (get "v1-create-a-project") :body :required)
     (-> widgets (get "create-widget") :method)
     (-> widgets (get "create-widget") :body :required)])
  => ["/v1/projects"
      true
      :post
      true])


^{:refer net.openapi.read/data-get :added "4.1"}
(fact "reads string and keyword keys interchangeably"
  [(read/data-get {"a" 1 :b 2} "a")
   (read/data-get {"a" 1 :b 2} :a)
   (read/data-get {"a" 1 :b 2} "b")]
  => [1 1 2])

^{:refer net.openapi.read/data-get-in :added "4.1"}
(fact "walks maps and vectors safely"
  [(read/data-get-in {"items" [{"name" "a"} {"name" "b"}]} ["items" 1 "name"])
   (read/data-get-in {"items" []} ["items" 0 "name"])]
  => ["b" nil])

^{:refer net.openapi.read/read-json-string :added "4.1"}
(fact "parses a raw json string"
  (read/data-get (read/read-json-string "{\"widget_id\":1}") "widget_id")
  => 1)

^{:refer net.openapi.read/slurp-source :added "4.1"}
(fact "accepts maps and raw json strings as sources"
  [(read/slurp-source {"swagger" "2.0"})
   (read/slurp-source "{\"swagger\":\"2.0\"}")]
  => [{"swagger" "2.0"}
      "{\"swagger\":\"2.0\"}"])

^{:refer net.openapi.read/read-schema :added "4.1"}
(fact "reads a schema from a raw json string"
  (read/spec-version (read/read-schema +v2-spec-json+))
  => :v2)

(fact "reads schemas through the resource cache when a classpath resource is available"
  (let [calls (atom [])]
    (with-redefs [read/resource-path (fn [_] "assets/lib.supabase/openapi.json")
                  std.lib.env/sys:resource-cached
                  (fn [_ path _]
                    (swap! calls conj path)
                    +v2-spec+)]
      [(read/spec-version (read/read-schema "resources/assets/lib.supabase/openapi.json"))
       @calls]))
  => [:v2
      ["assets/lib.supabase/openapi.json"]])

^{:refer net.openapi.read/spec-version :added "4.1"}
(fact "detects swagger 2 vs openapi 3"
  [(read/spec-version {"swagger" "2.0"})
   (read/spec-version {"openapi" "3.0.0"})]
  => [:v2 :v3])

^{:refer net.openapi.read/unescape-ref-token :added "4.1"}
(fact "unescapes json pointer tokens"
  (read/unescape-ref-token "Widget~1Version~0Ref")
  => "Widget/Version~Ref")

^{:refer net.openapi.read/ref-path :added "4.1"}
(fact "converts a local ref into a lookup path"
  (read/ref-path "#/components/schemas/Widget~1Version")
  => ["components" "schemas" "Widget/Version"])

^{:refer net.openapi.read/resolve-ref :added "4.1"}
(fact "resolves refs and merges sibling keys"
  (read/resolve-ref +v3-spec+ {"$ref" "#/components/schemas/Widget"
                               "nullable" true})
  => {"type" "object"
      "description" "A widget"
      "x-go-name" "Widget"
      "properties" {"meta" {"type" "object"
                            "x-go-name" "WidgetMeta"
                            "properties" {"enabled" {"type" "boolean"}}}
                    "name" {"type" "string"
                            "description" "Widget name"}}
      "nullable" true})

^{:refer net.openapi.read/operation-name :added "4.1"}
(fact "uses operation ids when present and path-derived names otherwise"
  [(read/operation-name :patch "/widgets/{widget_id}" {"operationId" "updateWidget"})
   (read/operation-name :get "/widgets/{widget_id}" {})]
  => ["update-widget"
      "get-widgets-by-widget-id"])

^{:refer net.openapi.read/merge-parameters :added "4.1"}
(fact "merges path and operation parameters by name and location"
  (let [path-item {"parameters" [{"name" "widget_id"
                                  "in" "path"
                                  "description" "path-level"}
                                 {"name" "trace-id"
                                  "in" "header"
                                  "description" "path-trace"}]}
        operation {"parameters" [{"name" "widget_id"
                                  "in" "path"
                                  "description" "operation-level"}]}]
    (mapv #(select-keys % ["name" "in" "description"])
          (read/merge-parameters {} path-item operation)))
  => [{"name" "widget_id"
       "in" "path"
       "description" "operation-level"}
      {"name" "trace-id"
       "in" "header"
       "description" "path-trace"}])

^{:refer net.openapi.read/normalize-schema :added "4.1"}
(fact "resolves schemas directly"
  (read/normalize-schema +v3-spec+ {"$ref" "#/components/schemas/Widget"})
  => {:type "object"
      :description "A widget"
      :properties {"meta" {:type "object"
                           :properties {"enabled" {:type "boolean"}}
                           :custom {"x-go-name" "WidgetMeta"}}
                   "name" {:type "string"
                           :description "Widget name"}}
      :custom {"x-go-name" "Widget"}})

^{:refer net.openapi.read/normalize-content :added "4.1"}
(fact "normalizes content maps and selects the preferred schema"
  (let [content (read/normalize-content +v3-spec+
                                        {"application/json" {"schema" {"$ref" "#/components/schemas/Widget"}}})]
    [(:content-types content)
     (:type content)
     (:custom content)])
  => [["application/json"]
      "object"
      {"x-go-name" "Widget"}])

^{:refer net.openapi.read/normalize-body-v2 :added "4.1"}
(fact "normalizes swagger 2 body parameters"
  (let [path-item (read/data-get-in +v2-spec+ ["paths" "/widgets"])
        operation (read/data-get path-item "post")
        parameters (read/merge-parameters +v2-spec+ path-item operation)]
    [(-> (read/normalize-body-v2 +v2-spec+ operation parameters) :required)
     (-> (read/normalize-body-v2 +v2-spec+ operation parameters) :content-types)
     (-> (read/normalize-body-v2 +v2-spec+ operation parameters) :type)
     (-> (read/normalize-body-v2 +v2-spec+ operation parameters) :properties keys vec)])
  => [true
      ["application/json"]
      "object"
      ["name"]])

^{:refer net.openapi.read/normalize-body-v3 :added "4.1"}
(fact "normalizes openapi 3 request bodies"
  (let [operation (read/data-get-in +v3-spec+ ["paths" "/widgets/{widget_id}" "patch"])
        body (read/normalize-body-v3 +v3-spec+ operation)]
    [(:required body)
     (:content-types body)
     (:type body)
     (-> body :properties keys sort vec)])
  => [true
      ["application/json"]
      "object"
      ["meta" "name"]])

^{:refer net.openapi.read/normalize-parameter :added "4.1"}
(fact "normalizes a non-body parameter"
  (read/normalize-parameter +v3-spec+ {"name" "widget_id"
                                       "in" "path"
                                       "required" true
                                       "description" "widget id"
                                       "schema" {"type" "string"}})
  => {:name "widget_id"
      :required true
      :description "widget id"
      :type "string"})

^{:refer net.openapi.read/parameter-bucket :added "4.1"}
(fact "collects parameters by location"
  (read/parameter-bucket +v3-spec+
                         [{"name" "trace-id"
                           "in" "header"
                           "required" false
                           "schema" {"type" "string"}}
                          {"name" "include"
                           "in" "query"
                           "required" false
                           "schema" {"type" "string"}}]
                         "query")
  => [{:name "include"
       :required false
       :description nil
       :type "string"}])

^{:refer net.openapi.read/response-content-types-v2 :added "4.1"}
(fact "reads swagger 2 response content types"
  (read/response-content-types-v2 +v2-spec+
                                  (read/data-get-in +v2-spec+ ["paths" "/widgets/{widget_id}" "get"]))
  => ["application/json"])

^{:refer net.openapi.read/response-content-types-v3 :added "4.1"}
(fact "reads openapi 3 response content types"
  (read/response-content-types-v3 +v3-spec+
                                  (read/data-get-in +v3-spec+ ["paths" "/widgets/{widget_id}" "patch"]))
  => ["application/json"])

^{:refer net.openapi.read/auth-names :added "4.1"}
(fact "prefers operation security and falls back to spec security"
  [(read/auth-names +v3-spec+
                    (read/data-get-in +v3-spec+ ["paths" "/widgets/{widget_id}" "patch"]))
   (read/auth-names +v3-spec+
                    (read/data-get-in +v3-spec+ ["paths" "/widgets/{widget_id}" "get"]))]
  => [["service_key"]
      ["bearer_auth"]])

^{:refer net.openapi.read/operation-entry :added "4.1"}
(fact "builds a normalized operation entry"
  (let [entry (read/operation-entry +v3-spec+
                                    :v3
                                    "/widgets/{widget_id}"
                                    (read/data-get-in +v3-spec+ ["paths" "/widgets/{widget_id}"])
                                    :patch
                                    (read/data-get-in +v3-spec+ ["paths" "/widgets/{widget_id}" "patch"]))]
    [(:fn-name entry)
     (count (:path-params entry))
     (count (:query-params entry))
     (count (:header-params entry))
     (-> entry :body :required)
     (:auth-names entry)])
  => ["update-widget" 1 1 1 true ["service_key"]])

^{:refer net.openapi.read/operation-map :added "4.1"}
(fact "returns a function-keyed sorted operation map"
  (let [operations (read/operation-map +v2-spec+ :v2)]
    [(count operations)
     (vec (keys operations))
     (-> operations (get "create-widget") :method)])
  => [5
      ["create-widget"
       "delete-widget"
       "get-widget"
       "list-widgets"
       "update-widget"]
      :post])

^{:refer net.openapi.read/read-v2 :added "4.1"}
(fact "reads swagger 2 schemas through the public reader"
  (let [operations (read/read-v2 +v2-spec+)]
    [(count operations)
     (-> operations (get "create-widget") :method)])
  => [5 :post])

^{:refer net.openapi.read/read-v3 :added "4.1"}
(fact "reads openapi 3 schemas through the public reader"
  (let [operations (read/read-v3 +v3-spec+)]
    [(vec (keys operations))
     (-> operations (get "update-widget") :method)])
  => [["get-widgets-by-widget-id" "update-widget"]
      :patch])

^{:refer net.openapi.read/read :added "4.1"}
(fact "auto-detects and reads normalized operations"
  (let [operations (read/read +v3-spec+)]
    [(count operations)
     (-> operations (get "update-widget") :auth-names)])
  => [2 ["service_key"]])

(fact "filters normalized operations by path selector"
  (let [operations (read/read +v3-spec+ "/widgets/{widget_id}")]
    [(vec (keys operations))
     (-> operations (get "update-widget") :method)])
  => [["get-widgets-by-widget-id" "update-widget"]
      :patch])

(fact "supports selector vectors as path allowlists"
  (let [operations (read/read +v2-spec+
                              ["/widgets/{widget_id}"
                               "/widgets"])]
    [(count operations)
     (contains? operations "create-widget")
     (contains? operations "update-widget")])
  => [5 true true])

(fact "can remove empty parameter buckets when requested"
  (let [operations (read/read +v3-spec+
                              {:selector "/widgets/{widget_id}"
                               :remove-empty? true})
        entry (get operations "get-widgets-by-widget-id")]
    [(contains? entry :path-params)
     (contains? entry :query-params)
     (contains? entry :header-params)
     (contains? entry :cookie-params)])
  => [true false false false])

^{:refer net.openapi.read/resource-path :added "4.1"}
(fact "resolves classpath resources and rejects json strings and urls"
  [(read/resource-path "{\"swagger\":\"2.0\"}")
   (read/resource-path "  {\"swagger\":\"2.0\"}")
   (read/resource-path "http://example.com/openapi.json")
   (read/resource-path "resources/assets/lib.supabase/openapi.json")
   (read/resource-path "assets/lib.supabase/openapi.json")]
  => [nil
      nil
      nil
      "assets/lib.supabase/openapi.json"
      "assets/lib.supabase/openapi.json"])

^{:refer net.openapi.read/vendor-extension-map :added "4.1"}
(fact "extracts x- prefixed keys into a sorted map"
  [(read/vendor-extension-map {"type" "object" "x-go-name" "Widget"})
   (read/vendor-extension-map {:type "object" :x-go-name "Widget"})
   (read/vendor-extension-map {"type" "object"})
   (read/vendor-extension-map nil)]
  => [{"x-go-name" "Widget"}
      {"x-go-name" "Widget"}
      {}
      {}])

^{:refer net.openapi.read/normalize-properties :added "4.1"}
(fact "normalizes each property schema and sorts the result"
  (read/normalize-properties {}
                             {"name" {"type" "string"}
                              "count" {"type" "integer"}})
  => {"count" {:type "integer"}
      "name" {:type "string"}})

^{:refer net.openapi.read/select-filter :added "4.1"}
(fact "matches ids against functions, strings, regexes, sets, seqs, vectors and numbers"
  [(read/select-filter #(= "foo" %) "foo")
   (read/select-filter #(= "foo" %) "bar")
   (read/select-filter "/widgets" "/widgets/{widget_id}")
   (read/select-filter :get :get-widgets)
   (read/select-filter 'get "get-widgets")
   (read/select-filter #"/widgets" "/widgets/{widget_id}")
   (read/select-filter #{"/a" "/widgets"} "/widgets")
   (read/select-filter #{"/a" "/widgets"} "/other")
   (read/select-filter '("/widgets" "/widgets/{widget_id}") "/widgets/{widget_id}")
   (read/select-filter ["/a" "/widgets"] "/widgets/{widget_id}")
   (read/select-filter 42 42)]
  => [true false true true true true true false true true true])

^{:refer net.openapi.read/select-operations :added "4.1"}
(fact "filters operations by applying the selector to the path"
  (let [ops (read/operation-map +v3-spec+ :v3)]
    [(count (read/select-operations ops nil))
     (count (read/select-operations ops "/widgets/{widget_id}"))
     (count (read/select-operations ops #"/widgets"))
     (count (read/select-operations ops "/unknown"))])
  => [2 2 2 0])

^{:refer net.openapi.read/remove-empty-options :added "4.1"}
(fact "drops empty parameter buckets but preserves non-empty ones"
  (read/remove-empty-options {:path-params [{:name "widget_id"}]
                              :query-params []
                              :header-params []
                              :cookie-params []
                              :body {:required true}})
  => {:path-params [{:name "widget_id"}]
      :body {:required true}})

^{:refer net.openapi.read/format-operations :added "4.1"}
(fact "optionally removes empty buckets from every operation"
  (let [ops {"create-widget" {:path-params []
                               :query-params [{:name "q"}]}}]
    [(-> (read/format-operations ops {:remove-empty? true})
         (get "create-widget")
         keys
         sort
         vec)
     (-> (read/format-operations ops {})
         (get "create-widget")
         keys
         sort
         vec)])
  => [[:query-params]
      [:path-params :query-params]])