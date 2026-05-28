(ns lib.supabase.route-test
  (:use code.test)
  (:require [lib.supabase.route :refer :all]))

^{:refer lib.supabase.route/route-summary :added "4.1.4"}
(fact "returns the grouped Supabase route structure"
  (-> (route-summary)
      keys
      sort)
  => [:admin :auth :realtime :rest])

^{:refer lib.supabase.route/route-entry :added "4.1.4"}
(fact "returns a route entry by route id"
  (route-entry :auth/signup)
  => {:group :auth
      :method :post
      :args []
      :path "/signup"})

^{:refer lib.supabase.route/route-path :added "4.1.4"}
(fact "formats route paths with arguments"
  [(route-path :auth/sign-out "global")
   (route-path :admin/user-by-id "user-1")
   (route-path :rest/rpc "echo_name")]
  => ["/logout?scope=global"
      "/users/user-1"
      "/rpc/echo_name"])

^{:refer lib.supabase.route/route-method :added "4.1.4"}
(fact "returns route methods with :get as the default"
  [(route-method :auth/signup)
   (route-method :rest/table)]
  => [:post :get])

^{:refer lib.supabase.route/route-group :added "4.1.4"}
(fact "returns the route group from the route id"
  [(route-group :auth/signup)
   (route-group :admin/users)
   (route-group :rest/rpc)]
  => [:auth :admin :rest])

^{:refer lib.supabase.route/route-request :added "4.1.4"}
(fact "creates compact request options from route metadata"
  [(route-request :auth/signup {:client :client})
   (route-request :admin/delete-user {:client :client} "user-1")]
  => [{:group :auth
       :method :post
       :client :client
       :route "/signup"}
      {:group :admin
       :method :delete
       :type :service
       :client :client
       :route "/users/user-1"}])

^{:refer lib.supabase.route/root-path :added "4.1.4"}
(fact "returns the root path for a route group"
  [(root-path :auth)
   (root-path :admin)
   (root-path :rest)
   (root-path :realtime)]
  => ["/auth/v1" "/auth/v1/admin" "/rest/v1" "/realtime/v1"])

^{:refer lib.supabase.route/auth-route :added "4.1.4"}
(fact "formats auth routes"
  [(auth-route :signup)
   (auth-route :refresh-session)]
  => ["/signup" "/token?grant_type=refresh_token"])

^{:refer lib.supabase.route/admin-route :added "4.1.4"}
(fact "formats admin routes"
  [(admin-route :users)
   (admin-route :update-user "user-1")]
  => ["/users" "/users/user-1"])

^{:refer lib.supabase.route/rest-route :added "4.1.4"}
(fact "formats rest routes"
  [(rest-route :table "Entry")
   (rest-route :table-all "Entry")]
  => ["/Entry" "/Entry?select=*"])

^{:refer lib.supabase.route/realtime-route :added "4.1.4"}
(fact "formats realtime routes"
  (realtime-route :websocket)
  => "/websocket")
