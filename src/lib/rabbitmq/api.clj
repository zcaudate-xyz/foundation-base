(ns lib.rabbitmq.api
  (:require [lib.rabbitmq.request :as request]
            [std.string :as str])
  (:refer-clojure :exclude [methods]))

(def ^:dynamic *default-methods*
  {:getter :get})

(def spec
  {"overview" {:methods #{:get}}
   "cluster-name" {:methods {:get {:action "get"}
                             :put {:action "set"
                                   :spec {:name :<cluster-name>}}}}
   "nodes" {:methods {:get {:action :list
                            :fn #(map :name %)}}}
   "nodes/{%1:name}" {:methods #{:get}}
   "extensions" {:methods #{:get}}
   "definitions" {:methods #{:get :post}}
   "connections" {:methods #{:get}}
   "connections/{%1:name}" {:methods #{:get :delete}}
   "connections/{%1:name}/channels" {:name "channels-from"
                                     :methods #{:get}}
   "channels" {:methods #{:get}}
   "channels/{%1:name}" {:methods #{:get}}
   "consumers" {:methods #{:get}}
   "consumers/{vhost-encode}" {:methods #{:get}}
   "exchanges" {:methods #{:get}}
   "exchanges/{vhost-encode}" {:methods #{:get}}
   "exchanges/{vhost-encode}/{%1:name}" {:methods #{:get :post :delete}}
   "exchanges/{vhost-encode}/{%1:name}/bindings/source" {:methods #{:get}}
   "exchanges/{vhost-encode}/{%1:name}/bindings/destination" {:methods #{:get}}
   "exchanges/{vhost-encode}/{%1:name}/publish" {:methods #{:post}}
   "queues" {:methods #{:get}}
   "queues/{vhost-encode}" {:methods #{:get}}
   "queues/{vhost-encode}/{%1:name}" {:methods #{:get :put :delete}}
   "queues/{vhost-encode}/{%1:name}/bindings" {:methods #{:get}}
   "queues/{vhost-encode}/{%1:name}/contents" {:methods #{:delete}}
   "queues/{vhost-encode}/{%1:name}/actions" {:methods #{:post}}
   "queues/{vhost-encode}/{%1:name}/get" {:methods #{:post}}
   "bindings" {:methods #{:get}}
   "bindings/{vhost-encode}" {:methods #{:get}}
   "bindings/{vhost-encode}/e/{%1:source}/q/{%2:dest}" {:methods #{:get :post}}
   "bindings/{vhost-encode}/e/{%1:source}/q/{%2:dest}/props" {:methods #{:get :delete}}
   "bindings/{vhost-encode}/e/{%1:source}/e/{%2:dest}" {:methods #{:get :post}}
   "bindings/{vhost-encode}/e/{%1:source}/e/{%2:dest}/props" {:methods #{:get :delete}}
   "vhosts" {:methods #{:get}}
   "vhosts/{%1:vhost}" {:methods #{:get :put :delete}}
   "vhosts/{%1:vhost}/permissions" {:methods #{:get}}
   "users" {:methods #{:get}}
   "users/{%1:name}" {:methods #{:get :put :delete}}
   "users/{%1:name}/permissions" {:methods #{:get}}
   "whoami" {:methods #{:get}}
   "permissions" {:methods #{:get}}
   "permissions/{vhost-encode}/{%1:user}" {:methods #{:get :put :delete}}
   "parameters" {:methods #{:get}}
   "parameters/{%1:param}" {:methods #{:get}}
   "parameters/{%1:param}/{vhost-encode}" {:methods #{:get}}
   "parameters/{%1:param}/{vhost-encode}/{%2:name}" {:methods #{:get :put :delete}}
   "policies" {:methods #{:get}}
   "policies/{vhost-encode}" {:methods #{:get}}
   "policies/{vhost-encode}/{%1:name}" {:methods #{:get :put :delete}}
   "aliveness-test/{vhost-encode}" {:methods #{:get}}})

(def ^:dynamic *methods*
  {:overview {:link "overview"}
   :cluster-name {:link "cluster-name"
                  :methods {:setter :put}}
   :extensions {:link "extensions"}
   :definitions {:link "definitions"
                 :methods {:setter :post}}
   :get-node {:link "nodes/{%1:name}"}
   :list-nodes {:link "nodes"}
   :vhost {:type :form
           :link "vhosts/{%1:vhost-encode}"
           :methods #{:get :put :delete}}
   :list-vhosts {:link "vhosts"}
   :queue {:type :form
           :link "queues/{vhost-encode}/{%1:name}"
           :methods #{:get :put :delete}}
   :list-queues {:link "queues/{vhost-encode}"}
   :exchange {:type :form
              :link "exchanges/{vhost-encode}/{%1:name}"
              :methods #{:get :put :delete}}
   :list-exchanges {:link "exchanges/{vhost-encode}"}
   :permissions {:type :form
                 :link "permissions/{vhost-encode}/{%1:user}"
                 :methods #{:get :put :delete}}
   :list-permissions {:link "permissions"}
   :bind-exchange {:link "bindings/{vhost-encode}/e/{%1:source}/e/{%2:dest}"
                   :methods {:setter :post}}
   :bind-queue {:link "bindings/{vhost-encode}/e/{%1:source}/q/{%2:dest}"
                :methods {:setter :post}}
   :list-bindings {:link "bindings/{vhost-encode}"}
   :all-bindings {:link "bindings"}
   :list-connections {:link "connections"}
   :connection {:link "connections/{%1:name}"
                :methods #{:get :delete}}
   :channels-in {:link "connections/{%1:connection}/channels"}
   :list-channels {:link "channels"}
   :get-channel {:link "channels/{%1:name}"}
   :list-consumers {:link "consumers/{vhost-encode}"}
   :user {:type :form
          :link "users/{%1:name}"
          :methods #{:get :put :delete}}
   :list-users {:link "users"}
   :healthcheck {:link "aliveness-test/{vhost-encode}"}
   :message {:type :form
             :link "exchanges/{vhost-encode}/{%1:name}/publish"
             :methods #{:post}}})

(defn classify-args
  "Classifies template fragments for API link generation."
  {:added "4.1.4"}
  [s]
  (cond (and (.startsWith s "{")
             (.endsWith s "}"))
        (let [s (subs s 1 (dec (count s)))]
          (cond (.startsWith s "%")
                (let [s (subs s 1)
                      [num name] (str/split s #":")]
                  [:entry [(Integer/parseInt num) name]])

                :else
                [:keyword (list (keyword s) 'rabbitmq)]))

        :else
        [:string s]))

(defn build-args
  "Builds the argument vector for generated link forms."
  {:added "4.1.4"}
  [args]
  (mapv (fn [[t data]]
          (if (= t :entry)
            (symbol (second data))
            data))
        args))

(defn link-args
  "Parses a link template into concrete and variable arguments."
  {:added "4.1.4"}
  [uri]
  (let [args (->> (str/split uri #"/")
                  (map classify-args))
        entries (->> args
                     (filter #(-> % first (= :entry)))
                     (sort-by #(-> % second first)))]
    {:inputs (build-args args)
     :vargs (mapv (comp symbol second second) entries)}))

(defn create-link-form
  "Creates a generated request form without a body."
  {:added "4.1.4"}
  [{:keys [inputs vargs]} key]
  (list (vec (cons 'rabbitmq vargs))
        (list `request/request 'rabbitmq (list `str/joinl inputs "/") key)))

(defn create-body-form
  "Creates a generated request form with a body."
  {:added "4.1.4"}
  [{:keys [inputs vargs]} key]
  (list (conj (vec (cons 'rabbitmq vargs)) 'body)
        (list `request/request 'rabbitmq (list `str/joinl inputs "/") key {:body 'body})))

(defn create-accessor-form
  "Creates an accessor form from route metadata."
  {:added "4.1.4"}
  [fname {:keys [link methods]}]
  (let [args (link-args link)
        getter-key (or (:getter methods) :get)
        getter-form (create-link-form args getter-key)
        setter-form (when-let [setter-key (:setter methods)]
                      (create-body-form args setter-key))]
    `(defn ~(symbol (if (keyword? fname) (name fname) fname))
       ~@(filter identity [getter-form setter-form]))))

(defn create-function-forms
  "Creates CRUD style forms for a route spec."
  {:added "4.1.4"}
  [fname {:keys [link methods]}]
  (let [args (link-args link)
        fname (if (keyword? fname) (name fname) fname)
        forms {:get (create-link-form args :get)
               :delete (create-link-form args :delete)
               :put (create-body-form args :put)
               :post (create-body-form args :post)}
        prefix {:get :get :delete :delete :put :add :post :add}]
    (reduce (fn [arr k]
              (conj arr `(defn ~(symbol (str (name (get prefix k)) "-" fname))
                           ~(get forms k))))
            []
            methods)))

(defn create-api-functions
  "Materializes the API functions for the route table."
  {:added "4.1.4"}
  [methods]
  (mapv (fn [[name opts]]
          (eval (if (= :form (:type opts))
                  `(do ~@(create-function-forms name opts))
                  (create-accessor-form name opts))))
        (seq methods)))

(defonce +init+ (create-api-functions *methods*))
