(ns dupasupa.seed
  "Database-owned seed exports; no hand-maintained fixture declarations."
  (:require [app.manage.postgres :as postgres]
            [dupasupa.config :as config]
            [dupasupa.db]))

(postgres/intern-seed! *ns* config/DATABASE)
