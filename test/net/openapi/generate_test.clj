(ns net.openapi.generate-test
  (:require [net.openapi.generate :as generate])
  (:use code.test))

(def +supabase-spec-path+
  "resources/assets/lib.supabase/openapi.json")

(def +rabbitmq-spec-path+
  "resources/assets/lib.rabbitmq/openapi.yaml")

^{:refer net.openapi.generate/read-schema :added "4.1.4"}
(fact "reads JSON and YAML schemas"
  [(get-in (generate/read-schema +supabase-spec-path+) ["info" "title"])
   (get-in (generate/read-schema +rabbitmq-spec-path+) ["info" "title"])]
  => ["Supabase API (v1)"
      "Rabbitmq Http API"])

^{:refer net.openapi.generate/schema-operations :added "4.1.4"}
(fact "normalizes schema operations through the generation layer"
  (let [supabase-ops (generate/schema-operations +supabase-spec-path+)
        rabbitmq-ops (generate/schema-operations +rabbitmq-spec-path+)]
    [(->> supabase-ops
          (filter #(= "/v1/branches/{branch_id_or_ref}" (:path %)))
          (mapv :fn_name)
          set)
     (->> rabbitmq-ops
          (filter #(= "/exchanges/{vhost}/{exchange}" (:path %)))
          (mapv :fn_name)
          set)])
  => [#{"v1-delete-a-branch"
        "v1-get-a-branch-config"
        "v1-update-a-branch-config"}
      #{"get-exchange"
        "create-exchange"
        "delete-exchange"}])

^{:refer net.openapi.generate/api-function-name :added "4.1.4"}
(fact "derives a function name from a normalized operation"
  (let [operation (first (filter #(and (= :put (:method %))
                                       (= "/exchanges/{vhost}/{exchange}" (:path %)))
                                 (generate/schema-operations +rabbitmq-spec-path+)))]
    (generate/api-function-name operation))
  => "create-exchange")

^{:refer net.openapi.generate/api-functions :added "4.1.4"}
(fact "projects schemas into function-name data"
  (let [supabase-functions (generate/api-functions +supabase-spec-path+)
        rabbitmq-functions (generate/api-functions +rabbitmq-spec-path+)]
    [(->> supabase-functions
          (filter #(= "v1-get-a-branch-config" (:fn_name %)))
          first
          (select-keys [:fn_name :method :path]))
     (->> rabbitmq-functions
          (filter #(= "list-exchanges" (:fn_name %)))
          first
          (select-keys [:fn_name :method :path]))])
  => [{:fn_name "v1-get-a-branch-config"
       :method :get
       :path "/v1/branches/{branch_id_or_ref}"}
      {:fn_name "list-exchanges"
       :method :get
       :path "/exchanges"}])
