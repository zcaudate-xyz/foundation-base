(ns rt.postgres.supabase-test
  (:use code.test)
  (:require [rt.postgres.supabase :as s]))

^{:refer rt.postgres.supabase/create-role :added "4.0"}
(fact "creates a role"
  ^:hidden

  (s/create-role 'anon)
  => "DO $$\nBEGIN\n  CREATE ROLE anon;\nEXCEPTION WHEN OTHERS THEN\nEND;\n$$ LANGUAGE 'plpgsql'")

^{:refer rt.postgres.supabase/grant-public :added "4.0"}
(fact "grants public access to schema"
  ^:hidden

  (s/grant-public #{"core/fn-util"})
  => "GRANT USAGE ON SCHEMA \"core/fn-util\" TO anon,authenticated,service_role;\nGRANT ALL ON ALL TABLES IN SCHEMA \"core/fn-util\" TO anon,authenticated,service_role;\nALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA \"core/fn-util\" GRANT ALL ON TABLES TO anon,authenticated,service_role;")

^{:refer rt.postgres.supabase/revoke-execute-privileges-from-public :added "4.0"}
(fact "revotes execute privileges"
  ^:hidden

  (s/revoke-execute-privileges-from-public #{"core/fn-util"})
  => "ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA \"core/fn-util\" REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC")

^{:refer rt.postgres.supabase/grant-usage :added "4.0"}
(fact "grants usage on a schema"
  ^:hidden
  
  (s/grant-usage #{"core/fn-util"})
  => "GRANT USAGE ON SCHEMA \"core/fn-util\" TO anon,authenticated,service_role")

^{:refer rt.postgres.supabase/grant-tables :added "4.0"}
(fact "grants table access on a schema"
  ^:hidden
  
  (s/grant-tables #{"core/fn-util"})
  => "GRANT ALL ON ALL TABLES IN SCHEMA \"core/fn-util\" TO anon,authenticated,service_role")

^{:refer rt.postgres.supabase/grant-privileges :added "4.0"}
(fact "grants privileges on a schema"
  ^:hidden
  
  (s/grant-privileges #{"core/fn-util"})
  => "ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA \"core/fn-util\" GRANT ALL ON TABLES TO anon,authenticated,service_role")

^{:refer rt.postgres.supabase/grant-all :added "4.0"}
(fact "grants privileges on a schema"
  ^:hidden
  
  (s/grant-all [#{"core/fn-util"}
                #{"core/fn-core"}])
  => "GRANT USAGE ON SCHEMA \"core/fn-util\" TO anon,authenticated,service_role;\nGRANT ALL ON ALL TABLES IN SCHEMA \"core/fn-util\" TO anon,authenticated,service_role;\nGRANT USAGE ON SCHEMA \"core/fn-core\" TO anon,authenticated,service_role;\nGRANT ALL ON ALL TABLES IN SCHEMA \"core/fn-core\" TO anon,authenticated,service_role;")

^{:refer rt.postgres.supabase/auth-uid :added "4.0"}
(fact "calls auth.uid()"
  ^:hidden

  (s/auth-uid)
  => "auth.uid()")

^{:refer rt.postgres.supabase/auth-email :added "4.0"}
(fact "calls auth.email()"
  ^:hidden

  (s/auth-email)
  => "auth.email()")

^{:refer rt.postgres.supabase/auth-role :added "4.0"}
(fact "calls auth.role()"
  ^:hidden

  (s/auth-role)
  => "auth.role()")

^{:refer rt.postgres.supabase/auth-jwt :added "4.0"}
(fact "calls auth.jwt()"
  ^:hidden

  (s/auth-jwt)
  => "auth.jwt()")

^{:refer rt.postgres.supabase/is-supabase :added "4.0"}
(fact "checks that supabase is installed"
  ^:hidden

  (s/is-supabase)
  => "exists(\n  SELECT 1 FROM information_schema.schemata WHERE \"schema_name\" = 'auth'\n)")

^{:refer rt.postgres.supabase/raise :added "4.0"}
(fact "raises an error"
  ^:hidden

  (s/raise "Hello" {:data 1})
  => "RAISE EXCEPTION USING detail = jsonb_build_object('data',1),message = 'Hello';")

^{:refer rt.postgres.supabase/show-roles :added "4.0"}
(fact "show supabase role information"
  ^:hidden

  (s/show-roles)
  => "SELECT rolname,rolsuper,rolbypassrls,rolcanlogin FROM pg_roles WHERE rolname IN ('authenticated','service_role','anon')")

^{:refer rt.postgres.supabase/get-form-type :added "4.0"}
(fact "gets the form type"
  ^:hidden

  (s/get-form-type '(:text s))
  => :text

  (s/get-form-type "hello")
  => :text

  (s/get-form-type 1.01)
  => :numeric)

^{:refer rt.postgres.supabase/with-role :added "4.0"}
(fact
 "executes a statement with role"
 (macroexpand-1 ' (s/with-role [anon :integer] (+ 1 2 3)))
 => '(!.pg
      (try
        [:set-local-role anon]
        (let [(:integer out) (+ 1 2 3)]
          (return out))
        (catch others (return {:code SQLSTATE, :message SQLERRM})))))

^{:refer rt.postgres.supabase/with-auth :added "4.0"}
(fact "TODO"

  (macroexpand-1
   '(s/with-auth ["00000000-0000-0000-0000-000000000000"
                  :integer]
      (+ 1 2 3)))
  => '(!.pg
       (try
         [:set-local-role authenticated]
         [:perform
          (set-config
           "request.jwt.claim.sub"
           "00000000-0000-0000-0000-000000000000"
           true)]
         (let [(:integer out) (+ 1 2 3)]
           (return out))
         (catch others (return {:code SQLSTATE, :message SQLERRM})))))

^{:refer rt.postgres.supabase/transform-entry-defn :added "4.0"}
(fact "transforms a defn entry")

^{:refer rt.postgres.supabase/transform-entry-deftype :added "4.0"}
(fact "transforms a deftype")

^{:refer rt.postgres.supabase/transform-entry :added "4.0"}
(fact "transforms a book entry")

^{:refer rt.postgres.supabase/api-call :added "4.0"}
(fact "calls an api"
  ^:hidden
  
  (api-call {} {})
  => {:status 404, :body {"message" "no Route matched with those values"}})

^{:refer rt.postgres.supabase/api-rpc :added "4.0"}
(fact "calls the rpc")

^{:refer rt.postgres.supabase/api-select-all :added "4.0"}
(fact "does a select all call")

^{:refer rt.postgres.supabase/api-signup :added "4.0"}
(fact "sign up via supabase api")

^{:refer rt.postgres.supabase/api-signin :added "4.0"}
(fact "sign in via supabase api")

^{:refer rt.postgres.supabase/api-signup-delete :added "4.0"}
(fact "remove user via supabase api")

^{:refer rt.postgres.supabase/api-impersonate :added "4.0"}
(fact "inpersonates a user")
