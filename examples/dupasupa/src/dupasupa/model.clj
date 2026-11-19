(ns dupasupa.model
  (:require [lang.core :as l]
            [dupasupa.groups :as groups]))

(l/script :js
  {:require [[dupasupa.bindings :as bindings]
             [app.manage.page-runtime :as runtime]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page]]
   :static {:export [open loadRows save create close snapshot]}})

(def.js GROUP_ID (. (runtime/definition bindings/DEFINITIONS "campaigns") group_id))

(defn.js ensureOpen
  [handle]
  (when handle.closed
    (throw (new Error "Campaign page is closed."))))

(defn.js open
  [transport]
  (var node (substrate/node-create {"id" (crypto.randomUUID)}))
  (page/group-add-attach node
                         "app"
                         -/GROUP_ID
                         {"rows" {"handler" (fn [_context] (return (. transport (list))))
                                  "defaults" {"args" [] "output" []}}})
  (return {"node" node "transport" transport "closed" false}))

(defn.js snapshot
  [handle]
  (-/ensureOpen handle)
  (return (page/model-get-output handle.node "app" -/GROUP_ID "rows")))

(defn.js ^{:- [:async]} loadRows
  [handle]
  (-/ensureOpen handle)
  (var acc (await (page/model-refresh handle.node "app" -/GROUP_ID "rows" {} nil)))
  (when acc.error
    (var failed
         (. (Object.values acc)
            (find (fn [value] (return (and (Array.isArray value) (== (. value [2]) true)))))))
    (var error (:? failed (. failed [1]) nil))
    (throw (or error (new Error "Could not load campaigns."))))
  (-/ensureOpen handle)
  (return (-/snapshot handle)))

(defn.js ^{:- [:async]} save
  [handle id title]
  (-/ensureOpen handle)
  (await (. handle.transport (call "save-campaign" {"i_id" id "i_title" title})))
  (return (await (-/loadRows handle))))

(defn.js ^{:- [:async]} create
  [handle title]
  (-/ensureOpen handle)
  (var created (await (. handle.transport (call "create-owned-campaign" {"i_title" title}))))
  (await (-/loadRows handle))
  (return created))

(defn.js close
  [handle]
  (when (not handle.closed)
    (:= handle.closed true)
    (page/group-remove handle.node "app" -/GROUP_ID))
  (return true))
