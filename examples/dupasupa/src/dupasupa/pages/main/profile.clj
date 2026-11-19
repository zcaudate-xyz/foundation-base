(ns dupasupa.pages.main.profile
  (:require [lang.core :as l]
            [dupasupa.seed :as seed]))
(def SITE-MAP-REQUIREMENTS
  {:tables [seed/User] :rpcs [seed/ret-profile seed/save-profile] :groups []})
(l/script :js
  {:import [["react" :as React] ["./auth-context.js" :as auth]
            ["../../lib/bindings.mjs" :as bindings]]
   :static {:export [Profile]}})

(defn.js ProfileEditor
  [props]
  (var [handle setHandle] (React.useState props.profile.handle))
  (var [name setName] (React.useState props.profile.display_name))
  (var [bio setBio] (React.useState props.profile.bio))
  (var [saved setSaved] (React.useState props.profile))
  (var [status setStatus] (React.useState ""))
  (var [busy setBusy] (React.useState false))
  (React.useEffect (fn []
                     (when props.inspect
                       (props.inspect {"store" {"profile" saved}
                                       "queries" {"profile"
                                                  {"output" saved "busy" busy "status" status}}})
                       (return (fn [] (props.inspect {})))))
                   [saved busy status])
  (var submit
       (async (fn [event]
                (. event (preventDefault))
                (when busy
                  (return))
                (setBusy true)
                (setStatus "Saving…")
                (try (var value
                          (await (props.save {"handle" handle "display_name" name "bio" bio})))
                     (setSaved value)
                     (setHandle value.handle)
                     (setName value.display_name)
                     (setBio value.bio)
                     (setStatus "Saved.")
                     (catch error (setStatus error.message))
                     (finally (setBusy false))))))
  (return
   (React.createElement
    "section"
    {"className" "campaign"}
    (React.createElement "h2" {"data-profile-name" true} (or saved.display_name saved.handle))
    (React.createElement "p" {"className" "profile-email"} props.email)
    (React.createElement
     "form"
     {"onSubmit" submit "className" "profile-form"}
     (React.createElement "label" {}
                          "Display name" (React.createElement
                                          "input"
                                          {"name" "display_name"
                                           "value" name
                                           "required" true
                                           "disabled" busy
                                           "onChange" (fn [event] (setName event.target.value))}))
     (React.createElement "label" {}
                          "Handle" (React.createElement
                                    "input"
                                    {"name" "handle"
                                     "value" handle
                                     "required" true
                                     "disabled" busy
                                     "onChange" (fn [event] (setHandle event.target.value))}))
     (React.createElement "label" {}
                          "Bio" (React.createElement "textarea"
                                                     {"name" "bio"
                                                      "value" bio
                                                      "rows" 4
                                                      "disabled" busy
                                                      "onChange" (fn [event]
                                                                   (setBio event.target.value))}))
     (React.createElement "button"
                          {"type" "submit" "disabled" busy}
                          (:? busy "Saving…" "Save profile")))
    (React.createElement "p" {"role" "status" "aria-live" "polite"} status))))

(defn.js Profile
  [props]
  (var account (auth.useAuth))
  (var [profile setProfile] (React.useState nil))
  (var [error setError] (React.useState ""))
  (var session (:? account account.session nil))
  (var request (or props.request (:? account account.request nil)))
  (React.useEffect (fn []
                     (var active true)
                     (when (or session props.request)
                       (. ((fn []
                             (var bound (bindings.request "profile" "model" "profile" {}))
                             (return (request bound.path bound.method bound.body))))
                          (then (fn [value]
                                  (when active
                                    (setProfile value))))
                          (catch (fn [failure]
                                   (when active
                                     (setError failure.message))))))
                     (return (fn [] (:= active false))))
                   [])
  (return
   (React.createElement
    "main"
    {}
    (React.createElement "header"
                         {}
                         (React.createElement "p" {"className" "eyebrow"} "DUPASUPA")
                         (React.createElement "h1" {} "Your profile"))
    (:? (and (not session) (not props.request))
        (React.createElement "p" {} "Sign in using Devtools to view your profile.")
        :else
        (:? error
            (React.createElement "p" {"role" "alert"} error)
            :else
            (:? profile
                (React.createElement
                 -/ProfileEditor
                 {"profile" profile
                  "inspect" (:? account account.inspect nil)
                  "email" (:? session session.user.email "")
                  "save" (fn [values]
                           (var bound (bindings.request "profile" "action" "save" values))
                           (return (request bound.path bound.method bound.body)))})
                (React.createElement "p" {"role" "status"} "Loading profile…")))))))
