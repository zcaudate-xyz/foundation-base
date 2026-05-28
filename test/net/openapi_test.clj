(ns net.openapi-test
  (:require [clojure.string :as string]
            [net.openapi :as openapi])
  (:use code.test))

(def +supabase-spec-path+
  "resources/assets/lib.supabase/openapi.json")

(def +rabbitmq-spec-path+
  "resources/assets/lib.rabbitmq/openapi.yaml")

(defn form-defn-names
  [form]
  (->> (rest form)
       (keep (fn [entry]
               (when (and (seq? entry)
                          (#{'defn 'clojure.core/defn} (first entry)))
                 (second entry))))
       vec))

^{:refer net.openapi/read-spec :added "4.1.4"}
(fact "reads JSON and YAML OpenAPI fixtures"
  [(openapi/spec-title (openapi/read-spec +supabase-spec-path+))
   (openapi/spec-title (openapi/read-spec +rabbitmq-spec-path+))]
  => ["Supabase API (v1)"
      "Rabbitmq Http API"])

^{:refer net.openapi/spec-base-url :added "4.1.4"}
(fact "reads the declared base url when the spec provides one"
  [(openapi/spec-base-url (openapi/read-spec +supabase-spec-path+))
   (openapi/spec-base-url (openapi/read-spec +rabbitmq-spec-path+))]
  => [nil
      "http://mb1.bus.adaptive.me/rabbitmq/api"])

^{:refer net.openapi/find-operation :added "4.1.4"}
(fact "normalizes Supabase and RabbitMQ operations from fixture specs"
  (let [supabase (openapi/read-spec +supabase-spec-path+)
        rabbitmq (openapi/read-spec +rabbitmq-spec-path+)
        branch-get (openapi/find-operation supabase :get "/v1/branches/{branch_id_or_ref}")
        exchange-put (openapi/find-operation rabbitmq :put "/exchanges/{vhost}/{exchange}")]
    [(:fn_name branch-get)
     (-> branch-get :path_params count)
     (get-in exchange-put [:request_body :required])
     (get-in exchange-put [:request_body :content_types])
     (:auth_names exchange-put)])
  => ["v1-get-a-branch-config"
      1
      false
      ["application/json"]
      ["basic_auth"]])

^{:refer net.openapi/scaffold :added "4.1.4"}
(fact "generates net.http scaffold forms from fixture specs"
  (let [rabbitmq-form (openapi/scaffold +rabbitmq-spec-path+
                                        {:ns_sym 'generated.rabbitmq})
        supabase-form (openapi/scaffold +supabase-spec-path+
                                        {:ns_sym 'generated.supabase})
        rabbitmq-names (set (map str (form-defn-names rabbitmq-form)))
        supabase-names (set (map str (form-defn-names supabase-form)))]
    [(contains? rabbitmq-names "list-exchanges")
     (contains? rabbitmq-names "create-exchange")
     (contains? supabase-names "v1-get-a-branch-config")
     (contains? supabase-names "v1-update-a-branch-config")])
  => [true true true true])

^{:refer net.openapi/scaffold-string :added "4.1.4"}
(fact "pretty prints generated scaffold code"
  (-> (openapi/scaffold-string +rabbitmq-spec-path+
                               {:ns_sym 'generated.rabbitmq})
      (string/includes? "list-exchanges"))
  => true)
