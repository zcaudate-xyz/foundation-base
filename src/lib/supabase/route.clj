(ns lib.supabase.route)

(def +roots+
  {:auth "/auth/v1"
   :admin "/auth/v1/admin"
   :rest "/rest/v1"
   :realtime "/realtime/v1"})

(def +routes+
  {:auth/signup           {:group :auth
                           :method :post
                           :args []
                           :path "/signup"}
   :auth/signin-password  {:group :auth
                           :method :post
                           :args []
                           :path "/token?grant_type=password"}
   :auth/refresh-session  {:group :auth
                           :method :post
                           :args []
                           :path "/token?grant_type=refresh_token"}
   :auth/impersonate      {:group :auth
                           :method :post
                           :type :service
                           :args []
                           :path "/token?grant_type=impersonate"}
   :auth/sign-out         {:group :auth
                           :method :post
                           :args [:scope]
                           :path "/logout?scope=%s"}
   :auth/invite           {:group :auth
                           :method :post
                           :type :service
                           :args []
                           :path "/invite"}

   :admin/users           {:group :admin
                           :method :get
                           :type :service
                           :args []
                           :path "/users"}
   :admin/create-user     {:group :admin
                           :method :post
                           :type :service
                           :args []
                           :path "/users"}
   :admin/user-by-id      {:group :admin
                           :method :get
                           :type :service
                           :args [:uid]
                           :path "/users/%s"}
   :admin/update-user     {:group :admin
                           :method :put
                           :type :service
                           :args [:uid]
                           :path "/users/%s"}
   :admin/delete-user     {:group :admin
                           :method :delete
                           :type :service
                           :args [:uid]
                           :path "/users/%s"}

   :rest/table            {:group :rest
                           :method :get
                           :args [:table]
                           :path "/%s"}
   :rest/table-all        {:group :rest
                           :method :get
                           :args [:table]
                           :path "/%s?select=*"}
   :rest/rpc              {:group :rest
                           :method :post
                           :args [:fn-name]
                           :path "/rpc/%s"}

   :realtime/websocket    {:group :realtime
                           :method :get
                           :args []
                           :path "/websocket"}})

(defn route-group
  "Returns the route group for a route id."
  {:added "4.1.4"}
  [route-id]
  (:group (get +routes+ route-id)))

(defn route-summary
  "Returns the grouped Supabase route summary."
  {:added "4.1.4"}
  []
  (reduce-kv (fn [out route-id entry]
               (assoc-in out [(:group entry) (keyword (name route-id))] entry))
             {}
             +routes+))

(defn route-entry
  "Returns a route entry by route id."
  {:added "4.1.4"}
  [route-id]
  (get +routes+ route-id))

(defn route-path
  "Formats a route path for a route id."
  {:added "4.1.4"}
  [route-id & args]
  (let [{:keys [path]} (route-entry route-id)]
    (apply format path args)))

(defn route-method
  "Returns the default HTTP method for a route id."
  {:added "4.1.4"}
  [route-id]
  (or (:method (route-entry route-id))
      :get))

(defn route-request
  "Returns request options for a route id, merging any overrides."
  {:added "4.1.4"}
  [route-id opts & route-args]
  (let [{:keys [path group] :as entry} (route-entry route-id)]
    (merge (dissoc entry :path :args)
           opts
           {:group group
            :route (apply format path route-args)})))

(defn root-path
  "Returns the root path prefix for a route group."
  {:added "4.1.4"}
  [group]
  (get +roots+ group))

(defn auth-route
  "Formats an auth route path."
  {:added "4.1.4"}
  [route-key & args]
  (apply route-path (keyword "auth" (name route-key)) args))

(defn admin-route
  "Formats an admin route path."
  {:added "4.1.4"}
  [route-key & args]
  (apply route-path (keyword "admin" (name route-key)) args))

(defn rest-route
  "Formats a PostgREST route path."
  {:added "4.1.4"}
  [route-key & args]
  (apply route-path (keyword "rest" (name route-key)) args))

(defn realtime-route
  "Formats a realtime route path."
  {:added "4.1.4"}
  [route-key & args]
  (apply route-path (keyword "realtime" (name route-key)) args))
