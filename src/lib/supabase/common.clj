(ns lib.supabase.common
  (:require [clojure.string :as str]
            [net.http :as http]
            [std.json :as json]
            [std.lib.foundation :as f]
            [lib.supabase.api :as api]
            [net.openapi.call :as call]))

(def ^:dynamic *default*
  {})




;;
;; what I'd like to do is to read the openapi spec
;; 1. read the api spec and have a net.openapi.read/read function convert the

(comment
  (net.openapi.read/read-spec "resources/assets/lib.supabase/openapi.json")
  => {"create-user"
      {:fn-name "create-user"
       :method :post
       :path "/auth/v1/admin/users"
       :path-params []
       :query-params []
       :header-params []
       :body {:required true
              :fields ["email" "password" "email_confirm"]}}})


;; 2. net.openapi.call/call function that takes an entry and sets up the net.http client to 

(comment

  (net.openapi.call/call
   {:fn-name "create-user"
    :method :post
    :path "/auth/v1/admin/users"
    :path-params []
    :query-params []
    :header-params []
    :body {:required true
           :fields ["email" "password" "email_confirm"]}}
   {:path []
    :params {}
    :body   {"email" "email"
             "password" "password"
             "email_confirm" "email_confirm"}})
  )


;; 3. 
;; 
;;


;;
(def +routes+
  {"create-user"
   {:fn-name "create-user"
    :method :post
    :path "/auth/v1/admin/users"
    :path-params   []
    :query-params   []
    :header-params []
    :body {:required true
           :fields ["email" "password" "email_confirm"]}}})



(comment
  (filter
   (fn [[k v]]
     (some (fn [k]
             (not-empty (get v k)))
           
           [:cookie-params
            :query-params
            :header-params
            :path-params]),
     )
   lib.supabase.api/+admin+)
  
  )

(get lib.supabase.api/+admin+ "admin-create-user")

(comment
  (template/)

  
  (defn admin-create-user
    [{:keys [email password email-confirm]
      :as body}
     client   ;; schema, host, port etc
     ]
    (call/call (get lib.supabase.api/+admin+ "admin-create-user")
               {:body body}
               lib.supabase.common/*default))
  
  (defn verify-get
    [{:keys [type email token phone redirect-to]
      :as query}
     entry]
    (call/call entry
               {:query query}
               lib.supabase.common/*default*)


    ))
