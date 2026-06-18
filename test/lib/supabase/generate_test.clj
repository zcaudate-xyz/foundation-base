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
