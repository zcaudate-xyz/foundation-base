(ns lib.supabase.admin
  (:require [lib.supabase.common :as common]))

(defn api-signup-create
  "Creates a user through the auth admin API."
  {:added "4.1.4"}
  [body & [opts]]
  (common/api-call (merge opts
                          {:route "/auth/v1/admin/users"
                           :type :service})
                   body))

(defn api-signup-delete
  "Deletes a user through the auth admin API."
  {:added "4.1.4"}
  [uid & [opts]]
  (common/api-call (merge opts
                          {:method :delete
                           :route (str "/auth/v1/admin/users/" uid)
                           :type :service})
                   {}))

(defn list-users
  "Lists auth users."
  {:added "4.1.4"}
  [client & [opts]]
  (common/api-call (merge opts
                          {:client client
                           :method :get
                           :route "/auth/v1/admin/users"
                           :type :service})
                   {}))

(defn get-user-by-id
  "Fetches a user by id."
  {:added "4.1.4"}
  [client uid & [opts]]
  (common/api-call (merge opts
                          {:client client
                           :method :get
                           :route (str "/auth/v1/admin/users/" uid)
                           :type :service})
                   {}))

(defn create-user
  "Creates an auth user."
  {:added "4.1.4"}
  [client attrs & [opts]]
  (api-signup-create attrs (merge opts {:client client})))

(defn update-user
  "Updates an auth user."
  {:added "4.1.4"}
  [client uid attrs & [opts]]
  (common/api-call (merge opts
                          {:client client
                           :method :put
                           :route (str "/auth/v1/admin/users/" uid)
                           :type :service})
                   attrs))

(defn delete-user
  "Deletes an auth user."
  {:added "4.1.4"}
  [client uid & [opts]]
  (api-signup-delete uid (merge opts {:client client})))

(defn invite-user-by-email
  "Invites a user by email."
  {:added "4.1.4"}
  [client attrs & [opts]]
  (common/api-call (merge opts
                          {:client client
                           :route "/auth/v1/invite"
                           :type :service})
                   attrs))
