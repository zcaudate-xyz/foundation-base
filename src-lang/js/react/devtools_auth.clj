(ns js.react.devtools-auth
  "Portable auth controls extracted from StatsTrade's FrameDevtool account workflow."
  (:require [lang.core :as l]))
(l/script :js
  {:import [["react" :as React]] :static {:export [signInWithAccount AuthPanel]}})

(defn.js ^{:- [:async]} signInWithAccount
  [signIn account]
  (when (not account.password)
    (throw (new Error "This account has no password configured.")))
  (var response (await (signIn account)))
  (when response.error
    (throw response.error))
  (return response))

(defn.js AuthPanel
  [props]
  (return
   (React.createElement
    "aside"
    {"className" "devtools" "aria-label" "Auth devtools"}
    (React.createElement
     "details"
     {}
     (React.createElement "summary" {} "Devtools · Auth")
     (React.createElement
      "p"
      {}
      (:? props.session (+ "Signed in as " props.session.user.email) "Signed out"))
     (. props.accounts
        (map (fn [account]
               (return (React.createElement "button"
                                            {"key" account.id
                                             "type" "button"
                                             "disabled" props.busy
                                             "onClick" (fn [] (props.signIn account))}
                                            (+ "Sign in as " account.handle))))))
     (:? props.session
         (React.createElement "button"
                              {"type" "button" "disabled" props.busy "onClick" props.signOut}
                              "Sign out")
         nil)
     (React.createElement "p" {"role" "status"} props.error)))))
