(ns lang-demos.shared-log.react
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.event.base-model :as event-model]
             [js.react :as r]
             [js.react.ext-model :as ext-model]]})

(defn.js useLogState
  "Keeps log state shared and refreshes through the view's backing node."
  [#{view append remove}]
  (var model-output (ext-model/listenModelOutput
                     view
                     ["output" "pending" "error"]
                     {}
                     nil))
  (var [message setMessage] (r/local ""))
  (var [submitting setSubmitting] (r/local (== 1 0)))
  (var [notice setNotice] (r/local ""))
  (var [noticeError setNoticeError] (r/local (== 1 0)))
  (var [deletingId setDeletingId] (r/local nil))
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
  (var deleteEntry
       (fn [entry-id]
         (when (and (not deletingId) entry-id)
           (setDeletingId entry-id)
           (setNotice (+ "Deleting entry " entry-id "..."))
           (setNoticeError false)
           (. (remove entry-id)
              (then
               (fn [_]
                 (setNotice (+ "Deleted entry " entry-id "."))
                 (return (ext-model/refresh-model view {}))))
              (catch
               (fn [error]
                 (setNotice (or (. error message)
                                "Unable to delete this entry."))
                 (setNoticeError true)))
              (finally
               (fn [] (setDeletingId nil)))))))
  (return {"message" message
           "setMessage" setMessage
           "submitting" submitting
           "notice" notice
           "noticeError" noticeError
           "appendEntry" appendEntry
           "deletingId" deletingId
           "deleteEntry" deleteEntry
           "rows" rows
           "pending" pending
           "failed" failed}))
