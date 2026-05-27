(ns postgres.core.supabase
  (:require [clojure.string]
            [hara.model.spec-postgres.common :as common]
            [lib.supabase :as supabase]
            [postgres.core.addon :as addon]
            [hara.lang :as l]
            [hara.lang.impl :as impl]
            [std.lib.context.pointer :as ptr]
            [std.lib.foundation :as f]
            [std.json :as json]))

(l/script :postgres
  {:macro-only true})

(defmacro.pg create-role
  "creates a role"
  {:added "4.0"}
  [role]
  (pop (common/block-do-suppress
        [:create-role role \;])))

(defmacro.pg alter-role-bypassrls
  "alters role to bypass rls"
  {:added "4.0"}
  [role]
  (pop (common/block-do-suppress
        [:alter-role role :bypassrls \;])))

(defmacro.pg grant-public
  "grants public access to schema"
  {:added "4.0"}
  [schema]
  `(do [:grant-usage-on-schema ~schema
        :to ~''[anon authenticated service_role]]
       [:grant-all-on-all-tables-in-schema ~schema :to ~''[anon authenticated service_role]]
       [:alter-default-privileges-for-role ~'postgres
        :in-schema ~schema
        :grant-all-on-tables-to 
        ~''[anon authenticated service_role]]))

(defmacro.pg revoke-execute-privileges-from-public
  "revotes execute privileges"
  {:added "4.0"}
  [schema]
  `[:alter-default-privileges-for-role ~'postgres
    :in-schema ~schema
    :revoke-execute-on-functions-from-public])

(defmacro.pg grant-usage
  "grants usage on a schema"
  {:added "4.0"}
  [schema & [roles]]
  `[:grant-usage-on-schema ~schema
    :to ~(or roles ''[anon authenticated service_role])])

(defmacro.pg grant-tables
  "grants table access on a schema"
  {:added "4.0"}
  [schema & [roles]]
  `[:grant-all-on-all-tables-in-schema ~schema
    :to ~(or roles ''[anon authenticated service_role])])

(defmacro.pg grant-privileges
  "grants privileges on a schema"
  {:added "4.0"}
  [schema & [roles]]
  `[:alter-default-privileges-for-role ~'postgres
    :in-schema ~schema
    :grant-all-on-tables-to
    ~(or roles ''[anon authenticated service_role])])

(defmacro.pg grant-all
  "grants privileges on a schema"
  {:added "4.0"}
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
  "calls auth.uid()"
  {:added "4.0"}
  []
  '(auth.uid))

(defmacro.pg
  auth-email
  "calls auth.email()"
  {:added "4.0"}
  []
  '(auth.email))

(defmacro.pg
  auth-role
  "calls auth.role()"
  {:added "4.0"}
  []
  '(auth.role))

(defmacro.pg
  auth-jwt
  "calls auth.jwt()"
  {:added "4.0"}
  []
  '(auth.jwt))

(defmacro.pg ^{:- [:boolean]}
  is-supabase
  "checks that supabase is installed"
  {:added "4.0"}
  []
  (list 'exists
        [:select 1 :from 'information_schema.schemata :where {:schema-name "auth"}]))

(defmacro.pg
  raise
  "raises an error"
  {:added "4.0"}
  ([message detail
    & [{:keys [http-status
               http-headers
               state]}]]
   (filter identity
           (list 'do
                 (if http-status
                   [:perform (list 'set-config "response.status" (str http-status) true)])
                 (if http-headers
                   [:perform (list 'set-config "response.headers"
                                   (std.json/write http-headers) true)])
                 [:raise (or state :exception) :using
                  (list 'quote [['detail := (list '% detail)]
                                ['message := message]])]))))

(defmacro.pg ^{:- [:block]}
  show-roles
  "show supabase role information"
  {:added "4.0"}
  []
  [:select ''[rolname rolsuper rolbypassrls rolcanlogin]
   :from  'pg_roles
   :where 'rolname :in ''("authenticated"
                          "service_role"
                          "anon")])

(defn request-event
  "Returns the canonical xt.db event name for an xt.db request."
  {:added "4.1.4"}
  [request]
  (cond (contains? request "db/sync")
        "db/sync"

        (contains? request "db/remove")
        "db/remove"

        :else
        nil))

(defn resolve-literal
  "Resolves literal data carried directly or via a bound var."
  {:added "4.1.4"}
  [form]
  (cond (or (map? form)
            (vector? form)
            (string? form)
            (number? form)
            (boolean? form)
            (nil? form))
        form

        (symbol? form)
        (let [v (resolve form)]
          (if (and v
                   (bound? v))
            @v
            form))

        :else
        form))

(defn normalize-realtime-payload
  "Normalizes literal map payloads into postgres jsonb forms."
  {:added "4.1.4"}
  [payload]
  (let [payload (resolve-literal payload)]
    (if (map? payload)
      (with-meta payload (assoc (meta payload) :js true))
      payload)))

(defmacro.pg
  realtime-send
  "calls Supabase realtime.send with payload, event, topic, and privacy flag"
  {:added "4.1.4"}
  ([topic event payload]
   `(s/realtime-send ~topic ~event ~payload false))
  ([topic event payload private?]
   (list 'realtime.send
         (normalize-realtime-payload payload)
         event
         topic
         private?)))

