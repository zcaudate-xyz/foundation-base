(ns rt.postgres.supabase
  (:require [net.http :as http]
            [std.lib :as h]
            [std.json :as json]
            [std.string :as str]
            [std.lang :as l]
            [std.lang.base.impl :as impl]))

(l/script :postgres
  {:macro-only true})

(defmacro.pg grant-public
  "grants the schema to be in public"
  {:added "0.1"}
  [schema]
  `(do [:grant-usage-on-schema ~schema
        :to ~''[anon authenticated service_role]]
       [:grant-all-on-all-tables-in-schema ~schema :to ~''[anon authenticated service_role]]
       [:alter-default-privileges-for-role ~'postgres
        :in-schema ~schema
        :grant-all-on-tables-to 
        ~''[anon authenticated service_role]]))

(defmacro.pg revoke-execute-privileges-from-public
  "revokes public prvilages"
  {:added "0.1"}
  [schema]
  `[:alter-default-privileges-for-role ~'postgres
    :in-schema ~schema
    :revoke-execute-on-functions-from-public])

(defmacro.pg grant-usage
  "grants usage to a schema"
  {:added "0.1"}
  [schema & [roles]]
  `[:grant-usage-on-schema ~schema
    :to ~(or roles ''[anon authenticated service_role])])

(defmacro.pg grant-tables
  "grants table access to schema"
  {:added "0.1"}
  [schema & [roles]]
  `[:grant-all-on-all-tables-in-schema ~schema
    :to ~(or roles ''[anon authenticated service_role])])

(defmacro.pg grant-privileges
  "grants privileges to a schema"
  {:added "0.1"}
  [schema & [roles]]
  `[:alter-default-privileges-for-role ~'postgres
    :in-schema ~schema
    :grant-all-on-tables-to
    ~(or roles ''[anon authenticated service_role])])

(defmacro.pg grant-all
  "grants all acess to multiple schemas"
  {:added "0.1"}
  [schemas & [roles access]]
  (let [roles (or roles '[anon authenticated service_role])
        {:keys [usage
                tables
                privileges]
         :or {usage  true
              tables true
              privileges false}} access]
    (->> (map (fn [schema]
                (apply list 'do
                       (filter identity
                               [(when usage  (list `grant-usage schema (list 'quote roles)))
                                (when tables (list `grant-tables schema (list 'quote roles)))
                                (when privileges (list `grant-privileges schema (list 'quote roles)))])))
              schemas)
         (apply list 'do))))

(defmacro.pg
  auth-uid
  "returns the superbase uid"
  {:added "0.1"}
  []
  '(auth.uid))

(defmacro.pg
  auth-email
  "returns the superbase email"
  {:added "0.1"}
  []
  '(auth.email))

(defmacro.pg
  auth-role
  "returns the user role"
  {:added "0.1"}
  []
  '(auth.role))

(defmacro.pg
  auth-jwt
  "returns the superbase jtw"
  {:added "0.1"}
  []
  '(auth.jwt))

(defmacro.pg ^{:- [:boolean]}
  is-supabase
  "checks if supabase is installed"
  {:added "0.1"}
  []
  (list 'exists
        [:select 1 :from 'information_schema.schemata :where {:schema-name "auth"}]))


;;
;; transformations
;;


(defn transform-entry-defn
  [body {:keys [grammar
                entry
                mopts]
         :as env}]
  (let [{:sb/keys [grant]} (:api/meta (meta (:id entry)))
        function-form (list '.
                            #{(:static/schema entry)}
                            (apply list
                                   (with-meta (:id entry)
                                     {})
                                   (map :modifiers
                                        (:static/input entry))))]
    (cond grant
          (let [grant-str (impl/emit-direct
                           grammar
                           (list 'do
                                 [:revoke-all-on-function
                                  function-form
                                  :from-public]
                                 [:grant-execute-on-function
                                  function-form
                                  :to
                                  (case grant
                                    :all ''[anon authenticated service_role]
                                    :auth ''[authenticated service_role]
                                    :admin ''[service_role]
                                    grant)])
                           *ns*
                           mopts)]
            (str body "\n" grant-str))
          
          :else body)))

(defn transform-entry-deftype
  [body {:keys [grammar
                entry
                mopts]
         :as env}]
  (let [{:sb/keys [rls]} (:api/meta (meta (:id entry)))
        table-form (list '.
                         #{(:static/schema entry)}
                         #{(str (:id entry))})]
    (cond rls
          (let [rls-str (impl/emit-direct
                         grammar
                         [:alter-table table-form :enable-row-level-security]
                         *ns*
                         mopts)]
            (str body "\n" rls-str))
          
          :else body)))

(defn transform-entry
  [body {:keys [grammar
                entry
                mopts]
         :as env}]
  (case (:op-key entry)
    :defn    (transform-entry-defn body env)
    :deftype (transform-entry-deftype body env)
    body))


;;
;; api calls
;;

(defn api-call
  [{:keys [key
           host
           route
           method
           type
           headers
           auth]
    :or {host (System/getenv "DEFAULT_SUPABASE_API_ENDPOINT")
         method :post
         type :anon}}
   body]
  (let [key (or key
                (case type
                  :anon (System/getenv "DEFAULT_SUPABASE_API_KEY_ANON")
                  :service (System/getenv "DEFAULT_SUPABASE_API_KEY_SERVICE")
                  :public ""))
        headers-default (case type
                          :public {"Content-Type" "application/json"}
                          {"apikey" key
                           "Authorization" (str "Bearer " (or auth key))
                           "Content-Type" "application/json"})
        headers (merge
                 headers-default
                 headers)
        call-fn (case method
                  :delete http/delete
                  :get http/get
                  :post http/post)]
    (-> (call-fn (str host route)
                 {:headers headers
                  :body   (std.json/write body)})
        (update :body json/read)
        (select-keys [:status :body]))))

(defn api-rpc
  [{:keys [fn
           args]
    :as opts}]
  (let [{:keys [id]
         :static/keys [schema]} (deref fn)
        headers (if schema
                  {"Content-Profile" schema})
        route  (str "/rest/v1/rpc/" (str/snake-case (str id)))
        opts (merge opts
                    {:headers headers
                     :route route})]
    (api-call opts args)))

(defn api-select-all
  [fn & [opts]]
  (let [{:keys [id]
         :static/keys [schema]} (deref fn)
        headers (if schema
                  {"Content-Profile" schema})
        route  (str "/rest/v1/" id "?select=*")
        opts (merge opts
                    {:headers headers
                     :route route})]
    (api-call opts {})))

(defn api-signup
  [{:keys [email
           password]
    :as body}
   & [opts]]
  (api-call (merge opts
                   {:route "/auth/v1/signup"})
            body))

(defn api-signup-delete
  [id & [opts]]
  (api-call (merge opts
                   {:method :delete
                    :type :service
                    :route (str "/auth/v1/admin/users/" id)})
            {}))

