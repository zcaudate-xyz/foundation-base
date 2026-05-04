(ns xt.db
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.instance :as instance]]})

(def.xt unsupported-op instance/unsupported-op)
(def.xt IMPL instance/IMPL)
(def.xt get-dbtype instance/get-dbtype)
(def.xt process-event instance/process-event)
(def.xt process-triggers instance/process-triggers)
(def.xt add-trigger instance/add-trigger)
(def.xt remove-trigger instance/remove-trigger)
(def.xt db-trigger instance/db-trigger)
(def.xt db-create instance/db-create)
(def.xt queue-event instance/queue-event)
(def.xt sync-event instance/sync-event)
(def.xt db-exec-sync instance/db-exec-sync)
(def.xt db-pull-sync instance/db-pull-sync)
(def.xt db-delete-sync instance/db-delete-sync)
(def.xt db-clear instance/db-clear)
(def.xt add-view-trigger instance/add-view-trigger)
