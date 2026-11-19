(ns dupasupa.pages.main.campaigns
  (:require [lang.core :as l]
            [dupasupa.seed :as seed]
            [dupasupa.groups :as groups]))

(def SITE-MAP-REQUIREMENTS
  {:tables [seed/Campaign] :rpcs [seed/list-campaigns seed/save-campaign] :groups [groups/CAMPAIGNS]})

(l/script :js
  {:static {:export [renderCampaigns mount]}})

(defn.js escapeText
  [text]
  (return (. (String text)
             (replaceAll "&" "&amp;")
             (replaceAll "<" "&lt;")
             (replaceAll ">" "&gt;")
             (replaceAll "\"" "&quot;"))))

(defn.js renderCampaigns
  [rows]
  (return (+ "<h1>Dupasupa</h1><p>Campaigns · local demonstration</p>"
             (. (. rows
                   (map (fn [row]
                          (return (+ "<form data-id=\""
                                     (-/escapeText row.id)
                                     "\"><label>Campaign title"
                                     "<input name=\"title\" required value=\""
                                     (-/escapeText row.title)
                                     "\"></label><button>Save</button></form>")))))
                (join ""))
             "<p data-status role=\"status\" aria-live=\"polite\"></p>")))

(defn.js setStatus
  [root message]
  (var status (. root (querySelector "[data-status]")))
  (when status
    (:= status.textContent message)))

(defn.js mount
  [root]
  (var worker (new Worker "./worker.mjs" {"type" "module"}))
  (var closed false)
  (:= root.innerHTML "<p data-status role=\"status\" aria-live=\"polite\">Loading campaigns…</p>")
  (var onMessage
       (fn [event]
         (when closed
           (return))
         (if event.data.error
           (-/setStatus root event.data.error)
           (do (:= root.innerHTML (-/renderCampaigns event.data.result))
               (-/setStatus root (:? (== event.data.request "save") "Saved." ""))))))
  (var onSubmit
       (fn [event]
         (. event (preventDefault))
         (var form event.target)
         (-/setStatus root "Saving…")
         (. worker
            (postMessage {"action" "save"
                          "request" "save"
                          "id" form.dataset.id
                          "title" (. (new FormData form) (get "title"))}))))
  (. worker (addEventListener "message" onMessage))
  (. worker
     (addEventListener "error"
                       (fn [event]
                         (when (not closed)
                           (-/setStatus root (or event.message "Campaign worker failed."))))))
  (. root (addEventListener "submit" onSubmit))
  (. worker (postMessage {"action" "load" "request" "load"}))
  (return (fn []
            (:= closed true)
            (. root (removeEventListener "submit" onSubmit))
            (. worker (terminate)))))

(defrun.js boot
  (when (not (== (typeof document) "undefined"))
    (-/mount (document.getElementById "app"))))
