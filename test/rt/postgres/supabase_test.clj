(ns rt.postgres.supabase-test
  (:use code.test)
  (:require [rt.postgres.supabase :as s]
            [std.lang :as l]
            [net.http :as http]
            [rt.postgres.grammar :as grammar]))

(l/script- :postgres
  {:require [[rt.postgres :as pg]
             [rt.postgres.supabase :as s]
             [rt.postgres.script.scratch :as scratch]]})

^{:refer rt.postgres.supabase/create-role :added "4.0"}
(fact "creates a role"
  ^:hidden
  
  (l/emit-as :postgres
             `[(s/create-role ~'anon)])
  => "DO $$\nBEGIN\n  CREATE ROLE anon;\nEXCEPTION WHEN OTHERS THEN\nEND;\n$$ LANGUAGE 'plpgsql'")

^{:refer rt.postgres.supabase/alter-role-bypassrls :added "4.0"}
(fact "alters role to bypass rls"
  (l/emit-as :postgres '[(s/alter-role-bypassrls 'role)])
  => "DO $$\nBEGIN\n  ALTER ROLE role BYPASSRLS;\nEXCEPTION WHEN OTHERS THEN\nEND;\n$$ LANGUAGE 'plpgsql'")

^{:refer rt.postgres.supabase/grant-public :added "4.0"}
(fact "grants public access to schema"
  (l/emit-as :postgres '[(s/grant-public "core/fn-util")])
  => "GRANT USAGE ON SCHEMA \"core/fn-util\" TO anon,authenticated,service_role;\nGRANT ALL ON ALL TABLES IN SCHEMA \"core/fn-util\" TO anon,authenticated,service_role;\nALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA \"core/fn-util\" GRANT ALL ON TABLES TO anon,authenticated,service_role;")

^{:refer rt.postgres.supabase/revoke-execute-privileges-from-public :added "4.0"}
(fact "revotes execute privileges"
  (l/emit-as :postgres '[(s/revoke-execute-privileges-from-public "core/fn-util")])
  => "ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA \"core/fn-util\" REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC")

^{:refer rt.postgres.supabase/grant-usage :added "4.0"}
(fact "grants usage on a schema"
  (l/emit-as :postgres '[(s/grant-usage "core/fn-util")])
  => "GRANT USAGE ON SCHEMA \"core/fn-util\" TO anon,authenticated,service_role")

^{:refer rt.postgres.supabase/grant-tables :added "4.0"}
(fact "grants table access on a schema"
  (l/emit-as :postgres '[(s/grant-tables "core/fn-util")])
  => "GRANT ALL ON ALL TABLES IN SCHEMA \"core/fn-util\" TO anon,authenticated,service_role")

^{:refer rt.postgres.supabase/grant-privileges :added "4.0"}
(fact "grants privileges on a schema"
  (l/emit-as :postgres '[(s/grant-privileges "core/fn-util")])
  => "ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA \"core/fn-util\" GRANT ALL ON TABLES TO anon,authenticated,service_role")

^{:refer rt.postgres.supabase/grant-all :added "4.0"}
(fact "grants privileges on a schema"
  (l/emit-as :postgres '[(s/grant-all ["core/fn-util" "core/fn-core"])])
  => "GRANT USAGE ON SCHEMA \"core/fn-util\" TO anon,authenticated,service_role;\nGRANT ALL ON ALL TABLES IN SCHEMA \"core/fn-util\" TO anon,authenticated,service_role;\nGRANT USAGE ON SCHEMA \"core/fn-core\" TO anon,authenticated,service_role;\nGRANT ALL ON ALL TABLES IN SCHEMA \"core/fn-core\" TO anon,authenticated,service_role;")

^{:refer rt.postgres.supabase/auth-uid :added "4.0"}
(fact "calls auth.uid()"
  (l/emit-as :postgres '[(s/auth-uid)])
  => "auth.uid()")

^{:refer rt.postgres.supabase/auth-email :added "4.0"}
(fact "calls auth.email()"
  (l/emit-as :postgres '[(s/auth-email)])
  => "auth.email()")

^{:refer rt.postgres.supabase/auth-role :added "4.0"}
(fact "calls auth.role()"
  (l/emit-as :postgres '[(s/auth-role)])
  => "auth.role()")

^{:refer rt.postgres.supabase/auth-jwt :added "4.0"}
(fact "calls auth.jwt()"
  (l/emit-as :postgres '[(s/auth-jwt)])
  => "auth.jwt()")

^{:refer rt.postgres.supabase/is-supabase :added "4.0"}
(fact "checks that supabase is installed"
  (l/emit-as :postgres '[(s/is-supabase)])
  => "exists(\n  SELECT 1 FROM information_schema.schemata WHERE \"schema_name\" = 'auth'\n)")

^{:refer rt.postgres.supabase/raise :added "4.0"}
(fact "raises an error"
  (l/emit-as :postgres '[(s/raise "Hello" {:data 1})])
  => "RAISE EXCEPTION USING detail = jsonb_build_object('data',1),message = 'Hello';")

^{:refer rt.postgres.supabase/show-roles :added "4.0"}
(fact "show supabase role information"
  (l/emit-as :postgres '[(s/show-roles)])
  => "SELECT rolname,rolsuper,rolbypassrls,rolcanlogin FROM pg_roles WHERE rolname IN ('authenticated','service_role','anon')")

^{:refer rt.postgres.supabase/process-return :added "4.0"}
(fact "processes the return value"
  (s/process-return "val") => "val"
  (s/process-return "") => nil)

^{:refer rt.postgres.supabase/get-form-type :added "4.0"}
(fact "gets the form type"
  ^:hidden

  (s/get-form-type '(:text s))
  => :text

  (s/get-form-type "hello")
  => :text

  (s/get-form-type 1.01)
  => :numeric)

^{:refer rt.postgres.supabase/with-role-single :added "4.0"}
(fact "executes a statement with role (single)"
  (macroexpand-1 '(s/with-role-single [anon :integer] (+ 1 2 3)))
  => (contains [list?] :in-any))

^{:refer rt.postgres.supabase/with-role :added "4.0"}
(fact
 "executes a statement with role"
 (macroexpand-1 ' (s/with-role [anon :integer] (+ 1 2 3)))
 => (contains [list?] :in-any))

^{:refer rt.postgres.supabase/with-auth-single :added "4.0"}
(fact "executes a statement with auth (single)"
  (macroexpand-1 '(s/with-auth-single ["user" :integer] (+ 1 2 3)))
  => (contains [list?] :in-any))

^{:refer rt.postgres.supabase/with-auth :added "4.0"}
(fact "executes a statement with auth"
  (macroexpand-1
   '(s/with-auth ["00000000-0000-0000-0000-000000000000"
                  :integer]
      (+ 1 2 3)))
  => (contains [list?] :in-any))

^{:refer rt.postgres.supabase/with-super-single :added "4.0"}
(fact "executes a statement with super (single)"
  (macroexpand-1 '(s/with-super-single ["user" :integer] (+ 1 2 3)))
  => (contains [list?] :in-any))

^{:refer rt.postgres.supabase/with-super :added "4.0"}
(fact "executes a statement with super"
  (macroexpand-1 '(s/with-super ["user" :integer] (+ 1 2 3)))
  => (contains [list?] :in-any))

^{:refer rt.postgres.supabase/transform-entry-defn :added "4.0"}
(fact "transforms a defn entry"
  ;; Transform logic
  (s/transform-entry-defn "BODY" {:grammar +postgres+
                                  :entry {:id 'myschema/myfunc
                                          :static/schema "myschema"
                                          :static/input []}
                                  :api/meta {:grant :all}
                                  :mopts {}})
  => string?)

^{:refer rt.postgres.supabase/transform-entry-deftype :added "4.0"}
(fact "transforms a deftype"
  ;; Transform logic
  (s/transform-entry-deftype "BODY" {:grammar +postgres+
                                     :entry {:id 'myschema/mytable
                                             :static/schema "myschema"}
                                     :api/meta {:rls true :access :all}
                                     :mopts {}})
  => string?)

^{:refer rt.postgres.supabase/transform-entry :added "4.0"}
(fact "transforms a book entry"
  (s/transform-entry "BODY" {:grammar +postgres+
                             :entry {:op-key :defn
                                     :id 'myschema/myfunc
                                     :static/schema "myschema"
                                     :static/input []}
                             :api/meta {:grant :all}})
  => string?)

^{:refer rt.postgres.supabase/api-call :added "4.0"}
(fact "calls an api"
  (with-redefs [http/post (fn [_ _] {:status 200 :body "{\"ok\":true}"})]
    (s/api-call {:key "key"} {}))
  => {:status 200 :body {"ok" true}})

^{:refer rt.postgres.supabase/api-rpc :added "4.0"}
(fact "calls the rpc"
  ;; api-call wrapper
  (with-redefs [http/post (fn [_ _] {:status 200 :body "{\"ok\":true}"})]
    (s/api-rpc {:fn (atom {:id 'rpc/func :static/schema "rpc"}) :key "key"}))
  => {:status 200 :body {"ok" true}})

^{:refer rt.postgres.supabase/api-select-all :added "4.0"}
(fact "does a select all call"
  ;; api-call wrapper
  (with-redefs [http/get (fn [_ _] {:status 200 :body "[{\"id\":1}]"})]
    (s/api-select-all (atom {:id 'table :static/schema "public"}) {:key "key"}))
  => {:status 200 :body [{"id" 1}]})

^{:refer rt.postgres.supabase/api-signup :added "4.0"}
(fact "sign up via supabase api"
  ;; api-call wrapper
  (with-redefs [http/post (fn [_ _] {:status 200 :body "{\"user\":{}}"})]
    (s/api-signup {:email "a@a.com" :password "pass"} {:key "key"}))
  => {:status 200 :body {"user" {}}})

^{:refer rt.postgres.supabase/api-signin :added "4.0"}
(fact "sign in via supabase api"
  ;; api-call wrapper
  (with-redefs [http/post (fn [_ _] {:status 200 :body "{\"token\":\"abc\"}"})]
    (s/api-signin {:email "a@a.com" :password "pass"} {:key "key"}))
  => {:status 200 :body {"token" "abc"}})

^{:refer rt.postgres.supabase/api-signup-create :added "4.0"}
(fact "create user via supabase api"
  ;; api-call wrapper
  (with-redefs [http/post (fn [_ _] {:status 200 :body "{\"user\":{}}"})]
    (s/api-signup-create {:email "a@a.com" :password "pass"} {:key "key"}))
  => {:status 200 :body {"user" {}}})

^{:refer rt.postgres.supabase/api-signup-delete :added "4.0"}
(fact "remove user via supabase api"
  ;; api-call wrapper
  (with-redefs [http/delete (fn [_ _] {:status 200 :body "{}"})]
    (s/api-signup-delete "uid" {:key "key"}))
  => {:status 200 :body {}})

^{:refer rt.postgres.supabase/api-impersonate :added "4.0"}
(fact "inpersonates a user"
  ;; api-call wrapper
  (with-redefs [http/post (fn [_ _] {:status 200 :body "{\"token\":\"imp\"}"})]
    (s/api-impersonate "uid" {:key "key"}))
  => {:status 200 :body {"token" "imp"}})
