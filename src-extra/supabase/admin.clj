(ns lib.supabase.admin
  (:require [lib.supabase.common :as common]
            [lib.supabase.route :as route]))

(defn api-signup-create
      "Creates a user through the auth admin API."
      ([body]
       (api-signup-create body {}))
      ([body opts]
       (common/admin-call (route/route-request :admin/create-user opts) body)))

(defn api-signup-delete
      "Deletes a user through the auth admin API."
      ([uid]
       (api-signup-delete uid {}))
      ([uid opts]
       (common/admin-call (route/route-request :admin/delete-user opts uid) {})))

(defn list-users
      "Lists auth users."
      ([client]
       (list-users client {}))
      ([client opts]
       (common/admin-call (route/route-request :admin/users (merge opts {:client client})) {})))

(defn get-user-by-id
      "Fetches a user by id."
      ([client uid]
       (get-user-by-id client uid {}))
      ([client uid opts]
       (common/admin-call (route/route-request :admin/user-by-id (merge opts {:client client}) uid) {})))

(defn create-user
      "Creates an auth user."
      ([client attrs]
       (create-user client attrs {}))
      ([client attrs opts]
       (api-signup-create attrs (merge opts {:client client}))))

(defn update-user
      "Updates an auth user."
      ([client uid attrs]
       (update-user client uid attrs {}))
      ([client uid attrs opts]
       (common/admin-call (route/route-request :admin/update-user (merge opts {:client client}) uid) attrs)))

(defn delete-user
      "Deletes an auth user."
      ([client uid]
       (delete-user client uid {}))
      ([client uid opts]
       (api-signup-delete uid (merge opts {:client client}))))

(defn invite-user-by-email
      "Invites a user by email."
      ([client attrs]
       (invite-user-by-email client attrs {}))
      ([client attrs opts]
       (common/auth-call (route/route-request :auth/invite (merge opts {:client client})) attrs)))
