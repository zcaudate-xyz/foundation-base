(ns xt.db.helpers.neon-http
  (:require [lang.core :as l]))
(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})
(defn.xt await-result
  [task]
  (var start (xt/x:now-ms))
  (while (and (xt/x:is-object? task) (== "xt.promise" (xt/x:get-key task "::" nil)))
    (when (> (- (xt/x:now-ms) start) 5000) (xt/x:err "HTTP fixture timed out"))
    (cond (== "rejected" (xt/x:get-key task "status" nil))
          (throw (xt/x:get-key task "error" nil))
          (== "resolved" (xt/x:get-key task "status" nil))
          (:= task (xt/x:get-key task "value" nil))))
  (return task))
(l/script :js)
(defn.js await-result
  [task]
  (return task))
(l/script :ruby)
(defn.rb await-result
  [task]
  (while (. task (respond_to? "value!"))
    (:= task (:- "task.value!")))
  (return task))
