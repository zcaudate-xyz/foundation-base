(ns lib.supabase.api)
 
(def +admin+
  ;; (vec (net.openapi.read/read "resources/assets/lib.supabase/official/auth_v1_openapi.json" {:remove-empty? true}))
  (->> [["admin-create-user"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/admin/users",
          :tags ["admin"],
          :method :post,
          :operation-id "admin-create-user",
          :fn-name "admin-create-user",
          :summary "Returns the created user.",
          :body
          {:required false,
           :content-types [],
           :type "object",
           :properties
           {"role" {:type "string", :custom {"x-go-name" "Role"}},
            "aud" {:type "string", :custom {"x-go-name" "Aud"}},
            "ban_duration"
            {:type "string", :custom {"x-go-name" "BanDuration"}},
            "email_confirm"
            {:type "boolean", :custom {"x-go-name" "EmailConfirm"}},
            "email" {:type "string", :custom {"x-go-name" "Email"}},
            "user_metadata"
            {:type "object", :custom {"x-go-name" "UserMetaData"}},
            "app_metadata"
            {:type "object", :custom {"x-go-name" "AppMetaData"}},
            "phone" {:type "string", :custom {"x-go-name" "Phone"}},
            "phone_confirm"
            {:type "boolean", :custom {"x-go-name" "PhoneConfirm"}},
            "password"
            {:type "string", :custom {"x-go-name" "Password"}}},
           :custom {"x-go-package" "github.com/netlify/gotrue/api"}},
          :auth-names ["bearer"]}]
        ["admin-delete-user"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/admin/user/{user_id}",
          :tags ["admin"],
          :method :delete,
          :operation-id "admin-delete-user",
          :fn-name "admin-delete-user",
          :summary "Deletes a user.",
          :path-params
          [{:name "user_id",
            :required true,
            :description "The user's id"}],
          :body nil,
          :auth-names ["bearer"]}]
        ["admin-generate-link"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/admin/generate_link",
          :tags ["admin"],
          :method :post,
          :operation-id "admin-generate-link",
          :fn-name "admin-generate-link",
          :summary "Generates an email action link.",
          :body
          {:required false,
           :content-types [],
           :type "object",
           :properties
           {"data" {:type "object", :custom {"x-go-name" "Data"}},
            "email" {:type "string", :custom {"x-go-name" "Email"}},
            "new_email"
            {:type "string", :custom {"x-go-name" "NewEmail"}},
            "password" {:type "string", :custom {"x-go-name" "Password"}},
            "redirect_to"
            {:type "string", :custom {"x-go-name" "RedirectTo"}},
            "type" {:type "string", :custom {"x-go-name" "Type"}}},
           :custom {"x-go-package" "github.com/netlify/gotrue/api"}},
          :auth-names ["bearer"]}]
        ["admin-get-user"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/admin/user/{user_id}",
          :tags ["admin"],
          :method :get,
          :operation-id "admin-get-user",
          :fn-name "admin-get-user",
          :summary "Get a user.",
          :path-params
          [{:name "user_id",
            :required true,
            :description "The user's id"}],
          :body nil,
          :auth-names ["bearer"]}]
        ["admin-list-users"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/admin/users",
          :tags ["admin"],
          :method :get,
          :operation-id "admin-list-users",
          :fn-name "admin-list-users",
          :summary "List all users.",
          :body nil,
          :auth-names ["bearer"]}]
        ["admin-update-user"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/admin/user/{user_id}",
          :tags ["admin"],
          :method :put,
          :operation-id "admin-update-user",
          :fn-name "admin-update-user",
          :summary "Update a user.",
          :path-params
          [{:name "user_id",
            :required true,
            :description "The user's id"}],
          :body nil,
          :auth-names ["bearer"]}]
        ["authorize"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/authorize",
          :tags ["oauth"],
          :method :get,
          :operation-id "authorize",
          :fn-name "authorize",
          :summary
          "Redirects the user to the 3rd-party OAuth provider to start the OAuth1.0 or OAuth2.0 authentication process.",
          :query-params
          [{:name "redirect_to",
            :required false,
            :description
            "The redirect url to return the user to after the `/callback` endpoint has completed."}],
          :body nil,
          :auth-names []}]
        ["callback"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/callback",
          :tags ["oauth"],
          :method :get,
          :operation-id "callback",
          :fn-name "callback",
          :summary
          "Receives the redirect from an external provider during the OAuth authentication process. Starts the process of creating an access and refresh token.",
          :body nil,
          :auth-names []}]
        ["health"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/health",
          :tags ["health"],
          :method :get,
          :operation-id "health",
          :fn-name "health",
          :summary
          "The healthcheck endpoint for gotrue. Returns the current gotrue version.",
          :body nil,
          :auth-names []}]
        ["invite"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/invite",
          :tags ["invite"],
          :method :post,
          :operation-id "invite",
          :fn-name "invite",
          :summary "Sends an invite link to the user.",
          :body
          {:required false,
           :content-types [],
           :type "object",
           :description
           "InviteParams are the parameters the Signup endpoint accepts",
           :properties
           {"data" {:type "object", :custom {"x-go-name" "Data"}},
            "email" {:type "string", :custom {"x-go-name" "Email"}}},
           :custom {"x-go-package" "github.com/netlify/gotrue/api"}},
          :auth-names []}]
        ["logout"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/logout",
          :tags ["logout"],
          :method :post,
          :operation-id "logout",
          :fn-name "logout",
          :summary "Logs out the user.",
          :body nil,
          :auth-names ["bearer"]}]
        ["otp"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/otp",
          :tags ["otp"],
          :method :post,
          :operation-id "otp",
          :fn-name "otp",
          :summary "Passwordless sign-in method for email or phone.",
          :body
          {:required false,
           :content-types [],
           :type "object",
           :description
           "OtpParams contains the request body params for the otp endpoint",
           :properties
           {"create_user" {:type "boolean",
                           :custom {"x-go-name" "CreateUser"}},
            "data"        {:type "object", :custom {"x-go-name" "Data"}},
            "email"       {:type "string", :custom {"x-go-name" "Email"}},
            "phone"       {:type "string", :custom {"x-go-name" "Phone"}}},
           :custom {"x-go-package" "github.com/netlify/gotrue/api"}},
          :auth-names []}]
        ["recovery"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/recover",
          :tags ["recovery"],
          :method :post,
          :operation-id "recovery",
          :fn-name "recovery",
          :summary
          "Sends a password recovery email link to the user's email.",
          :body
          {:required false,
           :content-types [],
           :type "object",
           :description
           "RecoverParams holds the parameters for a password recovery request",
           :properties
           {"email" {:type "string", :custom {"x-go-name" "Email"}}},
           :custom {"x-go-package" "github.com/netlify/gotrue/api"}},
          :auth-names []}]
        ["settings"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/settings",
          :tags ["settings"],
          :method :get,
          :operation-id "settings",
          :fn-name "settings",
          :summary
          "Returns the configuration settings for the gotrue server.",
          :body nil,
          :auth-names []}]
        ["signup"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/signup",
          :tags ["signup"],
          :method :post,
          :operation-id "signup",
          :fn-name "signup",
          :summary "Password-based signup with either email or phone.",
          :body
          {:required false,
           :content-types [],
           :type "object",
           :description
           "SignupParams are the parameters the Signup endpoint accepts",
           :properties
           {"data" {:type "object", :custom {"x-go-name" "Data"}},
            "email" {:type "string", :custom {"x-go-name" "Email"}},
            "password" {:type "string", :custom {"x-go-name" "Password"}},
            "phone" {:type "string", :custom {"x-go-name" "Phone"}}},
           :custom {"x-go-package" "github.com/netlify/gotrue/api"}},
          :auth-names []}]
        ["token-password"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/token?grant_type=password",
          :tags ["token"],
          :method :post,
          :operation-id "token-password",
          :fn-name "token-password",
          :summary "Signs in a user with a password.",
          :body
          {:required false,
           :content-types [],
           :type "object",
           :description
           "PasswordGrantParams are the parameters the ResourceOwnerPasswordGrant method accepts",
           :properties
           {"email" {:type "string", :custom {"x-go-name" "Email"}},
            "password" {:type "string", :custom {"x-go-name" "Password"}},
            "phone" {:type "string", :custom {"x-go-name" "Phone"}}},
           :custom {"x-go-package" "github.com/netlify/gotrue/api"}},
          :auth-names []}]
        ["token-refresh"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/token?grant_type=refresh_token",
          :tags ["token"],
          :method :post,
          :operation-id "token-refresh",
          :fn-name "token-refresh",
          :summary "Refreshes a user's refresh token.",
          :body
          {:required false,
           :content-types [],
           :type "object",
           :description
           "RefreshTokenGrantParams are the parameters the RefreshTokenGrant method accepts",
           :properties
           {"refresh_token"
            {:type "string", :custom {"x-go-name" "RefreshToken"}}},
           :custom {"x-go-package" "github.com/netlify/gotrue/api"}},
          :auth-names []}]
        ["user-get"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/user",
          :tags ["user"],
          :method :get,
          :operation-id "user-get",
          :fn-name "user-get",
          :summary "Get information for the logged-in user.",
          :body nil,
          :auth-names ["bearer"]}]
        ["user-put"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/user",
          :tags ["user"],
          :method :put,
          :operation-id "user-put",
          :fn-name "user-put",
          :summary "Returns the updated user.",
          :body
          {:required false,
           :content-types [],
           :type "object",
           :description "UserUpdateParams parameters for updating a user",
           :properties
           {"app_metadata"
            {:type "object", :custom {"x-go-name" "AppData"}},
            "data" {:type "object", :custom {"x-go-name" "Data"}},
            "email" {:type "string", :custom {"x-go-name" "Email"}},
            "nonce" {:type "string", :custom {"x-go-name" "Nonce"}},
            "password" {:type "string", :custom {"x-go-name" "Password"}},
            "phone" {:type "string", :custom {"x-go-name" "Phone"}}},
           :custom {"x-go-package" "github.com/netlify/gotrue/api"}},
          :auth-names ["bearer"]}]
        ["verify-get"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/verify",
          :tags ["verify"],
          :method :get,
          :operation-id "verify-get",
          :fn-name "verify-get",
          :summary "Verifies a sign up.",
          :query-params
          [{:name "type", :required false, :description nil}
           {:name "token", :required false, :description nil}
           {:name "email", :required false, :description nil}
           {:name "phone", :required false, :description nil}
           {:name "redirect_to", :required false, :description nil}],
          :body nil,
          :auth-names []}]
        ["verify-post"
         {:description nil,
          :response-content-types ["application/json"],
          :path "/verify",
          :tags ["verify"],
          :method :post,
          :operation-id "verify-post",
          :fn-name "verify-post",
          :summary "Verifies a sign up.",
          :body
          {:required false,
           :content-types [],
           :type "object",
           :description
           "VerifyParams are the parameters the Verify endpoint accepts",
           :properties
           {"email" {:type "string", :custom {"x-go-name" "Email"}},
            "phone" {:type "string", :custom {"x-go-name" "Phone"}},
            "redirect_to"
            {:type "string", :custom {"x-go-name" "RedirectTo"}},
            "token" {:type "string", :custom {"x-go-name" "Token"}},
            "type" {:type "string", :custom {"x-go-name" "Type"}}},
           :custom {"x-go-package" "github.com/netlify/gotrue/api"}},
          :auth-names []}]]
       (into {})))
