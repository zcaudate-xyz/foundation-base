(ns dupasupa.pages
  "Application page declarations shared by production bindings and the workbench."
  (:require [dupasupa.seed :as seed]
            [dupasupa.config :as config]
            [app.manage.postgres :as postgres]))

(defn operation
  [rpc write?]
  (let [spec (postgres/rpc-spec config/DATABASE rpc)]
    {"operation" (get spec "id") "operation-spec" (assoc spec "write" write?)}))

(defn definitions
  []
  [{:id "campaigns"
    :routes ["/" "/web-main"]
    :area "member"
    :kind "screen"
    :group-id "web-main/campaigns"
    :models {"rows" (merge (operation seed/list-owned-campaigns false)
                           {"kind" "dataview"
                            "table" "Campaign"
                            "transport" {"method" "GET" "path" "/api/campaigns"}})}
    :actions {"create" (assoc (operation seed/create-owned-campaign true)
                              "transport"
                              {"method" "POST"
                               "path" "/api/campaigns"
                               "constants" {"action" "create"}
                               "bindings" {"title" "i_title"}})
              "save" (assoc (operation seed/save-owned-campaign true)
                            "transport"
                            {"method" "POST"
                             "path" "/api/campaigns"
                             "constants" {"action" "save"}
                             "bindings" {"id" "i_id" "title" "i_title"}})}
    :order ["rows"]
    :target "next"
    :runtime
    "Generated Substrate campaign model → verified-session Next handlers → local PostgreSQL. Each scenario owns a disposable database and Auth users."}
   {:id "profile"
    :routes ["/profile"]
    :area "member"
    :kind "screen"
    :models {"profile" (assoc (operation seed/ret-profile false)
                              "transport"
                              {"method" "GET" "path" "/api/profile"})}
    :actions {"save" (assoc (operation seed/save-profile true)
                            "transport"
                            {"method" "POST"
                             "path" "/api/profile"
                             "bindings"
                             {"handle" "handle" "display_name" "display_name" "bio" "bio"}})}
    :target "next"
    :runtime
    "Profile reads use the verified-session Next handler. Profile mutations are tested in the DB/UI scaffold; not yet available in this workbench."}])

(def page-index definitions)
