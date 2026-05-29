(ns lib.supabase.admin-gen
  (:require [clojure.string :as string]
            [lib.supabase.template :as template]
            [std.block.template :as gen]))

(def +output-path+
  "src/lib/supabase/admin.clj")

(def ^:private HEADER_TEMPLATE
  "(ns ~ns-sym
  (:require [lib.supabase.common :as common]
            [lib.supabase.route :as route]))")

(def ^:private +header-template+
  (gen/get-template HEADER_TEMPLATE
                    (fn [{:keys [ns-sym]}]
                      {'ns-sym ns-sym})))

(def +entries+
  [{'fsym 'api-signup-create
    'doc "Creates a user through the auth admin API."
    'base-args '[body]
    'opts-args '[body opts]
    'body-form '(common/admin-call (route/route-request :admin/create-user opts)
                                   body)}
   {'fsym 'api-signup-delete
    'doc "Deletes a user through the auth admin API."
    'base-args '[uid]
    'opts-args '[uid opts]
    'body-form '(common/admin-call (route/route-request :admin/delete-user opts uid)
                                   {})}
   {'fsym 'list-users
    'doc "Lists auth users."
    'base-args '[client]
    'opts-args '[client opts]
    'body-form '(common/admin-call (route/route-request :admin/users
                                                         (merge opts {:client client}))
                                   {})}
   {'fsym 'get-user-by-id
    'doc "Fetches a user by id."
    'base-args '[client uid]
    'opts-args '[client uid opts]
    'body-form '(common/admin-call (route/route-request :admin/user-by-id
                                                         (merge opts {:client client})
                                                         uid)
                                   {})}
   {'fsym 'create-user
    'doc "Creates an auth user."
    'base-args '[client attrs]
    'opts-args '[client attrs opts]
    'body-form '(api-signup-create attrs (merge opts {:client client}))}
   {'fsym 'update-user
    'doc "Updates an auth user."
    'base-args '[client uid attrs]
    'opts-args '[client uid attrs opts]
    'body-form '(common/admin-call (route/route-request :admin/update-user
                                                         (merge opts {:client client})
                                                         uid)
                                   attrs)}
   {'fsym 'delete-user
    'doc "Deletes an auth user."
    'base-args '[client uid]
    'opts-args '[client uid opts]
    'body-form '(api-signup-delete uid (merge opts {:client client}))}
   {'fsym 'invite-user-by-email
    'doc "Invites a user by email."
    'base-args '[client attrs]
    'opts-args '[client attrs opts]
    'body-form '(common/auth-call (route/route-request :auth/invite
                                                        (merge opts {:client client}))
                                  attrs)}])

(defn render-entry
  [entry]
  (template/supabase-defn-string entry))

(defn render-admin-file
  "Renders the `lib.supabase.admin` namespace as plain `defn` forms."
  {:added "4.1.4"}
  ([] (render-admin-file 'lib.supabase.admin))
  ([ns-sym]
   (str (gen/fill-template +header-template+ {:ns-sym ns-sym})
        "\n\n"
        (string/join "\n\n" (map render-entry +entries+))
        "\n")))

(defn write-admin-file
  "Writes the generated `lib.supabase.admin` namespace to disk."
  {:added "4.1.4"}
  ([] (write-admin-file +output-path+ 'lib.supabase.admin))
  ([output-path ns-sym]
   (let [output (render-admin-file ns-sym)]
     (spit output-path output)
     output)))
