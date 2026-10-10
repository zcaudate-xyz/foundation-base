(ns lang-demos.js-007-ext-page.generated.core
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.event.base-model :as event-model]
             [js.react :as r]
             [js.react.ext-model :as ext-model]]})

(defn.js useLogState
  "Keeps the shared log view and append lifecycle independent of its UI/runtime."
  [#{view append}]
  (var model-output (ext-model/listenModelOutput
                     view
                     ["output" "pending" "error"]
                     {}
                     nil))
  (var [message setMessage] (r/local ""))
  (var [submitting setSubmitting] (r/local (== 1 0)))
  (var [notice setNotice] (r/local ""))
  (var [noticeError setNoticeError] (r/local (== 1 0)))
  (var rows (or (. model-output current) []))
  (var pending (or (event-model/is-pending view nil)
                   (. model-output pending)))
  (var failed (event-model/is-errored view nil))
  (var appendEntry
       (fn []
         (var clean-message (. message (trim)))
         (when (and (not submitting)
                    (> (. clean-message length) 0))
           (when (> (. clean-message length) 500)
             (setNotice "Messages are limited to 500 characters.")
             (setNoticeError true)
             (return))
           (setSubmitting true)
           (setNotice "")
           (setNoticeError false)
           (. (append clean-message)
              (then
               (fn [_]
                 (setMessage "")
                 (setNotice "Entry added to the shared log.")
                 (return (ext-model/refresh-model view {}))))
              (catch
               (fn [error]
                 (setNotice (or (. error message)
                                "Unable to append this entry."))
                 (setNoticeError true)))
              (finally
               (fn [] (setSubmitting false)))))))
  (return {"message" message
           "setMessage" setMessage
           "submitting" submitting
           "notice" notice
           "noticeError" noticeError
           "appendEntry" appendEntry
           "rows" rows
           "pending" pending
           "failed" failed}))
