(ns documentation.lib-postgres
  (:use code.test))

[[:hero {:title "lib.postgres"
         :subtitle "PostgreSQL runtime and connection lifecycle"
         :lead "Coordinate PostgreSQL connection startup, readiness, temporary database setup, and shutdown."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The runtime functions operate on a map containing connection settings, an instance atom, and notification state. Optional settings add setup and teardown hooks."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Start a runtime"}]]

(comment
  (require '[lib.postgres :as postgres])

  (def runtime
    {:host "localhost"
     :port 5432
     :dbname "application"
     :instance (atom nil)
     :notifications (atom {})})

  (postgres/start-pg runtime))

[[:section {:title "Inspect and close"}]]

(comment
  @(:instance runtime)
  (postgres/stop-pg runtime))

[[:section {:title "Lifecycle options"}]]

"Temporary database and external runtime options can be added to the same map. The start function waits for availability before creating the connection instance."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "lib.postgres"}]]
[[:api {:namespace "lib.postgres.connection"}]]
