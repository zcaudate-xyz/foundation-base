(ns dupasupa.pages.main.react-campaigns
  (:require [lang.core :as l]
            [dupasupa.seed :as seed]
            [dupasupa.groups :as groups]))
(def SITE-MAP-REQUIREMENTS
  {:tables [seed/Campaign]
   :rpcs [seed/list-owned-campaigns seed/create-owned-campaign seed/save-owned-campaign]
   :groups [groups/CAMPAIGNS]})
(l/script :js
  {:import [["react" :as React] ["./auth-context.js" :as auth] ["../../lib/model.mjs" :as model]
            ["../../lib/bindings.mjs" :as bindings]]
   :static {:export [Campaigns]}})

(defn.js CampaignForm
  [props]
  (var [title setTitle] (React.useState props.row.title))
  (var [status setStatus] (React.useState ""))
  (var [busy setBusy] (React.useState false))
  (React.useEffect (fn [] (setTitle props.row.title)) [props.row.title])
  (var submit
       (async (fn [event]
                (. event (preventDefault))
                (when busy
                  (return))
                (setBusy true)
                (setStatus "Saving…")
                (try (await (props.save props.row.id title))
                     (setStatus "Saved.")
                     (catch error (setStatus error.message))
                     (finally (setBusy false))))))
  (return
   (React.createElement
    "section"
    {"className" "campaign"}
    (React.createElement "h2" {"data-saved-title" true} props.row.title)
    (React.createElement
     "form"
     {"onSubmit" submit}
     (React.createElement "label" {}
                          "Campaign title" (React.createElement "input"
                                                                {"name" "title"
                                                                 "required" true
                                                                 "disabled" busy
                                                                 "value" title
                                                                 "onChange"
                                                                 (fn [event]
                                                                   (setTitle event.target.value))}))
     (React.createElement "button" {"type" "submit" "disabled" busy} (:? busy "Saving…" "Save")))
    (React.createElement "p" {"role" "status" "aria-live" "polite"} status))))

(defn.js CampaignCreate
  [props]
  (var [title setTitle] (React.useState ""))
  (var [busy setBusy] (React.useState false))
  (var [status setStatus] (React.useState ""))
  (var submit
       (async (fn [event]
                (. event (preventDefault))
                (when busy
                  (return))
                (setBusy true)
                (setStatus "Creating…")
                (try (await (props.create title))
                     (setTitle "")
                     (setStatus "Campaign created.")
                     (catch error (setStatus error.message))
                     (finally (setBusy false))))))
  (return
   (React.createElement "section"
                        {"className" "campaign"}
                        (React.createElement "h2" {} "New campaign")
                        (React.createElement
                         "form"
                         {"onSubmit" submit "aria-label" "Create campaign"}
                         (React.createElement "label" {}
                                              "New campaign title"
                                              (React.createElement
                                               "input"
                                               {"name" "new-title"
                                                "value" title
                                                "required" true
                                                "maxLength" 120
                                                "disabled" busy
                                                "onChange" (fn [event]
                                                             (setTitle event.target.value))}))
                         (React.createElement "button"
                                              {"type" "submit" "disabled" busy}
                                              (:? busy "Creating…" "Create campaign")))
                        (React.createElement "p" {"role" "status" "aria-live" "polite"} status))))

(defn.js Campaigns
  [props]
  (var account (auth.useAuth))
  (var allowed (or props.transport (and account account.session)))
  (var [rows setRows] (React.useState []))
  (var [error setError] (React.useState ""))
  (var [loading setLoading] (React.useState true))
  (var handle (React.useRef nil))
  (React.useEffect
   (fn []
     (when (not allowed)
       (return))
     (var active true)
     (var transport
          (or props.transport
              {"list" (fn []
                        (var bound (bindings.request "campaigns" "model" "rows" {}))
                        (return (account.request bound.path bound.method bound.body)))
               "call" (fn [operation values]
                        (var request
                             (bindings.request
                              "campaigns"
                              "action"
                              (:? (== operation "create-owned-campaign") "create" "save")
                              values))
                        (return (account.request request.path request.method request.body)))}))
     (var owner (model.open transport))
     (:= handle.current owner)
     (. (model.loadRows owner)
        (then (fn [value]
                (when active
                  (setRows value)
                  (setLoading false))))
        (catch (fn [failure]
                 (when active
                   (setError failure.message)
                   (setLoading false)))))
     (return (fn [] (:= active false) (model.close owner) (:= handle.current nil))))
   [])
  (React.useEffect (fn []
                     (when (and account account.inspect)
                       (account.inspect
                        {"store" {"campaigns" handle.current}
                         "queries" {"campaigns" {"output" rows "loading" loading "error" error}}})
                       (return (fn [] (account.inspect {})))))
                   [rows loading error])
  (var create
       (async (fn [title]
                (var created (await (model.create handle.current title)))
                (setRows (model.snapshot handle.current))
                (setError "")
                (return created))))
  (var refresh
       (async (fn []
                (setLoading true)
                (setError "")
                (try (setRows (await (model.loadRows handle.current)))
                     (catch failure (setError failure.message))
                     (finally (setLoading false))))))
  (var save
       (async (fn [id title]
                (var value (await (model.save handle.current id title)))
                (setRows value)
                (return value))))
  (when (not allowed)
    (return (React.createElement
             "main"
             {}
             (React.createElement "h1" {} "Campaigns")
             (React.createElement "p" {} "Sign in using Devtools to see your campaigns."))))
  (return
   (React.createElement
    "main"
    {}
    (React.createElement "header"
                         {}
                         (React.createElement "p" {"className" "eyebrow"} "DUPASUPA")
                         (React.createElement "h1" {} "Campaigns")
                         (React.createElement "p" {} "Saved titles are stored in PostgreSQL."))
    (:? loading (React.createElement "p" {"role" "status"} "Loading campaigns…") nil)
    (:? error (React.createElement "p" {"role" "alert"} error) nil)
    (:? (and (not loading) (not error) (== rows.length 0))
        (React.createElement "p" {} "No campaigns yet. Create your first campaign below.")
        nil)
    (. rows
       (map (fn [row]
              (return (React.createElement -/CampaignForm {"key" row.id "row" row "save" save})))))
    (:? (not loading) (React.createElement -/CampaignCreate {"create" create}) nil)
    (React.createElement "button"
                         {"type" "button" "onClick" refresh "disabled" loading}
                         (:? loading "Refreshing…" "Refresh campaigns")))))
