(ns app.manage.ui-model
  "Headless Substrate state model for the management frontend. Transport is injected."
  (:require [lang.core :as l] [app.manage.protocol :as protocol]))

(l/script :js
  {:require [[xt.substrate :as substrate] [xt.substrate.page-core :as page]]
   :static {:export [open refresh snapshot selectTarget selected affected command close]}})

(def.js operations (@! protocol/operations))

(defn.js open [transport]
  (var handle {"transport" transport "closed" false "sequence" 0 "revision" -1
               "value" nil "selected" nil "error" nil
               "node" (substrate/node-create {"id" (+ "manager-" (crypto.randomUUID))})})
  (page/group-add-attach handle.node "manager" "project"
    {"snapshot" {"handler" (fn [_] (return handle.value))
                  "defaults" {"args" [] "output" nil}}})
  (return handle))

(defn.js snapshot [handle]
  (when handle.closed (return nil))
  (return (page/model-get-output handle.node "manager" "project" "snapshot")))

(defn.js ^{:- [:async]} refresh [handle]
  (when handle.closed (throw (new Error "Management model is closed")))
  (var sequence (+ handle.sequence 1))
  (:= handle.sequence sequence)
  (try
    (var result (await (. handle.transport (state))))
    (when (or handle.closed (not (== sequence handle.sequence)) (< result.revision handle.revision))
      (return (-/snapshot handle)))
    (:= handle.value result) (:= handle.revision result.revision) (:= handle.error nil)
    (when (not (. result.project.targets (some (fn [target] (return (== target.id handle.selected))))))
      (:= handle.selected (and (. result.project.targets [0]) (. result.project.targets [0] ["id"]))))
    (await (page/model-refresh handle.node "manager" "project" "snapshot" {} nil))
    (return (-/snapshot handle))
    (catch error
      (when (and (not handle.closed) (== sequence handle.sequence)) (:= handle.error error.message))
      (throw error))))

(defn.js selectTarget [handle id]
  (var data (-/snapshot handle))
  (when (or (not data) (not (. data.project.targets (some (fn [target] (return (== target.id id)))))))
    (throw (new Error "Unknown target")))
  (:= handle.selected id)
  (return id))

(defn.js selected [handle]
  (var data (-/snapshot handle))
  (return (and data (. data.project.targets (find (fn [target] (return (== target.id handle.selected))))))))

(defn.js affected [handle]
  (var data (-/snapshot handle))
  (when (not data) (return []))
  (var ids (new Set [handle.selected]))
  (var changed true)
  (while changed
    (:= changed false)
    (. data.project.targets (forEach (fn [target]
      (when (and (not (. ids (has target.id))) (. target.depends (some (fn [id] (return (. ids (has id)))))))
        (. ids (add target.id)) (:= changed true))))))
  (return (. (Array.from ids) (sort))))

(defn.js ^{:- [:async]} command [handle operation options]
  (when handle.closed (throw (new Error "Management model is closed")))
  (when (not (. -/operations (includes operation))) (throw (new Error "Unknown management operation")))
  (var result (await (. handle.transport (action (Object.assign {} (or options {}) {"operation" operation})))))
  (await (-/refresh handle))
  (return result))

(defn.js close [handle]
  (when (not handle.closed)
    (:= handle.closed true)
    (page/group-remove handle.node "manager" "project"))
  (return true))
