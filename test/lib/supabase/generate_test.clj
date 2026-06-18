(ns lib.supabase.generate-test
  (:use code.test)
  (:require [clojure.string :as str]
            [lib.supabase.generate :refer :all]))

^{:refer lib.supabase.generate/string->kebab :added "4.1"}
(fact "converts snake_case to kebab-case"
  (string->kebab "email_confirm") => "email-confirm"
  (string->kebab "user_metadata") => "user-metadata"
  (string->kebab "redirect_to")   => "redirect-to")

^{:refer lib.supabase.generate/entry->args-form :added "4.1"}
(fact "builds destructuring args from endpoint metadata"
  (entry->args-form (get lib.supabase.api/+admin+ "admin-create-user"))
  => (fn [v] (and (vector? v)
                  (= 'client (last v))
                  (some #(and (map? %)
                              (= 'body (:as %))
                              (contains? (set (:keys %)) 'email))
                        v)))

  (entry->args-form (get lib.supabase.api/+admin+ "admin-delete-user"))
  => (fn [v] (and (vector? v)
                  (= 'client (last v))
                  (some #(and (map? %)
                              (= 'path (:as %))
                              (contains? (set (:keys %)) 'user-id))
                        v)))

  (entry->args-form (get lib.supabase.api/+admin+ "verify-get"))
  => (fn [v] (and (vector? v)
                  (= 'client (last v))
                  (some #(and (map? %)
                              (= 'query (:as %))
                              (contains? (set (:keys %)) 'redirect-to))
                        v)))

  (entry->args-form (get lib.supabase.api/+admin+ "health"))
  => '[client])

^{:refer lib.supabase.generate/entry->input-form :added "4.1"}
(fact "builds call/call input map from endpoint metadata"
  (entry->input-form (get lib.supabase.api/+admin+ "admin-create-user"))
  => {:body '(map->snake body)
      :content-type "application/json"}

  (entry->input-form (get lib.supabase.api/+admin+ "admin-delete-user"))
  => {:path '(map->snake path)}

  (entry->input-form (get lib.supabase.api/+admin+ "verify-get"))
  => {:query '(map->snake query)}

  (entry->input-form (get lib.supabase.api/+admin+ "health"))
  => {})

^{:refer lib.supabase.generate/generate-admin-fn :added "4.1"}
(fact "emits a defn form for an admin endpoint"
  (generate-admin-fn ["admin-create-user" (get lib.supabase.api/+admin+ "admin-create-user")])
  => (fn [s] (str/includes? s "(defn admin-create-user"))

  (generate-admin-fn ["admin-create-user" (get lib.supabase.api/+admin+ "admin-create-user")])
  => (fn [s] (str/includes? s "(call/call (get api/+admin+ \"admin-create-user\")"))

  (generate-admin-fn ["admin-create-user" (get lib.supabase.api/+admin+ "admin-create-user")])
  => (fn [s] (and (str/includes? s "{:body (map->snake body)")
                  (str/includes? s ":content-type \"application/json\"}")))

  (generate-admin-fn ["admin-delete-user" (get lib.supabase.api/+admin+ "admin-delete-user")])
  => (fn [s] (str/includes? s "{:path (map->snake path)}"))

  (generate-admin-fn ["verify-get" (get lib.supabase.api/+admin+ "verify-get")])
  => (fn [s] (str/includes? s "{:query (map->snake query)}"))

  (generate-admin-fn ["health" (get lib.supabase.api/+admin+ "health")])
  => (fn [s] (and (str/includes? s "[client]")
                  (str/includes? s "{}"))))

^{:refer lib.supabase.generate/generate-common-file :added "4.1"}
(fact "emits a complete lib.supabase.common namespace"
  (generate-common-file)
  => (fn [s] (str/includes? s "(ns lib.supabase.common"))

  (generate-common-file)
  => (fn [s] (str/includes? s "(def ^:dynamic *default*)"))

  (generate-common-file)
  => (fn [s] (str/includes? s "(defn admin-create-user")))


^{:refer lib.supabase.generate/entry->path-keys :added "4.1"}
(fact "extracts path params as kebab-cased symbols"
  (entry->path-keys (get lib.supabase.api/+admin+ "admin-delete-user"))
  => '[user-id]

  (entry->path-keys (get lib.supabase.api/+admin+ "admin-get-user"))
  => '[user-id]

  (entry->path-keys (get lib.supabase.api/+admin+ "health"))
  => '[])

^{:refer lib.supabase.generate/entry->query-keys :added "4.1"}
(fact "extracts query params as kebab-cased symbols"
  (entry->query-keys (get lib.supabase.api/+admin+ "verify-get"))
  => '[type token email phone redirect-to]

  (entry->query-keys (get lib.supabase.api/+admin+ "authorize"))
  => '[redirect-to]

  (entry->query-keys (get lib.supabase.api/+admin+ "health"))
  => '[])

^{:refer lib.supabase.generate/entry->body-keys :added "4.1"}
(fact "extracts body property names as kebab-cased symbols"
  (entry->body-keys (get lib.supabase.api/+admin+ "admin-create-user"))
  => '[role aud ban-duration email-confirm email user-metadata app-metadata phone phone-confirm password]

  (entry->body-keys (get lib.supabase.api/+admin+ "recovery"))
  => '[email]

  (entry->body-keys (get lib.supabase.api/+admin+ "health"))
  => '[])

^{:refer lib.supabase.generate/entry->template-input :added "4.1"}
(fact "builds template parameters for an endpoint"
  (entry->template-input ["health" (get lib.supabase.api/+admin+ "health")])
  => (fn [m] (and (map? m)
                  (= 'health (get m 'fn-name))
                  (= "health" (get m 'operation-id))
                  (= '[client] (get m 'args-form))
                  (= {} (get m 'input-form))
                  (= 'client (get m 'client-sym))))

  (entry->template-input ["admin-create-user" (get lib.supabase.api/+admin+ "admin-create-user")])
  => (fn [m] (and (map? m)
                  (= 'admin-create-user (get m 'fn-name))
                  (= "admin-create-user" (get m 'operation-id))
                  (= 'client (get m 'client-sym))
                  (vector? (get m 'args-form))
                  (map? (get m 'input-form)))))

^{:refer lib.supabase.generate/generate-admin-functions :added "4.1"}
(fact "emits wrapper source for all admin endpoints"
  (generate-admin-functions)
  => (fn [s] (and (string? s)
                  (str/includes? s "(defn admin-create-user")
                  (str/includes? s "(defn admin-delete-user")
                  (str/includes? s "(defn verify-get")
                  (str/includes? s "(defn health")))

  (generate-admin-functions)
  => (fn [s] (str/includes? s "\n\n")))

^{:refer lib.supabase.generate/write-common-file! :added "4.1"}
(fact "writes generated source to the common namespace file"
  (let [calls (atom [])]
    (with-redefs [spit (fn [path content] (swap! calls conj [path content]))]
      (write-common-file!))
    @calls)
  => (fn [calls] (and (= 1 (count calls))
                      (= "src/lib/supabase/common.clj" (ffirst calls))
                      (string? (second (first calls)))
                      (str/includes? (second (first calls)) "(ns lib.supabase.common"))))