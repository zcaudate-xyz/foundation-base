(ns lib.supabase.admin
  (:require [lib.supabase.common :as common]
            [lib.supabase.route :as route]))

(defn api-signup-create
  "Creates a user through the auth admin API."
  {:added "4.1.4"}
  [body & [opts]]
  (common/api-call (route/route-request :admin/create-user opts)
                   body))

(defn api-signup-delete
  "Deletes a user through the auth admin API."
  {:added "4.1.4"}
  [uid & [opts]]
  (common/api-call (route/route-request :admin/delete-user opts uid)
                   {}))

(defn list-users
  "Lists auth users."
  {:added "4.1.4"}
  [client & [opts]]
  (common/api-call (route/route-request :admin/users
                                       (merge opts {:client client}))
                   {}))

(defn get-user-by-id
  "Fetches a user by id."
  {:added "4.1.4"}
  [client uid & [opts]]
  (common/api-call (route/route-request :admin/user-by-id
                                       (merge opts {:client client})
                                       uid)
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
  (common/api-call (route/route-request :admin/update-user
                                       (merge opts {:client client})
                                       uid)
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
  (common/api-call (route/route-request :auth/invite
                                       (merge opts {:client client}))
                   attrs))
