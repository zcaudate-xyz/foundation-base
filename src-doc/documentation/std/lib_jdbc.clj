(ns documentation.lib-jdbc
  (:use code.test))

[[:hero {:title "lib.jdbc"
         :subtitle "JDBC connections, prepared statements, eager queries, and cursors"
         :lead "Use database specifications, URLs, data sources, or existing connections through one protocol-oriented JDBC API."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"`lib.jdbc` normalizes connection inputs and query forms. Queries may be SQL strings, SQL vectors with parameters, or prepared statements. Results can be fetched eagerly or streamed through an explicit cursor."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Open a connection"}]]

(comment
  (require '[lib.jdbc :as jdbc])

  (def dbspec
    {:vendor "postgresql"
     :host "localhost"
     :port 5432
     :name "application"
     :user "application"})

  (with-open [connection (jdbc/connection dbspec)]
    (jdbc/fetch-one connection
                    ["select ? as value" 42])))

[[:section {:title "Execute and fetch"}]]

(comment
  (with-open [connection (jdbc/connection dbspec)]
    (jdbc/execute connection
                  ["insert into events(kind) values (?)" "login"])
    (jdbc/fetch connection
                ["select * from events where kind = ?" "login"])))

[[:section {:title "Stream large results"}]]

"`fetch-lazy` returns a cursor. Keep the cursor inside `with-open` so its statement and result set are released even when iteration stops early."

(comment
  (with-open [connection (jdbc/connection dbspec)
              cursor (jdbc/fetch-lazy connection
                                      "select * from events")]
    (doseq [row (take 100 (jdbc/cursor->lazyseq cursor))]
      (process-row row))))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "lib.jdbc"}]]
[[:api {:namespace "lib.jdbc.constants"}]]
[[:api {:namespace "lib.jdbc.meta"}]]
[[:api {:namespace "lib.jdbc.protocol"}]]
[[:api {:namespace "lib.jdbc.resultset"}]]
[[:api {:namespace "lib.jdbc.types"}]]
