(ns postgres.core.supabase-test
  (:require [clojure.string :as str]
            [hara.model.spec-postgres :as grammar]
            [lib.supabase.support :as support]
            [postgres.core.supabase :as s]
            [hara.lang :as l])
  (:use code.test))

(l/script- :postgres
  {:require [[postgres.core :as pg]
             [postgres.core.supabase :as s]
             [postgres.sample.scratch-v1 :as scratch]]})

(fact:global
  {:setup [(support/start!)]
   :teardown [(support/stop!)]})

^{:refer postgres.core.supabase/create-role :added "4.0"}
(fact "creates a role"

  (l/emit-as :postgres
             `[(s/create-role ~'anon)])
  => "DO $$\nBEGIN\n  CREATE ROLE anon;\nEXCEPTION WHEN OTHERS THEN\nEND;\n$$ LANGUAGE 'plpgsql'")

^{:refer postgres.core.supabase/alter-role-bypassrls :added "4.0"}
(fact "alters role to bypass rls"

  (l/emit-as :postgres `[(s/alter-role-bypassrls ~'role)])
  => "DO $$\nBEGIN\n  ALTER ROLE role BYPASSRLS;\nEXCEPTION WHEN OTHERS THEN\nEND;\n$$ LANGUAGE 'plpgsql'")

^{:refer postgres.core.supabase/grant-public :added "4.0"}
(fact "grants public access to schema"

  (l/emit-as :postgres `[(s/grant-public "core/fn-util")])
  => "GRANT USAGE ON SCHEMA 'core/fn-util' TO anon,authenticated,service_role;\nGRANT ALL ON ALL TABLES IN SCHEMA 'core/fn-util' TO anon,authenticated,service_role;\nALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA 'core/fn-util' GRANT ALL ON TABLES TO anon,authenticated,service_role;")

^{:refer postgres.core.supabase/revoke-execute-privileges-from-public :added "4.0"}
(fact "revotes execute privileges"

  (l/emit-as :postgres `[(s/revoke-execute-privileges-from-public "core/fn-util")])
  => "ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA 'core/fn-util' REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC")

^{:refer postgres.core.supabase/grant-usage :added "4.0"}
(fact "grants usage on a schema"

  (l/emit-as :postgres `[(s/grant-usage "core/fn-util")])
  => "GRANT USAGE ON SCHEMA 'core/fn-util' TO anon,authenticated,service_role")

^{:refer postgres.core.supabase/grant-tables :added "4.0"}
(fact "grants table access on a schema"

  (l/emit-as :postgres `[(s/grant-tables "core/fn-util")])
  => "GRANT ALL ON ALL TABLES IN SCHEMA 'core/fn-util' TO anon,authenticated,service_role")

^{:refer postgres.core.supabase/grant-privileges :added "4.0"}
(fact "grants privileges on a schema"

  (l/emit-as :postgres `[(s/grant-privileges "core/fn-util")])
  => "ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA 'core/fn-util' GRANT ALL ON TABLES TO anon,authenticated,service_role")

^{:refer postgres.core.supabase/grant-all :added "4.0"}
(fact "grants privileges on a schema"

  (l/emit-as :postgres `[(s/grant-all ["core/fn-util" "core/fn-core"])])
  => "GRANT USAGE ON SCHEMA 'core/fn-util' TO anon,authenticated,service_role;\nGRANT ALL ON ALL TABLES IN SCHEMA 'core/fn-util' TO anon,authenticated,service_role;\nGRANT USAGE ON SCHEMA 'core/fn-core' TO anon,authenticated,service_role;\nGRANT ALL ON ALL TABLES IN SCHEMA 'core/fn-core' TO anon,authenticated,service_role;")

^{:refer postgres.core.supabase/auth-uid :added "4.0"}
(fact "calls auth.uid()"

  (l/emit-as :postgres `[(s/auth-uid)])
  => "auth.uid()")

^{:refer postgres.core.supabase/auth-email :added "4.0"}
(fact "calls auth.email()"

  (l/emit-as :postgres `[(s/auth-email)])
  => "auth.email()")

^{:refer postgres.core.supabase/auth-role :added "4.0"}
(fact "calls auth.role()"

  (l/emit-as :postgres `[(s/auth-role)])
  => "auth.role()")

^{:refer postgres.core.supabase/auth-jwt :added "4.0"}
(fact "calls auth.jwt()"

  (l/emit-as :postgres `[(s/auth-jwt)])
  => "auth.jwt()")

^{:refer postgres.core.supabase/is-supabase :added "4.0"}
(fact "checks that supabase is installed"

  (l/emit-as :postgres `[(s/is-supabase)])
  => "exists(\n  SELECT 1 FROM information_schema.schemata WHERE \"schema_name\" = 'auth'\n)")

^{:refer postgres.core.supabase/raise :added "4.0"}
(fact "raises an error"

  (l/emit-as :postgres `[(s/raise "Hello" {:data 1})])
  => "RAISE EXCEPTION USING detail = jsonb_build_object('data',1),message = 'Hello';")

^{:refer postgres.core.supabase/show-roles :added "4.0"}
(fact "show supabase role information"

  (l/emit-as :postgres `[(s/show-roles)])
  => "SELECT rolname,rolsuper,rolbypassrls,rolcanlogin FROM pg_roles WHERE rolname IN ('authenticated','service_role','anon')")

^{:refer postgres.core.supabase/request-event :added "4.1.4"}
(fact "returns canonical xt.db request event names"
  [(s/request-event {"db/sync" {"Entry" []}})
   (s/request-event {"db/remove" {"Entry" ["id-1"]}})
   (s/request-event {"db/query" {"Entry" []}})]
  => ["db/sync" "db/remove" nil])

^{:refer postgres.core.supabase/realtime-send :added "4.1.4"}
(fact "emits a supabase realtime send helper"

  (let [sql (l/emit-as :postgres
                       `[(s/realtime-send
                          "room:test"
                          "db/sync"
                          {"db/sync" {"Entry" []}}
                          false)])]
    [(str/includes? sql "realtime.send(")
     (str/includes? sql "'db/sync'")
     (str/includes? sql "'room:test'")
     (str/includes? sql "jsonb_build_object")])
  => [true true true true])

^{:refer postgres.core.supabase/realtime-send-request :added "4.1.4"}
(fact "emits an xt.db request as a supabase realtime send"

  (let [sql (l/emit-as :postgres
                       `[(s/realtime-send-request
                          "room:test"
                          {"db/sync" {"Entry" []}}
                          false)])]
    [(str/includes? sql "realtime.send(")
     (str/includes? sql "'db/sync'")
     (str/includes? sql "'room:test'")
     (str/includes? sql "jsonb_build_object")])
  => [true true true true])

^{:refer postgres.core.supabase/process-return :added "4.0"}
(fact "processes the return value"

  (s/process-return "val") => "val"
  (s/process-return "") => nil)

^{:refer postgres.core.supabase/get-form-type :added "4.0"}
(fact "gets the form type"

  (s/get-form-type '(:text s))
  => :text

  (s/get-form-type "hello")
  => :text

  (s/get-form-type 1.01)
  => :numeric)

^{:refer postgres.core.supabase/with-role-single :added "4.0"}
(fact "executes a statement with role (single)"

  (macroexpand-1 `(s/with-role-single [anon :integer] (+ 1 2 3)))
  => '(postgres.core.supabase/process-return (!.pg (try [:set-local-role postgres.core.supabase-test/anon] (let [(:integer out) (clojure.core/+ 1 2 3)] (return out)) (catch others (return {:code SQLSTATE, :message SQLERRM}))))))

^{:refer postgres.core.supabase/with-role :added "4.0"}
(fact "executes a statement with role"

  (macroexpand-1 ` (s/with-role [anon :integer] (+ 1 2 3)))
  => '(postgres.core.supabase/with-role-single [postgres.core.supabase-test/anon :integer] (clojure.core/+ 1 2 3)))

^{:refer postgres.core.supabase/with-auth-single :added "4.0"}
(fact "executes a statement with auth (single)"

  (macroexpand-1 `(s/with-auth-single ["user" :integer] (+ 1 2 3)))
  => '(postgres.core.supabase/process-return (!.pg (try [:set-local-role authenticated] [:perform (set-config "request.jwt.claim.sub" (:text "user") true)] (let [(:integer out) (clojure.core/+ 1 2 3)] (return out)) (catch others (return {:code SQLSTATE, :message SQLERRM}))))))

^{:refer postgres.core.supabase/with-auth :added "4.0"}
(fact "executes a statement with auth"

  (macroexpand-1
   `(s/with-auth ["00000000-0000-0000-0000-000000000000"
                  :integer]
      (+ 1 2 3)))
  => '(postgres.core.supabase/with-auth-single ["00000000-0000-0000-0000-000000000000" :integer] (clojure.core/+ 1 2 3)))

^{:refer postgres.core.supabase/with-super-single :added "4.0"}
(fact "executes a statement with super (single)"

  (macroexpand-1 `(s/with-super-single ["user" :integer] (+ 1 2 3)))
  => '(postgres.core.supabase/process-return (!.pg (try [:set-local-role authenticated] [:perform (set-config "request.jwt.claim.sub" "user" true)] [:perform (set-config "request.jwt.claims" (:text {:sub "user", :user_metadata {:super true}}) true)] (let [(:integer out) (clojure.core/+ 1 2 3)] (return out)) (catch others (return {:code SQLSTATE, :message SQLERRM}))))))

^{:refer postgres.core.supabase/with-super :added "4.0"}
(fact "executes a statement with super"

  (macroexpand-1 `(s/with-super ["user" :integer] (+ 1 2 3)))
  => '(postgres.core.supabase/with-super-single ["user" :integer] (clojure.core/+ 1 2 3)))

^{:refer postgres.core.supabase/transform-entry-defn :added "4.0"}
(fact "transforms a defn entry"

  ;; Transform logic
  (s/transform-entry-defn "BODY" {:grammar (l/grammar :postgres)
                                  :entry {:id `myschema/myfunc
                                          :static/schema "myschema"
                                          :static/input []}
                                  :api/meta {:grant :all}
                                  :mopts {}})
  => "BODY")

^{:refer postgres.core.supabase/transform-entry-deftype :added "4.0"}
(fact "transforms a deftype"

  ;; Transform logic
  (s/transform-entry-deftype "BODY" {:grammar (l/grammar :postgres)
                                     :entry {:id `myschema/mytable
                                             :static/schema "myschema"}
                                     :api/meta {:rls true :access :all}
                                     :mopts {}})
  => "BODY")

^{:refer postgres.core.supabase/transform-entry :added "4.0"}
(fact "transforms a book entry"

  (s/transform-entry "BODY" {:grammar (l/grammar :postgres)
                             :entry {:op-key :defn
                                     :id `myschema/myfunc
                                     :static/schema "myschema"
                                     :static/input []}
                             :api/meta {:grant :all}})
  => "BODY")

^{:refer postgres.core.supabase/api-call :added "4.0"}
(fact "calls a live api route"
  (let [message (support/random-log-message)
        _ (support/seed-log! message)]
    (try
      (= message
         (->> (s/api-call (merge (support/anon-opts)
                                 {:group :rest
                                  :method :get
                                  :route "/Log?select=message"
                                  :headers {"Content-Profile" support/+scratch-v0-schema+}})
                          {})
              :body
              (filter #(= message (support/log-message %)))
              first
              (support/log-message)))
      (finally
        (support/clear-log! message))))
  => true)

^{:refer postgres.core.supabase/api-call :added "4.0"}
(fact "throws when api key is missing"
  ^:hidden
  
  (s/api-call {} {})
  => (throws clojure.lang.ExceptionInfo "Supabase API key not configured"))

^{:refer postgres.core.supabase/api-call :added "4.0"}
(fact "throws on supabase api errors"
  ^:hidden
  
  (s/api-call {:host (support/base-url)
               :key "bad-key"
               :route "/auth/v1/user"
               :method :get}
              {})
  => (throws clojure.lang.ExceptionInfo "Supabase API request failed"))

^{:refer postgres.core.supabase/api-rpc :added "4.0"}
(fact "calls a public scratch-v0 ping rpc"
  (-> (s/api-rpc (merge (support/anon-opts)
                        {:fn support/+ping-fn+}))
      :body)
  => "pong")

^{:refer postgres.core.supabase/api-select-all :added "4.0"}
(fact "selects scratch-v0 log rows"
  (let [message (support/random-log-message)
        _ (support/seed-log! message)]
    (try
      (= message
         (->> (s/api-select-all support/+log-table+ (support/anon-opts))
              :body
              (filter #(= message (support/log-message %)))
              first
              (support/log-message)))
      (finally
        (support/clear-log! message))))
  => true)

^{:refer postgres.core.supabase/api-signup :added "4.0"}
(fact "sign up via supabase api"
 
  (let [email (support/random-email)
        password "pass123456"
        signed-up (s/api-signup {"email" email
                                 "password" password}
                                (support/anon-opts))
        uid (or (get-in signed-up [:body "user" "id"])
                (get-in signed-up [:body "id"]))]
    (try
      [(some? uid)
       (= email (get-in signed-up [:body "user" "email"]))]
      (finally
        (support/delete-auth-user! uid))))
  => [true true])

^{:refer postgres.core.supabase/api-signin :added "4.0"}
(fact "sign in via supabase api"
 
  (let [{:keys [uid signed-in]} (support/create-auth-session!)]
    (try
      [(string? (get-in signed-in [:body "access_token"]))
       (string? (get-in signed-in [:body "user" "email"]))]
      (finally
        (support/delete-auth-user! uid))))
  => [true true])

^{:refer postgres.core.supabase/api-signup-create :added "4.0"}
(fact "create user via supabase api"
 
  (let [{:keys [uid created]} (support/create-auth-user!)]
    (try
      [(some? uid)
       (= 200 (:status created))]
      (finally
        (support/delete-auth-user! uid))))
  => [true true])

^{:refer postgres.core.supabase/api-signup-delete :added "4.0"}
(fact "remove user via supabase api"
 
  (let [{:keys [uid]} (support/create-auth-user!)]
    [(= 200 (:status (s/api-signup-delete uid (support/service-opts))))
     (try
       (s/api-call (merge (support/service-opts)
                          {:group :admin
                           :method :get
                           :route (str "/users/" uid)})
                   {})
       false
       (catch clojure.lang.ExceptionInfo e
         (= 404 (:status (ex-data e)))) )])
  => [true true])

^{:refer postgres.core.supabase/api-impersonate :added "4.0"}
(fact "inpersonates a user"
 
  (let [{:keys [uid]} (support/create-auth-user!)]
    (try
      (s/api-impersonate uid (support/service-opts))
      (finally
        (support/delete-auth-user! uid))))
  => (throws clojure.lang.ExceptionInfo "Supabase API request failed"))

^{:refer postgres.core.supabase/api-rpc :added "4.0"}
(fact "allows authenticated users to append logs through scratch-v0"
  (let [{:keys [uid access-token]} (support/create-auth-session!)
        message (support/random-log-message)]
    (try
      (let [response (s/api-rpc (merge (support/auth-opts access-token)
                                       {:fn support/+log-append-fn+
                                        :args {"i_message" message}}))]
        [(:status response)
         (= message
            (->> (support/list-logs)
                 (filter #(= message (support/log-message %)))
                 first
                 (support/log-message)))])
      (finally
        (support/clear-log! message)
        (support/delete-auth-user! uid))))
  => [200 true])

^{:refer postgres.core.supabase/api-rpc :added "4.0"}
(fact "rejects anonymous users for scratch-v0 log append"
  (let [message (support/random-log-message)]
    (try
      (s/api-rpc (merge (support/anon-opts)
                        {:fn support/+log-append-fn+
                         :args {"i_message" message}}))
      (finally
        (support/clear-log! message))))
  => (throws clojure.lang.ExceptionInfo "Supabase API request failed"))