(defmacro.pg
  realtime-send-request
  "broadcasts a native xt.db request through Supabase realtime.send"
  {:added "4.1.4"}
  ([topic request]
   `(s/realtime-send-request ~topic ~request false))
  ([topic request private?]
   (let [request (resolve-literal request)
         event (request-event request)]
     (when-not event
       (f/error "Unsupported xt.db realtime request"
                {:request request}))
     (list 'realtime.send
           (normalize-realtime-payload request)
           event
           topic
           private?))))

(defn process-return
  "processes the return value"
  {:added "4.0"}
  [ret]
  (if (= ret "")
    nil
    ret))

(defn get-form-type
  "gets the form type"
  {:added "4.0"}
  [form]
  (cond (string? form)
        :text

        (integer? form)
        :bigint
        
        (number? form)
        :numeric

        (map? form)
        :jsonb

        (:type (meta form))
        (:type (meta form))

        (list? form)
        (let [fsym (first form)]
          (cond (keyword? fsym)
                fsym

                (symbol? fsym)
                (let [fvar (resolve fsym)]
                  (cond (ptr/pointer? (deref fvar))
                        (let [ret  (or (:static/type @@fvar)
                                       (:static/return @@fvar))]
                          (if (or (nil? ret)
                                  (= ret [:block]))
                            (f/error "Cannot determine type"
                                     {:form form})
                            (first ret)))
                        
                        :else
                        (f/error "Cannot determine type"
                                 {:form form})))))
        
        
        :else
        (f/error "Cannot determine type"
                 {:form form})))

(defmacro with-role-single
  "executes a statement with role (single)"
  {:added "4.0"}
  [[role type] form]
  (let [role (case role
               :admin 'service_role
               :auth  'authenticated
               :anon  'anon
               role)
        type (or type (get-form-type form))]
    (list `process-return
          (list '!.pg
                (list 'try
                      [:set-local-role role]
                      (list 'let [(list type 'out)  form]
                            (list 'return 'out))
                      (list 'catch 'others
                            (list 'return {:code 'SQLSTATE
                                           :message 'SQLERRM})
                            #_#_
                            (list 'rt.postgres/get-stack-diagnostics)
                            (list 'return {:code    'e_code
                                           :message 'e_msg})))))))

(defmacro with-role
  "executes a statement with role"
  {:added "4.0"}
  [[role type] & forms]
  (let [res (mapv (fn [form]
                    (list `with-role-single
                          [role type]
                          form))
                  forms)]
    (cond (= 1 (count forms))
          (first res)

          :else
          res)))

(defmacro with-auth-single
  "executes a statement with auth (single)"
  {:added "4.0"}
  [[user-id type] form]
  (let [type (or type (get-form-type form))]
    (list `process-return
          (list '!.pg
                (list 'try
                      [:set-local-role 'authenticated]
                      [:perform (list 'set-config
                                      "request.jwt.claim.sub"
                                      (list :text user-id)
                                      true)]
                      (list 'let [(list type 'out)  form]
                            (list 'return 'out))
                      (list 'catch 'others
                            (list 'return {:code 'SQLSTATE
                                           :message 'SQLERRM})))))))

(defmacro with-auth
  "executes a statement with auth"
  {:added "4.0"}
  [[user-id type] & forms]
  (let [res (mapv (fn [form]
                    (list `with-auth-single
                          [user-id type]
                          form))
                  forms)]
    (cond (= 1 (count forms))
          (first res)

          :else
          res)))

(defmacro with-super-single
  "executes a statement with super (single)"
  {:added "4.0"}
  [[user-id type] form]
  (let [type (or type (get-form-type form))]
    (list `process-return
          (list '!.pg
                (list 'try
                      [:set-local-role 'authenticated]
                      [:perform (list 'set-config
                                      "request.jwt.claim.sub"
                                      user-id
                                      true)]
                      [:perform (list 'set-config
                                      "request.jwt.claims"
                                      (list :text {:sub user-id
                                                   :user_metadata {:super true}})
                                      true)]
                      (list 'let [(list type 'out)  form]
                            (list 'return 'out))
                      (list 'catch 'others
                            (list 'return {:code 'SQLSTATE
                                           :message 'SQLERRM})))))))

(defmacro with-super
  "executes a statement with super"
  {:added "4.0"}
  [[user-id type] & forms]
  (let [res (mapv (fn [form]
                    (list `with-super-single
                          [user-id type]
                          form))
                  forms)]
    (cond (= 1 (count forms))
          (first res)

          :else
          res)))




;;
;; transformations
;;

(defn transform-entry-defn
  "transforms a defn entry"
  {:added "4.0"}
  [body {:keys [grammar
                entry
                mopts]
         :as env}]
  (let [{:sb/keys [grant]} (:api/meta entry)
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
  "transforms a deftype"
  {:added "4.0"}
  [body {:keys [grammar
                entry
                mopts]
         :as env}]
  (let [{:sb/keys [rls
                   access]} (:api/meta entry)
        table-form (list '.
                         #{(:static/schema entry)}
                         #{(str (:id entry))})
        rls-str    (if rls
                     (impl/emit-direct
                      grammar
                      [:alter-table table-form :enable-row-level-security \;]
                      *ns*
                      mopts))
        access-str (if access
                     (impl/emit-direct
                      grammar
                      (apply list 'do
                             (keep (fn [[role operations]]
                                    (let [operations (cond (vector? operations)
                                                           (list 'quote (mapv (comp symbol f/strn) operations))

                                                           (= :all operations)
                                                           ''[select update delete insert]

                                                           (= :none operations)
                                                           :skip
                                                           
                                                           :else operations)
                                          role (case role
                                                 :admin 'service_role
                                                 :auth  'authenticated
                                                 :anon  'anon)]
                                      (when-not (= :skip operations)
                                        [:grant operations :on-table table-form :to role])))
                                  access))
                      *ns*
                      mopts))]
    (->> [body rls-str access-str]
         (filter identity)
         (clojure.string/join "\n"))))

(defn transform-entry
  "transforms a book entry"
  {:added "4.0"}
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

(def ^{:added "4.0"} api-call supabase/api-call)
(def ^{:added "4.0"} api-rpc supabase/api-rpc)
(def ^{:added "4.0"} api-select-all supabase/api-select-all)
(def ^{:added "4.0"} api-signup supabase/api-signup)
(def ^{:added "4.0"} api-signin supabase/api-signin)
(def ^{:added "4.0"} api-signup-create supabase/api-signup-create)
(def ^{:added "4.0"} api-signup-delete supabase/api-signup-delete)
(def ^{:added "4.0"} api-impersonate supabase/api-impersonate)
