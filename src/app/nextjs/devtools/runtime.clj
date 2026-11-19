(ns app.nextjs.devtools.runtime
  "Devtool bindings only. The host supplies its existing Supabase client, accounts and inspection state."
  (:require [lang.core :as l]))
(l/script :js
  {:require [[js.react :as r]]})

(defn.js snapshot
  "Produces a bounded inspection snapshot without credentials or circular object references."
  [input]
  (var seen (new WeakSet))
  (var walk
       (fn [value depth]
         (when (== (typeof value) "function")
           (return "[function]"))
         (when (or (== value nil) (not= (typeof value) "object"))
           (return value))
         (when (> depth 8)
           (return "[depth limit]"))
         (when (. seen (has value))
           (return "[circular]"))
         (. seen (add value))
         (when (Array.isArray value)
           (return (. value (map (fn [item] (return (walk item (+ depth 1))))))))
         (var result {})
         (for:array
          [key (Object.keys value)]
          (:= (. result [key])
              (:? (. (new RegExp "password|token|secret|authorization|cookie|api.?key|^key$" "i")
                     (test key))
                  "[redacted]"
                  (walk (. value [key]) (+ depth 1)))))
         (return result)))
  (return (walk input 0)))

(defn.js ^{:- [:async]} sign-in
  [client account]
  (when (not client)
    (throw (new Error "Supply the application's Supabase client.")))
  (when (or (not account) (not account.password))
    (throw (new Error "This account has no password configured.")))
  (var result
       (await (. client.auth
                 (signInWithPassword {"email" account.email "password" account.password}))))
  (when result.error
    (throw result.error))
  (return result))

(defn.js ^{:- [:async]} invoke
  [action input]
  (when (not action)
    (throw (new Error "This devtool action is not configured.")))
  (var result (await (action input)))
  (when (and result result.error)
    (throw result.error))
  (return result))

(defn.js use-runtime
  [options]
  (var [session setSession] (r/useState nil))
  (var [busy setBusy] (r/useState false))
  (var [error setError] (r/useState nil))
  (var [debugContent setDebugContent] (r/useState nil))
  (var [sharedWorker setSharedWorker] (r/useState (or options.sharedWorker nil)))
  (var client options.client)
  (r/useEffect (fn []
                 (var active true)
                 (var subscription nil)
                 (when client
                   (:= subscription
                       (. (. client.auth
                             (onAuthStateChange (fn [_ value]
                                                  (when active
                                                    (setSession value)))))
                          data
                          subscription))
                   (. (. client.auth (getSession))
                      (then (fn [result]
                              (when active
                                (if result.error
                                  (setError result.error.message)
                                  (setSession result.data.session)))))
                      (catch (fn [failure]
                               (when active
                                 (setError failure.message))))))
                 (return (fn []
                           (:= active false)
                           (when subscription
                             (. subscription (unsubscribe))))))
               [client])
  (r/useEffect (fn [] (setSharedWorker (or options.sharedWorker nil))) [options.sharedWorker])
  (var toast (or options.toast {"show" (fn [title details] (console.info title details))}))
  (var run
       (async (fn [action input]
                (setBusy true)
                (setError nil)
                (try (var result (await (-/invoke action input)))
                     (return result)
                     (catch failure (setError failure.message) (throw failure))
                     (finally (setBusy false))))))
  (var
   api
   {:capabilities {:createUser (not (not options.createUser))
                   :deleteUser (not (not options.deleteUser))}
    :queries (or options.queries {})
    :mutations
    {:sign-in {:mutateAsync
               (fn [account] (return (run (fn [value] (return (-/sign-in client value))) account)))}
     :sign-out {:mutateAsync
                (fn []
                  (return (run (fn []
                                 (when (not client)
                                   (throw (new Error "Supply the application's Supabase client.")))
                                 (return (. client.auth (signOut))))
                               nil)))}
     :create-testuser {:mutateAsync (fn [account] (return (run options.createUser account)))}
     :delete-testuser {:mutateAsync (fn [account] (return (run options.deleteUser account)))}}})
  (return
   {:controls {:session session
               :setSession setSession
               :busy busy
               :error error
               :color (:? session "$green8" "$color1")}
    :accounts (or options.accounts [])
    :themeConfigured (not (not options.theme))
    :toastConfigured (not (not options.toast))
    :api api
    :page {:toast toast}
    :theme (or options.theme {:current "light" :setCurrent (fn [])})
    :store (or options.store {})
    :ui (or options.ui {})
    :context (or options.context {})
    :actions (or options.actions [])
    :debugContent debugContent
    :setDebugContent setDebugContent
    :sharedWorker sharedWorker
    :setSharedWorker setSharedWorker}))
