(ns dupasupa.auth
  (:require [lang.core :as l]))
(l/script :js
  {:import [["react" :as React] ["./auth-context.js" :as #{AuthContext}]
            ["@supabase/supabase-js" :as #{createClient}]
            ["../lib/devtools/devtools.mjs" :as #{FrameDevtoolProvider FrameDevtoolPanel}]
            ["tamagui" :as #{TamaguiProvider createTamagui}]
            ["@tamagui/config/v4" :as #{defaultConfig}]]
   :static {:export [AuthProvider]}})
(def.js toolConfig (createTamagui defaultConfig))
(defn.js AuthProvider
  [props]
  (var [session setSession] (React.useState nil))
  (var [client setClient] (React.useState nil))
  (var [accounts setAccounts] (React.useState []))
  (var [inspection setInspection] (React.useState {}))
  (var [error setError] (React.useState ""))
  (var [ready setReady] (React.useState false))
  (var current (React.useRef nil))
  (var initialized (React.useRef false))
  (React.useEffect
   (fn []
     (var active true)
     (var subscription nil)
     (var api nil)
     (. (fetch "/api/auth/config" {"cache" "no-store"})
        (then (fn [response] (return (. response (json)))))
        (then
         (async
          (fn [config]
            (when (not active)
              (return))
            (when config.error
              (throw (new Error config.error)))
            (:= api (createClient config.url config.key {"auth" {"storageKey" "dupasupa-auth"}}))
            (setClient api)
            (setAccounts (or config.accounts []))
            (:= subscription
                (. (. api.auth
                      (onAuthStateChange (fn [_ value]
                                           (when (and active initialized.current)
                                             (when (not= (:? current.current current.current.user.id nil)
                                                         (:? value value.user.id nil))
                                               (setInspection {}))
                                             (:= current.current value)
                                             (setSession value)))))
                   data
                   subscription))
            (var result (await (. api.auth (getSession))))
            (when result.error
              (throw result.error))
            (when result.data.session
              (var verified (await (. api.auth (getUser result.data.session.access_token))))
              (when verified.error
                (await (. api.auth (signOut {"scope" "local"})))
                (:= result.data.session nil)))
            (:= initialized.current true)
            (when active
              (:= current.current result.data.session)
              (setSession result.data.session)
              (setReady true)))))
        (catch (fn [failure]
                 (when active
                   (setError failure.message)
                   (setReady true)))))
     (return (fn []
               (:= active false)
               (when subscription
                 (. subscription (unsubscribe)))
               (when api
                 (. api.auth (stopAutoRefresh))))))
   [])
  (var request
       (async (fn [path method body]
                (var token (:? current.current current.current.access_token nil))
                (when (not token)
                  (throw (new Error "Sign in to continue.")))
                (var response
                     (await (fetch path
                                   {"method" method
                                    "cache" "no-store"
                                    "headers" {"content-type" "application/json"
                                               "authorization" (+ "Bearer " token)}
                                    "body" (:? body (JSON.stringify body) undefined)})))
                (var value (await (. response (json))))
                (when (not response.ok)
                  (throw (new Error (or value.error "Request failed."))))
                (return value))))
  (return
   (React.createElement
    AuthContext.Provider
    {"value" {"session" session "ready" ready "request" request "inspect" setInspection}}
    (React.createElement
     FrameDevtoolProvider
     {"client" client
      "accounts" accounts
      "store" (:? session inspection.store {})
      "queries" (:? session inspection.queries {})
      "context" {"application" "dupasupa" "ready" ready "error" error}
      "ui" {"actor" (:? session session.user.id nil)}}
     (React.createElement "nav"
                          {"className" "app-nav"}
                          (React.createElement "a" {"href" "/web-main"} "Campaigns")
                          (React.createElement "a" {"href" "/profile"} "Profile"))
     (React.createElement "div" {"key" (:? session session.user.id "signed-out")} props.children)
     (:? (and ready (== process.env.NODE_ENV "development"))
         (React.createElement TamaguiProvider
                              {"config" toolConfig "defaultTheme" "light"}
                              (React.createElement FrameDevtoolPanel))
         nil)))))
