(ns lang-demos.shared-log.entries
  (:require [lang.core :as l]
            [postgres.core :as pg]
            [postgres.gen.bind-macro :as pg-bind]
            [postgres.gen.rpc :as pg-rpc]
            [postgres.gen.template-code :as pg-template]
            [postgres.sample.scratch-v0]))

(defn source-entry
  [id]
  (or (some (fn [entry]
              (when (= id (:id entry))
                entry))
            (l/module-entries :postgres
                              'postgres.sample.scratch-v0
                              identity))
      (throw (ex-info "Missing scratch_v0 PostgreSQL entry"
                      {:id id}))))

(defn view-entry
  [id entry-key]
  (get-in (into {} (pg-template/view-entries
                    ['postgres.sample.scratch-v0]))
          [id entry-key]))

(l/script :js {})

(def.js Schema
  (@! (postgres.gen.bind-macro/bind-schema
       (:schema (postgres.core/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (postgres.gen.bind-macro/bind-app
       (postgres.core/app "scratch_v0"))))

(def.js LogAppendRPC
  (@! (postgres.gen.rpc/rpc-client-spec
       (lang-demos.shared-log.entries/source-entry 'log-append-public))))

(def.js LogDeleteRPC
  (@! (postgres.gen.rpc/rpc-client-spec
       (lang-demos.shared-log.entries/source-entry 'log-delete-public))))

(def.js LogSelectEntry
  (@! (lang-demos.shared-log.entries/view-entry
       'postgres.sample.scratch-v0/log-all
       :select-entry)))

(def.js LogReturnEntry
  (@! (lang-demos.shared-log.entries/view-entry
       'postgres.sample.scratch-v0/log-default
       :return-entry)))

(def.js LogQueries
  {"list" {"table" "Log"
           "select_entry" -/LogSelectEntry}
   "data" {"table" "Log"
           "return_entry" -/LogReturnEntry}})
