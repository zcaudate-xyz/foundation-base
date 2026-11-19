(ns dupasupa.server
  "Thin verified-session adapter; profile and ownership rules live in PostgreSQL DSL functions."
  (:require [lang.core :as l]))
(l/script :js
  {:import [["pg" :as [* postgres]] ["@supabase/supabase-js" :as #{createClient}]]
   :static {:export [GET POST GET_PROFILE POST_PROFILE GET_CONFIG]}})

(defn.js ^{:- [:async]} query
  [sql values]
  (when (not process.env.DUPASUPA_DATABASE_URL)
    (throw (new Error "Start Dupasupa with make serve to prepare its local database.")))
  (var client (new postgres.Client {"connectionString" process.env.DUPASUPA_DATABASE_URL}))
  (try (await (. client (connect)))
       (var result (await (. client (query sql values))))
       (return (. result.rows [0] ["value"]))
       (catch error (throw error))
       (finally (await (. client (end))))))

(defn.js ^{:- [:async]} verifiedUser
  [request]
  (var header (. request.headers (get "authorization")))
  (when (or (not header) (not (. header (startsWith "Bearer "))))
    (throw (Object.assign (new Error "Sign in to continue.") {"status" 401})))
  (var client
       (createClient process.env.DUPASUPA_SUPABASE_URL
                     process.env.DUPASUPA_SUPABASE_KEY
                     {"auth" {"persistSession" false "autoRefreshToken" false}}))
  (var response (await (. client.auth (getUser (. header (slice 7))))))
  (when (or response.error (not response.data.user))
    (throw (Object.assign (new Error "Sign in again.") {"status" 401})))
  (return response.data.user))

(defn.js failure
  [error]
  (var invalid (. (new RegExp "^(22|23)|^P0001$") (test (or error.code ""))))
  (var status (or error.status (:? invalid 400 500)))
  (return (Response.json {"error" (:? (< status 500) error.message "Request failed.")}
                         {"status" status})))

(defn.js ^{:- [:async]} GET
  [request]
  (try (var user (await (-/verifiedUser request)))
       (return (Response.json (await (-/query
                                      "SELECT dupasupa_rpc.list_owned_campaigns($1::uuid) AS value"
                                      [user.id]))))
       (catch error (return (-/failure error)))))
(defn.js ^{:- [:async]} POST
  [request]
  (try (var user (await (-/verifiedUser request)))
       (var values (await (. request (json))))
       (when (== values.action "create")
         (return (Response.json
                  (await (-/query
                          "SELECT dupasupa_rpc.create_owned_campaign($1::uuid,$2::text) AS value"
                          [user.id values.title]))
                  {"status" 201})))
       (when (and values.action (not= values.action "save"))
         (throw (Object.assign (new Error "Unknown campaign action.") {"status" 400})))
       (await (-/query
               "SELECT dupasupa_rpc.save_owned_campaign($1::uuid,$2::uuid,$3::text) AS value"
               [user.id values.id values.title]))
       (return (Response.json (await (-/query
                                      "SELECT dupasupa_rpc.list_owned_campaigns($1::uuid) AS value"
                                      [user.id]))))
       (catch error (return (-/failure error)))))
(defn.js ^{:- [:async]} GET_PROFILE
  [request]
  (try (var user (await (-/verifiedUser request)))
       (var profile
            (await (-/query "SELECT dupasupa_rpc.ret_profile($1::uuid) AS value" [user.id])))
       (when (not profile)
         (throw (Object.assign (new Error "Profile not found.") {"status" 404})))
       (return (Response.json profile))
       (catch error (return (-/failure error)))))
(defn.js ^{:- [:async]} POST_PROFILE
  [request]
  (try (var user (await (-/verifiedUser request)))
       (var values (await (. request (json))))
       (return (Response.json
                (await
                 (-/query
                  "SELECT dupasupa_rpc.save_profile($1::uuid,$2::text,$3::text,$4::text) AS value"
                  [user.id values.handle values.display_name values.bio]))))
       (catch error (return (-/failure error)))))
(defn.js GET_CONFIG
  []
  (when (or (not process.env.DUPASUPA_SUPABASE_URL) (not process.env.DUPASUPA_SUPABASE_KEY))
    (return (Response.json {"error" "Start Dupasupa with make serve to configure local Auth."}
                           {"status" 503})))
  (return (Response.json {"url" process.env.DUPASUPA_SUPABASE_URL
                          "key" process.env.DUPASUPA_SUPABASE_KEY
                          "accounts" (:? (== process.env.NODE_ENV "development")
                                         (JSON.parse (or process.env.DUPASUPA_DEV_ACCOUNTS "[]"))
                                         [])})))
